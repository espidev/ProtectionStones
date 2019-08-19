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

package dev.espi.protectionstones.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class WGUtils {

    private static final int MAX_BUILD_HEIGHT = 256;

    public static RegionManager getRegionManagerWithPlayer(Player p) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(p.getWorld()));
    }

    public static RegionManager getRegionManagerWithWorld(World w) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(w));
    }

    // Turn WG region name into a location (ex. ps138x35y358z)
    public static PSLocation parsePSRegionToLocation(String regionName) {
        int psx = Integer.parseInt(regionName.substring(2, regionName.indexOf("x")));
        int psy = Integer.parseInt(regionName.substring(regionName.indexOf("x") + 1, regionName.indexOf("y")));
        int psz = Integer.parseInt(regionName.substring(regionName.indexOf("y") + 1, regionName.length() - 1));
        return new PSLocation(psx, psy, psz);
    }

    public static boolean overlapsStrongerRegion(RegionManager rgm, ProtectedRegion r, LocalPlayer lp) {
        if (rgm.overlapsUnownedRegion(r, lp)) { // check if the lp is not owner of a intersecting region
            ApplicableRegionSet rp = rgm.getApplicableRegions(r);
            boolean powerfulOverLap = false;
            for (ProtectedRegion rg : rp) {
                if (rg.getPriority() >= r.getPriority()) { // if protection priority < overlap priority
                    powerfulOverLap = true;
                    break;
                }
            }
            // if we overlap a more powerful region
            return powerfulOverLap;
        }
        return false;
    }

    // returns "" if there is no psregion
    public static String matchLocationToPSID(Location l) {
        BlockVector3 v = BlockVector3.at(l.getX(), l.getY(), l.getZ());
        String currentPSID = "";
        RegionManager rgm = WGUtils.getRegionManagerWithWorld(l.getWorld());
        List<String> idList = rgm.getApplicableRegionsIDs(v);
        if (idList.size() == 1) { // if the location is only in one region
            if (ProtectionStones.isPSRegion(rgm.getRegion(idList.get(0)))) {
                currentPSID = idList.get(0);
            }
        } else {
            // Get nearest protection stone if in overlapping region
            double distanceToPS = -1, tempToPS;
            for (String currentID : idList) {
                if (ProtectionStones.isPSRegion(rgm.getRegion(currentID))) {
                    PSLocation psl = WGUtils.parsePSRegionToLocation(currentID);
                    Location psLocation = new Location(l.getWorld(), psl.x, psl.y, psl.z);
                    tempToPS = l.distance(psLocation);
                    if (distanceToPS == -1 || tempToPS < distanceToPS) {
                        distanceToPS = tempToPS;
                        currentPSID = currentID;
                    }
                }
            }
        }
        return currentPSID;
    }

    // remember to call with offsets
    public static BlockVector3 getMinVector(double bx, double by, double bz, long xRadius, long yRadius, long zRadius) {
        if (yRadius == -1) {
            return BlockVector3.at(bx - xRadius, 0, bz - zRadius);
        } else {
            return BlockVector3.at(bx - xRadius, by - yRadius, bz - zRadius);
        }
    }

    // remember to call with offsets
    public static BlockVector3 getMaxVector(double bx, double by, double bz, long xRadius, long yRadius, long zRadius) {
        if (yRadius == -1) {
            return BlockVector3.at(bx + xRadius, MAX_BUILD_HEIGHT, bz + zRadius);
        } else {
            return BlockVector3.at(bx + xRadius, by + yRadius, bz + zRadius);
        }
    }

    // create PS ids without making the numbers have scientific notation (addressed with long)
    public static String createPSID(double bx, double by, double bz) {
        return "ps" + (long) bx + "x" + (long) by + "y" + (long) bz + "z";
    }

    public static String createPSID(Location l) {
        return createPSID(l.getX(), l.getY(), l.getZ());
    }

    public static boolean hasNoAccess(ProtectedRegion region, Player p, LocalPlayer lp, boolean canBeMember) {
        // Region is not valid
        if (region == null) return true;

        return !p.hasPermission("protectionstones.superowner") && !region.isOwner(lp) && (!canBeMember || !region.isMember(lp));
    }

    public static HashMap<String, ArrayList<String>> getPlayerAdjacentRegionGroups(Player p, RegionManager rm) {
        List<PSRegion> pRegions = ProtectionStones.getPlayerPSRegions(p.getWorld(), p.getUniqueId(), false);
        HashMap<String, String> idToGroup = new HashMap<>();
        HashMap<String, ArrayList<String>> groupToIDs = new HashMap<>();

        for (PSRegion r : pRegions) {
            // create fake region to test overlap (to check for adjacent since borders will need to be 1 block larger)
            double fbx = r.getProtectBlock().getLocation().getX(),
                    fby = r.getProtectBlock().getLocation().getY(),
                    fbz = r.getProtectBlock().getLocation().getZ();

            BlockVector3 minT = WGUtils.getMinVector(fbx, fby, fbz, r.getTypeOptions().xRadius + 1, r.getTypeOptions().yRadius + 1, r.getTypeOptions().zRadius + 1);
            BlockVector3 maxT = WGUtils.getMaxVector(fbx, fby, fbz, r.getTypeOptions().xRadius + 1, r.getTypeOptions().yRadius + 1, r.getTypeOptions().zRadius + 1);

            ProtectedRegion td = new ProtectedCuboidRegion("regionOverlapTest", true, minT, maxT);
            ApplicableRegionSet overlapping = rm.getApplicableRegions(td);

            // algorithm to find adjacent regions (oooh boy)
            String adjacentGroup = idToGroup.get(r.getID());
            for (ProtectedRegion pr : overlapping) {
                if (ProtectionStones.isPSRegion(pr) && pr.isOwner(WorldGuardPlugin.inst().wrapPlayer(p)) && !pr.getId().equals(r.getID())) {

                    if (adjacentGroup == null) { // if the region hasn't been found to overlap a region yet

                        if (idToGroup.get(pr.getId()) == null) { // if the overlapped region isn't part of a group yet
                            idToGroup.put(pr.getId(), r.getID());
                            idToGroup.put(r.getID(), r.getID());
                            groupToIDs.put(r.getID(), new ArrayList<>(Arrays.asList(pr.getId(), r.getID()))); // create new group
                        } else { // if the overlapped region is part of a group
                            String groupID = idToGroup.get(pr.getId());
                            idToGroup.put(r.getID(), groupID);
                            groupToIDs.get(groupID).add(r.getID());
                        }

                        adjacentGroup = idToGroup.get(r.getID());
                    } else { // if the region is part of a group already

                        if (idToGroup.get(pr.getId()) == null) { // if the overlapped region isn't part of a group
                            idToGroup.put(pr.getId(), adjacentGroup);
                            groupToIDs.get(adjacentGroup).add(pr.getId());
                        } else if (!idToGroup.get(pr.getId()).equals(adjacentGroup)) { // if the overlapped region is part of a group, merge the groups
                            String mergeGroupID = idToGroup.get(pr.getId());
                            for (String gid : groupToIDs.get(mergeGroupID)) {
                                idToGroup.put(gid, adjacentGroup);
                            }
                            groupToIDs.get(adjacentGroup).addAll(groupToIDs.get(mergeGroupID));
                            groupToIDs.remove(mergeGroupID);
                        }

                    }
                }
            }
            if (adjacentGroup == null) {
                idToGroup.put(r.getID(), r.getID());
                groupToIDs.put(r.getID(), new ArrayList<>(Collections.singletonList(r.getID())));
            }
        }
        return groupToIDs;
    }

    public static ProtectedCuboidRegion getDefaultProtectedRegion(PSProtectBlock b, PSLocation v) {
        int bx = v.x, by = v.y, bz = v.z;
        int bxo = b.xOffset, bxy = b.yOffset, bxz = b.zOffset;

        BlockVector3 min = WGUtils.getMinVector(bx + bxo, by + bxy, bz + bxz, b.xRadius, b.yRadius, b.zRadius);
        BlockVector3 max = WGUtils.getMaxVector(bx + bxo, by + bxy, bz + bxz, b.xRadius, b.yRadius, b.zRadius);

        return new ProtectedCuboidRegion(createPSID(bx, by, bz), min, max);
    }

    public static void copyRegionValues(ProtectedRegion root, ProtectedRegion copyTo) {
        copyTo.setMembers(root.getMembers());
        copyTo.setOwners(root.getOwners());
        copyTo.setFlags(root.getFlags());
        copyTo.setPriority(root.getPriority());
    }

    public static void unmergeRegion(World w, RegionManager rm, Location l) {
        String id = createPSID(l);
        for (ProtectedRegion r : rm.getApplicableRegions(BlockVector3.at(l.getX(), l.getY(), l.getZ()))) {
            if (r.getFlag(FlagHandler.PS_MERGED_REGIONS) != null && r.getFlag(FlagHandler.PS_MERGED_REGIONS).contains(id)) {
                if (r.getId().equals(id)) {

                } else {

                }
            }
        }
    }

    // merge contains ALL regions to be merged
    // root is the base flags to be copied
    public static void mergeRegions(World w, RegionManager rm, ProtectedRegion root, List<ProtectedRegion> merge) {
        List<ProtectedRegion> decomposedMerge = new ArrayList<>();

        // decompose merged regions into their bases
        for (ProtectedRegion r : merge) {
            if (r.getFlag(FlagHandler.PS_MERGED_REGIONS) != null) {
                for (String merged : r.getFlag(FlagHandler.PS_MERGED_REGIONS)) {
                    PSLocation l = parsePSRegionToLocation(merged);
                    PSProtectBlock b = ProtectionStones.getBlockOptions(w.getBlockAt(l.x, l.y, l.z).getType().toString());
                    if (b != null) {
                        decomposedMerge.add(getDefaultProtectedRegion(b, l));
                    }
                }
            } else {
                decomposedMerge.add(r);
            }
        }

        // actually merge the base regions
        ProtectedRegion nRegion = mergeRegions(root, decomposedMerge);
        rm.removeRegion(root.getId());
        for (ProtectedRegion r : merge) rm.removeRegion(r.getId());
        rm.addRegion(nRegion);
    }

    // returns a merged region; root and merge must be overlapping
    public static ProtectedRegion mergeRegions(ProtectedRegion root, List<ProtectedRegion> merge) {
        HashSet<BlockVector2> points = new HashSet<>();
        for (ProtectedRegion r : merge) {
            points.addAll(r.getPoints());
        }

        List<BlockVector2> vertex = new ArrayList<>();

        List<ProtectedRegion> regions = new ArrayList<>(merge);
        RegionTraverse.traverseRegionEdge(points, regions, tr -> {
            if (tr.isVertex) vertex.add(tr.point);
        });

        for (BlockVector2 bv : vertex) Bukkit.getLogger().info(bv.toString()); // TODO

        // merge sets of region name flag
        Set<String> regionNames = new HashSet<>();
        for (ProtectedRegion r : regions) {
            if (r.getFlag(FlagHandler.PS_MERGED_REGIONS) != null) {
                regionNames.addAll(r.getFlag(FlagHandler.PS_MERGED_REGIONS));
            } else {
                regionNames.add(r.getId());
            }
        }

        ProtectedRegion r = new ProtectedPolygonalRegion(root.getId(), vertex, 0, MAX_BUILD_HEIGHT);
        copyRegionValues(root, r);
        r.setFlag(FlagHandler.PS_MERGED_REGIONS, regionNames);
        return r;
    }
}
