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

package me.vik1395.ProtectionStones.commands;

import com.sk89q.worldguard.LocalPlayer;
import me.vik1395.ProtectionStones.PlayerComparator;
import me.vik1395.ProtectionStones.ProtectionStones;
import me.vik1395.ProtectionStones.commands.admin.ArgAdminCleanup;
import me.vik1395.ProtectionStones.commands.admin.ArgAdminHide;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class ArgAdmin {
    public static boolean argumentAdmin(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage(ChatColor.RED + "Correct usage: /ps admin {version|settings|hide|unhide|");
            p.sendMessage(ChatColor.RED + "                          cleanup|lastlogon|lastlogons|stats}");
            return true;
        }

        switch (args[1].toLowerCase()) {
            case "version":
                p.sendMessage(ChatColor.YELLOW + "ProtectionStones " + ProtectionStones.getPlugin().getDescription().getVersion());
                p.sendMessage(ChatColor.YELLOW + "CraftBukkit  " + Bukkit.getVersion());
                break;
            case "settings":
                p.sendMessage(ProtectionStones.getPlugin().getConfig().saveToString().split("\n"));
                break;
            case "hide":
                return ArgAdminHide.argumentAdminHide(p, args);
                break;
            case "unhide":
                return ArgAdminHide.argumentAdminHide(p, args);
                break;
            case "cleanup":
                return ArgAdminCleanup.argumentAdminCleanup(p, args);
                break;
        }


        if (args[1].equalsIgnoreCase("cleanup")) {

        } else if (args[1].equalsIgnoreCase("lastlogon")) {
            if (args.length > 2) {
                String playerName = args[2];
                if (Bukkit.getOfflinePlayer(playerName).getFirstPlayed() > 0L) {
                    long lastPlayed = (System.currentTimeMillis() - Bukkit.getOfflinePlayer(playerName).getLastPlayed()) / 86400000L;
                    p.sendMessage(ChatColor.YELLOW + playerName + " last played " + lastPlayed + " days ago.");
                    if (Bukkit.getOfflinePlayer(playerName).isBanned()) {
                        p.sendMessage(ChatColor.YELLOW + playerName + " is banned.");
                    }
                } else {
                    p.sendMessage(ChatColor.YELLOW + "Player name not found.");
                }
            } else {
                p.sendMessage(ChatColor.YELLOW + "A player name is required.");
            }
        } else if (args[1].equalsIgnoreCase("lastlogons")) {
            int days = 0;
            if (args.length > 2) {
                days = Integer.parseInt(args[2]);
            }
            OfflinePlayer[] offlinePlayerList = getServer().getOfflinePlayers();
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
        } else if (args[1].equalsIgnoreCase("stats")) {
            if (args.length > 2) {
                String playerName = args[2];
                if (Bukkit.getOfflinePlayer(playerName).getFirstPlayed() > 0L) {
                    p.sendMessage(ChatColor.YELLOW + playerName + ":");
                    p.sendMessage(ChatColor.YELLOW + "================");
                    long firstPlayed = (System.currentTimeMillis() - Bukkit.getOfflinePlayer(playerName).getFirstPlayed()) / 86400000L;
                    p.sendMessage(ChatColor.YELLOW + "First played " + firstPlayed + " days ago.");
                    long lastPlayed = (System.currentTimeMillis() - Bukkit.getOfflinePlayer(playerName).getLastPlayed()) / 86400000L;
                    p.sendMessage(ChatColor.YELLOW + "Last played " + lastPlayed + " days ago.");
                    String banMessage = "Not Banned";
                    if (Bukkit.getOfflinePlayer(playerName).isBanned()) {
                        banMessage = "Banned";
                    }
                    p.sendMessage(ChatColor.YELLOW + banMessage);
                    int count = 0;
                    try {
                        LocalPlayer thePlayer = null;
                        thePlayer = wg.wrapOfflinePlayer(Bukkit.getOfflinePlayer(args[2]));
                        count = rgm.getRegionCountOfPlayer(thePlayer);
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
            int count = 0;
            try {
                count = rgm.size();
            } catch (Exception localException2) {
            }
            p.sendMessage(ChatColor.YELLOW + "Regions: " + count);
            p.sendMessage(ChatColor.YELLOW + "================");
        }

    }
}
