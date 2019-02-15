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

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.vik1395.ProtectionStones.ProtectionStones;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArgAdminCleanup {

    // /ps admin cleanup
    public static boolean argumentAdminCleanup(Player p, String[] args) {
        WorldGuardPlugin wg = (WorldGuardPlugin) ProtectionStones.wgd;
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

                LocalPlayer lp = wg.wrapOfflinePlayer(op);

                List<String> opRegions = new ArrayList<>();
                // add player's owned regions to list
                boolean found = false;
                for (String idname : regions.keySet()) {
                    try {
                        if (regions.get(idname).getOwners().contains(lp)) {
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
                    ProtectionStones.removeDisownRegenPSRegion(lp, args[2].toLowerCase(), region, rgm, p);
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
