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

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.vik1395.ProtectionStones.PSLocation;
import me.vik1395.ProtectionStones.ProtectionStones;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArgDisown {
    // /ps reclaim
    public static boolean argumentUnclaim(Player p, String[] args, String psID) { // psID: id of the current region the player is in
        WorldGuardPlugin wg = (WorldGuardPlugin) ProtectionStones.wgd;
        RegionManager rgm = ProtectionStones.getRegionManagerWithPlayer(p);
        if (!p.hasPermission("protectionstones.unclaim")) {
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

        if (!region.isOwner(wg.wrapPlayer(p)) && !p.hasPermission("protectionstones.superowner")) {
            p.sendMessage(ChatColor.YELLOW + "You are not the owner of this region.");
            return true;
        }

        // check if block is hidden first

        PSLocation psl = ProtectionStones.parsePSRegionToLocation(psID);
        Block blockToUnhide = p.getWorld().getBlockAt(psl.x, psl.y, psl.z);
        String entry;

        // Retrieve stored block data if air from file and delete from file
        if (blockToUnhide.getType() == Material.AIR) {
            YamlConfiguration hideFile = YamlConfiguration.loadConfiguration(ProtectionStones.psStoneData);
            entry = (int) blockToUnhide.getLocation().getX() + "x";
            entry = entry + (int) blockToUnhide.getLocation().getY() + "y";
            entry = entry + (int) blockToUnhide.getLocation().getZ() + "z";
            hideFile.set(entry, null);
            try {
                hideFile.save(ProtectionStones.psStoneData);
            } catch (IOException ex) {
                Logger.getLogger(ProtectionStones.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // Return and remove protection stone
        String type = blockToUnhide.getType().toString();
        if (!ProtectionStones.getProtectStoneOptions(type).noDrop()) {

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
                inventory.addItem(new ItemStack(blockToUnhide.getType()));
            } else {
                p.sendMessage(ChatColor.RED + "You don't have enough room in your inventory.");
                return true;
            }
        }

        // remove region
        blockToUnhide.setType(Material.AIR);
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
