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

import dev.espi.protectionstones.*;
import lombok.val;
import lombok.var;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class ArgTax implements PSCommandArg {

    static final String INFO_HELP = ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps tax info [region (optional)]", // maybe put in /ps info
            PAY_HELP = ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps tax pay [amount]",
            AUTOPAY_HELP = ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps tax autopay";

    @Override
    public List<String> getNames() {
        return Collections.singletonList("tax");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public List<String> getPermissionsToExecute() {
        return Collections.singletonList("protectionstones.tax");
    }

    @Override
    public HashMap<String, Boolean> getRegisteredFlags() {
        return null;
    }

    private void runHelp(CommandSender s) {
        PSL.msg(s, PSL.TAX_HELP_HEADER.msg());
        PSL.msg(s, INFO_HELP);
        PSL.msg(s, PAY_HELP);
        PSL.msg(s, AUTOPAY_HELP);
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args, HashMap<String, String> flags) {
        if (!s.hasPermission("protectionstones.tax")) {
            return PSL.msg(s, PSL.NO_PERMISSION_TAX.msg());
        }
        if (!ProtectionStones.getInstance().getConfigOptions().taxEnabled) {
            return PSL.msg(s, ChatColor.RED + "Taxes are disabled! Enable it in the config.");
        }

        Player p = (Player) s;
        PSPlayer psp = PSPlayer.fromPlayer(p);

        if (args.length == 1 || args[1].equals("help")) {
            runHelp(s);
            return true;
        }

        // /ps tax info
        if (args[1].equals("info")) {
            PSL.msg(p, PSL.TAX_INFO_HEADER.msg());
            Bukkit.getScheduler().runTaskAsynchronously(ProtectionStones.getInstance(), () -> {
                for (PSRegion r : psp.getTaxEligibleRegions()) {
                    double amountDue = 0;
                    for (var tp : r.getTaxPaymentsDue()) {
                        amountDue += tp.getAmount();
                    }

                    TextComponent component;
                    if (r.getTaxAutopayer() != null & r.getTaxAutopayer() == p.getUniqueId()) {
                        component = new TextComponent(PSL.TAX_PLAYER_REGION_INFO_AUTOPAYER.msg()
                                .replace("%region%", (r.getName() == null ? r.getId() : r.getName() + " (" + r.getId() + ")"))
                                .replace("%money%", String.format("%.2f", amountDue)));
                    } else {
                        component = new TextComponent(PSL.TAX_PLAYER_REGION_INFO.msg()
                                .replace("%region%", (r.getName() == null ? r.getId() : r.getName() + " (" + r.getId() + ")"))
                                .replace("%money%", String.format("%.2f", amountDue)));
                    }
                    // todo hover information, pages
                    p.spigot().sendMessage(component);
                }
            });
            return true;
        }

        // other tax sub commands requiring a region
        PSRegion r = PSRegion.fromLocationGroup(p.getLocation());
        if (r == null)
            return PSL.msg(p, PSL.NOT_IN_REGION.msg());

        PSProtectBlock cp = r.getTypeOptions();

        // if taxes disabled for this region
        if (cp.taxPeriod == -1)
            return PSL.msg(s, PSL.TAX_DISABLED_REGION.msg());

        switch (args[1]) {
            case "pay":
                return taxPay(args, psp, r);
            case "autopay":
                return taxAutoPay(args, psp, r);
            default:
                runHelp(s);
                break;
        }

        return true;
    }

    public boolean taxPay(String[] args, PSPlayer p, PSRegion r) {
        // player must be owner to pay for taxes
        if (!r.isOwner(p.getUuid()))
            return PSL.msg(p, PSL.NOT_OWNER.msg());
        // the amount to pay must be a number
        if (args.length != 3 && !NumberUtils.isNumber(args[2]))
            return PSL.msg(p, PAY_HELP);

        val payment = Double.parseDouble(args[2]);
        // must be higher than or equal to zero
        if (payment <= 0)
            return PSL.msg(p, PAY_HELP);
        // player must have this amount of money
        if (!p.hasAmount(payment))
            return PSL.msg(p, PSL.NOT_ENOUGH_MONEY.msg());

        // pay tax amount
        val res = r.payTax(p, payment);
        PSL.msg(p, PSL.TAX_PAID.msg()
                .replace("%amount%", ""+res.amount)
                .replace("%region%", r.getName() == null ? r.getId() : r.getName() + "(" + r.getId() + ")"));
        return true;
    }

    public boolean taxAutoPay(String[] args, PSPlayer p, PSRegion r) {
        // player must be the owner of the region
        if (!r.isOwner(p.getUuid()))
            return PSL.msg(p, PSL.NOT_OWNER.msg());

        if (r.getTaxAutopayer() != null && r.getTaxAutopayer().equals(p.getUuid())) { // if removing the the tax autopayer
            r.setTaxAutopayer(null);
            PSL.msg(p, PSL.TAX_SET_NO_AUTOPAYER.msg().replace("%region%", r.getName() == null ? r.getId() : r.getName() + "(" + r.getId() + ")"));
        } else { // if the player is setting themselves as the tax autopayer
            r.setTaxAutopayer(p.getUuid());
            PSL.msg(p, PSL.TAX_SET_AS_AUTOPAYER.msg().replace("%region%", r.getName() == null ? r.getId() : r.getName() + "(" + r.getId() + ")"));
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        List<String> arg = Arrays.asList("info", "pay", "autopay");
        return StringUtil.copyPartialMatches(args[1], arg, new ArrayList<>());
    }
}
