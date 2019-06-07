package dev.espi.ProtectionStones.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface PSCommandArg {
    List<String> getNames();
    boolean allowNonPlayersToExecute();
    boolean executeArgument(CommandSender s, String[] args);
}
