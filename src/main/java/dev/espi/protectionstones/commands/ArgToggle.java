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

import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.utils.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ArgToggle implements PSCommandArg {

    @Override
    public List<String> getNames() {
        return Collections.singletonList("toggle");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public List<String> getPermissionsToExecute() {
        return Collections.singletonList(Permissions.TOGGLE);
    }

    @Override
    public HashMap<String, Boolean> getRegisteredFlags() {
        return null;
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args, HashMap<String, String> flags) {
        Player p = (Player) s;
        if (p.hasPermission(Permissions.TOGGLE)) {
            if (!ProtectionStones.toggleList.contains(p.getUniqueId())) {
                ProtectionStones.toggleList.add(p.getUniqueId());
                p.sendMessage(PSL.TOGGLE_OFF.msg());
            } else {
                ProtectionStones.toggleList.remove(p.getUniqueId());
                p.sendMessage(PSL.TOGGLE_ON.msg());
            }
        } else {
            p.sendMessage(PSL.NO_PERMISSION_TOGGLE.msg());
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return null;
    }
}
