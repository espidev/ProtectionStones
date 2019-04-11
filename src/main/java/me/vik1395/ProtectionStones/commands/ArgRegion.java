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
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.vik1395.ProtectionStones.PSL;
import me.vik1395.ProtectionStones.ProtectionStones;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArgRegion {

    // /ps region
    public static boolean argumentRegion(Player p, String[] args) {
        WorldGuardPlugin wg = (WorldGuardPlugin) ProtectionStones.wgd;
        RegionManager rgm = ProtectionStones.getRegionManagerWithPlayer(p);

        if (!p.hasPermission("protectionstones.region")) {
            p.sendMessage(PSL.NO_PERMISSION_REGION.msg());
            return true;
        }

        if (args.length < 3) {
            p.sendMessage(PSL.REGION_HELP.msg());
            return true;
        }

        LocalPlayer lp = wg.wrapOfflinePlayer(Bukkit.getOfflinePlayer(args[2]));

        if (args[1].equalsIgnoreCase("count")) { // count player's regions
            int count = ArgCount.countRegionsOfPlayer(lp, rgm); // TODO check if rgm needs to be p2's

            p.sendMessage(PSL.OTHER_REGION_COUNT.msg()
                    .replace("%player%", args[2])
                    .replace("%num%", "" + count));

        } else if (args[1].equalsIgnoreCase("list")) { // list player's regions
            StringBuilder regionMessage = new StringBuilder();
            boolean found = false;
            for (String s : rgm.getRegions().keySet()) {
                if (s.startsWith("ps") && rgm.getRegions().get(s).getOwners().contains(lp)) {
                    regionMessage.append(s).append(", ");
                    found = true;
                }
            }

            if (!found) {
                p.sendMessage(PSL.REGION_NOT_FOUND_FOR_PLAYER.msg()
                        .replace("%player%", args[2]));
            } else {
                regionMessage = new StringBuilder(regionMessage.substring(0, regionMessage.length() - 2) + ".");
                p.sendMessage(PSL.REGION_LIST.msg()
                        .replace("%player%", args[2])
                        .replace("%regions%", regionMessage));
            }

        } else if ((args[1].equalsIgnoreCase("remove")) || (args[1].equalsIgnoreCase("regen")) || (args[1].equalsIgnoreCase("disown"))) {

            // Find regions
            Map<String, ProtectedRegion> regions = rgm.getRegions();
            List<String> regionIDList = new ArrayList<>();
            int index = 0;
            for (String idname : regions.keySet()) {
                if (idname.startsWith("ps") && (regions.get(idname).getOwners().contains(lp))) {
                    regionIDList.add(idname);
                    index++;
                }
            }
            if (index == 0) {
                p.sendMessage(PSL.REGION_NOT_FOUND_FOR_PLAYER.msg().replace("%player%", args[2]));
            } else {

                // Remove regions
                for (String s : regionIDList)
                    ProtectionStones.removeDisownRegenPSRegion(lp, args[1].toLowerCase(), s, rgm, p);

                p.sendMessage(PSL.REGION_REMOVE.msg().replace("%player%", args[2]));
                try {
                    rgm.save();
                } catch (Exception e) {
                    System.out.println("[ProtectionStones] WorldGuard Error [" + e + "] during Region File Save");
                }
            }
        }
        return true;
    }
}
