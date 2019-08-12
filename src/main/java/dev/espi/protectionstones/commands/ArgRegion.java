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

package dev.espi.protectionstones.commands;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.PSLocation;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.utils.UUIDCache;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class ArgRegion implements PSCommandArg {

    // /ps region

    @Override
    public List<String> getNames() {
        return Collections.singletonList("region");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public List<String> getPermissionsToExecute() {
        return Arrays.asList("protectionstones.region");
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args) {
        Player p = (Player) s;
        WorldGuardPlugin wg = WorldGuardPlugin.inst();
        RegionManager rgm = WGUtils.getRegionManagerWithPlayer(p);

        if (!p.hasPermission("protectionstones.region")) {
            PSL.msg(p, PSL.NO_PERMISSION_REGION.msg());
            return true;
        }

        if (args.length < 3) {
            PSL.msg(p, PSL.REGION_HELP.msg());
            return true;
        }

        if (!UUIDCache.nameToUUID.containsKey(args[2])) {
            PSL.msg(p, PSL.PLAYER_NOT_FOUND.msg());
            return true;
        }

        LocalPlayer lp = wg.wrapOfflinePlayer(Bukkit.getOfflinePlayer(UUIDCache.nameToUUID.get(args[2])));

        if (args[1].equalsIgnoreCase("list")) { // list player's regions
            StringBuilder regionMessage = new StringBuilder();
            boolean found = false;
            for (String str : rgm.getRegions().keySet()) {
                if (str.startsWith("ps") && rgm.getRegions().get(str).getOwners().contains(lp)) {
                    regionMessage.append(str).append(", ");
                    found = true;
                }
            }

            if (!found) {
                PSL.msg(p, PSL.REGION_NOT_FOUND_FOR_PLAYER.msg()
                        .replace("%player%", args[2]));
            } else {
                regionMessage = new StringBuilder(regionMessage.substring(0, regionMessage.length() - 2) + ".");
                PSL.msg(p, PSL.REGION_LIST.msg()
                        .replace("%player%", args[2])
                        .replace("%regions%", regionMessage));
            }

        } else if ((args[1].equalsIgnoreCase("remove")) || (args[1].equalsIgnoreCase("disown"))) {

            // Find regions
            Map<String, ProtectedRegion> regions = rgm.getRegions();
            List<String> regionIDList = new ArrayList<>();
            boolean found = false;
            for (String idname : regions.keySet()) {
                if (idname.startsWith("ps") && (regions.get(idname).getOwners().contains(lp))) {
                    regionIDList.add(idname);
                    found = true;
                }
            }
            if (!found) {
                PSL.msg(p, PSL.REGION_NOT_FOUND_FOR_PLAYER.msg().replace("%player%", args[2]));
                return true;
            }

            // Remove regions
            for (String str : regionIDList) {
                ProtectedRegion region = rgm.getRegion(str);
                switch (args[1].toLowerCase()) {
                    case "disown":
                        DefaultDomain owners = region.getOwners();
                        owners.removePlayer(lp);
                        region.setOwners(owners);
                        break;
                    case "remove":
                        if (str.substring(0, 2).equals("ps")) {
                            PSLocation psl = WGUtils.parsePSRegionToLocation(str);
                            Block blockToRemove = p.getWorld().getBlockAt(psl.x, psl.y, psl.z);
                            blockToRemove.setType(Material.AIR);
                        }
                        // remove region
                        // check if removing the region and firing region remove event blocked it
                        if (!ProtectionStones.removePSRegion(p.getWorld(), str, p)) {
                            return true;
                        }
                        break;
                }
            }

            PSL.msg(p, PSL.REGION_REMOVE.msg().replace("%player%", args[2]));
        } else {
            PSL.msg(p, PSL.REGION_HELP.msg());
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return args.length == 2 ? StringUtil.copyPartialMatches(args[1], Arrays.asList("disown", "remove", "list"), new ArrayList<>()) : null;
    }
}
