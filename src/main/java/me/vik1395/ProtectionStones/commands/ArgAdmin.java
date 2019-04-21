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

import com.sk89q.worldedit.WorldEdit;
import me.vik1395.ProtectionStones.PSL;
import me.vik1395.ProtectionStones.ProtectionStones;
import me.vik1395.ProtectionStones.commands.admin.ArgAdminCleanup;
import me.vik1395.ProtectionStones.commands.admin.ArgAdminHide;
import me.vik1395.ProtectionStones.commands.admin.ArgAdminLastlogon;
import me.vik1395.ProtectionStones.commands.admin.ArgAdminStats;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ArgAdmin {

    // /ps admin [arg]
    public static boolean argumentAdmin(CommandSender p, String[] args) {
        if (!p.hasPermission("protectionstones.admin")) {
            p.sendMessage(PSL.NO_PERMISSION_ADMIN.msg());
            return true;
        }

        if (args.length < 2) {
            p.sendMessage(PSL.ADMIN_HELP.msg());
            return true;
        }

        switch (args[1].toLowerCase()) {
            case "version":
                p.sendMessage(ChatColor.YELLOW + "ProtectionStones: " + ProtectionStones.getPlugin().getDescription().getVersion());
                p.sendMessage(ChatColor.YELLOW + "Developers: " + ProtectionStones.getPlugin().getDescription().getAuthors());
                p.sendMessage(ChatColor.YELLOW + "Bukkit:  " + Bukkit.getVersion());
                p.sendMessage(ChatColor.YELLOW + "WG: " + ProtectionStones.wgd.getDescription().getVersion());
                p.sendMessage(ChatColor.YELLOW + "WE: " + WorldEdit.getVersion());
                break;
            case "hide":
                return ArgAdminHide.argumentAdminHide(p, args);
            case "unhide":
                return ArgAdminHide.argumentAdminHide(p, args);
            case "cleanup":
                return ArgAdminCleanup.argumentAdminCleanup(p, args);
            case "stats":
                return ArgAdminStats.argumentAdminStats(p, args);
            case "lastlogon":
                return ArgAdminLastlogon.argumentAdminLastLogon(p, args);
            case "lastlogons":
                return ArgAdminLastlogon.argumentAdminLastLogons(p, args);
            case "fixregions":
                p.sendMessage(ChatColor.YELLOW + "Fixing...");
                ProtectionStones.upgradeRegions();
                p.sendMessage(ChatColor.YELLOW + "Done!");
                break;
        }
        return true;
    }
}
