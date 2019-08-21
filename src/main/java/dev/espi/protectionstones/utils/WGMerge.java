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

package dev.espi.protectionstones.utils;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.*;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.*;

public class WGMerge {

    // welcome to giant mess of code that does some bad stuff
    // :D
    // more to come in RegionTraverse

    public static void unmergeRegion(World w, RegionManager rm, PSMergedRegion toUnmerge) {

        // find the psgroupregion containing the region to unmerge
        for (ProtectedRegion r : rm.getApplicableRegions(BlockVector3.at(toUnmerge.getProtectBlock().getX(), toUnmerge.getProtectBlock().getY(), toUnmerge.getProtectBlock().getZ()))) {

            PSRegion psr = PSRegion.fromWGRegion(w, r);

            // if this is the holding region of the region to unmerge
            if (psr instanceof PSGroupRegion && ((PSGroupRegion) psr).hasMergedRegion(toUnmerge.getID())) {

                PSGroupRegion psgr = (PSGroupRegion) psr;
                psgr.removeMergedRegionInfo(toUnmerge.getID());

                // if there is only 1 region now, revert to standard region
                if (r.getFlag(FlagHandler.PS_MERGED_REGIONS).size() == 1) {

                    String[] spl = r.getFlag(FlagHandler.PS_MERGED_REGIONS_TYPES).iterator().next().split(" ");
                    String id = spl[0], type = spl[1];

                    ProtectedRegion nRegion = WGUtils.getDefaultProtectedRegion(ProtectionStones.getBlockOptions(type), WGUtils.parsePSRegionToLocation(id));
                    nRegion.copyFrom(r);
                    nRegion.setFlag(FlagHandler.PS_MERGED_REGIONS, null);
                    nRegion.setFlag(FlagHandler.PS_MERGED_REGIONS_TYPES, null);

                    rm.removeRegion(r.getId());
                    rm.addRegion(nRegion);

                } else { // otherwise, remove region

                    // check if unmerge will split the region into pieces
                    HashMap<String, String> idToGroup = new HashMap<>();
                    HashMap<String, ArrayList<String>> groupToIDs = new HashMap<>();

                    List<ProtectedRegion> toCheck = new ArrayList<>();
                    HashMap<String, PSMergedRegion> mergedRegions = new HashMap<>();

                    // add decomposed regions
                    for (PSMergedRegion ps : psgr.getMergedRegions()) {
                        mergedRegions.put(ps.getID(), ps);
                        toCheck.add(WGUtils.getDefaultProtectedRegion(ps.getTypeOptions(), WGUtils.parsePSRegionToLocation(ps.getID())));
                    }

                    // build set of groups of overlapping regions
                    for (ProtectedRegion iter : toCheck) {
                        List<ProtectedRegion> overlapping = iter.getIntersectingRegions(toCheck);
                        // algorithm to find adjacent regions (oooh boy)
                        String adjacentGroup = idToGroup.get(iter.getId());
                        for (ProtectedRegion pr : overlapping) {

                            if (adjacentGroup == null) { // if the region hasn't been found to overlap a region yet

                                if (idToGroup.get(pr.getId()) == null) { // if the overlapped region isn't part of a group yet
                                    idToGroup.put(pr.getId(), iter.getId());
                                    idToGroup.put(iter.getId(), iter.getId());
                                    groupToIDs.put(iter.getId(), new ArrayList<>(Arrays.asList(pr.getId(), iter.getId()))); // create new group
                                } else { // if the overlapped region is part of a group
                                    String groupID = idToGroup.get(pr.getId());
                                    idToGroup.put(iter.getId(), groupID);
                                    groupToIDs.get(groupID).add(iter.getId());
                                }

                                adjacentGroup = idToGroup.get(iter.getId());
                            } else { // if the region is part of a group already

                                if (idToGroup.get(pr.getId()) == null) { // if the overlapped region isn't part of a group
                                    idToGroup.put(pr.getId(), adjacentGroup);
                                    groupToIDs.get(adjacentGroup).add(pr.getId());
                                } else if (!idToGroup.get(pr.getId()).equals(adjacentGroup)) { // if the overlapped region is part of a group, merge the groups
                                    String mergeGroupID = idToGroup.get(pr.getId());
                                    for (String gid : groupToIDs.get(mergeGroupID))
                                        idToGroup.put(gid, adjacentGroup);
                                    groupToIDs.get(adjacentGroup).addAll(groupToIDs.get(mergeGroupID));
                                    groupToIDs.remove(mergeGroupID);
                                }

                            }
                        }
                        if (adjacentGroup == null) {
                            idToGroup.put(iter.getId(), iter.getId());
                            groupToIDs.put(iter.getId(), new ArrayList<>(Collections.singletonList(iter.getId())));
                        }
                    }

                    // if there is no splitting
                    if (groupToIDs.size() == 1) {
                        // actually unmerge the regions
                        if (r.getId().equals(toUnmerge.getID())) { // it is the root
                            mergeRegions(psgr.getMergedRegions().iterator().next().getID(), w, rm, psr, Arrays.asList(psr));
                        } else {
                            mergeRegions(w, rm, psr, Arrays.asList(psr));
                        }
                        continue;
                    }

                    // check how many groups there are and relabel the original root to be the head ID
                    boolean foundOriginal = false;

                    List<ProtectedRegion> regionsToAdd = new ArrayList<>();
                    for (String key : groupToIDs.keySet()) {
                        boolean found = false;
                        List<PSRegion> l = new ArrayList<>();

                        // add to cache and and also check if this set contains the original root region
                        for (String id : groupToIDs.get(key)) {
                            if (id.equals(psr.getID())) {
                                found = true;
                                foundOriginal = true;
                                break;
                            }
                            l.add(mergedRegions.get(id));
                        }

                        if (!found) { // if this set does NOT contain the root ID region
                            // remove id information from base region
                            for (String id : groupToIDs.get(key)) psgr.removeMergedRegionInfo(id);
                            regionsToAdd.add(mergeRegions(key, psr, l)); // create new region
                        }
                    }

                    // recreate original region with the new set (of removed psmergedregions)
                    if (foundOriginal) {
                        mergeRegions(w, rm, psr, Arrays.asList(psr));
                    } else {
                        rm.removeRegion(psr.getID());
                    }

                    for (ProtectedRegion pr : regionsToAdd) rm.addRegion(pr);

                }
                break;
            }
        }
    }

