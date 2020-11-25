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
import dev.espi.protectionstones.PSLocation;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class ArgAdminCleanup {

    // /ps admin cleanup
    static boolean argumentAdminCleanup(CommandSender p, String[] preParseArgs) {
        if (preParseArgs.length < 3 || (!preParseArgs[2].equalsIgnoreCase("remove") && !preParseArgs[2].equalsIgnoreCase("disown"))) {
            PSL.msg(p, ArgAdmin.getCleanupHelp());
            return true;
        }

        String cleanupOperation = preParseArgs[2]; // [remove|disown]

        World w;
        String alias = null;

        List<String> args = new ArrayList<>();

        // determine if there is an alias flag selected, and remove [-t typealias] if there is
        for (int i = 3; i < preParseArgs.length; i++) {
            if (preParseArgs[i].equals("-t") && i != preParseArgs.length-1) {
                alias = preParseArgs[++i];
            } else {
                args.add(preParseArgs[i]);
            }
        }

        // the args array should consist of: [days, world (optional)]

        if (args.size() > 1 && Bukkit.getWorld(args.get(1)) != null) {
            w = Bukkit.getWorld(args.get(1));
        } else {
            if (p instanceof Player) {
                w = ((Player) p).getWorld();
            } else {
                PSL.msg(p, args.size() > 1 ? PSL.INVALID_WORLD.msg() : PSL.ADMIN_CONSOLE_WORLD.msg());
                return true;
            }
        }

        RegionManager rgm = WGUtils.getRegionManagerWithWorld(w);

        Map<String, ProtectedRegion> regions = rgm.getRegions();

        // async cleanup task
        String finalAlias = alias;
        Bukkit.getScheduler().runTaskAsynchronously(ProtectionStones.getInstance(), () -> {
            int days = (args.size() > 0) ? Integer.parseInt(args.get(0)) : 30; // 30 days is default if days aren't specified

            PSL.msg(p, PSL.ADMIN_CLEANUP_HEADER.msg()
                    .replace("%arg%", cleanupOperation)
                    .replace("%days%", "" + days));

            List<UUID> inactivePlayers = new ArrayList<>();

            // loop over offline players and add to list if they haven't joined recently
            for (OfflinePlayer op : Bukkit.getServer().getOfflinePlayers()) {
                long lastPlayed = (System.currentTimeMillis() - op.getLastPlayed()) / 86400000L;
                if (lastPlayed < days) continue; // skip if the player hasn't been gone for that long
                try {
                    inactivePlayers.add(op.getUniqueId());
                } catch (NullPointerException ignored) {} // wg.wrapOfflinePlayer can return null if the player isn't in WG cache
            }

            // Loop over regions and check if offline player is in
            for (String idname : regions.keySet()) {
                PSRegion r = PSRegion.fromWGRegion(w, regions.get(idname));
                if (r == null) continue;

                // if an alias is specified, skip regions that aren't of the type
                if (finalAlias != null && (r.getTypeOptions() == null || !r.getTypeOptions().alias.equals(finalAlias))) continue;

                // remove inactive players from being owner
                for (UUID uuid : inactivePlayers) {
                    try {
                        if (r.isOwner(uuid)) {
                            r.removeOwner(uuid);
                        }
                    } catch (NullPointerException ignored) {}
                }

                // remove region if there are no owners left
                if (cleanupOperation.equalsIgnoreCase("remove") && r.getOwners().size() == 0) {
                    p.sendMessage(ChatColor.YELLOW + "Removed region " + idname + " due to inactive owners.");
                    Block blockToRemove = r.getProtectBlock();

                    // must be sync (both isHidden, and setType)
                    Bukkit.getScheduler().runTask(ProtectionStones.getInstance(), () -> {
                        if (!r.isHidden()) {
                            blockToRemove.setType(Material.AIR);
                        }
                        ProtectionStones.removePSRegion(w, idname);
                    });
                }
            }

            PSL.msg(p, PSL.ADMIN_CLEANUP_FOOTER.msg()
                    .replace("%arg%", cleanupOperation));
        });
        return true;
    }

}
