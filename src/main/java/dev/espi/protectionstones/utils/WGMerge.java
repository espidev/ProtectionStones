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

package dev.espi.protectionstones.utils;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.RemovalStrategy;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.*;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.*;

public class WGMerge {

    public static class RegionHoleException extends Exception {
    }

    public static class RegionCannotMergeWhileRentedException extends Exception {
        PSRegion rentedRegion;

        RegionCannotMergeWhileRentedException(PSRegion r) {
            rentedRegion = r;
        }

        public PSRegion getRentedRegion() {
            return rentedRegion;
        }
    }

    // welcome to giant mess of code that does some bad stuff
    // :D
    // more to come in RegionTraverse

    // build groups of overlapping regions in idToGroup and groupToIDs
    public static void findOverlappingRegionGroups(World w, List<ProtectedRegion> regions, HashMap<String, String> idToGroup, HashMap<String, ArrayList<String>> groupToIDs) {
        for (ProtectedRegion iter : regions) {
            Set<ProtectedRegion> overlapping = WGUtils.findOverlapOrAdjacentRegions(iter, regions, w);
            // algorithm to find adjacent regions
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
    }

    public static void unmergeRegion(World w, RegionManager rm, PSMergedRegion toUnmerge) throws RegionHoleException, RegionCannotMergeWhileRentedException {
        PSGroupRegion psr = toUnmerge.getGroupRegion(); // group region
        ProtectedRegion r = psr.getWGRegion();

        String blockType = toUnmerge.getType();
        try {
            // remove the actual region info
            psr.removeMergedRegionInfo(toUnmerge.getId());

            // if there is only 1 region now, revert to standard region
            if (r.getFlag(FlagHandler.PS_MERGED_REGIONS).size() == 1) {

                String[] spl = r.getFlag(FlagHandler.PS_MERGED_REGIONS_TYPES).iterator().next().split(" ");
                String id = spl[0], type = spl[1];

                ProtectedRegion nRegion = WGUtils.getDefaultProtectedRegion(ProtectionStones.getBlockOptions(type), WGUtils.parsePSRegionToLocation(id));
                nRegion.copyFrom(r);
                nRegion.setFlag(FlagHandler.PS_BLOCK_MATERIAL, type);
                nRegion.setFlag(FlagHandler.PS_MERGED_REGIONS, null);
                nRegion.setFlag(FlagHandler.PS_MERGED_REGIONS_TYPES, null);

                // reapply name cache
                PSRegion rr = PSRegion.fromWGRegion(w, nRegion);
                rr.setName(rr.getName());

                rm.removeRegion(r.getId());
                rm.addRegion(nRegion);

            } else { // otherwise, remove region

                // check if unmerge will split the region into pieces
                HashMap<String, String> idToGroup = new HashMap<>();
                HashMap<String, ArrayList<String>> groupToIDs = new HashMap<>();

                List<ProtectedRegion> toCheck = new ArrayList<>();
                HashMap<String, PSMergedRegion> mergedRegions = new HashMap<>();

                // add decomposed regions
                for (PSMergedRegion ps : psr.getMergedRegions()) {
                    mergedRegions.put(ps.getId(), ps);
                    toCheck.add(WGUtils.getDefaultProtectedRegion(ps.getTypeOptions(), WGUtils.parsePSRegionToLocation(ps.getId())));
                }

                // build set of groups of overlapping regions
                findOverlappingRegionGroups(w, toCheck, idToGroup, groupToIDs);

                // check how many groups there are and relabel the original root to be the head ID
                boolean foundOriginal = false;

                List<ProtectedRegion> regionsToAdd = new ArrayList<>();

                // loop over each set of overlapping region groups and add create full region for each
                for (String key : groupToIDs.keySet()) {
                    boolean found = false;
                    List<PSRegion> l = new ArrayList<>();
                    PSRegion newRoot = null;
                    try {
                        // loop over regions in a group
                        // add to cache and and also check if this set contains the original root region
                        for (String id : groupToIDs.get(key)) {
                            if (id.equals(psr.getId())) { // original root region
                                found = true;
                                foundOriginal = true;
                                break;
                            }
                            if (id.equals(key)) { // new root region
                                newRoot = mergedRegions.get(id);
                            }
                            l.add(mergedRegions.get(id));
                        }

                        if (!found) { // if this set does NOT contain the root ID region
                            // remove id information from base region
                            for (String id : groupToIDs.get(key)) psr.removeMergedRegionInfo(id);

                            // split off from base region
                            ProtectedRegion split = mergeRegions(key, psr, l);
                            split.setFlag(FlagHandler.PS_BLOCK_MATERIAL, newRoot.getType()); // apply new block type
                            regionsToAdd.add(split); // create new region
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // recreate original region with the new set (of removed psmergedregions)
                if (foundOriginal) {
                    mergeRegions(w, rm, psr, Arrays.asList(psr));
                } else {
                    psr.setName(null); // remove name from cache
                    rm.removeRegion(psr.getId(), RemovalStrategy.UNSET_PARENT_IN_CHILDREN);
                }

                // add all regions that do NOT contain the root ID region
                for (ProtectedRegion pr : regionsToAdd) {
                    PSRegion rr = PSRegion.fromWGRegion(w, pr);
                    rr.setName(rr.getName()); // reapply name cache
                    rm.addRegion(pr);
                }

            }

        } catch (RegionHoleException | RegionCannotMergeWhileRentedException e) {
            // if there is a region hole exception, put back the merged region info
            psr.getWGRegion().getFlag(FlagHandler.PS_MERGED_REGIONS).add(toUnmerge.getId());
            psr.getWGRegion().getFlag(FlagHandler.PS_MERGED_REGIONS_TYPES).add(toUnmerge.getId() + " " + blockType);
            throw e;
        }
    }

    // additional behaviour for merging region flags
    private static void mergeRegionFlags(List<PSRegion> baseRegions, PSRegion mergedRegion) {
        Set<String> taxPaymentsDue = mergedRegion.getWGRegion().getFlag(FlagHandler.PS_TAX_PAYMENTS_DUE);
        Set<String> lastTaxAdditions = mergedRegion.getWGRegion().getFlag(FlagHandler.PS_TAX_LAST_PAYMENT_ADDED);
        if (taxPaymentsDue == null) taxPaymentsDue = new HashSet<>();
        if (lastTaxAdditions == null) lastTaxAdditions = new HashSet<>();

        for (PSRegion r : baseRegions) {
            // merge owners and members list
            mergedRegion.getWGRegion().getOwners().addAll(r.getWGRegion().getOwners());
            mergedRegion.getWGRegion().getMembers().addAll(r.getWGRegion().getMembers());

            // merge tax payments
            if (r.getWGRegion().getFlag(FlagHandler.PS_TAX_PAYMENTS_DUE) != null) {
                taxPaymentsDue.addAll(r.getWGRegion().getFlag(FlagHandler.PS_TAX_PAYMENTS_DUE));
            }
            if (r.getWGRegion().getFlag(FlagHandler.PS_TAX_LAST_PAYMENT_ADDED) != null) {
                lastTaxAdditions.addAll(r.getWGRegion().getFlag(FlagHandler.PS_TAX_LAST_PAYMENT_ADDED));
            }
        }

        mergedRegion.getWGRegion().setFlag(FlagHandler.PS_TAX_PAYMENTS_DUE, taxPaymentsDue);
        mergedRegion.getWGRegion().setFlag(FlagHandler.PS_TAX_LAST_PAYMENT_ADDED, lastTaxAdditions);
    }

    // the regions in the merge list must actually exist
    // this is used by player merge interfaces
    public static PSRegion mergeRealRegions(World w, RegionManager rm, PSRegion root, List<PSRegion> merge) throws RegionHoleException, RegionCannotMergeWhileRentedException {
        PSRegion r = mergeRegions(w, rm, root, merge);
        mergeRegionFlags(merge, r);
        return r;
    }

    // each region in merge must not be of type PSMergedRegion
    private static PSRegion mergeRegions(World w, RegionManager rm, PSRegion root, List<PSRegion> merge) throws RegionHoleException, RegionCannotMergeWhileRentedException {
        return mergeRegions(root.getId(), w, rm, root, merge);
    }

    // merge contains ALL regions to be merged, and must ALL exist
    // root is the base flags to be copied
    public static PSRegion mergeRegions(String newID, World w, RegionManager rm, PSRegion root, List<PSRegion> merge) throws RegionHoleException, RegionCannotMergeWhileRentedException {
        List<PSRegion> decomposedMerge = new ArrayList<>();

        // decompose merged regions into their bases
        for (PSRegion r : merge) {
            if (r.getRentStage() != PSRegion.RentStage.NOT_RENTING) {
                throw new RegionCannotMergeWhileRentedException(r);
            }

            if (r instanceof PSGroupRegion) {
                decomposedMerge.addAll(((PSGroupRegion) r).getMergedRegions());
            } else {
                decomposedMerge.add(r);
            }
        }

        // actually merge the base regions
        PSRegion nRegion = PSRegion.fromWGRegion(w, mergeRegions(newID, root, decomposedMerge));
        for (PSRegion r : merge) {
            if (!r.getId().equals(newID)) {
                // run delete event for non-root real regions
                Bukkit.getScheduler().runTask(ProtectionStones.getInstance(), () -> r.deleteRegion(false));
            } else {
                rm.removeRegion(r.getId());
            }
        }
        try {
            nRegion.setName(nRegion.getName()); // reapply name cache
        } catch (NullPointerException ignored) {
        } // catch nulls

        rm.addRegion(nRegion.getWGRegion());
        return nRegion;
    }

    // returns a merged region; root and merge must be overlapping or adjacent
    // merge parameter must all be decomposed regions (down to cuboids, no polygon)
    private static ProtectedRegion mergeRegions(String newID, PSRegion root, List<PSRegion> merge) throws RegionHoleException {
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

        // allow_merging_holes option
        // prevent holes from being formed
        if (vertexGroups.size() > 1 && !ProtectionStones.getInstance().getConfigOptions().allowMergingHoles) {
            throw new RegionHoleException();
        }

        // assemble vertex group
        // draw in and out lines between holes
        boolean first = true;
        BlockVector2 backPoint = null;
        for (List<BlockVector2> l : vertexGroups.values()) {
            if (first) {
                first = false;
                vertex.addAll(l);
                backPoint = l.get(0);
            } else {
                vertex.addAll(l);
                vertex.add(l.get(0));
            }
            vertex.add(backPoint);
        }

        // merge sets of region name flag
        Set<String> regionNames = new HashSet<>(), regionLines = new HashSet<>();
        for (PSRegion r : merge) {
            if (r.getWGRegion().getFlag(FlagHandler.PS_MERGED_REGIONS) != null) {
                regionNames.addAll(r.getWGRegion().getFlag(FlagHandler.PS_MERGED_REGIONS));
                regionLines.addAll(r.getWGRegion().getFlag(FlagHandler.PS_MERGED_REGIONS_TYPES));
            } else {
                regionNames.add(r.getId());
                regionLines.add(r.getId() + " " + r.getType());
            }
        }

        // create new merged region
        ProtectedRegion r = new ProtectedPolygonalRegion(newID, vertex, WGUtils.MIN_BUILD_HEIGHT, WGUtils.MAX_BUILD_HEIGHT);

        r.copyFrom(root.getWGRegion());
        // only make it a merged region if there is more than one contained region
        if (regionNames.size() > 1 && regionLines.size() > 1) {
            r.setFlag(FlagHandler.PS_MERGED_REGIONS, regionNames);
            r.setFlag(FlagHandler.PS_MERGED_REGIONS_TYPES, regionLines);
        } else {
            r.setFlag(FlagHandler.PS_MERGED_REGIONS, null);
            r.setFlag(FlagHandler.PS_MERGED_REGIONS_TYPES, null);
        }
        return r;
    }

}
