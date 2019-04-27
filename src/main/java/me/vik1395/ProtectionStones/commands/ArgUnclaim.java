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
import me.vik1395.ProtectionStones.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class ArgUnclaim {
    // /ps unclaim
    public static boolean argumentUnclaim(Player p, String[] args) { // psID: id of the current region the player is in
        String psID = ProtectionStones.playerToPSID(p);

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

        String type = region.getFlag(FlagHandler.PS_BLOCK_MATERIAL);
        ConfigProtectBlock cpb = ProtectionStones.getBlockOptions(type);

        if (cpb == null || !cpb.noDrop) {
            // return protection stone
            if (!p.getInventory().addItem(ProtectionStones.createProtectBlockItem(cpb)).isEmpty()) {
                // method will return not empty if item couldn't be added
                p.sendMessage(PSL.NO_ROOM_IN_INVENTORY.msg());
                return true;
            }
        }

        // remove block if ps is unhidden
        if (region.getFlag(FlagHandler.PS_BLOCK_MATERIAL).equalsIgnoreCase(blockToUnhide.getType().toString())) {
            blockToUnhide.setType(Material.AIR);
        }

        // remove region
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
