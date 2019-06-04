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
import dev.espi.ProtectionStones.FlagHandler;
import dev.espi.ProtectionStones.PSL;
import dev.espi.ProtectionStones.ProtectionStones;
import org.bukkit.entity.Player;

public class ArgFlag {
    public static boolean argumentFlag(Player p, String[] args) {
        String psID = ProtectionStones.playerToPSID(p);

        WorldGuardPlugin wg = (WorldGuardPlugin) ProtectionStones.wgd;
        RegionManager rgm = ProtectionStones.getRegionManagerWithPlayer(p);

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
                FlagHandler fh = new FlagHandler();
                fh.setFlag(args, rgm.getRegion(psID), p);
            } else {
                PSL.msg(p, PSL.NO_PERMISSION_PER_FLAG.msg());
            }
        }
        return true;
    }
}
