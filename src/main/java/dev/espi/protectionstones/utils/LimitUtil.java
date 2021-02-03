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

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.*;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class LimitUtil {

    public static String checkAddOwner(PSPlayer psp, List<PSProtectBlock> blocksAdded) {
        HashMap<PSProtectBlock, Integer> regionLimits = psp.getRegionLimits();
        int maxPS = psp.getGlobalRegionLimits();

        if (maxPS != -1 || !regionLimits.isEmpty()) { // only check if limit was found

            // count player's protection blocks
            int total = 0;
            HashMap<PSProtectBlock, Integer> playerRegionCounts = getOwnedRegionTypeCounts(psp);

            // add the blocks
            for (PSProtectBlock block : blocksAdded) {
                playerRegionCounts.merge(block, 1, Integer::sum);
            }

            // check each limit
            for (PSProtectBlock type : playerRegionCounts.keySet()) {
                if (regionLimits.containsKey(type) && (playerRegionCounts.get(type) > regionLimits.get(type))) {
                    return PSL.ADDREMOVE_PLAYER_REACHED_LIMIT.msg();
                }

                total += playerRegionCounts.get(type);
            }

            // check if player has passed region limit
            if (total > maxPS && maxPS != -1) {
                return PSL.ADDREMOVE_PLAYER_REACHED_LIMIT.msg();
            }
        }

        return "";
    }

    public static boolean check(Player player, PSProtectBlock protectBlock) {
        if (player.hasPermission(Permissions.ADMIN)) {
            return true;
        }

        String message = LimitUtil.hasPlayerPassedRegionLimit(PSPlayer.fromPlayer(player), protectBlock);

        if (!message.isEmpty()) {
            PSL.msg(player, message);
        }

        return false;
    }

    public static boolean hasPassedOrEqualsRentLimit(Player player) {
        int limit = MiscUtil.getPermissionNumber(player, Permissions.RENT__LIMIT, -1);

        if (limit == -1) {
            return false;
        }

        int total = 0;

        // find total number of rented regions
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

        for (RegionManager regionManager : WGUtils.getAllRegionManagers().values()) {
            for (ProtectedRegion protectedRegion : regionManager.getRegions().values()) {
                if (!ProtectionStones.isPSRegion(protectedRegion)) {
                    continue;
                }

                // Player is not an owner of this region
                if (!protectedRegion.getOwners().contains(localPlayer)) {
                    continue;
                }

                PSRegion psRegion = PSRegion.fromWGRegion(player.getWorld(), protectedRegion);

                if (psRegion == null) {
                    continue;
                }

                // Player is not the tenant of this region
                if (!player.getUniqueId().equals(psRegion.getTenant())) {
                    continue;
                }

                total++;
            }
        }

        return total >= limit;
    }

    /**
     * Returns the region counts of a player (for all worlds).
     *
     * @param psp player
     * @return map of region types to the counts
     */
    private static HashMap<PSProtectBlock, Integer> getOwnedRegionTypeCounts(PSPlayer psp) {
        HashMap<PSProtectBlock, Integer> counts = new HashMap<>();
        HashMap<World, RegionManager> worldRegionManagers = WGUtils.getAllRegionManagers();

        for (World world : worldRegionManagers.keySet()) {
            RegionManager rgm = worldRegionManagers.get(world);

            rgm.getRegions().values().stream()
                    .filter(ProtectionStones::isPSRegion)
                    .filter(region -> region.getOwners().contains(psp.getUuid()))
                    .map(region -> PSRegion.fromWGRegion(world, region))
                    .filter(Objects::nonNull)
                    .forEach(region -> {
                        if (region instanceof PSGroupRegion) {
                            for (PSMergedRegion groupRegion : ((PSGroupRegion) region).getMergedRegions()) {
                                if (groupRegion.getTypeOptions() == null) {
                                    continue;
                                }

                                counts.merge(groupRegion.getTypeOptions(), 1, Integer::sum);
                            }
                        } else {
                            if (region.getTypeOptions() == null) {
                                return;
                            }

                            counts.merge(region.getTypeOptions(), 1, Integer::sum);
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
                if (type.equals(b)) {
                    bFound = playerRegionCounts.get(type);
                }
                total += playerRegionCounts.get(type);
            }

            // check if player has passed region limit
            if (total >= maxPS && maxPS != -1) {
                return PSL.REACHED_REGION_LIMIT.msg().replace("%limit%", "" + maxPS);
            }

            // check if player has passed per block limit
            if (regionLimits.get(b) != null && bFound >= regionLimits.get(b)) {
                return PSL.REACHED_PER_BLOCK_REGION_LIMIT.msg().replace("%limit%", "" + regionLimits.get(b));
            }
        }

        return "";
    }

}
