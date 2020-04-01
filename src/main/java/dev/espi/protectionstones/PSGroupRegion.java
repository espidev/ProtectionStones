/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.espi.protectionstones;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.utils.MiscUtil;
import dev.espi.protectionstones.utils.Objs;
import lombok.val;
import lombok.var;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.*;

/**
 * Represents a region that exists but is a group of merged {@link PSStandardRegion}s.
 * Contains multiple {@link PSMergedRegion} representing the individual merged regions (which don't technically exist in WorldGuard).
 */

public class PSGroupRegion extends PSStandardRegion {
    PSGroupRegion(ProtectedRegion wgregion, RegionManager rgmanager, World world) {
        super(wgregion, rgmanager, world);
        assert getWGRegion().getFlag(FlagHandler.PS_MERGED_REGIONS) != null;
    }

    @Override
    public double getTaxRate() {
        double taxRate = 0;
        for (PSMergedRegion r : getMergedRegions()) {
            taxRate += r.getTaxRate();
        }
        return taxRate;
    }

    @Override
    public String getTaxPeriod() {
        Set<String> s = new HashSet<>();
        getMergedRegions().forEach(r -> s.add(r.getTaxPeriod()));
        return MiscUtil.concatWithoutLast(new ArrayList<>(s), ", ");
    }

    @Override
    public String getTaxPaymentPeriod() {
        Set<String> s = new HashSet<>();
        getMergedRegions().forEach(r -> s.add(r.getTaxPaymentPeriod()));
        return MiscUtil.concatWithoutLast(new ArrayList<>(s), ", ");
    }

    @Override
    public void updateTaxPayments() {
        val currentTime = System.currentTimeMillis();

        List<TaxPayment> payments = Objs.replaceNull(getTaxPaymentsDue(), new ArrayList<>());
        List<LastRegionTaxPaymentEntry> lastAdded = Objs.replaceNull(getRegionLastTaxPaymentAddedEntries(), new ArrayList<>());

        // loop over merged regions
        for (val r : getMergedRegions()) {
            // taxes disabled
            if (getTypeOptions().taxPeriod == -1) continue;

            var found = false;
            for (var last : lastAdded) {
                // if the last region payment entry refers to this region
                if (last.getRegionId().equals(r.getId())) {
                    found = true;
                    // if it's time to pay
                    if (last.getLastPaymentAdded() + Duration.ofSeconds(r.getTypeOptions().taxPeriod).toMillis() < currentTime) {
                        payments.add(new TaxPayment(currentTime + Duration.ofSeconds(r.getTypeOptions().taxPaymentTime).toMillis(), r.getTaxRate(), r.getId()));
                        last.setLastPaymentAdded(currentTime);
                    }
                    break;
                }
            }

            if (!found) {
                payments.add(new TaxPayment(currentTime + Duration.ofSeconds(r.getTypeOptions().taxPaymentTime).toMillis(), r.getTaxRate(), r.getId()));
                lastAdded.add(new LastRegionTaxPaymentEntry(r.getId(), currentTime));
            }
        }
        setTaxPaymentsDue(payments);
        setRegionLastTaxPaymentAddedEntries(lastAdded);
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
            if (r.getId().equals(getId())) return r;
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

        // remove from taxes
        if (getWGRegion().getFlag(FlagHandler.PS_TAX_LAST_PAYMENT_ADDED) != null) {
            String entry = "";
            for (val e : getWGRegion().getFlag(FlagHandler.PS_TAX_LAST_PAYMENT_ADDED)) {
                if (e.startsWith(id)) entry = e;
            }
            getWGRegion().getFlag(FlagHandler.PS_TAX_LAST_PAYMENT_ADDED).remove(entry);
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
            l.add(new PSMergedRegion(id, this, getWGRegionManager(), getWorld()));
        }
        return l;
    }
}
