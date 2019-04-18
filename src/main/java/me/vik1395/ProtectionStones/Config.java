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

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.file.FileConfig;
import org.bukkit.Bukkit;

import java.util.List;

public class Config {

    @Path("config_version")
    String configVersion;
    @Path("uuidupdated")
    Boolean uuidupdated;
    @Path("placing_cooldown")
    int placingCooldown;
    @Path("allow_dangerous_commands")
    Boolean allowDangerousCommands;
    @Path("block")
    List<ConfigProtectBlock> blocks;

    public static void initConfig() {

        if (ProtectionStones.config == null) {
            ProtectionStones.config = FileConfig.of(ProtectionStones.configLocation);
        }
        ProtectionStones.config.load();

        // not necessary for now
        // TODO UPGRADE FROM OLD CONFIG

        // keep in mind that there is /ps reload, so clear arrays before adding config options!

        ProtectionStones.protectionStonesOptions.clear();

        // add protection stones to options map
        if (ProtectionStones.config.get("block") == null) {
            Bukkit.getLogger().info("Region block not found! You do not have any protection blocks configured!");
        } else {
            Bukkit.getLogger().info("Protection Stone Blocks:");
            for (CommentedConfig block : ProtectionStones.config.getConfigurationSection("Blocks").getKeys(false)) {
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
