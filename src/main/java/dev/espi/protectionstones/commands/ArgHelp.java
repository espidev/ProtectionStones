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
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class ArgHelp implements PSCommandArg{
    @Override
    public List<String> getNames() {
        return Collections.singletonList("help");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public boolean executeArgument(CommandSender p, String[] args) {
        p.sendMessage(PSL.HELP.msg());
        sendWithPerm(p, PSL.INFO_HELP.msg(), PSL.INFO_HELP_DESC.msg(), "/ps info", "protectionstones.info");
        sendWithPerm(p, PSL.ADDREMOVE_HELP.msg(), PSL.ADDREMOVE_HELP_DESC.msg(), "/ps", "protectionstones.members");
        sendWithPerm(p, PSL.ADDREMOVE_OWNER_HELP.msg(), PSL.ADDREMOVE_OWNER_HELP_DESC.msg(), "/ps", "protectionstones.owners");
        sendWithPerm(p, PSL.GET_HELP.msg(), PSL.GET_HELP_DESC.msg(), "/ps get", "protectionstones.get");
        sendWithPerm(p, PSL.GIVE_HELP.msg(), PSL.GIVE_HELP_DESC.msg(), "/ps give", "protectionstones.give");
        sendWithPerm(p, PSL.COUNT_HELP.msg(), PSL.COUNT_HELP_DESC.msg(), "/ps count", "protectionstones.count", "protectionstones.count.others");
        sendWithPerm(p, PSL.LIST_HELP.msg(), PSL.LIST_HELP_DESC.msg(), "/ps list", "protectionstones.list", "protectionstones.list.others");
        sendWithPerm(p, PSL.NAME_HELP.msg(), PSL.NAME_HELP_DESC.msg(), "/ps name", "protectionstones.name");
        sendWithPerm(p, PSL.SETPARENT_HELP.msg(), PSL.SETPARENT_HELP_DESC.msg(), "/ps setparent", "protectionstones.setparent", "protectionstones.setparent.others");
        sendWithPerm(p, PSL.FLAG_HELP.msg(), PSL.FLAG_HELP_DESC.msg(), "/ps flag", "protectionstones.flags");
        sendWithPerm(p, PSL.HOME_HELP.msg(), PSL.HOME_HELP_DESC.msg(), "/ps home", "protectionstones.home");
        sendWithPerm(p, PSL.SETHOME_HELP.msg(), PSL.SETHOME_HELP_DESC.msg(), "/ps sethome", "protectionstones.sethome");
        sendWithPerm(p, PSL.TP_HELP.msg(), PSL.TP_HELP_DESC.msg(), "/ps tp", "protectionstones.tp");
        sendWithPerm(p, PSL.VISIBILITY_HIDE_HELP.msg(), PSL.VISIBILITY_HIDE_HELP_DESC.msg(), "/ps hide", "protectionstones.hide");
        sendWithPerm(p, PSL.VISIBILITY_UNHIDE_HELP.msg(), PSL.VISIBILITY_UNHIDE_HELP_DESC.msg(), "/ps unhide", "protectionstones.unhide");
        sendWithPerm(p, PSL.TOGGLE_HELP.msg(), PSL.TOGGLE_HELP_DESC.msg(), "/ps toggle", "protectionstones.toggle");
        sendWithPerm(p, PSL.VIEW_HELP.msg(), PSL.VIEW_HELP_DESC.msg(), "/ps view", "protectionstones.view");
        sendWithPerm(p, PSL.UNCLAIM_HELP.msg(), PSL.UNCLAIM_HELP_DESC.msg(), "/ps unclaim", "protectionstones.unclaim");
        sendWithPerm(p, PSL.PRIORITY_HELP.msg(), PSL.PRIORITY_HELP_DESC.msg(), "/ps priority", "protectionstones.priority");
        sendWithPerm(p, PSL.REGION_HELP.msg(), PSL.REGION_HELP_DESC.msg(), "/ps region", "protectionstones.region");
        sendWithPerm(p, PSL.ADMIN_HELP.msg(), PSL.ADMIN_HELP_DESC.msg(), "/ps admin", "protectionstones.admin");
        sendWithPerm(p, PSL.RELOAD_HELP.msg(), PSL.RELOAD_HELP_DESC.msg(), "/ps reload", "protectionstones.admin");
        return true;
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
