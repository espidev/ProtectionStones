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

package dev.espi.protectionstones;

import dev.espi.protectionstones.commands.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class PSCommand extends Command {

    PSCommand(String name) {
        super(name);
    }

    static void addDefaultArguments() {
        ProtectionStones.getInstance().addCommandArgument(new ArgAddRemove());
        ProtectionStones.getInstance().addCommandArgument(new ArgAdmin());
        ProtectionStones.getInstance().addCommandArgument(new ArgCount());
        ProtectionStones.getInstance().addCommandArgument(new ArgFlag());
        ProtectionStones.getInstance().addCommandArgument(new ArgGet());
        ProtectionStones.getInstance().addCommandArgument(new ArgGive());
        ProtectionStones.getInstance().addCommandArgument(new ArgHideUnhide());
        ProtectionStones.getInstance().addCommandArgument(new ArgHome());
        ProtectionStones.getInstance().addCommandArgument(new ArgInfo());
        ProtectionStones.getInstance().addCommandArgument(new ArgList());
        ProtectionStones.getInstance().addCommandArgument(new ArgName());
        ProtectionStones.getInstance().addCommandArgument(new ArgPriority());
        ProtectionStones.getInstance().addCommandArgument(new ArgRegion());
        ProtectionStones.getInstance().addCommandArgument(new ArgReload());
        ProtectionStones.getInstance().addCommandArgument(new ArgSethome());
        ProtectionStones.getInstance().addCommandArgument(new ArgSetparent());
        ProtectionStones.getInstance().addCommandArgument(new ArgToggle());
        ProtectionStones.getInstance().addCommandArgument(new ArgTp());
        ProtectionStones.getInstance().addCommandArgument(new ArgUnclaim());
        ProtectionStones.getInstance().addCommandArgument(new ArgView());
        ProtectionStones.getInstance().addCommandArgument(new ArgHelp());
    }

    /*
    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        List<String> l = new ArrayList<>();
        for (PSCommandArg ps : ProtectionStones.getInstance().getCommandArguments()) {
            l.add(ps.getNames().get(0));
        }
        return (args.length > 0) ? StringUtil.copyPartialMatches(args[0], l, new ArrayList<>()) : null;
    }*/

    @Override
    public boolean execute(CommandSender s, String label, String[] args) {
        if (args.length == 0) { // no arguments
            new ArgHelp().executeArgument(s, args);
            return true;
        }
        for (PSCommandArg command : ProtectionStones.getInstance().getCommandArguments()) {
            if (command.getNames().contains(args[0])) {
                if (command.allowNonPlayersToExecute() || s instanceof Player) {
                    return command.executeArgument(s, args);
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
