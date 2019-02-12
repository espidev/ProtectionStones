/*
 * Copyright 2019
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

import me.vik1395.ProtectionStones.PlayerComparator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class ArgAdminLastlogon {
    // /ps admin lastlogon
    public static boolean argumentAdminLastLogon(Player p, String[] args) {
        if (args.length < 3) {
            p.sendMessage(ChatColor.YELLOW + "A player name is required.");
            return true;
        }
        OfflinePlayer op = Bukkit.getOfflinePlayer(args[2]);
        if (op == null) {
            p.sendMessage(ChatColor.YELLOW + "A player name is required (must have joined the server before).");
        }

        String playerName = args[2];
        long lastPlayed = (System.currentTimeMillis() - op.getLastPlayed()) / 86400000L;
        p.sendMessage(ChatColor.YELLOW + playerName + " last played " + lastPlayed + " days ago.");
        if (op.isBanned()) {
            p.sendMessage(ChatColor.YELLOW + playerName + " is banned.");
        }

        return true;
    }

    // /ps admin lastlogons
    public static boolean argumentAdminLastLogons(Player p, String[] args) {
        int days = 0;
        if (args.length > 2) {
            try {
                days = Integer.parseInt(args[2]);
            } catch (Exception e) {
                p.sendMessage(ChatColor.YELLOW + "Error parsing days, are you sure it is a number?");
                return true;
            }
        }
        OfflinePlayer[] offlinePlayerList = Bukkit.getServer().getOfflinePlayers().clone();
        int playerCount = offlinePlayerList.length;
        int playerCounter = 0;
        p.sendMessage(ChatColor.YELLOW + "" + days + " Days Plus:");
        p.sendMessage(ChatColor.YELLOW + "================");
        Arrays.sort(offlinePlayerList, new PlayerComparator());
        for (int iii = 0; iii < playerCount; iii++) {
            long lastPlayed = (System.currentTimeMillis() - offlinePlayerList[iii].getLastPlayed()) / 86400000L;
            if (lastPlayed >= days) {
                playerCounter++;
                p.sendMessage(ChatColor.YELLOW + offlinePlayerList[iii].getName() + " " + lastPlayed + " days");
            }
        }
        p.sendMessage(ChatColor.YELLOW + "================");
        p.sendMessage(ChatColor.YELLOW + "" + playerCounter + " Total Players Shown");
        p.sendMessage(ChatColor.YELLOW + "" + playerCount + " Total Players Checked");
        return true;
    }
}
