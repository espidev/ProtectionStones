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

package me.vik1395.ProtectionStones.commands;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.vik1395.ProtectionStones.PSLocation;
import me.vik1395.ProtectionStones.ProtectionStones;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArgReclaim {
    // /ps reclaim
    public static boolean argumentReclaim(Player p, String[] args, String psID) { // psID: id of the current region the player is in
        WorldGuardPlugin wg = (WorldGuardPlugin) ProtectionStones.wgd;
        RegionManager rgm = ProtectionStones.getRegionManagerWithPlayer(p);
        if (p.hasPermission("protectionstones.reclaim")) {
            p.sendMessage(ChatColor.RED + "You don't have permission to use the Reclaim Command");
            return true;
        }
        if (psID.equals("")) {
            p.sendMessage(ChatColor.RED + "You are not in a protection stone region!");
            return true;
        }
        ProtectedRegion region = rgm.getRegion(psID);

        if (region == null) {
            p.sendMessage(ChatColor.YELLOW + "You are currently not in a region.");
            return true;
        }
        if (!psID.substring(0, 2).equals("ps")) {
            p.sendMessage(ChatColor.YELLOW + "You are currently not in a protection stones region.");
            return true;
        }

        PSLocation psl = ProtectionStones.parsePSRegionToLocation(psID);
        Block blockToUnhide = p.getWorld().getBlockAt(psl.x, psl.y, psl.z);
        String entry;
        String setmat = null;

        // Retrieve stored block data if air from file and delete from file
        if (blockToUnhide.getType() == Material.AIR) {
            YamlConfiguration hideFile = YamlConfiguration.loadConfiguration(ProtectionStones.psStoneData);
            entry = (int) blockToUnhide.getLocation().getX() + "x";
            entry = entry + (int) blockToUnhide.getLocation().getY() + "y";
            entry = entry + (int) blockToUnhide.getLocation().getZ() + "z";
            setmat = hideFile.getString(entry);
            hideFile.set(entry, null);
            try {
                hideFile.save(ProtectionStones.psStoneData);
            } catch (IOException ex) {
                Logger.getLogger(ProtectionStones.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // set block data
        int type = 0;
        String blocktypedata = blockToUnhide.getType().toString() + "-" + blockToUnhide.getData();
        if (ProtectionStones.mats.contains(blocktypedata)) {
            type = 1;
        } else if (ProtectionStones.mats.contains(blockToUnhide.getType().toString())) {
            type = 2;
        }

        if (setmat != null) blockToUnhide.setType(Material.getMaterial(setmat));

        BlockVector3 max = region.getMaximumPoint();
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 middle = max.add(min).divide(2);

        Collection<Block> blocks = new HashSet<>();
        if (type == 2) blocktypedata = blockToUnhide.getType().toString();
        if (ProtectionStones.StoneTypeData.RegionY(blocktypedata) == 0) {
            double xx = middle.getX();
            double zz = middle.getZ();
            for (double yy = 0; yy <= p.getWorld().getMaxHeight(); yy++) {
                Block block = new Location(p.getWorld(), xx, yy, zz).getBlock();
                if (ProtectionStones.mats.contains(block.getType().toString() + "-" + block.getData())) {
                    blocks.add(new Location(p.getWorld(), xx, yy, zz).getBlock());
                } else if (ProtectionStones.mats.contains(block.getType().toString())) {
                    blocks.add(new Location(p.getWorld(), xx, yy, zz).getBlock());
                }
            }
        }


        if (!region.isOwner(wg.wrapPlayer(p)) && !p.hasPermission("protectionstones.superowner")) {
            p.sendMessage(ChatColor.YELLOW + "You are not the owner of this region.");
            return true;
        }

        // Find centre of protection stone
        Block middleblock;
        Block it = null;
        if (!(blocks.isEmpty())) {
            it = blocks.iterator().next();
        }
        if (it != null && ProtectionStones.StoneTypeData.RegionY(it.getType().toString() + "-" + it.getData()) == 0) {
            middleblock = it;
        } else if (it != null && ProtectionStones.StoneTypeData.RegionY(it.getType().toString()) == 0) {
            middleblock = it;
        } else {
            middleblock = p.getWorld().getBlockAt(middle.getX(), middle.getY(), middle.getZ());
        }

        // Return and remove protection stone
        if (!ProtectionStones.StoneTypeData.NoDrop(middleblock.getType().toString() + "-" + middleblock.getData()) && !ProtectionStones.StoneTypeData.NoDrop(middleblock.getType().toString())) {
            ItemStack oreblock = new ItemStack(middleblock.getType(), 1, middleblock.getData());
            boolean freeSpace = false;

            for (ItemStack is : p.getInventory().getContents()) {
                if (is == null) {
                    freeSpace = true;
                    break;
                }
            }

            // return protection stone
            if (freeSpace) {
                PlayerInventory inventory = p.getInventory();
                inventory.addItem(oreblock);
            } else {
                p.sendMessage(ChatColor.RED + "You don't have enough room in your inventory.");
                return true;
            }
        }

        // remove region
        middleblock.setType(Material.AIR);
        rgm.removeRegion(psID);
        try {
            rgm.save();
        } catch (Exception e1) {
            Bukkit.getLogger().severe("[ProtectionStones] WorldGuard Error [" + e1 + "] during Region File Save");
        }
        p.sendMessage(ChatColor.YELLOW + "This area is no longer protected.");

        return true;
    }
}
