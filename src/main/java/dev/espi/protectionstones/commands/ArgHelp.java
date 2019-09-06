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
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ArgHelp implements PSCommandArg {

    private static class HelpEntry {
        String[] permission;
        TextComponent msg;

        HelpEntry(TextComponent msg, String... permission) {
            this.permission = permission;
            this.msg = msg;
        }
    }

    public static List<HelpEntry> helpMenu = new ArrayList<>();

    public static void initHelpMenu() {
        String base = "/" + ProtectionStones.getInstance().getConfigOptions().base_command + " ";

        helpMenu.clear();
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.INFO_HELP.msg(), PSL.INFO_HELP_DESC.msg(), base + "info"), "protectionstones.info"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.ADDREMOVE_HELP.msg(), PSL.ADDREMOVE_HELP_DESC.msg(), base), "protectionstones.members"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.ADDREMOVE_OWNER_HELP.msg(), PSL.ADDREMOVE_OWNER_HELP_DESC.msg(), base), "protectionstones.owners"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.GET_HELP.msg(), PSL.GET_HELP_DESC.msg(), base + "get"), "protectionstones.get"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.GIVE_HELP.msg(), PSL.GIVE_HELP_DESC.msg(), base + "give"), "protectionstones.give"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.COUNT_HELP.msg(), PSL.COUNT_HELP_DESC.msg(), base + "count"), "protectionstones.count", "protectionstones.count.others"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.LIST_HELP.msg(), PSL.LIST_HELP_DESC.msg(), base + "list"), "protectionstones.list", "protectionstones.list.others"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.NAME_HELP.msg(), PSL.NAME_HELP_DESC.msg(), base + "name"), "protectionstones.name"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.MERGE_HELP.msg(), PSL.MERGE_HELP_DESC.msg(), base + "merge"), "protectionstones.merge"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.SETPARENT_HELP.msg(), PSL.SETPARENT_HELP_DESC.msg(), base + "setparent"), "protectionstones.setparent", "protectionstones.setparent.others"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.FLAG_HELP.msg(), PSL.FLAG_HELP_DESC.msg(), base + "flag"), "protectionstones.flags"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.HOME_HELP.msg(), PSL.HOME_HELP_DESC.msg(), base + "home"), "protectionstones.home"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.SETHOME_HELP.msg(), PSL.SETHOME_HELP_DESC.msg(), base + "sethome"), "protectionstones.sethome"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.TP_HELP.msg(), PSL.TP_HELP_DESC.msg(), base + "tp"), "protectionstones.tp"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.VISIBILITY_HIDE_HELP.msg(), PSL.VISIBILITY_HIDE_HELP_DESC.msg(), base + "hide"), "protectionstones.hide"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.VISIBILITY_UNHIDE_HELP.msg(), PSL.VISIBILITY_UNHIDE_HELP_DESC.msg(), base + "unhide"), "protectionstones.unhide"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.TOGGLE_HELP.msg(), PSL.TOGGLE_HELP_DESC.msg(), base + "toggle"), "protectionstones.toggle"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.VIEW_HELP.msg(), PSL.VIEW_HELP_DESC.msg(), base + "view"), "protectionstones.view"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.UNCLAIM_HELP.msg(), PSL.UNCLAIM_HELP_DESC.msg(), base + "unclaim"), "protectionstones.unclaim"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.PRIORITY_HELP.msg(), PSL.PRIORITY_HELP_DESC.msg(), base + "priority"), "protectionstones.priority"));
        helpMenu.add(new HelpEntry(sendWithPerm( PSL.REGION_HELP.msg(), PSL.REGION_HELP_DESC.msg(), base + "region"), "protectionstones.region"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.ADMIN_HELP.msg(), PSL.ADMIN_HELP_DESC.msg(), base + "admin"), "protectionstones.admin"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.RELOAD_HELP.msg(), PSL.RELOAD_HELP_DESC.msg(), base + "reload"), "protectionstones.admin"));
    }

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

    private static final int GUI_SIZE = 16;

    @Override
    public boolean executeArgument(CommandSender p, String[] args, HashMap<String, String> flags) {
        int page = 0;
        if (args.length > 1 && StringUtils.isNumeric(args[1])) {
            page = Integer.parseInt(args[1])-1;
        }

        p.sendMessage(PSL.HELP.msg());

        // display help items
        int i = 0;
        for (HelpEntry he : helpMenu) {
            for (String perm : he.permission) {
                if (p.hasPermission(perm)) {
                    if (i >= GUI_SIZE*page && i < GUI_SIZE*(page+1)) {
                        p.spigot().sendMessage(he.msg);
                    }
                    i++;
                    break;
                }
            }
        }

        // footer page buttons
        TextComponent backPage = new TextComponent(ChatColor.AQUA + " <<"), nextPage = new TextComponent(ChatColor.AQUA + ">> ");
        backPage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(PSL.GO_BACK_PAGE.msg()).create()));
        nextPage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(PSL.GO_NEXT_PAGE.msg()).create()));
        backPage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + ProtectionStones.getInstance().getConfigOptions().base_command + " help " + (page)));
        nextPage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + ProtectionStones.getInstance().getConfigOptions().base_command + " help " + (page + 2)));

        TextComponent footer = new TextComponent(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "=====" + ChatColor.RESET);
        // add back page button if the page isn't 0
        if (page != 0) footer.addExtra(backPage);
        // add page number
        footer.addExtra(new TextComponent(ChatColor.WHITE + " " + (page + 1) + " "));
        // add forward page button if the page isn't last
        if (page * GUI_SIZE + GUI_SIZE < i) footer.addExtra(nextPage);
        footer.addExtra(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "=====");

        p.spigot().sendMessage(footer);
        if (page * GUI_SIZE + GUI_SIZE < i) PSL.msg(p, PSL.HELP_NEXT.msg().replace("%page%", page+2 + "")); // TODO

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return null;
    }

    private static TextComponent sendWithPerm(String msg, String desc, String cmd) {
        TextComponent m = new TextComponent(msg);
        m.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, cmd));
        m.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(desc).create()));
        return m;
    }
}
