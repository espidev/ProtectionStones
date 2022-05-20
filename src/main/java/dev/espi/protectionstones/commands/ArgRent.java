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

import dev.espi.protectionstones.PSEconomy;
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.utils.LimitUtil;
import dev.espi.protectionstones.utils.MiscUtil;
import dev.espi.protectionstones.utils.UUIDCache;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.time.Duration;
import java.util.*;

public class ArgRent implements PSCommandArg {

    public static String getLeaseHelp() {
        return ChatColor.AQUA + "> " + ChatColor.GRAY + "/" + ProtectionStones.getInstance().getConfigOptions().base_command +
                " rent lease [price] [period]";
    }

    public static String getStopLeaseHelp() {
        return ChatColor.AQUA + "> " + ChatColor.GRAY + "/" + ProtectionStones.getInstance().getConfigOptions().base_command +
                " rent stoplease";
    }

    public static String getRentHelp() {
        return ChatColor.AQUA + "> " + ChatColor.GRAY + "/" + ProtectionStones.getInstance().getConfigOptions().base_command +
                " rent rent";
    }

    public static String getStopRentingHelp() {
        return ChatColor.AQUA + "> " + ChatColor.GRAY + "/" + ProtectionStones.getInstance().getConfigOptions().base_command +
                " rent stoprenting";
    }

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
        PSL.msg(s, getLeaseHelp());
        PSL.msg(s, getStopLeaseHelp());
        PSL.msg(s, getRentHelp());
        PSL.msg(s, getStopRentingHelp());
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args, HashMap<String, String> flags) {
        if (!s.hasPermission("protectionstones.rent")) {
            return PSL.msg(s, PSL.NO_PERMISSION_RENT.msg());
        }

        if (!ProtectionStones.getInstance().isVaultSupportEnabled()) {
            Bukkit.getLogger().info(ChatColor.RED + "Vault is required, but is not enabled on this server. Contact an administrator.");
            s.sendMessage(ChatColor.RED + "Vault is required, but is not enabled on this server. Contact an administrator.");
            return true;
        }

        Player p = (Player) s;

        if (args.length == 1) {
            runHelp(s);
        } else {
            if (args[1].equals("help")) {
                runHelp(s);
                return true;
            }

            PSRegion r = PSRegion.fromLocationGroup(p.getLocation());

            if (r == null) {
                return PSL.msg(p, PSL.NOT_IN_REGION.msg());
            }

            switch (args[1]) {
                case "lease":
                    if (!r.isOwner(p.getUniqueId())) // check if player is a region owner
                        return PSL.msg(p, PSL.NOT_OWNER.msg());

                    if (r.getRentStage() == PSRegion.RentStage.RENTING) // check if already renting
                        return PSL.msg(p, PSL.RENT_ALREADY_RENTING.msg());

                    if (args.length < 4)
                        return PSL.msg(p, getLeaseHelp());

                    if (!NumberUtils.isNumber(args[2])) // check price
                        return PSL.msg(p, getLeaseHelp());

                    if (r.forSale()) // if region is already being sold
                        return PSL.msg(p, PSL.RENT_BEING_SOLD.msg());

                    double price = Double.parseDouble(args[2]);
                    if (ProtectionStones.getInstance().getConfigOptions().minRentPrice != -1 && price < ProtectionStones.getInstance().getConfigOptions().minRentPrice) // if rent price is too low
                        return PSL.msg(p, PSL.RENT_PRICE_TOO_LOW.msg().replace("%price%", ""+ProtectionStones.getInstance().getConfigOptions().minRentPrice));

                    if (ProtectionStones.getInstance().getConfigOptions().maxRentPrice != -1 && price > ProtectionStones.getInstance().getConfigOptions().maxRentPrice) // if rent price is too high
                        return PSL.msg(p, PSL.RENT_PRICE_TOO_HIGH.msg().replace("%price%", ""+ProtectionStones.getInstance().getConfigOptions().maxRentPrice));

                    String period = String.join(" ", Arrays.asList(args).subList(3, args.length));

                    try {
                        Duration d = MiscUtil.parseRentPeriod(period);
                        if (ProtectionStones.getInstance().getConfigOptions().minRentPeriod != -1 && d.getSeconds() < ProtectionStones.getInstance().getConfigOptions().minRentPeriod) {
                            return PSL.msg(p, PSL.RENT_PERIOD_TOO_SHORT.msg().replace("%period%", ""+ProtectionStones.getInstance().getConfigOptions().minRentPeriod));
                        }
                        if (ProtectionStones.getInstance().getConfigOptions().maxRentPeriod != -1 && d.getSeconds() > ProtectionStones.getInstance().getConfigOptions().maxRentPeriod) {
                            return PSL.msg(p, PSL.RENT_PERIOD_TOO_LONG.msg().replace("%period%", ""+ProtectionStones.getInstance().getConfigOptions().maxRentPeriod));
                        }
                    } catch (NumberFormatException e) {
                        return PSL.msg(p, PSL.RENT_PERIOD_INVALID.msg());
                    }

                    r.setRentable(p.getUniqueId(), period, price);
                    return PSL.msg(p, PSL.RENT_LEASE_SUCCESS.msg().replace("%price%", args[2]).replace("%period%", period));

                case "stoplease":
                    if (r.getRentStage() == PSRegion.RentStage.NOT_RENTING)
                        return PSL.msg(p, PSL.RENT_NOT_RENTED.msg());

                    if (r.getTypeOptions().landlordStillOwner) {
                        // landlord can be any of the region's owner; doesn't really matter if tenant calls /ps rent stoplease
                        if (!r.isOwner(p.getUniqueId()))
                            return PSL.msg(p, PSL.NOT_OWNER.msg());
                    } else {
                        // landlord must be the specified landlord
                        if (r.getLandlord() != null && !p.getUniqueId().equals(r.getLandlord()))
                            return PSL.msg(p, PSL.NOT_OWNER.msg());
                    }

                    UUID tenant = r.getTenant();
                    r.removeRenting();

                    PSL.msg(p, PSL.RENT_STOPPED.msg());
                    if (tenant != null) {
                        PSL.msg(p, PSL.RENT_EVICTED.msg().replace("%tenant%", UUIDCache.getNameFromUUID(tenant)));
                        Player tenantPlayer = Bukkit.getPlayer(tenant);
                        if (tenantPlayer != null && tenantPlayer.isOnline()) {
                            PSL.msg(p, PSL.RENT_TENANT_STOPPED_TENANT.msg().replace("%region%", r.getName() == null ? r.getId() : r.getName()));
                        }
                    }
                    break;

                case "rent":
                    if (r.getRentStage() != PSRegion.RentStage.LOOKING_FOR_TENANT)
                        return PSL.msg(p, PSL.RENT_NOT_RENTING.msg());

                    if (!ProtectionStones.getInstance().getVaultEconomy().has(p, r.getPrice()))
                        return PSL.msg(p, PSL.NOT_ENOUGH_MONEY.msg().replace("%price%", String.format("%.2f", r.getPrice())));

                    if (r.getLandlord().equals(p.getUniqueId()))
                        return PSL.msg(p, PSL.RENT_CANNOT_RENT_OWN_REGION.msg());

                    if (LimitUtil.hasPassedOrEqualsRentLimit(p))
                        return PSL.msg(p, PSL.RENT_REACHED_LIMIT.msg());

                    r.rentOut(r.getLandlord(), p.getUniqueId(), r.getRentPeriod(), r.getPrice());
                    PSL.msg(p, PSL.RENT_RENTING_TENANT.msg()
                            .replace("%region%", r.getName() == null ? r.getId() : r.getName())
                            .replace("%price%", String.format("%.2f", r.getPrice()))
                            .replace("%period%", r.getRentPeriod()));

                    if (Bukkit.getPlayer(r.getLandlord()) != null) {
                        PSL.msg(Bukkit.getPlayer(r.getLandlord()), PSL.RENT_RENTING_LANDLORD.msg()
                                .replace("%player%", p.getName())
                                .replace("%region%", r.getName() == null ? r.getId() : r.getName()));
                    }
                    PSEconomy.doRentPayment(r);

                    break;

                case "stoprenting":
                    if (r.getTenant() == null || !r.getTenant().equals(p.getUniqueId()))
                        return PSL.msg(p, PSL.RENT_NOT_TENANT.msg());

                    r.removeOwner(r.getTenant());
                    r.removeMember(r.getTenant());
                    r.addOwner(r.getLandlord());

                    r.setTenant(null);

                    PSL.msg(p, PSL.RENT_TENANT_STOPPED_TENANT.msg().replace("%region%", r.getName() == null ? r.getId() : r.getName()));
                    if (Bukkit.getPlayer(r.getLandlord()) != null) {
                        PSL.msg(Bukkit.getPlayer(r.getLandlord()), PSL.RENT_TENANT_STOPPED_LANDLORD.msg()
                                .replace("%player%", p.getName())
                                .replace("%region%", r.getName() == null ? r.getId() : r.getName()));
                    }
                    break;
                default:
                    runHelp(s);
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