    // each region in merge must not be of type PSMergedRegion
    public static void mergeRegions(World w, RegionManager rm, PSRegion root, List<PSRegion> merge) {
        mergeRegions(root.getID(), w, rm, root, merge);
    }

    // merge contains ALL regions to be merged, and must ALL exist
    // root is the base flags to be copied
    public static void mergeRegions(String newID, World w, RegionManager rm, PSRegion root, List<PSRegion> merge) {
        List<PSRegion> decomposedMerge = new ArrayList<>();

        // decompose merged regions into their bases
        for (PSRegion r : merge) {
            if (r instanceof PSGroupRegion) {
                decomposedMerge.addAll(((PSGroupRegion) r).getMergedRegions());
            } else {
                decomposedMerge.add(r);
            }
        }

        // actually merge the base regions
        PSRegion nRegion = PSRegion.fromWGRegion(w, mergeRegions(newID, root, decomposedMerge));
        for (PSRegion r : merge) {
            if (!r.getID().equals(newID)) {
                // run delete event for non-root real regions
                Bukkit.getScheduler().runTask(ProtectionStones.getInstance(), () -> r.deleteRegion(false));
            } else {
                rm.removeRegion(r.getID());
            }
        }
        nRegion.setName(nRegion.getName()); // reapply name cache

        rm.addRegion(nRegion.getWGRegion());
    }

    // returns a merged region; root and merge must be overlapping
    // merge parameter must all be decomposed regions (down to cuboids, no polygon)
    private static ProtectedRegion mergeRegions(String newID, PSRegion root, List<PSRegion> merge) {
        HashSet<BlockVector2> points = new HashSet<>();
        List<ProtectedRegion> regions = new ArrayList<>();

        // decompose regions down to their points
        for (PSRegion r : merge) {
            points.addAll(WGUtils.getPointsFromDecomposedRegion(r));
            regions.add(r.getWGRegion());
        }

        // points of new region
        List<BlockVector2> vertex = new ArrayList<>();
        HashMap<Integer, ArrayList<BlockVector2>> vertexGroups = new HashMap<>();

        // traverse region edges for vertex
        RegionTraverse.traverseRegionEdge(points, regions, tr -> {
            if (tr.isVertex) {
                if (vertexGroups.containsKey(tr.vertexGroupID)) {
                    vertexGroups.get(tr.vertexGroupID).add(tr.point);
                } else {
                    vertexGroups.put(tr.vertexGroupID, new ArrayList<>(Arrays.asList(tr.point)));
                }
            }
        });

        // assemble vertex group
        // draw in and out lines between holes
        boolean first = true;
        BlockVector2 backPoint = null;
        for (List<BlockVector2> l : vertexGroups.values()) {
            if (first) {
                first = false;
                vertex.addAll(l);
                backPoint = l.get(0);
                vertex.add(backPoint);
            } else {
                vertex.addAll(l);
                vertex.add(l.get(0));
                vertex.add(backPoint);
            }
        }

        //for (BlockVector2 bv : vertex) Bukkit.getLogger().info(bv.toString()); // TODO

        // merge sets of region name flag
        Set<String> regionNames = new HashSet<>(), regionLines = new HashSet<>();
        for (PSRegion r : merge) {
            if (r.getWGRegion().getFlag(FlagHandler.PS_MERGED_REGIONS) != null) {
                regionNames.addAll(r.getWGRegion().getFlag(FlagHandler.PS_MERGED_REGIONS));
                regionLines.addAll(r.getWGRegion().getFlag(FlagHandler.PS_MERGED_REGIONS_TYPES));
            } else {
                regionNames.add(r.getID());
                regionLines.add(r.getID() + " " + r.getType());
            }
        }

        // create new merged region
        ProtectedRegion r = new ProtectedPolygonalRegion(newID, vertex, 0, WGUtils.MAX_BUILD_HEIGHT);
        r.copyFrom(root.getWGRegion());
        r.setFlag(FlagHandler.PS_MERGED_REGIONS, regionNames);
        r.setFlag(FlagHandler.PS_MERGED_REGIONS_TYPES, regionLines);
        return r;
    }

}
