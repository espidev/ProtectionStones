package me.vik1395.ProtectionStones;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.PermissionAttachmentInfo;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.file.YamlConfiguration;

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
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        WorldGuardPlugin wg = (WorldGuardPlugin) Main.wgd;
        Player p = e.getPlayer();
        Block b = e.getBlock();
        LocalPlayer lp = wg.wrapPlayer(p);
        int count = wg.getRegionManager(p.getWorld()).getRegionCountOfPlayer(lp);
        if (Main.mats == null) {
            e.setCancelled(false);
            return; 
        }
        int type = 0;
        String blocktypedata = b.getType().toString() + "-" +  b.getData();
        String blocktype = b.getType().toString();
        if(Main.mats.contains(blocktypedata)) {
            type = 1;
        } else if (Main.mats.contains(blocktype)) {
            type = 2;
        }
        if (type > 0) {
            if (wg.canBuild(p, b.getLocation())) {
                if(p.hasPermission("protectionstones.create")) {
                    if(Main.toggleList!=null) {
                        for (String temp : Main.toggleList) {
                            if (temp.equalsIgnoreCase(p.getName())) {
                                e.setCancelled(false);
                                return;
                            }
                        }
                    }
                    if(!p.hasPermission("protectionstones.admin")) {
                        int max = 0;
                        for(PermissionAttachmentInfo rawperm : p.getEffectivePermissions()) {
                            String perm = rawperm.getPermission();
                            if(perm.startsWith("protectionstones.limit")) {
                                try {
                                    int lim = Integer.parseInt(perm.substring(23));
                                    if(lim>max) {
                                        max = lim;
                                    }
                                } catch (Exception er) {
                                    max = 0;
                                }
                            }
                        }
                        if(count>=max) {
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
                    Vector v1=null, v2=null;
                    blocktypedata = b.getType().toString() + "-" + b.getData();
                    blocktype = b.getType().toString();
                    if(type == 1) {
                        if(StoneTypeData.RegionY(blocktypedata) == -1) {
                            v1 = new Vector(bx - StoneTypeData.RegionX(blocktypedata), 0, bz - StoneTypeData.RegionZ(blocktypedata));
                            v2 = new Vector(bx + StoneTypeData.RegionX(blocktypedata), p.getWorld().getMaxHeight(), bz + StoneTypeData.RegionZ(blocktypedata));
                        } else {
                            v1 = new Vector(bx - StoneTypeData.RegionX(blocktypedata), by - StoneTypeData.RegionY(blocktypedata), bz - StoneTypeData.RegionZ(blocktypedata));
                            v2 = new Vector(bx + StoneTypeData.RegionX(blocktypedata), by + StoneTypeData.RegionY(blocktypedata), bz + StoneTypeData.RegionZ(blocktypedata));
                        }
                    } else {
                        if(StoneTypeData.RegionY(b.getType().toString()) == -1) {
                            v1 = new Vector(bx - StoneTypeData.RegionX(blocktype), 0, bz - StoneTypeData.RegionZ(blocktype));
                            v2 = new Vector(bx + StoneTypeData.RegionX(blocktype), p.getWorld().getMaxHeight(), bz + StoneTypeData.RegionZ(blocktype));
                        } else {
                            v1 = new Vector(bx - StoneTypeData.RegionX(blocktype), by - StoneTypeData.RegionY(blocktype), bz - StoneTypeData.RegionZ(blocktype));
                            v2 = new Vector(bx + StoneTypeData.RegionX(blocktype), by + StoneTypeData.RegionY(blocktype), bz + StoneTypeData.RegionZ(blocktype));
                        }
                    }
                    BlockVector min = v1.toBlockVector();
                    BlockVector max = v2.toBlockVector();
                    String id = "ps" + (int)bx + "x" + (int)by + "y" + (int)bz + "z";

                    RegionManager rgm = wg.getRegionManager(p.getWorld());
                    ProtectedRegion region = new ProtectedCuboidRegion(id, min, max);
                    region.getOwners().addPlayer(p.getName());
                    if (Main.uuid) {
                        region.getOwners().addPlayer(p.getUniqueId());
                        
                    }
                    
                    rgm.addRegion(region);
                    boolean overLap = rgm.overlapsUnownedRegion(region, lp);
                    if (overLap) {
                        rgm.removeRegion(id);
                        p.updateInventory();
                        try {
                            rgm.saveChanges();
                            rgm.save();
                        } catch (StorageException e1) {
                            e1.printStackTrace();
                        }
                        if (!p.hasPermission("protectionstones.admin")) {
                            p.sendMessage(ChatColor.RED + "You can not a protection here as it overlaps another unowned region");
                            e.setCancelled(true);
                            return;
                        }						
                    }

                    HashMap<Flag<?>, Object> newFlags = new HashMap<Flag<?>, Object>();
                    for (int i = 0; i < DefaultFlag.flagsList.length; i++) {
                        for(int j = 0; j<Main.flags.size();j++) {
                            String[] rawflag = Main.flags.get(j).split(" ");
                            String flag = rawflag[0];
                            String setting = Main.flags.get(j).replace(flag + " ", "");
                            if (DefaultFlag.flagsList[i].getName().equalsIgnoreCase(flag)) {
                                if (setting != null) {
                                    if ((DefaultFlag.flagsList[i].getName().equalsIgnoreCase("greeting")) || (DefaultFlag.flagsList[i].getName().equalsIgnoreCase("farewell"))) {
                                        String msg = setting.replaceAll("%player%", p.getName());
                                        newFlags.put(DefaultFlag.flagsList[i], msg);
                                    } else {
                                        if(setting.equalsIgnoreCase("allow")) {
                                            newFlags.put(DefaultFlag.flagsList[i], StateFlag.State.ALLOW);
                                        } else if(setting.equalsIgnoreCase("deny")) {
                                                newFlags.put(DefaultFlag.flagsList[i], StateFlag.State.DENY);
                                        } else if(setting.equalsIgnoreCase("true")) {
                                                newFlags.put(DefaultFlag.flagsList[i], true);
                                        } else if(setting.equalsIgnoreCase("false")) {
                                                newFlags.put(DefaultFlag.flagsList[i], false);
                                        } else {
                                                newFlags.put(DefaultFlag.flagsList[i], setting);
                                        }
                                    }
                                } else {
                                    newFlags.put(DefaultFlag.flagsList[i], null);
                                }
                            }
                        }
                    }
                    region.setFlags(newFlags);
                    region.setPriority(Main.priority);
                    p.sendMessage(ChatColor.YELLOW + "This area is now protected.");
                    try {
                        rgm.saveChanges();
                        rgm.save();
                    } catch (StorageException e1) {
                        e1.printStackTrace();
                    }
                    if (type == 2) blocktypedata = b.getType().toString();
                    if(StoneTypeData.AutoHide(blocktypedata)) {
                        ItemStack ore = p.getItemInHand();
                        ore.setAmount(ore.getAmount() - 1);
                        p.setItemInHand(ore.getAmount() == 0 ? null : ore);
                        Block blockToHide = p.getWorld().getBlockAt((int)bx, (int)by,(int)bz);
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
        RegionManager rgm = wg.getRegionManager(player.getWorld());
        if (Main.mats == null) {
            e.setCancelled(false);
            return; 
        }
        int type = 0;
        String blocktypedata = pb.getType().toString() + "-" +  pb.getData();
        String blocktype = pb.getType().toString();
        if(Main.mats.contains(blocktypedata)) {
            type = 1;
        } else if (Main.mats.contains(blocktype)) {
            type = 2;
        }
        if (type > 0) {
            World world = player.getWorld();
            RegionManager regionManager = wg.getRegionManager(world);
            String psx = Double.toString(pb.getLocation().getX());
            String psy = Double.toString(pb.getLocation().getY());
            String psz = Double.toString(pb.getLocation().getZ());
            String id = (new StringBuilder("ps")).append(psx.substring(0, psx.indexOf("."))).append("x").append(psy.substring(0, psy.indexOf("."))).append("y").append(psz.substring(0, psz.indexOf("."))).append("z").toString();
            if(wg.canBuild(player, pb.getLocation())) {
                if(player.hasPermission("protectionstones.destroy")) {
                    if (type == 2) blocktypedata = pb.getType().toString();
                    if(regionManager.getRegion(id) != null) {
                        LocalPlayer localPlayer = wg.wrapPlayer(player);
                        if(regionManager.getRegion(id).isOwner(localPlayer) || player.hasPermission("protectionstones.superowner")) {
                            if(!StoneTypeData.NoDrop(blocktypedata)) {
                                ItemStack oreblock = new ItemStack(pb.getType(), 1, pb.getData());
                                int freeSpace = 0;
                                for(ListIterator<ItemStack> iterator = player.getInventory().iterator(); iterator.hasNext();) {
                                    ItemStack i = (ItemStack)iterator.next();
                                    if(i == null) {
                                        freeSpace += oreblock.getType().getMaxStackSize();
                                    } else if(i.getType() == oreblock.getType()) {
                                        freeSpace += i.getType().getMaxStackSize() - i.getAmount();
                                    }
                                }
                                if(freeSpace >= 1) {
                                    PlayerInventory inventory = player.getInventory();
                                    inventory.addItem(new ItemStack[] {
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
                    } else if(StoneTypeData.SilkTouch(blocktypedata)) {
                        pb.breakNaturally();
                        e.setCancelled(true);
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
            while(it.hasNext()) {
                Block b = it.next();
                int type = 0;
                String blocktypedata = b.getType().toString() + "-" + b.getData();
                if(Main.mats.contains(blocktypedata)) {
                    type = 1;
                } else if (Main.mats.contains(b.getType().toString())) {
                    type = 2;
                }
                if (type == 2) blocktypedata = b.getType().toString();
                if (type > 0) {
                    if (StoneTypeData.BlockPiston(blocktypedata)){   
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
            while(it.hasNext()) {
                Block b = it.next();
                int type = 0;
                String blocktypedata = b.getType().toString() + "-" + b.getData();
                if(Main.mats.contains(blocktypedata)) {
                    type = 1;
                } else if (Main.mats.contains(b.getType().toString())) {
                    type = 2;
                }
                if (type == 2) blocktypedata = b.getType().toString();
                if (type > 0) {
                    if (StoneTypeData.BlockPiston(blocktypedata)){
                       e.setCancelled(true);
                    }
                }
            }
        }
    }
}
