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

import dev.espi.protectionstones.PSConfig;
import dev.espi.protectionstones.PSProtectBlock;
import dev.espi.protectionstones.ProtectionStones;

import java.time.Duration;
import java.util.List;

public class ConfigPlaceholders {

    private static PSConfig getConf() {
        return ProtectionStones.getInstance().getConfigOptions();
    }

    static String resolveConfig(String identifier) {
        String[] spl = identifier.split("-");
        if (spl.length > 1) {
            if (spl[1].equals("block")) { // config-block-[alias]-...
                if (spl.length > 3 && ProtectionStones.getProtectBlockFromAlias(spl[2]) != null) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 3; i < spl.length; i++) sb.append(spl[i]).append("-");
                    return resolveBlockConfig(ProtectionStones.getProtectBlockFromAlias(spl[2]), sb.toString().substring(0, sb.length()-1));
                }
            } else { // config-...
                return resolveGlobalConfig(identifier);
            }
        }
        return null;
    }

    private static String resolveGlobalConfig(String identifier) {
        StringBuilder sb = new StringBuilder();
        switch (identifier) {
            case "config-config-version":
                return getConf().configVersion + "";
            case "config-uuidupdated":
                return getConf().uuidupdated + "";
            case "config-placing-cooldown":
                return getConf().placingCooldown + "";
            case "config-async-load-uuid-cache":
                return getConf().asyncLoadUUIDCache + "";
            case "config-allow-duplicate-region-names":
                return getConf().allowDuplicateRegionNames + "";
            case "config-ps-view-cooldown":
                return getConf().psViewCooldown + "";
            case "config-ps-base-command":
                return getConf().base_command;
            case "config-aliases": // comma separated list
                for (int i = 0; i < getConf().aliases.size(); i++) {
                    sb.append(getConf().aliases.get(i)).append(i == getConf().aliases.size()-1 ? "" : ", ");
                }
                return sb.toString();
            case "config-drop-item-when-inventory-full":
                return getConf().dropItemWhenInventoryFull + "";
            case "config-regions-must-be-adjacent":
                return getConf().regionsMustBeAdjacent + "";
            case "config-allow-merging-regions":
                return getConf().allowMergingRegions + "";
            case "config-allow-merging-holes":
                return getConf().allowMergingHoles + "";

            case "config-economy-max-rent-price":
                return String.format("%.2f", getConf().maxRentPrice);
            case "config-economy-min-rent-price":
                return String.format("%.2f", getConf().minRentPrice);
            case "config-economy-max-rent-period":
                return getConf().maxRentPeriod + "";
            case "config-economy-max-rent-period-pretty":
                return describeDuration(Duration.ofSeconds(getConf().maxRentPeriod));
            case "config-economy-min-rent-period":
                return getConf().minRentPeriod + "";
            case "config-economy-min-rent-period-pretty":
                return describeDuration(Duration.ofSeconds(getConf().minRentPeriod));
            case "config-economy-tax-enabled":
                return getConf().taxEnabled + "";
            case "config-economy-tax-message-on-join":
                return getConf().taxMessageOnJoin + "";
        }
        return null;
    }

    private static String resolveBlockConfig(PSProtectBlock b, String identifier) {
        StringBuilder sb = new StringBuilder();
        switch (identifier) {
            case "type":
                return b.type;
            case "alias":
                return b.alias;
            case "restrict-obtaining":
                return b.restrictObtaining + "";
            case "world-list-type":
                return b.worldListType;
            case "worlds": // comma separated list
                for (int i = 0; i < b.worlds.size(); i++) {
                    sb.append(b.worlds.get(i)).append(i == b.worlds.size()-1 ? "" : " ");
                }
                return sb.toString();
            case "prevent-block-place-in-restricted-world":
                return b.preventBlockPlaceInRestrictedWorld + "";

            case "region-distance-between-claims":
                return b.distanceBetweenClaims + "";
            case "region-x-radius":
                return b.xRadius + "";
            case "region-y-radius":
                return b.yRadius + "";
            case "region-z-radius":
                return b.zRadius + "";
            case "region-x-offset":
                return b.xOffset + "";
            case "region-y-offset":
                return b.yOffset + "";
            case "region-z-offset":
                return b.zOffset + "";
            case "region-home-x-offset":
                return b.homeXOffset + "";
            case "region-home-y-offset":
                return b.homeYOffset + "";
            case "region-home-z-offset":
                return b.homeZOffset + "";
            case "region-flags": // comma separated list
                for (int i = 0; i < b.flags.size(); i++) {
                    sb.append(b.flags.get(i)).append(i == b.flags.size()-1 ? "" : ", ");
                }
                return sb.toString();
            case "region-allowed-flags": // comma separated list
                for (int i = 0; i < b.allowedFlagsRaw.size(); i++) {
                    sb.append(b.allowedFlagsRaw.get(i)).append(i == b.flags.size()-1 ? "" : ", ");
                }
                return sb.toString();
            case "region-hidden-flags-from-info": // comma separated list
                for (int i = 0; i < b.hiddenFlagsFromInfo.size(); i++) {
                    sb.append(b.hiddenFlagsFromInfo.get(i)).append(i == b.flags.size()-1 ? "" : ", ");
                }
                return sb.toString();
            case "region-priority":
                return b.priority + "";
            case "region-allow-overlap-unowned-regions":
                return b.allowOverlapUnownedRegions + "";
            case "region-allow-other-regions-to-overlap":
                return b.allowOtherRegionsToOverlap;
            case "region-allow-merging":
                return b.allowMerging + "";

            case "block-data-display-name":
                return b.displayName;
            case "block-data-lore": // \n separated list
                for (int i = 0; i < b.lore.size(); i++) {
                    sb.append(b.lore.get(i)).append("\n");
                }
                return sb.toString();
            case "block-data-price":
                return String.format("%.2f", b.price);
            case "block-data-allow-craft-with-custom-recipe":
                return b.allowCraftWithCustomRecipe + "";
            case "block-data-custom-recipe":
                for (List<String> l : b.customRecipe) {
                    for (String s : l) {
                        sb.append(s).append(" ");
                    }
                    sb.append("\n");
                }
                return sb.toString();
            case "block-data-recipe-amount":
                return b.recipeAmount + "";

            case "economy-tax-amount":
                return String.format("%.2f", b.taxAmount);
            case "economy-tax-period":
                return b.taxPeriod + "";
            case "economy-tax-period-pretty":
                return describeDuration(Duration.ofSeconds(b.taxPeriod));
            case "economy-tax-payment-time":
                return b.taxPaymentTime + "";
            case "economy-tax-payment-time-pretty":
                return describeDuration(Duration.ofSeconds(b.taxPaymentTime));
            case "economy-start-with-tax-autopay":
                return b.startWithTaxAutopay + "";

            case "behaviour-auto-hide":
                return b.autoHide + "";
            case "behaviour-auto-merge":
                return b.autoMerge + "";
            case "behaviour-no-drop":
                return b.noDrop + "";
            case "behaviour-prevent-piston-push":
                return b.preventPistonPush + "";
            case "behaviour-prevent-explode":
                return b.preventExplode + "";
            case "behaviour-destroy-region-when-explode":
                return b.destroyRegionWhenExplode + "";
            case "behaviour-prevent-silk-touch":
                return b.preventSilkTouch + "";
            case "behaviour-cost-to-place":
                return String.format("%.2f", b.costToPlace);

            case "player-allow-shift-right-break":
                return b.allowShiftRightBreak + "";
            case "player-prevent-teleport-in":
                return b.preventTeleportIn + "";
            case "player-no-moving-when-tp-waiting":
                return b.noMovingWhenTeleportWaiting + "";
            case "player-tp-waiting-seconds":
                return b.tpWaitingSeconds + "";
            case "player-prevent-ps-get":
                return b.preventPsGet + "";
            case "player-permission":
                return b.permission;

            case "event-enable":
                return b.eventsEnabled + "";
            case "event-on-region-create":
                for (int i = 0; i < b.regionCreateCommands.size(); i++) {
                    sb.append(b.regionCreateCommands.get(i)).append(i == b.flags.size()-1 ? "" : ", ");
                }
                return sb.toString();
            case "event-on-region-destroy":
                for (int i = 0; i < b.regionDestroyCommands.size(); i++) {
                    sb.append(b.regionDestroyCommands.get(i)).append(i == b.flags.size()-1 ? "" : ", ");
                }
                return sb.toString();
        }
        return null;
    }

    private static String describeDuration(Duration duration) {

    }
}
