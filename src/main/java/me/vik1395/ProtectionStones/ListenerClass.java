package me.vik1395.ProtectionStones;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/*

Author: Vik1395
Project: VanishBungee

Copyright 2015

Licensed under Creative CommonsAttribution-ShareAlike 4.0 International Public License (the "License");
You may not use this file except in compliance with the License.

You may obtain a copy of the License at http://creativecommons.org/licenses/by-sa/4.0/legalcode

You may find an abridged version of the License at http://creativecommons.org/licenses/by-sa/4.0/
 */

public class ListenerClass implements Listener {
    StoneTypeData StoneTypeData = new StoneTypeData();
    private HashMap<Player, Double> lastProtectStonePlaced = new HashMap<>();

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        WorldGuardPlugin wg = (WorldGuardPlugin) Main.wgd;
        Player p = e.getPlayer();
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager rm = regionContainer.get(BukkitAdapter.adapt(p.getWorld()));
        Block b = e.getBlock();
        LocalPlayer lp = wg.wrapPlayer(p);
        int count = rm.getRegionCountOfPlayer(lp);
        if (Main.mats == null) {
            e.setCancelled(false);
            return;
        }
        int type = 0;
        String blocktypedata = b.getType().toString() + "-" + b.getData();
        String blocktype = b.getType().toString();
        if (Main.mats.contains(blocktypedata)) {
            type = 1;
        } else if (Main.mats.contains(blocktype)) {
            type = 2;
        }
        if (type > 0) {
            if (Main.isCooldownEnable) {
                double currentTime = System.currentTimeMillis();
                if (this.lastProtectStonePlaced.containsKey(p)) {
                    int cooldown = Main.cooldown;
                    double lastPlace = this.lastProtectStonePlaced.get(p);
                    if (lastPlace + cooldown > currentTime) {
                        e.setCancelled(true);
                        if (Main.cooldownMessage == null) return;
                        String cooldownMessage = Main.cooldownMessage.replace("%time%", String.format("%.1f", (cooldown / 1000) - ((currentTime - lastPlace) / 1000)));
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', cooldownMessage));
                        return;
                    }
                }
                this.lastProtectStonePlaced.put(p, currentTime);
            }
            if (wg.createProtectionQuery().testBlockPlace(p, b.getLocation(), b.getType())) {
                if (p.hasPermission("protectionstones.create")) {
                    if (Main.toggleList != null) {
                        for (String temp : Main.toggleList) {
                            if (temp.equalsIgnoreCase(p.getName())) {
                                e.setCancelled(false);
                                return;
                            }
                        }
                    }
                    if (!p.hasPermission("protectionstones.admin")) {
                        int max = 0;
                        for (PermissionAttachmentInfo rawperm : p.getEffectivePermissions()) {
                            String perm = rawperm.getPermission();
                            if (perm.startsWith("protectionstones.limit")) {
                                try {
                                    int lim = Integer.parseInt(perm.substring(23));
                                    if (lim > max) {
                                        max = lim;
                                    }
                                } catch (Exception er) {
                                    max = 0;
                                }
                            }
                        }
                        if (count >= max) {
                            if (max != 0) {
                                p.sendMessage(ChatColor.RED + "You can not create any more protected regions");
                                e.setCancelled(true);
                                return;
                            }
                        }
                        for (String world : Main.deniedWorlds) {
                            if (world.equals(p.getLocation().getWorld().getName())) {
                                p.sendMessage(ChatColor.RED + "You can not create protections in this world");
                                e.setCancelled(true);
                                return;
                            }
                        }
                    }

                    double bx = b.getLocation().getX();
                    double by = b.getLocation().getY();
                    double bz = b.getLocation().getZ();
                    Vector v1 = null, v2 = null;
                    blocktypedata = b.getType().toString() + "-" + b.getData();
                    blocktype = b.getType().toString();
                    if (type == 1) {
                        if (StoneTypeData.RegionY(blocktypedata) == -1) {
                            v1 = new Vector(bx - StoneTypeData.RegionX(blocktypedata), 0, bz - StoneTypeData.RegionZ(blocktypedata));
                            v2 = new Vector(bx + StoneTypeData.RegionX(blocktypedata), p.getWorld().getMaxHeight(), bz + StoneTypeData.RegionZ(blocktypedata));
                        } else {
                            v1 = new Vector(bx - StoneTypeData.RegionX(blocktypedata), by - StoneTypeData.RegionY(blocktypedata), bz - StoneTypeData.RegionZ(blocktypedata));
                            v2 = new Vector(bx + StoneTypeData.RegionX(blocktypedata), by + StoneTypeData.RegionY(blocktypedata), bz + StoneTypeData.RegionZ(blocktypedata));
                        }
                    } else {
                        if (StoneTypeData.RegionY(b.getType().toString()) == -1) {
                            v1 = new Vector(bx - StoneTypeData.RegionX(blocktype), 0, bz - StoneTypeData.RegionZ(blocktype));
                            v2 = new Vector(bx + StoneTypeData.RegionX(blocktype), p.getWorld().getMaxHeight(), bz + StoneTypeData.RegionZ(blocktype));
                        } else {
                            v1 = new Vector(bx - StoneTypeData.RegionX(blocktype), by - StoneTypeData.RegionY(blocktype), bz - StoneTypeData.RegionZ(blocktype));
                            v2 = new Vector(bx + StoneTypeData.RegionX(blocktype), by + StoneTypeData.RegionY(blocktype), bz + StoneTypeData.RegionZ(blocktype));
                        }
                    }
                    BlockVector min = v1.toBlockVector();
                    BlockVector max = v2.toBlockVector();
                    String id = "ps" + (int) bx + "x" + (int) by + "y" + (int) bz + "z";

                    ProtectedRegion region = new ProtectedCuboidRegion(id, min, max);
                    region.getOwners().addPlayer(p.getName());
                    if (Main.uuid) {
                        region.getOwners().addPlayer(p.getUniqueId());

                    }

                    rm.addRegion(region);
                    boolean overLap = rm.overlapsUnownedRegion(region, lp);
                    if (overLap) {
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
                            } // commented out below because the region gets removed anyways ¯\_(ツ)_/¯
                            //if (!p.hasPermission("protectionstones.admin")) {
                            p.sendMessage(ChatColor.YELLOW + "You can not place a protection here as it overlaps another region");
                            e.setCancelled(true);
                            return;
                            //}
                        }


                    }

                    HashMap<Flag<?>, Object> newFlags = new HashMap<Flag<?>, Object>();
                    for (Flag<?> iFlag : WorldGuard.getInstance().getFlagRegistry().getAll()) {
                        for (int j = 0; j < Main.flags.size(); j++) {
                            String[] rawflag = Main.flags.get(j).split(" ");
                            String flag = rawflag[0];
                            String setting = Main.flags.get(j).replace(flag + " ", "");
                            if (iFlag.getName().equalsIgnoreCase(flag)) {
                                if ((iFlag.getName().equalsIgnoreCase("greeting")) || (iFlag.getName().equalsIgnoreCase("farewell"))) {
                                    String msg = setting.replaceAll("%player%", p.getName());
                                    newFlags.put(iFlag, msg);
                                } else {
                                    if (setting.equalsIgnoreCase("allow")) {
                                        newFlags.put(iFlag, StateFlag.State.ALLOW);
                                    } else if (setting.equalsIgnoreCase("deny")) {
                                        newFlags.put(iFlag, StateFlag.State.DENY);
                                    } else if (setting.equalsIgnoreCase("true")) {
                                        newFlags.put(iFlag, true);
                                    } else if (setting.equalsIgnoreCase("false")) {
                                        newFlags.put(iFlag, false);
                                    } else {
                                        newFlags.put(iFlag, setting);
                                    }
                                }
                            }
                        }
                    }
                    region.setFlags(newFlags);
                    region.setPriority(Main.priority);
                    p.sendMessage(ChatColor.YELLOW + "This area is now protected.");
                    try {
                        rm.saveChanges();
                        rm.save();
                    } catch (StorageException e1) {
                        e1.printStackTrace();
                    }
                    if (type == 2) blocktypedata = b.getType().toString();
                    if (StoneTypeData.AutoHide(blocktypedata)) {
                        ItemStack ore = p.getItemInHand();
                        ore.setAmount(ore.getAmount() - 1);
                        p.setItemInHand(ore.getAmount() == 0 ? null : ore);
                        Block blockToHide = p.getWorld().getBlockAt((int) bx, (int) by, (int) bz);
                        YamlConfiguration hideFile = YamlConfiguration.loadConfiguration(Main.psStoneData);
                        String entry = (int) blockToHide.getLocation().getX() + "x";
                        entry = entry + (int) blockToHide.getLocation().getY() + "y";
                        entry = entry + (int) blockToHide.getLocation().getZ() + "z";
                        hideFile.set(entry, blockToHide.getType().toString());
                        b.setType(Material.AIR);
                        try {
                            hideFile.save(Main.psStoneData);
                        } catch (IOException ex) {
                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "You don't have permission to place a ProtectionStone.");
                    e.setCancelled(true);
                }
            } else {
                p.sendMessage(ChatColor.RED + "You can't protect that area.");
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        WorldGuardPlugin wg = (WorldGuardPlugin) Main.wgd;
        Player player = e.getPlayer();
        Block pb = e.getBlock();

        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager rgm = regionContainer.get(BukkitAdapter.adapt(player.getWorld()));
        if (Main.mats == null) {
            e.setCancelled(false);
            return;
        }
        int type = 0;
        String blocktypedata = pb.getType().toString() + "-" + pb.getData();
        String blocktype = pb.getType().toString();
        if (Main.mats.contains(blocktypedata)) {
            type = 1;
        } else if (Main.mats.contains(blocktype)) {
            type = 2;
        }
        if (type > 0) {
            RegionManager regionManager = rgm;
            String psx = Double.toString(pb.getLocation().getX());
            String psy = Double.toString(pb.getLocation().getY());
            String psz = Double.toString(pb.getLocation().getZ());
            String id = (new StringBuilder("ps")).append(psx.substring(0, psx.indexOf("."))).append("x").append(psy.substring(0, psy.indexOf("."))).append("y").append(psz.substring(0, psz.indexOf("."))).append("z").toString();
            if (wg.createProtectionQuery().testBlockBreak(player, pb)) {
                if (player.hasPermission("protectionstones.destroy")) {
                    if (type == 2) blocktypedata = pb.getType().toString();
                    if (regionManager.getRegion(id) != null) {
                        LocalPlayer localPlayer = wg.wrapPlayer(player);
                        if (regionManager.getRegion(id).isOwner(localPlayer) || player.hasPermission("protectionstones.superowner")) {
                            if (!StoneTypeData.NoDrop(blocktypedata)) {
                                ItemStack oreblock = new ItemStack(pb.getType(), 1, pb.getData());
                                int freeSpace = 0;
                                for (ListIterator<ItemStack> iterator = player.getInventory().iterator(); iterator.hasNext(); ) {
                                    ItemStack i = (ItemStack) iterator.next();
                                    if (i == null) {
                                        freeSpace += oreblock.getType().getMaxStackSize();
                                    } else if (i.getType() == oreblock.getType()) {
                                        freeSpace += i.getType().getMaxStackSize() - i.getAmount();
                                    }
                                }
                                if (freeSpace >= 1) {
                                    PlayerInventory inventory = player.getInventory();
                                    inventory.addItem(new ItemStack[]{
                                            oreblock
                                    });
                                    pb.setType(Material.AIR);
                                    regionManager.removeRegion(id);
                                    try {
                                        rgm.save();
                                    } catch (Exception e1) {
                                        System.out.println("[ProtectionStones] WorldGuard Error [" + e1 + "] during Region File Save");
                                    }
                                    player.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("This area is no longer protected.").toString());
                                } else {
                                    player.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You don't have enough room in your inventory.").toString());
                                }
                            } else {
                                pb.setType(Material.AIR);
                                regionManager.removeRegion(id);
                                try {
                                    rgm.save();
                                } catch (Exception e1) {
                                    System.out.println("[ProtectionStones] WorldGuard Error [" + e1 + "] during Region File Save");
                                }
                                player.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("This area is no longer protected.").toString());
                            }
                            e.setCancelled(true);
                        } else {
                            player.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You are not the owner of this region.").toString());
                            e.setCancelled(true);
                        }
                    } else if (StoneTypeData.SilkTouch(blocktypedata)) {
                        e.setCancelled(true);
                        ItemStack left = e.getPlayer().getInventory().getItemInMainHand();
                        ItemStack right = e.getPlayer().getInventory().getItemInOffHand();
                        Collection<ItemStack> drops = null;
                        Collection<ItemStack> baseDrops = null;
                        if (left.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS)) {
                            baseDrops = pb.getDrops(left);
                            if (!(baseDrops.isEmpty())) {
                                int fortuneLevel = left.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
                                if (fortuneLevel > 5) fortuneLevel = 5;
                                ItemStack stack = baseDrops.iterator().next();
                                double amount = stack.getAmount();
                                amount = Math.random() * amount * fortuneLevel + 2;
                                stack.setAmount((int) amount);
                            }
                            drops = baseDrops;
                        } else if (right.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS)) {
                            baseDrops = pb.getDrops(right);
                            if (!(baseDrops.isEmpty())) {
                                int fortuneLevel = right.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
                                if (fortuneLevel > 5) fortuneLevel = 5;
                                ItemStack stack = baseDrops.iterator().next();
                                double amount = stack.getAmount();
                                amount = Math.random() * amount * fortuneLevel + 2;
                                stack.setAmount((int) amount);
                            }
                            drops = baseDrops;
                        } else {
                            pb.breakNaturally();
                            return;
                        }
                        for (ItemStack drop : drops) {
                            pb.getWorld().dropItem(pb.getLocation(), drop);
                        }
                        pb.setType(Material.AIR);

                    } else {
                        e.setCancelled(false);
                    }
                } else {
                    e.setCancelled(true);
                }
            } else {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        List<Block> pushedBlocks = e.getBlocks();
        if (pushedBlocks != null) {
            Iterator<Block> it = pushedBlocks.iterator();
            while (it.hasNext()) {
                Block b = it.next();
                int type = 0;
                String blocktypedata = b.getType().toString() + "-" + b.getData();
                if (Main.mats.contains(blocktypedata)) {
                    type = 1;
                } else if (Main.mats.contains(b.getType().toString())) {
                    type = 2;
                }
                if (type == 2) blocktypedata = b.getType().toString();
                if (type > 0) {
                    if (StoneTypeData.BlockPiston(blocktypedata)) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        List<Block> retractedBlocks = e.getBlocks();
        if (retractedBlocks != null) {
            Iterator<Block> it = retractedBlocks.iterator();
            while (it.hasNext()) {
                Block b = it.next();
                int type = 0;
                String blocktypedata = b.getType().toString() + "-" + b.getData();
                if (Main.mats.contains(blocktypedata)) {
                    type = 1;
                } else if (Main.mats.contains(b.getType().toString())) {
                    type = 2;
                }
                if (type == 2) blocktypedata = b.getType().toString();
                if (type > 0) {
                    if (StoneTypeData.BlockPiston(blocktypedata)) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (Main.config.getBoolean("Teleport to PVP.Block Teleport") == true) {
            Player p = event.getPlayer();
            WorldGuardPlugin wg = (WorldGuardPlugin) Main.wgd;

            if (!wg.isEnabled()) {
                return;
            }
            RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager rgm = regionContainer.get(BukkitAdapter.adapt(event.getFrom().getWorld()));
            Vector v = new Vector(event.getTo().getX(), event.getTo().getY(), event.getTo().getZ());
            if (rgm.getApplicableRegions(v) != null) {
                ApplicableRegionSet regions = rgm.getApplicableRegions(v);
                ApplicableRegionSet regionsFrom = rgm.getApplicableRegions(v);

                if (event.getCause() == TeleportCause.ENDER_PEARL) return;
                try {
                    if (event.getCause() == TeleportCause.CHORUS_FRUIT) return;
                } catch (NoSuchFieldError e1) {
                }
                boolean ownsAll = false;
                for (ProtectedRegion r : regions) {
                    if (r.getOwners().contains(p.getName())) {
                        ownsAll = true;
                    }
                }
                if (!ownsAll) {
                    if (p.hasMetadata("psBypass")) {
                        List<MetadataValue> values = p.getMetadata("psBypass");
                        for (MetadataValue value : values) {
                            if (value.asBoolean() == true) {
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
        if (Main.config.getBoolean("Teleport to PVP.Display Warning") == true) {
            Player p = event.getPlayer();
            WorldGuardPlugin wg = (WorldGuardPlugin) Main.wgd;

            if (!wg.isEnabled()) {
                return;
            }
            RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager rgm = regionContainer.get(BukkitAdapter.adapt(event.getFrom().getWorld()));
            Vector v = new Vector(event.getTo().getX(), event.getTo().getY(), event.getTo().getZ());
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