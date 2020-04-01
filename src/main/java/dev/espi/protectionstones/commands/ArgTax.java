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
import dev.espi.protectionstones.utils.MiscUtil;
import dev.espi.protectionstones.utils.TextGUI;
import dev.espi.protectionstones.utils.UUIDCache;
import lombok.val;
import lombok.var;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class ArgTax implements PSCommandArg {

    static final String INFO_HELP = ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps tax info [region (optional)]", // maybe put in /ps info
            PAY_HELP = ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps tax pay [amount] [region (optional)]",
            AUTOPAY_HELP = ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps tax autopay [region (optional)]";

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
        HashMap<String, Boolean> m = new HashMap<>();
        m.put("-p", true);
        return m;
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

        switch (args[1]) {
            case "info":
                return taxInfo(args, flags, psp);
            case "pay":
                return taxPay(args, psp);
            case "autopay":
                return taxAutoPay(args, psp);
            default:
                runHelp(s);
                break;
        }

        return true;
    }

    private static final int GUI_SIZE = 17;

    public boolean taxInfo(String[] args, HashMap<String, String> flags, PSPlayer p) {
        if (args.length == 2) { // /ps tax info
            Bukkit.getScheduler().runTaskAsynchronously(ProtectionStones.getInstance(), () -> {
                int pageNum = (flags.get("-p") == null || !StringUtils.isNumeric(flags.get("-p")) ? 0 : Integer.parseInt(flags.get("-p"))-1);

                List<TextComponent> entries = new ArrayList<>();
                for (PSRegion r : p.getTaxEligibleRegions()) {
                    double amountDue = 0;
                    for (var tp : r.getTaxPaymentsDue()) {
                        amountDue += tp.getAmount();
                    }

                    TextComponent component;
                    if (r.getTaxAutopayer() != null & r.getTaxAutopayer() == p.getUuid()) {
                        component = new TextComponent(PSL.TAX_PLAYER_REGION_INFO_AUTOPAYER.msg()
                                .replace("%region%", (r.getName() == null ? r.getId() : r.getName() + " (" + r.getId() + ")"))
                                .replace("%money%", String.format("%.2f", amountDue)));
                    } else {
                        component = new TextComponent(PSL.TAX_PLAYER_REGION_INFO.msg()
                                .replace("%region%", (r.getName() == null ? r.getId() : r.getName() + " (" + r.getId() + ")"))
                                .replace("%money%", String.format("%.2f", amountDue)));
                    }
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + ProtectionStones.getInstance().getConfigOptions().base_command + " tax info " + r.getId()));
                    component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(PSL.TAX_CLICK_TO_SHOW_MORE_INFO.msg()).create()));
                    entries.add(component);
                }

                TextGUI.displayGUI(p.getPlayer(), PSL.TAX_INFO_HEADER.msg(), "/" + ProtectionStones.getInstance().getConfigOptions().base_command + " tax info -p %page%", pageNum, GUI_SIZE, entries, true);

                if (pageNum * GUI_SIZE + GUI_SIZE < entries.size())
                    PSL.msg(p, PSL.TAX_NEXT.msg().replace("%page%", pageNum + 2 + ""));
            });
        } else if (args.length == 3) { // /ps tax info [region]
            var list = ProtectionStones.getPSRegions(p.getPlayer().getWorld(), args[2]);
            if (list.isEmpty()) {
                return PSL.msg(p, PSL.REGION_DOES_NOT_EXIST.msg());
            }
            PSRegion r = list.get(0);
            double taxesOwed = 0;
            for (PSRegion.TaxPayment tp : r.getTaxPaymentsDue()) {
                taxesOwed += tp.getAmount();
            }

            PSL.msg(p, PSL.TAX_REGION_INFO_HEADER.msg().replace("%region%", r.getName() == null ? r.getId() : r.getName() + " (" + r.getId() + ")"));
            PSL.msg(p, PSL.TAX_REGION_INFO.msg()
                        .replace("%taxrate%", String.format("%.2f", r.getTaxRate()))
                        .replace("%taxperiod%", r.getTaxPeriod())
                        .replace("%taxpaymentperiod%", r.getTaxPaymentPeriod())
                        .replace("%taxautopayer%", r.getTaxAutopayer() == null ? "none" : UUIDCache.getNameFromUUID(r.getTaxAutopayer()))
                        .replace("%taxowed%", String.format("%.2f", taxesOwed)));
        } else {
            PSL.msg(p, INFO_HELP);
        }
        return true;
    }

    public boolean taxPay(String[] args, PSPlayer p) {
        if (args.length != 3 && args.length != 4)
            return PSL.msg(p, PAY_HELP);
        // the amount to pay must be a number
        if (!NumberUtils.isNumber(args[2]))
            return PSL.msg(p, PAY_HELP);

        PSRegion r = resolveRegion(args.length == 4 ? args[3] : null, p);
        if (r == null) return true;

        // player must be owner to pay for taxes
        if (!r.isOwner(p.getUuid()))
            return PSL.msg(p, PSL.NOT_OWNER.msg());

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
                .replace("%amount%", String.format("%.2f", res.amount))
                .replace("%region%", r.getName() == null ? r.getId() : r.getName() + "(" + r.getId() + ")"));
        return true;
    }

    public boolean taxAutoPay(String[] args, PSPlayer p) {
        if (args.length != 2 && args.length != 3)
            return PSL.msg(p, AUTOPAY_HELP);

        PSRegion r = resolveRegion(args.length == 3 ? args[2] : null, p);
        if (r == null) return true;

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

    public PSRegion resolveRegion(String region, PSPlayer p) {
        PSRegion r;
        if (region == null) { // region the player is standing in
            r = PSRegion.fromLocationGroup(p.getPlayer().getLocation());
            if (r == null) {
                PSL.msg(p, PSL.NOT_IN_REGION.msg());
                return null;
            }

            // if taxes are disabled for this region
            if (r.getTypeOptions() == null || r.getTypeOptions().taxPeriod == -1) {
                PSL.msg(p, PSL.TAX_DISABLED_REGION.msg());
                return null;
            }

        } else { // region query
            var list = ProtectionStones.getPSRegions(p.getPlayer().getWorld(), region);
            if (list.isEmpty()) {
                PSL.msg(p, PSL.REGION_DOES_NOT_EXIST.msg());
                return null;
            } else {
                r = list.get(0);
            }
        }
        return r;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 2) {
            List<String> arg = Arrays.asList("info", "pay", "autopay");
            return StringUtil.copyPartialMatches(args[1], arg, new ArrayList<>());
        }
        return null;
    }
}
