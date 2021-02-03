/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.espi.protectionstones.commands;

import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.utils.Permissions;
import dev.espi.protectionstones.utils.TextGUI;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.*;

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
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.INFO_HELP.msg(), PSL.INFO_HELP_DESC.msg(), base + "info"), Permissions.INFO));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.ADDREMOVE_HELP.msg(), PSL.ADDREMOVE_HELP_DESC.msg(), base), Permissions.MEMBERS));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.ADDREMOVE_OWNER_HELP.msg(), PSL.ADDREMOVE_OWNER_HELP_DESC.msg(), base), Permissions.OWNERS));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.GET_HELP.msg(), PSL.GET_HELP_DESC.msg(), base + "get"), Permissions.GET));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.GIVE_HELP.msg(), PSL.GIVE_HELP_DESC.msg(), base + "give"), Permissions.GIVE));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.COUNT_HELP.msg(), PSL.COUNT_HELP_DESC.msg(), base + "count"), Permissions.COUNT, Permissions.COUNT__OTHERS));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.LIST_HELP.msg(), PSL.LIST_HELP_DESC.msg(), base + "list"), Permissions.LIST, Permissions.LIST__OTHERS));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.NAME_HELP.msg(), PSL.NAME_HELP_DESC.msg(), base + "name"), Permissions.NAME));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.MERGE_HELP.msg(), PSL.MERGE_HELP_DESC.msg(), base + "merge"), Permissions.MERGE));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.SETPARENT_HELP.msg(), PSL.SETPARENT_HELP_DESC.msg(), base + "setparent"), Permissions.SET_PARENT, Permissions.SET_PARENT__OTHERS));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.FLAG_HELP.msg(), PSL.FLAG_HELP_DESC.msg(), base + "flag"), Permissions.FLAGS));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.RENT_HELP.msg(), PSL.RENT_HELP_DESC.msg(), base + "rent"), Permissions.RENT));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.TAX_HELP.msg(), PSL.TAX_HELP_DESC.msg(), base + "tax"), Permissions.TAX));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.BUY_HELP.msg(), PSL.BUY_HELP_DESC.msg(), base + "buy"), Permissions.BUY_SELL));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.SELL_HELP.msg(), PSL.SELL_HELP_DESC.msg(), base + "sell"), Permissions.BUY_SELL));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.HOME_HELP.msg(), PSL.HOME_HELP_DESC.msg(), base + "home"), Permissions.HOME));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.SETHOME_HELP.msg(), PSL.SETHOME_HELP_DESC.msg(), base + "sethome"), Permissions.SET_HOME));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.TP_HELP.msg(), PSL.TP_HELP_DESC.msg(), base + "tp"), Permissions.TP));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.VISIBILITY_HIDE_HELP.msg(), PSL.VISIBILITY_HIDE_HELP_DESC.msg(), base + "hide"), Permissions.HIDE));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.VISIBILITY_UNHIDE_HELP.msg(), PSL.VISIBILITY_UNHIDE_HELP_DESC.msg(), base + "unhide"), Permissions.UNHIDE));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.TOGGLE_HELP.msg(), PSL.TOGGLE_HELP_DESC.msg(), base + "toggle"), Permissions.TOGGLE));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.VIEW_HELP.msg(), PSL.VIEW_HELP_DESC.msg(), base + "view"), Permissions.VIEW));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.UNCLAIM_HELP.msg(), PSL.UNCLAIM_HELP_DESC.msg(), base + "unclaim"), Permissions.UNCLAIM));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.PRIORITY_HELP.msg(), PSL.PRIORITY_HELP_DESC.msg(), base + "priority"), Permissions.PRIORITY));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.REGION_HELP.msg(), PSL.REGION_HELP_DESC.msg(), base + "region"), Permissions.REGION));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.ADMIN_HELP.msg(), PSL.ADMIN_HELP_DESC.msg(), base + "admin"), Permissions.ADMIN));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.RELOAD_HELP.msg(), PSL.RELOAD_HELP_DESC.msg(), base + "reload"), Permissions.ADMIN));
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
            page = Integer.parseInt(args[1]) - 1;
        }

        List<TextComponent> entries = new ArrayList<>();
        for (HelpEntry he : helpMenu) {
            for (String perm : he.permission) {
                if (p.hasPermission(perm)) {
                    entries.add(he.msg);
                    break;
                }
            }
        }

        TextGUI.displayGUI(p, PSL.HELP.msg(), "/" + ProtectionStones.getInstance().getConfigOptions().base_command + " help %page%", page, GUI_SIZE, entries, false);

        if (page * GUI_SIZE + GUI_SIZE < entries.size()) PSL.msg(p, PSL.HELP_NEXT.msg().replace("%page%", page + 2 + ""));

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
