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
import org.bukkit.Material;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    @Path("base_command")
    String baseCommand;


    public static void initConfig() {

        // check if config files exist
        try {
            if (!ProtectionStones.psStoneData.exists()) {
                ProtectionStones.psStoneData.createNewFile();
            }
            if (!ProtectionStones.blockDataFolder.exists()) {
                ProtectionStones.blockDataFolder.mkdir();
                Files.copy(Config.class.getResourceAsStream("/block1.toml"), Paths.get(ProtectionStones.blockDataFolder.getAbsolutePath() + "/block1.toml"), StandardCopyOption.REPLACE_EXISTING);
            }
            if (!ProtectionStones.configLocation.exists()) {
                Files.copy(Config.class.getResourceAsStream("/config.toml"), Paths.get(ProtectionStones.configLocation.toURI()), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            Logger.getLogger(ProtectionStones.class.getName()).log(Level.SEVERE, null, ex);
        }


        // clear data (for /ps reload)
        ProtectionStones.protectionStonesOptions.clear();

        if (ProtectionStones.config == null) {
            ProtectionStones.config = FileConfig.of(ProtectionStones.configLocation);
        }
        ProtectionStones.config.load();

        // TODO UPGRADE FROM OLD CONFIG
        // TODO RUN INITCUSTOMFLAGSFORPS for ps

        // keep in mind that there is /ps reload, so clear arrays before adding config options!

        // load config into configOptions object
        ProtectionStones.configOptions = new ObjectConverter().toObject(ProtectionStones.config, Config::new);

        // add protection stones to options map
        if (ProtectionStones.blockDataFolder.listFiles().length == 0) {
            Bukkit.getLogger().info("The blocks folder is empty! You do not have any protection blocks configured!");
        } else {
            Bukkit.getLogger().info("Protection Stone Blocks:");

            // iterate over block files and load into map
            for (File file : ProtectionStones.blockDataFolder.listFiles()) {
                Bukkit.getLogger().info(file.getAbsolutePath());

                FileConfig c = FileConfig.of(file);
                c.load();

                // convert toml data into object
                ConfigProtectBlock b = new ObjectConverter().toObject(c, ConfigProtectBlock::new);

                if (Material.getMaterial(b.type) == null) {
                    Bukkit.getLogger().info("Unrecognized material: " + b.type);
                    Bukkit.getLogger().info("Block will not be added. Please fix this in your config.");
                    continue;
                }

                Bukkit.getLogger().info("- " + b.type);
                FlagHandler.initDefaultFlagsForBlock(b); // process flags for block and set regionFlags field
                ProtectionStones.protectionStonesOptions.put(b.type, b); // add block
            }
        }

    }
}
