package dev.espi.ProtectionStones.commands;

import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

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
    public boolean executeArgument(CommandSender s, String[] args) {
        return true;
    }

// TODO ADD GROUP AND NAME
    // TO README, PERMISSIONS, HELP
}

