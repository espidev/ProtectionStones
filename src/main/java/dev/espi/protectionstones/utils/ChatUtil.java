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
import dev.espi.protectionstones.PSRegion;

import dev.espi.protectionstones.ProtectionStones;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ChatUtil {
    public static void displayDuplicateRegionAliases(Player p, List<PSRegion> r) {
        StringBuilder rep = new StringBuilder(r.get(0).getId() + " (" + r.get(0).getWorld().getName() + ")");

        for (int i = 1; i < r.size(); i++) {
            rep.append(String.format(", %s (%s)", r.get(i).getId(), r.get(i).getWorld().getName()));
        }

        PSL.msg(p, PSL.SPECIFY_ID_INSTEAD_OF_ALIAS.replace("%regions%", rep.toString()));
    }

    //Convert between Paper Kyori and Spigot safe Chat events
    // legacy serializer for plain fallback
    private static final LegacyComponentSerializer LEGACY_SERIALIZER =
            LegacyComponentSerializer.legacySection();

    private static final LegacyComponentSerializer AMPERSAND_SERIALIZER =
            LegacyComponentSerializer.legacyAmpersand();

    // full serializer for hover/click
    private static final BungeeComponentSerializer BUNGEE_SERIALIZER =
            BungeeComponentSerializer.get();

    /** Convert Adventure → Spigot (BaseComponent[]) preserving events */
    public static BaseComponent[] toSpigotDeep(Component comp) {
        return BUNGEE_SERIALIZER.serialize(comp);
    }

    /** Convert Adventure → plain ChatColor string (no events) */
    public static String toChatColorString(Component comp) {
        return LEGACY_SERIALIZER.serialize(comp);
    }

    /** Universal send method */
    public static boolean send(CommandSender p, Component comp) {
        if (p == null || comp == null) return false;

        if (Bukkit.getServer().getName().toLowerCase().contains("paper")) {
            ProtectionStones.getInstance().audiences().sender(p).sendMessage(comp);
        } else if (p instanceof Player player) {
            player.spigot().sendMessage(toSpigotDeep(comp)); // keep hover/click
        } else {
            p.sendMessage(toChatColorString(comp)); // console fallback
        }
        return true;
    }
    public static boolean sendActionBar(Player p, String message) {
        if (Bukkit.getServer().getName().toLowerCase().contains("paper")) {
            // Paper: Adventure API native
            Component comp = AMPERSAND_SERIALIZER.deserialize(message);
            ProtectionStones.getInstance().audiences().player(p).sendActionBar(comp);
        } else {
            // Spigot: Use Bungee API fallback
            p.spigot().sendMessage(
                    ChatMessageType.ACTION_BAR,
                    new TextComponent(ChatColor.translateAlternateColorCodes('&', message))
            );
        }
        return false;
    }
}
