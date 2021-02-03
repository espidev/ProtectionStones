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
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.utils.Permissions;
import dev.espi.protectionstones.utils.WGUtils;
import lombok.var;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class PlayerPlaceholders {

    static String resolvePlayer(Player p, String identifier) {
        if (p == null) return "";
        PSPlayer psp = PSPlayer.fromPlayer(p);

        if (identifier.equals("currentplayer_global_region_limit")) {
            if (p.hasPermission(Permissions.ADMIN)) {
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

            if (p.hasPermission(Permissions.ADMIN)) {
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
                for (var tp : psr.getTaxPaymentsDue()) {
                    amount += tp.getAmount();
                }
            }
            return String.format("%.2f", amount);
        } else if (identifier.startsWith("currentplayer_num_of_owned_regions")) {
            List<PSRegion> regions = new ArrayList<>();
            WGUtils.getAllRegionManagers().forEach((w, rgm) -> rgm.getRegions().values().forEach(r -> {
                if (ProtectionStones.isPSRegion(r) && (r.getOwners().contains(psp.getUuid()))) {
                    regions.add(PSRegion.fromWGRegion(w, r));
                }
            }));
            return "" + regions.size();
        }
        return "";
    }

}
