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

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import me.vik1395.ProtectionStones.ProtectionStones;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

public class ArgView {
    public static boolean argumentView(Player p, String[] args, String psID) {
        WorldGuardPlugin wg = (WorldGuardPlugin) ProtectionStones.wgd;
        RegionManager rgm = ProtectionStones.getRegionManagerWithPlayer(p);

        if (!p.hasPermission("protectionstones.view")) {
            p.sendMessage(ChatColor.RED + "You don't have permission to use that command");
            return true;
        }
        if (ProtectionStones.hasNoAccess(rgm.getRegion(psID), p, wg.wrapPlayer(p), true)) {
            p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You are not allowed to do that here.").toString());
            return true;
        }
        BlockVector3 minVector = rgm.getRegion(psID).getMinimumPoint();
        BlockVector3 maxVector = rgm.getRegion(psID).getMaximumPoint();
        final int minX = minVector.getBlockX();
        final int minY = minVector.getBlockY();
        final int minZ = minVector.getBlockZ();
        final int maxX = maxVector.getBlockX();
        final int maxY = maxVector.getBlockY();
        final int maxZ = maxVector.getBlockZ();
        double px = p.getLocation().getX();
        double py = p.getLocation().getY();
        double pz = p.getLocation().getZ();
        BlockVector3 playerVector = BlockVector3.at(px, py, pz);
        final int playerY = playerVector.getBlockY();

        BlockData tempBlock = Material.GLOWSTONE.createBlockData();
        int[] xs = {minX, maxX}, ys = {playerY, minY, maxY}, zs = {minZ, maxZ};

        // send fake blocks to client
        for (int x : xs) {
            for (int y : ys) {
                for (int z : zs) {
                    p.sendBlockChange(p.getWorld().getBlockAt(x, y, z).getLocation(), tempBlock);
                }
            }
        }
        return true;
    }
}
