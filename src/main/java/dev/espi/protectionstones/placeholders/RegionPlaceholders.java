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

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
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
        String[] spl = identifier.split("-");
        if (spl.length > 2) {
            String regionIdentifier = spl[2];
            List<PSRegion> r;
            if (p == null) {
                r = new ArrayList<>();
                WGUtils.getAllRegionManagers().forEach((w, rgm) -> r.addAll(ProtectionStones.getPSRegions(w, regionIdentifier)));
            } else {
                r = ProtectionStones.getPSRegions(p.getWorld(), regionIdentifier);
            }

            if (!r.isEmpty()) {
                if (spl[3].equals("config")) {
                    return ConfigPlaceholders.resolveBlockConfig(r.get(0).getTypeOptions(), identifier.substring(("region-" + regionIdentifier + "-config-").length()));
                } else {
                    return resolveRegionPlaceholders(p, r.get(0), identifier.substring(("region-" + regionIdentifier + "-").length()));
                }
            }
        }
        return "";
    }

    public static String resolveCurrentRegionPlaceholders(Player p, String identifier) {
        if (p == null) return "";
        PSRegion r = PSRegion.fromLocationGroup(p.getLocation());
        if (r == null) return "";

        if (identifier.startsWith("currentregion-config-")) { // config options for current region
            return ConfigPlaceholders.resolveBlockConfig(r.getTypeOptions(), identifier.substring("currentregion-config-".length()));
        } else { // current region placeholders
            return resolveRegionPlaceholders(p, r, identifier.substring("currentregion-".length())); // cut out "currentregion-"
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
        } else if (identifier.equals("is-hidden")) {
            return r.isHidden() + "";
        } else if (identifier.equals("home-location")) {
            return String.format("%.2f %.2f %.2f", r.getHome().getX(), r.getHome().getY(), r.getHome().getZ());
        } else if (identifier.equals("is-for-sale")) {
            return r.forSale() + "";
        } else if (identifier.equals("rent-stage")) {
            return r.getRentStage().toString().toLowerCase();
        } else if (identifier.equals("landlord")) {
            return r.getLandlord() == null ? "" : UUIDCache.getNameFromUUID(r.getLandlord());
        } else if (identifier.equals("tenant")) {
            return r.getTenant() == null ? "" : UUIDCache.getNameFromUUID(r.getTenant());
        } else if (identifier.equals("rent-period")) {
            return r.getRentPeriod();
        } else if (identifier.equals("sale-price")) {
            return r.getPrice() + "";
        } else if (identifier.equals("rent-amount")) {
            return r.getPrice() + "";
        } else if (identifier.equals("tax-owed")) {
            return r.getTaxPaymentsDue().stream().mapToLong(tp -> (long) tp.getAmount()).sum() + "";
        } else if (identifier.equals("tax-autopayer")) {
            return r.getTaxAutopayer() == null ? "" : UUIDCache.getNameFromUUID(r.getTaxAutopayer());
        } else if (identifier.equals("flags")) {
            List<String> flags = new ArrayList<>();
            for (Flag<?> f : r.getWGRegion().getFlags().keySet()) {
                if (!r.getTypeOptions().hiddenFlagsFromInfo.contains(f.getName())) {
                    flags.add(r.getName() + " " + r.getWGRegion().getFlag(f));
                }
            }
            return MiscUtil.concatWithoutLast(flags, ", ");
        } else if (identifier.startsWith("flags-")) {
            String[] spl = identifier.split("-");
            if (spl.length > 1) {
                Flag<?> f = Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), spl[1]);
                if (r.getWGRegion().getFlag(f) != null) {
                    return r.getWGRegion().getFlag(f).toString();
                }
            }
        }

        return "";
    }
}
