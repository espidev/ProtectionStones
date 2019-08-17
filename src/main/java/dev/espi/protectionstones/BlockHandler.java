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

package dev.espi.protectionstones;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.event.PSCreateEvent;
import dev.espi.protectionstones.utils.MiscUtil;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.*;

class BlockHandler {
    private static HashMap<Player, Double> lastProtectStonePlaced = new HashMap<>();

    private static String checkCooldown(Player p) {
        double currentTime = System.currentTimeMillis();
        if (lastProtectStonePlaced.containsKey(p)) {
            double cooldown = ProtectionStones.getInstance().getConfigOptions().placingCooldown; // seconds
            double lastPlace = lastProtectStonePlaced.get(p); // milliseconds

            if (lastPlace + cooldown * 1000 > currentTime) { // if cooldown has not been finished
                return String.format("%.1f", cooldown - ((currentTime - lastPlace) / 1000));
            }
            lastProtectStonePlaced.remove(p);
        }
        lastProtectStonePlaced.put(p, currentTime);
        return null;
    }

    private static String hasPlayerPassedRegionLimit(Player p, PSProtectBlock b) {
        HashMap<PSProtectBlock, Integer> regionLimits = ProtectionStones.getPlayerRegionLimits(p);
        int maxPS = ProtectionStones.getPlayerGlobalRegionLimits(p);

        if (maxPS != -1 || !regionLimits.isEmpty()) { // only check if limit was found
            // count player's protection stones
            int total = 0, bFound = 0;
            for (World w : Bukkit.getWorlds()) {
                RegionManager rgm = WGUtils.getRegionManagerWithWorld(w);
                for (ProtectedRegion r : rgm.getRegions().values()) {
                    if (ProtectionStones.isPSRegion(r) && r.getOwners().contains(WorldGuardPlugin.inst().wrapPlayer(p))) {
                        String f = r.getFlag(FlagHandler.PS_BLOCK_MATERIAL);
                        total++;
                        if (f.equals(b.type)) bFound++; // if the specific block was found
                    }
                }
            }
            // check if player has passed region limit
            if (total >= maxPS && maxPS != -1) {
                return PSL.REACHED_REGION_LIMIT.msg();
            }

            // check if player has passed per block limit
            if (regionLimits.get(b) != null && bFound >= regionLimits.get(b)) {
                return PSL.REACHED_PER_BLOCK_REGION_LIMIT.msg();
            }
        }
        return "";
    }

    private static boolean isFarEnoughFromOtherClaims(PSProtectBlock blockOptions, RegionManager rm, LocalPlayer lp, double bx, double by, double bz) {
        BlockVector3 min = WGUtils.getMinVector(bx, by, bz, blockOptions.distanceBetweenClaims, blockOptions.distanceBetweenClaims, blockOptions.distanceBetweenClaims);
        BlockVector3 max = WGUtils.getMaxVector(bx, by, bz, blockOptions.distanceBetweenClaims, blockOptions.distanceBetweenClaims, blockOptions.distanceBetweenClaims);

        ProtectedRegion td = new ProtectedCuboidRegion("regionRadiusTest" + (long) (bx + by + bz), true, min, max);
        td.setPriority(blockOptions.priority);
        return !WGUtils.overlapsStrongerRegion(rm, td, lp);
    }

    // create PS region from a block place event
    static void createPSRegion(BlockPlaceEvent e) {
        if (e.isCancelled()) return;
        Player p = e.getPlayer();
        Block b = e.getBlock();

        // check if the block is a protection stone
        if (!ProtectionStones.isProtectBlockType(b.getType().toString())) return;
        PSProtectBlock blockOptions = ProtectionStones.getBlockOptions(b.getType().toString());

        // check if the item was created by protection stones (stored in custom tag)
        // block must have restrictObtaining enabled for blocking place
        if (blockOptions.restrictObtaining && !ProtectionStones.isProtectBlockItem(e.getItemInHand(), true)) return;

        // check if player has toggled off placement of protection stones
        if (ProtectionStones.toggleList.contains(p.getUniqueId())) return;

        // check if player can place block in that area
        if (!WorldGuardPlugin.inst().createProtectionQuery().testBlockPlace(p, b.getLocation(), b.getType())) {
            PSL.msg(p, PSL.CANT_PROTECT_THAT.msg());
            e.setCancelled(true);
            return;
        }

        if (!createPSRegion(p, b.getLocation(), blockOptions)) {
            e.setCancelled(true);
        }
    }

