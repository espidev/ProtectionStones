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

package dev.espi.protectionstones.gui.screens.members;

import dev.espi.protectionstones.PSPlayer;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.commands.ArgAddRemove;
import dev.espi.protectionstones.gui.BaseGui;
import dev.espi.protectionstones.gui.GuiItems;
import dev.espi.protectionstones.gui.GuiManager;
import dev.espi.protectionstones.utils.UUIDCache;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Player selection GUI for /ps add, /ps remove, /ps addowner, /ps removeowner.
 * - add/addowner: lists currently online players (visible to viewer)
 * - remove/removeowner: lists players currently in the region (members/owners)
 */
public class RegionPlayerSelectGui extends BaseGui {

    public enum Mode {
        ADD_MEMBER,
        REMOVE_MEMBER,
        ADD_OWNER,
        REMOVE_OWNER
    }

    private static final int SIZE = 54; // 6 rows
    private static final int PER_PAGE = 45; // top 5 rows

    private final PSRegion region;
    private final Mode mode;
    private final int page;

    public RegionPlayerSelectGui(GuiManager gui, PSRegion region, Mode mode, int page) {
        super(gui, SIZE, titleFor(region, mode));
        this.region = region;
        this.mode = mode;
        this.page = Math.max(0, page);
    }

    private static String titleFor(PSRegion r, Mode m) {
        String base = switch (m) {
            case ADD_MEMBER -> "Add Member";
            case REMOVE_MEMBER -> "Remove Member";
            case ADD_OWNER -> "Add Owner";
            case REMOVE_OWNER -> "Remove Owner";
        };
        String rn = (r.getName() == null ? r.getId() : r.getName());
        return ChatColor.DARK_GRAY + base + " - " + rn;
    }

    @Override
    protected void draw(Player viewer) {
        inv.clear();

        List<UUID> entries = getEntries(viewer);

        int start = page * PER_PAGE;
        int end = Math.min(entries.size(), start + PER_PAGE);

        int slot = 0;
        for (int i = start; i < end; i++) {
            UUID uuid = entries.get(i);
            String name = safeName(uuid);

            boolean isSelf = uuid.equals(viewer.getUniqueId());

            // icon + lore
            Material fallback = Material.NAME_TAG;

            if (mode == Mode.ADD_MEMBER) {
                inv.setItem(slot++, GuiItems.playerHead(uuid, "&b" + name,
                        "&7Click to add as &fMember",
                        isSelf ? "&8(You)" : "&8"));
            } else if (mode == Mode.ADD_OWNER) {
                inv.setItem(slot++, GuiItems.playerHead(uuid, "&b" + name,
                        "&7Click to add as &fOwner",
                        isSelf ? "&8(You)" : "&8"));
            } else if (mode == Mode.REMOVE_MEMBER) {
                inv.setItem(slot++, GuiItems.playerHead(uuid, "&b" + name,
                        "&7Click to remove from &fMembers",
                        isSelf ? "&8(You)" : "&8"));
            } else { // REMOVE_OWNER
                int owners = region.getOwners().size();
                boolean lastOwnerSelf = isSelf && owners <= 1;
                inv.setItem(slot++, GuiItems.playerHead(uuid, "&b" + name,
                        lastOwnerSelf ? "&cYou are the last owner" : "&7Click to remove from &fOwners",
                        lastOwnerSelf ? "&cCannot remove last owner" : "&8"));
            }
        }

        // nav row
        int base = SIZE - 9;
        boolean hasPrev = page > 0;
        boolean hasNext = end < entries.size();

        if (hasPrev) inv.setItem(base, GuiItems.item(Material.ARROW, "&aPrevious", "&7Page " + page));
        inv.setItem(base + 3, GuiItems.item(Material.PAPER, "&eRefresh"));
        inv.setItem(base + 4, GuiItems.item(Material.BARRIER, "&cClose"));
        if (hasNext) inv.setItem(SIZE - 1, GuiItems.item(Material.ARROW, "&aNext", "&7Page " + (page + 2)));

        // footer hint
        inv.setItem(base + 8, GuiItems.item(Material.GRAY_STAINED_GLASS_PANE, "&7",
                "&8Select a player"));
    }

