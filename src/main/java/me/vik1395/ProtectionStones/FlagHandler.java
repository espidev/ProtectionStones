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

package me.vik1395.ProtectionStones;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class FlagHandler {

    // Custom WorldGuard Flags used by ProtectionStones
    // Added to blocks on BlockPlaceEvent Listener
    public static final Flag<String> PS_HOME = new StringFlag("ps-home");
    public static final Flag<String> PS_BLOCK_MATERIAL = new StringFlag("ps-block-material");

    public static void registerFlags() {
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
    public static void initCustomFlagsForPS(ProtectedRegion region, Block block, ConfigProtectBlock cpb) {

        String home = block.getLocation().getBlockX() + cpb.homeXOffset + "|";
        home += (block.getLocation().getBlockY() + cpb.homeYOffset) + "|";
        home += (block.getLocation().getBlockZ() + cpb.homeZOffset);
        region.setFlag(PS_HOME, home);

        region.setFlag(PS_BLOCK_MATERIAL, cpb.type);
    }

    // Initializes user defined default flags for block
    public static void initDefaultFlagsForBlock(ConfigProtectBlock b) {
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

    // /ps flag logic (utilizing WG internal /region flag logic)
    public void setFlag(String[] args, ProtectedRegion region, Player p) {
        Flag flag;

        if (args[1].equalsIgnoreCase("-g")) {
            flag = Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), args[3]);
        } else {
            flag = Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), args[1]);
        }

        if (args[2].equalsIgnoreCase("default")) {
            region.setFlag(flag, flag.getDefault());
            region.setFlag(flag.getRegionGroupFlag(), null);
            p.sendMessage(PSL.FLAG_SET.msg().replace("%flag%", args[1]));
        } else {
            String settings = "";
            if (args[1].equalsIgnoreCase("-g")) {
                for (int i = 4; i < args.length; i++) settings += args[i] + " ";
            } else {
                for (int i = 2; i < args.length; i++) settings += args[i] + " ";
            }

            FlagContext fc = FlagContext.create().setInput(settings.trim()).build();
            try {
                region.setFlag(flag, flag.parseInput(fc));
                if (args[1].equalsIgnoreCase("-g")) {
                    region.setFlag(flag.getRegionGroupFlag(), flag.getRegionGroupFlag().detectValue(args[2]));
                }
            } catch (InvalidFlagFormat invalidFlagFormat) {
                invalidFlagFormat.printStackTrace();
                p.sendMessage(PSL.FLAG_NOT_SET.msg().replace("%flag%", args[1]));
                return;
            }
            p.sendMessage(PSL.FLAG_SET.msg().replace("%flag%", args[1]));
        }
    }

}