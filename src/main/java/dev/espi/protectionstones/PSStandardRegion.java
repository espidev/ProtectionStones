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
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.event.PSRemoveEvent;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents an instance of a standard PS region, that has not been merged or contains merged regions.
 */

public class PSStandardRegion extends PSRegion {
    private ProtectedRegion wgregion;

    PSStandardRegion(ProtectedRegion wgregion, RegionManager rgmanager, World world) {
        super(rgmanager, world);
        this.wgregion = checkNotNull(wgregion);
    }

    // ~~~~~~~~~~~ instance ~~~~~~~~~~~~~~~~

    @Override
    public String getID() {
        return wgregion.getId();
    }

    @Override
    public String getName() {
        return wgregion.getFlag(FlagHandler.PS_NAME);
    }

    @Override
    public void setName(String name) {
        HashMap<String, ArrayList<String>> m = ProtectionStones.regionNameToID.get(getWorld());
        if (m.get(getName()) != null) {
            m.get(getName()).remove(getID());
        }
        if (name != null) {
            if (m.containsKey(name)) {
                m.get(name).add(getID());
            } else {
                m.put(name, new ArrayList<>(Collections.singletonList(getID())));
            }
        }
        wgregion.setFlag(FlagHandler.PS_NAME, name);
    }

    @Override
    public void setParent(PSRegion r) throws ProtectedRegion.CircularInheritanceException {
        wgregion.setParent(r == null ? null : r.getWGRegion());
    }

    @Override
    public PSRegion getParent() {
        return wgregion.getParent() == null ? null : fromWGRegion(world, wgregion.getParent());
    }

    @Override
    public Location getHome() {
        String oPos = wgregion.getFlag(FlagHandler.PS_HOME);
        if (oPos == null) return null;
        String[] pos = oPos.split(" ");
        return new Location(world, Double.parseDouble(pos[0]), Double.parseDouble(pos[1]), Double.parseDouble(pos[2]));
    }

    @Override
    public void setHome(double blockX, double blockY, double blockZ) {
        wgregion.setFlag(FlagHandler.PS_HOME, blockX + " " + blockY + " " + blockZ);
    }

    @Override
    public RentStage getRentStage() {
        if (getLandlord() == null && getTenant() == null) {
            return RentStage.NOT_RENTING;
        } else if (getTenant() == null) {
            return RentStage.LOOKING_FOR_TENANT;
        } else {
            return RentStage.RENTING;
        }
    }

    @Override
    public UUID getLandlord() {
        return wgregion.getFlag(FlagHandler.PS_LANDLORD) == null ? null : UUID.fromString(wgregion.getFlag(FlagHandler.PS_LANDLORD));
    }

    @Override
    public void setLandlord(UUID landlord) {
        wgregion.setFlag(FlagHandler.PS_LANDLORD, landlord == null ? null : landlord.toString());
    }

    @Override
    public UUID getTenant() {
        return wgregion.getFlag(FlagHandler.PS_TENANT) == null ? null : UUID.fromString(wgregion.getFlag(FlagHandler.PS_TENANT));
    }

    @Override
    public void setTenant(UUID tenant) {
        wgregion.setFlag(FlagHandler.PS_TENANT, tenant == null ? null : tenant.toString());
    }

    @Override
    public String getRentPeriod() {
        return wgregion.getFlag(FlagHandler.PS_RENT_PERIOD);
    }

    @Override
    public void setRentPeriod(String s) {
        wgregion.setFlag(FlagHandler.PS_RENT_PERIOD, s);
    }

    @Override
    public Double getPrice() {
        return wgregion.getFlag(FlagHandler.PS_PRICE);
    }

    @Override
    public void setPrice(Double price) {
        wgregion.setFlag(FlagHandler.PS_PRICE, price);
    }

    @Override
    public void setRentLastPaid(Long timestamp) {
        wgregion.setFlag(FlagHandler.PS_RENT_LAST_PAID, timestamp == null ? null : timestamp.doubleValue());
    }

    @Override
    public Long getRentLastPaid() {
        return wgregion.getFlag(FlagHandler.PS_RENT_LAST_PAID) == null ? null : wgregion.getFlag(FlagHandler.PS_RENT_LAST_PAID).longValue();
    }

    @Override
    public void setRentable(UUID landlord, String rentPeriod, double rentPrice) {
        setLandlord(landlord);
        setTenant(null);
        setRentPeriod(rentPeriod);
        setPrice(rentPrice);
    }

    @Override
    public void rentOut(UUID landlord, UUID tenant, String rentPeriod, double rentPrice) {
        setLandlord(landlord);
        setTenant(tenant);
        setRentPeriod(rentPeriod);
        setPrice(rentPrice);
        setRentLastPaid(Instant.now().getEpochSecond());

        ProtectionStones.getEconomy().getRentedList().add(this);
        getWGRegion().getOwners().removeAll();
        getWGRegion().getMembers().removeAll();
        getWGRegion().getOwners().addPlayer(tenant);
    }

    @Override
    public void removeRenting() {
        getWGRegion().getOwners().removeAll();
        getWGRegion().getMembers().removeAll();
        getWGRegion().getOwners().addPlayer(getLandlord());

        setLandlord(null);
        setTenant(null);
        setRentPeriod(null);
        setPrice(null);
        setRentLastPaid(null);

        ProtectionStones.getEconomy().getRentedList().remove(this);
    }

    @Override
    public Block getProtectBlock() {
        PSLocation psl = WGUtils.parsePSRegionToLocation(wgregion.getId());
        return world.getBlockAt(psl.x, psl.y, psl.z);
    }

    @Override
    public PSProtectBlock getTypeOptions() {
        return ProtectionStones.getBlockOptions(getType());
    }

    @Override
    public String getType() {
        return wgregion.getFlag(FlagHandler.PS_BLOCK_MATERIAL);
    }

    @Override
    public void setType(PSProtectBlock type) {
        super.setType(type);
        getWGRegion().setFlag(FlagHandler.PS_BLOCK_MATERIAL, type.type);
    }

    @Override
    public boolean isOwner(UUID uuid) {
        return wgregion.getOwners().contains(uuid);
    }

    @Override
    public boolean isMember(UUID uuid) {
        return wgregion.getMembers().contains(uuid);
    }

    @Override
    public ArrayList<UUID> getOwners() {
        return new ArrayList<>(wgregion.getOwners().getUniqueIds());
    }

    @Override
    public ArrayList<UUID> getMembers() {
        return new ArrayList<>(wgregion.getMembers().getUniqueIds());
    }

    @Override
    public List<BlockVector2> getPoints() {
        return wgregion.getPoints();
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

        if (getName() != null) { // remove name from cache
            if (ProtectionStones.regionNameToID.get(getWorld()).containsKey(getName())) {
                if (ProtectionStones.regionNameToID.get(getWorld()).get(getName()).size() == 1) {
                    ProtectionStones.regionNameToID.get(getWorld()).remove(getName());
                } else {
                    ProtectionStones.regionNameToID.get(getWorld()).get(getName()).remove(getID());
                }
            }
        }
        rgmanager.removeRegion(wgregion.getId());
        return true;
    }

    @Override
    public ProtectedRegion getWGRegion() {
        return wgregion;
    }
}
