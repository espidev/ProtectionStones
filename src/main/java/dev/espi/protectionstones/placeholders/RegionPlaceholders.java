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

import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.utils.MiscUtil;
import dev.espi.protectionstones.utils.UUIDCache;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class RegionPlaceholders {

    public static String resolveSpecifiedRegionPlaceholders(Player p, String identifier) {
        String[] spl = identifier.split("_");
        if (spl.length > 2) {
            String regionIdentifier = spl[1];
            List<PSRegion> r;
            if (p == null) {
                r = new ArrayList<>();
                WGUtils.getAllRegionManagers().forEach((w, rgm) -> r.addAll(ProtectionStones.getPSRegions(w, regionIdentifier)));
            } else {
                r = ProtectionStones.getPSRegions(p.getWorld(), regionIdentifier);
            }

            if (!r.isEmpty()) {
                if (spl[2].equals("config")) {
                    return ConfigPlaceholders.resolveBlockConfig(r.get(0).getTypeOptions(), identifier.substring(("region_" + regionIdentifier + "_config_").length()));
                } else {
                    return resolveRegionPlaceholders(p, r.get(0), identifier.substring(("region_" + regionIdentifier + "_").length()));
                }
            }
        }
        return "";
    }

    public static String resolveCurrentRegionPlaceholders(Player p, String identifier) {
        if (p == null) return "";
        PSRegion r = PSRegion.fromLocationGroup(p.getLocation());
        if (r == null) return "";

        if (identifier.startsWith("currentregion_config_")) { // config options for current region
            return ConfigPlaceholders.resolveBlockConfig(r.getTypeOptions(), identifier.substring("currentregion_config_".length()));
        } else { // current region placeholders
            return resolveRegionPlaceholders(p, r, identifier.substring("currentregion_".length())); // cut out "currentregion_"
        }
    }

    public static String resolveRegionPlaceholders(Player p, PSRegion r, String identifier) {
        if (identifier.equals("owners")) {
            return MiscUtil.concatWithoutLast(r.getOwners().stream().map(UUIDCache::getNameFromUUID).collect(Collectors.toList()), ", ");
        } else if (identifier.equals("members")) {
            return MiscUtil.concatWithoutLast(r.getMembers().stream().map(UUIDCache::getNameFromUUID).collect(Collectors.toList()), ", ");
        } else if (identifier.equals("name")) {
            return r.getName() == null ? r.getId() : r.getName();
        } else if (identifier.equals("id")) {
            return r.getId();
        } else if (identifier.equals("type")) {
            return r.getType();
        } else if (identifier.equals("alias")) {
            return r.getTypeOptions().alias;
        } else if (identifier.equals("is_hidden")) {
            return r.isHidden() + "";
        } else if (identifier.equals("home_location")) {
            return String.format("%.1f %.1f %.1f", r.getHome().getX(), r.getHome().getY(), r.getHome().getZ());
        } else if (identifier.equals("is_for_sale")) {
            return r.forSale() + "";
        } else if (identifier.equals("rent_stage")) {
            return r.getRentStage().toString().toLowerCase();
        } else if (identifier.equals("landlord")) {
            return r.getLandlord() == null ? "" : UUIDCache.getNameFromUUID(r.getLandlord());
        } else if (identifier.equals("tenant")) {
            return r.getTenant() == null ? "" : UUIDCache.getNameFromUUID(r.getTenant());
        } else if (identifier.equals("rent_period")) {
            return r.getRentPeriod();
        } else if (identifier.equals("sale_price")) {
            return r.getPrice() + "";
        } else if (identifier.equals("rent_amount")) {
            return r.getPrice() + "";
        } else if (identifier.equals("tax_owed")) {
            return String.format("%.2f", r.getTaxPaymentsDue().stream().mapToDouble(PSRegion.TaxPayment::getAmount).sum());
        } else if (identifier.equals("tax_autopayer")) {
            return r.getTaxAutopayer() == null ? "" : UUIDCache.getNameFromUUID(r.getTaxAutopayer());
        } else if (identifier.equals("flags")) {
            List<String> flags = new ArrayList<>();
            for (Flag<?> f : r.getWGRegion().getFlags().keySet()) {
                if (!r.getTypeOptions().hiddenFlagsFromInfo.contains(f.getName())) {
                    flags.add(f.getName() + " " + r.getWGRegion().getFlag(f) + "&r");
                }
            }
            return MiscUtil.concatWithoutLast(flags, ", ");
        } else if (identifier.startsWith("flags_")) {
            String[] spl = identifier.split("_");
            if (spl.length > 1) {
                Flag<?> f = DefaultFlag.fuzzyMatchFlag(WGUtils.getFlagRegistry(), spl[1]);
                if (r.getWGRegion().getFlag(f) != null) {
                    return r.getWGRegion().getFlag(f).toString();
                }
            }
        }

        return "";
    }
}
