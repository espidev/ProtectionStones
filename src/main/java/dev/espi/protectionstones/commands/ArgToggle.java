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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ArgToggle implements PSCommandArg {

    // /ps on
    public static class ArgToggleOn implements PSCommandArg {
        @Override
        public List<String> getNames() {
            return Collections.singletonList("on");
        }
        @Override
        public boolean allowNonPlayersToExecute() {
            return false;
        }
        @Override
        public List<String> getPermissionsToExecute() {
            return Collections.singletonList("protectionstones.toggle");
        }
        @Override
        public HashMap<String, Boolean> getRegisteredFlags() {
            return null;
        }
        @Override
        public boolean executeArgument(CommandSender s, String[] args, HashMap<String, String> flags) {
            Player p = (Player) s;
            if (p.hasPermission("protectionstones.toggle")) {
                ProtectionStones.toggleList.remove(p.getUniqueId());
                PSL.msg(p, PSL.TOGGLE_ON.msg());
            } else {
                PSL.msg(p, PSL.NO_PERMISSION_TOGGLE.msg());
            }
            return true;
        }
        @Override
        public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
            return null;
        }
    }

    // /ps off
    public static class ArgToggleOff implements PSCommandArg {
        @Override
        public List<String> getNames() {
            return Collections.singletonList("off");
        }
        @Override
        public boolean allowNonPlayersToExecute() {
            return false;
        }
        @Override
        public List<String> getPermissionsToExecute() {
            return Collections.singletonList("protectionstones.toggle");
        }
        @Override
        public HashMap<String, Boolean> getRegisteredFlags() {
            return null;
        }
        @Override
        public boolean executeArgument(CommandSender s, String[] args, HashMap<String, String> flags) {
            Player p = (Player) s;
            if (p.hasPermission("protectionstones.toggle")) {
                ProtectionStones.toggleList.add(p.getUniqueId());
                PSL.msg(p, PSL.TOGGLE_OFF.msg());
            } else {
                PSL.msg(p, PSL.NO_PERMISSION_TOGGLE.msg());
            }
            return true;
        }
        @Override
        public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
            return null;
        }
    }

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
        return Collections.singletonList("protectionstones.toggle");
    }

    @Override
    public HashMap<String, Boolean> getRegisteredFlags() {
        return null;
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args, HashMap<String, String> flags) {
        Player p = (Player) s;
        if (p.hasPermission("protectionstones.toggle")) {
            if (!ProtectionStones.toggleList.contains(p.getUniqueId())) {
                ProtectionStones.toggleList.add(p.getUniqueId());
                PSL.msg(p, PSL.TOGGLE_OFF.msg());
            } else {
                ProtectionStones.toggleList.remove(p.getUniqueId());
                PSL.msg(p, PSL.TOGGLE_ON.msg());
            }
        } else {
            PSL.msg(p, PSL.NO_PERMISSION_TOGGLE.msg());
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return null;
    }
}
