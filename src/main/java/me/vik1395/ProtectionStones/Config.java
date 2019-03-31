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

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

class Config {

    static void initConfig() {
        ProtectionStones.config = new YamlConfiguration();
        try {
            ProtectionStones.config.load(ProtectionStones.conf);
        } catch (IOException | InvalidConfigurationException ex) {
            Logger.getLogger(ProtectionStones.class.getName()).log(Level.SEVERE, null, ex);
        }

        // not necessary for now
//        ProtectionStones.getPlugin().getLogger().info("[ProtectionStones] Checking Configuration Version");
//
//        if (ProtectionStones.getPlugin().getConfig().get("ConfVer") == null) {
//            ProtectionStones.getPlugin().getLogger().info("Config is outdated, this WILL generate errors, please refresh it!");
//        } else {
//            if (ProtectionStones.config.getInt("ConfVer") == 1) {
//                ProtectionStones.getPlugin().getLogger().info("Config is correct version, continuing start-up");
//            } else if (ProtectionStones.config.getInt("ConfVer") > 1) {
//                ProtectionStones.getPlugin().getLogger().info("Config version is higher than required version, this might cause trouble");
//            } else {
//                fixInitialHidden(ProtectionStones.config.get("Block"));
//                ProtectionStones.getPlugin().getLogger().info("Config is outdated, this WILL generate errors, please refresh it!");
//            }
//        }

        // add protection stones to options map
        if (ProtectionStones.config.get("Region") == null) {
            ProtectionStones.getPlugin().getLogger().info("Region block not found! You do not have any protection blocks configured!");
        } else {
            for (String block : ProtectionStones.config.getConfigurationSection("Region").getKeys(false)) {
                // code looks cleaner without constructor
                ConfigProtectBlock b = new ConfigProtectBlock();
                b.setRegionX(ProtectionStones.config.getInt("Region." + block + ".X Radius"));
                b.setRegionY(ProtectionStones.config.getInt("Region." + block + ".Y Radius"));
                b.setRegionZ(ProtectionStones.config.getInt("Region." + block + ".Z Radius"));

                b.setAutoHide(ProtectionStones.config.getBoolean("Region." + block + ".Auto Hide"));
                b.setBlockPiston(ProtectionStones.config.getBoolean("Region." + block + ".Block Piston"));
                b.setNoDrop(ProtectionStones.config.getBoolean("Region." + block + ".No Drop"));
                b.setSilkTouch(ProtectionStones.config.getBoolean("Region." + block + ".Silk Touch"));
                b.setDefaultPriority(ProtectionStones.config.getInt("Region." + block + ".Priority"));

                ProtectionStones.protectionStonesOptions.put(block, b);
            }
        }

    }

    private static void fixInitialHidden(Object block) {
        YamlConfiguration hideFile = YamlConfiguration.loadConfiguration(ProtectionStones.psStoneData);
        Bukkit.getLogger().info("Patching initial hiddenpstones.yml");
        for (World world : Bukkit.getWorlds()) {
            RegionManager rgm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
            Map<String, ProtectedRegion> regions = rgm.getRegions();
            for (String selected : regions.keySet()) {
                if (selected.startsWith("ps")) {
                    Material mat = Material.valueOf(block.toString());
                    String sub = null;
                    if (block.toString().contains("-")) {
                        sub = block.toString().split("-")[1];
                    }
                    if (sub != null) {
                        hideFile.set(selected, mat.toString() + "-" + sub);
                    } else {
                        hideFile.set(selected, mat.toString() + "-0");
                    }
                }
            }
        }
        try {
            hideFile.save(ProtectionStones.psStoneData);
        } catch (IOException ex) {
            Logger.getLogger(ProtectionStones.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
