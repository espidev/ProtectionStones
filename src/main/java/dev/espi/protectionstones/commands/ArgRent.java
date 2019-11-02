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

import dev.espi.protectionstones.PSEconomy;
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.utils.UUIDCache;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class ArgRent implements PSCommandArg {

    static final String LEASE_HELP = ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps rent lease [price] [period]",
            STOPLEASE_HELP = ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps rent stoplease",
            RENT_HELP = ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps rent rent",
            STOPRENTING_HELP = ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps rent stoprenting";

    @Override
    public List<String> getNames() {
        return Arrays.asList("rent");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public List<String> getPermissionsToExecute() {
        return Arrays.asList("protectionstones.rent");
    }

    @Override
    public HashMap<String, Boolean> getRegisteredFlags() {
        return null;
    }

    private void runHelp(CommandSender s) {
        PSL.msg(s, PSL.RENT_HELP_HEADER.msg());
        PSL.msg(s, LEASE_HELP);
        PSL.msg(s, STOPLEASE_HELP);
        PSL.msg(s, RENT_HELP);
        PSL.msg(s, STOPRENTING_HELP);
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args, HashMap<String, String> flags) {
        if (!s.hasPermission("protectionstones.rent")) {
            PSL.msg(s, PSL.NO_PERMISSION_RENT.msg());
            return true;
        }

        Player p = (Player) s;

        if (args.length == 1) {
            runHelp(s);
        } else {
            if (args[0].equals("help")) {
                runHelp(s);
                return true;
            }

            PSRegion r = PSRegion.fromLocation(p.getLocation());

            if (r == null) {
                PSL.msg(p, PSL.NOT_IN_REGION.msg());
                return true;
            }

            switch (args[1]) {
                case "lease":
                    if (!r.isOwner(p.getUniqueId())) { // check if player is a region owner
                        PSL.msg(p, PSL.NOT_OWNER.msg());
                        break;
                    }
                    if (r.getRentStage() == PSRegion.RentStage.RENTING) { // check if already renting
                        PSL.msg(p, PSL.RENT_ALREADY_RENTING.msg());
                        break;
                    }
                    if (args.length < 4) {
                        PSL.msg(p, LEASE_HELP);
                        break;
                    }
                    if (!NumberUtils.isNumber(args[2])) { // check price
                        PSL.msg(p, LEASE_HELP);
                        break;
                    }
                    if (r.forSale()) {
                        PSL.msg(p, PSL.RENT_BEING_SOLD.msg());
                        break;
                    }

                    String period = String.join(" ", Arrays.asList(args).subList(3, args.length));
                    // TODO period check

                    r.setRentable(p.getUniqueId(), period, Double.parseDouble(args[2]));
                    PSL.msg(p, PSL.RENT_LEASE_SUCCESS.msg().replace("%price%", args[2]).replace("%period%", period));
                    break;

                case "stoplease":
                    if ((!r.isOwner(p.getUniqueId()) && r.getRentStage() != PSRegion.RentStage.RENTING) || (r.getLandlord() != null && !p.getUniqueId().equals(r.getLandlord()) && r.getRentStage() == PSRegion.RentStage.RENTING)) {
                        PSL.msg(p, PSL.NOT_OWNER.msg());
                        break;
                    }
                    if (r.getRentStage() == PSRegion.RentStage.NOT_RENTING) {
                        PSL.msg(p, PSL.RENT_NOT_RENTED.msg());
                        break;
                    }

                    UUID tenant = r.getTenant();
                    r.removeRenting();

                    PSL.msg(p, PSL.RENT_STOPPED.msg());
                    if (tenant != null) {
                        PSL.msg(p, PSL.RENT_EVICTED.msg().replace("%tenant%", UUIDCache.uuidToName.get(tenant)));
                    }
                    break;

                case "rent":
                    if (r.getRentStage() != PSRegion.RentStage.LOOKING_FOR_TENANT) {
                        PSL.msg(p, PSL.RENT_NOT_RENTING.msg());
                        break;
                    }
                    if (!ProtectionStones.getInstance().getVaultEconomy().has(p, r.getPrice())) {
                        PSL.msg(p, PSL.NOT_ENOUGH_MONEY.msg().replace("%price%", "" + r.getPrice()));
                        break;
                    }

                    r.rentOut(r.getLandlord(), p.getUniqueId(), r.getRentPeriod(), r.getPrice());
                    PSL.msg(p, PSL.RENT_RENTING_TENANT.msg()
                            .replace("%region%", r.getName() == null ? r.getID() : r.getName())
                            .replace("%price%", String.format("%.2f", r.getPrice()))
                            .replace("%period%", r.getRentPeriod()));

                    if (Bukkit.getPlayer(r.getLandlord()) != null) {
                        PSL.msg(Bukkit.getPlayer(r.getLandlord()), PSL.RENT_RENTING_LANDLORD.msg()
                                .replace("%player%", p.getName())
                                .replace("%region%", r.getName() == null ? r.getID() : r.getName()));
                    }
                    PSEconomy.doRentPayment(r);

                    break;

                case "stoprenting":
                    if (r.getTenant() == null || !r.getTenant().equals(p.getUniqueId())) {
                        PSL.msg(p, PSL.RENT_NOT_TENANT.msg());
                        break;
                    }

                    r.setTenant(null);

                    PSL.msg(p, PSL.RENT_TENANT_STOPPED_TENANT.msg().replace("%region%", r.getName() == null ? r.getID() : r.getName()));
                    if (Bukkit.getPlayer(r.getLandlord()) != null) {
                        PSL.msg(Bukkit.getPlayer(r.getLandlord()), PSL.RENT_TENANT_STOPPED_LANDLORD.msg()
                                .replace("%player%", p.getName())
                                .replace("%region%", r.getName() == null ? r.getID() : r.getName()));
                    }

                    break;
            }
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        List<String> arg = Arrays.asList("lease", "stoplease", "rent", "stoprenting");
        if (args.length == 3 && args[1].equals("lease")) {
            return StringUtil.copyPartialMatches(args[2], Arrays.asList("100"), new ArrayList<>());
        } else if (args.length == 4 && args[1].equals("lease")) {
            return StringUtil.copyPartialMatches(args[3], Arrays.asList("1w", "1d", "1h"), new ArrayList<>());
        }
        return args.length == 2 ? StringUtil.copyPartialMatches(args[1], arg, new ArrayList<>()) : null;
    }
}
