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

package dev.espi.ProtectionStones;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FlagHandler {

    // Custom WorldGuard Flags used by ProtectionStones
    // Added to blocks on BlockPlaceEvent Listener
    public static final Flag<String> PS_HOME = new StringFlag("ps-home");
    public static final Flag<String> PS_BLOCK_MATERIAL = new StringFlag("ps-block-material");
    public static final Flag<String> PS_NAME = new StringFlag("ps-name");

    // called on initial start
    static void registerFlags() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            registry.register(PS_HOME);
            registry.register(PS_BLOCK_MATERIAL);
        } catch (FlagConflictException e ) {
            Bukkit.getLogger().severe("Flag conflict found! The plugin will not work properly! Please contact the developers of the plugin.");
            e.printStackTrace();
        }
    }

    // Add the correct flags for the ps region
    static void initCustomFlagsForPS(ProtectedRegion region, Block block, PSProtectBlock cpb) {

        String home = block.getLocation().getBlockX() + cpb.homeXOffset + " ";
        home += (block.getLocation().getBlockY() + cpb.homeYOffset) + " ";
        home += (block.getLocation().getBlockZ() + cpb.homeZOffset);
        region.setFlag(PS_HOME, home);

        region.setFlag(PS_BLOCK_MATERIAL, cpb.type);
    }

    static void initDefaultFlagPlaceholders(HashMap<Flag<?>, Object> flags, Player p) {
        List<Flag<?>> replaceFlags = new ArrayList<>();
        replaceFlags.add(WorldGuard.getInstance().getFlagRegistry().get("greeting"));
        replaceFlags.add(WorldGuard.getInstance().getFlagRegistry().get("greeting-title"));
        replaceFlags.add(WorldGuard.getInstance().getFlagRegistry().get("farewell"));
        replaceFlags.add(WorldGuard.getInstance().getFlagRegistry().get("farewell-title"));
        for (Flag<?> f : replaceFlags) {
            if (flags.get(f) != null) {
                flags.put(f, ((String) flags.get(f)).replaceAll("%player%", p.getName()));
            }
        }
    }

    // Initializes user defined default flags for block
    static void initDefaultFlagsForBlock(PSProtectBlock b) {
        b.regionFlags = new HashMap<>();
        for (String flagraw : b.flags) {
            String[] split = flagraw.split(" ");
            String settings = "";
            for (int i = 1; i < split.length; i++) settings += split[i] + " ";
            settings = settings.trim();

            Flag<?> flag = Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), split[0]);
            try {
                FlagContext fc = FlagContext.create().setInput(settings).build();
                b.regionFlags.put(flag, flag.parseInput(fc));
            } catch (Exception e) {
                Bukkit.getLogger().info("Error parsing flag: " + split[0] + "\nError: ");
                e.printStackTrace();
            }
        }
    }

}