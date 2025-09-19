/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.espi.protectionstones.commands;

import dev.espi.protectionstones.PSPlayer;
import dev.espi.protectionstones.PSProtectBlock;
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.ProtectionStones;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
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
        // Send the header
        PSL.msg(p, PSL.GET_HEADER.msg());

        for (PSProtectBlock b : ProtectionStones.getInstance().getConfiguredBlocks()) {
            if ((!b.permission.equals("") && !p.hasPermission(b.permission))
                    || (b.preventPsGet && !p.hasPermission("protectionstones.admin"))) {
                continue; // no permission
            }

            String price = new DecimalFormat("#.##").format(b.price);

            // Build the line using placeholders
            Component line = PSL.GET_GUI_BLOCK.replaceAll(Map.of(
                    "%alias%", b.alias,
                    "%price%", price,
                    "%description%", b.description,
                    "%xradius%", String.valueOf(b.xRadius),
                    "%yradius%", String.valueOf(b.yRadius),
                    "%zradius%", String.valueOf(b.zRadius)
            ));

            // Build hover text
            Component hover = PSL.GET_GUI_HOVER.replaceAll(Map.of(
                    "%alias%", b.alias,
                    "%price%", price,
                    "%description%", b.description,
                    "%xradius%", String.valueOf(b.xRadius),
                    "%yradius%", String.valueOf(b.yRadius),
                    "%zradius%", String.valueOf(b.zRadius)
            ));

            // Add hover + click events
            Component clickable = line
                    .hoverEvent(HoverEvent.showText(hover))
                    .clickEvent(ClickEvent.runCommand(
                            "/" + ProtectionStones.getInstance().getConfigOptions().base_command + " get " + b.alias
                    ));

            // Send Adventure component to player
            PSL.msg(p, clickable);
        }
        return true;
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args, HashMap<String, String> flags) {
        Player p = (Player) s;
        PSPlayer psp = PSPlayer.fromPlayer(p);
        if (!p.hasPermission("protectionstones.get"))
            return PSL.msg(p, PSL.NO_PERMISSION_GET.msg());

        // /ps get (for GUI)
        if (args.length == 1) return openGetGUI(p);

        if (args.length != 2)
            return PSL.msg(p, PSL.GET_HELP.msg());

        // check if argument is valid block
        PSProtectBlock cp = ProtectionStones.getProtectBlockFromAlias(args[1]);
        if (cp == null)
            return PSL.msg(p, PSL.INVALID_BLOCK.msg());

        // check for block permission (custom)
        if (!cp.permission.equals("") && !p.hasPermission(cp.permission))
            return PSL.msg(p, PSL.GET_NO_PERMISSION_BLOCK.msg());

        // check if /ps get is disabled on this
        if (cp.preventPsGet && !p.hasPermission("protectionstones.admin"))
            return PSL.msg(p, PSL.GET_NO_PERMISSION_BLOCK.msg());

        // check if player has enough money
        if (ProtectionStones.getInstance().isVaultSupportEnabled() && cp.price != 0 && !psp.hasAmount(cp.price))
            return PSL.msg(p, PSL.NOT_ENOUGH_MONEY.replace("%price%", String.format("%.2f", cp.price)));

        // debug message
        if (!ProtectionStones.getInstance().isVaultSupportEnabled() && cp.price != 0) {
            Bukkit.getLogger().info("Vault is not enabled but there is a price set on the protection stone! It will not work!");
        }

        // take money
        if (ProtectionStones.getInstance().isVaultSupportEnabled() && cp.price != 0) {
            EconomyResponse er = psp.withdrawBalance(cp.price);
            if (!er.transactionSuccess()) {
                return PSL.msg(p, Component.text(er.errorMessage));
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
                    EconomyResponse er = psp.depositBalance(cp.price);
                    if (!er.transactionSuccess()) {
                        return PSL.msg(p, Component.text(er.errorMessage));
                    }
                }
            }
            return true;
        }

        return PSL.msg(p, PSL.GET_GOTTEN.msg());
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
