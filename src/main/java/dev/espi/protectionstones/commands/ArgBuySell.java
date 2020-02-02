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

import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.PSPlayer;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.utils.LimitUtil;
import dev.espi.protectionstones.utils.UUIDCache;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
                return PSL.msg(p, PSL.REACHED_REGION_LIMIT.msg().replace("%limit%", "" + PSPlayer.fromPlayer(p).getGlobalRegionLimits()));

            if (!PSPlayer.fromPlayer(p).hasAmount(r.getPrice()))
                return PSL.msg(p, PSL.NOT_ENOUGH_MONEY.msg().replace("%price%", new DecimalFormat("#.##").format(r.getPrice())));

            PSL.msg(p, PSL.BUY_SOLD_BUYER.msg()
                    .replace("%region%", r.getName() == null ? r.getID() : r.getName())
                    .replace("%price%", String.format("%.2f", r.getPrice()))
                    .replace("%player%", UUIDCache.uuidToName.get(r.getLandlord())));

            if (Bukkit.getPlayer(r.getLandlord()) != null) {
                PSL.msg(Bukkit.getPlayer(r.getLandlord()), PSL.BUY_SOLD_SELLER.msg()
                        .replace("%region%", r.getName() == null ? r.getID() : r.getName())
                        .replace("%price%", String.format("%.2f", r.getPrice()))
                        .replace("%player%", p.getName()));
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

                PSL.msg(p, PSL.SELL_FOR_SALE.msg().replace("%price%", String.format("%.2f", Double.parseDouble(args[1]))));
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
