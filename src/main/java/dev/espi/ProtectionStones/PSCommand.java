package dev.espi.ProtectionStones;

import dev.espi.ProtectionStones.commands.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
        ProtectionStones.getInstance().addCommandArgument(new ArgPriority());
        ProtectionStones.getInstance().addCommandArgument(new ArgRegion());
        ProtectionStones.getInstance().addCommandArgument(new ArgReload());
        ProtectionStones.getInstance().addCommandArgument(new ArgSethome());
        ProtectionStones.getInstance().addCommandArgument(new ArgToggle());
        ProtectionStones.getInstance().addCommandArgument(new ArgTp());
        ProtectionStones.getInstance().addCommandArgument(new ArgUnclaim());
        ProtectionStones.getInstance().addCommandArgument(new ArgView());
        ProtectionStones.getInstance().addCommandArgument(new ArgHelp());
    }

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
                    s.sendMessage(ChatColor.RED + "You can only use /ps reload, /ps admin and /ps give from console.");
                    return true;
                }
            }
        }

        PSL.msg(s, PSL.NO_SUCH_COMMAND.msg());
        return true;
    }
}
