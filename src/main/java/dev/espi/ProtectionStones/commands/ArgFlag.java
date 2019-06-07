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
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.ProtectionStones.FlagHandler;
import dev.espi.ProtectionStones.PSL;
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
        String psID = WGUtils.playerToPSID(p);

        WorldGuardPlugin wg = WorldGuardPlugin.inst();
        RegionManager rgm = WGUtils.getRegionManagerWithPlayer(p);

        if (!p.hasPermission("protectionstones.flags")) {
            PSL.msg(p, PSL.NO_PERMISSION_FLAGS.msg());
            return true;
        }
        if (psID.equals("")) {
            PSL.msg(p, PSL.NOT_IN_REGION.msg());
            return true;
        }
        if (ProtectionStones.hasNoAccess(rgm.getRegion(psID), p, wg.wrapPlayer(p), false)) {
            PSL.msg(p, PSL.NO_ACCESS.msg());
            return true;
        }

        if (args.length < 3) {
            PSL.msg(p, PSL.FLAG_HELP.msg());
        } else {
            String blockType = rgm.getRegion(psID).getFlag(FlagHandler.PS_BLOCK_MATERIAL);
            if (ProtectionStones.getBlockOptions(blockType).allowed_flags.contains((args[1].equals("-g") ? args[3].toLowerCase() : args[1].toLowerCase()))) {
                setFlag(args, rgm.getRegion(psID), p);
            } else {
                PSL.msg(p, PSL.NO_PERMISSION_PER_FLAG.msg());
            }
        }
        return true;
    }

    // /ps flag logic (utilizing WG internal /region flag logic)
    private void setFlag(String[] args, ProtectedRegion region, Player p) {
        Flag flag;

        if (args[1].equalsIgnoreCase("-g")) {
            flag = Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), args[3]);
        } else {
            flag = Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), args[1]);
        }

        if (args[2].equalsIgnoreCase("default")) {
            region.setFlag(flag, flag.getDefault());
            region.setFlag(flag.getRegionGroupFlag(), null);
            PSL.msg(p, PSL.FLAG_SET.msg().replace("%flag%", args[1]));
        } else {
            String settings = "";
            if (args[1].equalsIgnoreCase("-g")) {
                for (int i = 4; i < args.length; i++) settings += args[i] + " ";
            } else {
                for (int i = 2; i < args.length; i++) settings += args[i] + " ";
            }

            FlagContext fc = FlagContext.create().setInput(settings.trim()).build();
            try {
                region.setFlag(flag, flag.parseInput(fc));
                if (args[1].equalsIgnoreCase("-g")) {
                    region.setFlag(flag.getRegionGroupFlag(), flag.getRegionGroupFlag().detectValue(args[2]));
                }
            } catch (InvalidFlagFormat invalidFlagFormat) {
                //invalidFlagFormat.printStackTrace();
                PSL.msg(p, PSL.FLAG_NOT_SET.msg().replace("%flag%", args[1]));
                return;
            }
            PSL.msg(p, PSL.FLAG_SET.msg().replace("%flag%", args[1]));
        }
    }

}