    // create a PS region (no checks for items)
    static boolean createPSRegion(Player p, Location l, PSProtectBlock blockOptions) {
        // check permission
        if (!p.hasPermission("protectionstones.create") || (!blockOptions.permission.equals("") && !p.hasPermission(blockOptions.permission))) {
            PSL.msg(p, PSL.NO_PERMISSION_CREATE.msg());
            return false;
        }

        // check cooldown
        if (ProtectionStones.getInstance().getConfigOptions().placingCooldown != -1) {
            String time = checkCooldown(p);
            if (time != null) {
                PSL.msg(p, PSL.COOLDOWN.msg().replace("%time%", time));
                return false;
            }
        }

        // non-admin checks
        if (!p.hasPermission("protectionstones.admin")) {
            // check if player has limit on protection stones
            String msg = hasPlayerPassedRegionLimit(p, blockOptions);
            if (!msg.equals("")) {
                PSL.msg(p, msg);
                return false;
            }
            // check if in world blacklist or not in world whitelist
            boolean containsWorld = blockOptions.worlds.contains(p.getLocation().getWorld().getName());

            if ((containsWorld && blockOptions.worldListType.equalsIgnoreCase("blacklist")) || (!containsWorld && blockOptions.worldListType.equalsIgnoreCase("whitelist"))) {
                if (blockOptions.preventBlockPlaceInRestrictedWorld) {
                    PSL.msg(p, PSL.WORLD_DENIED_CREATE.msg());
                    return false;
                } else {
                    return true;
                }
            }

        } // end of non-admin checks

        return createActualRegion(p, l, blockOptions);
    }

    // create the actual WG region for PS region
    static boolean createActualRegion(Player p, Location l, PSProtectBlock blockOptions) {
        // create region
        double bx = l.getX(), bxo = blockOptions.xOffset;
        double by = l.getY(), bxy = blockOptions.yOffset;
        double bz = l.getZ(), bxz = blockOptions.zOffset;

        RegionManager rm = WGUtils.getRegionManagerWithPlayer(p);
        LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(p);

        String id = WGUtils.createPSID(bx, by, bz);

        // check for minimum distance between claims by using fake region
        if (blockOptions.distanceBetweenClaims != -1) {
            if (!isFarEnoughFromOtherClaims(blockOptions, rm, lp, bx + bxo, by + bxy, bz + bxz)) {
                PSL.msg(p, PSL.REGION_TOO_CLOSE.msg().replace("%num%", "" + blockOptions.distanceBetweenClaims));
                return false;
            }
        }

        // create actual region
        BlockVector3 min = WGUtils.getMinVector(bx + bxo, by + bxy, bz + bxz, blockOptions.xRadius, blockOptions.yRadius, blockOptions.zRadius);
        BlockVector3 max = WGUtils.getMaxVector(bx + bxo, by + bxy, bz + bxz, blockOptions.xRadius, blockOptions.yRadius, blockOptions.zRadius);

        ProtectedRegion region = new ProtectedCuboidRegion(id, min, max);
        region.getOwners().addPlayer(p.getUniqueId());
        region.setPriority(blockOptions.priority);
        rm.addRegion(region);

        // check if new region overlaps more powerful region
        if (!blockOptions.allowOverlapUnownedRegions && WGUtils.overlapsStrongerRegion(rm, region, lp)) {
            rm.removeRegion(id);
            PSL.msg(p, PSL.REGION_OVERLAP.msg());
            return false;
        }

        // add corresponding flags to new region by cloning blockOptions default flags
        HashMap<Flag<?>, Object> flags = new HashMap<>(blockOptions.regionFlags);

        // replace greeting and farewell messages with player name
        FlagHandler.initDefaultFlagPlaceholders(flags, p);

        // set flags
        region.setFlags(flags);
        FlagHandler.initCustomFlagsForPS(region, l, blockOptions);

        // check for player's number of adjacent region groups
        if (ProtectionStones.getInstance().getConfigOptions().regionsMustBeAdjacent) {
            if (MiscUtil.getPermissionNumber(p, "protectionstones.adjacent.", 1) >= 0 && !p.hasPermission("protectionstones.admin")) {
                HashMap<String, ArrayList<String>> adjGroups = WGUtils.getPlayerAdjacentRegionGroups(p, rm);

                if (adjGroups.size() > MiscUtil.getPermissionNumber(p, "protectionstones.adjacent.", 1)) {
                    PSL.msg(p, PSL.REGION_NOT_ADJACENT.msg());
                    rm.removeRegion(id);
                    return false;
                }
            }
        }

        // fire event and check if cancelled
        PSCreateEvent event = new PSCreateEvent(PSRegion.fromWGRegion(p.getWorld(), region), p);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            rm.removeRegion(id);
            return false;
        }

        PSL.msg(p, PSL.PROTECTED.msg());

        // hide block if auto hide is enabled
        if (blockOptions.autoHide) l.getBlock().setType(Material.AIR);
        return true;
    }
}
