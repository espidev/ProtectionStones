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

package dev.espi.protectionstones;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.utils.BlockUtil;
import dev.espi.protectionstones.utils.WGUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents an instance of a protectionstones protected region.
 */

public abstract class PSRegion {
    RegionManager rgmanager;
    World world;

    PSRegion(RegionManager rgmanager, World world) {
        this.rgmanager = checkNotNull(rgmanager);
        this.world = checkNotNull(world);
    }

    // ~~~~~~~~~~~~~~~~~ static ~~~~~~~~~~~~~~~~~

    /**
     * Get the protection stone region that the location is in, or the closest one if there are overlapping regions.
     * Returns either {@link PSGroupRegion}, {@link PSStandardRegion} or {@link PSMergedRegion}.
     *
     * @param l the location
     * @return the {@link PSRegion} object if the location is in a region, or null if the location is not in a region
     */
    public static PSRegion fromLocation(Location l) {
        checkNotNull(checkNotNull(l).getWorld());
        RegionManager rgm = WGUtils.getRegionManagerWithWorld(l.getWorld());

        // check exact location first
        PSMergedRegion pr = PSMergedRegion.getMergedRegion(l);
        if (pr != null) return pr;

        // check if location is in a region
        String psID = WGUtils.matchLocationToPSID(l);
        ProtectedRegion r = rgm.getRegion(psID);

        if (r == null) {
            return null;
        } else if (r.getFlag(FlagHandler.PS_MERGED_REGIONS) != null) {
            return new PSGroupRegion(r, rgm, l.getWorld());
        } else {
            return new PSStandardRegion(r, rgm, l.getWorld());
        }
    }

    /**
     * Get the protection stone region with the world and region.
     *
     * @param w the world
     * @param r the WorldGuard region
     * @return the {@link PSRegion} based on the parameters, or null if the region given is not a protectionstones region
     */
    public static PSRegion fromWGRegion(World w, ProtectedRegion r) {
        if (!ProtectionStones.isPSRegion(r)) return null;
        if (r.getFlag(FlagHandler.PS_MERGED_REGIONS) != null) {
            return new PSGroupRegion(r, WGUtils.getRegionManagerWithWorld(checkNotNull(w)), w);
        } else {
            return new PSStandardRegion(r, WGUtils.getRegionManagerWithWorld(checkNotNull(w)), w);
        }
    }

    /**
     * Get the protection stone regions that have the given name as their set nickname (/ps name)
     *
     * @param w    the world to look for regions in
     * @param name the nickname of the region
     * @return the list of regions that have that name
     */

    public static List<PSRegion> fromName(World w, String name) {
        List<PSRegion> l = new ArrayList<>();

        if (ProtectionStones.regionNameToID.get(w).get(name) == null) return l;

        for (int i = 0; i < ProtectionStones.regionNameToID.get(w).get(name).size(); i++) {
            String id = ProtectionStones.regionNameToID.get(w).get(name).get(i);
            if (WGUtils.getRegionManagerWithWorld(w).getRegion(id) == null) { // cleanup cache
                ProtectionStones.regionNameToID.get(w).get(name).remove(i);
                i--;
            } else {
                l.add(fromWGRegion(w, WGUtils.getRegionManagerWithWorld(w).getRegion(id)));
            }
        }
        return l;
    }

    // ~~~~~~~~~~~ instance ~~~~~~~~~~~~~~~~

    /**
     * @return gets the world that the region is in
     */
    public World getWorld() {
        return world;
    }

    /**
     * Get the WorldGuard ID of the region. Note that this is not guaranteed to be unique between worlds.
     *
     * @return the id of the region
     */
    public abstract String getID();

    /**
     * Get the name (nickname) of the region from /ps name.
     *
     * @return the name of the region, or null if the region does not have a name
     */

    public abstract String getName();

    /**
     * Set the name of the region (from /ps name).
     *
     * @param name new name, or null to remove the name
     */

    public abstract void setName(String name);

    /**
     * Set the parent of this region.
     *
     * @param r the region to be the parent, or null for no parent
     * @throws ProtectedRegion.CircularInheritanceException thrown when the parent already inherits from the child
     */

    public abstract void setParent(PSRegion r) throws ProtectedRegion.CircularInheritanceException;

    /**
     * Get the parent of this region, if there is one.
     *
     * @return the parent of the region, or null if there isn't one
     */

    public abstract PSRegion getParent();

    /**
     * Get the location of the set home the region has (for /ps tp).
     *
     * @return the location of the home, or null if the ps_home flag is not set.
     */
    public abstract Location getHome();

