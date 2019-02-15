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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ArgView {
    public static boolean argumentView(Player p, String[] args, String psID) {
        WorldGuardPlugin wg = (WorldGuardPlugin) ProtectionStones.wgd;
        RegionManager rgm = ProtectionStones.getRegionManagerWithPlayer(p);

        if (!p.hasPermission("protectionstones.view") && !p.hasPermission("protectionstones.view.others")) {
            p.sendMessage(ChatColor.RED + "You don't have permission to use that command");
            return true;
        }
        if (ProtectionStones.hasNoAccess(rgm.getRegion(psID), p, wg.wrapPlayer(p), true)) {
            p.sendMessage(ChatColor.RED + "You are not allowed to do that here.");
            return true;
        }
        p.sendMessage(ChatColor.YELLOW + "Generating border...");

        BlockVector3 minVector = rgm.getRegion(psID).getMinimumPoint();
        BlockVector3 maxVector = rgm.getRegion(psID).getMaximumPoint();
        final int minX = minVector.getBlockX();
        final int minY = minVector.getBlockY();
        final int minZ = minVector.getBlockZ();
        final int maxX = maxVector.getBlockX();
        final int maxY = maxVector.getBlockY();
        final int maxZ = maxVector.getBlockZ();

        int playerY = p.getLocation().getBlockY(), playerX = p.getLocation().getBlockX(), playerZ = p.getLocation().getBlockZ();

        BlockData tempBlock = Material.GLOWSTONE.createBlockData();
        int[] xs = {Math.min(minX, maxX), Math.max(minX, maxX)}, ys = {playerY, Math.min(minY, maxY), Math.max(minY, maxY)}, zs = {Math.min(minZ, maxZ), Math.max(minZ, maxZ)};

        // send fake blocks to client

        Bukkit.getScheduler().runTaskAsynchronously(ProtectionStones.getPlugin(), () -> {

            List<Block> blocks = new ArrayList<>();
            // base lines
            for (int x : xs)
                for (int y : ys)
                    for (int z : zs) {
                        handleFakeBlock(p, x, y, z, tempBlock, blocks, 0, 0);
                    }

            int wait = 0;
            // x lines
            for (int x = Math.max(xs[0], playerX - 40); x <= Math.min(xs[1], playerX + 40); x += 7) { // max radius of 40
                for (int z : zs) {
                    for (int y : ys) {
                        wait++;
                        handleFakeBlock(p, x, y, z, tempBlock, blocks, 1, wait/2);
                    }
                }
            }

            // z lines
            for (int z = Math.max(zs[0], playerZ - 40); z <= Math.min(zs[1], playerZ + 40); z += 7) { // max radius of 40
                for (int x : xs) {
                    for (int y : ys) {
                        wait++;
                        handleFakeBlock(p, x, y, z, tempBlock, blocks, 1, wait/2);
                    }
                }
            }

            // y lines last
            for (int y = Math.max(ys[1], playerY - 40); y <= Math.min(ys[2], playerY + 40); y += 10) {
                for (int x : xs) {
                    for (int z : zs) {
                        wait++;
                        handleFakeBlock(p, x, y, z, tempBlock, blocks, 1, wait);
                    }
                }
            }

            Bukkit.getScheduler().runTaskLater(ProtectionStones.getPlugin(), () -> {
                p.sendMessage(ChatColor.YELLOW + "Done! The border will disappear after 30 seconds!");
            }, wait);

            Bukkit.getScheduler().runTaskLaterAsynchronously(ProtectionStones.getPlugin(), () -> {
                for (Block b : blocks) {
                    p.sendBlockChange(b.getLocation(), b.getBlockData());
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, 600L); // remove after 10 seconds
        });
        return true;
    }

    public static void handleFakeBlock(Player p, int x, int y, int z, BlockData tempBlock, List<Block> restore, long delay, long multiplier) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(ProtectionStones.getPlugin(), () -> {
            restore.add(p.getWorld().getBlockAt(x, y, z));
            p.sendBlockChange(p.getWorld().getBlockAt(x, y, z).getLocation(), tempBlock);
        }, delay*multiplier);
    }
}
