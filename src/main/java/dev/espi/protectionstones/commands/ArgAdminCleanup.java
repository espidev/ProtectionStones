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

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.*;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

class ArgAdminCleanup {

    // /ps admin cleanup
    static boolean argumentAdminCleanup(CommandSender p, String[] args) {
        WorldGuardPlugin wg = WorldGuardPlugin.inst();
        if (args.length < 3 || (!args[2].equalsIgnoreCase("remove") && !args[2].equalsIgnoreCase("disown"))) {
            PSL.msg(p, ArgAdmin.CLEANUP_HELP);
            return true;
        }

        RegionManager rgm;
        World w;
        if (p instanceof Player) {
            rgm = WGUtils.getRegionManagerWithPlayer((Player) p);
            w = ((Player) p).getWorld();
        } else {
            if (args.length != 5) {
                PSL.msg(p, PSL.ADMIN_CONSOLE_WORLD.msg());
                return true;
            }
            if (Bukkit.getWorld(args[4]) == null) {
                PSL.msg(p, PSL.INVALID_WORLD.msg());
                return true;
            }
            w = Bukkit.getWorld(args[4]);
            rgm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(w));
        }

        Map<String, ProtectedRegion> regions = rgm.getRegions();

        // async cleanup task
        Bukkit.getScheduler().runTaskAsynchronously(ProtectionStones.getInstance(), () -> {
            if ((args[2].equalsIgnoreCase("remove")) || (args[2].equalsIgnoreCase("disown"))) {
                int days = (args.length > 3) ? Integer.parseInt(args[3]) : 30; // 30 days is default if days aren't specified

                PSL.msg(p, PSL.ADMIN_CLEANUP_HEADER.msg()
                        .replace("%arg%", args[2])
                        .replace("%days%", "" + days));

                List<LocalPlayer> inactivePlayers = new ArrayList<>();

                // loop over offline players and add to list if they haven't joined recently
                for (OfflinePlayer op : Bukkit.getServer().getOfflinePlayers()) {
                    long lastPlayed = (System.currentTimeMillis() - op.getLastPlayed()) / 86400000L;
                    if (lastPlayed < days) continue; // skip if the player hasn't been gone for that long
                    try {
                        inactivePlayers.add(wg.wrapOfflinePlayer(op));
                    } catch (NullPointerException ignored) {} // wg.wrapOfflinePlayer can return null if the player isn't in WG cache
                }

                List<String> toRemove = new ArrayList<>();

                // Loop over regions and check if offline player is in
                for (String idname : regions.keySet()) {
                    ProtectedRegion region = regions.get(idname);
                    if (!ProtectionStones.isPSRegion(region)) continue;
                    // remove inactive players from being owner
                    for (LocalPlayer lp : inactivePlayers) {
                        try {
                            if (region.isOwner(lp)) {
                                region.getOwners().removePlayer(lp);
                            }
                        } catch (NullPointerException ignored){}
                    }

                    // remove region if there are no owners left
                    if (args[2].equalsIgnoreCase("remove") && region.getOwners().size() == 0) {
                        p.sendMessage(ChatColor.YELLOW + "Removed region " + idname + " due to inactive owners.");
                        PSLocation psl = WGUtils.parsePSRegionToLocation(idname);
                        Block blockToRemove = w.getBlockAt(psl.x, psl.y, psl.z);
                        Bukkit.getScheduler().runTask(ProtectionStones.getInstance(), ()->blockToRemove.setType(Material.AIR));
                        toRemove.add(idname);
                    }
                }

                for (String r : toRemove) {
                    // remove region
                    // check if removing the region and firing region remove event blocked it
                    Bukkit.getScheduler().runTask(ProtectionStones.getInstance(), () -> ProtectionStones.removePSRegion(w, r));
                }

                try {
                    rgm.save();
                } catch (Exception e) {
                    Bukkit.getLogger().severe("[ProtectionStones] WorldGuard Error [" + e + "] during Region File Save");
                }

                PSL.msg(p, PSL.ADMIN_CLEANUP_FOOTER.msg()
                        .replace("%arg%", args[2]));
            }
        });
        return true;
    }

}
