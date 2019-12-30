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

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.SessionManager;
import com.sk89q.worldguard.session.handler.ExitFlag;
import dev.espi.protectionstones.flags.FarewellFlagHandler;
import dev.espi.protectionstones.flags.GreetingFlagHandler;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class FlagHandler {

    // Custom WorldGuard Flags
    public static final Flag<String> GREET_ACTION = new StringFlag("greeting-action");
    public static final Flag<String> FAREWELL_ACTION = new StringFlag("farewell-action");

    // Custom WorldGuard Flags used by ProtectionStones
    // Added to blocks on BlockPlaceEvent Listener
    public static final Flag<String> PS_HOME = new StringFlag("ps-home");
    public static final Flag<String> PS_BLOCK_MATERIAL = new StringFlag("ps-block-material");
    public static final Flag<String> PS_NAME = new StringFlag("ps-name");
    public static final Flag<Set<String>> PS_MERGED_REGIONS = new SetFlag<>("ps-merged-regions", new StringFlag("ps-merged-region"));
    public static final Flag<Set<String>> PS_MERGED_REGIONS_TYPES = new SetFlag<>("ps-merged-regions-types", new StringFlag("ps-merged-region-type")); // each entry: "[psID] [type]"

    public static final Flag<String> PS_LANDLORD = new StringFlag("ps-landlord");
    public static final Flag<String> PS_TENANT = new StringFlag("ps-tenant");
    public static final Flag<String> PS_RENT_PERIOD = new StringFlag("ps-rent-period");
    public static final Flag<Double> PS_PRICE = new DoubleFlag("ps-price");
    public static final Flag<Double> PS_RENT_LAST_PAID = new DoubleFlag("ps-rent-last-paid");
    public static final Flag<Boolean> PS_FOR_SALE = new BooleanFlag("ps-for-sale");
    public static final Flag<Set<String>> PS_RENT_SETTINGS = new SetFlag<>("ps-rent-settings", new StringFlag("ps-rent-setting")); // TODO

    // called on initial start
    static void registerFlags() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            registry.register(PS_HOME);
            registry.register(PS_BLOCK_MATERIAL);
            registry.register(PS_NAME);
            registry.register(PS_MERGED_REGIONS);
            registry.register(PS_MERGED_REGIONS_TYPES);

            registry.register(PS_LANDLORD);
            registry.register(PS_TENANT);
            registry.register(PS_RENT_PERIOD);
            registry.register(PS_PRICE);
            registry.register(PS_RENT_LAST_PAID);
            registry.register(PS_FOR_SALE);
        } catch (FlagConflictException e) {
            Bukkit.getLogger().severe("Flag conflict found! The plugin will not work properly! Please contact the developers of the plugin.");
            e.printStackTrace();
        }

        // extra custom flag registration
        try {
            registry.register(GREET_ACTION);
            registry.register(FAREWELL_ACTION);
        } catch (FlagConflictException ignored) {
            // ignore if flag conflict
        }
    }

    static void registerHandlers() {
        SessionManager sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();
        sessionManager.registerHandler(GreetingFlagHandler.FACTORY, ExitFlag.FACTORY);
        sessionManager.registerHandler(FarewellFlagHandler.FACTORY, ExitFlag.FACTORY);
    }

    // Add the correct flags for the ps region
    static void initCustomFlagsForPS(ProtectedRegion region, Location l, PSProtectBlock cpb) {
        String home = l.getBlockX() + cpb.homeXOffset + " ";
        home += (l.getBlockY() + cpb.homeYOffset) + " ";
        home += (l.getBlockZ() + cpb.homeZOffset);
        region.setFlag(PS_HOME, home);

        region.setFlag(PS_BLOCK_MATERIAL, cpb.type);
    }

    // Edit flags that require placeholders (variables)
    public static void initDefaultFlagPlaceholders(HashMap<Flag<?>, Object> flags, Player p) {
        List<Flag<?>> replaceFlags = new ArrayList<>();
        replaceFlags.add(WorldGuard.getInstance().getFlagRegistry().get("greeting"));
        replaceFlags.add(WorldGuard.getInstance().getFlagRegistry().get("greeting-title"));
        replaceFlags.add(WorldGuard.getInstance().getFlagRegistry().get("greeting-action"));
        replaceFlags.add(WorldGuard.getInstance().getFlagRegistry().get("farewell"));
        replaceFlags.add(WorldGuard.getInstance().getFlagRegistry().get("farewell-title"));
        replaceFlags.add(WorldGuard.getInstance().getFlagRegistry().get("farewell-action"));
        for (Flag<?> f : replaceFlags) {
            if (flags.get(f) != null) {
                String s = (String) flags.get(f);

                // apply placeholders
                if (ProtectionStones.getInstance().isPlaceholderAPISupportEnabled()) {
                    s = PlaceholderAPI.setPlaceholders(p, s);
                }
                flags.put(f, s.replaceAll("%player%", p.getName()));
            }
        }
    }

    // Initializes user defined default flags for block
    static void initDefaultFlagsForBlock(PSProtectBlock b) {
        b.regionFlags = new HashMap<>();
        for (String flagraw : b.flags) {
            String[] split = flagraw.split(" ");
            String settings = "", group = "";

            if (split[0].equals("-g")) { // if it's a group flag
                group = split[1];
                for (int i = 3; i < split.length; i++) settings += split[i] + " ";
            } else { // not group flag
                for (int i = 1; i < split.length; i++) settings += split[i] + " ";
            }

            settings = settings.trim();

            String f = split[0];
            if (split[0].equals("-g")) f = split[2];
            Flag<?> flag = Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), f);
            try {
                FlagContext fc = FlagContext.create().setInput(settings).build();
                b.regionFlags.put(flag, flag.parseInput(fc));
                if (group.equals("-e")) { // empty flag
                    b.regionFlags.put(flag.getRegionGroupFlag(), "");
                } else if (!group.equals("")) { // normal flag value detection
                    b.regionFlags.put(flag.getRegionGroupFlag(), flag.getRegionGroupFlag().detectValue(group));
                }
            } catch (Exception e) {
                Bukkit.getLogger().info("Error parsing flag: " + split[0] + "\nError: ");
                e.printStackTrace();
            }
        }
    }

}