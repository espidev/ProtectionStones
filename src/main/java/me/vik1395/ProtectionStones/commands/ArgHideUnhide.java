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
import me.vik1395.ProtectionStones.FlagHandler;
import me.vik1395.ProtectionStones.PSL;
import me.vik1395.ProtectionStones.PSLocation;
import me.vik1395.ProtectionStones.ProtectionStones;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class ArgHideUnhide {
    public static boolean template(Player p, String arg) {
        String psID = ProtectionStones.playerToPSID(p);

        WorldGuardPlugin wg = (WorldGuardPlugin) ProtectionStones.wgd;
        RegionManager rgm = ProtectionStones.getRegionManagerWithPlayer(p);
        ProtectedRegion r = rgm.getRegion(psID);

        // preliminary checks
        if (arg.equals("unhide") && !p.hasPermission("protectionstones.unhide")) {
            PSL.msg(p, PSL.NO_PERMISSION_UNHIDE.msg());
            return true;
        }
        if (arg.equals("hide") && !p.hasPermission("protectionstones.hide")) {
            PSL.msg(p, PSL.NO_PERMISSION_HIDE.msg());
            return true;
        }
        if (ProtectionStones.hasNoAccess(r, p, wg.wrapPlayer(p), false)) {
            PSL.msg(p, PSL.NO_ACCESS.msg());
            return true;
        }
        if (!psID.startsWith("ps")) {
            PSL.msg(p, PSL.NOT_PS_REGION.msg());
            return true;
        }

        PSLocation psl = ProtectionStones.parsePSRegionToLocation(psID);
        Block blockToEdit = p.getWorld().getBlockAt(psl.x, psl.y, psl.z);

        Material currentType = blockToEdit.getType();

        if (ProtectionStones.isProtectBlock(currentType.toString())) {
            if (arg.equals("unhide")) {
                PSL.msg(p, PSL.ALREADY_NOT_HIDDEN.msg());
                return true;
            }
            blockToEdit.setType(Material.AIR);
        } else {
            if (arg.equals("hide")) {
                PSL.msg(p, PSL.ALREADY_HIDDEN.msg());
                return true;
            }
            blockToEdit.setType(Material.getMaterial(r.getFlag(FlagHandler.PS_BLOCK_MATERIAL)));
        }
        return true;
    }
}
