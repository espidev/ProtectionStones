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
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.utils.ChatUtil;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
    public List<String> getPermissionsToExecute() {
        return Arrays.asList("protectionstones.setparent");
    }

    @Override
    public HashMap<String, Boolean> getRegisteredFlags() {
        return null;
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args, HashMap<String, String> flags) {
        if (!s.hasPermission("protectionstones.setparent")) {
            PSL.msg(s, PSL.NO_PERMISSION_SETPARENT.msg());
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
        if (args.length != 2) {
            PSL.msg(s, PSL.SETPARENT_HELP.msg());
            return true;
        }

        if (args[1].equals("none")) { // remove parent
            try {
                r.setParent(null);
                PSL.msg(s, PSL.SETPARENT_SUCCESS_REMOVE.msg().replace("%id%", r.getName() == null ? r.getId() : r.getName()));
            } catch (ProtectedRegion.CircularInheritanceException e) {
                e.printStackTrace(); // won't happen ever
            }
        } else {
            List<PSRegion> parent = ProtectionStones.getPSRegions(p.getWorld(), args[1]);

            if (parent.isEmpty()) {
                PSL.msg(s, PSL.REGION_DOES_NOT_EXIST.msg());
                return true;
            }
            if (!p.hasPermission("protectionstones.setparent.others") && !parent.get(0).isOwner(p.getUniqueId())) {
                PSL.msg(s, PSL.NO_PERMISSION_SETPARENT_OTHERS.msg());
                return true;
            }
            if (parent.size() > 1) {
                ChatUtil.displayDuplicateRegionAliases(p, parent);
                return true;
            }

            try {
                r.setParent(parent.get(0));
            } catch (ProtectedRegion.CircularInheritanceException e) {
                PSL.msg(s, PSL.SETPARENT_CIRCULAR_INHERITANCE.msg());
                return true;
            }

            PSL.msg(s, PSL.SETPARENT_SUCCESS.msg().replace("%id%", r.getName() == null ? r.getId() : r.getName())
                    .replace("%parent%", parent.get(0).getName() == null ? parent.get(0).getId() : parent.get(0).getName()));
        }
        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return null;
    }
}
