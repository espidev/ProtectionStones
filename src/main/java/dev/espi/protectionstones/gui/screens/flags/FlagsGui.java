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

package dev.espi.protectionstones.gui.screens.flags;

import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.commands.ArgFlag;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** /ps flag (GUI mode): basic inventory GUI for toggling common flag types. */
public class FlagsGui extends BaseGui {

    private static final int SIZE = 54;
    private static final int PER_PAGE = 45;

    private final UUID worldId;
    private final String regionId;
    private final int page;

    public FlagsGui(GuiManager gui, UUID worldId, String regionId, int page) {
        super(gui, SIZE, ChatColor.DARK_GRAY + "Flags");
        this.worldId = worldId;
        this.regionId = regionId;
        this.page = Math.max(0, page);
    }

    @Override
    protected void draw(Player viewer) {
        inv.clear();

        PSRegion r = resolveRegion();
        if (r == null) {
            inv.setItem(22, GuiItems.item(Material.BARRIER, "&cRegion not found", "&7Move back into the region and reopen /ps flag"));
            inv.setItem(SIZE - 5, GuiItems.item(Material.BARRIER, "&cClose"));
            return;
        }

        // Title with region name/id
        String titleLine = (r.getName() == null ? r.getId() : (r.getName() + " (" + r.getId() + ")"));
        inv.setItem(SIZE - 5, GuiItems.item(Material.NAME_TAG, "&b" + titleLine, "&7Click flags above to change"));

        List<String> allowed = new ArrayList<>(r.getTypeOptions().allowedFlags.keySet());

        int start = page * PER_PAGE;
        int end = Math.min(allowed.size(), start + PER_PAGE);

        int slot = 0;
        for (int i = start; i < end; i++) {
            String flagName = allowed.get(i);
            Flag<?> f = Flags.fuzzyMatchFlag(WGUtils.getFlagRegistry(), flagName);
            if (f == null) continue;

            Object value = r.getWGRegion().getFlag(f);
            Material icon = iconForFlag(f, value);

            List<String> lore = new ArrayList<>();
            lore.add("&7Current: &f" + (value == null ? "none" : value.toString()));
            lore.add("&8");
            if (f instanceof StateFlag) {
                lore.add("&eClick: cycle Allow/Deny/None");
            } else if (f instanceof BooleanFlag) {
                lore.add("&eClick: cycle True/False/None");
            } else {
                lore.add("&eClick: print command help");
            }
            lore.add("&8Perm: protectionstones.flags.edit." + flagName);

            inv.setItem(slot++, GuiItems.item(icon, ChatColor.AQUA + flagName, lore));
        }

        int base = SIZE - 9;
        boolean hasPrev = page > 0;
        boolean hasNext = end < allowed.size();
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
            gui.open(viewer, new FlagsGui(gui, worldId, regionId, page - 1));
            return;
        }
        // next
        if (raw == (SIZE - 1)) {
            gui.open(viewer, new FlagsGui(gui, worldId, regionId, page + 1));
            return;
        }

        if (raw >= PER_PAGE) return;

        PSRegion r = resolveRegion();
        if (r == null) {
            PSL.msg(viewer, "&cRegion not found.");
            gui.close(viewer);
            return;
        }

        List<String> allowed = new ArrayList<>(r.getTypeOptions().allowedFlags.keySet());
        int index = page * PER_PAGE + raw;
        if (index < 0 || index >= allowed.size()) return;

        String flagName = allowed.get(index);

        if (!viewer.hasPermission("protectionstones.flags.edit." + flagName)) {
            PSL.msg(viewer, PSL.NO_PERMISSION_PER_FLAG.msg());
            return;
        }

        Flag<?> f = Flags.fuzzyMatchFlag(WGUtils.getFlagRegistry(), flagName);
        if (f == null) return;

        Object value = r.getWGRegion().getFlag(f);

        if (f instanceof StateFlag) {
            String next;
            if (value == StateFlag.State.ALLOW) next = "deny";
            else if (value == StateFlag.State.DENY) next = "none";
            else next = "allow";
            ArgFlag.setFlag(r, viewer, flagName, next, "all");
        } else if (f instanceof BooleanFlag) {
            String next;
            if (Boolean.TRUE.equals(value)) next = "false";
            else if (Boolean.FALSE.equals(value)) next = "none";
            else next = "true";
            ArgFlag.setFlag(r, viewer, flagName, next, "all");
        } else {
            // Non-simple flags: just tell them the command to use
            String base = ProtectionStones.getInstance().getConfigOptions().base_command;
            viewer.sendMessage(ChatColor.GRAY + "Use: /" + base + " flag " + flagName + " <value>");
        }

        // refresh
        gui.open(viewer, new FlagsGui(gui, worldId, regionId, page));
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

    private Material iconForFlag(Flag<?> f, Object value) {
        if (f instanceof StateFlag) {
            if (value == StateFlag.State.ALLOW) return Material.LIME_DYE;
            if (value == StateFlag.State.DENY) return Material.RED_DYE;
            return Material.GRAY_DYE;
        }
        if (f instanceof BooleanFlag) {
            if (Boolean.TRUE.equals(value)) return Material.LIME_DYE;
            if (Boolean.FALSE.equals(value)) return Material.RED_DYE;
            return Material.GRAY_DYE;
        }
        return Material.PAPER;
    }
}
