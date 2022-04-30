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
import dev.espi.protectionstones.*;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
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
    public List<String> getPermissionsToExecute() {
        return Arrays.asList("protectionstones.hide", "protectionstones.unhide");
    }

    @Override
    public HashMap<String, Boolean> getRegisteredFlags() {
        return null;
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] arg, HashMap<String, String> flags) {
        Player p = (Player) s;
        PSRegion r = PSRegion.fromLocationGroup(p.getLocation());

        // preliminary checks
        if (arg[0].equals("unhide") && !p.hasPermission("protectionstones.unhide"))
            return PSL.msg(p, PSL.NO_PERMISSION_UNHIDE.msg());

        if (arg[0].equals("hide") && !p.hasPermission("protectionstones.hide"))
            return PSL.msg(p, PSL.NO_PERMISSION_HIDE.msg());

        if (r == null)
            return PSL.msg(p, PSL.NOT_IN_REGION.msg());

        if (WGUtils.hasNoAccess(r.getWGRegion(), p, WorldGuardPlugin.inst().wrapPlayer(p), false))
            return PSL.msg(p, PSL.NO_ACCESS.msg());

        if (r.isHidden()) {
            if (arg[0].equals("hide")) {
                return PSL.msg(p, PSL.ALREADY_HIDDEN.msg());
            }
            r.unhide();
        } else {
            if (arg[0].equals("unhide")) {
                return PSL.msg(p, PSL.ALREADY_NOT_HIDDEN.msg());
            }
            r.hide();
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return null;
    }

}
