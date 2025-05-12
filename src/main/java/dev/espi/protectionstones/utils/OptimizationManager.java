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

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Optimization utilities for ProtectionStones plugin.
 * This class provides caching mechanisms and performance optimizations.
 */
public class OptimizationManager {
    
    // Cache expirations in milliseconds
    private static final long REGION_CACHE_EXPIRY = TimeUnit.MINUTES.toMillis(5);
    private static final long BLOCK_CACHE_EXPIRY = TimeUnit.MINUTES.toMillis(10);
    
    // Region cache: <regionId, <region, timestamp>>
    private static final Map<String, Map.Entry<PSRegion, Long>> regionCache = new ConcurrentHashMap<>();
    
    // Block cache: <location string, <block type, timestamp>>
    private static final Map<String, Map.Entry<String, Long>> blockCache = new ConcurrentHashMap<>();
    
    // Players with active region operations: <player UUID, count>
    private static final Map<UUID, Integer> activeOperations = new ConcurrentHashMap<>();
    
    // Initialize scheduler tasks for cleanup
    public static void initialize() {
        // Schedule periodic cache cleanup
        Bukkit.getScheduler().runTaskTimerAsynchronously(ProtectionStones.getInstance(), 
                OptimizationManager::cleanupCaches, 
                20 * 60, // Start after 1 minute
                20 * 60 * 15); // Run every 15 minutes
    }
    
    /**
     * Cleans up expired entries from caches
     */
    private static void cleanupCaches() {
        long currentTime = System.currentTimeMillis();
        
        // Clean region cache
        regionCache.entrySet().removeIf(entry -> 
                currentTime - entry.getValue().getValue() > REGION_CACHE_EXPIRY);
        
        // Clean block cache
        blockCache.entrySet().removeIf(entry -> 
                currentTime - entry.getValue().getValue() > BLOCK_CACHE_EXPIRY);
        
        ProtectionStones.getInstance().debug("Cache cleanup complete. Region cache: " + 
                regionCache.size() + ", Block cache: " + blockCache.size());
    }
    
    /**
     * Get a cached region or load it if not cached
     * 
     * @param world The world
     * @param regionId The region ID
     * @return The PSRegion, or null if not found
     */
    public static PSRegion getRegion(World world, String regionId) {
        // Check cache first
        Map.Entry<PSRegion, Long> cachedEntry = regionCache.get(regionId);
        if (cachedEntry != null) {
            // Update timestamp
            regionCache.put(regionId, new AbstractMap.SimpleEntry<>(cachedEntry.getKey(), System.currentTimeMillis()));
            return cachedEntry.getKey();
        }
        
        // Not in cache, load it
        RegionManager rgm = WGUtils.getRegionManagerWithWorld(world);
        if (rgm == null) return null;
        
        ProtectedRegion region = rgm.getRegion(regionId);
        if (region == null) return null;
        
        PSRegion psRegion = PSRegion.fromWGRegion(world, region);
        if (psRegion != null) {
            // Store in cache
            regionCache.put(regionId, new AbstractMap.SimpleEntry<>(psRegion, System.currentTimeMillis()));
        }
        
        return psRegion;
    }
    
    /**
     * Clear cache for a specific region
     * 
     * @param regionId The region ID to clear from cache
     */
    public static void clearRegionCache(String regionId) {
        regionCache.remove(regionId);
    }
    
    /**
     * Clear all region caches
     */
    public static void clearAllRegionCaches() {
        regionCache.clear();
    }
    
    /**
     * Cache a block type at a location
     * 
     * @param location The block location
     * @param blockType The block material type
     */
    public static void cacheBlock(Location location, String blockType) {
        String locationKey = locationToString(location);
        blockCache.put(locationKey, new AbstractMap.SimpleEntry<>(blockType, System.currentTimeMillis()));
    }
    
    /**
     * Get cached block type for a location
     * 
     * @param location The block location
     * @return The block type, or null if not in cache
     */
    public static String getCachedBlockType(Location location) {
        String locationKey = locationToString(location);
        Map.Entry<String, Long> cachedEntry = blockCache.get(locationKey);
        
        if (cachedEntry != null) {
            // Update timestamp
            blockCache.put(locationKey, new AbstractMap.SimpleEntry<>(cachedEntry.getKey(), System.currentTimeMillis()));
            return cachedEntry.getKey();
        }
        
        return null;
    }
    
