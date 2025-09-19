package dev.espi.protectionstones.commands;

import dev.espi.protectionstones.*;
import dev.espi.protectionstones.utils.MiscUtil;
import dev.espi.protectionstones.utils.TextGUI;
import dev.espi.protectionstones.utils.UUIDCache;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.economy.EconomyResponse;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;

public class ArgTax implements PSCommandArg {

    private static final Component INFO_HELP =
            Component.text("> /ps tax info [region (optional)]", NamedTextColor.AQUA);
    private static final Component PAY_HELP =
            Component.text("> /ps tax pay [amount] [region (optional)]", NamedTextColor.AQUA);
    private static final Component AUTOPAY_HELP =
            Component.text("> /ps tax autopay [region (optional)]", NamedTextColor.AQUA);

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
            return PSL.msg(s, Component.text("Taxes are disabled! Enable it in the config.", NamedTextColor.RED));
        }

        Player p = (Player) s;
        PSPlayer psp = PSPlayer.fromPlayer(p);

        if (args.length == 1 || args[1].equalsIgnoreCase("help")) {
            runHelp(s);
            return true;
        }

        switch (args[1].toLowerCase()) {
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
                int pageNum = (flags.get("-p") == null || !MiscUtil.isValidInteger(flags.get("-p"))
                        ? 0
                        : Integer.parseInt(flags.get("-p")) - 1);

                List<Component> entries = new ArrayList<>();
                for (PSRegion r : p.getTaxEligibleRegions()) {
                    double amountDue = r.getTaxPaymentsDue().stream()
                            .mapToDouble(PSRegion.TaxPayment::getAmount).sum();

                    Component base = (r.getTaxAutopayer() != null && r.getTaxAutopayer().equals(p.getUuid()))
                            ? PSL.TAX_PLAYER_REGION_INFO_AUTOPAYER.replaceAll(Map.of(
                            "%region%", (r.getName() == null ? r.getId() : r.getName() + " (" + r.getId() + ")"),
                            "%money%", String.format("%.2f", amountDue)
                    ))
                            : PSL.TAX_PLAYER_REGION_INFO.replaceAll(Map.of(
                            "%region%", (r.getName() == null ? r.getId() : r.getName() + " (" + r.getId() + ")"),
                            "%money%", String.format("%.2f", amountDue)
                    ));

                    Component clickable = base
                            .clickEvent(ClickEvent.runCommand("/" + ProtectionStones.getInstance().getConfigOptions().base_command + " tax info " + r.getId()))
                            .hoverEvent(HoverEvent.showText(PSL.TAX_CLICK_TO_SHOW_MORE_INFO.msg()));

                    entries.add(clickable);
                }

                TextGUI.displayGUI(
                        p.getPlayer(),
                        PSL.TAX_INFO_HEADER.msg(),
                        "/" + ProtectionStones.getInstance().getConfigOptions().base_command + " tax info -p %page%",
                        pageNum,
                        GUI_SIZE,
                        entries,
                        true
                );

                if (pageNum * GUI_SIZE + GUI_SIZE < entries.size()) {
                    PSL.msg(p.getPlayer(), PSL.TAX_NEXT.replaceAll(Map.of("%page%", String.valueOf(pageNum + 2))));
                }
            });
        } else if (args.length == 3) { // /ps tax info [region]
            List<PSRegion> list = ProtectionStones.getPSRegions(p.getPlayer().getWorld(), args[2]);
            if (list.isEmpty()) {
                return PSL.msg(p.getPlayer(), PSL.REGION_DOES_NOT_EXIST.msg());
            }
            PSRegion r = list.get(0);
            double taxesOwed = r.getTaxPaymentsDue().stream().mapToDouble(PSRegion.TaxPayment::getAmount).sum();

            PSL.msg(p.getPlayer(), PSL.TAX_REGION_INFO_HEADER.replaceAll(Map.of(
                    "%region%", r.getName() == null ? r.getId() : r.getName() + " (" + r.getId() + ")"
            )));
            PSL.msg(p.getPlayer(), PSL.TAX_REGION_INFO.replaceAll(Map.of(
                    "%taxrate%", String.format("%.2f", r.getTaxRate()),
                    "%taxperiod%", r.getTaxPeriod(),
                    "%taxpaymentperiod%", r.getTaxPaymentPeriod(),
                    "%taxautopayer%", r.getTaxAutopayer() == null ? "none" : UUIDCache.getNameFromUUID(r.getTaxAutopayer()),
                    "%taxowed%", String.format("%.2f", taxesOwed)
            )));
        } else {
            runHelp(p.getPlayer());
        }
        return true;
    }

    public boolean taxPay(String[] args, PSPlayer p) {
        if (args.length != 3 && args.length != 4)
            return PSL.msg(p.getPlayer(), PAY_HELP);

        if (!NumberUtils.isNumber(args[2]))
            return PSL.msg(p.getPlayer(), PAY_HELP);

        PSRegion r = resolveRegion(args.length == 4 ? args[3] : null, p);
        if (r == null) return true;

        if (!r.isOwner(p.getUuid()))
            return PSL.msg(p.getPlayer(), PSL.NOT_OWNER.msg());

        double payment = Double.parseDouble(args[2]);
        if (payment <= 0)
            return PSL.msg(p.getPlayer(), PAY_HELP);

        if (!p.hasAmount(payment))
            return PSL.msg(p.getPlayer(), PSL.NOT_ENOUGH_MONEY.replaceAll(Map.of("%price%", String.format("%.2f", payment))));

        EconomyResponse res = r.payTax(p, payment);
        PSL.msg(p.getPlayer(), PSL.TAX_PAID.replaceAll(Map.of(
                "%amount%", String.format("%.2f", res.amount),
                "%region%", (r.getName() == null ? r.getId() : r.getName() + " (" + r.getId() + ")")
        )));
        return true;
    }

    public boolean taxAutoPay(String[] args, PSPlayer p) {
        if (args.length != 2 && args.length != 3)
            return PSL.msg(p.getPlayer(), AUTOPAY_HELP);

        PSRegion r = resolveRegion(args.length == 3 ? args[2] : null, p);
        if (r == null) return true;

        if (!r.isOwner(p.getUuid()))
            return PSL.msg(p.getPlayer(), PSL.NOT_OWNER.msg());

        if (r.getTaxAutopayer() != null && r.getTaxAutopayer().equals(p.getUuid())) {
            r.setTaxAutopayer(null);
            PSL.msg(p.getPlayer(), PSL.TAX_SET_NO_AUTOPAYER.replaceAll(Map.of(
                    "%region%", (r.getName() == null ? r.getId() : r.getName() + " (" + r.getId() + ")")
            )));
        } else {
            r.setTaxAutopayer(p.getUuid());
            PSL.msg(p.getPlayer() , PSL.TAX_SET_AS_AUTOPAYER.replaceAll(Map.of(
                    "%region%", (r.getName() == null ? r.getId() : r.getName() + " (" + r.getId() + ")")
            )));
        }
        return true;
    }

    public PSRegion resolveRegion(String region, PSPlayer p) {
        PSRegion r;
        if (region == null) {
            r = PSRegion.fromLocationGroup(p.getPlayer().getLocation());
            if (r == null) {
                PSL.msg(p.getPlayer(), PSL.NOT_IN_REGION.msg());
                return null;
            }

            if (r.getTypeOptions() == null || r.getTypeOptions().taxPeriod == -1) {
                PSL.msg(p.getPlayer(), PSL.TAX_DISABLED_REGION.msg());
                return null;
            }

        } else {
            List<PSRegion> list = ProtectionStones.getPSRegions(p.getPlayer().getWorld(), region);
            if (list.isEmpty()) {
                PSL.msg(p.getPlayer(), PSL.REGION_DOES_NOT_EXIST.msg());
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
