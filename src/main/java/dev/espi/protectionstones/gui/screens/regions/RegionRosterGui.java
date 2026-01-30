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
import dev.espi.protectionstones.gui.BaseGui;
import dev.espi.protectionstones.gui.GuiItems;
import dev.espi.protectionstones.gui.GuiManager;
import dev.espi.protectionstones.utils.UUIDCache;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/** Read-only roster GUI for region members or owners. */
public class RegionRosterGui extends BaseGui {

    public enum Mode { MEMBERS, OWNERS }

    private static final int SIZE = 54;
    private static final int PER_PAGE = 45;

    private final UUID worldId;
    private final String regionId;
    private final Mode mode;
    private final int page;
    private final Supplier<BaseGui> back;

    public RegionRosterGui(GuiManager gui, UUID worldId, String regionId, Mode mode, int page, Supplier<BaseGui> back) {
        super(gui, SIZE, ChatColor.DARK_GRAY + (mode == Mode.MEMBERS ? "Members" : "Owners"));
        this.worldId = worldId;
        this.regionId = regionId;
        this.mode = mode;
        this.page = Math.max(0, page);
        this.back = back;
    }

    @Override
    protected void draw(Player viewer) {
        inv.clear();
        PSRegion r = resolveRegion();
        if (r == null) {
            inv.setItem(22, GuiItems.item(Material.BARRIER, "&cRegion not found"));
            inv.setItem(SIZE - 5, GuiItems.item(Material.BARRIER, "&cClose"));
            return;
        }

        Collection<UUID> uuids = (mode == Mode.MEMBERS) ? r.getMembers() : r.getOwners();
        List<UUID> entries = new ArrayList<>(uuids);
        entries.sort(Comparator.comparing(this::safeName, String.CASE_INSENSITIVE_ORDER));

        int start = page * PER_PAGE;
        int end = Math.min(entries.size(), start + PER_PAGE);

        int slot = 0;
        for (int i = start; i < end; i++) {
            UUID u = entries.get(i);
            String name = safeName(u);
            inv.setItem(slot++, GuiItems.playerHead(u, "&b" + name, "&7" + u.toString()));
        }

        int base = SIZE - 9;
        boolean hasPrev = page > 0;
        boolean hasNext = end < entries.size();
        if (hasPrev) inv.setItem(base, GuiItems.item(Material.ARROW, "&aPrevious", "&7Page " + page));
        if (back != null) inv.setItem(base + 3, GuiItems.item(Material.ARROW, "&bBack"));
        inv.setItem(base + 4, GuiItems.item(Material.BARRIER, "&cClose"));
        if (hasNext) inv.setItem(SIZE - 1, GuiItems.item(Material.ARROW, "&aNext", "&7Page " + (page + 2)));

        inv.setItem(base + 8, GuiItems.item(Material.GRAY_STAINED_GLASS_PANE, "&7", "&8" + entries.size() + " player(s)"));
    }

    @Override
    protected void onClick(Player viewer, InventoryClickEvent e) {
        int raw = e.getRawSlot();
        int base = SIZE - 9;

        if (raw == base + 4) {
            gui.close(viewer);
            return;
        }
        if (raw == base + 3 && back != null) {
            gui.open(viewer, back.get());
            return;
        }
        if (raw == base && page > 0) {
            gui.open(viewer, new RegionRosterGui(gui, worldId, regionId, mode, page - 1, back));
            return;
        }
        if (raw == SIZE - 1) {
            gui.open(viewer, new RegionRosterGui(gui, worldId, regionId, mode, page + 1, back));
            return;
        }
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

    private String safeName(UUID uuid) {
        String n = UUIDCache.getNameFromUUID(uuid);
        if (n == null || n.isEmpty() || n.equalsIgnoreCase("null")) {
            try {
                n = Bukkit.getOfflinePlayer(uuid).getName();
            } catch (Exception ignored) {}
        }
        if (n == null || n.isEmpty()) n = uuid.toString().substring(0, 8);
        return n;
    }
}
