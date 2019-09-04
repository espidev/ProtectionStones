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

package dev.espi.protectionstones.commands;

import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.ProtectionStones;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ArgHelp implements PSCommandArg {
    @Override
    public List<String> getNames() {
        return Collections.singletonList("help");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public List<String> getPermissionsToExecute() {
        return null;
    }

    @Override
    public HashMap<String, Boolean> getRegisteredFlags() {
        return null;
    }

    @Override
    public boolean executeArgument(CommandSender p, String[] args, HashMap<String, String> flags) {
        String base = "/" + ProtectionStones.getInstance().getConfigOptions().base_command + " ";

        p.sendMessage(PSL.HELP.msg());
        sendWithPerm(p, PSL.INFO_HELP.msg(), PSL.INFO_HELP_DESC.msg(), base + "info", "protectionstones.info");
        sendWithPerm(p, PSL.ADDREMOVE_HELP.msg(), PSL.ADDREMOVE_HELP_DESC.msg(), base, "protectionstones.members");
        sendWithPerm(p, PSL.ADDREMOVE_OWNER_HELP.msg(), PSL.ADDREMOVE_OWNER_HELP_DESC.msg(), base, "protectionstones.owners");
        sendWithPerm(p, PSL.GET_HELP.msg(), PSL.GET_HELP_DESC.msg(), base + "get", "protectionstones.get");
        sendWithPerm(p, PSL.GIVE_HELP.msg(), PSL.GIVE_HELP_DESC.msg(), base + "give", "protectionstones.give");
        sendWithPerm(p, PSL.COUNT_HELP.msg(), PSL.COUNT_HELP_DESC.msg(), base + "count", "protectionstones.count", "protectionstones.count.others");
        sendWithPerm(p, PSL.LIST_HELP.msg(), PSL.LIST_HELP_DESC.msg(), base + "list", "protectionstones.list", "protectionstones.list.others");
        sendWithPerm(p, PSL.NAME_HELP.msg(), PSL.NAME_HELP_DESC.msg(), base + "name", "protectionstones.name");
        sendWithPerm(p, PSL.MERGE_HELP.msg(), PSL.MERGE_HELP_DESC.msg(), base + "merge", "protectionstones.merge");
        sendWithPerm(p, PSL.SETPARENT_HELP.msg(), PSL.SETPARENT_HELP_DESC.msg(), base + "setparent", "protectionstones.setparent", "protectionstones.setparent.others");
        sendWithPerm(p, PSL.FLAG_HELP.msg(), PSL.FLAG_HELP_DESC.msg(), base + "flag", "protectionstones.flags");
        sendWithPerm(p, PSL.HOME_HELP.msg(), PSL.HOME_HELP_DESC.msg(), base + "home", "protectionstones.home");
        sendWithPerm(p, PSL.SETHOME_HELP.msg(), PSL.SETHOME_HELP_DESC.msg(), base + "sethome", "protectionstones.sethome");
        sendWithPerm(p, PSL.TP_HELP.msg(), PSL.TP_HELP_DESC.msg(), base + "tp", "protectionstones.tp");
        sendWithPerm(p, PSL.VISIBILITY_HIDE_HELP.msg(), PSL.VISIBILITY_HIDE_HELP_DESC.msg(), base + "hide", "protectionstones.hide");
        sendWithPerm(p, PSL.VISIBILITY_UNHIDE_HELP.msg(), PSL.VISIBILITY_UNHIDE_HELP_DESC.msg(), base + "unhide", "protectionstones.unhide");
        sendWithPerm(p, PSL.TOGGLE_HELP.msg(), PSL.TOGGLE_HELP_DESC.msg(), base + "toggle", "protectionstones.toggle");
        sendWithPerm(p, PSL.VIEW_HELP.msg(), PSL.VIEW_HELP_DESC.msg(), base + "view", "protectionstones.view");
        sendWithPerm(p, PSL.UNCLAIM_HELP.msg(), PSL.UNCLAIM_HELP_DESC.msg(), base + "unclaim", "protectionstones.unclaim");
        sendWithPerm(p, PSL.PRIORITY_HELP.msg(), PSL.PRIORITY_HELP_DESC.msg(), base + "priority", "protectionstones.priority");
        sendWithPerm(p, PSL.REGION_HELP.msg(), PSL.REGION_HELP_DESC.msg(), base + "region", "protectionstones.region");
        sendWithPerm(p, PSL.ADMIN_HELP.msg(), PSL.ADMIN_HELP_DESC.msg(), base + "admin", "protectionstones.admin");
        sendWithPerm(p, PSL.RELOAD_HELP.msg(), PSL.RELOAD_HELP_DESC.msg(), base + "reload", "protectionstones.admin");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return null;
    }

    private static void sendWithPerm(CommandSender p, String msg, String desc, String cmd, String... permission) {
        if (msg.equals("")) return;
        for (String perm : permission) {
            if (p.hasPermission(perm)) {
                TextComponent m = new TextComponent(msg);
                m.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, cmd));
                m.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(desc).create()));
                p.spigot().sendMessage(m);
                break;
            }
        }
    }
}