    /**
     * Set the home of the region (internally changes the flag).
     *
     * @param blockX block x location
     * @param blockY block y location
     * @param blockZ block z location
     */
    public abstract void setHome(double blockX, double blockY, double blockZ);

    public enum RentStage {
        NOT_RENTING, LOOKING_FOR_TENANT, RENTING
    }

    /**
     * @return whether or not the region is for sale
     */
    public abstract boolean forSale();

    /**
     * MUST BE CALLED when setting up the region to be sold or cancelling sale
     *
     * @param forSale  whether or not the region is for sale
     * @param landlord the owner of the region
     * @param price    the price to sell for
     */
    public abstract void setSellable(boolean forSale, UUID landlord, double price);

    /**
     * Sells the region to a player at the price listed.
     *
     * @param player player to transfer the region to
     */
    public abstract void sell(UUID player);

    /**
     * @return get the stage of the renting process
     */
    public abstract RentStage getRentStage();

    /**
     * Get the landlord of the region.
     *
     * @return returns the UUID of the landlord, or null if there is none.
     */
    public abstract UUID getLandlord();

    /**
     * Set the landlord of the region.
     *
     * @param landlord uuid of landlord, or null to remove
     */
    public abstract void setLandlord(UUID landlord);

    /**
     * Get the tenant of the region.
     *
     * @return returns the UUID of the tenant, or null if there is none.
     */
    public abstract UUID getTenant();

    /**
     * Set the tenant of the region
     *
     * @param tenant uuid of tenant, or null to remove
     */
    public abstract void setTenant(UUID tenant);

    /**
     * Get the rent period of the region
     *
     * @return returns the rent duration, or null if there is none
     */
    public abstract String getRentPeriod();

    /**
     * Set the rent period of the region
     *
     * @param s the duration between rent payments (d h m s), or null to remove
     */
    public abstract void setRentPeriod(String s);

    /**
     * Get the price of the region
     * This applies to either the rent or the full purchase of a region.
     *
     * @return the price of the region during rent payments, or null if there is no rent
     */
    public abstract Double getPrice();

    /**
     * Set the price of the region.
     * This applies to either the rent or the full purchase of a region.
     *
     * @param price the price of the region, or null if there is no rent
     */
    public abstract void setPrice(Double price);

    /**
     * Set the unix timestamp of when rent was last paid.
     *
     * @param timestamp the unix timestamp of when rent was last paid, or null
     */
    public abstract void setRentLastPaid(Long timestamp);

    /**
     * Get the unix timestamp of when rent was last paid.
     *
     * @return the unix timestamp of when rent was last paid, or null if not renting
     */
    public abstract Long getRentLastPaid();

    /**
     * MUST BE CALLED when the region is looking for a tenant.
     *
     * @param landlord   the landlord of the region
     * @param rentPeriod the rent period (d h m s) of the region
     * @param rentPrice  the price to charge during each rent payment
     */
    public abstract void setRentable(UUID landlord, String rentPeriod, double rentPrice);

    /**
     * Starts renting process (adds to rent queue) tenant.
     * MUST BE CALLED when renting the region out to a tenant.
     *
     * @param landlord   the landlord of the region
     * @param tenant     the tenant of the region
     * @param rentPeriod the rent period (d h m s) of the region
     * @param rentPrice  the price to charge during each rent payment
     */
    public abstract void rentOut(UUID landlord, UUID tenant, String rentPeriod, double rentPrice);

    /**
     * Stop renting process and remove tenant.
     * MUST BE CALLED when removing rent.
     */
    public abstract void removeRenting();

    @Getter
    @Setter
    @AllArgsConstructor
    public static class TaxPayment implements Comparable<TaxPayment> {
        long whenPaymentWasGiven;
        double amount;

        @Override
        public int compareTo(TaxPayment t) {
            return Double.compare(amount, t.amount);
        }
    }

    /**
     * Get the tax rate for this region type.
     *
     * @return the tax rate
     */
    public abstract double getTaxRate();

    /**
     * Get the period between tax payments for this region type.
     *
     * @return the duration between tax payments
     */
    public abstract Duration getTaxPeriod();

    /**
     * Get the period allowed for the payment of tax.
     *
     * @return the duration of time allowed to pay a tax
     */
    public abstract Duration getTaxPaymentPeriod();

    /**
     * Get the list of tax payments that are due.
     *
     * @return the list of tax payments outstanding
     */
    public abstract List<TaxPayment> getTaxPaymentsDue();

