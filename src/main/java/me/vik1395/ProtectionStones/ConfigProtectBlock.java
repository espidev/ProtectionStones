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

import com.sk89q.worldguard.protection.flags.Flag;

import java.util.HashMap;
import java.util.List;

public class ConfigProtectBlock {

    /*
     * Object to represent a protection block as defined in config ("Blocks" section)
     */

    // main section
    public boolean restrictObtaining;
    public String worldListType;
    public List<String> worlds;

    // region section
    public int xRadius, yRadius, zRadius;
    public int homeXOffset, homeYOffset, homeZOffset;
    public HashMap<Flag<?>, Object> flags = new HashMap<>();
    public List<String> allowedFlags;
    public int priority;

    // block data section
    public String displayName;
    public List<String> lore;
    public double price;

    // behaviour section
    public boolean autoHide, noDrop, preventPistonPush, silkTouch;

    // player section
    public boolean preventTeleportIn, noMovingWhenTeleportWaiting;
    public int tpWaitingSeconds;
    public String permission;

}
