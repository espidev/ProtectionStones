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

/** Simple loading screen GUI used while running async region lookups. */
public class LoadingGui extends BaseGui {

    public LoadingGui(GuiManager gui, String title) {
        super(gui, 27, ChatColor.DARK_GRAY + title);
    }

    @Override
    protected void draw(Player viewer) {
        inv.clear();
        inv.setItem(13, GuiItems.item(Material.CLOCK, "&eLoading...", "&7Please wait"));
        inv.setItem(22, GuiItems.item(Material.BARRIER, "&cClose"));
    }

    @Override
    protected void onClick(Player viewer, InventoryClickEvent e) {
        if (e.getRawSlot() == 22) {
            gui.close(viewer);
        }
    }
}
