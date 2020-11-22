/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.espi.protectionstones.commands;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

class ArgAdminFlag {
    static boolean argumentAdminFlag(CommandSender p, String[] args) {

        if (args.length < 5) {
            PSL.msg(p, ArgAdmin.getFlagHelp());
            return true;
        }

        String flag, value = "", gee = "";
        World w = Bukkit.getWorld(args[2]);
        if (w == null)
            return PSL.msg(p, PSL.INVALID_WORLD.msg());

        if (args[3].equalsIgnoreCase("-g")) {
            flag = args[5];
            for (int i = 6; i < args.length; i++) value += args[i] + " ";
            gee = args[4];
        } else {
            flag = args[3];
            for (int i = 4; i < args.length; i++) value += args[i] + " ";
        }

        if (WGUtils.getFlagRegistry().get(flag) == null)
            return PSL.msg(p, PSL.FLAG_NOT_SET.msg());

        final String fValue = value, fGee = gee;
        RegionManager rgm = WGUtils.getRegionManagerWithWorld(w);
        for (ProtectedRegion r : rgm.getRegions().values()) {
            if (ProtectionStones.isPSRegion(r) && PSRegion.fromWGRegion(w, r) != null) {
                ArgFlag.setFlag(PSRegion.fromWGRegion(w, r), p, flag, fValue.trim(), fGee);
            }
        }
        return true;
    }
}
