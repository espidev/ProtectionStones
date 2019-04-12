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

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import me.vik1395.ProtectionStones.PSL;
import me.vik1395.ProtectionStones.ProtectionStones;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ArgTp {

    // /ps tp
    public static boolean argumentTp(Player p, String[] args) {
        WorldGuardPlugin wg = (WorldGuardPlugin) ProtectionStones.wgd;
        RegionManager rgm = ProtectionStones.getRegionManagerWithPlayer(p);

        int index = 0, rgnum; // index: index in playerRegions for selected region, rgnum: index specified by player to teleport to
        Map<Integer, String> playerRegions = new HashMap<>();

        // preliminary checks
        if (args[0].equalsIgnoreCase("tp")) { // argument tp
            if (!p.hasPermission("protectionstones.tp")) {
                p.sendMessage(PSL.NO_PERMISSION_TP.msg());
                return true;
            } else if (args.length != 3) {
                p.sendMessage(PSL.TP_HELP.msg());
                return true;
            }
            rgnum = Integer.parseInt(args[2]);
        } else { // argument home
            if (!p.hasPermission("protectionstones.home")) {
                p.sendMessage(PSL.NO_PERMISSION_HOME.msg());
                return true;
            } else if (args.length != 2) {
                p.sendMessage(PSL.HOME_HELP.msg());
                return true;
            }
            rgnum = Integer.parseInt(args[1]);
        }

        if (rgnum <= 0) {
            p.sendMessage(PSL.NUMBER_ABOVE_ZERO.msg());
            return true;
        }

        // region checks
        if (args[0].equalsIgnoreCase("tp")) {
            LocalPlayer lp;
            try {
                lp = wg.wrapOfflinePlayer(Bukkit.getOfflinePlayer(args[1]));
            } catch (Exception e) {
                p.sendMessage(PSL.REGION_ERROR_SEARCH.msg()
                        .replace("%player%", args[1]));
                return true;
            }

            // find regions that the player has
            for (String region : rgm.getRegions().keySet()) {
                if (region.startsWith("ps")) {
                    if (rgm.getRegions().get(region).getOwners().contains(lp)) {
                        index++;
                        playerRegions.put(index, region);
                    }
                }
            }

            if (index <= 0) {
                p.sendMessage(PSL.REGION_NOT_FOUND_FOR_PLAYER.msg()
                        .replace("%player%", lp.getName()));
                return true;
            } else if (rgnum > index) {
                p.sendMessage(PSL.ONLY_HAS_REGIONS.msg()
                        .replace("%player%", lp.getName())
                        .replace("%num%", "" + index));
                return true;
            }
        } else if (args[0].equalsIgnoreCase("home")) {
            // find regions that the player has
            for (String region : rgm.getRegions().keySet()) {
                if (region.startsWith("ps")) {
                    if (rgm.getRegions().get(region).getOwners().contains(wg.wrapPlayer(p))) {
                        index++;
                        playerRegions.put(index, region);
                    }
                }
            }

            if (index <= 0) {
                p.sendMessage(PSL.NO_REGIONS_OWNED.msg());
            }
            if (rgnum > index) {
                p.sendMessage(PSL.HOME_ONLY.msg().replace("%num%", "" + index));
                return true;
            }
        }

        // teleport player
        if (rgnum <= index) {
            String region = rgm.getRegion(playerRegions.get(rgnum)).getId();
            String[] pos = region.split("x|y|z");
            if (pos.length == 3) {
                pos[0] = pos[0].substring(2);
                p.sendMessage(PSL.TPING.msg());
                Location tploc = new Location(p.getWorld(), Integer.parseInt(pos[0]), Integer.parseInt(pos[1]), Integer.parseInt(pos[2]));
                p.teleport(tploc);
            } else {
                p.sendMessage(PSL.TP_ERROR_NAME.msg());
            }
        } else {
            p.sendMessage(PSL.TP_ERROR_TP.msg());
        }

        return true;
    }
}
