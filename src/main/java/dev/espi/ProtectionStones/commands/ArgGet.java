package dev.espi.ProtectionStones.commands;

import dev.espi.ProtectionStones.PSProtectBlock;
import dev.espi.ProtectionStones.PSL;
import dev.espi.ProtectionStones.ProtectionStones;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ArgGet implements PSCommandArg {

    @Override
    public List<String> getNames() {
        return Collections.singletonList("get");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args) {
        Player p = (Player) s;
        if (!p.hasPermission("protectionstones.get")) {
            PSL.msg(p, PSL.NO_PERMISSION_GET.msg());
            return true;
        }

        if (args.length != 2) {
            PSL.msg(p, PSL.GET_HELP.msg());
            return true;
        }

        // check if argument is valid block
        PSProtectBlock cp = ProtectionStones.getProtectBlockFromAlias(args[1]);
        if (cp == null) {
            PSL.msg(p, PSL.INVALID_BLOCK.msg());
            return true;
        }

        if (!cp.permission.equals("") && !p.hasPermission(cp.permission)) {
            PSL.msg(p, PSL.NO_PERMISSION_GET.msg());
            return true;
        }

        // check if player has enough money
        if (ProtectionStones.getInstance().isVaultSupportEnabled() && !ProtectionStones.getInstance().getVaultEconomy().has(p, cp.price)) {
            PSL.msg(p, PSL.NOT_ENOUGH_MONEY.msg().replace("%price%", String.format("%.2f", cp.price)));
            return true;
        }

        // debug message
        if (!ProtectionStones.getInstance().isVaultSupportEnabled() && cp.price != 0) {
            Bukkit.getLogger().info("Vault is not enabled but there is a price set on the protection stone! It will not work!");
        }

        // take money
        if (ProtectionStones.getInstance().isVaultSupportEnabled()) {
            EconomyResponse er = ProtectionStones.getInstance().getVaultEconomy().withdrawPlayer(p, cp.price);
            if (!er.transactionSuccess()) {
                PSL.msg(p, er.errorMessage);
                return true;
            }
        }

        // check if item was able to be added (inventory not full)
        if (!p.getInventory().addItem(cp.createItem()).isEmpty()) {
            PSL.msg(p, PSL.NO_ROOM_IN_INVENTORY.msg());
            return true;
        }

        PSL.msg(p, PSL.GET_GOTTEN.msg());

        return true;
    }

}
