/*
 * Copyright 2019 ProtectionStones team and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.vik1395.ProtectionStones;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.HashMap;
import java.util.List;

public class ListenerClass implements Listener {
    private static HashMap<Player, Double> lastProtectStonePlaced = new HashMap<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        ProtectionStones.uuidToName.remove(e.getPlayer().getUniqueId());
        ProtectionStones.uuidToName.put(e.getPlayer().getUniqueId(), e.getPlayer().getName());
        ProtectionStones.nameToUUID.remove(e.getPlayer().getName());
        ProtectionStones.nameToUUID.put(e.getPlayer().getName(), e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.isCancelled()) return;

        Player p = e.getPlayer();
        Block b = e.getBlock();

        String blockType = b.getType().toString();

        // check if the block is a protection stone
        if (!ProtectionStones.isProtectBlock(blockType)) {
            return;
        }

        ConfigProtectBlock blockOptions = ProtectionStones.getBlockOptions(blockType);

        // check if player has toggled off placement of protection stones
        if (ProtectionStones.toggleList.contains(p.getName())) return;

        // check if the item was created by protection stones (stored in custom tag)
        // block must have restrictObtaining enabled for blocking place
        String tag = null;
        try {
            if (e.getItemInHand().getItemMeta() != null) {
                CustomItemTagContainer tagContainer = e.getItemInHand().getItemMeta().getCustomTagContainer();
                tag = tagContainer.getCustomTag(new NamespacedKey(ProtectionStones.plugin, "isPSBlock"), ItemTagType.STRING);
            }
        } catch (IllegalArgumentException ignored) {
        } // ignore item tags that aren't of "string" type
        if (blockOptions.restrictObtaining && (tag == null || !tag.equalsIgnoreCase("true"))) return;

        // check permission
        if (!p.hasPermission("protectionstones.create") || (!blockOptions.permission.equals("") && !p.hasPermission(blockOptions.permission))) {
            PSL.msg(p, PSL.NO_PERMISSION_CREATE.msg());
            e.setCancelled(true);
            return;
        }

        WorldGuardPlugin wg = (WorldGuardPlugin) ProtectionStones.wgd;
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager rm = regionContainer.get(BukkitAdapter.adapt(e.getPlayer().getWorld()));

        LocalPlayer lp = wg.wrapPlayer(p);

        // check if player can place block in that area
        if (!wg.createProtectionQuery().testBlockPlace(p, b.getLocation(), b.getType())) {
            PSL.msg(p, PSL.CANT_PROTECT_THAT.msg());
            e.setCancelled(true);
            return;
        }

        // check cooldown
        if (ProtectionStones.configOptions.placingCooldown != -1) {
            double currentTime = System.currentTimeMillis();
            if (lastProtectStonePlaced.containsKey(p)) {
                double cooldown = ProtectionStones.configOptions.placingCooldown; // seconds
                double lastPlace = lastProtectStonePlaced.get(p); // milliseconds

                if (lastPlace + cooldown * 1000 > currentTime) { // if cooldown has not been finished
                    e.setCancelled(true);
                    PSL.msg(p, PSL.COOLDOWN.msg().replace("%time%", String.format("%.1f", cooldown - ((currentTime - lastPlace) / 1000))));
                    return;
                }

                lastProtectStonePlaced.remove(p);
            }
            lastProtectStonePlaced.put(p, currentTime);
        }

        // non-admin checks
        if (!p.hasPermission("protectionstones.admin")) {
            int max = 0;

            // check if player has limit on protection stones
            for (PermissionAttachmentInfo rawperm : p.getEffectivePermissions()) {
                String perm = rawperm.getPermission();
                if (perm.startsWith("protectionstones.limit")) {
                    try {
                        int lim = Integer.parseInt(perm.substring(23).trim());
                        if (lim > max) {
                            max = lim;
                        }
                    } catch (Exception er) {
                        max = 0;
                    }
                }
            }

            // check if player has passed region limit
            if (rm.getRegionCountOfPlayer(lp) >= max) {
                if (max != 0) {
                    PSL.msg(p, PSL.REACHED_REGION_LIMIT.msg());
                    e.setCancelled(true);
                    return;
                }
            }
            // check if in world blacklist or not in world whitelist
            if (blockOptions.worldListType.equalsIgnoreCase("blacklist")) {
                for (String world : blockOptions.worlds) {
                    if (world.trim().equals(p.getLocation().getWorld().getName())) {
                        PSL.msg(p, PSL.WORLD_DENIED_CREATE.msg());
                        e.setCancelled(true);
                        return;
                    }
                }
            } else if (blockOptions.worldListType.equalsIgnoreCase("whitelist")) {
                boolean found = false;
                for (String world : blockOptions.worlds) {
                    if (world.trim().equals(p.getLocation().getWorld().getName())) {
                        found = true;
                    }
                }
                if (!found) {
                    PSL.msg(p, PSL.WORLD_DENIED_CREATE.msg());
                    e.setCancelled(true);
                    return;
                }
            }

        } // end of non-admin checks

        // create region
        double bx = b.getLocation().getX();
        double by = b.getLocation().getY();
        double bz = b.getLocation().getZ();
        BlockVector3 v1, v2;

        if (blockOptions.yRadius == -1) {
            v1 = BlockVector3.at(bx - blockOptions.xRadius, 0, bz - blockOptions.zRadius);
            v2 = BlockVector3.at(bx + blockOptions.xRadius, p.getWorld().getMaxHeight(), bz + blockOptions.zRadius);
        } else {
            v1 = BlockVector3.at(bx - blockOptions.xRadius, by - blockOptions.yRadius, bz - blockOptions.zRadius);
            v2 = BlockVector3.at(bx + blockOptions.xRadius, by + blockOptions.yRadius, bz + blockOptions.zRadius);
        }

        BlockVector3 min = v1;
        BlockVector3 max = v2;
        String id = "ps" + (long) bx + "x" + (long) by + "y" + (long) bz + "z";

        ProtectedRegion region = new ProtectedCuboidRegion(id, min, max);
        region.getOwners().addPlayer(p.getUniqueId());

        rm.addRegion(region);

        // check if new region overlaps more powerful region
        if (rm.overlapsUnownedRegion(region, lp)) {
            ApplicableRegionSet rp = rm.getApplicableRegions(region);
            boolean powerfulOverLap = false;
            for (ProtectedRegion rg : rp) {
                if (!rg.isOwner(lp) && rg.getPriority() >= region.getPriority()) { // if protection priority < overlap priority
                    powerfulOverLap = true;
                    break;
                }
            }
            if (powerfulOverLap) { // if we overlap a more powerful region
                rm.removeRegion(id);
                p.updateInventory();
                try {
                    rm.saveChanges();
                    rm.save();
                } catch (StorageException e1) {
                    e1.printStackTrace();
                }
                PSL.msg(p, PSL.REGION_OVERLAP.msg());
                e.setCancelled(true);
                return;
            }
        }

        // add corresponding flags to new region by cloning blockOptions default flags
        HashMap<Flag<?>, Object> flags = new HashMap<>(blockOptions.regionFlags);

        // replace greeting and farewell messages with player name
        Flag<?> greeting = WorldGuard.getInstance().getFlagRegistry().get("greeting");
        Flag<?> farewell = WorldGuard.getInstance().getFlagRegistry().get("farewell");

        if (flags.containsKey(greeting)) {
            flags.put(greeting, ((String) flags.get(greeting)).replaceAll("%player%", p.getName()));
        }
        if (flags.containsKey(farewell)) {
            flags.put(farewell, ((String) flags.get(farewell)).replaceAll("%player%", p.getName()));
        }

        // set flags
        region.setFlags(flags);
        FlagHandler.initCustomFlagsForPS(region, b, blockOptions);

        region.setPriority(blockOptions.priority);
        p.sendMessage(PSL.PROTECTED.msg());

        // save
        try {
            rm.saveChanges();
            rm.save();
        } catch (StorageException e1) {
            e1.printStackTrace();
        }

        // hide block if auto hide is enabled
        if (blockOptions.autoHide) {
            b.setType(Material.AIR);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled()) return;

        Player p = e.getPlayer();
        Block pb = e.getBlock();

        String blockType = pb.getType().toString();
        ConfigProtectBlock blockOptions = ProtectionStones.getBlockOptions(blockType);

        // check if block broken is protection stone
        if (blockOptions == null) return;

        WorldGuardPlugin wg = (WorldGuardPlugin) ProtectionStones.wgd;

        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager rgm = regionContainer.get(BukkitAdapter.adapt(p.getWorld()));

        String id = "ps" + (long) pb.getLocation().getX() + "x" + (long) pb.getLocation().getY() + "y" + (long) pb.getLocation().getZ() + "z";

        // check if that is actually a protection stone block (owns a region)
        if (rgm.getRegion(id) == null) {

            // prevent silk touching of protection stone blocks (that aren't holding a region)
            if (blockOptions.preventSilkTouch) {
                ItemStack left = e.getPlayer().getInventory().getItemInMainHand();
                ItemStack right = e.getPlayer().getInventory().getItemInOffHand();
                if (!left.containsEnchantment(Enchantment.SILK_TOUCH) && !right.containsEnchantment(Enchantment.SILK_TOUCH)) {
                    return;
                }
                e.setDropItems(false);
            }

            return;
        }

        // check for destroy permission
        if (!p.hasPermission("protectionstones.destroy")) {
            PSL.msg(p, PSL.NO_PERMISSION_DESTROY.msg());
            e.setCancelled(true);
            return;
        }

        // check if player is owner of region
        if (!rgm.getRegion(id).isOwner(wg.wrapPlayer(p)) && !p.hasPermission("protectionstones.superowner")) {
            PSL.msg(p, PSL.NO_REGION_PERMISSION.msg());
            e.setCancelled(true);
            return;
        }

        // return protection stone if no drop option is off
        if (!blockOptions.noDrop) {
            if (!p.getInventory().addItem(ProtectionStones.createProtectBlockItem(blockOptions)).isEmpty()) {
                // method will return not empty if item couldn't be added
                PSL.msg(p, PSL.NO_ROOM_IN_INVENTORY.msg());
                e.setCancelled(true);
                return;
            }
        }

        // remove block
        pb.setType(Material.AIR);
        rgm.removeRegion(id);

        try {
            rgm.save();
        } catch (Exception e1) {
            ProtectionStones.getPlugin().getLogger().severe("WorldGuard Error [" + e1 + "] during Region File Save");
        }
        PSL.msg(p, PSL.NO_LONGER_PROTECTED.msg());

        e.setDropItems(false);
        e.setExpToDrop(0);
    }

    private void pistonUtil(List<Block> pushedBlocks, BlockPistonEvent e) {
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager rgm = regionContainer.get(BukkitAdapter.adapt(e.getBlock().getWorld()));
        for (Block b : pushedBlocks) {
            ConfigProtectBlock cpb = ProtectionStones.getBlockOptions(b.getType().toString());
            if (cpb != null && rgm.getRegion("ps" + b.getX() + "x" + b.getY() + "y" + b.getZ() + "z") != null && cpb.preventPistonPush) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager rgm = regionContainer.get(BukkitAdapter.adapt(e.getEntity().getWorld()));

        // loop through exploded blocks
        for (int i = 0; i < e.blockList().size(); i++) {
            Block b = e.blockList().get(i);

            if (ProtectionStones.isProtectBlock(b.getType().toString())) {
                String id = "ps" + b.getX() + "x" + b.getY() + "y" + b.getZ() + "z";
                if (rgm.getRegion(id) != null) {
                    if (ProtectionStones.getBlockOptions(b.getType().toString()).preventExplode) {
                        // remove block from exploded list if prevent_explode is enabled
                        e.blockList().remove(i);
                        i--;
                    } else if (ProtectionStones.getBlockOptions(b.getType().toString()).destroyRegionWhenExplode) {
                        // remove region from worldguard if destroy_region_when_explode is enabled
                        rgm.removeRegion(id);
                        try {
                            rgm.save();
                        } catch (StorageException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        pistonUtil(e.getBlocks(), e);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        pistonUtil(e.getBlocks(), e);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == TeleportCause.ENDER_PEARL || event.getCause() == TeleportCause.CHORUS_FRUIT) return;

        if (event.getPlayer().hasPermission("protectionstones.tp.bypassprevent")) return;

        WorldGuardPlugin wg = (WorldGuardPlugin) ProtectionStones.wgd;
        RegionManager rgm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(event.getTo().getWorld()));
        BlockVector3 v = BlockVector3.at(event.getTo().getX(), event.getTo().getY(), event.getTo().getZ());

        // check if player can teleport into region (no region with preventTeleportIn = true)
        ApplicableRegionSet regions = rgm.getApplicableRegions(v);
        if (regions.getRegions().isEmpty()) return;
        boolean foundNoTeleport = false;
        for (ProtectedRegion r : regions) {
            String f = r.getFlag(FlagHandler.PS_BLOCK_MATERIAL);
            if (f != null && ProtectionStones.getBlockOptions(f) != null && ProtectionStones.getBlockOptions(f).preventTeleportIn)
                foundNoTeleport = true;
            if (r.getOwners().contains(wg.wrapPlayer(event.getPlayer()))) return;
        }

        if (foundNoTeleport) {
            PSL.msg(event.getPlayer(), PSL.REGION_CANT_TELEPORT.msg());
            event.setCancelled(true);
        }
    }

}
