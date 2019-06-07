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

package dev.espi.ProtectionStones.commands;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import dev.espi.ProtectionStones.ProtectionStones;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

class ArgAdminStats {

    // /ps admin stats
    static boolean argumentAdminStats(CommandSender p, String[] args) {
        WorldGuardPlugin wg = (WorldGuardPlugin) ProtectionStones.wgd;

        int size = 0;
        for (World w : Bukkit.getWorlds()) {
            size += WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(w)).size();
        }

        if (args.length > 2) {
            String playerName = args[2];
            OfflinePlayer op = Bukkit.getOfflinePlayer(playerName);
            int count = 0;
            for (World w : Bukkit.getWorlds()) {
                count += WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(w)).getRegionCountOfPlayer(wg.wrapOfflinePlayer(op));
            }
            p.sendMessage(ChatColor.YELLOW + playerName + ":");
            p.sendMessage(ChatColor.YELLOW + "================");
            long firstPlayed = (System.currentTimeMillis() - op.getFirstPlayed()) / 86400000L;
            p.sendMessage(ChatColor.YELLOW + "First played " + firstPlayed + " days ago.");
            long lastPlayed = (System.currentTimeMillis() - op.getLastPlayed()) / 86400000L;
            p.sendMessage(ChatColor.YELLOW + "Last played " + lastPlayed + " days ago.");

            String banMessage = (op.isBanned()) ? "Banned" : "Not Banned";
            p.sendMessage(ChatColor.YELLOW + banMessage);
            p.sendMessage(ChatColor.YELLOW + "Regions: " + count);
            p.sendMessage(ChatColor.YELLOW + "================");
            return true;
        }

        p.sendMessage(ChatColor.YELLOW + "================");
        p.sendMessage(ChatColor.YELLOW + "Regions: " + size);
        p.sendMessage(ChatColor.YELLOW + "================");

        return true;
    }
}
