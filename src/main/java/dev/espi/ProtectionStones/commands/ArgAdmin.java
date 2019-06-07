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

import com.sk89q.worldedit.WorldEdit;
import dev.espi.ProtectionStones.PSL;
import dev.espi.ProtectionStones.ProtectionStones;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class ArgAdmin implements PSCommandArg {

    @Override
    public List<String> getNames() {
        return Collections.singletonList("admin");
    }

    // /ps admin [arg]
    @Override
    public boolean executeArgument(CommandSender s, String[] args) {
        if (!s.hasPermission("protectionstones.admin")) {
            PSL.msg(s, PSL.NO_PERMISSION_ADMIN.msg());
            return true;
        }

        if (args.length < 2) {
            s.sendMessage(PSL.ADMIN_HELP.msg());
            return true;
        }

        switch (args[1].toLowerCase()) {
            case "version":
                s.sendMessage(ChatColor.YELLOW + "ProtectionStones: " + ProtectionStones.getPlugin().getDescription().getVersion());
                s.sendMessage(ChatColor.YELLOW + "Developers: " + ProtectionStones.getPlugin().getDescription().getAuthors());
                s.sendMessage(ChatColor.YELLOW + "Bukkit:  " + Bukkit.getVersion());
                s.sendMessage(ChatColor.YELLOW + "WG: " + ProtectionStones.wgd.getDescription().getVersion());
                s.sendMessage(ChatColor.YELLOW + "WE: " + WorldEdit.getVersion());
                break;
            case "hide":
                return ArgAdminHide.argumentAdminHide(s, args);
            case "unhide":
                return ArgAdminHide.argumentAdminHide(s, args);
            case "cleanup":
                return ArgAdminCleanup.argumentAdminCleanup(s, args);
            case "stats":
                return ArgAdminStats.argumentAdminStats(s, args);
            case "lastlogon":
                return ArgAdminLastlogon.argumentAdminLastLogon(s, args);
            case "lastlogons":
                return ArgAdminLastlogon.argumentAdminLastLogons(s, args);
            case "fixregions":
                s.sendMessage(ChatColor.YELLOW + "Fixing...");
                ProtectionStones.upgradeRegions();
                s.sendMessage(ChatColor.YELLOW + "Done!");
                break;
        }
        return true;
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return true;
    }
}
