package me.vik1395.ProtectionStones.commands;

import me.vik1395.ProtectionStones.ConfigProtectBlock;
import me.vik1395.ProtectionStones.PSL;
import me.vik1395.ProtectionStones.ProtectionStones;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

public class ArgGet {
    public static boolean argumentGet(Player p, String[] args) {
        if (!p.hasPermission("protectionstones.get")) {
            p.sendMessage(PSL.NO_PERMISSION_GET.msg());
            return true;
        }

        boolean found = false;
        ConfigProtectBlock cp = null;
        for (ConfigProtectBlock cpb : ProtectionStones.protectionStonesOptions.values()) {
            if (cpb.alias.equalsIgnoreCase(args[1]) || cpb.type.equalsIgnoreCase(args[1])) {
                found = true;
                cp = cpb;
                break;
            }
        }
        if (!found) {
            p.sendMessage(PSL.INVALID_BLOCK.msg());
            return true;
        }

        // check if player has enough money
        if (ProtectionStones.isVaultEnabled && ProtectionStones.vaultEconomy.has(p, cp.price)) {
            p.sendMessage(PSL.NOT_ENOUGH_MONEY.msg().replace("%price%", String.format("%.2f", cp.price)));
            return true;
        }

        // take money
        if (ProtectionStones.isVaultEnabled) {
            EconomyResponse er = ProtectionStones.vaultEconomy.withdrawPlayer(p, cp.price);
            if (!er.transactionSuccess()) {
                p.sendMessage(er.errorMessage);
                return true;
            }
        }

        // check if item was able to be added (inventory not full)
        if (!p.getInventory().addItem(ProtectionStones.createProtectBlockItem(cp)).isEmpty()) {
            p.sendMessage(PSL.NO_ROOM_IN_INVENTORY.msg());
            return true;
        }

        return true;
    }
}
