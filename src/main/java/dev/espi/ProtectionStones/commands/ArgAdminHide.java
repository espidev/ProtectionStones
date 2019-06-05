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

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import dev.espi.ProtectionStones.FlagHandler;
import dev.espi.ProtectionStones.PSL;
import dev.espi.ProtectionStones.PSLocation;
import dev.espi.ProtectionStones.ProtectionStones;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ArgAdminHide implements PSCommandArg {

    // /ps admin hide
    @Override
    public boolean executeArgument(CommandSender p, String[] args) {
        RegionManager mgr;
        World w;
        if (p instanceof Player) {
            mgr = ProtectionStones.getRegionManagerWithPlayer((Player) p);
            w = ((Player) p).getWorld();
        } else {
            if (args.length != 3) {
                PSL.msg(p, PSL.ADMIN_CONSOLE_WORLD.msg());
                return true;
            }
            if (Bukkit.getWorld(args[2]) == null) {
                PSL.msg(p, PSL.INVALID_WORLD.msg());
                return true;
            }
            w = Bukkit.getWorld(args[2]);
            mgr = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(w));
        }
        List<String> regionIDList = new ArrayList<>();

        Bukkit.getScheduler().runTaskAsynchronously(ProtectionStones.getPlugin(), () -> {
            // add all protection stone regions to regions map
            for (String idname : mgr.getRegions().keySet()) {
                if (idname.length() >= 9 && idname.substring(0, 2).equals("ps")) {
                    regionIDList.add(idname);
                }
            }

            // loop through regions that are protection stones and hide or unhide the block
            for (String regionID : regionIDList) {
                PSLocation psl = ProtectionStones.parsePSRegionToLocation(regionID);
                Block blockToChange = w.getBlockAt(psl.x, psl.y, psl.z);

                if (args[1].equalsIgnoreCase("unhide")) {
                    Bukkit.getScheduler().runTask(ProtectionStones.getPlugin(), () -> blockToChange.setType(Material.getMaterial(mgr.getRegion(regionID).getFlag(FlagHandler.PS_BLOCK_MATERIAL))));
                } else if (args[1].equalsIgnoreCase("hide")) {
                    if (ProtectionStones.isProtectBlock(blockToChange.getType().toString())) {
                        Bukkit.getScheduler().runTask(ProtectionStones.getPlugin(), () -> blockToChange.setType(Material.AIR));
                    }
                }

            }

            String hMessage = args[1].equalsIgnoreCase("unhide") ? "unhidden" : "hidden";
            PSL.msg(p, PSL.ADMIN_HIDE_TOGGLED.msg()
                    .replace("%message%", hMessage));
        });

        return true;
    }
}
