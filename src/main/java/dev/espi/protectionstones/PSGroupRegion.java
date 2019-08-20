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

package dev.espi.protectionstones;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class PSGroupRegion extends PSStandardRegion {
    PSGroupRegion(ProtectedRegion wgregion, RegionManager rgmanager, World world) {
        super(wgregion, rgmanager, world);
        assert getWGRegion().getFlag(FlagHandler.PS_MERGED_REGIONS) != null;
    }

    @Override
    public boolean hide() {
        for (PSMergedRegion r : getMergedRegions()) r.hide();
        return true;
    }

    @Override
    public boolean unhide() {
        for (PSMergedRegion r : getMergedRegions()) r.unhide();
        return true;
    }

    public PSMergedRegion getRootRegion() {
        for (PSMergedRegion r : getMergedRegions()) {
            if (r.getID().equals(getID())) return r;
        }
        return null;
    }

    public boolean hasMergedRegion(String id) {
        return getWGRegion().getFlag(FlagHandler.PS_MERGED_REGIONS).contains(id);
    }

    /**
     * Get the PSMergedRegion objects of the regions that were merged into this region.
     * @return the list of regions merged into this region
     */

    public List<PSMergedRegion> getMergedRegions() {
        List<PSMergedRegion> l = new ArrayList<>();
        for (String line : getWGRegion().getFlag(FlagHandler.PS_MERGED_REGIONS_TYPES)) {
            String[] spl = line.split(" ");
            String id = spl[0], type = spl[1];
            l.add(new PSMergedRegion(id, ProtectionStones.getBlockOptions(type), this, getWGRegionManager(), getWorld()));
        }
        return l;
    }
}
