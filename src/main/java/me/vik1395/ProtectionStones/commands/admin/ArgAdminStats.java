/*
 * Copyright 2019 ProtectionStones team and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.vik1395.ProtectionStones.commands.admin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import me.vik1395.ProtectionStones.ProtectionStones;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class ArgAdminStats {

    // /ps admin stats
    public static boolean argumentAdminStats(Player p, String[] args) {
        WorldGuardPlugin wg = (WorldGuardPlugin) ProtectionStones.wgd;
        RegionManager rgm = ProtectionStones.getRegionManagerWithPlayer(p);

        if (args.length > 2) {
            String playerName = args[2];
            if (Bukkit.getOfflinePlayer(playerName) != null) {
                OfflinePlayer op = Bukkit.getOfflinePlayer(playerName);
                p.sendMessage(ChatColor.YELLOW + playerName + ":");
                p.sendMessage(ChatColor.YELLOW + "================");
                long firstPlayed = (System.currentTimeMillis() - op.getFirstPlayed()) / 86400000L;
                p.sendMessage(ChatColor.YELLOW + "First played " + firstPlayed + " days ago.");
                long lastPlayed = (System.currentTimeMillis() - op.getLastPlayed()) / 86400000L;
                p.sendMessage(ChatColor.YELLOW + "Last played " + lastPlayed + " days ago.");

                String banMessage = (op.isBanned()) ? "Banned" : "Not Banned";
                p.sendMessage(ChatColor.YELLOW + banMessage);

                int count = 0;
                try {
                    count = rgm.getRegionCountOfPlayer(wg.wrapOfflinePlayer(op));
                } catch (Exception localException1) {
                }
                p.sendMessage(ChatColor.YELLOW + "Regions: " + count);
                p.sendMessage(ChatColor.YELLOW + "================");
            } else {
                p.sendMessage(ChatColor.YELLOW + "Player name not found.");
            }
            return true;
        }

        p.sendMessage(ChatColor.YELLOW + "World:");
        p.sendMessage(ChatColor.YELLOW + "================");
        p.sendMessage(ChatColor.YELLOW + "Regions: " + rgm.size());
        p.sendMessage(ChatColor.YELLOW + "================");

        return true;
    }
}
