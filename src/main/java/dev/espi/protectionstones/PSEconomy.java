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

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

// TODO prevent rented regions from being merged
// rented loop

public class PSEconomy {
    private List<PSRegion> rentedList = new ArrayList<>();

    public PSEconomy() {
        // find regions that are being rented out (called on startup or reload)
        loadRentList();
    }

    /**
     * Load list of regions that are rented into memory.
     */
    public void loadRentList() {
        rentedList = new ArrayList<>();
        for (World w : Bukkit.getWorlds()) {
            for (ProtectedRegion pr : WGUtils.getRegionManagerWithWorld(w).getRegions().values()) {
                if (ProtectionStones.isPSRegion(pr)) {
                    rentedList.add(PSRegion.fromWGRegion(w, pr));
                }
            }
        }
    }

    /**
     * Get list of rented regions.
     * @return the list of rented regions
     */
    public List<PSRegion> getRentedList() {
        return rentedList;
    }

}


// TODO check for nulls when looping through rented lists