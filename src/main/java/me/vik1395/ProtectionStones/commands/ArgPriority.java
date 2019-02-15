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

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import me.vik1395.ProtectionStones.ProtectionStones;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ArgPriority {
    public static boolean argPriority(Player p, String[] args, String psID) {
        WorldGuardPlugin wg = (WorldGuardPlugin) ProtectionStones.wgd;
        RegionManager rgm = ProtectionStones.getRegionManagerWithPlayer(p);

        if (!p.hasPermission("protectionstones.priority")) {
            p.sendMessage(ChatColor.RED + "You don't have permission to use Priority Commands");
            return true;
        }
        if (ProtectionStones.hasNoAccess(rgm.getRegion(psID), p, wg.wrapPlayer(p), false)) {
            p.sendMessage(ChatColor.RED + "You are not allowed to do that here.");
            return true;
        }
        if (args.length < 2) {
            int priority = rgm.getRegion(psID).getPriority();
            p.sendMessage(ChatColor.YELLOW + "Priority: " + priority);
            return true;
        }

        try {
            int priority = Integer.parseInt(args[1]);
            rgm.getRegion(psID).setPriority(priority);
            try {
                rgm.save();
            } catch (Exception e) {
                Bukkit.getLogger().severe("[ProtectionStones] WorldGuard Error [" + e + "] during Region File Save");
            }
            p.sendMessage(ChatColor.YELLOW + "Priority has been set.");
        } catch (Exception e) {
            p.sendMessage("Error parsing input, check it again?");
        }
        return true;
    }
}
