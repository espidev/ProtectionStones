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

package me.vik1395.ProtectionStones.commands;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.vik1395.ProtectionStones.ProtectionStones;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class ArgCount {

    // /ps count
    public static boolean argumentCount(Player p, String[] args) {
        RegionManager rgm = ProtectionStones.getRegionManagerWithPlayer(p);
        int count;
        UUID playerid = p.getUniqueId();

        if (args.length == 1) {
            if (p.hasPermission("protectionstones.count")) {
                count = countRegionsOfPlayer(playerid, rgm);
                p.sendMessage(ChatColor.YELLOW + "Your region count: " + count);
            } else {
                p.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            }
            return true;
        } else if (args.length == 2) {
            if (p.hasPermission("protectionstones.count.others")) {
                count = countRegionsOfPlayer(playerid, rgm);
                p.sendMessage(ChatColor.YELLOW + args[1] + "'s region count: " + count);
            } else {
                p.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            }
            return true;
        } else {
            p.sendMessage(ChatColor.RED + "Usage: /ps count, /ps count [player]");
            return true;
        }
    }

    // Only PS regions, not other regions
    public static int countRegionsOfPlayer(UUID uuid, RegionManager rgm) {
        int count = 0;
        try {
            Map<String, ProtectedRegion> regions = rgm.getRegions();
            for (String selected : regions.keySet()) {
                if (regions.get(selected).getOwners().contains(uuid) && regions.get(selected).getId().startsWith("ps")) {
                    count++;
                }
            }
        } catch (Exception ignored) {

        }
        return count;
    }
}
