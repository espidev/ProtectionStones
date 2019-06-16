package dev.espi.ProtectionStones.commands;

import dev.espi.ProtectionStones.PSProtectBlock;
import dev.espi.ProtectionStones.PSL;
import dev.espi.ProtectionStones.ProtectionStones;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class ArgGive implements PSCommandArg {

    @Override
    public List<String> getNames() {
        return Collections.singletonList("give");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return true;
    }

    @Override
    public boolean executeArgument(CommandSender p, String[] args) {
        if (!p.hasPermission("protectionstones.give")) {
            PSL.msg(p, PSL.NO_PERMISSION_GIVE.msg());
            return true;
        }

        if (args.length != 3) {
            PSL.msg(p, PSL.GIVE_HELP.msg());
            return true;
        }

        // check if player online
        if (Bukkit.getPlayer(args[2]) == null) {
            PSL.msg(p, PSL.PLAYER_NOT_FOUND.msg() + " (" + args[2] + ")");
            return true;
        }

        // check if argument is valid block
        PSProtectBlock cp = ProtectionStones.getProtectBlockFromAlias(args[1]);
        if (cp == null) {
            PSL.msg(p, PSL.INVALID_BLOCK.msg());
            return true;
        }

        // check if item was able to be added (inventory not full)
        if (!Bukkit.getPlayer(args[2]).getInventory().addItem(cp.createItem()).isEmpty()) {
            PSL.msg(p, PSL.GIVE_NO_INVENTORY_ROOM.msg());
            return true;
        }

        PSL.msg(p, PSL.GIVE_GIVEN.msg().replace("%block%", args[1]).replace("%player%", Bukkit.getPlayer(args[2]).getDisplayName()));

        return true;
    }

}
