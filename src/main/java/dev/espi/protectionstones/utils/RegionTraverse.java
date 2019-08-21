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
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.ProtectionStones;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

public class RegionTraverse {
    private static final ArrayList<Vector2> DIRECTIONS = new ArrayList<>(Arrays.asList(Vector2.at(0, 1), Vector2.at(1, 0), Vector2.at(-1, 0), Vector2.at(0, -1)));
    private static final ArrayList<Vector2> CORNER_DIRECTIONS = new ArrayList<>(Arrays.asList(Vector2.at(1, 1), Vector2.at(-1, -1), Vector2.at(-1, 1), Vector2.at(1, -1)));

    public static class TraverseReturn {
        public BlockVector2 point;
        public boolean isVertex;
        public int vertexGroupID;
        public int numberOfExposedEdges;
        public TraverseReturn(BlockVector2 point, boolean isVertex, int vertexGroupID, int numberOfExposedEdges) {
            this.point = point;
            this.isVertex = isVertex;
            this.vertexGroupID = vertexGroupID;
            this.numberOfExposedEdges = numberOfExposedEdges;
        }
    }

    private static class TraverseData {
        BlockVector2 v, previous, start;
        boolean first;

    }

    private static boolean isInRegion(BlockVector2 point, List<ProtectedRegion> regions) {
        for (ProtectedRegion r : regions) {
            if (r.contains(point)) return true;
        }
        return false;
    }

    public static void traverseRegionEdge(HashSet<BlockVector2> points, List<ProtectedRegion> regions, Consumer<TraverseReturn> run) {
        int pointID = 0;
        while (!points.isEmpty()) {
            BlockVector2 start = points.iterator().next();
            traverseRegionEdge(start, null, start, true, points, regions, pointID, run);

            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~



            pointID++;
        }
    }

    // i need to find a faster way to do this
    // doesn't do so well with 1 block wide segments jutting out
    private static void traverseRegionEdge(BlockVector2 v, BlockVector2 previous, BlockVector2 start, boolean first, HashSet<BlockVector2> points, List<ProtectedRegion> regions, int pointID, Consumer<TraverseReturn> run) {
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
        points.remove(v); // remove current point if it exists

        switch (exposedEdges) {
            case 1: // normal edge
                run.accept(new TraverseReturn(v, false, pointID, exposedEdges)); // run consumer

                if (previous == null) { // if this is the first node we need to determine a direction to go to (that isn't into the polygon, but is on edge)
                    if (insideVertex.get(0).getX() == insideVertex.get(1).getZ() || insideVertex.get(0).getZ() == insideVertex.get(1).getZ() || insideVertex.get(0).getX() == insideVertex.get(2).getZ() || insideVertex.get(0).getZ() == insideVertex.get(2).getZ()) {
                        previous = insideVertex.get(0);
                    } else {
                        previous = insideVertex.get(1);
                    }
                }
                traverseRegionEdge(BlockVector2.at(v.getX() + (v.getX() - previous.getX()), v.getZ() + (v.getZ() - previous.getZ())), v, start, false, points, regions, pointID, run);
                break;
            case 2: // convex vertex
                // possibly also 1 block wide segment with 2 edges opposite, but we'll ignore that
                run.accept(new TraverseReturn(v, true, pointID, exposedEdges)); // run consumer
                if (insideVertex.get(0).equals(previous)) {
                    traverseRegionEdge(insideVertex.get(1), v, start, false, points, regions, pointID, run);
                } else {
                    traverseRegionEdge(insideVertex.get(0), v, start, false, points, regions, pointID, run);
                }
                break;
            case 3: // random 1x1 jutting out
                if (isInRegion(v, regions)) ProtectionStones.getInstance().getLogger().info("Reached impossible situation in region edge traversal at " + v.getX() + " " + v.getZ() + ", please notify the developers that you saw this message!");
                break;
            case 0: // concave vertex, or point in middle of region
                List<Vector2> cornersNotIn = new ArrayList<>();
                for (Vector2 dir : CORNER_DIRECTIONS) {
                    BlockVector2 test = BlockVector2.at(v.getX() + dir.getX(), v.getZ() + dir.getZ());

                    if (!isInRegion(test, regions)) cornersNotIn.add(dir);
                }

                if (cornersNotIn.size() == 1) { // concave vertex
                    run.accept(new TraverseReturn(v, true, pointID, exposedEdges)); // run consumer

                    Vector2 dir = cornersNotIn.get(0);
                    if (previous == null || previous.equals(BlockVector2.at(v.getX() + dir.getX(), v.getZ()))) {
                        traverseRegionEdge(BlockVector2.at(v.getX(), v.getZ() + dir.getZ()), v, start, false, points, regions, pointID, run);
                    } else {
                        traverseRegionEdge(BlockVector2.at(v.getX() + dir.getX(), v.getZ()), v, start, false, points, regions, pointID, run);
                    }
                } else if (cornersNotIn.size() == 2) { // 1 block diagonal perfect overlap
                    run.accept(new TraverseReturn(v, false, pointID, exposedEdges)); // run consumer

                    if (previous == null) previous = insideVertex.get(0);
                    traverseRegionEdge(BlockVector2.at(v.getX() + (v.getX() - previous.getX()), v.getZ() + (v.getZ() - previous.getZ())), v, start, false, points, regions, pointID, run);
                }
                // ignore if in middle of region (cornersNotIn size = 0)
                break;
        }

    }
}