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

import dev.espi.protectionstones.ProtectionStones;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ArgAdminHelp {

    private static void send(CommandSender p, String text, String info) {
        TextComponent tc = new TextComponent(text);
        tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(info).create()));
        p.spigot().sendMessage(tc);
    }

    static boolean argumentAdminHelp(CommandSender p, String[] args) {
        String bc = "/" + ProtectionStones.getInstance().getConfigOptions().base_command;

        p.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "=====" + ChatColor.RESET + " PS Admin Help " + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "=====\n" + ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps admin help");
        send(p, ChatColor.AQUA + "> " + ChatColor.GRAY + bc + " admin version", "Show the version number of the plugin.");
        send(p, ChatColor.AQUA + "> " + ChatColor.GRAY + bc + " admin hide", "Hide all of the protection stone blocks in the world you are in.");
        send(p, ChatColor.AQUA + "> " + ChatColor.GRAY + bc + " admin unhide", "Unhide all of the protection stone blocks in the world you are in.");
        send(p, ChatColor.AQUA + "> " + ChatColor.GRAY + bc + " admin cleanup remove [days] [-t typealias (optional)] [world (console)]", "Remove inactive players that haven't joined within the last [days] days from protected regions in the world you are in (or specified). Then, remove any regions with no owners left.");
        send(p, ChatColor.AQUA + "> " + ChatColor.GRAY + bc + " admin cleanup disown [days] [-t typealias (optional)] [world (console)]", "Remove inactive players that haven't joined within the last [days] days from protected regions in the world you are in (or specified).");
        send(p, ArgAdmin.getFlagHelp(), "Set a flag for all protection stone regions in a world.");
        send(p, ChatColor.AQUA + "> " + ChatColor.GRAY + bc + " admin lastlogon [player]", "Get the last time a player logged on.");
        send(p, ChatColor.AQUA + "> " + ChatColor.GRAY + bc + " admin lastlogons", "List all of the last logons of each player.");
        send(p, ChatColor.AQUA + "> " + ChatColor.GRAY + bc + " admin stats [player (optional)]", "Show some statistics of the plugin.");
        send(p, ChatColor.AQUA + "> " + ChatColor.GRAY + bc + " admin recreate", "Recreate all PS regions using radius set in config.");
        send(p, ChatColor.AQUA + "> " + ChatColor.GRAY + bc + " admin debug", "Toggles debug mode.");
        send(p, ChatColor.AQUA + "> " + ChatColor.GRAY + bc + " admin settaxautopayers", "Add a tax autopayer for every region on the server that does not have one.");
        send(p, ArgAdmin.getForceMergeHelp(), "Merge overlapping PS regions together if they have the same owners, members and flags.");
        send(p, ArgAdmin.getChangeBlockHelp(), "Change all of the PS blocks and regions in a world to a different block. Both blocks must be configured in config.");
        send(p, ArgAdmin.getChangeRegionTypeHelp(), "Change the internal type of all PS regions of a certain type. Useful for error correction.");
        send(p, ChatColor.AQUA + "> " + ChatColor.GRAY + bc + " admin fixregions", "Use this command to recalculate block types for PS regions in a world.");

        return true;
    }
}
