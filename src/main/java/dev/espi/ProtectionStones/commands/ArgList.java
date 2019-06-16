package dev.espi.ProtectionStones.commands;

import dev.espi.ProtectionStones.PSL;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class ArgList implements PSCommandArg {
    @Override
    public List<String> getNames() {
        return Collections.singletonList("list");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return true;
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args) {
        if (!s.hasPermission("protectionstones.list")) {
            PSL.msg(s, PSL.NO_PERMISSION_LIST.msg());
            return true;
        }
        if (args.length == 2 && !s.hasPermission("protectionstones.list.others")) {
            PSL.msg(s, PSL.NO_PERMISSION_LIST_OTHERS.msg());
            return true;
        }



        return true;
    }
}
