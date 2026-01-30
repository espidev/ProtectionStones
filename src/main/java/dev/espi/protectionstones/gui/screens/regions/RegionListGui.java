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

import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.gui.BaseGui;
import dev.espi.protectionstones.gui.GuiItems;
import dev.espi.protectionstones.gui.GuiManager;
import dev.espi.protectionstones.utils.UUIDCache;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Generic region list GUI used by /ps list, /ps tp (no args), and /ps unclaim list.
 */
public class RegionListGui extends BaseGui {

    public enum Mode {
        LIST,
        TELEPORT,
        UNCLAIM
    }

    private static final int SIZE = 54;
    private static final int PER_PAGE = 45;

    private final Mode mode;
    private final List<PSRegion> regions;
    private final int page;
    private final Supplier<BaseGui> back;

    public RegionListGui(GuiManager gui, Mode mode, String title, List<PSRegion> regions, int page, Supplier<BaseGui> back) {
        super(gui, SIZE, ChatColor.DARK_GRAY + title);
        this.mode = mode;
        this.regions = regions == null ? new ArrayList<>() : new ArrayList<>(regions);
        this.page = Math.max(0, page);
        this.back = back;

        // stable order
        this.regions.sort(Comparator.comparing(RegionListGui::displayName, String.CASE_INSENSITIVE_ORDER));
    }

    @Override
    protected void draw(Player viewer) {
        inv.clear();

        int start = page * PER_PAGE;
        int end = Math.min(regions.size(), start + PER_PAGE);

        int slot = 0;
        for (int i = start; i < end; i++) {
            PSRegion r = regions.get(i);

            ItemStack icon = iconFor(r);
            ItemMeta im = icon.getItemMeta();
            if (im != null) {
                im.setDisplayName(ChatColor.AQUA + displayName(r));

                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "ID: " + ChatColor.WHITE + r.getId());

                World w = r.getWorld();
                lore.add(ChatColor.GRAY + "World: " + ChatColor.WHITE + (w == null ? "?" : w.getName()));

                if (r.getTypeOptions() != null) {
                    lore.add(ChatColor.GRAY + "Type: " + ChatColor.WHITE + r.getTypeOptions().alias);
                }

                int owners = safeSize(r.getOwners());
                int members = safeSize(r.getMembers());
                lore.add(ChatColor.GRAY + "Owners: " + ChatColor.WHITE + owners + ChatColor.GRAY + "  Members: " + ChatColor.WHITE + members);

                if (r.isHidden()) {
                    lore.add(ChatColor.DARK_GRAY + "Hidden");
                }

                lore.add(ChatColor.DARK_GRAY + "");
                switch (mode) {
                    case LIST -> {
                        lore.add(ChatColor.YELLOW + "Left-click" + ChatColor.GRAY + " to open info");
                        lore.add(ChatColor.YELLOW + "Shift-left" + ChatColor.GRAY + " to teleport");
                    }
                    case TELEPORT -> lore.add(ChatColor.YELLOW + "Click" + ChatColor.GRAY + " to teleport");
                    case UNCLAIM -> lore.add(ChatColor.RED + "Click" + ChatColor.GRAY + " to unclaim");
                }

                im.setLore(lore);
                icon.setItemMeta(im);
            }

            inv.setItem(slot++, icon);
        }

        int base = SIZE - 9;
        boolean hasPrev = page > 0;
        boolean hasNext = end < regions.size();

        if (hasPrev) inv.setItem(base, GuiItems.item(Material.ARROW, "&aPrevious", "&7Page " + page));
        if (back != null) inv.setItem(base + 3, GuiItems.item(Material.ARROW, "&bBack"));
        inv.setItem(base + 4, GuiItems.item(Material.BARRIER, "&cClose"));
        if (hasNext) inv.setItem(SIZE - 1, GuiItems.item(Material.ARROW, "&aNext", "&7Page " + (page + 2)));

        // footer
        inv.setItem(base + 8, GuiItems.item(Material.GRAY_STAINED_GLASS_PANE, "&7", "&8" + regions.size() + " region(s)"));
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
            gui.open(viewer, new RegionListGui(gui, mode, stripColor(title), regions, page - 1, back));
            return;
        }
        if (raw == SIZE - 1) {
            gui.open(viewer, new RegionListGui(gui, mode, stripColor(title), regions, page + 1, back));
            return;
        }

        if (raw >= PER_PAGE) return;

        int idx = page * PER_PAGE + raw;
        if (idx < 0 || idx >= regions.size()) return;

        PSRegion r = regions.get(idx);

        if (mode == Mode.LIST) {
            if (e.isShiftClick()) {
                RegionInfoGui.teleport(viewer, r);
                return;
            }
            gui.open(viewer, new RegionInfoGui(gui, r.getWorld().getUID(), r.getId(), () -> new RegionListGui(gui, mode, stripColor(title), regions, page, back)));
            return;
        }

        if (mode == Mode.TELEPORT) {
            RegionInfoGui.teleport(viewer, r);
            gui.close(viewer);
            return;
        }

        // UNCLAIM
        gui.open(viewer, new UnclaimConfirmGui(gui, r.getWorld().getUID(), r.getId(), () -> new RegionListGui(gui, mode, stripColor(title), regions, page, back)));
    }

    private static String stripColor(String s) {
        return ChatColor.stripColor(s == null ? "" : s);
    }

    private ItemStack iconFor(PSRegion r) {
        try {
            if (r.getTypeOptions() != null) {
                ItemStack is = r.getTypeOptions().createItem();
                if (is != null) return is;
            }
        } catch (Exception ignored) {}
        return new ItemStack(Material.NAME_TAG);
    }

    private static String displayName(PSRegion r) {
        if (r == null) return "?";
        return r.getName() == null ? r.getId() : (r.getName() + " (" + r.getId() + ")");
    }

    private static int safeSize(Object maybeCollection) {
        if (maybeCollection instanceof java.util.Collection<?> c) return c.size();
        return 0;
    }
}
