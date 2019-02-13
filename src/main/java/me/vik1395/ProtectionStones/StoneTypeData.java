
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
        int xradius = ProtectionStones.plugin.getConfig().getInt(ConfigString);
        return xradius;
    }

    public int RegionY(String StoneType) {
        String ConfigString = "Region." + StoneType + ".Y Radius";
        int yradius = ProtectionStones.plugin.getConfig().getInt(ConfigString);
        return yradius;   
    }

    public int RegionZ(String StoneType) {
        String ConfigString = "Region." + StoneType + ".Z Radius";
        int zradius = ProtectionStones.plugin.getConfig().getInt(ConfigString);
        return zradius;  
    }

    public Boolean AutoHide(String StoneType) {
        String ConfigString = "Region." + StoneType + ".Auto Hide";
        boolean autohide = ProtectionStones.plugin.getConfig().getBoolean(ConfigString);
        return autohide;
    }

    public boolean NoDrop(String StoneType) {
        String ConfigString = "Region." + StoneType + ".No Drop";
        boolean nodrop = ProtectionStones.plugin.getConfig().getBoolean(ConfigString);
        return nodrop;
    }

    public boolean BlockPiston(String StoneType) {
        String ConfigString = "Region." + StoneType + ".Block Piston";
        boolean blockpiston = ProtectionStones.plugin.getConfig().getBoolean(ConfigString);
        return blockpiston;
    }

    public boolean SilkTouch(String StoneType) {
        String ConfigString = "Region." + StoneType + ".Silk Touch";
        boolean silktouch = ProtectionStones.plugin.getConfig().getBoolean(ConfigString);
        return silktouch;
    }

    public int DefaultPriority(String StoneType) {
        String ConfigString = "Region." + StoneType + ".Priority";
        int priority = ProtectionStones.plugin.getConfig().getInt(ConfigString);
        return priority;
    }
}