    /**
     * Get the player that is set to autopay the tax amount.
     *
     * @return the player that is set as the autopayer, or null if no player is set
     */
    public abstract UUID getTaxAutopayer();

    /**
     * Pay outstanding taxes.
     * It will only withdraw the amount required to pay the taxes, and will take up to the amount
     * specified if the outstanding payments are larger.
     *
     * @param p the player to take money from
     * @param amount the amount to take
     * @return the {@link EconomyResponse} returned by Vault
     */
    public abstract EconomyResponse payTax(PSPlayer p, double amount);

    /**
     * Check if any tax payments are now late (exceeded tax payment time shown in config).
     *
     * @return whether or not any tax payments are now late
     */
    public abstract boolean isTaxPaymentLate();

    /**
     * Update with the current time and calculate any tax payments that are now due.
     */
    public abstract void updateTaxPayments();

    /**
     * @return whether or not the protection block is hidden (/ps hide)
     */
    public boolean isHidden() {
        return !this.getType().equals(BlockUtil.getProtectBlockType(this.getProtectBlock()));
    }

    /**
     * Hides the protection block, if it is not hidden.
     *
     * @return whether or not the block was hidden
     */
    public boolean hide() {
        if (!isHidden()) {
            getProtectBlock().setType(Material.AIR);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Unhides the protection block, if it is hidden.
     *
     * @return whether or not the block was unhidden
     */
    public boolean unhide() {
        if (isHidden()) {
            if (getType().startsWith("PLAYER_HEAD")) {
                getProtectBlock().setType(Material.PLAYER_HEAD);
                if (getType().split(":").length > 1) {
                    BlockUtil.setHeadType(getType(), getProtectBlock());
                }
            } else {
                getProtectBlock().setType(Material.getMaterial(getType()));
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Toggle whether or not the protection block is hidden.
     */
    public void toggleHide() {
        if (!hide()) unhide();
    }

    /**
     * This method returns the block that is supposed to contain the protection block.
     * Warning: If the protection stone is hidden, this will give the block that took its place!
     *
     * @return returns the block that may contain the protection stone
     */
    public abstract Block getProtectBlock();

    /**
     * @return returns the type
     */
    public abstract PSProtectBlock getTypeOptions();

    /**
     * @return returns the protect block type (may include custom player heads PLAYER_HEAD:playername) that the region is
     */
    public abstract String getType();

    /**
     * Change the type of the protection region.
     *
     * @param type the type of protection region to switch to
     */
    public void setType(PSProtectBlock type) {
        if (!isHidden()) {
            Material set = Material.matchMaterial(type.type) == null ? Material.PLAYER_HEAD : Material.matchMaterial(type.type);
            getProtectBlock().setType(set);
            if (type.type.startsWith("PLAYER_HEAD") && type.type.split(":").length > 1) {
                BlockUtil.setHeadType(type.type, getProtectBlock());
            }
        }
    }

    /**
     * Get whether or not a player is an owner of this region.
     *
     * @param uuid the player's uuid
     * @return whether or not the player is a member
     */

    public abstract boolean isOwner(UUID uuid);

    /**
     * Get whether or not a player is a member of this region.
     *
     * @param uuid the player's uuid
     * @return whether or not the player is a member
     */

    public abstract boolean isMember(UUID uuid);

    /**
     * @return returns a list of the owners of the protected region
     */
    public abstract ArrayList<UUID> getOwners();

    /**
     * @return returns a list of the members of the protected region
     */
    public abstract ArrayList<UUID> getMembers();

    /**
     * @return returns a list of the bounding points of the protected region
     */
    public abstract List<BlockVector2> getPoints();

    /**
     * Deletes the region forever. Can be cancelled by event cancellation.
     *
     * @param deleteBlock whether or not to also set the protection block to air (if not hidden)
     * @return whether or not the region was able to be successfully removed
     */
    public abstract boolean deleteRegion(boolean deleteBlock);

    /**
     * Deletes the region forever. Can be cancelled by event cancellation.
     *
     * @param deleteBlock whether or not to also set the protection block to air (if not hidden)
     * @param cause       the player that caused the region to break
     * @return whether or not the region was able to be successfully removed
     */
    public abstract boolean deleteRegion(boolean deleteBlock, Player cause);

    /**
     * @return returns the WorldGuard region object directly
     */
    public abstract ProtectedRegion getWGRegion();

    /**
     * @return returns the WorldGuard region manager that stores this region
     */
    public RegionManager getWGRegionManager() {
        return rgmanager;
    }
}