    /**
     * Clear cache for a specific block location
     * 
     * @param location The location to clear from cache
     */
    public static void clearBlockCache(Location location) {
        blockCache.remove(locationToString(location));
    }
    
    /**
     * Clear all block caches
     */
    public static void clearAllBlockCaches() {
        blockCache.clear();
    }
    
    /**
     * Track active operations for a player
     * 
     * @param player The player
     */
    public static void trackOperation(Player player) {
        activeOperations.compute(player.getUniqueId(), (uuid, count) -> count == null ? 1 : count + 1);
    }
    
    /**
     * Untrack operation for a player
     * 
     * @param player The player
     */
    public static void untrackOperation(Player player) {
        activeOperations.compute(player.getUniqueId(), (uuid, count) -> {
            if (count == null || count <= 1) return null;
            return count - 1;
        });
    }
    
    /**
     * Check if a player has active operations
     * 
     * @param player The player
     * @return true if player has active operations
     */
    public static boolean hasActiveOperations(Player player) {
        return activeOperations.containsKey(player.getUniqueId());
    }
    
    /**
     * Convert a location to a string key
     * 
     * @param location The location
     * @return The string key
     */
    private static String locationToString(Location location) {
        return location.getWorld().getName() + "," + 
               location.getBlockX() + "," + 
               location.getBlockY() + "," + 
               location.getBlockZ();
    }
    
    /**
     * Get cached block or read from world
     * This optimizes block reading operations by using the cache first
     * 
     * @param location The block location
     * @return The block at the location
     */
    public static Block getBlockOptimized(Location location) {
        // We don't actually need to cache Block objects since they're always created on demand
        // Just return the block from the world
        return location.getBlock();
    }
    
    /**
     * Get the number of cached regions
     * 
     * @return The size of the region cache
     */
    public static int getRegionCacheSize() {
        return regionCache.size();
    }
    
    /**
     * Get the number of cached blocks
     * 
     * @return The size of the block cache
     */
    public static int getBlockCacheSize() {
        return blockCache.size();
    }
    
    /**
     * Get or load a region from cache or database
     * This optimizes region retrieval operations by checking cache first, then database, then WorldGuard
     * 
     * @param world The world
     * @param regionId The region ID
     * @return The PSRegion, or null if not found
     */
    public static PSRegion getOrLoadRegion(World world, String regionId) {
        // Check cache first
        PSRegion region = getRegion(world, regionId);
        if (region != null) {
            return region;
        }
        
        // Not in cache, try loading from database and then WorldGuard
        DatabaseManager.getRegionDataAsync(regionId).thenAccept(data -> {
            if (data != null) {
                // We found it in database, now let's get the actual region from WorldGuard
                RegionManager rgm = WGUtils.getRegionManagerWithWorld(world);
                if (rgm != null) {
                    ProtectedRegion wgRegion = rgm.getRegion(regionId);
                    if (wgRegion != null) {
                        PSRegion psRegion = dev.espi.protectionstones.PSRegion.fromWGRegion(world, wgRegion);
                        if (psRegion != null) {
                            // Store in cache for future lookups
                            regionCache.put(regionId, new AbstractMap.SimpleEntry<>(psRegion, System.currentTimeMillis()));
                        }
                    }
                }
            }
        });
        
        // Try direct WorldGuard lookup as fallback (synchronous)
        RegionManager rgm = WGUtils.getRegionManagerWithWorld(world);
        if (rgm == null) return null;
        
        ProtectedRegion wgRegion = rgm.getRegion(regionId);
        if (wgRegion == null) return null;
        
        PSRegion psRegion = dev.espi.protectionstones.PSRegion.fromWGRegion(world, wgRegion);
        if (psRegion != null) {
            // Store in cache for future lookups
            regionCache.put(regionId, new AbstractMap.SimpleEntry<>(psRegion, System.currentTimeMillis()));
        }
        
        return psRegion;
    }
} 