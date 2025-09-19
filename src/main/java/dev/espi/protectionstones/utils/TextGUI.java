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

package dev.espi.protectionstones.utils;

import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.ProtectionStones;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

import static org.enginehub.piston.impl.LogManagerCompat.getLogger;

public class TextGUI {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    // page starts at zero, but displays start at one
    // pageCommand will be replacing %page%
    public static void displayGUI(
            CommandSender s,
            Component header,
            String pageCommand,
            int currentPage,
            int guiSize,
            List<Component> lines,
            boolean sendBlankLines
    ){
        final int start = currentPage * guiSize;
        final int end   = Math.min(start + guiSize, lines.size());

        if (currentPage < 0 || start > lines.size()) return;

        // header
        PSL.msg(s, header);

        // page body
        for (int i = start; i < end; i++) {
            Component line = lines.get(i);
            if (sendBlankLines || !isBlank(line)) {
                ProtectionStones.getInstance().audiences().sender(s).sendMessage(line);
            }
        }

        // footer with paging
        final boolean hasPrev = currentPage != 0;
        final boolean hasNext = (currentPage + 1) * guiSize < lines.size();

        if (lines.size() >= guiSize) {
            Component footer = Component.empty().append(Component.text("=====", NamedTextColor.DARK_GRAY)
                    .decoration(TextDecoration.STRIKETHROUGH, true));

            if (hasPrev) {
                Component back = Component.text(" <<", NamedTextColor.AQUA)
                        .decoration(TextDecoration.STRIKETHROUGH, false) // turn off strikethrough
                        .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(PSL.GO_BACK_PAGE.msg()))
                        .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand(pageCommand.replace("%page%", String.valueOf(currentPage))));
                footer = footer.append(back);
            }

            footer = footer.appendSpace()
                    .decoration(TextDecoration.STRIKETHROUGH, false)
                    .append(Component.text(currentPage + 1, NamedTextColor.WHITE))
                    .appendSpace();

            if (hasNext) {
                Component next = Component.text(">> ", NamedTextColor.AQUA)
                        .decoration(TextDecoration.STRIKETHROUGH, false)
                        .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(PSL.GO_NEXT_PAGE.msg()))
                        .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand(pageCommand.replace("%page%", String.valueOf(currentPage + 2))));
                footer = footer.append(next);
            }

            footer = footer.append(Component.text("=====", NamedTextColor.DARK_GRAY)
                    .decoration(TextDecoration.STRIKETHROUGH, true));

            PSL.msg(s, footer);
            getLogger().info(GsonComponentSerializer.gson().serialize(footer));
        }

    }

    private static boolean isBlank(Component c) {
        return PLAIN.serialize(c).isBlank();
    }
}
