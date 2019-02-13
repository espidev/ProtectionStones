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

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import me.vik1395.ProtectionStones.ProtectionStones;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ArgAddRemove {
    private static OfflinePlayer checks(Player p, String args[], String psID, RegionManager rgm, WorldGuardPlugin wg) {
        if (!p.hasPermission("protectionstones.members")) {
            p.sendMessage(ChatColor.RED + "You don't have permission to use Members Commands");
            return null;
        }
        if (ProtectionStones.hasNoAccess(rgm.getRegion(psID), p, wg.wrapPlayer(p), false)) {
            p.sendMessage(ChatColor.RED + "You are not allowed to do that here.");
            return null;
        }
        if (args.length < 2) {
            p.sendMessage(ChatColor.RED + "This command requires a player name.");
            return null;
        }
        if (psID.equals("")) {
            p.sendMessage(ChatColor.RED + "You are not in a protection stone region!");
            return null;
        }
        OfflinePlayer op = Bukkit.getOfflinePlayer(args[1]);
        if (op == null) {
            p.sendMessage(ChatColor.RED + "Player not found. Are your sure they have joined the server before?");
            return null;
        }
        return op;
    }

    private static boolean template(Player p, String[] args, String psID, String type) {
        WorldGuardPlugin wg = (WorldGuardPlugin) ProtectionStones.wgd;
        RegionManager rgm = ProtectionStones.getRegionManagerWithPlayer(p);
        OfflinePlayer op = checks(p, args, psID, rgm, wg); // validate permissions and stuff
        if (op == null) return true;

        if (type.equals("add")) {
            rgm.getRegion(psID).getMembers().removePlayer(op.getUniqueId()); // TODO check if works
        } else if (type.equals("remove")) {
            rgm.getRegion(psID).getMembers().removePlayer(op.getUniqueId()); // TODO check if works
        }
        try {
            rgm.save();
        } catch (Exception e) {
            Bukkit.getLogger().severe("[ProtectionStones] WorldGuard Error [" + e + "] during Region File Save");
        }

        if (type.equals("add")) {
            p.sendMessage(ChatColor.YELLOW + op.getName() + " has been added to your region.");
        } else if (type.equals("remove")) {
            p.sendMessage(ChatColor.YELLOW + op.getName() + " has been removed from region.");
        }
        return true;
    }

    public static boolean argumentAdd(Player p, String[] args, String psID) {
        return template(p, args, psID, "add");
    }

    public static boolean argumentRemove(Player p, String[] args, String psID) {
        return template(p, args, psID, "remove");
    }
}
