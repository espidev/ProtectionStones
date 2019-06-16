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
import dev.espi.ProtectionStones.PSL;
import dev.espi.ProtectionStones.PSRegion;
import dev.espi.ProtectionStones.ProtectionStones;
import dev.espi.ProtectionStones.utils.WGUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ArgName implements PSCommandArg {
    @Override
    public List<String> getNames() {
        return Collections.singletonList("name");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args) {
        if (!s.hasPermission("protectionstones.name")) {
            PSL.msg(s, PSL.NO_PERMISSION_NAME.msg());
            return true;
        }
        Player p = (Player) s;
        PSRegion r = ProtectionStones.getPSRegion(p.getLocation());
        if (r == null) {
            PSL.msg(s, PSL.NOT_IN_REGION.msg());
            return true;
        }
        if (WGUtils.hasNoAccess(r.getWGRegion(), p, WorldGuardPlugin.inst().wrapPlayer(p), false)) {
            PSL.msg(s, PSL.NO_ACCESS.msg());
            return true;
        }
        if (args.length < 2) {
            PSL.msg(s, PSL.NAME_HELP.msg());
            return true;
        }

        if (args[1].equals("none")) {
            r.setName(null);
            PSL.msg(p, PSL.NAME_REMOVED.msg().replace("%id%", r.getID()));
        } else {
            if (ProtectionStones.isPSNameAlreadyUsed(args[1])) {
                PSL.msg(p, PSL.NAME_TAKEN.msg().replace("%name%", args[1]));
                return true;
            }
            r.setName(args[1]);
            PSL.msg(p, PSL.NAME_SET_NAME.msg().replace("%id%", r.getID()).replace("%name%", r.getName()));
        }
        return true;
    }

}

