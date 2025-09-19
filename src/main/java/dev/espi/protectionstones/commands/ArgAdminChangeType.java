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

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.FlagHandler;
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.utils.WGUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.HashSet;
import java.util.Set;

class ArgAdminChangeType {

    // /ps admin changeregiontype [world] [fromblocktype] [toblocktype]
    static boolean argumentAdminChangeType(CommandSender p, String[] args) {
        if (args.length < 5) {
            return PSL.msg(p, ArgAdmin.getChangeRegionTypeHelp());
        }

        World w = Bukkit.getWorld(args[2]);
        if (w == null) {
            return PSL.msg(p, PSL.INVALID_WORLD.msg());
        }

        RegionManager rgm = WGUtils.getRegionManagerWithWorld(w);
        if (rgm == null) {
            return PSL.msg(p, MiniMessage.miniMessage().deserialize(
                    "<gray>The world does not have WorldGuard configured!"
            ));
        }

        String fromType = args[3], toType = args[4];

        // loop through and update flags manually (do not rely on PSRegion API, since this can include invalid regions)
        for (ProtectedRegion r : rgm.getRegions().values()) {
            // update block material
            if (r.getFlag(FlagHandler.PS_BLOCK_MATERIAL) != null && r.getFlag(FlagHandler.PS_BLOCK_MATERIAL).equals(fromType)) {
                r.setFlag(FlagHandler.PS_BLOCK_MATERIAL, toType);
                p.sendMessage(ChatColor.GRAY + "Updated region " + r.getId());
            }
            // update merged regions
            if (r.getFlag(FlagHandler.PS_MERGED_REGIONS_TYPES) != null) {
                Set<String> s = new HashSet<>();
                for (String entry : r.getFlag(FlagHandler.PS_MERGED_REGIONS_TYPES)) {
                    String[] spl = entry.split(" ");

                    if (spl.length == 2) {
                        if (spl[1].equals(fromType)) { // if it is of the type to change
                            p.sendMessage(ChatColor.GRAY + "Updated merged region " + spl[0]);
                            s.add(spl[0] + " " + toType);
                        } else {
                            s.add(entry);
                        }
                    }

                }

                r.setFlag(FlagHandler.PS_MERGED_REGIONS_TYPES, s);
            }
        }

        p.sendMessage(ChatColor.GREEN + "Finished!");
        p.sendMessage(ChatColor.GRAY + "You should restart the server, and make sure the new block type is configured in the config.");
        return true;
    }

}
