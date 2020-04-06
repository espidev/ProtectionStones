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

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

public class RegionTraverse {
    private static final ArrayList<Vector2D> DIRECTIONS = new ArrayList<>(Arrays.asList(new Vector2D(0, 1), new Vector2D(1, 0), new Vector2D(-1, 0), new Vector2D(0, -1)));
    private static final ArrayList<Vector2D> CORNER_DIRECTIONS = new ArrayList<>(Arrays.asList(new Vector2D(1, 1), new Vector2D(-1, -1), new Vector2D(-1, 1), new Vector2D(1, -1)));

    public static class TraverseReturn {
        public BlockVector2D point;
        public boolean isVertex;
        public int vertexGroupID;
        public int numberOfExposedEdges;
        public TraverseReturn(BlockVector2D point, boolean isVertex, int vertexGroupID, int numberOfExposedEdges) {
            this.point = point;
            this.isVertex = isVertex;
            this.vertexGroupID = vertexGroupID;
            this.numberOfExposedEdges = numberOfExposedEdges;
        }
    }

    private static class TraverseData {
        BlockVector2D v, previous;
        boolean first;

        TraverseData(BlockVector2D v, BlockVector2D previous, boolean first) {
            this.v = v;
            this.previous = previous;
            this.first = first;
        }
    }

    private static boolean isInRegion(BlockVector2D point, List<ProtectedRegion> regions) {
        for (ProtectedRegion r : regions)
            if (r.contains(point)) return true;
        return false;
    }


    // can't use recursion because stack overflow
    // doesn't do so well with 1 block wide segments jutting out
    public static void traverseRegionEdge(HashSet<BlockVector2D> points, List<ProtectedRegion> regions, Consumer<TraverseReturn> run) {
        int pointID = 0;
        while (!points.isEmpty()) {
            BlockVector2D start = points.iterator().next();
            TraverseData td = new TraverseData(start, null, true);

            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ algorithm starts ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

            boolean cont = true;
            while (cont) {
                cont = false;
                BlockVector2D v = td.v, previous = td.previous;

                if (!td.first && v.equals(start)) break;

                int exposedEdges = 0;
                List<BlockVector2D> insideVertex = new ArrayList<>();
                for (Vector2D dir : DIRECTIONS) {
                    BlockVector2D test = new BlockVector2D(v.getX() + dir.getX(), v.getZ() + dir.getZ());
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
                        td = new TraverseData(new BlockVector2D(v.getX() + (v.getX() - previous.getX()), v.getZ() + (v.getZ() - previous.getZ())), v, false);
                        cont = true;
                        break;
                    case 2: // convex vertex
                        // possibly also 1 block wide segment with 2 edges opposite, but we'll ignore that
                        run.accept(new TraverseReturn(v, true, pointID, exposedEdges)); // run consumer
                        if (insideVertex.get(0).equals(previous)) {
                            td = new TraverseData(insideVertex.get(1), v, false);
                            cont = true;
                        } else {
                            td = new TraverseData(insideVertex.get(0), v, false);
                            cont = true;
                        }
                        break;
                    case 3: // random 1x1 jutting out
                        //if (isInRegion(v, regions)) ProtectionStones.getInstance().getLogger().info("Reached impossible situation in region edge traversal at " + v.getX() + " " + v.getZ() + ", please notify the developers that you saw this message!");
                        // it's fine right now but it'd be nice if it worked
                        break;
                    case 0: // concave vertex, or point in middle of region
                        List<Vector2D> cornersNotIn = new ArrayList<>();
                        for (Vector2D dir : CORNER_DIRECTIONS) {
                            BlockVector2D test = new BlockVector2D(v.getX() + dir.getX(), v.getZ() + dir.getZ());

                            if (!isInRegion(test, regions)) cornersNotIn.add(dir);
                        }

                        if (cornersNotIn.size() == 1) { // concave vertex
                            run.accept(new TraverseReturn(v, true, pointID, exposedEdges)); // run consumer

                            Vector2D dir = cornersNotIn.get(0);
                            if (previous == null || previous.equals(new BlockVector2D(v.getX() + dir.getX(), v.getZ()))) {
                                td = new TraverseData(new BlockVector2D(v.getX(), v.getZ() + dir.getZ()), v, false);
                                cont = true;
                            } else {
                                td = new TraverseData(new BlockVector2D(v.getX() + dir.getX(), v.getZ()), v, false);
                                cont = true;
                            }
                        } else if (cornersNotIn.size() == 2) { // 1 block diagonal perfect overlap
                            run.accept(new TraverseReturn(v, false, pointID, exposedEdges)); // run consumer

                            if (previous == null) previous = insideVertex.get(0);
                            td = new TraverseData(new BlockVector2D(v.getX() + (v.getX() - previous.getX()), v.getZ() + (v.getZ() - previous.getZ())), v, false);
                            cont = true;
                        }
                        // ignore if in middle of region (cornersNotIn size = 0)
                        break;
                }

            }

            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ algorithm ends ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

            pointID++;
        }
    }

}
