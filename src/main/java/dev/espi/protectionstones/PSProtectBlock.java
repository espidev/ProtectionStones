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

package dev.espi.protectionstones;

import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.SpecDoubleInRange;
import com.electronwill.nightconfig.core.conversion.SpecIntInRange;
import com.sk89q.worldguard.protection.flags.Flag;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Object to represent a protection block as defined in config (blocks folder). The fields are the exact same as
 * the ones in the config.
 */

public class PSProtectBlock {

    // Annotations are for types that have names that aren't the same as the config name
    // Check here for help: https://github.com/TheElectronWill/Night-Config

    // main section
    public String type, alias;
    @Path("description")
    public String description;
    @Path("restrict_obtaining")
    public boolean restrictObtaining;
    @Path("world_list_type")
    public String worldListType;
    @Path("worlds")
    public List<String> worlds;
    @Path("prevent_block_place_in_restricted_world")
    public boolean preventBlockPlaceInRestrictedWorld;
    @Path("allow_placing_in_wild")
    public boolean allowPlacingInWild;
    @Path("placing_bypasses_wg_passthrough")
    public Boolean placingBypassesWGPassthrough;

    // region section
    @Path("region.distance_between_claims")
    public int distanceBetweenClaims;
    @Path("region.x_radius")
    @SpecIntInRange(min = 0, max = Integer.MAX_VALUE)
    public int xRadius;
    @Path("region.y_radius")
    @SpecIntInRange(min = -1, max = Integer.MAX_VALUE)
    public int yRadius;
    @Path("region.z_radius")
    @SpecIntInRange(min = 0, max = Integer.MAX_VALUE)
    public int zRadius;
    @Path("region.chunk_radius")
    @SpecIntInRange(min = -1, max = Integer.MAX_VALUE)
    public int chunkRadius;
    @Path("region.home_x_offset")
    public double homeXOffset;
    @Path("region.home_y_offset")
    public double homeYOffset;
    @Path("region.home_z_offset")
    public double homeZOffset;
    @Path("region.flags")
    public List<String> flags;
    @Path("region.allowed_flags")
    public List<String> allowedFlagsRaw;
    @Path("region.hidden_flags_from_info")
    public List<String> hiddenFlagsFromInfo;
    @Path("region.priority")
    public int priority;
    @Path("region.allow_overlap_unowned_regions")
    public boolean allowOverlapUnownedRegions;
    @Path("region.allow_other_regions_to_overlap")
    public String allowOtherRegionsToOverlap;
    @Path("region.allow_merging")
    public boolean allowMerging;
    @Path("region.allowed_merging_into_types")
    public List<String> allowedMergingIntoTypes;

    // block data section
    @Path("block_data.display_name")
    public String displayName;
    @Path("block_data.lore")
    public List<String> lore;
    @Path("block_data.enchanted_effect")
    public boolean enchantedEffect;
    @Path("block_data.price")
    @SpecDoubleInRange(min = 0.0, max = Double.MAX_VALUE)
    public double price;
    @Path("block_data.allow_craft_with_custom_recipe")
    public boolean allowCraftWithCustomRecipe;
    @Path("block_data.custom_recipe")
    public List<List<String>> customRecipe;
    @Path("block_data.recipe_amount")
    @SpecIntInRange(min = 0, max = 64)
    public int recipeAmount;
    @Path("block_data.custom_model_data")
    public int customModelData;

    // economy section
    @Path("economy.tax_amount")
    public double taxAmount;
    @Path("economy.tax_period")
    @SpecIntInRange(min = -1, max = Integer.MAX_VALUE)
    public int taxPeriod;
    @Path("economy.tax_payment_time")
    @SpecIntInRange(min = 1, max = Integer.MAX_VALUE)
    public int taxPaymentTime;
    @Path("economy.start_with_tax_autopay")
    public boolean startWithTaxAutopay;
    @Path("economy.tenant_rent_role")
    public String tenantRentRole;
    @Path("economy.landlord_still_owner")
    public boolean landlordStillOwner;

    // behaviour section
    @Path("behaviour.auto_hide")
    public boolean autoHide;
    @Path("behaviour.auto_merge")
    public boolean autoMerge;
    @Path("behaviour.no_drop")
    public boolean noDrop;
    @Path("behaviour.prevent_piston_push")
    public boolean preventPistonPush;
    @Path("behaviour.prevent_explode")
    public boolean preventExplode;
    @Path("behaviour.destroy_region_when_explode")
    public boolean destroyRegionWhenExplode;
    @Path("behaviour.prevent_silk_touch")
    public boolean preventSilkTouch;
    @Path("behaviour.cost_to_place")
    @SpecDoubleInRange(min = 0.0, max = Double.MAX_VALUE)
    public double costToPlace;
    @Path("behaviour.allow_smelt_item")
    public boolean allowSmeltItem;
    @Path("behaviour.allow_use_in_crafting")
    public boolean allowUseInCrafting;

    // player section
    @Path("player.allow_shift_right_break")
    public boolean allowShiftRightBreak;
    @Path("player.prevent_teleport_in")
    public boolean preventTeleportIn;
    @Path("player.no_moving_when_tp_waiting")
    public boolean noMovingWhenTeleportWaiting;
    @Path("player.tp_waiting_seconds")
    @SpecIntInRange(min = 0, max = Integer.MAX_VALUE)
    public int tpWaitingSeconds;
    @Path("player.prevent_ps_get")
    public boolean preventPsGet;
    @Path("player.prevent_ps_home")
    public boolean preventPsHome;
    @Path("player.permission")
    public String permission;

    // event section
    @Path("event.enable")
    public boolean eventsEnabled;
    @Path("event.on_region_create")
    public List<String> regionCreateCommands;
    @Path("event.on_region_destroy")
    public List<String> regionDestroyCommands;


    // non-config items
    public HashMap<Flag<?>, Object> regionFlags = new HashMap<>();
    public LinkedHashMap<String, List<String>> allowedFlags = new LinkedHashMap<>();

    /**
     * Get the protection block item for this specific protection block.
     *
     * @return the item with NBT and other metadata to signify that it was created by protection stones
     */
    public ItemStack createItem() {
        return ProtectionStones.createProtectBlockItem(this);
    }
}
