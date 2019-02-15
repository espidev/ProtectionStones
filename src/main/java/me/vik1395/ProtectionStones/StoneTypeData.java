
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

public class StoneTypeData {
    public int RegionX(String StoneType) {
        String ConfigString = "Region." + StoneType + ".X Radius";
        return ProtectionStones.plugin.getConfig().getInt(ConfigString);
    }

    public int RegionY(String StoneType) {
        String ConfigString = "Region." + StoneType + ".Y Radius";
        return ProtectionStones.plugin.getConfig().getInt(ConfigString);
    }

    public int RegionZ(String StoneType) {
        String ConfigString = "Region." + StoneType + ".Z Radius";
        return ProtectionStones.plugin.getConfig().getInt(ConfigString);
    }

    public Boolean AutoHide(String StoneType) {
        String ConfigString = "Region." + StoneType + ".Auto Hide";
        return ProtectionStones.plugin.getConfig().getBoolean(ConfigString);
    }

    public boolean NoDrop(String StoneType) {
        String ConfigString = "Region." + StoneType + ".No Drop";
        return ProtectionStones.plugin.getConfig().getBoolean(ConfigString);
    }

    public boolean BlockPiston(String StoneType) {
        String ConfigString = "Region." + StoneType + ".Block Piston";
        return ProtectionStones.plugin.getConfig().getBoolean(ConfigString);
    }

    public boolean SilkTouch(String StoneType) {
        String ConfigString = "Region." + StoneType + ".Silk Touch";
        return ProtectionStones.plugin.getConfig().getBoolean(ConfigString);
    }

    public int DefaultPriority(String StoneType) {
        String ConfigString = "Region." + StoneType + ".Priority";
        return ProtectionStones.plugin.getConfig().getInt(ConfigString);
    }
}