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

public class ConfigProtectBlock {
    private int regionX, regionY, regionZ, defaultPriority;
    boolean autoHide, noDrop, blockPiston, silkTouch;

    /*
     * Object to represent a protection block as defined in config ("Region" section)
     */

    public int getRegionX() {
        return regionX;
    }

    public void setRegionX(int regionX) {
        this.regionX = regionX;
    }

    public int getRegionY() {
        return regionY;
    }

    public void setRegionY(int regionY) {
        this.regionY = regionY;
    }

    public int getRegionZ() {
        return regionZ;
    }

    public void setRegionZ(int regionZ) {
        this.regionZ = regionZ;
    }

    public int getDefaultPriority() {
        return defaultPriority;
    }

    public void setDefaultPriority(int defaultPriority) {
        this.defaultPriority = defaultPriority;
    }

    public boolean isAutoHide() {
        return autoHide;
    }

    public void setAutoHide(boolean autoHide) {
        this.autoHide = autoHide;
    }

    public boolean noDrop() {
        return noDrop;
    }

    public void setNoDrop(boolean noDrop) {
        this.noDrop = noDrop;
    }

    public boolean denyBlockPiston() {
        return blockPiston;
    }

    public void setBlockPiston(boolean blockPiston) {
        this.blockPiston = blockPiston;
    }

    public boolean denySilkTouch() {
        return silkTouch;
    }

    public void setSilkTouch(boolean silkTouch) {
        this.silkTouch = silkTouch;
    }
}
