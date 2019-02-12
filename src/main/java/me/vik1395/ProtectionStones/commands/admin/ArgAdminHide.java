/*
 * Copyright 2019
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

package me.vik1395.ProtectionStones.commands.admin;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.vik1395.ProtectionStones.PSLocation;
import me.vik1395.ProtectionStones.ProtectionStones;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArgAdminHide {

    // /ps admin hide
    public static boolean argumentAdminHide(Player p, String[] args) {
        RegionManager mgr = ProtectionStones.getRegionManagerWithPlayer(p);
        Map<String, ProtectedRegion> regions = mgr.getRegions();
        if (regions.isEmpty()) {
            p.sendMessage(ChatColor.YELLOW + "No ProtectionStones Regions Found");
        }
        List<String> regionIDList = new ArrayList<>();
        String blockMaterial = "AIR";
        String hMessage = "hidden";

        // add all protection stone regions to regions map
        for (String idname : regions.keySet()) {
            if (idname.length() >= 9 && idname.substring(0, 2).equals("ps")) {
                regionIDList.add(idname);
            }
        }


        if (regionIDList.isEmpty()) {
            p.sendMessage(ChatColor.YELLOW + "No ProtectionStones Regions Found");
            return true;
        }
        for (String regionID : regionIDList) {
            PSLocation psl = ProtectionStones.parsePSRegionToLocation(regionID);
            Block blockToChange = p.getWorld().getBlockAt(psl.x, psl.y, psl.z);
            String entry = (int) blockToChange.getLocation().getX() + "x" + (int) blockToChange.getLocation().getY() + "y" + (int) blockToChange.getLocation().getZ() + "z";
            String subtype = null;
            if (args[1].equalsIgnoreCase("unhide")) {
                YamlConfiguration hideFile = YamlConfiguration.loadConfiguration(ProtectionStones.psStoneData);
                blockMaterial = hideFile.getString(entry);
                if (blockMaterial != null && blockMaterial.contains("-")) {
                    String[] str = blockMaterial.split("-");
                    blockMaterial = str[0];
                    subtype = str[1];
                }
                if (hideFile.contains(entry)) {
                    hideFile.set(entry, null);
                    try {
                        hideFile.save(ProtectionStones.psStoneData);
                    } catch (IOException ex) {
                        Logger.getLogger(ProtectionStones.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (blockMaterial != null) {
                        blockToChange.setType(Material.getMaterial(blockMaterial));
                    }
                } else {
                    p.sendMessage(ChatColor.YELLOW + "This PStone doesn't appear hidden...");
                }
                //}
            } else if (args[1].equalsIgnoreCase("hide")) {
                if (blockToChange.getType() != Material.getMaterial(blockMaterial)) {
                    YamlConfiguration hideFile = YamlConfiguration.loadConfiguration(ProtectionStones.psStoneData);
                    if (!(hideFile.contains(entry))) {
                        hideFile.set(entry, blockToChange.getType().toString());
                        try {
                            hideFile.save(ProtectionStones.psStoneData);
                        } catch (IOException ex) {
                            Logger.getLogger(ProtectionStones.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        p.sendMessage(ChatColor.YELLOW + "This PStone appears to already be hidden...");
                    }
                } else {
                    if (subtype != null && (blockToChange.getData() != (byte) (Integer.parseInt(subtype))))
                        ;
                }
                if (ProtectionStones.mats.contains(blockToChange.getType().toString()) || ProtectionStones.mats.contains(blockToChange.getType().toString() + "-" + blockToChange.getData())) {
                    blockToChange.setType(Material.getMaterial(blockMaterial));
                }
            }
            if (subtype != null) {
                //TODO removed subtype support blockToChange.setData((byte) Integer.parseInt(subtype));
            }

        }

        if (args[1].equalsIgnoreCase("unhide")) {
            hMessage = "unhidden";
        }
        p.sendMessage(ChatColor.YELLOW + "All ProtectionStones have been " + hMessage);
        return true;
    }
}
