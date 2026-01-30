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

package dev.espi.protectionstones.gui.screens.home;

import dev.espi.protectionstones.PSPlayer;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.commands.ArgTp;
import dev.espi.protectionstones.gui.BaseGui;
import dev.espi.protectionstones.gui.GuiItems;
import dev.espi.protectionstones.gui.GuiManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

/** World -> homes list. */
public class HomeListGui extends BaseGui {

    private static final int SIZE = 54;
    private static final int PER_PAGE = 45;

    private final World world;
    private final int page;

    public HomeListGui(GuiManager gui, World world, int page) {
        super(gui, SIZE, ChatColor.DARK_GRAY + "Homes - " + world.getName());
        this.world = world;
        this.page = Math.max(0, page);
    }

    @Override
    protected void draw(Player viewer) {
        inv.clear();

        PSPlayer psp = PSPlayer.fromPlayer(viewer);
        List<PSRegion> homes = psp.getHomes(world);

        int start = page * PER_PAGE;
        int end = Math.min(homes.size(), start + PER_PAGE);

        int slot = 0;
        for (int i = start; i < end; i++) {
            PSRegion r = homes.get(i);
            String display = (r.getName() == null ? r.getId() : (r.getName() + " (" + r.getId() + ")"));
            inv.setItem(slot++, GuiItems.item(
                    Material.COMPASS,
                    "&b" + display,
                    "&7World: &f" + world.getName(),
                    "&8Click to teleport"
            ));
        }

        int base = SIZE - 9;
        boolean hasPrev = page > 0;
        boolean hasNext = end < homes.size();

        if (hasPrev) inv.setItem(base, GuiItems.item(Material.ARROW, "&aPrevious", "&7Page " + page));
        inv.setItem(base + 3, GuiItems.item(Material.OAK_DOOR, "&eBack", "&7Back to worlds"));
        inv.setItem(base + 4, GuiItems.item(Material.BARRIER, "&cClose"));
        if (hasNext) inv.setItem(SIZE - 1, GuiItems.item(Material.ARROW, "&aNext", "&7Page " + (page + 2)));
    }

    @Override
    protected void onClick(Player viewer, InventoryClickEvent e) {
        int raw = e.getRawSlot();

        // nav row
        int base = SIZE - 9;
        if (raw == base + 4) {
            gui.close(viewer);
            return;
        }
        if (raw == base + 3) {
            gui.open(viewer, new HomeWorldsGui(gui, 0));
            return;
        }
        if (raw == base && page > 0) {
            gui.open(viewer, new HomeListGui(gui, world, page - 1));
            return;
        }
        if (raw == SIZE - 1) {
            gui.open(viewer, new HomeListGui(gui, world, page + 1));
            return;
        }

        // entry click
        if (raw < PER_PAGE) {
            PSPlayer psp = PSPlayer.fromPlayer(viewer);
            List<PSRegion> homes = psp.getHomes(world);

            int index = page * PER_PAGE + raw;
            if (index < 0 || index >= homes.size()) return;

            ArgTp.teleportPlayer(viewer, homes.get(index));
            gui.close(viewer);
        }
    }
}
