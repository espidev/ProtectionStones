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

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Config {



    public static void initConfig() {
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

        // keep in mind that there is /ps reload, so clear arrays before adding config options!

        ProtectionStones.flags = ProtectionStones.getPlugin().getConfig().getStringList("Flags");
        ProtectionStones.allowedFlags = Arrays.asList((ProtectionStones.getPlugin().getConfig().getString("Allowed Flags").toLowerCase()).split(","));
        ProtectionStones.deniedWorlds = ProtectionStones.getPlugin().getConfig().getStringList("Worlds Denied");
        ProtectionStones.isCooldownEnable = ProtectionStones.getPlugin().getConfig().getBoolean("cooldown.enable");
        ProtectionStones.cooldown = ProtectionStones.getPlugin().getConfig().getInt("cooldown.cooldown") * 1000;

        ProtectionStones.protectBlocks.clear();
        ProtectionStones.protectionStonesOptions.clear();

        Bukkit.getLogger().info("Placing of Protection Stones is disabled in the following worlds (override with protectionstones.admin): ");
        for (String world : ProtectionStones.deniedWorlds) {
            Bukkit.getLogger().info("- " + world);
        }

        // add block types
        for (String material : ProtectionStones.getPlugin().getConfig().getString("Blocks").split(",")) {
            if (Material.getMaterial(material) == null) {
                Bukkit.getLogger().info("Unrecognized block: " + material + ". Please make sure you have updated your block name for 1.13!");
            } else {
                ProtectionStones.protectBlocks.add(material.toUpperCase());
            }
        }

        // add protection stones to options map
        if (ProtectionStones.config.get("Region") == null) {
            Bukkit.getLogger().info("Region block not found! You do not have any protection blocks configured!");
        } else {
            Bukkit.getLogger().info("Protection Stone Blocks:");
            for (String block : ProtectionStones.config.getConfigurationSection("Region").getKeys(false)) {
                Bukkit.getLogger().info("- " + block);
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
}
