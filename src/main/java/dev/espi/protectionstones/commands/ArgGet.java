package dev.espi.protectionstones.commands;

import dev.espi.protectionstones.PSProtectBlock;
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.ProtectionStones;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.text.DecimalFormat;
import java.util.*;

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
    public List<String> getPermissionsToExecute() {
        return Collections.singletonList("protectionstones.get");
    }

    @Override
    public HashMap<String, Boolean> getRegisteredFlags() {
        return null;
    }

    private boolean openGetGUI(Player p) {
        PSL.msg(p, PSL.GET_HEADER.msg());
        for (PSProtectBlock b : ProtectionStones.getInstance().getConfiguredBlocks()) {
            if ((!b.permission.equals("") && !p.hasPermission(b.permission)) || (b.preventPsGet && !p.hasPermission("protectionstones.admin"))) {
                continue; // no permission
            }

            String price = new DecimalFormat("#.##").format(b.price);

            TextComponent tc = new TextComponent(PSL.GET_GUI_BLOCK.msg()
                    .replace("%alias%", b.alias)
                    .replace("%price%", price)
                    .replace("%xradius%", ""+b.xRadius)
                    .replace("%yradius%", ""+b.yRadius)
                    .replace("%zradius%", ""+b.zRadius));

            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(PSL.GET_GUI_HOVER.msg()
                    .replace("%alias%", b.alias)
                    .replace("%price%", price)
                    .replace("%xradius%", ""+b.xRadius)
                    .replace("%yradius%", ""+b.yRadius)
                    .replace("%zradius%", ""+b.zRadius)).create()));
            tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + ProtectionStones.getInstance().getConfigOptions().base_command + " get " + b.alias));

            p.spigot().sendMessage(tc);
        }
        return true;
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args, HashMap<String, String> flags) {
        Player p = (Player) s;
        if (!p.hasPermission("protectionstones.get")) {
            PSL.msg(p, PSL.NO_PERMISSION_GET.msg());
            return true;
        }

        // /ps get (for GUI)
        if (args.length == 1) return openGetGUI(p);

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

        // check for block permission (custom)
        if (!cp.permission.equals("") && !p.hasPermission(cp.permission)) {
            PSL.msg(p, PSL.GET_NO_PERMISSION_BLOCK.msg());
            return true;
        }

        // check if /ps get is disabled on this
        if (cp.preventPsGet && !p.hasPermission("protectionstones.admin")) {
            PSL.msg(p, PSL.GET_NO_PERMISSION_BLOCK.msg());
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
            if (ProtectionStones.getInstance().getConfigOptions().dropItemWhenInventoryFull) { // drop on floor
                PSL.msg(p, PSL.NO_ROOM_DROPPING_ON_FLOOR.msg());
                p.getWorld().dropItem(p.getLocation(), cp.createItem());
            } else { // cancel event
                PSL.msg(p, PSL.NO_ROOM_IN_INVENTORY.msg());
                if (ProtectionStones.getInstance().isVaultSupportEnabled()) {
                    EconomyResponse er = ProtectionStones.getInstance().getVaultEconomy().depositPlayer(p, cp.price);
                    if (!er.transactionSuccess()) {
                        PSL.msg(p, er.errorMessage);
                        return true;
                    }
                }
            }
            return true;
        }

        PSL.msg(p, PSL.GET_GOTTEN.msg());

        return true;
    }

    // tab completion
    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        List<String> l = new ArrayList<>();
        for (PSProtectBlock b : ProtectionStones.getInstance().getConfiguredBlocks()) {
            if ((!b.permission.equals("") && !sender.hasPermission(b.permission)) || (b.preventPsGet && !sender.hasPermission("protectionstones.admin"))) continue; // no permission
            l.add(b.alias);
        }
        return args.length == 2 ? StringUtil.copyPartialMatches(args[1], l, new ArrayList<>()) : null;
    }

}
