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
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ArgAdminHelp {

    // Provided helper method to generate a dot string.
    private static String getDots(int num) {
        StringBuilder str = new StringBuilder(" " + ChatColor.DARK_GRAY);
        for (int i = 0; i < num; i++) {
            str.append(".");
        }
        return str.toString();
    }

    /**
     * Sends a help message to the sender with a right-aligned clickable tag.
     * This version allows you to manually supply the exact number of dots.
     *
     * @param p            The CommandSender.
     * @param text         The left-side command text (short form).
     * @param info         The detailed info for hover (contains full command usage).
     * @param clickCommand The command to run or suggest (should be unformatted).
     * @param run          If true the tag will execute the command; if false it will suggest it.
     * @param manualDots   The manual number of dots (filler) to add before the tag.
     */
    private static void send(CommandSender p, String text, String info, String clickCommand, boolean run, int manualDots) {
        // Create the main text component from legacy text.
        BaseComponent[] mainComponents = TextComponent.fromLegacyText(text);
        TextComponent mainText = new TextComponent("");
        for (BaseComponent component : mainComponents) {
            mainText.addExtra(component);
        }

        // Determine the tag label.
        String tagLabel = run ? "[Run]" : "[Prep]";

        // Use the provided manual dot count.
        String dotsString = getDots(manualDots);

        // Create a dots component.
        BaseComponent[] dotsComponents = TextComponent.fromLegacyText(dotsString);
        TextComponent dotsText = new TextComponent("");
        for (BaseComponent component : dotsComponents) {
            dotsText.addExtra(component);
        }

        // Create the clickable tag component.
        BaseComponent[] tagComponents = TextComponent.fromLegacyText(tagLabel);
        TextComponent clickableComponent = new TextComponent("");
        for (BaseComponent component : tagComponents) {
            clickableComponent.addExtra(component);
        }

        // Create the hover event from the info text.
        BaseComponent[] hoverComponents = TextComponent.fromLegacyText(info);
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponents);

        // Set the same hover event on all parts.
        mainText.setHoverEvent(hoverEvent);
        dotsText.setHoverEvent(hoverEvent);
        clickableComponent.setHoverEvent(hoverEvent);

        // Setup the click event on the [Run/Prep] tag.
        if (run) {
            clickableComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ChatColor.stripColor(clickCommand)));
        } else {
            clickableComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ChatColor.stripColor(clickCommand)));
        }

        // Append the dots and clickable tag to the main text.
        mainText.addExtra(dotsText);
        mainText.addExtra(clickableComponent);

        // Send the assembled message.
        p.spigot().sendMessage(mainText);
    }

    static boolean argumentAdminHelp(CommandSender p, String[] args) {
        String baseCommand = ProtectionStones.getInstance().getConfigOptions().base_command;
        String bc = "/" + baseCommand;
        String tx = ChatColor.AQUA + "> " + ChatColor.GRAY + bc;

        // Display header as before.
        p.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "===============" +
                ChatColor.RESET + " PS Admin Help " +
                ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "===============\n");

        // Now you can manually supply each dot count.
        send(p,
                tx + " admin version",
                "Show the version number of the plugin.\n\n" + bc + " admin version",
                baseCommand + " admin version",
                true, 61);

        send(p,
                tx + " admin hide",
                "Hide all of the protection stone blocks in the world you are in.\n\n" + bc + " admin hide",
                bc + " admin hide",
                true, 70);

        send(p,
                tx + " admin unhide",
                "Unhide all of the protection stone blocks in the world you are in.\n\n" + bc + " admin unhide",
                bc + " admin unhide",
                true, 64);

        send(p,
                tx + " admin cleanup remove",
                "Remove inactive players that haven't joined within the last [days] days from protected regions in the world you are in (or specified). Then, remove any regions with no owners left.\n\n" +
                        bc + " admin cleanup remove [days] [-t typealias (optional)] [world (console)]",
                bc + " admin cleanup remove",
                false, 39);

        send(p,
                tx + " admin cleanup disown",
                "Remove inactive players that haven't joined within the last [days] days from protected regions in the world you are in (or specified).\n\n" +
                        bc + " admin cleanup disown",
                bc + " admin cleanup disown",
                false, 41);

        send(p,
                tx + " admin flag",
                "Set a flag for all protection stone regions in a world.\n\n" +
                        bc + " admin flag [world] [flagname] [value|null|default]",
                bc + " admin flag [world] [flagname] [value|null|default]",
                false, 69);

        send(p,
                tx + " admin lastlogon",
                "Get the last time a player logged on.\n\n" + bc + " admin lastlogon [player]",
                bc + " admin lastlogon",
                false, 56);

        send(p,
                tx + " admin lastlogons",
                "List all of the last logons of each player.\n\n" + bc + " admin lastlogons",
                bc + " admin lastlogons",
                true, 55);

        send(p,
                tx + " admin stats",
                "Show some statistics of the plugin.\n\n" + bc + " admin stats [player (optional)]",
                bc + " admin stats",
                true, 68);

        send(p,
                tx + " admin recreate",
                "Recreate all PS regions using radius set in config.\n\n" + bc + " admin recreate",
                bc + " admin recreate",
                true, 58);

        send(p,
                tx + " admin debug",
                "Toggle debug mode.\n\n" + bc + " admin debug",
                bc + " admin debug",
                false, 64);

        send(p,
                tx + " admin settaxautopayers",
                "Add a tax autopayer for every region on the server that does not have one.\n\n" + bc + " admin settaxautopayers",
                bc + " admin settaxautopayers",
                false, 34);

        send(p,
                tx + " admin forcemerge",
                "Merge overlapping PS regions together if they have the same owners, members and flags.\n\n" +
                        bc + " admin forcemerge [world]",
                bc + " admin forcemerge [world]",
                true, 52);

        send(p,
                tx + " admin changeblock",
                "Change all of the PS blocks and regions in a world to a different block. Both blocks must be configured in config.\n\n" +
                        bc + " admin changeblock [world] [oldtypealias] [newtypealias]",
                bc + " admin changeblock [world] [oldtypealias] [newtypealias]",
                false, 49);

        send(p,
                tx + " admin changeregiontype",
                "Change the internal type of all PS regions of a certain type. Useful for error correction.\n\n" +
                        bc + " admin changeregiontype [world] [oldtype] [newtype]",
                bc + " admin changeregiontype [world] [oldtype] [newtype]",
                false, 34);

        send(p,
                tx + " admin fixregions",
                "Use this command to recalculate block types for PS regions in a world.\n\n" + bc + " admin fixregions",
                bc + " admin fixregions",
                true, 56);
        p.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "=============================================");

        return true;
    }
}
