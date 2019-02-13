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

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import me.vik1395.ProtectionStones.ProtectionStones;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArgTp {

    // /ps tp
    public static boolean argumentTp(Player p, String[] args) {
        WorldGuardPlugin wg = (WorldGuardPlugin) ProtectionStones.wgd;
        RegionManager rgm = ProtectionStones.getRegionManagerWithPlayer(p);

        UUID playerid = p.getUniqueId();
        int index = 0, rgnum; // index: index in playerRegions for selected region, rgnum: index specified by player to teleport to
        Map<Integer, String> playerRegions = new HashMap<>();

        // preliminary checks
        if (args[0].equalsIgnoreCase("tp")) { // argument tp
            if (!p.hasPermission("protectionstones.tp")) {
                p.sendMessage(ChatColor.RED + "You do not have permission to execute this command!");
                return true;
            } else if (args.length != 3) {
                p.sendMessage(ChatColor.RED + "Usage: /ps tp [player] [num]");
                return true;
            }
            rgnum = Integer.parseInt(args[2]);
        } else { // argument home
            if (!p.hasPermission("protectionstones.home")) {
                p.sendMessage(ChatColor.RED + "You do not have permission to execute this command!");
                return true;
            } else if (args.length != 2) {
                p.sendMessage(ChatColor.RED + "Usage: /ps home [num]");
                p.sendMessage(ChatColor.YELLOW + "To see your ps count, type /ps count. Use any number within the range to teleport to that ps");
                return true;
            }
            rgnum = Integer.parseInt(args[1]);
        }

        if (rgnum <= 0) {
            p.sendMessage(ChatColor.RED + "Please enter a number above 0.");
            return true;
        }

        // find regions that the player has
        for (String region : rgm.getRegions().keySet()) {
            if (region.startsWith("ps")) {
                if (rgm.getRegions().get(region).getOwners().contains(playerid)) {
                    index++;
                    playerRegions.put(index, region);
                }
            }
        }

        // region checks
        if (args[0].equalsIgnoreCase("tp")) {
            LocalPlayer lp;
            try {
                lp = wg.wrapOfflinePlayer(Bukkit.getOfflinePlayer(args[1]));
            } catch (Exception e) {
                p.sendMessage(ChatColor.RED + "Error while searching for " + args[1] + "'s regions. Please make sure you have entered the correct name.");
                return true;
            }
            if (index <= 0) {
                p.sendMessage(ChatColor.RED + lp.getName() + " doesn't own any protected regions!");
                return true;
            } else if (rgnum > index) {
                p.sendMessage(ChatColor.RED + lp.getName() + " only has " + index + " protected regions!");
                return true;
            }
        } else if (args[0].equalsIgnoreCase("home")) {
            if (index <= 0) {
                p.sendMessage(ChatColor.RED + "You don't own any protected regions!");
            }
            if (rgnum > index) {
                p.sendMessage(ChatColor.RED + "You only have " + index + " total regions!");
                return true;
            }
        }

        // teleport player
        if (rgnum <= index) {
            String region = rgm.getRegion(playerRegions.get(rgnum)).getId();
            String[] pos = region.split("x|y|z");
            if (pos.length == 3) {
                pos[0] = pos[0].substring(2);
                p.sendMessage(ChatColor.GREEN + "Teleporting...");
                Location tploc = new Location(p.getWorld(), Integer.parseInt(pos[0]), Integer.parseInt(pos[1]), Integer.parseInt(pos[2]));
                p.teleport(tploc);
            } else {
                p.sendMessage(ChatColor.RED + "Error in teleporting to protected region! (parsing WG region name error)");
            }
        } else {
            p.sendMessage(ChatColor.RED + "Error in finding the region to teleport to!");
        }

        return true;
    }
}
