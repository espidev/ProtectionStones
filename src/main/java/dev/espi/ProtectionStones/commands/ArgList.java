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

import dev.espi.ProtectionStones.PSL;
import dev.espi.ProtectionStones.PSRegion;
import dev.espi.ProtectionStones.ProtectionStones;
import dev.espi.ProtectionStones.utils.UUIDCache;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ArgList implements PSCommandArg {
    @Override
    public List<String> getNames() {
        return Collections.singletonList("list");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args) {
        if (!s.hasPermission("protectionstones.list")) {
            PSL.msg(s, PSL.NO_PERMISSION_LIST.msg());
            return true;
        }
        if (args.length == 2 && !s.hasPermission("protectionstones.list.others")) {
            PSL.msg(s, PSL.NO_PERMISSION_LIST_OTHERS.msg());
            return true;
        }
        if (args.length == 2 && !UUIDCache.nameToUUID.containsKey(args[1])) {
            PSL.msg(s, PSL.PLAYER_NOT_FOUND.msg());
            return true;
        }

        // run query async to reduce load
        Bukkit.getScheduler().runTaskAsynchronously(ProtectionStones.getInstance(), () -> {
            Player p = (Player) s;
            if (args.length == 1) {
                List<PSRegion> regions = ProtectionStones.getPlayerPSRegions(p.getWorld(), p.getUniqueId());
                display(s, regions, p.getName(), p.getUniqueId());
            } else if (args.length == 2) {
                List<PSRegion> regions = ProtectionStones.getPlayerPSRegions(p.getWorld(), UUIDCache.nameToUUID.get(args[1]));
                display(s, regions, args[1], UUIDCache.nameToUUID.get(args[1]));
            } else {
                PSL.msg(s, PSL.LIST_HELP.msg());
            }
        });
        return true;
    }

    private void display(CommandSender s, List<PSRegion> regions, String pName, UUID pUUID) {
        PSL.msg(s, PSL.LIST_HEADER.msg().replace("%player%", pName));
        PSL.msg(s, PSL.LIST_OWNER.msg());
        for (PSRegion r : regions) {
            if (r.isOwner(pUUID)) {
                if (r.getName() == null) {
                    s.sendMessage(ChatColor.GRAY + "> " + ChatColor.AQUA + r.getID());
                } else {
                    s.sendMessage(ChatColor.GRAY + "> " + ChatColor.AQUA + r.getName() + " (" + r.getID() + ")");
                }
            }
        }
        PSL.msg(s, PSL.LIST_MEMBER.msg());
        for (PSRegion r : regions) {
            if (r.isMember(pUUID)) {
                if (r.getName() == null) {
                    s.sendMessage(ChatColor.GRAY + "> " + ChatColor.AQUA + r.getID());
                } else {
                    s.sendMessage(ChatColor.GRAY + "> " + ChatColor.AQUA + r.getName() + " (" + r.getID() + ")");
                }
            }
        }
    }

}
