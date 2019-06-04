package dev.espi.ProtectionStones.commands;

import org.bukkit.command.CommandSender;

public interface PSCommandArg {
    boolean executeArgument(CommandSender s, String[] args);
}
