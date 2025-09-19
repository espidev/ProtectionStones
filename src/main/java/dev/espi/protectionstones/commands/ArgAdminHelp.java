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
import dev.espi.protectionstones.PSL;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

public class ArgAdminHelp {

    private static void send(CommandSender sender, String command, String description, boolean run) {
        Component line = Component.text("> ", NamedTextColor.AQUA)
                .append(Component.text(command, NamedTextColor.GRAY))
                .hoverEvent(HoverEvent.showText(Component.text(description, NamedTextColor.WHITE)))
                .clickEvent(run
                        ? ClickEvent.runCommand(command)
                        : ClickEvent.suggestCommand(command));

        PSL.msg(sender, line);
    }

    static boolean argumentAdminHelp(CommandSender sender, String[] args) {
        String baseCommand = "/" + ProtectionStones.getInstance().getConfigOptions().base_command;

        // Header
        PSL.msg(sender, Component.empty().append(Component.text("===============", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.space())
                .append(Component.text("PS Admin Help", NamedTextColor.AQUA).decoration(TextDecoration.STRIKETHROUGH, false))
                .append(Component.space())
                .append(Component.text("===============", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH)));

        // Entries
        send(sender, baseCommand + " admin version", "Show the version number of the plugin.", false);
        send(sender, baseCommand + " admin hide", "Hide all protection stone blocks in the current world.", false);
        send(sender, baseCommand + " admin unhide", "Unhide all protection stone blocks in the current world.", false);
        send(sender, baseCommand + " admin cleanup remove", "Remove inactive players, then remove empty regions.", false);
        send(sender, baseCommand + " admin cleanup disown", "Remove inactive players from regions only.", false);
        send(sender, baseCommand + " admin flag [world] [flagname] [value|null|default]", "Set a flag for all PS regions in a world.", false);
        send(sender, baseCommand + " admin lastlogon [player]", "Get the last time a player logged on.", false);
        send(sender, baseCommand + " admin lastlogons", "List last logons of all players.", false);
        send(sender, baseCommand + " admin stats [player?]", "Show plugin statistics.", false);
        send(sender, baseCommand + " admin recreate", "Recreate all PS regions using the configured radius.", false);
        send(sender, baseCommand + " admin debug", "Toggle debug mode.", false);
        send(sender, baseCommand + " admin settaxautopayers", "Assign a tax autopayer to all regions without one.", false);
        send(sender, baseCommand + " admin forcemerge [world]", "Merge overlapping PS regions if owners/members/flags match.", false);
        send(sender, baseCommand + " admin changeblock [world] [oldtypealias] [newtypealias]", "Change all PS blocks/regions in a world to a different block.", false);
        send(sender, baseCommand + " admin changeregiontype [world] [oldtype] [newtype]", "Change the type of all PS regions of a certain type.", false);
        send(sender, baseCommand + " admin fixregions", "Recalculate block types for PS regions in a world.", false);

        // Footer
        PSL.msg(sender, Component.text("=============================================", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH));

        return true;
    }
}
