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
 * See the License for the specific  language governing permissions and
 * limitations under the License.
 */

package dev.espi.ProtectionStones.commands;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.ProtectionStones.PSL;
import dev.espi.ProtectionStones.PSRegion;
import dev.espi.ProtectionStones.ProtectionStones;
import dev.espi.ProtectionStones.utils.WGUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ArgFlag implements PSCommandArg {

    @Override
    public List<String> getNames() {
        return Collections.singletonList("flag");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args) {
        Player p = (Player) s;
        PSRegion r = PSRegion.fromLocation(p.getLocation());

        if (!p.hasPermission("protectionstones.flags")) {
            PSL.msg(p, PSL.NO_PERMISSION_FLAGS.msg());
            return true;
        }
        if (r == null) {
            PSL.msg(p, PSL.NOT_IN_REGION.msg());
            return true;
        }
        if (WGUtils.hasNoAccess(r.getWGRegion(), p, WorldGuardPlugin.inst().wrapPlayer(p), false)) {
            PSL.msg(p, PSL.NO_ACCESS.msg());
            return true;
        }

        if (args.length < 3) {
            PSL.msg(p, PSL.FLAG_HELP.msg());
        } else {
            if (r.getTypeOptions().allowedFlags.contains((args[1].equals("-g") ? args[3].toLowerCase() : args[1].toLowerCase()))) {
                String flag, value = "", gee = "";
                if (args[1].equalsIgnoreCase("-g")) {
                    flag = args[3];
                    for (int i = 4; i < args.length; i++) value += args[i] + " ";
                    gee = args[2];
                } else {
                    flag = args[1];
                    for (int i = 2; i < args.length; i++) value += args[i] + " ";
                }
                setFlag(r.getWGRegion(), p, flag, value.trim(), gee);
            } else {
                PSL.msg(p, PSL.NO_PERMISSION_PER_FLAG.msg());
            }
        }
        return true;
    }

    // /ps flag logic (utilizing WG internal /region flag logic)
    static void setFlag(ProtectedRegion region, CommandSender p, String flagName, String value, String groupValue) {
        Flag flag = Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), flagName);

        try {
            if (value.equalsIgnoreCase("default")) {
                region.setFlag(flag, flag.getDefault());
                region.setFlag(flag.getRegionGroupFlag(), null);
                PSL.msg(p, PSL.FLAG_SET.msg().replace("%flag%", flagName));
            } else if (value.equalsIgnoreCase("null")) {
                region.setFlag(flag, null);
                region.setFlag(flag.getRegionGroupFlag(), null);
                PSL.msg(p, PSL.FLAG_SET.msg().replace("%flag%", flagName));
            } else {
                FlagContext fc = FlagContext.create().setInput(value).build();
                region.setFlag(flag, flag.parseInput(fc));
                if (!groupValue.equals("")) {
                    region.setFlag(flag.getRegionGroupFlag(), flag.getRegionGroupFlag().detectValue(groupValue));
                }
                PSL.msg(p, PSL.FLAG_SET.msg().replace("%flag%", flagName));
            }

        } catch (InvalidFlagFormat invalidFlagFormat) {
            //invalidFlagFormat.printStackTrace();
            PSL.msg(p, PSL.FLAG_NOT_SET.msg().replace("%flag%", flagName));
        }
    }

}
