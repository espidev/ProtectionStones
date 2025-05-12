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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.PSLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class DatabaseManager {
    private static HikariDataSource ds;
    private static ExecutorService executor;
    private static final String DB_FILE = "database.db";

    // Table names
    private static final String BLOCKS_TABLE = "ps_blocks";
    private static final String REGIONS_TABLE = "ps_regions";
    private static final String CACHED_UUIDS_TABLE = "ps_cached_uuids";

    public static void initialize() {
        executor = Executors.newFixedThreadPool(4);
        setupDatabase();
    }

    public static void shutdown() {
        if (ds != null && !ds.isClosed()) {
            ds.close();
        }
        if (executor != null) {
            executor.shutdown();
        }
    }

    private static void setupDatabase() {
        try {
            File dataFolder = ProtectionStones.getInstance().getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            String dbPath = dataFolder.getAbsolutePath() + File.separator + DB_FILE;
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:sqlite:" + dbPath);
            config.setDriverClassName("org.sqlite.JDBC");
            config.setMaximumPoolSize(10);
            config.setConnectionTimeout(30000);
            config.setLeakDetectionThreshold(60000);
            
            ds = new HikariDataSource(config);
            
            createTables();
            
            ProtectionStones.getInstance().getLogger().info("Database connection established!");
        } catch (Exception e) {
            ProtectionStones.getInstance().getLogger().log(Level.SEVERE, "Failed to initialize database", e);
        }
    }

    private static void createTables() {
        try (Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Table for storing protected blocks
            stmt.execute("CREATE TABLE IF NOT EXISTS " + BLOCKS_TABLE + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "world VARCHAR(64) NOT NULL," +
                    "x INTEGER NOT NULL," +
                    "y INTEGER NOT NULL," +
                    "z INTEGER NOT NULL," +
                    "block_type VARCHAR(64) NOT NULL," +
                    "is_hidden BOOLEAN NOT NULL DEFAULT 0," +
                    "region_id VARCHAR(128) NOT NULL," +
                    "UNIQUE(world, x, y, z)" +
                    ")");
            
            // Table for caching region information
            stmt.execute("CREATE TABLE IF NOT EXISTS " + REGIONS_TABLE + " (" +
                    "id VARCHAR(128) PRIMARY KEY," +
                    "world VARCHAR(64) NOT NULL," +
                    "name VARCHAR(128)," +
                    "owner_uuid VARCHAR(36) NOT NULL," +
                    "last_tax_payment BIGINT DEFAULT 0," +
                    "rent_last_paid BIGINT DEFAULT 0" +
                    ")");
            
            // Table for caching UUIDs
            stmt.execute("CREATE TABLE IF NOT EXISTS " + CACHED_UUIDS_TABLE + " (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "name VARCHAR(36) NOT NULL," +
                    "last_seen BIGINT NOT NULL" +
                    ")");
            
            // Create indexes for performance
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_blocks_region_id ON " + BLOCKS_TABLE + " (region_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_regions_owner ON " + REGIONS_TABLE + " (owner_uuid)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_regions_name ON " + REGIONS_TABLE + " (name)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_uuids_name ON " + CACHED_UUIDS_TABLE + " (name)");
            
        } catch (SQLException e) {
            ProtectionStones.getInstance().getLogger().log(Level.SEVERE, "Failed to create database tables", e);
        }
    }

    // Async database operations
    public static CompletableFuture<Void> saveBlockAsync(String world, int x, int y, int z, String blockType, boolean isHidden, String regionId) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = ds.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT OR REPLACE INTO " + BLOCKS_TABLE + 
                         " (world, x, y, z, block_type, is_hidden, region_id) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                ps.setString(1, world);
                ps.setInt(2, x);
                ps.setInt(3, y);
                ps.setInt(4, z);
                ps.setString(5, blockType);
                ps.setBoolean(6, isHidden);
                ps.setString(7, regionId);
                ps.executeUpdate();
            } catch (SQLException e) {
                ProtectionStones.getInstance().getLogger().log(Level.SEVERE, "Failed to save block data", e);
            }
        }, executor);
    }

    public static CompletableFuture<Void> saveBlockAsync(Location location, String blockType, boolean isHidden, String regionId) {
        return saveBlockAsync(
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                blockType,
                isHidden,
                regionId
        );
    }

    public static CompletableFuture<Void> deleteBlockAsync(String world, int x, int y, int z) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = ds.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "DELETE FROM " + BLOCKS_TABLE + " WHERE world = ? AND x = ? AND y = ? AND z = ?")) {
                ps.setString(1, world);
                ps.setInt(2, x);
                ps.setInt(3, y);
                ps.setInt(4, z);
                ps.executeUpdate();
            } catch (SQLException e) {
                ProtectionStones.getInstance().getLogger().log(Level.SEVERE, "Failed to delete block data", e);
            }
        }, executor);
    }

    public static CompletableFuture<Void> deleteBlockAsync(Location location) {
        return deleteBlockAsync(
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
    }

    public static CompletableFuture<Map<String, Object>> getBlockDataAsync(String world, int x, int y, int z) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = ds.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT * FROM " + BLOCKS_TABLE + " WHERE world = ? AND x = ? AND y = ? AND z = ?")) {
                ps.setString(1, world);
                ps.setInt(2, x);
                ps.setInt(3, y);
                ps.setInt(4, z);
                
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("world", rs.getString("world"));
                        data.put("x", rs.getInt("x"));
                        data.put("y", rs.getInt("y"));
                        data.put("z", rs.getInt("z"));
                        data.put("block_type", rs.getString("block_type"));
                        data.put("is_hidden", rs.getBoolean("is_hidden"));
                        data.put("region_id", rs.getString("region_id"));
                        return data;
                    }
                }
            } catch (SQLException e) {
                ProtectionStones.getInstance().getLogger().log(Level.SEVERE, "Failed to get block data", e);
            }
            return null;
        }, executor);
    }

    public static CompletableFuture<Map<String, Object>> getBlockDataAsync(Location location) {
        return getBlockDataAsync(
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
    }

    public static CompletableFuture<List<Map<String, Object>>> getBlocksByRegionIdAsync(String regionId) {
        return CompletableFuture.supplyAsync(() -> {
            List<Map<String, Object>> blocks = new ArrayList<>();
            try (Connection conn = ds.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT * FROM " + BLOCKS_TABLE + " WHERE region_id = ?")) {
                ps.setString(1, regionId);
                
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("world", rs.getString("world"));
                        data.put("x", rs.getInt("x"));
                        data.put("y", rs.getInt("y"));
                        data.put("z", rs.getInt("z"));
                        data.put("block_type", rs.getString("block_type"));
                        data.put("is_hidden", rs.getBoolean("is_hidden"));
                        data.put("region_id", rs.getString("region_id"));
                        blocks.add(data);
                    }
                }
            } catch (SQLException e) {
                ProtectionStones.getInstance().getLogger().log(Level.SEVERE, "Failed to get blocks by region ID", e);
            }
            return blocks;
        }, executor);
    }

    // Region operations
    public static CompletableFuture<Void> saveRegionAsync(String id, String world, String name, UUID ownerUuid, long lastTaxPayment, long rentLastPaid) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = ds.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT OR REPLACE INTO " + REGIONS_TABLE + 
                         " (id, world, name, owner_uuid, last_tax_payment, rent_last_paid) VALUES (?, ?, ?, ?, ?, ?)")) {
                ps.setString(1, id);
                ps.setString(2, world);
                ps.setString(3, name);
                ps.setString(4, ownerUuid.toString());
                ps.setLong(5, lastTaxPayment);
                ps.setLong(6, rentLastPaid);
                ps.executeUpdate();
            } catch (SQLException e) {
                ProtectionStones.getInstance().getLogger().log(Level.SEVERE, "Failed to save region data", e);
            }
        }, executor);
    }

    public static CompletableFuture<Void> deleteRegionAsync(String id) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = ds.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "DELETE FROM " + REGIONS_TABLE + " WHERE id = ?")) {
                ps.setString(1, id);
                ps.executeUpdate();
            } catch (SQLException e) {
                ProtectionStones.getInstance().getLogger().log(Level.SEVERE, "Failed to delete region data", e);
            }
        }, executor);
    }

    public static CompletableFuture<Map<String, Object>> getRegionDataAsync(String id) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = ds.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT * FROM " + REGIONS_TABLE + " WHERE id = ?")) {
                ps.setString(1, id);
                
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("id", rs.getString("id"));
                        data.put("world", rs.getString("world"));
                        data.put("name", rs.getString("name"));
                        data.put("owner_uuid", UUID.fromString(rs.getString("owner_uuid")));
                        data.put("last_tax_payment", rs.getLong("last_tax_payment"));
                        data.put("rent_last_paid", rs.getLong("rent_last_paid"));
                        return data;
                    }
                }
            } catch (SQLException e) {
                ProtectionStones.getInstance().getLogger().log(Level.SEVERE, "Failed to get region data", e);
            }
            return null;
        }, executor);
    }

    public static CompletableFuture<List<Map<String, Object>>> getRegionsByOwnerAsync(UUID ownerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            List<Map<String, Object>> regions = new ArrayList<>();
            try (Connection conn = ds.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT * FROM " + REGIONS_TABLE + " WHERE owner_uuid = ?")) {
                ps.setString(1, ownerUuid.toString());
                
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("id", rs.getString("id"));
                        data.put("world", rs.getString("world"));
                        data.put("name", rs.getString("name"));
                        data.put("owner_uuid", UUID.fromString(rs.getString("owner_uuid")));
                        data.put("last_tax_payment", rs.getLong("last_tax_payment"));
                        data.put("rent_last_paid", rs.getLong("rent_last_paid"));
                        regions.add(data);
                    }
                }
            } catch (SQLException e) {
                ProtectionStones.getInstance().getLogger().log(Level.SEVERE, "Failed to get regions by owner", e);
            }
            return regions;
        }, executor);
    }

    public static CompletableFuture<List<Map<String, Object>>> getRegionsByNameAsync(String name) {
        return CompletableFuture.supplyAsync(() -> {
            List<Map<String, Object>> regions = new ArrayList<>();
            try (Connection conn = ds.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT * FROM " + REGIONS_TABLE + " WHERE name = ?")) {
                ps.setString(1, name);
                
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("id", rs.getString("id"));
                        data.put("world", rs.getString("world"));
                        data.put("name", rs.getString("name"));
                        data.put("owner_uuid", UUID.fromString(rs.getString("owner_uuid")));
                        data.put("last_tax_payment", rs.getLong("last_tax_payment"));
                        data.put("rent_last_paid", rs.getLong("rent_last_paid"));
                        regions.add(data);
                    }
                }
            } catch (SQLException e) {
                ProtectionStones.getInstance().getLogger().log(Level.SEVERE, "Failed to get regions by name", e);
            }
            return regions;
        }, executor);
    }

    // UUID cache operations
    public static CompletableFuture<Void> saveUuidCacheAsync(UUID uuid, String name) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = ds.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT OR REPLACE INTO " + CACHED_UUIDS_TABLE + 
                         " (uuid, name, last_seen) VALUES (?, ?, ?)")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, name.toLowerCase());
                ps.setLong(3, System.currentTimeMillis());
                ps.executeUpdate();
            } catch (SQLException e) {
                ProtectionStones.getInstance().getLogger().log(Level.SEVERE, "Failed to save UUID cache", e);
            }
        }, executor);
    }

    public static CompletableFuture<String> getNameFromUuidAsync(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = ds.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT name FROM " + CACHED_UUIDS_TABLE + " WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("name");
                    }
                }
            } catch (SQLException e) {
                ProtectionStones.getInstance().getLogger().log(Level.SEVERE, "Failed to get name from UUID", e);
            }
            return null;
        }, executor);
    }

    public static CompletableFuture<UUID> getUuidFromNameAsync(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = ds.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT uuid FROM " + CACHED_UUIDS_TABLE + " WHERE name = ?")) {
                ps.setString(1, name.toLowerCase());
                
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return UUID.fromString(rs.getString("uuid"));
                    }
                }
            } catch (SQLException e) {
                ProtectionStones.getInstance().getLogger().log(Level.SEVERE, "Failed to get UUID from name", e);
            }
            return null;
        }, executor);
    }

    // Utility methods
    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static void executeAsync(Runnable task) {
        executor.execute(task);
    }
} 