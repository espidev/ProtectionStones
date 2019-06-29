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

import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ArgHome implements PSCommandArg {

    @Override
    public List<String> getNames() {
        return Collections.singletonList("home");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args) {
        Player p = (Player) s;

        // prelim checks
        if (!p.hasPermission("protectionstones.home")) {
            PSL.msg(p, PSL.NO_PERMISSION_HOME.msg());
            return true;
        }
        if (args.length != 2) {
            PSL.msg(p, PSL.HOME_HELP.msg());
            return true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(ProtectionStones.getInstance(), () -> {
            // get regions from the query
            List<PSRegion> regions = ProtectionStones.getPSRegions(p.getWorld(), args[1]);

            // remove regions not owned by the player
            for (int i = 0; i < regions.size(); i++) {
                if (!regions.get(i).isOwner(p.getUniqueId())) {
                    regions.remove(i);
                    i--;
                }
            }

            if (regions.isEmpty()) {
                PSL.msg(s, PSL.REGION_DOES_NOT_EXIST.msg());
                return;
            }
            if (regions.size() > 1) {
                ChatUtils.displayDuplicateRegionAliases(p, regions);
                return;
            }

            ArgTp.teleportPlayer(p, regions.get(0));
        });


        /* OLD NUMBER BASED SYSTEM
        // get the region id the player wants to teleport to
        int regionNumber;
        try {
            regionNumber = Integer.parseInt(args[1]);
            if (regionNumber <= 0) {
                PSL.msg(p, PSL.NUMBER_ABOVE_ZERO.msg());
                return true;
            }
        } catch (NumberFormatException e) {
            PSL.msg(p, PSL.TP_VALID_NUMBER.msg());
            return true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(ProtectionStones.getInstance(), () -> {
            LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(p);
            List<ProtectedRegion> regions = ArgTp.getRegionsPlayerHas(lp, WGUtils.getRegionManagerWithPlayer(p));
            if (regions.isEmpty()) {
                PSL.msg(p, PSL.NO_REGIONS_OWNED.msg());
                return;
            }
            if (regionNumber > regions.size()) {
                PSL.msg(p, PSL.HOME_ONLY.msg().replace("%num%", "" + regions.size()));
                return;
            }

            PSRegion r = ProtectionStones.toPSRegion(p.getWorld(), regions.get(regionNumber-1));

            ArgTp.teleportPlayer(p, r);
        });*/

        return true;
    }
}
