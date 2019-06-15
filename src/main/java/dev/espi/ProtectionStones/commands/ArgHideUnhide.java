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
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.ProtectionStones.*;
import dev.espi.ProtectionStones.utils.WGUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class ArgHideUnhide implements PSCommandArg {

    @Override
    public List<String> getNames() {
        return Arrays.asList("hide", "unhide");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] arg) {
        Player p = (Player) s;
        PSRegion r = ProtectionStones.getPSRegion(p.getLocation());

        // preliminary checks
        if (arg.equals("unhide") && !p.hasPermission("protectionstones.unhide")) {
            PSL.msg(p, PSL.NO_PERMISSION_UNHIDE.msg());
            return true;
        }
        if (arg.equals("hide") && !p.hasPermission("protectionstones.hide")) {
            PSL.msg(p, PSL.NO_PERMISSION_HIDE.msg());
            return true;
        }
        if (r == null) {
            PSL.msg(p, PSL.NOT_IN_REGION.msg());
            return true;
        }
        if (ProtectionStones.hasNoAccess(r.getWGRegion(), p, WorldGuardPlugin.inst().wrapPlayer(p), false)) {
            PSL.msg(p, PSL.NO_ACCESS.msg());
            return true;
        }

        if (r.isHidden()) {
            if (arg.equals("hide")) {
                PSL.msg(p, PSL.ALREADY_HIDDEN.msg());
                return true;
            }
            r.hide();
        } else {
            if (arg.equals("unhide")) {
                PSL.msg(p, PSL.ALREADY_NOT_HIDDEN.msg());
                return true;
            }
            r.unhide();
        }
        return true;
    }

}
