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

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

class ArgAdminStats {

    // /ps admin stats
    static boolean argumentAdminStats(CommandSender p, String[] args) {
        WorldGuardPlugin wg = WorldGuardPlugin.inst();

        int size = 0;
        for (World w : Bukkit.getWorlds()) {
            size += WGUtils.getRegionManagerWithWorld(w).size();
        }

        if (args.length > 2) {
            String playerName = args[2];
            OfflinePlayer op = Bukkit.getOfflinePlayer(playerName);
            int count = 0;
            HashMap<World, RegionManager> m = WGUtils.getAllRegionManagers();
            for (RegionManager rgm : m.values()) {
                count += rgm.getRegionCountOfPlayer(wg.wrapOfflinePlayer(op));
            }
            p.sendMessage(ChatColor.YELLOW + playerName + ":");
            p.sendMessage(ChatColor.YELLOW + "================");
            long firstPlayed = (System.currentTimeMillis() - op.getFirstPlayed()) / 86400000L;
            p.sendMessage(ChatColor.YELLOW + "First played " + firstPlayed + " days ago.");
            long lastPlayed = (System.currentTimeMillis() - op.getLastPlayed()) / 86400000L;
            p.sendMessage(ChatColor.YELLOW + "Last played " + lastPlayed + " days ago.");

            String banMessage = (op.isBanned()) ? "Banned" : "Not Banned";
            p.sendMessage(ChatColor.YELLOW + banMessage);
            p.sendMessage(ChatColor.YELLOW + "Regions: " + count);
            p.sendMessage(ChatColor.YELLOW + "================");
            return true;
        }

        p.sendMessage(ChatColor.YELLOW + "================");
        p.sendMessage(ChatColor.YELLOW + "Regions: " + size);
        p.sendMessage(ChatColor.YELLOW + "================");

        return true;
    }
}
