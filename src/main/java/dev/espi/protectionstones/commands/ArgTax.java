/*
 * Copyright 2019 ProtectionStones team and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package dev.espi.protectionstones.commands;

import dev.espi.protectionstones.*;
import lombok.val;
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
            return PSL.msg(s, ChatColor.RED + "Renting is disabled! Enable it in the config.");
        }

        Player p = (Player) s;
        PSPlayer psp = PSPlayer.fromPlayer(p);

        if (args.length == 1 || args[1].equals("help")) {
            runHelp(s);
            return true;
        }

        if (args[1].equals("info")) {
            PSL.msg(p, PSL.TAX_INFO_HEADER.msg());
            Bukkit.getScheduler().runTaskAsynchronously(ProtectionStones.getInstance(), () -> { // TODO
                for (PSRegion r : psp.getPSRegions(p.getWorld(), false)) {
                    if (r.getTypeOptions() != null && r.getTypeOptions().taxPeriod != -1) {
                        PSL.msg(p, "-" + (r.getName() == null ? r.getID() : r.getName() + " (" + r.getID() + ")"));
                    }
                }
            });
            return true;
        }

        PSRegion r = PSRegion.fromLocation(p.getLocation());

        if (r == null)
            return PSL.msg(p, PSL.NOT_IN_REGION.msg());

        PSProtectBlock cp = r.getTypeOptions();

        if (cp.taxPeriod == -1) { // taxes disabled for this region
            return PSL.msg(s, PSL.TAX_DISABLED_REGION.msg());
        }

        switch (args[1]) {
            case "pay":

                if (!r.isOwner(p.getUniqueId()))
                    return PSL.msg(p, PSL.NOT_OWNER.msg());

                if (!NumberUtils.isNumber(args[2]))
                    return PSL.msg(p, PAY_HELP);

                val payment = Double.parseDouble(args[2]);

                if (payment <= 0)
                    return PSL.msg(p, PAY_HELP);

                if (!psp.hasAmount(payment))
                    return PSL.msg(p, PSL.NOT_ENOUGH_MONEY.msg());

                val res = r.payTax(psp, payment);

                PSL.msg(p, PSL.TAX_PAID.msg()
                        .replace("%amount%", ""+res.amount)
                        .replace("%region%", r.getName() == null ? r.getID() : r.getName() + "(" + r.getID() + ")"));

                break;
            case "autopay":

                if (!r.isOwner(p.getUniqueId()))
                    return PSL.msg(p, PSL.NOT_OWNER.msg());

                if (r.getTaxAutopayer() != null && r.getTaxAutopayer().equals(p.getUniqueId())) {
                    r.setTaxAutopayer(null);
                    PSL.msg(s, PSL.TAX_SET_NO_AUTOPAYER.msg().replace("%region%", r.getName() == null ? r.getID() : r.getName() + "(" + r.getID() + ")"));
                } else {
                    r.setTaxAutopayer(p.getUniqueId());
                    PSL.msg(s, PSL.TAX_SET_AS_AUTOPAYER.msg().replace("%region%", r.getName() == null ? r.getID() : r.getName() + "(" + r.getID() + ")"));
                }
                break;
            default:
                runHelp(s);
                break;
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        List<String> arg = Arrays.asList("info", "pay", "autopay");
        return StringUtil.copyPartialMatches(args[1], arg, new ArrayList<>());
    }
}
