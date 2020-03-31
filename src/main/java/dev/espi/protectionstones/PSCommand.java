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

package dev.espi.protectionstones;

import dev.espi.protectionstones.commands.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PSCommand extends Command {

    PSCommand(String name) {
        super(name);
    }

    static void addDefaultArguments() {
        ProtectionStones.getInstance().addCommandArgument(new ArgAddRemove());
        ProtectionStones.getInstance().addCommandArgument(new ArgAdmin());
        ProtectionStones.getInstance().addCommandArgument(new ArgBuySell());
        ProtectionStones.getInstance().addCommandArgument(new ArgCount());
        ProtectionStones.getInstance().addCommandArgument(new ArgFlag());
        ProtectionStones.getInstance().addCommandArgument(new ArgGet());
        ProtectionStones.getInstance().addCommandArgument(new ArgGive());
        ProtectionStones.getInstance().addCommandArgument(new ArgHideUnhide());
        ProtectionStones.getInstance().addCommandArgument(new ArgHome());
        ProtectionStones.getInstance().addCommandArgument(new ArgInfo());
        ProtectionStones.getInstance().addCommandArgument(new ArgList());
        ProtectionStones.getInstance().addCommandArgument(new ArgMerge());
        ProtectionStones.getInstance().addCommandArgument(new ArgName());
        ProtectionStones.getInstance().addCommandArgument(new ArgPriority());
        ProtectionStones.getInstance().addCommandArgument(new ArgRegion());
        ProtectionStones.getInstance().addCommandArgument(new ArgReload());
        ProtectionStones.getInstance().addCommandArgument(new ArgRent());
        ProtectionStones.getInstance().addCommandArgument(new ArgSethome());
        ProtectionStones.getInstance().addCommandArgument(new ArgSetparent());
        ProtectionStones.getInstance().addCommandArgument(new ArgTax());
        ProtectionStones.getInstance().addCommandArgument(new ArgToggle());
        ProtectionStones.getInstance().addCommandArgument(new ArgTp());
        ProtectionStones.getInstance().addCommandArgument(new ArgUnclaim());
        ProtectionStones.getInstance().addCommandArgument(new ArgView());
        ProtectionStones.getInstance().addCommandArgument(new ArgHelp());
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            List<String> l = new ArrayList<>();
            for (PSCommandArg ps : ProtectionStones.getInstance().getCommandArguments()) {
                boolean hasPerm = false;
                if (ps.getPermissionsToExecute() == null) {
                    hasPerm = true;
                } else {
                    for (String perm : ps.getPermissionsToExecute()) {
                        if (sender.hasPermission(perm)) {
                            hasPerm = true;
                            break;
                        }
                    }
                }
                if (hasPerm) l.addAll(ps.getNames());
            }
            return StringUtil.copyPartialMatches(args[0], l, new ArrayList<>());
        } else if (args.length >= 2) {
            for (PSCommandArg ps : ProtectionStones.getInstance().getCommandArguments()) {
                for (String arg : ps.getNames()) {
                    if (arg.equalsIgnoreCase(args[0])) {
                        return ps.tabComplete(sender, alias, args);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean execute(CommandSender s, String label, String[] args) {
        if (args.length == 0) { // no arguments
            if (s instanceof ConsoleCommandSender) {
                s.sendMessage(ChatColor.RED + "You can only use /ps reload, /ps admin, /ps give from console.");
            } else {
                new ArgHelp().executeArgument(s, args, null);
            }
            return true;
        }
        for (PSCommandArg command : ProtectionStones.getInstance().getCommandArguments()) {
            if (command.getNames().contains(args[0])) {
                if (command.allowNonPlayersToExecute() || s instanceof Player) {

                    // extract flags
                    List<String> nArgs = new ArrayList<>();
                    HashMap<String, String> flags = new HashMap<>();
                    for (int i = 0; i < args.length; i++) {

                        if (command.getRegisteredFlags() != null && command.getRegisteredFlags().containsKey(args[i])) {
                            if (command.getRegisteredFlags().get(args[i])) { // has value after
                                if (i != args.length-1) {
                                    flags.put(args[i], args[++i]);
                                }
                            } else {
                                flags.put(args[i], null);
                            }
                        } else {
                            nArgs.add(args[i]);
                        }
                    }

                    return command.executeArgument(s, nArgs.toArray(new String[0]), flags);
                } else if (!command.allowNonPlayersToExecute()) {
                    s.sendMessage(ChatColor.RED + "You can only use /ps reload, /ps admin, /ps give from console.");
                    return true;
                }
            }
        }

        PSL.msg(s, PSL.NO_SUCH_COMMAND.msg());
        return true;
    }
}
