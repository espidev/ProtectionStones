package dev.espi.ProtectionStones.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UUIDCache {
    public static Map<UUID, String> uuidToName = new HashMap<>();
    public static Map<String, UUID> nameToUUID = new HashMap<>();
}
