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
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.gui.BaseGui;
import dev.espi.protectionstones.gui.GuiItems;
import dev.espi.protectionstones.gui.GuiManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** /ps home (GUI mode): select a world, then select a home within that world. */
public class HomeWorldsGui extends BaseGui {

    private static final int SIZE = 54; // 6 rows
    private static final int PER_PAGE = 45; // top 5 rows

    private final int page;

    public HomeWorldsGui(GuiManager gui, int page) {
        super(gui, SIZE, ChatColor.DARK_GRAY + "Homes - Select World");
        this.page = Math.max(0, page);
    }

    @Override
    protected void draw(Player viewer) {
        inv.clear();

        PSPlayer psp = PSPlayer.fromPlayer(viewer);

        List<World> worlds = new ArrayList<>(Bukkit.getWorlds());
        worlds.sort(Comparator.comparing(World::getName, String.CASE_INSENSITIVE_ORDER));

        int start = page * PER_PAGE;
        int end = Math.min(worlds.size(), start + PER_PAGE);

        // entries
        int slot = 0;
        for (int i = start; i < end; i++) {
            World w = worlds.get(i);
            List<PSRegion> homes = psp.getHomes(w);
            Material icon = worldIcon(w);
            inv.setItem(slot++, GuiItems.item(
                    icon,
                    "&b" + w.getName(),
                    "&7Homes: &f" + homes.size(),
                    "&8Click to view homes"
            ));
        }

        // nav row
        int base = SIZE - 9;
        boolean hasPrev = page > 0;
        boolean hasNext = end < worlds.size();

        if (hasPrev) inv.setItem(base, GuiItems.item(Material.ARROW, "&aPrevious", "&7Page " + page));
        inv.setItem(base + 4, GuiItems.item(Material.BARRIER, "&cClose"));
        if (hasNext) inv.setItem(SIZE - 1, GuiItems.item(Material.ARROW, "&aNext", "&7Page " + (page + 2)));
    }

    @Override
    protected void onClick(Player viewer, InventoryClickEvent e) {
        int raw = e.getRawSlot();

        // close
        if (raw == (SIZE - 9) + 4) {
            gui.close(viewer);
            return;
        }

        // prev
        if (raw == (SIZE - 9) && page > 0) {
            gui.open(viewer, new HomeWorldsGui(gui, page - 1));
            return;
        }

        // next
        if (raw == (SIZE - 1)) {
            gui.open(viewer, new HomeWorldsGui(gui, page + 1));
            return;
        }

        // entry click
        if (raw < PER_PAGE) {
            int worldIndex = page * PER_PAGE + raw;
            List<World> worlds = new ArrayList<>(Bukkit.getWorlds());
            worlds.sort(Comparator.comparing(World::getName, String.CASE_INSENSITIVE_ORDER));
            if (worldIndex < 0 || worldIndex >= worlds.size()) return;

            World w = worlds.get(worldIndex);
            gui.open(viewer, new HomeListGui(gui, w, 0));
        }
    }

    private Material worldIcon(World w) {
        switch (w.getEnvironment()) {
            case NETHER:
                return Material.NETHERRACK;
            case THE_END:
                return Material.END_STONE;
            default:
                return Material.GRASS_BLOCK;
        }
    }
}
