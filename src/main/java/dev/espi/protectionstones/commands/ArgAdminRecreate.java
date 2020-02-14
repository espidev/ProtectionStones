/*
 * Copyright 2019 ProtectionStones team and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package dev.espi.protectionstones.commands;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.*;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class ArgAdminRecreate {
    static boolean argumentAdminRecreate(CommandSender s, String[] args) {
        s.sendMessage(ChatColor.YELLOW + "Recreating...");

        HashMap<World, RegionManager> m = WGUtils.getAllRegionManagers();
        for (World w : m.keySet()) {
            RegionManager rgm = m.get(w);

            List<ProtectedRegion> toAdd = new ArrayList<>();

            for (ProtectedRegion r : rgm.getRegions().values()) {
                if (ProtectionStones.isPSRegion(r)) {
                    PSRegion wr = PSRegion.fromWGRegion(w, r);
                    if (wr instanceof PSGroupRegion) continue; // skip group regions for now TODO
                    PSProtectBlock blockOptions = wr.getTypeOptions();

                    double bx = wr.getProtectBlock().getLocation().getX(), bxo = blockOptions.xOffset;
                    double by = wr.getProtectBlock().getLocation().getY(), bxy = blockOptions.yOffset;
                    double bz = wr.getProtectBlock().getLocation().getZ(), bxz = blockOptions.zOffset;
                    BlockVector3 min = WGUtils.getMinVector(bx + bxo, by + bxy, bz + bxz, blockOptions.xRadius, blockOptions.yRadius, blockOptions.zRadius);
                    BlockVector3 max = WGUtils.getMaxVector(bx + bxo, by + bxy, bz + bxz, blockOptions.xRadius, blockOptions.yRadius, blockOptions.zRadius);

                    ProtectedRegion nr = new ProtectedCuboidRegion(r.getId(), min, max);
                    WGUtils.copyRegionValues(r, nr);
                    toAdd.add(nr);
                }
            }

            for (ProtectedRegion r : toAdd) {
                rgm.removeRegion(r.getId());
                rgm.addRegion(r);
            }
        }

        s.sendMessage(ChatColor.YELLOW + "Done.");
        return true;
    }
}
