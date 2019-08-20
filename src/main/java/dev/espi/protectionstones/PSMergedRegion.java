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
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents an instance of a PS region that has been merged into another region. There is no actual WG region that
 * this contains, and instead takes properties from its parent region.
 */

public class PSMergedRegion extends PSRegion {

    private PSGroupRegion mergedGroup;
    private String id;
    private PSProtectBlock originalType;

    PSMergedRegion(String id, PSProtectBlock originalType, PSGroupRegion mergedGroup, RegionManager rgmanager, World world) {
        super(rgmanager, world);
        this.id = id;
        this.mergedGroup = mergedGroup;
        this.originalType = originalType;
    }

    // ~~~~~~~~~~~ static ~~~~~~~~~~~~~~~~

    public static PSMergedRegion getMergedRegion(World w, Location l) {
        String psID = WGUtils.createPSID(l);
        RegionManager rgm = WGUtils.getRegionManagerWithWorld(w);

        for (ProtectedRegion pr : rgm.getApplicableRegions(BlockVector3.at(l.getX(), l.getY(), l.getZ()))) {
            if (pr.getFlag(FlagHandler.PS_MERGED_REGIONS) != null && pr.getFlag(FlagHandler.PS_MERGED_REGIONS).contains(psID)) {
                for (String s : pr.getFlag(FlagHandler.PS_MERGED_REGIONS_TYPES)) {
                    String[] spl = s.split(" ");
                    String id = spl[0], type = spl[1];
                    if (id.equals(psID)) {
                        return new PSMergedRegion(psID, ProtectionStones.getBlockOptions(type), new PSGroupRegion(pr, rgm, l.getWorld()), rgm, l.getWorld());
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
    public void setHome(int blockX, int blockY, int blockZ) {
        mergedGroup.setHome(blockX, blockY, blockZ);
    }

    @Override
    public boolean isHidden() {
        return !getProtectBlock().getType().toString().equals(originalType.type);
    }

    @Override
    public Block getProtectBlock() {
        PSLocation psl = WGUtils.parsePSRegionToLocation(id);
        return world.getBlockAt(psl.x, psl.y, psl.z);
    }

    @Override
    public PSProtectBlock getTypeOptions() {
        return originalType;
    }

    @Override
    public String getType() {
        return originalType.type;
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
        return WGUtils.getDefaultProtectedRegion(originalType, WGUtils.parsePSRegionToLocation(id)).getPoints();
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

        WGUtils.unmergeRegion(getWorld(), getWGRegionManager(), this);

        return true;
    }

    @Override
    public ProtectedRegion getWGRegion() {
        return WGUtils.getDefaultProtectedRegion(originalType, WGUtils.parsePSRegionToLocation(id));
    }
}
