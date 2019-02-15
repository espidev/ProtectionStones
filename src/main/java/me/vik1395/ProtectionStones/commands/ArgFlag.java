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
import me.vik1395.ProtectionStones.FlagHandler;
import me.vik1395.ProtectionStones.ProtectionStones;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ArgFlag {
    public static boolean argumentFlag(Player p, String[] args, String psID) {
        WorldGuardPlugin wg = (WorldGuardPlugin) ProtectionStones.wgd;
        RegionManager rgm = ProtectionStones.getRegionManagerWithPlayer(p);

        if (!p.hasPermission("protectionstones.flags")) {
            p.sendMessage(ChatColor.RED + "You don't have permission to use flag commands");
            return true;
        }
        if (ProtectionStones.hasNoAccess(rgm.getRegion(psID), p, wg.wrapPlayer(p), false)) {
            p.sendMessage(ChatColor.RED + "You are not allowed to do that here.");
            return true;
        }

        if (args.length < 3) {
            p.sendMessage(ChatColor.RED + "Use:  /ps flag {flagname} {flagvalue}");
        } else {
            if (ProtectionStones.allowedFlags.contains(args[1].toLowerCase()) || p.hasPermission("protectionstones.flag." + args[1].toLowerCase()) || p.hasPermission("protectionstones.flag.*")) {
                FlagHandler fh = new FlagHandler();
                fh.setFlag(args, rgm.getRegion(psID), p);
            } else {
                p.sendMessage(ChatColor.RED + "You don't have permission to set that flag");
            }
        }
        return true;
    }
}
