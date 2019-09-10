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

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.*;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Skull;
import org.bukkit.command.CommandSender;

import java.util.function.Consumer;

public class ArgAdminChangeblock {

    // /ps admin changeblock [fromblock] [toblock]
    static boolean argumentAdminChangeblock(CommandSender p, String[] args) {
        if (args.length < 4) {
            PSL.msg(p, PSL.ADMIN_CHANGEBLOCK_HELP.msg());
            return true;
        }

        String fromBlock = args[2], toBlock = args[3];
        if (Material.matchMaterial(toBlock) == null && !toBlock.startsWith("PLAYER_HEAD")) {
            PSL.msg(p, ChatColor.GRAY + "The block to change to is not valid!");
            return true;
        }
        if (toBlock.startsWith("PLAYER_HEAD") && toBlock.split(":").length != 2) {
            PSL.msg(p, ChatColor.GRAY + "The block to change to is not valid!");
            return true;
        }

        Material set = Material.matchMaterial(toBlock) == null ? Material.PLAYER_HEAD : Material.matchMaterial(toBlock);

        Consumer<PSRegion> convertFunction = (region) -> {
            if (region.getType().equals(fromBlock)) {
                p.sendMessage(ChatColor.GRAY + "Changing " + region.getID() + "...");

                region.getWGRegion().setFlag(FlagHandler.PS_BLOCK_MATERIAL, toBlock);
                if (!region.isHidden()) {
                    region.getProtectBlock().setType(set);
                    if (toBlock.startsWith("PLAYER_HEAD")) {
                        Skull s = (Skull) region.getProtectBlock().getState();
                        s.setOwningPlayer(Bukkit.getOfflinePlayer(toBlock.split(":")[1]));
                    }
                }
            }
        };

        for (World w : Bukkit.getWorlds()) {
            for (ProtectedRegion r : WGUtils.getRegionManagerWithWorld(w).getRegions().values()) {
                if (ProtectionStones.isPSRegion(r)) {
                    PSRegion pr = PSRegion.fromWGRegion(w, r);

                    convertFunction.accept(pr);

                    if (pr instanceof PSGroupRegion) {
                        for (PSMergedRegion psmr : ((PSGroupRegion) pr).getMergedRegions()) {
                            convertFunction.accept(psmr);
                        }
                    }
                }
            }
        }

        return true;
    }
}
