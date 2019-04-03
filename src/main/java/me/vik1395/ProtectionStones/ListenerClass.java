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
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ListenerClass implements Listener {
    private static HashMap<Player, Double> lastProtectStonePlaced = new HashMap<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        ProtectionStones.uuidToName.put(e.getPlayer().getUniqueId(), e.getPlayer().getName());
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        Block b = e.getBlock();

        if (ProtectionStones.protectBlocks == null) return;

        String blockType = b.getType().toString();
        ConfigProtectBlock blockOptions = ProtectionStones.getProtectStoneOptions(blockType);

        // check if the block is a protection stone
        if (!ProtectionStones.protectBlocks.contains(blockType) || blockOptions == null) return;

        // check if player has toggled off placement of protection stones
        if (ProtectionStones.toggleList.contains(p.getName())) return;

        // check permission
        if (!p.hasPermission("protectionstones.create")) {
            p.sendMessage(ChatColor.RED + "You don't have permission to place a ProtectionStone.");
            e.setCancelled(true);
            return;
        }

        WorldGuardPlugin wg = (WorldGuardPlugin) ProtectionStones.wgd;
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager rm = regionContainer.get(BukkitAdapter.adapt(e.getPlayer().getWorld()));

        LocalPlayer lp = wg.wrapPlayer(p);

        // check if player can place block in that area
        if (!wg.createProtectionQuery().testBlockPlace(p, b.getLocation(), b.getType())) {
            p.sendMessage(ChatColor.RED + "You can't protect that area.");
            e.setCancelled(true);
            return;
        }

        // check cooldown
        if (ProtectionStones.isCooldownEnable) {
            double currentTime = System.currentTimeMillis();
            if (lastProtectStonePlaced.containsKey(p)) {
                double cooldown = ProtectionStones.cooldown;
                double lastPlace = lastProtectStonePlaced.get(p);

                if (lastPlace + cooldown > currentTime) { // if cooldown has not been finished
                    e.setCancelled(true);
                    if (ProtectionStones.cooldownMessage == null) return;
                    String cooldownMessage = ProtectionStones.cooldownMessage.replace("%time%", String.format("%.1f", (cooldown / 1000) - ((currentTime - lastPlace) / 1000)));
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', cooldownMessage));
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
                    p.sendMessage(ChatColor.RED + "You can not create any more protected regions.");
                    e.setCancelled(true);
                    return;
                }
            }
            // check if in denied world
            for (String world : ProtectionStones.deniedWorlds) {
                if (world.trim().equals(p.getLocation().getWorld().getName())) {
                    p.sendMessage(ChatColor.RED + "You can not create protections in this world.");
                    e.setCancelled(true);
                    return;
                }
            }
        }

        // create region
        double bx = b.getLocation().getX();
        double by = b.getLocation().getY();
        double bz = b.getLocation().getZ();
        BlockVector3 v1, v2;

        if (blockOptions.getRegionY() == -1) {
            v1 = BlockVector3.at(bx - blockOptions.getRegionX(), 0, bz - blockOptions.getRegionZ());
            v2 = BlockVector3.at(bx + blockOptions.getRegionX(), p.getWorld().getMaxHeight(), bz + blockOptions.getRegionZ());
        } else {
            v1 = BlockVector3.at(bx - blockOptions.getRegionX(), by - blockOptions.getRegionY(), bz - blockOptions.getRegionZ());
            v2 = BlockVector3.at(bx + blockOptions.getRegionX(), by + blockOptions.getRegionY(), bz + blockOptions.getRegionZ());
        }

        BlockVector3 min = v1;
        BlockVector3 max = v2;
        String id = "ps" + (int) bx + "x" + (int) by + "y" + (int) bz + "z";

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
                p.sendMessage(ChatColor.YELLOW + "You can not place a protection here as it overlaps another region.");
                e.setCancelled(true);
                return;
            }
        }

        // add corresponding flags to new region
        HashMap<Flag<?>, Object> flags = new HashMap<>(FlagHandler.defaultFlags);

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
        region.setPriority(ProtectionStones.priority);
        p.sendMessage(ChatColor.YELLOW + "This area is now protected.");

        // save
        try {
            rm.saveChanges();
            rm.save();
        } catch (StorageException e1) {
            e1.printStackTrace();
        }

        // hide block if auto hide is enabled
        if (blockOptions.isAutoHide()) {
            Block blockToHide = p.getWorld().getBlockAt((int) bx, (int) by, (int) bz);
            YamlConfiguration hideFile = YamlConfiguration.loadConfiguration(ProtectionStones.psStoneData);
            String entry = (int) blockToHide.getLocation().getX() + "x";
            entry = entry + (int) blockToHide.getLocation().getY() + "y";
            entry = entry + (int) blockToHide.getLocation().getZ() + "z";
            hideFile.set(entry, blockToHide.getType().toString());
            b.setType(Material.AIR);
            Bukkit.getScheduler().runTaskAsynchronously(ProtectionStones.getPlugin(), () -> {
                try {
                    hideFile.save(ProtectionStones.psStoneData);
                } catch (IOException ex) {
                    Logger.getLogger(ProtectionStones.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Block pb = e.getBlock();

        String blockType = pb.getType().toString();
        ConfigProtectBlock blockOptions = ProtectionStones.getProtectStoneOptions(blockType);

        // check if block broken is protection stone
        if (blockOptions == null) return;

        WorldGuardPlugin wg = (WorldGuardPlugin) ProtectionStones.wgd;

        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager rgm = regionContainer.get(BukkitAdapter.adapt(p.getWorld()));

        // check if player can actually break block
        if (!wg.createProtectionQuery().testBlockBreak(p, pb)) {
            e.setCancelled(true);
            return;
        }

        String psx = Double.toString(pb.getLocation().getX());
        String psy = Double.toString(pb.getLocation().getY());
        String psz = Double.toString(pb.getLocation().getZ());
        String id = "ps" + psx.substring(0, psx.indexOf(".")) + "x" + psy.substring(0, psy.indexOf(".")) + "y" + psz.substring(0, psz.indexOf(".")) + "z";

        // check if that is actually a protection stone block (owns a region)
        if (rgm.getRegion(id) == null) {

            // prevent silk touching of protection stone blocks (but non region)
            if (blockOptions.denySilkTouch()) {
                e.setCancelled(true);
                ItemStack left = e.getPlayer().getInventory().getItemInMainHand();
                ItemStack right = e.getPlayer().getInventory().getItemInOffHand();
                Collection<ItemStack> drops;
                if (left.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS)) {
                    drops = getItemStacks(pb, left);
                } else if (right.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS)) {
                    drops = getItemStacks(pb, right);
                } else {
                    pb.breakNaturally();
                    return;
                }
                for (ItemStack drop : drops) {
                    pb.getWorld().dropItem(pb.getLocation(), drop);
                }
                pb.setType(Material.AIR);
            }
            return;
        }

        // check for destroy permission
        if (!p.hasPermission("protectionstones.destroy")) {
            e.setCancelled(true);
            p.sendMessage(ChatColor.RED + "You do not have permissions to break protection stones.");
            return;
        }

        // check if player is owner of region
        if (!rgm.getRegion(id).isOwner(wg.wrapPlayer(p)) && !p.hasPermission("protectionstones.superowner")) {
            p.sendMessage(ChatColor.YELLOW + "You are not the owner of this region.");
            e.setCancelled(true);
            return;
        }

        // return protection stone if no drop option is off
        if (!blockOptions.noDrop()) {
            ItemStack oreblock = new ItemStack(pb.getType(), 1, pb.getData());
            int freeSpace = 0;
            for (ItemStack i : p.getInventory()) {
                if (i == null) {
                    freeSpace += oreblock.getType().getMaxStackSize();
                } else if (i.getType() == oreblock.getType()) {
                    freeSpace += i.getType().getMaxStackSize() - i.getAmount();
                }
            }
            if (freeSpace >= 1) {
                p.getInventory().addItem(oreblock);
            } else {
                p.sendMessage(ChatColor.RED + "You don't have enough room in your inventory.");
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
        p.sendMessage(ChatColor.YELLOW + "This area is no longer protected.");

        e.setDropItems(false);
        e.setExpToDrop(0);
    }

    private Collection<ItemStack> getItemStacks(Block pb, ItemStack left) {
        Collection<ItemStack> baseDrops = pb.getDrops(left);
        if (!baseDrops.isEmpty()) {
            int fortuneLevel = left.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
            if (fortuneLevel > 5) fortuneLevel = 5;
            ItemStack stack = baseDrops.iterator().next();
            double amount = stack.getAmount();
            amount = Math.random() * amount * fortuneLevel + 2;
            stack.setAmount((int) amount);
        }
        return baseDrops;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        List<Block> pushedBlocks = e.getBlocks();
        for (Block b : pushedBlocks) {
            if (ProtectionStones.getProtectStoneOptions(b.getType().toString()).denyBlockPiston()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        List<Block> retractedBlocks = e.getBlocks();
        for (Block b : retractedBlocks) {
            if (ProtectionStones.getProtectStoneOptions(b.getType().toString()).denyBlockPiston()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (ProtectionStones.config.getBoolean("Teleport to PVP.Block Teleport")) {
            Player p = event.getPlayer();
            WorldGuardPlugin wg = (WorldGuardPlugin) ProtectionStones.wgd;

            if (!wg.isEnabled()) return;

            RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager rgm = regionContainer.get(BukkitAdapter.adapt(event.getFrom().getWorld()));
            BlockVector3 v = BlockVector3.at(event.getTo().getX(), event.getTo().getY(), event.getTo().getZ());
            if (rgm.getApplicableRegions(v) != null) {
                ApplicableRegionSet regions = rgm.getApplicableRegions(v);

                if (event.getCause() == TeleportCause.ENDER_PEARL) return;
                try {
                    if (event.getCause() == TeleportCause.CHORUS_FRUIT) return;
                } catch (NoSuchFieldError e1) {
                }

                boolean ownsAll = false;
                for (ProtectedRegion r : regions) {
                    if (r.getOwners().contains(wg.wrapPlayer(p))) {
                        ownsAll = true;
                    }
                }
                if (!ownsAll) {
                    if (p.hasMetadata("psBypass")) {
                        List<MetadataValue> values = p.getMetadata("psBypass");
                        for (MetadataValue value : values) {
                            if (value.asBoolean()) {
                                return;
                            } else {
                                if (regions.testState(WorldGuardPlugin.inst().wrapPlayer(p), Flags.PVP)) {
                                    event.setCancelled(true);
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cTeleportation blocked! &eDestination was a &cPVP &earea and cannot be teleported to."));
                                }
                            }
                        }
                    } else {
                        if (regions.testState(WorldGuardPlugin.inst().wrapPlayer(p), Flags.PVP)) {
                            event.setCancelled(true);
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cTeleportation blocked! &eDestination was a &cPVP &earea and cannot be teleported to."));
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (ProtectionStones.config.getBoolean("Teleport to PVP.Display Warning")) {
            Player p = event.getPlayer();
            WorldGuardPlugin wg = (WorldGuardPlugin) ProtectionStones.wgd;

            if (!wg.isEnabled()) return;
            RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager rgm = regionContainer.get(BukkitAdapter.adapt(event.getFrom().getWorld()));
            BlockVector3 v = BlockVector3.at(event.getTo().getX(), event.getTo().getY(), event.getTo().getZ());
            if (rgm.getApplicableRegions(v) != null) {
                ApplicableRegionSet region = rgm.getApplicableRegions(v);
                ApplicableRegionSet regionFrom = rgm.getApplicableRegions(v);
                if (regionFrom != null) {
                    if (!(regionFrom.testState(WorldGuardPlugin.inst().wrapPlayer(p), Flags.PVP))) {
                        if (region.testState(WorldGuardPlugin.inst().wrapPlayer(p), Flags.PVP)) {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cWarning! &eThis area is a &cPVP &earea! You may &cdie &eand &close stuff&e!"));
                        }
                    }
                }
            }
        }
    }

}
