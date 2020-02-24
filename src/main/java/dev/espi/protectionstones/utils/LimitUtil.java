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

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class LimitUtil {

    public static boolean check(Player p, PSProtectBlock b) {
        if (!p.hasPermission("protectionstones.admin")) {
            // check if player has limit on protection stones
            String msg = LimitUtil.hasPlayerPassedRegionLimit(p, b);
            if (!msg.isEmpty()) {
                PSL.msg(p, msg);
                return false;
            }
        }

        return true;
    }

    public static boolean hasPassedOrEqualsRentLimit(Player p) {
        int lim = MiscUtil.getPermissionNumber(p, "protectionstones.rent.limit.", -1);
        if (lim != -1) {
            int total = 0;

            // find total number of rented regions
            HashMap<World, RegionManager> m = WGUtils.getAllRegionManagers();
            for (World w : m.keySet()) {
                RegionManager rgm = m.get(w);
                for (ProtectedRegion r : rgm.getRegions().values()) {
                    if (ProtectionStones.isPSRegion(r) && r.getOwners().contains(WorldGuardPlugin.inst().wrapPlayer(p))) {
                        PSRegion psr = PSRegion.fromWGRegion(p.getWorld(), r);

                        if (psr != null && psr.getTenant() != null && psr.getTenant().equals(p.getUniqueId())) total++;
                    }
                }
            }

            return total >= lim;
        }
        return false;
    }

    private static String hasPlayerPassedRegionLimit(Player p, PSProtectBlock b) {
        PSPlayer psp = PSPlayer.fromPlayer(p);
        HashMap<PSProtectBlock, Integer> regionLimits = psp.getRegionLimits();
        int maxPS = psp.getGlobalRegionLimits();

        if (maxPS != -1 || !regionLimits.isEmpty()) { // only check if limit was found

            // count player's protection stones
            int total = 0, bFound = 0;
            HashMap<World, RegionManager> m = WGUtils.getAllRegionManagers();
            for (World w : m.keySet()) {
                RegionManager rgm = m.get(w);
                for (ProtectedRegion r : rgm.getRegions().values()) {
                    if (ProtectionStones.isPSRegion(r) && r.getOwners().contains(WorldGuardPlugin.inst().wrapPlayer(p))) {
                        PSRegion psr = PSRegion.fromWGRegion(p.getWorld(), r);

                        if (psr instanceof PSGroupRegion) {
                            for (PSMergedRegion psmr : ((PSGroupRegion) psr).getMergedRegions()) {
                                total++;
                                if (psmr.getType().equals(b.type)) bFound++; // if the specific block was found
                            }
                        } else {
                            total++;
                            if (psr.getType().equals(b.type)) bFound++; // if the specific block was found
                        }
                    }
                }
            }

            //Bukkit.getLogger().info("TOTAL: " + total + ", FOUND: " + bFound); // TODO

            // check if player has passed region limit
            if (total >= maxPS && maxPS != -1) {
                return PSL.REACHED_REGION_LIMIT.msg().replace("%limit%", ""+maxPS);
            }

            // check if player has passed per block limit
            if (regionLimits.get(b) != null && bFound >= regionLimits.get(b)) {
                return PSL.REACHED_PER_BLOCK_REGION_LIMIT.msg().replace("%limit%", ""+regionLimits.get(b));
            }
        }
        return "";
    }

}
