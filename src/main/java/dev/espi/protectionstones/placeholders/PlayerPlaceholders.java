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

package dev.espi.protectionstones.placeholders;

import dev.espi.protectionstones.PSPlayer;
import dev.espi.protectionstones.PSProtectBlock;
import dev.espi.protectionstones.PSRegion;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class PlayerPlaceholders {

    static String resolvePlayer(Player p, String identifier) {
        if (p == null) return "";
        PSPlayer psp = PSPlayer.fromPlayer(p);

        if (identifier.equals("currentplayer_global_region_limit")) {

            if (p.hasPermission("protectionstones.admin")) {
                return "-1";
            } else {
                return psp.getGlobalRegionLimits() + "";
            }

        } else if (identifier.startsWith("currentplayer_region_limit_")) {

            String alias = identifier.substring("currentplayer_region_limit_".length());
            List<Map.Entry<PSProtectBlock, Integer>> l = psp.getRegionLimits()
                    .entrySet()
                    .stream()
                    .filter(e -> e.getKey().alias.equals(alias))
                    .collect(Collectors.toList());

            if (p.hasPermission("protectionstones.admin")) {
                return "-1";
            }

            if (!l.isEmpty()) {
                return l.get(0).getValue() + "";
            } else {
                return psp.getGlobalRegionLimits() + "";
            }

        } else if (identifier.startsWith("currentplayer_total_tax_owed")) {

            double amount = 0;
            for (PSRegion psr : psp.getTaxEligibleRegions()) {
                for (PSRegion.TaxPayment tp : psr.getTaxPaymentsDue()) {
                    amount += tp.getAmount();
                }
            }
            return String.format("%.2f", amount);

        } else if (identifier.startsWith("currentplayer_num_of_accessible_regions_")) {

            String world = identifier.substring("currentplayer_num_of_accessible_regions_".length());
            World w = Bukkit.getWorld(world);
            return w == null ? "Invalid world." : "" + psp.getPSRegions(w, true).size();

        } else if (identifier.startsWith("currentplayer_num_of_accessible_regions")) {

            return "" + Bukkit.getWorlds().stream().mapToInt(w -> psp.getPSRegions(w, true).size()).sum();

        } else if (identifier.startsWith("currentplayer_num_of_owned_regions_")) {

            String world = identifier.substring("currentplayer_num_of_owned_regions_".length());
            World w = Bukkit.getWorld(world);
            return w == null ? "Invalid world." : "" + psp.getPSRegions(w, false).size();

        } else if (identifier.startsWith("currentplayer_num_of_owned_regions")) {

            return "" + Bukkit.getWorlds().stream().mapToInt(w -> psp.getPSRegions(w, false).size()).sum();

        } else if (identifier.startsWith("currentplayer_owned_regions_ids_")) {

            World w = Bukkit.getWorld(identifier.substring("currentplayer_owned_regions_ids_".length()));
            return w == null ? "Invalid world." : getRegionsString(psp.getPSRegions(w, false), false);

        } else if (identifier.startsWith("currentplayer_accessible_regions_ids_")) {

            World w = Bukkit.getWorld(identifier.substring("currentplayer_accessible_regions_ids_".length()));
            return w == null ? "Invalid world." : getRegionsString(psp.getPSRegions(w, true), false);

        } else if (identifier.startsWith("currentplayer_owned_regions_names_")) {

            World w = Bukkit.getWorld(identifier.substring("currentplayer_owned_regions_names_".length()));
            return w == null ? "Invalid world." : getRegionsString(psp.getPSRegions(w, false), true);

        } else if (identifier.startsWith("currentplayer_accessible_regions_names_")) {

            World w = Bukkit.getWorld(identifier.substring("currentplayer_accessible_regions_names_".length()));
            return w == null ? "Invalid world." : getRegionsString(psp.getPSRegions(w, true), true);

        }
        return "";
    }

    private static String getRegionsString(List<PSRegion> regions, boolean useNamesIfPossible) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < regions.size(); i++) {

            if (useNamesIfPossible && regions.get(i).getName() != null) {
                sb.append(regions.get(i).getName());
            } else {
                sb.append(regions.get(i).getId());
            }

            if (i < regions.size() - 1) {
                sb.append(", ");
            }

        }
        return sb.toString();
    }

}
