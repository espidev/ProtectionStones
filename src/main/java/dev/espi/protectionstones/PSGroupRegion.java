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
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a region that exists but is a group of merged {@link PSStandardRegion}s.
 * Contains multiple {@link PSMergedRegion} representing the individual merged regions (which don't technically exist).
 */

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

    @Override
    public boolean deleteRegion(boolean deleteBlock, Player cause) {
        List<PSMergedRegion> l = getMergedRegions();
        if (super.deleteRegion(deleteBlock, cause)) {
            for (PSMergedRegion r : l) {
                if (!r.isHidden() && deleteBlock) r.getProtectBlock().setType(Material.AIR);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get the merged region who's ID is the same as the group region ID
     * @return the root region
     */

    public PSMergedRegion getRootRegion() {
        for (PSMergedRegion r : getMergedRegions()) {
            if (r.getID().equals(getID())) return r;
        }
        return null;
    }

    /**
     * Check if this region contains a specific merged region
     * @param id the psID that would've been generated if the merged region was a standard region
     * @return whether or not the id is a merged region
     */
    public boolean hasMergedRegion(String id) {
        return getWGRegion().getFlag(FlagHandler.PS_MERGED_REGIONS).contains(id);
    }

    /**
     * Removes the merged region's information from the object.
     * Note: This DOES NOT remove the actual PSMergedRegion object, you have to call deleteRegion() on that as well.
     * @param id the id of the merged region
     */
    public void removeMergedRegionInfo(String id) {
        getWGRegion().getFlag(FlagHandler.PS_MERGED_REGIONS).remove(id);

        // remove from ps merged region types
        Iterator<String> i = getWGRegion().getFlag(FlagHandler.PS_MERGED_REGIONS_TYPES).iterator();
        while (i.hasNext()) {
            String[] spl = i.next().split(" ");
            String rid = spl[0];
            if (rid.equals(id)) {
                i.remove();
                break;
            }
        }
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
