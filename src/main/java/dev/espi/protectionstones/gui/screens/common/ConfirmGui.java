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

package dev.espi.protectionstones.gui.screens.common;

import dev.espi.protectionstones.gui.BaseGui;
import dev.espi.protectionstones.gui.GuiItems;
import dev.espi.protectionstones.gui.GuiManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** Generic confirmation GUI for running an action. */
public class ConfirmGui extends BaseGui {

    private final Material icon;
    private final String name;
    private final String[] lore;
    private final Consumer<Player> onConfirm;
    private final Supplier<BaseGui> back;
    private final Supplier<BaseGui> afterConfirm;

    public ConfirmGui(
            GuiManager gui,
            String title,
            Material icon,
            String name,
            String[] lore,
            Consumer<Player> onConfirm,
            Supplier<BaseGui> back,
            Supplier<BaseGui> afterConfirm
    ) {
        super(gui, 27, ChatColor.DARK_GRAY + title);
        this.icon = icon == null ? Material.PAPER : icon;
        this.name = name;
        this.lore = lore;
        this.onConfirm = onConfirm;
        this.back = back;
        this.afterConfirm = afterConfirm;
    }

    @Override
    protected void draw(Player viewer) {
        inv.clear();

        inv.setItem(13, GuiItems.item(icon, name, lore));
        inv.setItem(11, GuiItems.item(Material.LIME_WOOL, "&aConfirm"));
        inv.setItem(15, GuiItems.item(Material.RED_WOOL, "&cCancel"));

        if (back != null) inv.setItem(18, GuiItems.item(Material.ARROW, "&bBack"));
        inv.setItem(22, GuiItems.item(Material.BARRIER, "&cClose"));
    }

    @Override
    protected void onClick(Player viewer, InventoryClickEvent e) {
        int raw = e.getRawSlot();

        if (raw == 22) {
            gui.close(viewer);
            return;
        }

        if (raw == 18 && back != null) {
            gui.open(viewer, back.get());
            return;
        }

        if (raw == 15) {
            if (back != null) gui.open(viewer, back.get());
            else gui.close(viewer);
            return;
        }

        if (raw != 11) return;

        try {
            if (onConfirm != null) onConfirm.accept(viewer);
        } finally {
            if (afterConfirm != null) gui.open(viewer, afterConfirm.get());
            else gui.close(viewer);
        }
    }
}
