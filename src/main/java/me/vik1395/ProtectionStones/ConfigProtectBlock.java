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

package me.vik1395.ProtectionStones;

import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.SpecIntInRange;
import com.sk89q.worldguard.protection.flags.Flag;

import java.util.HashMap;
import java.util.List;

public class ConfigProtectBlock {

    /*
     * Object to represent a protection block as defined in config ("Blocks" section)
     */

    // Annotations are for types that have names that aren't the same as the config name

    // main section
    public String type;
    @Path("restrict_obtaining")
    public boolean restrictObtaining;
    @Path("word_list_type")
    public String worldListType;
    @Path("worlds")
    public List<String> worlds;

    // region section
    @Path("region.x_radius")
    @SpecIntInRange(min = 0, max = Integer.MAX_VALUE)
    public int xRadius;
    @Path("region.y_radius")
    @SpecIntInRange(min = 0, max = Integer.MAX_VALUE)
    public int yRadius;
    @Path("region.z_radius")
    @SpecIntInRange(min = 0, max = Integer.MAX_VALUE)
    public int zRadius;
    @Path("region.home_x_offset")
    @SpecIntInRange(min = 0, max = Integer.MAX_VALUE)
    public int homeXOffset;
    @Path("region.home_y_offset")
    @SpecIntInRange(min = 0, max = Integer.MAX_VALUE)
    public int homeYOffset;
    @Path("region.home_z_offset")
    @SpecIntInRange(min = 0, max = Integer.MAX_VALUE)
    public int homeZOffset;
    @Path("region.flags")
    public List<String> flags;
    @Path("region.allowed_flags")
    public List<String> allowed_flags;
    @Path("region.priority")
    public int priority;

    // block data section
    @Path("block_data.display_name")
    public String displayName;
    @Path("block_data.lore")
    public List<String> lore;
    @Path("block_data.price")
    public double price;

    // behaviour section
    @Path("behaviour.auto_hide")
    public boolean autoHide;
    @Path("behaviour.no_drop")
    public boolean noDrop;
    @Path("behaviour.prevent_piston_push")
    public boolean preventPistonPush;
    @Path("behaviour.prevent_silk_touch")
    public boolean preventSilkTouch;

    // player section
    @Path("player.prevent_teleport_in")
    public boolean preventTeleportIn;
    @Path("player.no_moving_when_tp_waiting")
    public boolean noMovingWhenTeleportWaiting;
    @Path("player.tp_waiting_seconds")
    @SpecIntInRange(min = 0, max = Integer.MAX_VALUE)
    public int tpWaitingSeconds;
    @Path("player.permission")
    public String permission;

    // non-config items
    public HashMap<Flag<?>, Object> regionFlags = new HashMap<>();
}
