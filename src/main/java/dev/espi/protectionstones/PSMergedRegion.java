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
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.Location;
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
    private Block block;
    private PSProtectBlock originalType;

    PSMergedRegion(String id, PSProtectBlock originalType, PSGroupRegion mergedGroup, RegionManager rgmanager, World world) {
        super(rgmanager, world);
        this.id = id;
        this.mergedGroup = mergedGroup;
        this.originalType = originalType;

        PSLocation psl = WGUtils.parsePSRegionToLocation(id);
        block = world.getBlockAt(psl.x, psl.y, psl.z);
    }

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
        return block.getType().toString().equals(originalType.type);
    }

    @Override
    public Block getProtectBlock() {
        return block;
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

        return false;
    }

    @Override
    public boolean deleteRegion(boolean deleteBlock, Player cause) {
        return false;
    }

    @Override
    public ProtectedRegion getWGRegion() {
        return WGUtils.getDefaultProtectedRegion(originalType, WGUtils.parsePSRegionToLocation(id));
    }
}
