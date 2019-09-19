/*
 * Copyright 2019 ProtectionStones team and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package dev.espi.protectionstones.commands;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class ArgAdminForceMerge {

    // /ps admin forcemerge [world]
    public static boolean argumentAdminForceMerge(CommandSender p, String[] args) {
        if (args.length < 3) {
            PSL.msg(p, PSL.ADMIN_FORCEMERGE_HELP.msg());
        }

        String world = args[2];
        for (ProtectedRegion pr : WGUtils.getRegionManagerWithWorld(Bukkit.getWorld(world)).getRegions().values()) {
            // TODO
        }
        return true;
    }
}
