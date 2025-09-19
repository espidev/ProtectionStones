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
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

class ArgAdminHide {

    // /ps admin hide
    static boolean argumentAdminHide(CommandSender p, String[] args) {
        RegionManager mgr;
        World w;
        if (p instanceof Player) {
            mgr = WGUtils.getRegionManagerWithPlayer((Player) p);
            w = ((Player) p).getWorld();
        } else {
            if (args.length != 3) {
                PSL.msg(p, PSL.ADMIN_CONSOLE_WORLD.msg());
                return true;
            }
            if (Bukkit.getWorld(args[2]) == null) {
                PSL.msg(p, PSL.INVALID_WORLD.msg());
                return true;
            }
            w = Bukkit.getWorld(args[2]);
            mgr = WGUtils.getRegionManagerWithWorld(w);
        }

        Bukkit.getScheduler().runTaskAsynchronously(ProtectionStones.getInstance(), () -> {
            // loop through regions that are protection stones and hide or unhide the block
            for (ProtectedRegion r : mgr.getRegions().values()) {
                if (ProtectionStones.isPSRegion(r)) {
                    PSRegion region = PSRegion.fromWGRegion(w, r);
                    if (args[1].equalsIgnoreCase("hide")) {
                        Bukkit.getScheduler().runTask(ProtectionStones.getInstance(), region::hide);
                    } else if (args[1].equalsIgnoreCase("unhide")){
                        Bukkit.getScheduler().runTask(ProtectionStones.getInstance(), region::unhide);
                    }
                }
            }

            String hMessage = args[1].equalsIgnoreCase("unhide") ? "unhidden" : "hidden";
            PSL.msg(p, PSL.ADMIN_HIDE_TOGGLED
                    .replace("%message%", hMessage));
        });

        return true;
    }
}
