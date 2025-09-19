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

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

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
    public List<String> getPermissionsToExecute() {
        return Arrays.asList("protectionstones.name");
    }

    @Override
    public HashMap<String, Boolean> getRegisteredFlags() {
        return null;
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args, HashMap<String, String> flags) {
        if (!s.hasPermission("protectionstones.name")) {
            PSL.msg(s, PSL.NO_PERMISSION_NAME.msg());
            return true;
        }
        Player p = (Player) s;
        PSRegion r = PSRegion.fromLocationGroup(p.getLocation());
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
            PSL.msg(p, PSL.NAME_REMOVED.replace("%id%", r.getId()));
        } else {
            if (!ProtectionStones.getInstance().getConfigOptions().allowDuplicateRegionNames && ProtectionStones.isPSNameAlreadyUsed(args[1])) {
                PSL.msg(p, PSL.NAME_TAKEN.replace("%name%", args[1]));
                return true;
            }
            r.setName(args[1]);
            PSL.msg(p,
                    PSL.NAME_SET_NAME.replaceAll(Map.of(
                            "%id%", r.getId(),
                            "%name%", r.getName()
                    ))
            );
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return null;
    }

}

