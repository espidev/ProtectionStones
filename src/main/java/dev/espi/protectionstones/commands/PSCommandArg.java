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

import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.List;

public interface PSCommandArg {
    List<String> getNames();
    boolean allowNonPlayersToExecute();
    List<String> getPermissionsToExecute();
    HashMap<String, Boolean> getRegisteredFlags(); // <flag, has value after>
    boolean executeArgument(CommandSender s, String[] args, HashMap<String, String> flags);
    List<String> tabComplete(CommandSender sender, String alias, String[] args);
}
