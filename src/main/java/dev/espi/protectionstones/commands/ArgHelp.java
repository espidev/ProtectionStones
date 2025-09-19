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
import dev.espi.protectionstones.utils.MiscUtil;
import dev.espi.protectionstones.utils.TextGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ArgHelp implements PSCommandArg {

    private static class HelpEntry {
        String[] permission;
        Component msg;

        HelpEntry(Component msg, String... permission) {
            this.permission = permission;
            this.msg = msg;
        }
    }

    public static List<HelpEntry> helpMenu = new ArrayList<>();

    public static void initHelpMenu() {
        final String base = "/" + ProtectionStones.getInstance().getConfigOptions().base_command + " ";

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
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.RENT_HELP.msg(), PSL.RENT_HELP_DESC.msg(), base + "rent"), "protectionstones.rent"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.TAX_HELP.msg(), PSL.TAX_HELP_DESC.msg(), base + "tax"), "protectionstones.tax"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.BUY_HELP.msg(), PSL.BUY_HELP_DESC.msg(), base + "buy"), "protectionstones.buysell"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.SELL_HELP.msg(), PSL.SELL_HELP_DESC.msg(), base + "sell"), "protectionstones.buysell"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.HOME_HELP.msg(), PSL.HOME_HELP_DESC.msg(), base + "home"), "protectionstones.home"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.SETHOME_HELP.msg(), PSL.SETHOME_HELP_DESC.msg(), base + "sethome"), "protectionstones.sethome"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.TP_HELP.msg(), PSL.TP_HELP_DESC.msg(), base + "tp"), "protectionstones.tp"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.VISIBILITY_HIDE_HELP.msg(), PSL.VISIBILITY_HIDE_HELP_DESC.msg(), base + "hide"), "protectionstones.hide"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.VISIBILITY_UNHIDE_HELP.msg(), PSL.VISIBILITY_UNHIDE_HELP_DESC.msg(), base + "unhide"), "protectionstones.unhide"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.TOGGLE_HELP.msg(), PSL.TOGGLE_HELP_DESC.msg(), base + "toggle"), "protectionstones.toggle"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.VIEW_HELP.msg(), PSL.VIEW_HELP_DESC.msg(), base + "view"), "protectionstones.view"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.UNCLAIM_HELP.msg(), PSL.UNCLAIM_HELP_DESC.msg(), base + "unclaim"), "protectionstones.unclaim"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.PRIORITY_HELP.msg(), PSL.PRIORITY_HELP_DESC.msg(), base + "priority"), "protectionstones.priority"));
        helpMenu.add(new HelpEntry(sendWithPerm(PSL.REGION_HELP.msg(), PSL.REGION_HELP_DESC.msg(), base + "region"), "protectionstones.region"));
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
        if (args.length > 1 && MiscUtil.isValidInteger(args[1])) {
            page = Integer.parseInt(args[1]) - 1;
        }

        // Build visible entries based on permissions, skip “blank” (plain-text empty) lines
        final PlainTextComponentSerializer plain = PlainTextComponentSerializer.plainText();
        List<Component> entries = new ArrayList<>();
        for (HelpEntry he : helpMenu) {
            Component line = he.msg;
            if (line == null) continue;

            // ignore blank lines (no visible text)
            if (plain.serialize(line).isBlank()) continue;

            // check player permissions
            for (String perm : he.permission) {
                if (p.hasPermission(perm)) {
                    entries.add(line);
                    break;
                }
            }
        }

        TextGUI.displayGUI(
                p,
                PSL.HELP.msg(),
                "/" + ProtectionStones.getInstance().getConfigOptions().base_command + " help %page%",
                page,
                GUI_SIZE,
                entries,
                false
        );

        if (page >= 0 && page * GUI_SIZE + GUI_SIZE < entries.size()) {
            PSL.msg(p, PSL.HELP_NEXT.replace("%page%", String.valueOf(page + 2)));
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return null;
    }

    /** Adventure version: clickable + hoverable line. */
    private static Component sendWithPerm(Component title, Component description, String cmd) {
        return title
                .clickEvent(ClickEvent.suggestCommand(cmd))
                .hoverEvent(HoverEvent.showText(description));
    }
}
