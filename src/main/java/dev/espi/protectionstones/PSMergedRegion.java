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
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Represents an instance of a PS region that has been merged into another region. There is no actual WG region that
 * this contains, and instead takes properties from its parent region.
 */

public class PSMergedRegion extends PSRegion {


    PSMergedRegion(RegionManager rgmanager, World world) {
        super(rgmanager, world);
    }

    @Override
    public String getID() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public void setParent(PSRegion r) throws ProtectedRegion.CircularInheritanceException {

    }

    @Override
    public PSRegion getParent() {
        return null;
    }

    @Override
    public Location getHome() {
        return null;
    }

    @Override
    public void setHome(int blockX, int blockY, int blockZ) {

    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public boolean hide() {
        return false;
    }

    @Override
    public boolean unhide() {
        return false;
    }

    @Override
    public Block getProtectBlock() {
        return null;
    }

    @Override
    public PSProtectBlock getTypeOptions() {
        return null;
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public boolean isOwner(UUID uuid) {
        return false;
    }

    @Override
    public boolean isMember(UUID uuid) {
        return false;
    }

    @Override
    public ArrayList<UUID> getOwners() {
        return null;
    }

    @Override
    public ArrayList<UUID> getMembers() {
        return null;
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
        return null;
    }
}
