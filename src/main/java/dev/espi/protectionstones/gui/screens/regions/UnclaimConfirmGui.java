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

/** Confirmation GUI for unclaiming a region. */
public class UnclaimConfirmGui extends BaseGui {

    private final UUID worldId;
    private final String regionId;
    private final Supplier<BaseGui> back;

    public UnclaimConfirmGui(GuiManager gui, UUID worldId, String regionId, Supplier<BaseGui> back) {
        super(gui, 27, ChatColor.DARK_GRAY + "Confirm Unclaim");
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

        String display = r.getName() == null ? r.getId() : (r.getName() + " (" + r.getId() + ")");

        inv.setItem(13, GuiItems.item(Material.TNT, "&cUnclaim", "&7You are about to unclaim:", "&f" + display,
                "&8", "&7This will delete the region."));

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

        PSRegion r = resolveRegion();
        if (r == null) {
            gui.close(viewer);
            return;
        }

        // safety checks
        if (r.getRentStage() == PSRegion.RentStage.RENTING && !viewer.hasPermission("protectionstones.superowner")) {
            PSL.msg(viewer, PSL.RENT_CANNOT_BREAK_WHILE_RENTING.msg());
            gui.close(viewer);
            return;
        }

        RegionActions.unclaimRegion(r, viewer);
        gui.close(viewer);
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
