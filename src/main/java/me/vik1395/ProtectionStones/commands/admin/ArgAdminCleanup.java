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

import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.vik1395.ProtectionStones.PSLocation;
import me.vik1395.ProtectionStones.ProtectionStones;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArgAdminCleanup {
    public static boolean argumentAdminCleanup(Player p, String[] args) {
        if (args.length < 3 || (!args[2].equalsIgnoreCase("remove") && !args[2].equalsIgnoreCase("regen") && !args[2].equalsIgnoreCase("disown"))) {
            p.sendMessage(ChatColor.YELLOW + "/ps admin cleanup {remove|regen|disown} {days}");
            return true;
        }

        RegionManager rgm = ProtectionStones.getRegionManagerWithPlayer(p);
        Map<String, ProtectedRegion> regions = rgm.getRegions();

        try {
            Integer.parseInt(args[3]);
        } catch (Exception e) {
            p.sendMessage(ChatColor.YELLOW + "Error parsing days.");
            return true;
        }

        if ((args[2].equalsIgnoreCase("remove")) || (args[2].equalsIgnoreCase("regen")) || (args[2].equalsIgnoreCase("disown"))) {
            int days = (args.length > 3) ? Integer.parseInt(args[3]) : 30; // 30 days is default if days aren't specified
            p.sendMessage(ChatColor.YELLOW + "Cleanup " + args[2] + " " + days + " days");
            p.sendMessage(ChatColor.YELLOW + "================");

            // loop over offline players to delete old data
            for (OfflinePlayer op : Bukkit.getServer().getOfflinePlayers()) {
                long lastPlayed = (System.currentTimeMillis() - op.getLastPlayed()) / 86400000L;
                if (lastPlayed < days) continue; // skip if the player hasn't been gone for that long

                List<String> opRegions = new ArrayList<>();
                // add player's owned regions to list
                boolean found = false;
                for (String idname : regions.keySet()) { // TODO convert to UUID ??
                    try {
                        if (regions.get(idname).getOwners().getUniqueIds().contains(op.getUniqueId())) {
                            opRegions.add(idname);
                            found = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (!found) {
                    p.sendMessage(ChatColor.YELLOW + "No regions found for " + op.getName());
                    continue;
                }

                // remove regions
                p.sendMessage(ChatColor.YELLOW + args[2] + ": " + op.getName());
                for (String region : opRegions) {
                    if (args[2].equalsIgnoreCase("disown")) { // disown arg
                        DefaultDomain owners = rgm.getRegion(region).getOwners();
                        owners.removePlayer(op.getUniqueId());
                        rgm.getRegion(region).setOwners(owners);
                    } else if (args[2].equalsIgnoreCase("regen")) { // regen arg
                        Bukkit.dispatchCommand(p, "region select " + region);
                        Bukkit.dispatchCommand(p, "/regen");
                        rgm.removeRegion(region);
                    } else if (region.substring(0, 2).equals("ps")) { // remove arg
                        PSLocation psl = ProtectionStones.parsePSRegionToLocation(region);
                        Block blockToRemove = p.getWorld().getBlockAt(psl.x, psl.y, psl.z);
                        blockToRemove.setType(Material.AIR);
                        rgm.removeRegion(region);
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
        }
        return true;
    }
}
