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

package dev.espi.ProtectionStones.commands;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.ProtectionStones.*;
import dev.espi.ProtectionStones.utils.WGUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
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
    public boolean executeArgument(CommandSender s, String[] args) {
        Player p = (Player) s;
        String psID = WGUtils.playerToPSID(p); // id of the current region the player is in

        WorldGuardPlugin wg = WorldGuardPlugin.inst();
        RegionManager rgm = WGUtils.getRegionManagerWithPlayer(p);
        if (!p.hasPermission("protectionstones.unclaim")) {
            PSL.msg(p, PSL.NO_PERMISSION_UNCLAIM.msg());
            return true;
        }
        if (psID.equals("")) {
            PSL.msg(p, PSL.NOT_IN_REGION.msg());
            return true;
        }
        ProtectedRegion region = rgm.getRegion(psID);

        if (region == null) {
            PSL.msg(p, PSL.NOT_IN_REGION.msg());
            return true;
        }
        if (!psID.substring(0, 2).equals("ps")) {
            PSL.msg(p, PSL.NOT_IN_REGION.msg());
            return true;
        }

        if (!region.isOwner(wg.wrapPlayer(p)) && !p.hasPermission("protectionstones.superowner")) {
            PSL.msg(p, PSL.NO_REGION_PERMISSION.msg());
            return true;
        }

        // check if block is hidden first
        PSLocation psl = WGUtils.parsePSRegionToLocation(psID);
        Block blockToUnhide = p.getWorld().getBlockAt(psl.x, psl.y, psl.z);

        String type = region.getFlag(FlagHandler.PS_BLOCK_MATERIAL);
        PSProtectBlock cpb = ProtectionStones.getBlockOptions(type);

        if (cpb == null || !cpb.noDrop) {
            // return protection stone
            if (!p.getInventory().addItem(ProtectionStones.createProtectBlockItem(cpb)).isEmpty()) {
                // method will return not empty if item couldn't be added
                PSL.msg(p, PSL.NO_ROOM_IN_INVENTORY.msg());
                return true;
            }
        }

        // remove block if ps is unhidden
        if (region.getFlag(FlagHandler.PS_BLOCK_MATERIAL).equalsIgnoreCase(blockToUnhide.getType().toString())) {
            blockToUnhide.setType(Material.AIR);
        }

        // remove region
        rgm.removeRegion(psID);
        PSL.msg(p, PSL.NO_LONGER_PROTECTED.msg());

        return true;
    }
}
