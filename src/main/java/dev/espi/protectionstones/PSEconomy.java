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

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

// TODO prevent rented regions from being merged

public class PSEconomy {
    private List<PSRegion> rentedList = new ArrayList<>();
    private static int rentRunner = -1;

    public PSEconomy() {
        if (rentRunner != -1) Bukkit.getScheduler().cancelTask(rentRunner);
        if (!ProtectionStones.getInstance().isVaultSupportEnabled()) {
            ProtectionStones.getInstance().getLogger().warning("Vault is not enabled! Economy functions (renting & buying) will not work!");
            return;
        }
        // find regions that are being rented out (called on startup or reload)
        loadRentList();

        // start rent
        rentRunner = Bukkit.getScheduler().runTaskTimerAsynchronously(ProtectionStones.getInstance(), this::updateRents, 200, 0).getTaskId();
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

    public void updateRents() {

        for (int i = 0; i < rentedList.size(); i++) {
            PSRegion r = rentedList.get(i);
            if (r.getRentStage() != PSRegion.RentStage.RENTING) {
                // remove entry if it isn't in renting stage.
                rentedList.remove(i);
                i--;
                continue;
            }

            Duration rentPeriod = parseRentPeriod(r.getRentPeriod());
            // if tenant needs to pay
            if (Instant.now().getEpochSecond() > r.getRentLastPaid() + rentPeriod.getSeconds()) {
                ProtectionStones.getInstance().getVaultEconomy().withdrawPlayer(Bukkit.getOfflinePlayer(r.getTenant()), r.getPrice());
                ProtectionStones.getInstance().getVaultEconomy().depositPlayer(Bukkit.getOfflinePlayer(r.getLandlord()), r.getPrice());
                r.setRentLastPaid(Instant.now().getEpochSecond());
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

    public Duration parseRentPeriod(String period) {
        Duration rentPeriod = Duration.ZERO;
        for (String s : period.split(" ")) {
            try {
                if (s.contains("w")) {
                    rentPeriod = rentPeriod.plusDays(Long.parseLong(s.replace("w", ""))*7);
                } else if (s.contains("d")) {
                    rentPeriod = rentPeriod.plusDays(Long.parseLong(s.replace("d", "")));
                } else if (s.contains("h")) {
                    rentPeriod = rentPeriod.plusHours(Long.parseLong(s.replace("h", "")));
                } else if (s.contains("m")) {
                    rentPeriod = rentPeriod.plusMinutes(Long.parseLong(s.replace("m", "")));
                } else if (s.contains("s")) {
                    rentPeriod = rentPeriod.plusSeconds(Long.parseLong(s.replace("s", "")));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return rentPeriod;
    }


}