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

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public abstract class BaseGui implements InventoryHolder {
    protected final GuiManager gui;
    protected final int size; // must be multiple of 9
    protected final String title;

    protected Inventory inv;

    protected BaseGui(GuiManager gui, int size, String title) {
        this.gui = gui;
        this.size = size;
        this.title = title;
    }

    public final void open(Player player) {
        this.inv = Bukkit.createInventory(this, size, title);
        draw(player);
        gui.setOpenGui(player, this);
        player.openInventory(inv);
    }

    /** Populate inventory contents. Called each time the GUI opens. */
    protected abstract void draw(Player viewer);

    /** Called by listener. */
    protected abstract void onClick(Player viewer, InventoryClickEvent e);

    /** Called by listener. Optional. */
    protected void onClose(Player viewer, InventoryCloseEvent e) {}

    @Override
    public final Inventory getInventory() {
        return inv;
    }
}

