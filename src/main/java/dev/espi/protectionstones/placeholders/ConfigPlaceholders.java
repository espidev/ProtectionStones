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
import dev.espi.protectionstones.utils.MiscUtil;

import java.time.Duration;
import java.util.List;

class ConfigPlaceholders {

    private static PSConfig getConf() {
        return ProtectionStones.getInstance().getConfigOptions();
    }

    static String resolveConfig(String identifier) {
        String[] spl = identifier.split("_");
        if (spl.length > 1) {
            if (spl[1].equals("block")) { // config_block_[alias]_...
                if (spl.length > 3 && ProtectionStones.getProtectBlockFromAlias(spl[2]) != null) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 3; i < spl.length; i++)
                        sb.append(spl[i]).append(i == spl.length-1 ? "" : "_");

                    return resolveBlockConfig(ProtectionStones.getProtectBlockFromAlias(spl[2]), sb.toString());
                }
            } else { // config_...
                return resolveGlobalConfig(identifier);
            }
        }
        return "";
    }

    private static String resolveGlobalConfig(String identifier) {
        StringBuilder sb = new StringBuilder();
        switch (identifier) {
            case "config_config_version":
                return getConf().configVersion + "";
            case "config_uuidupdated":
                return getConf().uuidupdated + "";
            case "config_region_negative_min_max_updated":
                return getConf().regionNegativeMinMaxUpdated + "";
            case "config_placing_cooldown":
                return getConf().placingCooldown + "";
            case "config_async_load_uuid_cache":
                return getConf().asyncLoadUUIDCache + "";
            case "config_allow_duplicate_region_names":
                return getConf().allowDuplicateRegionNames + "";
            case "config_ps_view_cooldown":
                return getConf().psViewCooldown + "";
            case "config_ps_base_command":
                return getConf().base_command;
            case "config_aliases": // comma separated list
                for (int i = 0; i < getConf().aliases.size(); i++) {
                    sb.append(getConf().aliases.get(i)).append(i == getConf().aliases.size()-1 ? "" : ", ");
                }
                return sb.toString();
            case "config_drop_item_when_inventory_full":
                return getConf().dropItemWhenInventoryFull + "";
            case "config_regions_must_be_adjacent":
                return getConf().regionsMustBeAdjacent + "";
            case "config_allow_merging_regions":
                return getConf().allowMergingRegions + "";
            case "config_allow_merging_holes":
                return getConf().allowMergingHoles + "";
            case "default_protection_block_placement_off":
                return getConf().defaultProtectionBlockPlacementOff + "";
            case "allow_addowner_for_offline_players_without_lp":
                return getConf().allowAddownerForOfflinePlayersWithoutLp + "";
            case "allow_home_teleport_for_members":
                return getConf().allowHomeTeleportForMembers + "";

            case "admin_cleanup_delete_regions_with_members_but_no_owners":
                return getConf().cleanupDeleteRegionsWithMembersButNoOwners + "";

            case "config_economy_max_rent_price":
                return String.format("%.2f", getConf().maxRentPrice);
            case "config_economy_min_rent_price":
                return String.format("%.2f", getConf().minRentPrice);
            case "config_economy_max_rent_period":
                return getConf().maxRentPeriod + "";
            case "config_economy_max_rent_period_pretty":
                return MiscUtil.describeDuration(Duration.ofSeconds(getConf().maxRentPeriod));
            case "config_economy_min_rent_period":
                return getConf().minRentPeriod + "";
            case "config_economy_min_rent_period_pretty":
                return MiscUtil.describeDuration(Duration.ofSeconds(getConf().minRentPeriod));
            case "config_economy_tax_enabled":
                return getConf().taxEnabled + "";
            case "config_economy_tax_message_on_join":
                return getConf().taxMessageOnJoin + "";
        }
        return "";
    }

    static String resolveBlockConfig(PSProtectBlock b, String identifier) {
        StringBuilder sb = new StringBuilder();
        switch (identifier) {
            case "type":
                return b.type;
            case "alias":
                return b.alias;
            case "description":
                return b.description;
            case "restrict_obtaining":
                return b.restrictObtaining + "";
            case "world_list_type":
                return b.worldListType;
            case "worlds": // comma separated list
                return MiscUtil.concatWithoutLast(b.worlds, " ");
            case "prevent_block_place_in_restricted_world":
                return b.preventBlockPlaceInRestrictedWorld + "";

            case "region_distance_between_claims":
                return b.distanceBetweenClaims + "";
            case "region_x_radius":
                return b.xRadius + "";
            case "region_y_radius":
                return b.yRadius + "";
            case "region_z_radius":
                return b.zRadius + "";
            case "region_chunk_radius":
                return b.chunkRadius + "";
            case "region_home_x_offset":
                return b.homeXOffset + "";
            case "region_home_y_offset":
                return b.homeYOffset + "";
            case "region_home_z_offset":
                return b.homeZOffset + "";
            case "region_flags": // comma separated list
                return MiscUtil.concatWithoutLast(b.flags, ", ");
            case "region_allowed_flags": // comma separated list
                return MiscUtil.concatWithoutLast(b.allowedFlagsRaw, ", ");
            case "region_hidden_flags_from_info": // comma separated list
                return MiscUtil.concatWithoutLast(b.hiddenFlagsFromInfo, ", ");
            case "region_priority":
                return b.priority + "";
            case "region_allow_overlap_unowned_regions":
                return b.allowOverlapUnownedRegions + "";
            case "region_allow_other_regions_to_overlap":
                return b.allowOtherRegionsToOverlap;
            case "region_allow_merging":
                return b.allowMerging + "";
            case "region_allowed_merging_into_types":
                return MiscUtil.concatWithoutLast(b.allowedMergingIntoTypes, ", ");

            case "block_data_display_name":
                return b.displayName;
            case "block_data_lore": // \n separated list
                for (int i = 0; i < b.lore.size(); i++) {
                    sb.append(b.lore.get(i)).append("\n");
                }
                return sb.toString();
            case "block_data_price":
                return String.format("%.2f", b.price);
            case "block_data_allow_craft_with_custom_recipe":
                return b.allowCraftWithCustomRecipe + "";
            case "block_data_custom_recipe":
                for (List<String> l : b.customRecipe) {
                    for (String s : l) {
                        sb.append(s).append(" ");
                    }
                    sb.append("\n");
                }
                return sb.toString();
            case "block_data_recipe_amount":
                return b.recipeAmount + "";

            case "economy_tax_amount":
                return String.format("%.2f", b.taxAmount);
            case "economy_tax_period":
                return b.taxPeriod + "";
            case "economy_tax_period_pretty":
                return MiscUtil.describeDuration(Duration.ofSeconds(b.taxPeriod));
            case "economy_tax_payment_time":
                return b.taxPaymentTime + "";
            case "economy_tax_payment_time_pretty":
                return MiscUtil.describeDuration(Duration.ofSeconds(b.taxPaymentTime));
            case "economy_start_with_tax_autopay":
                return b.startWithTaxAutopay + "";
            case "economy_tenant_rent_role":
                return b.tenantRentRole;
            case "economy_landlord_still_owner":
                return b.landlordStillOwner + "";

            case "behaviour_auto_hide":
                return b.autoHide + "";
            case "behaviour_auto_merge":
                return b.autoMerge + "";
            case "behaviour_no_drop":
                return b.noDrop + "";
            case "behaviour_prevent_piston_push":
                return b.preventPistonPush + "";
            case "behaviour_prevent_explode":
                return b.preventExplode + "";
            case "behaviour_destroy_region_when_explode":
                return b.destroyRegionWhenExplode + "";
            case "behaviour_prevent_silk_touch":
                return b.preventSilkTouch + "";
            case "behaviour_cost_to_place":
                return String.format("%.2f", b.costToPlace);
            case "behaviour_allow_smelt_item":
                return b.allowSmeltItem + "";
            case "behaviour_allow_use_in_crafting":
                return b.allowUseInCrafting + "";

            case "player_allow_shift_right_break":
                return b.allowShiftRightBreak + "";
            case "player_prevent_teleport_in":
                return b.preventTeleportIn + "";
            case "player_no_moving_when_tp_waiting":
                return b.noMovingWhenTeleportWaiting + "";
            case "player_tp_waiting_seconds":
                return b.tpWaitingSeconds + "";
            case "player_prevent_ps_get":
                return b.preventPsGet + "";
            case "player_prevent_ps_home":
                return b.preventPsHome + "";
            case "player_permission":
                return b.permission;

            case "event_enable":
                return b.eventsEnabled + "";
            case "event_on_region_create":
                return MiscUtil.concatWithoutLast(b.regionCreateCommands, ", ");
            case "event_on_region_destroy":
                return MiscUtil.concatWithoutLast(b.regionDestroyCommands, ", ");
        }
        return "";
    }

}
