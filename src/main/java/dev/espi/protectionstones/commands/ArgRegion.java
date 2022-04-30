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

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.utils.UUIDCache;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class ArgRegion implements PSCommandArg {

    // /ps region

    @Override
    public List<String> getNames() {
        return Collections.singletonList("region");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public List<String> getPermissionsToExecute() {
        return Arrays.asList("protectionstones.region");
    }

    @Override
    public HashMap<String, Boolean> getRegisteredFlags() {
        return null;
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args, HashMap<String, String> flags) {
        Player p = (Player) s;
        RegionManager rgm = WGUtils.getRegionManagerWithPlayer(p);

        if (!p.hasPermission("protectionstones.region")) {
            PSL.msg(p, PSL.NO_PERMISSION_REGION.msg());
            return true;
        }

        if (args.length < 3) {
            PSL.msg(p, PSL.REGION_HELP.msg());
            return true;
        }

        if (!UUIDCache.containsName(args[2])) {
            PSL.msg(p, PSL.PLAYER_NOT_FOUND.msg());
            return true;
        }

        UUID playerUuid = UUIDCache.getUUIDFromName(args[2]);

        if (args[1].equalsIgnoreCase("list")) { // list player's regions
            StringBuilder regionMessage = new StringBuilder();
            boolean found = false;
            for (ProtectedRegion r : rgm.getRegions().values()) {
                if (ProtectionStones.isPSRegion(r) && r.getOwners().contains(playerUuid)) {
                    found = true;
                    regionMessage.append(r.getId()).append(", ");
                }
            }

            if (!found) {
                PSL.msg(p, PSL.REGION_NOT_FOUND_FOR_PLAYER.msg()
                        .replace("%player%", args[2]));
            } else {
                regionMessage = new StringBuilder(regionMessage.substring(0, regionMessage.length() - 2) + ".");
                PSL.msg(p, PSL.REGION_LIST.msg()
                        .replace("%player%", args[2])
                        .replace("%regions%", regionMessage));
            }

        } else if ((args[1].equalsIgnoreCase("remove")) || (args[1].equalsIgnoreCase("disown"))) {

            boolean found = false;
            for (ProtectedRegion r : rgm.getRegions().values()) {
                if (ProtectionStones.isPSRegion(r)) {

                    PSRegion psr = PSRegion.fromWGRegion(p.getWorld(), r);
                    if (psr.isOwner(playerUuid)) {
                        found = true;

                        // remove as owner
                        psr.removeOwner(playerUuid);

                        // remove region if empty and is "remove" mode
                        if (psr.getOwners().size() == 0 && args[1].equalsIgnoreCase("remove")) {
                            psr.deleteRegion(true);
                        }
                    }
                }
            }

            if (!found) {
                PSL.msg(p, PSL.REGION_NOT_FOUND_FOR_PLAYER.msg().replace("%player%", args[2]));
                return true;
            }

            if (args[1].equalsIgnoreCase("remove")) {
                PSL.msg(p, PSL.REGION_REMOVE.msg().replace("%player%", args[2]));
            } else if (args[1].equalsIgnoreCase("disown")) {
                PSL.msg(p, PSL.REGION_DISOWN.msg().replace("%player%", args[2]));
            }
        } else {
            PSL.msg(p, PSL.REGION_HELP.msg());
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return args.length == 2 ? StringUtil.copyPartialMatches(args[1], Arrays.asList("disown", "remove", "list"), new ArrayList<>()) : null;
    }
}
