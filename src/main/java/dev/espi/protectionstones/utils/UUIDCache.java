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

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.util.profile.Profile;
import dev.espi.protectionstones.ProtectionStones;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class UUIDCache {
    private static Map<UUID, String> uuidToName = new HashMap<>();
    private static Map<String, UUID> nameToUUID = new HashMap<>();

    // Initialize the cache from database
    public static void initialize() {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT uuid, name FROM ps_cached_uuids");
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                String name = rs.getString("name");
                uuidToName.put(uuid, name);
                nameToUUID.put(name.toLowerCase(), uuid);
            }
            
            ProtectionStones.getInstance().getLogger().info("Loaded " + uuidToName.size() + " UUID cache entries from database");
        } catch (SQLException e) {
            ProtectionStones.getInstance().getLogger().log(Level.SEVERE, "Failed to load UUID cache from database", e);
        }
    }

    // toLowerCase for case insensitive search
    public static UUID getUUIDFromName(String name) {
        if (name == null) return null;
        return nameToUUID.get(name.toLowerCase());
    }

    public static String getNameFromUUID(UUID uuid) {
        if (uuid == null) return null;
        return uuidToName.get(uuid);
    }

    public static boolean containsName(String name) {
        if (name == null) return false;
        return nameToUUID.containsKey(name.toLowerCase());
    }

    public static boolean containsUUID(UUID uuid) {
        if (uuid == null) return false;
        return uuidToName.containsKey(uuid);
    }

    public static void storeUUIDNamePair(UUID uuid, String name) {
        if (uuid == null || name == null) return;
        uuidToName.put(uuid, name);
        nameToUUID.put(name.toLowerCase(), uuid);
        
        // Store in database asynchronously
        DatabaseManager.saveUuidCacheAsync(uuid, name);
    }

    public static void removeUUID(UUID uuid) {
        if (uuid == null) return;
        String name = uuidToName.remove(uuid);
        if (name != null) {
            nameToUUID.remove(name.toLowerCase());
        }
        
        // Remove from database asynchronously
        DatabaseManager.executeAsync(() -> {
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM ps_cached_uuids WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                ProtectionStones.getInstance().getLogger().log(Level.SEVERE, "Failed to remove UUID from database", e);
            }
        });
    }

    public static void removeName(String name) {
        if (name == null) return;
        UUID uuid = nameToUUID.remove(name.toLowerCase());
        if (uuid != null) {
            uuidToName.remove(uuid);
        }
        
        // Remove from database asynchronously
        DatabaseManager.executeAsync(() -> {
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM ps_cached_uuids WHERE name = ?")) {
                ps.setString(1, name.toLowerCase());
                ps.executeUpdate();
            } catch (SQLException e) {
                ProtectionStones.getInstance().getLogger().log(Level.SEVERE, "Failed to remove name from database", e);
            }
        });
    }

    public static void storeWGProfile(UUID uuid, String name) {
        WorldGuard.getInstance().getProfileCache().put(new Profile(uuid, name));
    }
}