    @Override
    protected void onClick(Player viewer, InventoryClickEvent e) {
        int raw = e.getRawSlot();
        int base = SIZE - 9;

        // close
        if (raw == base + 4) {
            gui.close(viewer);
            return;
        }

        // refresh
        if (raw == base + 3) {
            gui.open(viewer, new RegionPlayerSelectGui(gui, region, mode, page));
            return;
        }

        // prev
        if (raw == base && page > 0) {
            gui.open(viewer, new RegionPlayerSelectGui(gui, region, mode, page - 1));
            return;
        }

        // next
        if (raw == SIZE - 1) {
            gui.open(viewer, new RegionPlayerSelectGui(gui, region, mode, page + 1));
            return;
        }

        if (raw >= PER_PAGE) return;

        List<UUID> entries = getEntries(viewer);
        int idx = page * PER_PAGE + raw;
        if (idx < 0 || idx >= entries.size()) return;

        UUID target = entries.get(idx);

        // Safety checks
        if (mode == Mode.REMOVE_OWNER) {
            if (target.equals(viewer.getUniqueId()) && region.getOwners().size() <= 1) {
                PSL.msg(viewer, PSL.CANNOT_REMOVE_YOURSELF_LAST_OWNER.msg());
                return;
            }
        }

        // Apply (async like original command implementation)
        Bukkit.getScheduler().runTaskAsynchronously(ProtectionStones.getInstance(), () -> {
            String name = safeName(target);

            switch (mode) {
                case ADD_MEMBER -> {
                    if (region.isMember(target) || region.isOwner(target)) {
                        PSL.msg(viewer, "&e" + name + " &7is already added.");
                        return;
                    }
                    region.addMember(target);
                    PSL.msg(viewer, PSL.ADDED_TO_REGION.msg().replace("%player%", name));
                    Bukkit.getScheduler().runTaskAsynchronously(ProtectionStones.getInstance(), () -> UUIDCache.storeWGProfile(target, name));
                }
                case REMOVE_MEMBER -> {
                    if (!region.isMember(target)) {
                        PSL.msg(viewer, "&e" + name + " &7is not a member.");
                        return;
                    }
                    region.removeMember(target);
                    PSL.msg(viewer, PSL.REMOVED_FROM_REGION.msg().replace("%player%", name));
                }
                case ADD_OWNER -> {
                    if (region.isOwner(target)) {
                        PSL.msg(viewer, "&e" + name + " &7is already an owner.");
                        return;
                    }
                    // limit checks (reuse existing logic)
                    ArgAddRemove helper = new ArgAddRemove();
                    if (helper.determinePlayerSurpassedLimit(viewer, Collections.singletonList(region), PSPlayer.fromUUID(target))) {
                        return;
                    }
                    region.addOwner(target);
                    PSL.msg(viewer, PSL.ADDED_TO_REGION.msg().replace("%player%", name));
                    Bukkit.getScheduler().runTaskAsynchronously(ProtectionStones.getInstance(), () -> UUIDCache.storeWGProfile(target, name));
                }
                case REMOVE_OWNER -> {
                    if (!region.isOwner(target)) {
                        PSL.msg(viewer, "&e" + name + " &7is not an owner.");
                        return;
                    }
                    region.removeOwner(target);
                    PSL.msg(viewer, PSL.REMOVED_FROM_REGION.msg().replace("%player%", name));
                }
            }

            // refresh GUI back on main thread
            Bukkit.getScheduler().runTask(ProtectionStones.getInstance(), () -> gui.open(viewer, new RegionPlayerSelectGui(gui, region, mode, page)));
        });
    }

    private List<UUID> getEntries(Player viewer) {
        if (mode == Mode.ADD_MEMBER || mode == Mode.ADD_OWNER) {
            // online players only (visible)
            List<UUID> list = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!viewer.canSee(p)) continue;
                list.add(p.getUniqueId());
            }

            // filter by already-added status
            if (mode == Mode.ADD_MEMBER) {
                list = list.stream()
                        .filter(u -> !region.isMember(u) && !region.isOwner(u))
                        .collect(Collectors.toList());
            } else {
                list = list.stream()
                        .filter(u -> !region.isOwner(u))
                        .collect(Collectors.toList());
            }

            // sort by name
            list.sort(Comparator.comparing(this::safeName, String.CASE_INSENSITIVE_ORDER));
            return list;
        }

        // remove modes: list members/owners currently on region
        // Note: region.getMembers() and region.getOwners() are not guaranteed to return the same concrete type.
        // Use Collection to avoid conditional type mismatch.
        Collection<UUID> uuids = (mode == Mode.REMOVE_MEMBER) ? region.getMembers() : region.getOwners();
        List<UUID> list = new ArrayList<>(uuids);
        list.sort(Comparator.comparing(this::safeName, String.CASE_INSENSITIVE_ORDER));
        return list;
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
