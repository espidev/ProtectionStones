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
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.utils.LimitUtil;
import dev.espi.protectionstones.utils.UUIDCache;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

        PSRegion r = PSRegion.fromLocation(p.getLocation());
        if (r == null) {
            PSL.msg(p, PSL.NOT_IN_REGION.msg());
            return true;
        }

        if (args[0].equals("buy")) { // buying

            if (!r.forSale()) {
                PSL.msg(p, PSL.BUY_NOT_FOR_SALE.msg());
                return true;
            }

            if ((!r.getTypeOptions().permission.equals("") && !p.hasPermission(r.getTypeOptions().permission))) {
                PSL.msg(p, PSL.NO_PERMISSION_REGION_TYPE.msg());
                return true;
            }

            // check if player reached region limit
            if (!LimitUtil.check(p, r.getTypeOptions())) {
                return true;
            }

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

            if (!r.isOwner(p.getUniqueId())) {
                PSL.msg(p, PSL.NOT_OWNER.msg());
                return true;
            }
            if (args.length != 2) {
                PSL.msg(p, PSL.SELL_HELP.msg());
                return true;
            }
            if (r.getRentStage() != PSRegion.RentStage.NOT_RENTING) {
                PSL.msg(p, PSL.SELL_RENTED_OUT.msg());
                return true;
            }

            if (args[1].equals("stop")) {
                r.setSellable(false, null, 0);
                PSL.msg(p, PSL.BUY_STOP_SELL.msg());
            } else {
                if (!NumberUtils.isNumber(args[1])) {
                    PSL.msg(p, PSL.SELL_HELP.msg());
                    return true;
                }

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
