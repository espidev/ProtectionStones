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

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.vik1395.ProtectionStones.PlayerComparator;
import me.vik1395.ProtectionStones.ProtectionStones;
import me.vik1395.ProtectionStones.commands.admin.ArgAdminHide;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Map;

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
        }


        if (args[1].equalsIgnoreCase("cleanup")) {
            if (args.length >= 3) {
                if ((args[2].equalsIgnoreCase("remove")) || (args[2].equalsIgnoreCase("regen")) || (args[2].equalsIgnoreCase("disown"))) {
                    int days = 30;
                    if (args.length > 3) {
                        days = Integer.parseInt(args[3]);
                    }
                    p.sendMessage(ChatColor.YELLOW + "Cleanup " + args[2] + " " + days + " days");
                    p.sendMessage(ChatColor.YELLOW + "================");
                    RegionManager mgr = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(p.getWorld()));
                    Map<String, ProtectedRegion> regions = mgr.getRegions();
                    int size = regions.size();
                    String name = "";
                    int index = 0;
                    String[] regionIDList = new String[size];
                    OfflinePlayer[] offlinePlayerList = getServer().getOfflinePlayers();
                    int playerCount = offlinePlayerList.length;
                    for (int iii = 0; iii < playerCount; iii++) {
                        long lastPlayed = (System.currentTimeMillis() - offlinePlayerList[iii].getLastPlayed()) / 86400000L;
                        if (lastPlayed >= days) {
                            index = 0;
                            name = offlinePlayerList[iii].getName().toLowerCase();
                            for (String idname : regions.keySet()) {
                                try {
                                    if (((ProtectedRegion) regions.get(idname)).getOwners().getPlayers().contains(name)) {
                                        regionIDList[index] = idname;
                                        index++;
                                    }
                                } catch (Exception e) {
                                }
                            }
                            if (index == 0) {
                                p.sendMessage(ChatColor.YELLOW + "No regions found for " + name);
                            } else {
                                p.sendMessage(ChatColor.YELLOW + args[2] + ": " + name);
                                for (int i = 0; i < index; i++) {
                                    if (args[2].equalsIgnoreCase("disown")) {
                                        DefaultDomain owners = rgm.getRegion(regionIDList[i]).getOwners();
                                        owners.removePlayer(name);
                                        rgm.getRegion(regionIDList[i]).setOwners(owners);
                                    } else {
                                        if (args[2].equalsIgnoreCase("regen")) {
                                            if (this.getServer().getPluginManager().getPlugin("WorldEdit") != null) {
                                                Bukkit.dispatchCommand(p, "region select " + regionIDList[i]);
                                                Bukkit.dispatchCommand(p, "/regen");
                                            }
                                        } else if (regionIDList[i].substring(0, 2).equals("ps")) {
                                            int indexX = regionIDList[i].indexOf("x");
                                            int indexY = regionIDList[i].indexOf("y");
                                            int indexZ = regionIDList[i].length() - 1;
                                            int psx = Integer.parseInt(regionIDList[i].substring(2, indexX));
                                            int psy = Integer.parseInt(regionIDList[i].substring(indexX + 1, indexY));
                                            int psz = Integer.parseInt(regionIDList[i].substring(indexY + 1, indexZ));
                                            Block blockToRemove = p.getWorld().getBlockAt(psx, psy, psz);
                                            blockToRemove.setType(Material.AIR);
                                        }
                                        mgr.removeRegion(regionIDList[i]);
                                    }
                                }
                            }
                        }
                    }
                    try {
                        rgm.save();
                    } catch (Exception e) {
                        System.out.println("[ProtectionStones] WorldGuard Error [" + e + "] during Region File Save");
                    }
                    p.sendMessage(ChatColor.YELLOW + "================");
                    p.sendMessage(ChatColor.YELLOW + "Completed " + args[2] + " cleanup");
                    return true;
                }
            } else {
                p.sendMessage(ChatColor.YELLOW + "/ps admin cleanup {remove|regen|disown} {days}");
                return true;
            }
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
