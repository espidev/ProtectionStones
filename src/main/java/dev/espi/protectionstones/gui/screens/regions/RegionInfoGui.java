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

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.commands.ArgTp;
import dev.espi.protectionstones.gui.BaseGui;
import dev.espi.protectionstones.gui.GuiItems;
import dev.espi.protectionstones.gui.GuiManager;
import dev.espi.protectionstones.gui.screens.flags.FlagsGui;
import dev.espi.protectionstones.gui.screens.members.RegionPlayerSelectGui;
import dev.espi.protectionstones.utils.UUIDCache;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Region information GUI, with quick actions (members/owners/flags/priority/unclaim/teleport).
 */
public class RegionInfoGui extends BaseGui {

    private static final int SIZE = 36;

    private final UUID worldId;
    private final String regionId;
    private final Supplier<BaseGui> back;

    public RegionInfoGui(GuiManager gui, UUID worldId, String regionId, Supplier<BaseGui> back) {
        super(gui, SIZE, ChatColor.DARK_GRAY + "Region Info");
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
            inv.setItem(31, GuiItems.item(Material.BARRIER, "&cClose"));
            return;
        }

        // access check (same behavior as text info)
        if (!viewer.hasPermission("protectionstones.info.others")
                && WGUtils.hasNoAccess(r.getWGRegion(), viewer, WorldGuardPlugin.inst().wrapPlayer(viewer), true)) {
            inv.setItem(13, GuiItems.item(Material.BARRIER, "&cNo access", "&7You don't have permission"));
            inv.setItem(31, GuiItems.item(Material.BARRIER, "&cClose"));
            return;
        }

        String display = r.getName() == null ? r.getId() : (r.getName() + " (" + r.getId() + ")");
        World w = r.getWorld();

        List<String> lore = new ArrayList<>();
        lore.add("&7World: &f" + (w == null ? "?" : w.getName()));
        if (r.getTypeOptions() != null) lore.add("&7Type: &f" + r.getTypeOptions().alias);
        lore.add("&7Priority: &f" + r.getWGRegion().getPriority());
        lore.add("&7Owners: &f" + r.getOwners().size() + " &7Members: &f" + r.getMembers().size());
        if (r.isHidden()) lore.add("&8Hidden");
        if (r.forSale()) lore.add("&eFor sale: &f" + String.format("%.2f", r.getPrice()));
        if (r.getRentStage() != PSRegion.RentStage.NOT_RENTING) lore.add("&eRent: &f" + String.format("%.2f", r.getPrice()));

        inv.setItem(13, GuiItems.item(Material.NAME_TAG, ChatColor.AQUA + display, lore));

        // actions
        inv.setItem(10, GuiItems.item(Material.ENDER_PEARL, "&aTeleport", "&7Teleport to region home"));
        inv.setItem(11, GuiItems.item(Material.PLAYER_HEAD, "&bMembers", "&7View members"));
        inv.setItem(12, GuiItems.item(Material.GOLDEN_HELMET, "&6Owners", "&7View owners"));
        inv.setItem(14, GuiItems.item(Material.COMPARATOR, "&dFlags", "&7Edit common flags"));
        inv.setItem(15, GuiItems.item(Material.ANVIL, "&ePriority", "&7View/set priority"));

        boolean canUnclaim = viewer.hasPermission("protectionstones.unclaim")
                && (r.isOwner(viewer.getUniqueId()) || viewer.hasPermission("protectionstones.superowner"));
        if (canUnclaim) {
            inv.setItem(16, GuiItems.item(Material.TNT, "&cUnclaim", "&7Unclaim this region"));
        } else {
            inv.setItem(16, GuiItems.item(Material.BARRIER, "&cUnclaim", "&8No permission"));
        }

        // footer
        if (back != null) inv.setItem(27, GuiItems.item(Material.ARROW, "&bBack"));
        inv.setItem(31, GuiItems.item(Material.BARRIER, "&cClose"));
    }

    @Override
    protected void onClick(Player viewer, InventoryClickEvent e) {
        int raw = e.getRawSlot();

        if (raw == 31) {
            gui.close(viewer);
            return;
        }
        if (raw == 27 && back != null) {
            gui.open(viewer, back.get());
            return;
        }

        PSRegion r = resolveRegion();
        if (r == null) {
            gui.close(viewer);
            return;
        }

        switch (raw) {
            case 10 -> {
                teleport(viewer, r);
                gui.close(viewer);
            }
            case 11 -> gui.open(viewer, new RegionRosterGui(gui, worldId, regionId, RegionRosterGui.Mode.MEMBERS, 0, () -> new RegionInfoGui(gui, worldId, regionId, back)));
            case 12 -> gui.open(viewer, new RegionRosterGui(gui, worldId, regionId, RegionRosterGui.Mode.OWNERS, 0, () -> new RegionInfoGui(gui, worldId, regionId, back)));
            case 14 -> {
                if (!viewer.hasPermission("protectionstones.flags")) {
                    PSL.msg(viewer, PSL.NO_PERMISSION_FLAGS.msg());
                    return;
                }
                gui.open(viewer, new FlagsGui(gui, worldId, regionId, 0));
            }
            case 15 -> {
                if (!viewer.hasPermission("protectionstones.priority")) {
                    PSL.msg(viewer, PSL.NO_PERMISSION_PRIORITY.msg());
                    return;
                }
                gui.open(viewer, new RegionPriorityGui(gui, worldId, regionId, () -> new RegionInfoGui(gui, worldId, regionId, back)));
            }
            case 16 -> {
                boolean canUnclaim = viewer.hasPermission("protectionstones.unclaim")
                        && (r.isOwner(viewer.getUniqueId()) || viewer.hasPermission("protectionstones.superowner"));
                if (!canUnclaim) {
                    PSL.msg(viewer, PSL.NO_REGION_PERMISSION.msg());
                    return;
                }
                if (r.getRentStage() == PSRegion.RentStage.RENTING && !viewer.hasPermission("protectionstones.superowner")) {
                    PSL.msg(viewer, PSL.RENT_CANNOT_BREAK_WHILE_RENTING.msg());
                    return;
                }
                gui.open(viewer, new UnclaimConfirmGui(gui, worldId, regionId, () -> new RegionInfoGui(gui, worldId, regionId, back)));
            }
        }
    }

    public static void teleport(Player viewer, PSRegion r) {
        // use existing tp behavior (delay / no-move rules etc)
        ArgTp.teleportPlayer(viewer, r);
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
