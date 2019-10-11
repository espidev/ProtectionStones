/*
 * Copyright 2019 ProtectionStones team and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.espi.protectionstones;

import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.SpecDoubleInRange;
import com.electronwill.nightconfig.core.conversion.SpecIntInRange;
import com.sk89q.worldguard.protection.flags.Flag;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
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
    @Path("restrict_obtaining")
    public boolean restrictObtaining;
    @Path("world_list_type")
    public String worldListType;
    @Path("worlds")
    public List<String> worlds;
    @Path("prevent_block_place_in_restricted_world")
    public boolean preventBlockPlaceInRestrictedWorld;

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
    @Path("region.x_offset")
    @SpecIntInRange(min = 0, max = Integer.MAX_VALUE)
    public int xOffset;
    @Path("region.y_offset")
    @SpecIntInRange(min = 0, max = Integer.MAX_VALUE)
    public int yOffset;
    @Path("region.z_offset")
    @SpecIntInRange(min = 0, max = Integer.MAX_VALUE)
    public int zOffset;
    @Path("region.home_x_offset")
    @SpecDoubleInRange(min = 0, max = Integer.MAX_VALUE)
    public double homeXOffset;
    @Path("region.home_y_offset")
    @SpecDoubleInRange(min = 0, max = Integer.MAX_VALUE)
    public double homeYOffset;
    @Path("region.home_z_offset")
    @SpecDoubleInRange(min = 0, max = Integer.MAX_VALUE)
    public double homeZOffset;
    @Path("region.flags")
    public List<String> flags;
    @Path("region.allowed_flags")
    public List<String> allowedFlags;
    @Path("region.hidden_flags_from_info")
    public List<String> hiddenFlagsFromInfo;
    @Path("region.priority")
    public int priority;
    @Path("region.allow_overlap_unowned_regions")
    public boolean allowOverlapUnownedRegions;
    @Path("region.allow_merging")
    public boolean allowMerging;

    // block data section
    @Path("block_data.display_name")
    public String displayName;
    @Path("block_data.lore")
    public List<String> lore;
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
    @Path("player.permission")
    public String permission;

    // event section
    @Path("event.enable")
    public boolean eventsEnabled;
    @Path("event.on_region_create")
    List<String> regionCreateCommands;
    @Path("event.on_region_destroy")
    List<String> regionDestroyCommands;


    // non-config items
    public HashMap<Flag<?>, Object> regionFlags = new HashMap<>();

    /**
     * Get the protection block item for this specific protection block.
     *
     * @return the item with NBT and other metadata to signify that it was created by protection stones
     */
    public ItemStack createItem() {
        return ProtectionStones.createProtectBlockItem(this);
    }
}
