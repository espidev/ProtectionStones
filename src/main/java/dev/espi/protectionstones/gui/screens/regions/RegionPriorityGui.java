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

package dev.espi.protectionstones.gui.screens.regions;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.gui.BaseGui;
import dev.espi.protectionstones.gui.GuiItems;
import dev.espi.protectionstones.gui.GuiManager;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Small GUI to view and adjust region priority. */
public class RegionPriorityGui extends BaseGui {

    private final UUID worldId;
    private final String regionId;
    private final Supplier<BaseGui> back;

    public RegionPriorityGui(GuiManager gui, UUID worldId, String regionId, Supplier<BaseGui> back) {
        super(gui, 27, ChatColor.DARK_GRAY + "Priority");
        this.worldId = worldId;
        this.regionId = regionId;
        this.back = back;
    }

    @Override
    protected void draw(Player viewer) {
        inv.clear();
        PSRegion r = resolveRegion();
        if (r == null) {
            inv.setItem(13, GuiItems.item(Material.BARRIER, "&cRegion not found"));
            inv.setItem(22, GuiItems.item(Material.BARRIER, "&cClose"));
            return;
        }

        int priority = r.getWGRegion().getPriority();

        inv.setItem(13, GuiItems.item(Material.PAPER, "&eCurrent Priority", "&f" + priority, "&8Higher = takes precedence"));

        inv.setItem(10, GuiItems.item(Material.RED_DYE, "&c-10"));
        inv.setItem(11, GuiItems.item(Material.RED_DYE, "&c-1"));
        inv.setItem(15, GuiItems.item(Material.LIME_DYE, "&a+1"));
        inv.setItem(16, GuiItems.item(Material.LIME_DYE, "&a+10"));

        inv.setItem(22, GuiItems.item(Material.BARRIER, "&cClose"));
        if (back != null) inv.setItem(18, GuiItems.item(Material.ARROW, "&bBack"));
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

        PSRegion r = resolveRegion();
        if (r == null) {
            gui.close(viewer);
            return;
        }

        int delta = 0;
        if (raw == 10) delta = -10;
        else if (raw == 11) delta = -1;
        else if (raw == 15) delta = 1;
        else if (raw == 16) delta = 10;
        else return;

        int newPriority = r.getWGRegion().getPriority() + delta;
        r.getWGRegion().setPriority(newPriority);
        PSL.msg(viewer, PSL.PRIORITY_SET.msg());

        gui.open(viewer, new RegionPriorityGui(gui, worldId, regionId, back));
    }

    private PSRegion resolveRegion() {
        World w = Bukkit.getWorld(worldId);
        if (w == null) return null;
        RegionManager rm = WGUtils.getRegionManagerWithWorld(w);
        if (rm == null) return null;
        ProtectedRegion pr = rm.getRegion(regionId);
        if (pr == null) return null;
        return PSRegion.fromWGRegion(w, pr);
    }
}
