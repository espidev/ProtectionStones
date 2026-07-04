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

package dev.espi.protectionstones.commands;

import dev.espi.protectionstones.*;
import dev.espi.protectionstones.utils.BlockUtil;
import dev.espi.protectionstones.utils.TextGUI;
import dev.espi.protectionstones.utils.WGUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ArgUnclaim implements PSCommandArg {

    // /ps unclaim

    @Override
    public List<String> getNames() {
        return Collections.singletonList("unclaim");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public List<String> getPermissionsToExecute() {
        return Collections.singletonList("protectionstones.unclaim");
    }

    @Override
    public HashMap<String, Boolean> getRegisteredFlags() {
        return null;
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args, HashMap<String, String> flags) {
        Player p = (Player) s;


        if (!p.hasPermission("protectionstones.unclaim")) {
            PSL.msg(p, PSL.NO_PERMISSION_UNCLAIM.msg());
            return true;
        }

        if (args.length >= 2) { // /ps unclaim [list|region-id] (unclaim remote region)

            if (!p.hasPermission("protectionstones.unclaim.remote")) {
                PSL.msg(p, PSL.NO_PERMISSION_UNCLAIM_REMOTE.msg());
                return true;
            }

            PSPlayer psp = PSPlayer.fromPlayer(p);

            // list of regions that the player owns
            List<PSRegion> regions = psp.getPSRegionsCrossWorld(psp.getPlayer().getWorld(), false);

            if (args[1].equalsIgnoreCase("list")) {
                displayPSRegions(s, regions, args.length == 2 ? 0 : tryParseInt(args[2]) - 1);
            } else {
                for (PSRegion psr : regions) {
                    if (psr.getId().equalsIgnoreCase(args[1])) {
                        // cannot break region being rented (prevents splitting merged regions, and breaking as tenant owner)
                        if (psr.getRentStage() == PSRegion.RentStage.RENTING && !p.hasPermission("protectionstones.superowner")) {
                            PSL.msg(p, PSL.RENT_CANNOT_BREAK_WHILE_RENTING.msg());
                            return false;
                        }
                        return unclaimBlock(psr, p);
                    }
                }
                PSL.msg(p, PSL.REGION_DOES_NOT_EXIST.msg());
            }

            return true;
        } else { // /ps unclaim (no arguments, unclaim current region)
            PSRegion r = PSRegion.fromLocationGroupUnsafe(p.getLocation()); // allow unclaiming unconfigured regions

            if (r == null) {
                PSL.msg(p, PSL.NOT_IN_REGION.msg());
                return true;
            }

            if (!r.isOwner(p.getUniqueId()) && !p.hasPermission("protectionstones.superowner")) {
                PSL.msg(p, PSL.NO_REGION_PERMISSION.msg());
                return true;
            }

            // cannot break region being rented (prevents splitting merged regions, and breaking as tenant owner)
            if (r.getRentStage() == PSRegion.RentStage.RENTING && !p.hasPermission("protectionstones.superowner")) {
                PSL.msg(p, PSL.RENT_CANNOT_BREAK_WHILE_RENTING.msg());
                return true;
            }
            return unclaimBlock(r, p);
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return null;
    }

    private int tryParseInt(String arg) {
        int i = 1;
        try {
            i = Integer.parseInt(arg);
        } catch (NumberFormatException ignore) {
            //ignore
        }
        return i;
    }

    private void displayPSRegions(CommandSender s, List<PSRegion> regions, int page) {
        List<TextComponent> entries = new ArrayList<>();
        for (PSRegion rs : regions) {
            String msg;
            if (rs.getName() == null) {
                msg = ChatColor.GRAY + "> " + ChatColor.AQUA + rs.getId();
            } else {
                msg = ChatColor.GRAY + "> " + ChatColor.AQUA + rs.getName() + " (" + rs.getId() + ")";
            }
            TextComponent tc = new TextComponent(ChatColor.AQUA + " [-] " + msg);
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to unclaim " + rs.getId()).create()));
            tc.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + ProtectionStones.getInstance().getConfigOptions().base_command + " unclaim " + rs.getId()));
            entries.add(tc);
        }
        TextGUI.displayGUI(s, PSL.UNCLAIM_HEADER.msg(), "/" + ProtectionStones.getInstance().getConfigOptions().base_command + " unclaim list %page%", page, 17, entries, true);
    }

    private boolean unclaimBlock(PSRegion r, Player p) {
        List<ItemStack> items = getReturnedItems(r);
        List<ProtectBlockSnapshot> blocksToRemove = getProtectBlocksToRemove(r);
        if (!items.isEmpty()
                && !ProtectionStones.getInstance().getConfigOptions().dropItemWhenInventoryFull
                && !hasInventorySpace(p, items)) {
            PSL.msg(p, PSL.NO_ROOM_IN_INVENTORY.msg());
            return true;
        }

        PSLocation psl = WGUtils.parsePSRegionToLocation(r.getId());
        Location protectBlockLocation = new Location(r.getWorld(), psl.x, psl.y, psl.z);
        ProtectionStones.getScheduler().runTask(protectBlockLocation, () -> unclaimBlockAtRegion(r, p, items, blocksToRemove));
        return true;
    }

    private void unclaimBlockAtRegion(PSRegion r, Player p, List<ItemStack> items, List<ProtectBlockSnapshot> blocksToRemove) {
        if (!regionStillExists(r)) {
            return;
        }

        // remove region
        // check if removing the region and firing region remove event blocked it
        boolean deleted;
        try {
            deleted = r.deleteRegion(false, p);
        } catch (RuntimeException e) {
            ProtectionStones.getPluginLogger().warning("Failed to unclaim region " + r.getId() + ": " + e.getMessage());
            ProtectionStones.getScheduler().runTask(p, () -> PSL.msg(p, PSL.REGION_DOES_NOT_EXIST.msg()));
            return;
        }

        if (!deleted) {
            if (!ProtectionStones.getInstance().getConfigOptions().allowMergingHoles) { // side case if the removing creates a hole and those are prevented
                ProtectionStones.getScheduler().runTask(p, () -> PSL.msg(p, PSL.DELETE_REGION_PREVENTED_NO_HOLES.msg()));
            }
            return;
        }

        removeProtectBlocks(blocksToRemove);

        ProtectionStones.getScheduler().runTask(p, () -> {
            giveReturnedItems(p, items);
            PSL.msg(p, PSL.NO_LONGER_PROTECTED.msg());
        });
    }

    private List<ItemStack> getReturnedItems(PSRegion r) {
        List<ItemStack> items = new ArrayList<>();
        PSProtectBlock cpb = r.getTypeOptions();
        if (cpb != null && !cpb.noDrop) {
            // return protection stone
            if (r instanceof PSGroupRegion) {
                for (PSRegion rp : ((PSGroupRegion) r).getMergedRegions()) {
                    if (rp.getTypeOptions() != null) items.add(rp.getTypeOptions().createItem());
                }
            } else {
                items.add(cpb.createItem());
            }
        }
        return items;
    }

    private List<ProtectBlockSnapshot> getProtectBlocksToRemove(PSRegion r) {
        List<ProtectBlockSnapshot> blocks = new ArrayList<>();
        if (r instanceof PSGroupRegion) {
            for (PSMergedRegion mergedRegion : ((PSGroupRegion) r).getMergedRegions()) {
                blocks.add(ProtectBlockSnapshot.fromRegion(mergedRegion));
            }
        } else {
            blocks.add(ProtectBlockSnapshot.fromRegion(r));
        }
        return blocks;
    }

    private void removeProtectBlocks(List<ProtectBlockSnapshot> blocks) {
        for (ProtectBlockSnapshot snapshot : blocks) {
            ProtectionStones.getScheduler().runTask(snapshot.location, () -> {
                Block block = snapshot.location.getBlock();
                if (snapshot.type.equals(BlockUtil.getProtectBlockType(block))) {
                    block.setType(Material.AIR);
                }
            });
        }
    }

    private void giveReturnedItems(Player p, List<ItemStack> items) {
        for (ItemStack item : items) {
            HashMap<Integer, ItemStack> leftovers = p.getInventory().addItem(item);
            if (!leftovers.isEmpty()) {
                // method will return not empty if item couldn't be added
                if (ProtectionStones.getInstance().getConfigOptions().dropItemWhenInventoryFull) {
                    PSL.msg(p, PSL.NO_ROOM_DROPPING_ON_FLOOR.msg());
                    leftovers.values().forEach(leftover -> p.getWorld().dropItem(p.getLocation(), leftover));
                } else {
                    PSL.msg(p, PSL.NO_ROOM_IN_INVENTORY.msg());
                    leftovers.values().forEach(leftover -> p.getWorld().dropItem(p.getLocation(), leftover));
                }
            }
        }
    }

    private boolean hasInventorySpace(Player p, List<ItemStack> items) {
        List<ItemStack> simulatedContents = new ArrayList<>();
        for (ItemStack content : p.getInventory().getStorageContents()) {
            simulatedContents.add(content == null ? null : content.clone());
        }

        for (ItemStack item : items) {
            int remaining = item.getAmount();
            for (ItemStack content : simulatedContents) {
                if (content == null || !content.isSimilar(item)) continue;
                int canFit = Math.min(item.getMaxStackSize(), content.getMaxStackSize()) - content.getAmount();
                if (canFit <= 0) continue;
                int moved = Math.min(canFit, remaining);
                content.setAmount(content.getAmount() + moved);
                remaining -= moved;
                if (remaining <= 0) break;
            }

            while (remaining > 0) {
                int emptySlot = firstEmptySlot(simulatedContents);
                if (emptySlot == -1) return false;
                int moved = Math.min(item.getMaxStackSize(), remaining);
                ItemStack copy = item.clone();
                copy.setAmount(moved);
                simulatedContents.set(emptySlot, copy);
                remaining -= moved;
            }
        }

        return true;
    }

    private int firstEmptySlot(List<ItemStack> items) {
        for (int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);
            if (item == null || item.getType().isAir()) return i;
        }
        return -1;
    }

    private boolean regionStillExists(PSRegion r) {
        if (r instanceof PSMergedRegion) {
            return ((PSMergedRegion) r).getGroupRegion().hasMergedRegion(r.getId());
        }
        return r.getWGRegionManager().getRegion(r.getId()) != null;
    }

    private static class ProtectBlockSnapshot {
        private final Location location;
        private final String type;

        private ProtectBlockSnapshot(Location location, String type) {
            this.location = location;
            this.type = type;
        }

        private static ProtectBlockSnapshot fromRegion(PSRegion r) {
            PSLocation psl = WGUtils.parsePSRegionToLocation(r.getId());
            return new ProtectBlockSnapshot(new Location(r.getWorld(), psl.x, psl.y, psl.z), r.getType());
        }
    }
}
