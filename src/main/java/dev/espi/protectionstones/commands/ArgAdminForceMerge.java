/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.espi.protectionstones.commands;

import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.FlagHandler;
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.utils.WGMerge;
import dev.espi.protectionstones.utils.WGUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.*;

public class ArgAdminForceMerge {

    private static Set<String> flags = new HashSet<>(Arrays.asList("no_member_match", "no_flag_match", "one_owner_match"));

    private static Map<Flag<?>, Object> getFlags(Map<Flag<?>, Object> flags) {
        Map<Flag<?>, Object> f = new HashMap<>(flags);
        f.remove(FlagHandler.PS_BLOCK_MATERIAL);
        f.remove(FlagHandler.PS_MERGED_REGIONS_TYPES);
        f.remove(FlagHandler.PS_MERGED_REGIONS);
        f.remove(FlagHandler.PS_NAME);
        f.remove(FlagHandler.PS_HOME);

        return f;
    }

    private static boolean areDomainsEqualByOne(DefaultDomain o1, DefaultDomain o2) {
        boolean ret = false;
        for (UUID uuid : o1.getUniqueIds()) {
            if (o2.contains(uuid)) ret = true;
        }
        for (UUID uuid : o2.getUniqueIds()) {
            if (o1.contains(uuid)) ret = true;
        }
        return ret;
    }

    private static boolean areDomainsEqual(DefaultDomain o1, DefaultDomain o2) {
        for (UUID uuid : o1.getUniqueIds()) {
            if (!o2.contains(uuid)) return false;
        }
        for (UUID uuid : o2.getUniqueIds()) {
            if (!o1.contains(uuid)) return false;
        }
        return true;
    }

    // /ps admin forcemerge [world]
    public static boolean argumentAdminForceMerge(CommandSender p, String[] args) {
        if (args.length < 3) {
            PSL.msg(p, ArgAdmin.getForceMergeHelp());
            return true;
        }

        String world = args[2];
        World w = Bukkit.getWorld(world);

        if (w == null) {
            PSL.msg(p, PSL.INVALID_WORLD.msg());
            return true;
        }

        Set<String> options = new HashSet<>();
        for (int i = 3; i < args.length; i++) {
            if (!flags.contains(args[i])) {
                PSL.msg(p, Component.text("Invalid option.", NamedTextColor.RED));
                return true;
            } else {
                options.add(args[i]);
            }
        }

        RegionManager rm = WGUtils.getRegionManagerWithWorld(Bukkit.getWorld(world));

        HashMap<String, String> idToGroup = new HashMap<>();
        HashMap<String, List<PSRegion>> groupToMembers = new HashMap<>();

        // loop over regions in world
        for (ProtectedRegion r : rm.getRegions().values()) {
            if (!ProtectionStones.isPSRegion(r)) continue;
            if (r.getParent() != null) continue;
            boolean merged = idToGroup.get(r.getId()) != null;

            Map<Flag<?>, Object> baseFlags = getFlags(r.getFlags()); // comparison flags

            PSRegion psr = PSRegion.fromWGRegion(w, r);

            // loop over overlapping regions
            for (ProtectedRegion rOverlap : rm.getApplicableRegions(r)) {
                if (!ProtectionStones.isPSRegion(rOverlap)) continue;
                if (rOverlap.getId().equals(r.getId())) continue;

                Map<Flag<?>, Object> mergeFlags = getFlags(rOverlap.getFlags()); // comparison flags

                // check if regions are roughly equal
                if (!options.contains("no_member_match") && !areDomainsEqual(rOverlap.getMembers(), r.getMembers())) continue;

                if (!options.contains("no_flag_match") && !baseFlags.equals(mergeFlags)) continue;

                if (!options.contains("one_owner_match") && !areDomainsEqual(rOverlap.getOwners(), r.getOwners())) continue;

                if (options.contains("one_owner_match") && !areDomainsEqualByOne(rOverlap.getOwners(), r.getOwners())) continue;

                if (rOverlap.getParent() != null) continue;

                // check groupings

                String rOverlapGroup = idToGroup.get(rOverlap.getId());

                if (merged) { // r is part of a group
                    String rGroup = idToGroup.get(r.getId());
                    if (rOverlapGroup == null) { // rOverlap not part of a group
                        idToGroup.put(rOverlap.getId(), rGroup);
                        groupToMembers.get(rGroup).add(PSRegion.fromWGRegion(w, rOverlap));
                    } else if (!rOverlapGroup.equals(rGroup)) { // rOverlap is part of a group (both are part of group)

                        for (PSRegion pr : groupToMembers.get(rOverlapGroup)) {
                            idToGroup.put(pr.getId(), rGroup);
                        }
                        groupToMembers.get(rGroup).addAll(groupToMembers.get(rOverlapGroup));
                        groupToMembers.remove(rOverlapGroup);
                    }
                } else { // r not part of group
                    if (rOverlapGroup == null) { // both are not part of group
                        idToGroup.put(r.getId(), r.getId());
                        idToGroup.put(rOverlap.getId(), r.getId());
                        groupToMembers.put(r.getId(), new ArrayList<>(Arrays.asList(psr, PSRegion.fromWGRegion(w, rOverlap))));
                    } else { // rOverlap is part of group
                        idToGroup.put(r.getId(), rOverlapGroup);
                        groupToMembers.get(rOverlapGroup).add(psr);
                    }
                    merged = true;
                }

            }
        }

        // actually do region merging
        for (String key : groupToMembers.keySet()) {
            PSRegion root = null;
            p.sendMessage(ChatColor.GRAY + "Merging these regions into " + key + ":");
            for (PSRegion r : groupToMembers.get(key)) {
                if (r.getId().equals(key)) root = r;
                p.sendMessage(ChatColor.GRAY + r.getId());
            }
            try {
                WGMerge.mergeRealRegions(w, rm, root, groupToMembers.get(key));
            } catch (WGMerge.RegionHoleException | WGMerge.RegionCannotMergeWhileRentedException e) {
                // TODO
            }
        }

        p.sendMessage(ChatColor.GRAY + "Done!");

        return true;
    }

    static List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 3) {
            List<String> l = new ArrayList<>();
            for (World w : Bukkit.getWorlds()) l.add(w.getName());

            return StringUtil.copyPartialMatches(args[2], l, new ArrayList<>());
        } else {
            return StringUtil.copyPartialMatches(args[args.length - 1], flags, new ArrayList<>());
        }
    }
}
