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

package dev.espi.protectionstones.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/** Small helper for building GUI items consistently. */
public final class GuiItems {
    private GuiItems() {}

    public static ItemStack item(Material material, String name, List<String> lore) {
        ItemStack is = new ItemStack(material);
        ItemMeta im = is.getItemMeta();
        if (im != null) {
            if (name != null) im.setDisplayName(name);
            if (lore != null) {
                List<String> colored = new ArrayList<>(lore.size());
                for (String line : lore) colored.add(color(line));
                im.setLore(colored);
            }
            im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_PLACED_ON);
            is.setItemMeta(im);
        }
        return is;
    }

    public static ItemStack item(Material material, String name, String... lore) {
        List<String> l = null;
        if (lore != null) {
            l = new ArrayList<>();
            for (String s : lore) l.add(color(s));
        }
        return item(material, name == null ? null : color(name), l);
    }


    public static ItemStack playerHead(java.util.UUID uuid, String name, String... lore) {
        ItemStack is = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta im = is.getItemMeta();
        if (im instanceof org.bukkit.inventory.meta.SkullMeta sm) {
            try {
                sm.setOwningPlayer(org.bukkit.Bukkit.getOfflinePlayer(uuid));
            } catch (Exception ignored) {}
            if (name != null) sm.setDisplayName(color(name));
            if (lore != null) {
                List<String> l = new ArrayList<>();
                for (String s : lore) l.add(color(s));
                sm.setLore(l);
            }
            sm.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_PLACED_ON);
            is.setItemMeta(sm);
            return is;
        }
        // fallback
        return item(Material.PLAYER_HEAD, name, lore);
    }

    public static String color(String s) {
        if (s == null) return null;
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
