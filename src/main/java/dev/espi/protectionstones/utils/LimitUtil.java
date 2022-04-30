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
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

public class LimitUtil {

    // warning: group regions should be split into merged regions first
    public static String checkAddOwner(PSPlayer psp, List<PSProtectBlock> blocksAdded) {
        HashMap<PSProtectBlock, Integer> regionLimits = psp.getRegionLimits();
        int maxPS = psp.getGlobalRegionLimits();

        ProtectionStones.getInstance().debug(String.format("Player's global limit is %d.", maxPS));
        ProtectionStones.getInstance().debug(String.format("Player has limits on %d region types.", regionLimits.size()));

        if (maxPS != -1 || !regionLimits.isEmpty()) { // only check if limit was found

            // count player's protection blocks
            int total = 0;
            HashMap<PSProtectBlock, Integer> playerRegionCounts = getOwnedRegionTypeCounts(psp);

            // add the blocks
            for (PSProtectBlock b : blocksAdded) {
                ProtectionStones.getInstance().debug(String.format("Adding region type %s.", b.alias));
                if (playerRegionCounts.containsKey(b)) {
                    playerRegionCounts.put(b, playerRegionCounts.get(b)+1);
                } else {
                    playerRegionCounts.put(b, 1);
                }
            }

            // check each limit
            for (PSProtectBlock type : playerRegionCounts.keySet()) {
                if (regionLimits.containsKey(type)) {
                    ProtectionStones.getInstance().debug(String.format("Of type %s: player will have %d regions - Player's limit is %d regions.", type.alias, playerRegionCounts.get(type), regionLimits.get(type)));
                    if (playerRegionCounts.get(type) > regionLimits.get(type)) {
                        return PSL.ADDREMOVE_PLAYER_REACHED_LIMIT.msg();
                    }
                }
                total += playerRegionCounts.get(type);
            }

            // check if player has passed region limit
            ProtectionStones.getInstance().debug(String.format("The player will have %d regions in total. Their limit is %d.", total, maxPS));
            if (total > maxPS && maxPS != -1) {
                return PSL.ADDREMOVE_PLAYER_REACHED_LIMIT.msg();
            }
        }
        return "";
    }

    public static boolean check(Player p, PSProtectBlock b) {
        if (!p.hasPermission("protectionstones.admin")) {
            // check if player has limit on protection stones
            String msg = LimitUtil.hasPlayerPassedRegionLimit(PSPlayer.fromPlayer(p), b);
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

    /**
     * Returns the region counts of a player (for all worlds).
     * @param psp player
     * @return map of region types to the counts
     */
    private static HashMap<PSProtectBlock, Integer> getOwnedRegionTypeCounts(PSPlayer psp) {
        if (ProtectionStones.getInstance().isDebug()) { // psp.getName may incur a performance penalty
            ProtectionStones.getInstance().debug(String.format("Debug limits for: %s", psp.getName()));
        }

        HashMap<PSProtectBlock, Integer> counts = new HashMap<>();
        HashMap<World, RegionManager> m = WGUtils.getAllRegionManagers();

        for (World w : m.keySet()) {
            psp.getPSRegions(w, false).forEach(r -> {
                if (r instanceof PSGroupRegion) {
                    ProtectionStones.getInstance().debug(String.format("Checking group region %s's (world %s) (type %s) regions:", r.getId(), w.getName(), r.getTypeOptions().alias));
                    for (PSMergedRegion psmr : ((PSGroupRegion) r).getMergedRegions()) {
                        if (psmr.getTypeOptions() == null) continue;
                        if (!counts.containsKey(psmr.getTypeOptions())) {
                            counts.put(psmr.getTypeOptions(), 1);
                        } else {
                            counts.put(psmr.getTypeOptions(), counts.get(psmr.getTypeOptions())+1);
                        }

                        ProtectionStones.getInstance().debug(String.format("Merged region %s (world %s) (type %s)", psmr.getId(), w.getName(), psmr.getTypeOptions().alias));
                    }
                } else {
                    if (r.getTypeOptions() == null) return;
                    if (!counts.containsKey(r.getTypeOptions())) {
                        counts.put(r.getTypeOptions(), 1);
                    } else {
                        counts.put(r.getTypeOptions(), counts.get(r.getTypeOptions())+1);
                    }
                    ProtectionStones.getInstance().debug(String.format("Region %s (world %s) (type %s)", r.getId(), w.getName(), r.getTypeOptions().alias));
                }
            });
        }
        return counts;
    }

    private static String hasPlayerPassedRegionLimit(PSPlayer psp, PSProtectBlock b) {
        HashMap<PSProtectBlock, Integer> regionLimits = psp.getRegionLimits();
        int maxPS = psp.getGlobalRegionLimits();

        if (maxPS != -1 || !regionLimits.isEmpty()) { // only check if limit was found

            // count player's protection stones
            int total = 0, bFound = 0;
            HashMap<PSProtectBlock, Integer> playerRegionCounts = getOwnedRegionTypeCounts(psp);
            for (PSProtectBlock type : playerRegionCounts.keySet()) {
                ProtectionStones.getInstance().debug(String.format("Adding region type %s.", b.alias));
                if (type.equals(b)) {
                    bFound = playerRegionCounts.get(type);
                }
                total += playerRegionCounts.get(type);
            }

            // check if player has passed region limit
            ProtectionStones.getInstance().debug(String.format("The player will have %d regions in total. Their limit is %d.", total, maxPS));
            if (total >= maxPS && maxPS != -1) {
                return PSL.REACHED_REGION_LIMIT.msg().replace("%limit%", ""+maxPS);
            }

            // check if player has passed per block limit
            ProtectionStones.getInstance().debug(String.format("Of type %s: player will have %d regions - Player's limit is %d regions.", b.alias, bFound, regionLimits.get(b) == null ? -1 : regionLimits.get(b)));
            if (regionLimits.get(b) != null && bFound >= regionLimits.get(b)) {
                return PSL.REACHED_PER_BLOCK_REGION_LIMIT.msg().replace("%limit%", ""+regionLimits.get(b));
            }
        }
        return "";
    }

}
