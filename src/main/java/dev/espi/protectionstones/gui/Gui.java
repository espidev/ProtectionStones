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

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

/**
 * Represents one GUI screen.
 */
public interface Gui {

    /**
     * Create (or rebuild) the inventory for this GUI.
     * Called right before opening.
     */
    Inventory createInventory(Player player);

    /**
     * Called after the inventory is opened.
     */
    default void onOpen(Player player) {}

    /**
     * Called when the player clicks in this GUI.
     * Tip: cancel the event if you don't want item movement.
     */
    default void onClick(Player player, InventoryClickEvent event) {}

    /**
     * Called when the player drags items in this GUI.
     */
    default void onDrag(Player player, InventoryDragEvent event) {}

    /**
     * Called when this GUI is closed.
     */
    default void onClose(Player player, InventoryCloseEvent event) {}

    /**
     * If true, clicks are cancelled by default in GuiListener.
     */
    default boolean cancelClicksByDefault() { return true; }

    /**
     * If true, drags are cancelled by default in GuiListener.
     */
    default boolean cancelDragsByDefault() { return true; }
}
