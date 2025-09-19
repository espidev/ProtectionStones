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

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.*;
import dev.espi.protectionstones.utils.WGMerge;
import dev.espi.protectionstones.utils.WGUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class ArgMerge implements PSCommandArg {
    @Override
    public List<String> getNames() {
        return Arrays.asList("merge");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public List<String> getPermissionsToExecute() {
        return Arrays.asList("protectionstones.merge");
    }

    @Override
    public HashMap<String, Boolean> getRegisteredFlags() {
        return null;
    }

    public static List<Component> getGUI(Player p, PSRegion r) {
        return r.getMergeableRegions(p).stream()
                .map(psr -> {
                    // Base label: > id
                    Component base = Component.text()
                            .append(Component.text("> ").color(NamedTextColor.AQUA))
                            .append(Component.text(psr.getId(), NamedTextColor.WHITE))
                            .build();

                    // Optional name
                    if (psr.getName() != null) {
                        base = base.append(Component.text(" (" + psr.getName() + ")", NamedTextColor.GRAY));
                    }

                    // Region type
                    base = base.append(Component.text(" (" + psr.getTypeOptions().alias + ")", NamedTextColor.GRAY));

                    // Add click + hover events
                    String cmd = "/" + ProtectionStones.getInstance().getConfigOptions().base_command
                            + " merge " + r.getId() + " " + psr.getId();

                    base = base.clickEvent(ClickEvent.runCommand(cmd));

                    // Hover: use your PSL message (converted to Component already)
                    base = base.hoverEvent(HoverEvent.showText(
                            PSL.MERGE_CLICK_TO_MERGE.replaceAll(Map.of("%region%", psr.getId()))
                    ));

                    return base;
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args, HashMap<String, String> flags) {
        if (!s.hasPermission("protectionstones.merge"))
            return PSL.msg(s, PSL.NO_PERMISSION_MERGE.msg());

        if (!ProtectionStones.getInstance().getConfigOptions().allowMergingRegions)
            return PSL.msg(s, PSL.MERGE_DISABLED.msg());

        Player p = (Player) s;
        if (args.length == 1) { // GUI

            PSRegion r = PSRegion.fromLocationGroup(p.getLocation());
            if (r == null)
                return PSL.msg(s, PSL.NOT_IN_REGION.msg());

            if (r.getTypeOptions() == null) {
                PSL.msg(p,
                        Component.text("This region is problematic, and the block type (", NamedTextColor.RED)
                                .append(Component.text(r.getType(), NamedTextColor.AQUA))
                                .append(Component.text(") is not configured. Please contact an administrator.", NamedTextColor.RED))
                );
                Bukkit.getLogger().info(ChatColor.RED + "This region is problematic, and the block type (" + r.getType() + ") is not configured.");
                return true;
            }

            if (!r.getTypeOptions().allowMerging)
                return PSL.msg(s, PSL.MERGE_NOT_ALLOWED.msg());

            List<Component> components = getGUI(p, r);
            if (components.isEmpty()) {
                PSL.msg(p, PSL.MERGE_NO_REGIONS.msg());
            } else {
                PSL.msg(p, Component.empty());
                PSL.msg(p, PSL.MERGE_HEADER.replaceAll(Map.of("%region%", r.getId())));
                PSL.msg(p, PSL.MERGE_WARNING.msg());
                for (Component tc : components) {
                    PSL.msg(p, tc);
                }

                // send empty line again
                PSL.msg(p, Component.empty());
            }

        } else if (args.length == 3) { // /ps merge [region] [root]
            RegionManager rm = WGUtils.getRegionManagerWithPlayer(p);
            ProtectedRegion region = rm.getRegion(args[1]), root = rm.getRegion(args[2]);
            LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(p);

            if (!ProtectionStones.isPSRegion(region) || !ProtectionStones.isPSRegion(root))
                return PSL.msg(p, PSL.MULTI_REGION_DOES_NOT_EXIST.msg());

            if (!p.hasPermission("protectionstones.admin") && (!region.isOwner(lp) || !root.isOwner(lp)))
                return PSL.msg(p, PSL.NO_ACCESS.msg());

            // check if region is actually overlapping the region
            var overlappingRegionIds = WGUtils.findOverlapOrAdjacentRegions(root, rm, p.getWorld()).stream().map(ProtectedRegion::getId).collect(Collectors.toList());
            if (!overlappingRegionIds.contains(region.getId()))
                return PSL.msg(p, PSL.REGION_NOT_OVERLAPPING.msg());

            // check if merging is allowed in config
            PSRegion aRegion = PSRegion.fromWGRegion(p.getWorld(), region), aRoot = PSRegion.fromWGRegion(p.getWorld(), root);
            if (!aRegion.getTypeOptions().allowMerging || !aRoot.getTypeOptions().allowMerging)
                return PSL.msg(p, PSL.MERGE_NOT_ALLOWED.msg());

            // check if the region types allow for it
            if (!WGUtils.canMergeRegionTypes(aRegion.getTypeOptions(), aRoot))
                return PSL.msg(p, PSL.MERGE_NOT_ALLOWED.msg());

            Bukkit.getScheduler().runTaskAsynchronously(ProtectionStones.getInstance(), () -> {
                try {
                    WGMerge.mergeRealRegions(p.getWorld(), rm, aRoot, Arrays.asList(aRegion, aRoot));
                } catch (WGMerge.RegionHoleException e) {
                    PSL.msg(p, PSL.NO_REGION_HOLES.msg());
                    return;
                } catch (WGMerge.RegionCannotMergeWhileRentedException e) {
                    PSL.msg(p, PSL.CANNOT_MERGE_RENTED_REGION.replace("%region%", e.getRentedRegion().getName() == null ? e.getRentedRegion().getId() : e.getRentedRegion().getName()));
                    return;
                }
                PSL.msg(p, PSL.MERGE_MERGED.msg());

                // show menu again if the new region still has overlapping regions
                Bukkit.getScheduler().runTask(ProtectionStones.getInstance(), () -> {
                    if (!getGUI(p, PSRegion.fromWGRegion(p.getWorld(), rm.getRegion(aRoot.getId()))).isEmpty()) {
                        Bukkit.dispatchCommand(p, ProtectionStones.getInstance().getConfigOptions().base_command + " merge");
                    }
                });
            });

        } else {
            PSL.msg(s, PSL.MERGE_HELP.msg());
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return null;
    }
}
