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

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.ProtectionStones.PSL;
import dev.espi.ProtectionStones.PSRegion;
import dev.espi.ProtectionStones.ProtectionStones;
import dev.espi.ProtectionStones.utils.ChatUtils;
import dev.espi.ProtectionStones.utils.WGUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ArgSetparent implements PSCommandArg {
    @Override
    public List<String> getNames() {
        return Collections.singletonList("setparent");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args) {
        if (!s.hasPermission("protectionstones.setparent")) {
            PSL.msg(s, PSL.NO_PERMISSION_NAME.msg());
            return true;
        }
        Player p = (Player) s;
        PSRegion r = PSRegion.fromLocation(p.getLocation());
        if (r == null) {
            PSL.msg(s, PSL.NOT_IN_REGION.msg());
            return true;
        }
        if (WGUtils.hasNoAccess(r.getWGRegion(), p, WorldGuardPlugin.inst().wrapPlayer(p), false)) {
            PSL.msg(s, PSL.NO_ACCESS.msg());
            return true;
        }
        if (args.length != 2) {
            PSL.msg(s, PSL.SETPARENT_HELP.msg());
            return true;
        }

        if (args[1].equals("none")) { // remove parent
            try {
                r.setParent(null);
                PSL.msg(s, PSL.SETPARENT_SUCCESS_REMOVE.msg().replace("%id%", r.getName() == null ? r.getID() : r.getName()));
            } catch (ProtectedRegion.CircularInheritanceException e) {
                e.printStackTrace(); // won't happen ever
            }
        } else {
            List<PSRegion> parent = ProtectionStones.getPSRegions(p.getWorld(), args[1]);

            if (!p.hasPermission("protectionstones.setparent.others")) {
                // remove regions not owned by the player
                for (int i = 0; i < parent.size(); i++) {
                    if (!parent.get(i).isOwner(p.getUniqueId())) {
                        parent.remove(i);
                        i--;
                    }
                }
            }

            if (parent.isEmpty()) {
                PSL.msg(s, PSL.REGION_DOES_NOT_EXIST.msg());
                return true;
            }
            if (parent.size() > 1) {
                ChatUtils.displayDuplicateRegionAliases(p, parent);
                return true;
            }

            try {
                r.setParent(parent.get(0));
            } catch (ProtectedRegion.CircularInheritanceException e) {
                PSL.msg(s, PSL.SETPARENT_CIRCULAR_INHERITANCE.msg());
                return true;
            }

            PSL.msg(s, PSL.SETPARENT_SUCCESS.msg().replace("%id%", r.getName() == null ? r.getID() : r.getName())
                    .replace("%parent%", parent.get(0).getName() == null ? parent.get(0).getID() : parent.get(0).getName()));
        }
        return false;
    }
}
