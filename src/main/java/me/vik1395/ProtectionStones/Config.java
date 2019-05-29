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
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.apache.commons.io.IOUtils;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Config {

    // config options
    // config.toml will be loaded into these fields
    // use autoboxing for primitives to allow for null
    @Path("config_version")
    int configVersion;
    @Path("uuidupdated")
    Boolean uuidupdated;
    @Path("placing_cooldown")
    int placingCooldown;
    @Path("async_load_uuid_cache")
    Boolean asyncLoadUUIDCache;
    @Path("ps_view_cooldown")
    public Integer psViewCooldown;
    //@Path("allow_dangerous_commands")
    //Boolean allowDangerousCommands;
    @Path("base_command")
    String base_command;
    @Path("aliases")
    List<String> aliases;


    public static void initConfig() {

        // check if using config v1 or v2 (config.yml -> config.toml)
        if (new File(ProtectionStones.getPlugin().getDataFolder() + "/config.yml").exists() && !ProtectionStones.configLocation.exists()) {
            upgradeFromV1V2();
        }

        // check if config files exist
        try {
            if (!ProtectionStones.getPlugin().getDataFolder().exists()) {
                ProtectionStones.getPlugin().getDataFolder().mkdir();
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

        // keep in mind that there is /ps reload, so clear arrays before adding config options!
        // clear data (for /ps reload)
        ProtectionStones.protectionStonesOptions.clear();

        // create config object
        if (ProtectionStones.config == null) {
            ProtectionStones.config = CommentedFileConfig.builder(ProtectionStones.configLocation).build();
        }

        // loop upgrades until the config has been updated to the latest version
        do {
            ProtectionStones.config.load(); // load latest settings

            // load config into configOptions object
            ProtectionStones.configOptions = new ObjectConverter().toObject(ProtectionStones.config, Config::new);

            // upgrade config if need be (v3+)
            boolean leaveLoop = doConfigUpgrades();
            if (leaveLoop) break; // leave loop if config version is correct

            // save config if upgrading
            ProtectionStones.config.save();
        } while (true);

        // load protection stones to options map
        if (ProtectionStones.blockDataFolder.listFiles().length == 0) {
            Bukkit.getLogger().info("The blocks folder is empty! You do not have any protection blocks configured!");
        } else {

            // temp file to load in default ps block config
            File tempFile;
            try {
                tempFile = File.createTempFile("psconfigtemp", ".toml");
                try (FileOutputStream out = new FileOutputStream(tempFile)) {
                    IOUtils.copy(Config.class.getResourceAsStream("/block1.toml"), out);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            FileConfig template = FileConfig.of(tempFile);
            template.load();

            // iterate over block files and load into map
            Bukkit.getLogger().info("Protection Stone Blocks:");
            for (File file : ProtectionStones.blockDataFolder.listFiles()) {

                CommentedFileConfig c = CommentedFileConfig.of(file);
                c.load();

                // check to make sure all options are not null
                boolean updated = false;
                for (String str : template.valueMap().keySet()) {
                    if (c.get(str) == null) {
                        c.set(str, template.get(str));
                        updated = true;
                    } else if (c.get(str) instanceof CommentedConfig) {
                        // no DFS for now (since there's only 2 layers of config)
                        CommentedConfig template2 = template.get(str);
                        CommentedConfig c2 = c.get(str);
                        for (String str2 : template2.valueMap().keySet()) {
                            if (c2.get(str2) == null) {
                                c2.set(str2, template2.get(str2));
                                updated = true;
                            }
                        }
                    }
                }
                if (updated) c.save();

                // convert toml data into object
                ConfigProtectBlock b = new ObjectConverter().toObject(c, ConfigProtectBlock::new);

                if (Material.getMaterial(b.type) == null) {
                    Bukkit.getLogger().info("Unrecognized material: " + b.type);
                    Bukkit.getLogger().info("Block will not be added. Please fix this in your config.");
                    continue;
                }

                Bukkit.getLogger().info("- " + b.type + " (" + b.alias + ")");
                FlagHandler.initDefaultFlagsForBlock(b); // process flags for block and set regionFlags field
                ProtectionStones.protectionStonesOptions.put(b.type, b); // add block
            }

            // cleanup temp file
            template.close();
            tempFile.delete();
        }

    }

    // Upgrade the config one version up (ex. 3 -> 4)
    public static boolean doConfigUpgrades() {
        boolean leaveLoop = false;
        switch (ProtectionStones.configOptions.configVersion) {
            case 3:
                ProtectionStones.config.set("config_version", 4);
                ProtectionStones.config.set("base_command", "ps");
                ProtectionStones.config.set("aliases", Arrays.asList("pstone", "protectionstones", "protectionstone"));
                break;
            case 4:
                ProtectionStones.config.set("config_version", 5);
                ProtectionStones.config.set("async_load_uuid_cache", false);
                ProtectionStones.config.set("ps_view_cooldown", 20);
                break;
            case ProtectionStones.CONFIG_VERSION:
                leaveLoop = true;
                break;
            default:
                Bukkit.getLogger().info("Invalid config version! The plugin may not load correctly!");
                leaveLoop = true;
                break;
        }
        return leaveLoop;
    }

    // upgrade from config < v2.0.0
    public static void upgradeFromV1V2() {
        Bukkit.getLogger().info(ChatColor.AQUA + "Upgrading configs from v1.x to v2.0+...");

        try {
            ProtectionStones.blockDataFolder.mkdir();
            Files.copy(Config.class.getResourceAsStream("/config.toml"), Paths.get(ProtectionStones.configLocation.toURI()), StandardCopyOption.REPLACE_EXISTING);

            FileConfig fc = FileConfig.builder(ProtectionStones.configLocation).build();
            fc.load();

            File oldConfig = new File(ProtectionStones.getPlugin().getDataFolder() + "/config.yml");
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(oldConfig);

            fc.set("uuidupdated", (yml.get("UUIDUpdated") != null) && yml.getBoolean("UUIDUpdated"));
            fc.set("placing_cooldown", (yml.getBoolean("cooldown.enable")) ? yml.getInt("cooldown.cooldown") : -1);

            // options from global scope
            List<String> worldsDenied = yml.getStringList("Worlds Denied");
            List<String> flags = yml.getStringList("Flags");
            List<String> allowedFlags = new ArrayList<>(Arrays.asList(yml.getString("Allowed Flags").split(",")));

            // upgrade blocks
            for (String type : yml.getConfigurationSection("Region").getKeys(false)) {
                File file = new File(ProtectionStones.blockDataFolder.getAbsolutePath() + "/" + type + ".toml");
                Files.copy(Config.class.getResourceAsStream("/block1.toml"), Paths.get(file.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
                FileConfig b = FileConfig.builder(file).build();
                b.load();

                b.set("type", type);
                b.set("alias", type);
                b.set("restrict_obtaining", false);
                b.set("world_list_type", "blacklist");
                b.set("worlds", worldsDenied);
                b.set("region.x_radius", yml.getInt("Region." + type + ".X Radius"));
                b.set("region.y_radius", yml.getInt("Region." + type + ".Y Radius"));
                b.set("region.z_radius", yml.getInt("Region." + type + ".Z Radius"));
                b.set("region.flags", flags);
                b.set("region.allowed_flags", allowedFlags);
                b.set("region.priority", yml.getInt("Region." + type + ".Priority"));
                b.set("block_data.display_name", "");
                b.set("block_data.lore", Arrays.asList());
                b.set("behaviour.auto_hide", yml.getBoolean("Region." + type + ".Auto Hide"));
                b.set("behaviour.no_drop", yml.getBoolean("Region." + type + ".No Drop"));
                b.set("behaviour.prevent_piston_push", yml.getBoolean("Region." + type + ".Block Piston"));
                // ignore silk touch option
                b.set("player.prevent_teleport_in", yml.getBoolean("Teleport To PVP.Block Teleport"));

                b.save();
                b.close();
            }

            fc.save();
            fc.close();

            oldConfig.renameTo(new File(ProtectionStones.getPlugin().getDataFolder() + "/config.yml.old"));

        } catch (IOException e) {
            e.printStackTrace();
        }

        Bukkit.getLogger().info(ChatColor.GREEN + "Done!");
        Bukkit.getLogger().info(ChatColor.GREEN + "Please be sure to double check your configs with the new options!");

        Bukkit.getLogger().info(ChatColor.AQUA + "Updating PS Regions to new format...");
        ProtectionStones.upgradeRegions();
        Bukkit.getLogger().info(ChatColor.GREEN + "Done!");
    }
}
