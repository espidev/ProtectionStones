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

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.event.PSRemoveEvent;
import dev.espi.protectionstones.utils.WGMerge;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Represents an instance of a PS region that has been merged into another region. There is no actual WG region that
 * this contains, and instead takes properties from its parent region (see {@link PSGroupRegion}).
 */

public class PSMergedRegion extends PSRegion {

    private PSGroupRegion mergedGroup;
    private String id, type;

    PSMergedRegion(String id, PSGroupRegion mergedGroup, RegionManager rgmanager, World world) {
        super(rgmanager, world);
        this.id = id;
        this.mergedGroup = mergedGroup;

        // get type
        // stored instead of fetched on the fly because unmerge algorithm removes the flag causing getType to return null
        for (String s : mergedGroup.getWGRegion().getFlag(FlagHandler.PS_MERGED_REGIONS_TYPES)) {
            String[] spl = s.split(" ");
            String did = spl[0], type = spl[1];
            if (did.equals(getID())) {
                this.type = type;
                break;
            }
        }
    }

    // ~~~~~~~~~~~ static ~~~~~~~~~~~~~~~~

    /**
     * Finds the {@link PSMergedRegion} at a location that is a part of a merged region.
     *
     * @param l location to look at
     * @return the {@link PSMergedRegion} the location is in, or null if not applicable
     */
    public static PSMergedRegion getMergedRegion(Location l) {
        String psID = WGUtils.createPSID(l);
        RegionManager rgm = WGUtils.getRegionManagerWithWorld(l.getWorld());

        for (ProtectedRegion pr : rgm.getApplicableRegions(BlockVector3.at(l.getX(), l.getY(), l.getZ()))) {
            if (pr.getFlag(FlagHandler.PS_MERGED_REGIONS) != null && pr.getFlag(FlagHandler.PS_MERGED_REGIONS).contains(psID)) {
                for (String s : pr.getFlag(FlagHandler.PS_MERGED_REGIONS_TYPES)) {
                    String[] spl = s.split(" ");
                    String id = spl[0], type = spl[1];
                    if (id.equals(psID)) {
                        return new PSMergedRegion(psID, new PSGroupRegion(pr, rgm, l.getWorld()), rgm, l.getWorld());
                    }
                }
            }
        }

        return null;
    }

    // ~~~~~~~~~~~ instance ~~~~~~~~~~~~~~~~

    @Override
    public String getID() {
        return id;
    }

    @Override
    public String getName() {
        return mergedGroup.getName();
    }

    @Override
    public void setName(String name) {
        mergedGroup.setName(name);
    }

    @Override
    public void setParent(PSRegion r) throws ProtectedRegion.CircularInheritanceException {
        mergedGroup.setParent(r);
    }

    @Override
    public PSRegion getParent() {
        return mergedGroup.getParent();
    }

    @Override
    public Location getHome() {
        return mergedGroup.getHome();
    }

    @Override
    public void setHome(double blockX, double blockY, double blockZ) {
        mergedGroup.setHome(blockX, blockY, blockZ);
    }

    @Override
    public boolean forSale() {
        return getParent().forSale();
    }

    @Override
    public void setSellable(boolean forSale, UUID landlord, double price) {
        getParent().setSellable(forSale, landlord, price);
    }

    @Override
    public void sell(UUID player) {
        getParent().sell(player);
    }

    @Override
    public RentStage getRentStage() {
        return getParent().getRentStage();
    }

    @Override
    public UUID getLandlord() {
        return getParent().getLandlord();
    }

    @Override
    public void setLandlord(UUID landlord) {
        getParent().setLandlord(landlord);
    }

    @Override
    public UUID getTenant() {
        return getParent().getTenant();
    }

    @Override
    public void setTenant(UUID tenant) {
        getParent().setTenant(tenant);
    }

    @Override
    public String getRentPeriod() {
        return getParent().getRentPeriod();
    }

    @Override
    public void setRentPeriod(String s) {
        getParent().setRentPeriod(s);
    }

    @Override
    public Double getPrice() {
        return getParent().getPrice();
    }

    @Override
    public void setPrice(Double price) {
        getParent().setPrice(price);
    }

    @Override
    public void setRentLastPaid(Long timestamp) {
        getParent().setRentLastPaid(timestamp);
    }

    @Override
    public Long getRentLastPaid() {
        return getParent().getRentLastPaid();
    }

    @Override
    public void setRentable(UUID landlord, String rentPeriod, double rentPrice) {
        getParent().setRentable(landlord, rentPeriod, rentPrice);
    }

    @Override
    public void rentOut(UUID landlord, UUID tenant, String rentPeriod, double rentPrice) {
        getParent().rentOut(landlord, tenant, rentPeriod, rentPrice);
    }

    @Override
    public void removeRenting() {
        getParent().removeRenting();
    }

    @Override
    public Block getProtectBlock() {
        PSLocation psl = WGUtils.parsePSRegionToLocation(id);
        return world.getBlockAt(psl.x, psl.y, psl.z);
    }

    @Override
    public PSProtectBlock getTypeOptions() {
        return ProtectionStones.getBlockOptions(getType());
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(PSProtectBlock type) {

        super.setType(type);

        // has to be after isHidden query
        this.type = type.type;

        Set<String> flag = mergedGroup.getWGRegion().getFlag(FlagHandler.PS_MERGED_REGIONS_TYPES);
        String original = null;
        for (String s : flag) {
            String[] spl = s.split(" ");
            String id = spl[0];
            if (id.equals(getID())) {
                original = s;
                break;
            }
        }

        if (original != null) {
            flag.remove(original);
            flag.add(getID() + " " + type.type);
        }
    }

    @Override
    public boolean isOwner(UUID uuid) {
        return mergedGroup.isOwner(uuid);
    }

    @Override
    public boolean isMember(UUID uuid) {
        return mergedGroup.isMember(uuid);
    }

    @Override
    public ArrayList<UUID> getOwners() {
        return mergedGroup.getOwners();
    }

    @Override
    public ArrayList<UUID> getMembers() {
        return mergedGroup.getMembers();
    }

    @Override
    public List<BlockVector2> getPoints() {
        return WGUtils.getDefaultProtectedRegion(getTypeOptions(), WGUtils.parsePSRegionToLocation(id)).getPoints();
    }

    @Override
    public boolean deleteRegion(boolean deleteBlock) {
        return deleteRegion(deleteBlock, null);
    }

    @Override
    public boolean deleteRegion(boolean deleteBlock, Player cause) {
        PSRemoveEvent event = new PSRemoveEvent(this, cause);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) { // if event was cancelled, prevent execution
            return false;
        }

        if (deleteBlock && !this.isHidden()) {
            this.getProtectBlock().setType(Material.AIR);
        }

        try {
            WGMerge.unmergeRegion(getWorld(), getWGRegionManager(), this);
        } catch (WGMerge.RegionHoleException | WGMerge.RegionCannotMergeWhileRentedException e) {
            this.unhide();
            return false;
        }

        return true;
    }

    @Override
    public ProtectedRegion getWGRegion() {
        return WGUtils.getDefaultProtectedRegion(getTypeOptions(), WGUtils.parsePSRegionToLocation(id));
    }
}
