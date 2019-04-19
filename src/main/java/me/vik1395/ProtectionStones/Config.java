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

import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.file.FileConfig;
import org.bukkit.Bukkit;

import java.util.List;

public class Config {

    // config options
    // config.toml will be loaded into these fields
    @Path("config_version")
    int configVersion;
    @Path("uuidupdated")
    Boolean uuidupdated;
    @Path("placing_cooldown")
    int placingCooldown;
    @Path("allow_dangerous_commands")
    Boolean allowDangerousCommands;
    @Path("block")
    List<ConfigProtectBlock> blocks;


    public static void initConfig() {
        // clear data (for /ps reload)
        ProtectionStones.protectionStonesOptions.clear();

        if (ProtectionStones.config == null) {
            ProtectionStones.config = FileConfig.of(ProtectionStones.configLocation);
        }
        ProtectionStones.config.load();

        // TODO UPGRADE FROM OLD CONFIG

        // keep in mind that there is /ps reload, so clear arrays before adding config options!

        // load config into configOptions object
        ProtectionStones.configOptions = new ObjectConverter().toObject(ProtectionStones.config, Config::new);

        // add protection stones to options map
        if (ProtectionStones.configOptions.blocks.isEmpty()) {
            Bukkit.getLogger().info("Region block not found! You do not have any protection blocks configured!");
        } else {
            Bukkit.getLogger().info("Protection Stone Blocks:");
            for (ConfigProtectBlock b : ProtectionStones.configOptions.blocks) {
                Bukkit.getLogger().info("- " + b.type);
                FlagHandler.initDefaultFlagsForBlock(b); // process flags for block and set regionFlags field
                ProtectionStones.protectionStonesOptions.put(b.type, b); // init block
            }
        }

    }
}
