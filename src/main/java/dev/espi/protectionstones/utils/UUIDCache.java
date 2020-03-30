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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UUIDCache {
    private static Map<UUID, String> uuidToName = new HashMap<>();
    private static Map<String, UUID> nameToUUID = new HashMap<>();

    // toLowerCase for case insensitive search

    public static UUID getUUIDFromName(String name) {
        return nameToUUID.get(name.toLowerCase());
    }

    public static String getNameFromUUID(UUID uuid) {
        return uuidToName.get(uuid);
    }

    public static boolean containsName(String name) {
        return nameToUUID.containsKey(name.toLowerCase());
    }

    public static boolean containsUUID(UUID uuid) {
        return uuidToName.containsKey(uuid);
    }

    public static void storeUUIDNamePair(UUID uuid, String name) {
        uuidToName.put(uuid, name);
        nameToUUID.put(name.toLowerCase(), uuid);
    }

    public static void removeUUID(UUID uuid) {
        uuidToName.remove(uuid);
    }

    public static void removeName(String name) {
        nameToUUID.remove(name.toLowerCase());
    }
}
