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

import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.PSPlayer;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.utils.LimitUtil;
import dev.espi.protectionstones.utils.UUIDCache;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArgBuySell implements PSCommandArg {
    @Override
    public List<String> getNames() {
        return Arrays.asList("buy", "sell");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public List<String> getPermissionsToExecute() {
        return Arrays.asList("protectionstones.buysell");
    }

    @Override
    public HashMap<String, Boolean> getRegisteredFlags() {
        return null;
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args, HashMap<String, String> flags) {
        Player p = (Player) s;
        if (!p.hasPermission("protectionstones.buysell")) {
            PSL.msg(p, PSL.NO_PERMISSION_BUYSELL.msg());
            return true;
        }

        if (!ProtectionStones.getInstance().isVaultSupportEnabled()) {
            Bukkit.getLogger().info(ChatColor.RED + "Vault is required, but is not enabled on this server. Contact an administrator.");
            s.sendMessage(ChatColor.RED + "Vault is required, but is not enabled on this server. Contact an administrator.");
            return true;
        }

        PSRegion r = PSRegion.fromLocationGroup(p.getLocation());
        if (r == null)
            return PSL.msg(p, PSL.NOT_IN_REGION.msg());

        if (args[0].equals("buy")) { // buying

            if (!r.forSale())
                return PSL.msg(p, PSL.BUY_NOT_FOR_SALE.msg());

            if ((!r.getTypeOptions().permission.equals("") && !p.hasPermission(r.getTypeOptions().permission)))
                return PSL.msg(p, PSL.NO_PERMISSION_REGION_TYPE.msg());

            // check if player reached region limit
            if (!LimitUtil.check(p, r.getTypeOptions()))
                return PSL.msg(p, PSL.REACHED_REGION_LIMIT.msg().replaceText(b -> b.matchLiteral("%limit%")
                        .replacement(String.valueOf(PSPlayer.fromPlayer(p).getGlobalRegionLimits()))));

            if (!PSPlayer.fromPlayer(p).hasAmount(r.getPrice()))
                return PSL.msg(p, PSL.NOT_ENOUGH_MONEY.replace("%price%", new DecimalFormat("#.##").format(r.getPrice())));

            PSL.msg(p, PSL.BUY_SOLD_BUYER.replaceAll(Map.of(
                    "%region%", (r.getName() == null ? r.getId() : r.getName()),
                    "%price%",  String.format("%.2f", r.getPrice()),
                    "%player%", UUIDCache.getNameFromUUID(r.getLandlord())
            )));

            if (Bukkit.getPlayer(r.getLandlord()) != null) {
                PSL.msg(
                        Bukkit.getPlayer(r.getLandlord()),
                        PSL.BUY_SOLD_SELLER.replaceAll(Map.of(
                                "%region%", (r.getName() == null ? r.getId() : r.getName()),
                                "%price%",  String.format("%.2f", r.getPrice()),
                                "%player%", p.getName()
                        ))
                );
            }

            r.sell(p.getUniqueId());

        } else if (args[0].equals("sell")) { // selling

            if (!r.isOwner(p.getUniqueId()))
                return PSL.msg(p, PSL.NOT_OWNER.msg());

            if (args.length != 2)
                return PSL.msg(p, PSL.SELL_HELP.msg());

            if (r.getRentStage() != PSRegion.RentStage.NOT_RENTING)
                return PSL.msg(p, PSL.SELL_RENTED_OUT.msg());

            if (args[1].equals("stop")) {
                r.setSellable(false, null, 0);
                PSL.msg(p, PSL.BUY_STOP_SELL.msg());
            } else {
                if (!NumberUtils.isNumber(args[1]))
                    return PSL.msg(p, PSL.SELL_HELP.msg());

                PSL.msg(p, PSL.SELL_FOR_SALE.replace("%price%", String.format("%.2f", Double.parseDouble(args[1]))));
                r.setSellable(true, p.getUniqueId(), Double.parseDouble(args[1]));
            }
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return null;
    }
}
