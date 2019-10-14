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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ArgRent implements PSCommandArg {

    static final String LEASE_HELP = ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps rent lease [price] [period]",
            UNLEASE_HELP = ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps rent unlease",
            RENT_HELP = ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps rent rent",
            UNRENT_HELP = ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps rent unrent";

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
        PSL.msg(s, UNLEASE_HELP);
        PSL.msg(s, RENT_HELP);
        PSL.msg(s, UNRENT_HELP);
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
            }

            switch (args[0]) {
                case "lease":
                    if (!r.isOwner(p.getUniqueId())) {

                        break;
                    }
                    if (r.getRentStage() != PSRegion.RentStage.NOT_RENTING) {
                        break;
                    }
                    if (args.length < 4) {
                        PSL.msg(p, LEASE_HELP);
                        break;
                    }
                    if (!NumberUtils.isNumber(args[2])) {
                        PSL.msg(p, LEASE_HELP);
                    }

                    String period = String.join(" ", Arrays.asList(args).subList(3, args.length));

                    r.setRentable(p.getUniqueId(), period, Double.parseDouble(args[2]));

                    break;
                case "unlease":
                    if (r.getLandlord() == null || !r.getLandlord().equals(p.getUniqueId())) {
                        break;
                    }
                    break;
                case "rent":
                    break;
                case "unrent":
                    break;
            }
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        List<String> arg = Arrays.asList("lease", "unlease", "rent", "unrent");
        return args.length == 2 ? StringUtil.copyPartialMatches(args[1], arg, new ArrayList<>()) : null;
    }
}
