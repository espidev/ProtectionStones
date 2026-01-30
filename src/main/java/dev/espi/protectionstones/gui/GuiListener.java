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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class GuiListener implements Listener {
    private final GuiManager gui;

    GuiListener(GuiManager gui) {
        this.gui = gui;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        var top = e.getView().getTopInventory();
        if (top == null) return;
        if (!(top.getHolder() instanceof BaseGui g)) return;

        // Only handle clicks in our GUI (top inventory)
        if (e.getClickedInventory() == null || e.getClickedInventory() != top) {
            e.setCancelled(true);
            return;
        }

        e.setCancelled(true);
        g.onClick(p, e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent e) {
        var top = e.getView().getTopInventory();
        if (top == null) return;
        if (!(top.getHolder() instanceof BaseGui)) return;

        // prevent dragging into GUI
        e.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player p)) return;

        var top = e.getView().getTopInventory();
        if (top == null) return;
        if (!(top.getHolder() instanceof BaseGui g)) return;

        gui.setOpenGui(p, null);
        g.onClose(p, e);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        gui.setOpenGui(e.getPlayer(), null);
    }
}
