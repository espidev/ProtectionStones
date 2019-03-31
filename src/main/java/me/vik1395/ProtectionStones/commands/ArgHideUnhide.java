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
import me.vik1395.ProtectionStones.PSLocation;
import me.vik1395.ProtectionStones.ProtectionStones;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArgHideUnhide {
    public static boolean template(Player p, String arg, String psID) {
        WorldGuardPlugin wg = (WorldGuardPlugin) ProtectionStones.wgd;
        RegionManager rgm = ProtectionStones.getRegionManagerWithPlayer(p);

        // preliminary checks
        if (arg.equals("unhide") && !p.hasPermission("protectionstones.unhide")) {
            p.sendMessage(ChatColor.RED + "You don't have permission to use that command");
            return true;
        }
        if (arg.equals("hide") && !p.hasPermission("protectionstones.hide")) {
            p.sendMessage(ChatColor.RED + "You don't have permission to use that command");
            return true;
        }
        if (ProtectionStones.hasNoAccess(rgm.getRegion(psID), p, wg.wrapPlayer(p), false)) {
            p.sendMessage(ChatColor.RED + "You are not allowed to do that here.");
            return true;
        }
        if (!psID.substring(0, 2).equals("ps")) {
            p.sendMessage(ChatColor.YELLOW + "Not a ProtectionStones Region");
            return true;
        }
        PSLocation psl = ProtectionStones.parsePSRegionToLocation(psID);
        Block blockToEdit = p.getWorld().getBlockAt(psl.x, psl.y, psl.z);

        YamlConfiguration hideFile = YamlConfiguration.loadConfiguration(ProtectionStones.psStoneData);
        String entry = psl.x + "x" + psl.y + "y" + psl.z + "z";
        String setmat = hideFile.getString(entry);
        Material currentType = blockToEdit.getType();

        if (ProtectionStones.mats.contains(currentType.toString())) {
            if (arg.equals("unhide")) {
                p.sendMessage(ChatColor.YELLOW + "This PStone doesn't appear hidden...");
                return true;
            }
            if (!hideFile.contains(entry)) {
                hideFile.set(entry, currentType.toString());
                try {
                    hideFile.save(ProtectionStones.psStoneData);
                } catch (IOException ex) {
                    Logger.getLogger(ProtectionStones.class.getName()).log(Level.SEVERE, null, ex);
                }
                blockToEdit.setType(Material.AIR);
            } else {
                p.sendMessage(ChatColor.YELLOW + "This PStone appears to already be hidden...");
            }
        } else {
            if (arg.equals("hide")) {
                p.sendMessage(ChatColor.YELLOW + "This PStone appears to already be hidden...");
                return true;
            }

            if (hideFile.contains(entry)) {
                hideFile.set(entry, null);
                try {
                    hideFile.save(ProtectionStones.psStoneData);
                } catch (IOException ex) {
                    Logger.getLogger(ProtectionStones.class.getName()).log(Level.SEVERE, null, ex);
                }
                blockToEdit.setType(Material.getMaterial(setmat));
            } else {
                p.sendMessage(ChatColor.YELLOW + "This PStone doesn't appear hidden...");
            }
        }
        return true;
    }
}
