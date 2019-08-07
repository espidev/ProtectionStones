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
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.PSLocation;
import dev.espi.protectionstones.PSProtectBlock;
import dev.espi.protectionstones.ProtectionStones;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

public class WGUtils {

    private static final int MAX_BUILD_HEIGHT = 256;

    public static RegionManager getRegionManagerWithPlayer(Player p) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(p.getWorld()));
    }

    public static RegionManager getRegionManagerWithWorld(World w){
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
}
