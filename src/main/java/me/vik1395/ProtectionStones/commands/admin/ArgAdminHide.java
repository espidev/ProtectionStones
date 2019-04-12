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

package me.vik1395.ProtectionStones.commands.admin;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.vik1395.ProtectionStones.PSL;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArgAdminHide {

    // /ps admin hide
    public static boolean argumentAdminHide(Player p, String[] args) {
        RegionManager mgr = ProtectionStones.getRegionManagerWithPlayer(p);
        List<String> regionIDList = new ArrayList<>();

        // add all protection stone regions to regions map
        for (String idname : mgr.getRegions().keySet()) {
            if (idname.length() >= 9 && idname.substring(0, 2).equals("ps")) {
                regionIDList.add(idname);
            }
        }

        YamlConfiguration hideFile = YamlConfiguration.loadConfiguration(ProtectionStones.psStoneData);

        // loop through regions that are protection stones and hide or unhide the block
        for (String regionID : regionIDList) {
            PSLocation psl = ProtectionStones.parsePSRegionToLocation(regionID);
            Block blockToChange = p.getWorld().getBlockAt(psl.x, psl.y, psl.z);
            String entry = psl.x + "x" + psl.y + "y" + psl.z + "z";

            if (args[1].equalsIgnoreCase("unhide")) {
                String blockMaterial = hideFile.getString(entry);

                if (hideFile.contains(entry)) {
                    hideFile.set(entry, null);
                    if (blockMaterial != null) {
                        blockToChange.setType(Material.getMaterial(blockMaterial));
                    }
                }

            } else if (args[1].equalsIgnoreCase("hide")) {
                if (ProtectionStones.protectBlocks.contains(blockToChange.getType().toString()) && !hideFile.contains(entry)) {
                    hideFile.set(entry, blockToChange.getType().toString());
                    blockToChange.setType(Material.AIR);
                }
            }

        }

        try {
            hideFile.save(ProtectionStones.psStoneData);
        } catch (IOException ex) {
            Logger.getLogger(ProtectionStones.class.getName()).log(Level.SEVERE, null, ex);
        }

        String hMessage = args[1].equalsIgnoreCase("unhide") ? "unhidden" : "hidden";
        p.sendMessage(PSL.ADMIN_HIDE_TOGGLED.msg()
                .replace("%message%", hMessage));

        return true;
    }
}
