package me.vik1395.ProtectionStones.commands;

import me.vik1395.ProtectionStones.ConfigProtectBlock;
import me.vik1395.ProtectionStones.PSL;
import me.vik1395.ProtectionStones.ProtectionStones;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ArgGive {
    public static boolean argumentGive(Player p, String[] args) {
        if (!p.hasPermission("protectionstones.give")) {
            PSL.msg(p, PSL.NO_PERMISSION_GET.msg());
            return true;
        }

        if (args.length != 3) {
            PSL.msg(p, PSL.GIVE_HELP.msg());
            return true;
        }

        // check if argument is valid block
        ConfigProtectBlock cp = ProtectionStones.getProtectBlockFromName(args[1]);
        if (cp == null) {
            PSL.msg(p, PSL.INVALID_BLOCK.msg());
            return true;
        }

        // check if player online
        if (Bukkit.getPlayer(args[2]) == null) {
            PSL.msg(p, PSL.PLAYER_NOT_FOUND.msg());
            return true;
        }

        // check if item was able to be added (inventory not full)
        if (!Bukkit.getPlayer(args[2]).getInventory().addItem(ProtectionStones.createProtectBlockItem(cp)).isEmpty()) {
            PSL.msg(p, PSL.GIVE_NO_INVENTORY_ROOM.msg());
            return true;
        }

        PSL.msg(p, PSL.GIVE_GIVEN.msg().replace("%block%", args[1]).replace("%player%", p.getDisplayName()));

        return true;
    }
}
