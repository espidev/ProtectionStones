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
import me.vik1395.ProtectionStones.PSL;
import me.vik1395.ProtectionStones.PSLocation;
import me.vik1395.ProtectionStones.ProtectionStones;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArgUnclaim {
    // /ps unclaim
    public static boolean argumentUnclaim(Player p, String[] args, String psID) { // psID: id of the current region the player is in
        WorldGuardPlugin wg = (WorldGuardPlugin) ProtectionStones.wgd;
        RegionManager rgm = ProtectionStones.getRegionManagerWithPlayer(p);
        if (!p.hasPermission("protectionstones.unclaim")) {
            p.sendMessage(PSL.NO_PERMISSION_UNCLAIM.msg());
            return true;
        }
        if (psID.equals("")) {
            p.sendMessage(PSL.NOT_IN_REGION.msg());
            return true;
        }
        ProtectedRegion region = rgm.getRegion(psID);

        if (region == null) {
            p.sendMessage(PSL.NOT_IN_REGION.msg());
            return true;
        }
        if (!psID.substring(0, 2).equals("ps")) {
            p.sendMessage(PSL.NOT_IN_REGION.msg());
            return true;
        }

        if (!region.isOwner(wg.wrapPlayer(p)) && !p.hasPermission("protectionstones.superowner")) {
            p.sendMessage(PSL.NO_REGION_PERMISSION.msg());
            return true;
        }

        // check if block is hidden first
        PSLocation psl = ProtectionStones.parsePSRegionToLocation(psID);
        Block blockToUnhide = p.getWorld().getBlockAt(psl.x, psl.y, psl.z);

        String type = blockToUnhide.getType().toString();

        // Retrieve stored block data if air from file and delete from file
        if (blockToUnhide.getType() == Material.AIR) {
            YamlConfiguration hideFile = YamlConfiguration.loadConfiguration(ProtectionStones.psStoneData);
            String entry = (int) blockToUnhide.getLocation().getX() + "x";
            entry = entry + (int) blockToUnhide.getLocation().getY() + "y";
            entry = entry + (int) blockToUnhide.getLocation().getZ() + "z";

            type = hideFile.getString(entry);
            if (type == null) {
                p.sendMessage(PSL.UNCLAIM_CANT_FIND.msg());
                return true;
            }

            blockToUnhide.setType(Material.getMaterial(type));
            hideFile.set(entry, null);
            try {
                hideFile.save(ProtectionStones.psStoneData);
            } catch (IOException ex) {
                Logger.getLogger(ProtectionStones.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // Return and remove protection stone
        if (!ProtectionStones.isProtectBlock(type)) {
            p.sendMessage(PSL.UNCLAIM_CANT_FIND.msg());
            return true;
        }

        if (!ProtectionStones.getBlockOptions(type).noDrop) {

            // return protection stone
            if (!p.getInventory().addItem(new ItemStack(blockToUnhide.getType())).isEmpty()) {
                // method will return not empty if item couldn't be added
                p.sendMessage(PSL.NO_ROOM_IN_INVENTORY.msg());
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
        p.sendMessage(PSL.NO_LONGER_PROTECTED.msg());

        return true;
    }
}
