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
import dev.espi.protectionstones.PSLocation;
import dev.espi.protectionstones.PSProtectBlock;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
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

    public static void copyRegionValues(ProtectedRegion root, ProtectedRegion copyTo) {
        copyTo.setMembers(root.getMembers());
        copyTo.setOwners(root.getOwners());
        copyTo.setFlags(root.getFlags());
        copyTo.setPriority(root.getPriority());
    }

    private static final ArrayList<Vector2> DIRECTIONS = (ArrayList<Vector2>) Arrays.asList(Vector2.at(0, 1), Vector2.at(1, 0), Vector2.at(-1, 0), Vector2.at(0, -1));
    private static final ArrayList<Vector2> CORNER_DIRECTIONS = (ArrayList<Vector2>) Arrays.asList(Vector2.at(1, 1), Vector2.at(-1, -1), Vector2.at(-1, 1), Vector2.at(1, -1));

    private static boolean isInRegion(BlockVector2 point, List<ProtectedRegion> regions) {
        for (ProtectedRegion r : regions) {
            if (r.contains(point)) return true;
        }
        return false;
    }

    // i need to find a faster way to do this
    // doesn't do so well with 1 block wide segments jutting out
    private static void dfsRegionEdge(BlockVector2 v, BlockVector2 previous, BlockVector2 start, boolean first, List<ProtectedRegion> regions, List<BlockVector2> vertex) {
        if (!first && v.equals(start)) return;

        int exposedEdges = 0;
        List<BlockVector2> insideVertex = new ArrayList<>();
        for (Vector2 dir : DIRECTIONS) {
            BlockVector2 test = BlockVector2.at(v.getX() + dir.getX(), v.getZ() + dir.getZ());
            if (!isInRegion(test, regions)) {
                exposedEdges++;
            } else {
                insideVertex.add(test);
            }
        }

        switch (exposedEdges) {
            case 1: // normal edge
                // note: will fail if previous is null? possible todo
                dfsRegionEdge(BlockVector2.at(v.getX() + (v.getX() - previous.getX()), v.getZ() + (v.getZ() - previous.getZ())), v, start, false, regions, vertex);
                break;
            case 2: // convex vertex
                // possibly also 1 block wide segment with 2 edges opposite, but we'll ignore that
                vertex.add(v);
                if (insideVertex.get(0).equals(previous)) {
                    dfsRegionEdge(insideVertex.get(1), v, start, false, regions, vertex);
                } else {
                    dfsRegionEdge(insideVertex.get(0), v, start, false, regions, vertex);
                }
                break;
            case 3: // random 1x1 jutting out
                // ignore
                ProtectionStones.getInstance().getLogger().info("Reached impossible situation in region merge, please notify the developers that you saw this message!");
                break;
            case 4: // concave vertex
                vertex.add(v);
                for (Vector2 dir : CORNER_DIRECTIONS) {
                    BlockVector2 test = BlockVector2.at(v.getX() + dir.getX(), v.getZ() + dir.getZ());
                    if (!isInRegion(test, regions)) {
                        if (previous.equals(BlockVector2.at(v.getX() + dir.getX(), v.getZ()))) {
                            dfsRegionEdge(BlockVector2.at(v.getX(), v.getZ() + dir.getZ()), v, start, false, regions, vertex);
                        } else {
                            dfsRegionEdge(BlockVector2.at(v.getX() + dir.getX(), v.getZ()), v, start, false, regions, vertex);
                        }
                    }
                }
                break;
        }

    }

    // returns a merged region; root and merge must be overlapping
    public static ProtectedRegion mergeRegions(ProtectedRegion root, ProtectedRegion merge) {
        List<BlockVector2> points = new ArrayList<>();
        points.addAll(root.getPoints());
        points.addAll(merge.getPoints());

        // find starting point
        BlockVector2 start = null;
        for (BlockVector2 p : points) {
            if (!(root.contains(p) && merge.contains(p))) {
                start = p;
            }
        }

        // the 2 regions are exactly intersecting
        if (start == null) return root;

        List<BlockVector2> vertex = new ArrayList<>();

        dfsRegionEdge(start, null, start, true, Arrays.asList(root, merge), vertex);

        ProtectedRegion r = new ProtectedPolygonalRegion(root.getId(), vertex, 0, MAX_BUILD_HEIGHT);
        copyRegionValues(root, r);
        return r;
    }
}
