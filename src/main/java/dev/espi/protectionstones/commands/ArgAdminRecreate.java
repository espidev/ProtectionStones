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
import dev.espi.protectionstones.PSProtectBlock;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ArgAdminRecreate {
    static boolean argumentAdminRecreate(CommandSender s, String[] args) {
        s.sendMessage(ChatColor.YELLOW + "Recreating...");

        for (World w : Bukkit.getWorlds()) {
            RegionManager rgm = WGUtils.getRegionManagerWithWorld(w);

            List<ProtectedRegion> toAdd = new ArrayList<>();

            for (ProtectedRegion r : rgm.getRegions().values()) {
                if (ProtectionStones.isPSRegion(r)) {
                    PSRegion wr = PSRegion.fromWGRegion(w, r);

                    double bx = wr.getProtectBlock().getLocation().getX();
                    double by = wr.getProtectBlock().getLocation().getY();
                    double bz = wr.getProtectBlock().getLocation().getZ();
                    BlockVector3 min, max;
                    PSProtectBlock blockOptions = wr.getTypeOptions();

                    if (blockOptions.yRadius == -1) {
                        min = BlockVector3.at(bx - blockOptions.xRadius, 0, bz - blockOptions.zRadius);
                        max = BlockVector3.at(bx + blockOptions.xRadius, w.getMaxHeight(), bz + blockOptions.zRadius);
                    } else {
                        min = BlockVector3.at(bx - blockOptions.xRadius, by - blockOptions.yRadius, bz - blockOptions.zRadius);
                        max = BlockVector3.at(bx + blockOptions.xRadius, by + blockOptions.yRadius, bz + blockOptions.zRadius);
                    }

                    ProtectedRegion nr = new ProtectedCuboidRegion(r.getId(), min, max);
                    nr.setMembers(r.getMembers());
                    nr.setOwners(r.getOwners());
                    nr.setFlags(r.getFlags());
                    nr.setPriority(r.getPriority());
                    toAdd.add(nr);
                }
            }

            for (ProtectedRegion r : toAdd) {
                rgm.removeRegion(r.getId());
                rgm.addRegion(r);
            }
        }

        s.sendMessage(ChatColor.YELLOW + "Done...");
        return true;
    }
}
