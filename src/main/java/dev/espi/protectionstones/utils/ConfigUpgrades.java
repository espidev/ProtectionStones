/*
 * Copyright 2019 ProtectionStones team and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package dev.espi.protectionstones.utils;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.espi.protectionstones.ProtectionStones;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ConfigUpgrades {
    // Upgrade the config one version up (ex. 3 -> 4)
    public static boolean doConfigUpgrades() {
        boolean leaveLoop = false;
        switch (ProtectionStones.getInstance().getConfigOptions().configVersion) {
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
            case 5:
                ProtectionStones.config.set("config_version", 6);
                ProtectionStones.config.set("allow_duplicate_region_names", false);
                break;
            case 6:
                ProtectionStones.config.set("config_version", 7);
                ProtectionStones.config.set("drop_item_when_inventory_full", true);
                break;
            case 7:
                ProtectionStones.config.set("config_version", 8);
                ProtectionStones.config.set("regions_must_be_adjacent", false);
                break;
            case 8:
                ProtectionStones.config.set("config_version", 9);
                ProtectionStones.config.set("allow_merging_regions", true);
                break;
            case 9:
                ProtectionStones.config.set("config_version", 10);
                ProtectionStones.config.set("allow_merging_holes", true);
                break;
            case 10:
                ProtectionStones.config.set("config_version", 11);
                for (File file : ProtectionStones.blockDataFolder.listFiles()) {
                    CommentedFileConfig c = CommentedFileConfig.builder(file).sync().build();
                    c.load();
                    c.setComment("type", " Define your protection block below\n" +
                            " Use block type from here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html\n" +
                            " --------------------------------------------------------------------------------------------------\n" +
                            " If you want to use player heads, you can use \"PLAYER_HEAD:player_name\" (ex. \"PLAYER_HEAD:Notch\")\n" +
                            " To use custom player heads, you need the base64 value of the head. On minecraft-heads.com, you will find this value in the Other section under \"Value:\".\n" +
                            " To use UUIDs for player heads, go to https://sessionserver.mojang.com/session/minecraft/profile/PUT-UUID-HERE and copy the value from the \"value\" field not including quotes.\n" +
                            " When you have the value, you can set the type to \"PLAYER_HEAD:value\"");

                    try {
                        c.set("region.home_x_offset", ((Integer) c.get("region.home_x_offset")).doubleValue());
                        c.set("region.home_y_offset", ((Integer) c.get("region.home_y_offset")).doubleValue());
                        c.set("region.home_z_offset", ((Integer) c.get("region.home_z_offset")).doubleValue());
                    } catch (Exception e) {}
                    c.save();
                    c.close();
                }
                break;
            case 11:
                ProtectionStones.config.set("config_version", 12);
                ProtectionStones.config.set("economy.max-rent-price", -1.0);
                ProtectionStones.config.set("economy.min-rent-price", -1.0);
                ProtectionStones.config.set("economy.max-rent-price", "Set limits on the price for renting. Set to -1 to disable.");
                ProtectionStones.config.set("economy.max-rent-period", -1);
                ProtectionStones.config.set("economy.min-rent-period", 1);
                ProtectionStones.config.setComment("economy.max-rent-period", "Set limits on the period between rent payments, in seconds (86400 seconds = 1 day). Set to -1 to disable.");
                ProtectionStones.config.set("economy.tax-enabled", false);
                ProtectionStones.config.setComment("economy.tax-enabled", "# Set taxes on regions.\n" +
                        "    # Taxes are configured in each individual block config.\n" +
                        "    # Whether or not to enable the tax command.");

                for (File file : ProtectionStones.blockDataFolder.listFiles()) {
                    CommentedFileConfig c = CommentedFileConfig.builder(file).sync().build();
                    c.load();
                    try {
                        List<String> l = c.get("region.hidden_flags_from_info");
                        l.addAll(Arrays.asList("ps-rent-settings", "ps-tax-payments-due", "ps-tax-last-payment-added", "ps-tax-autopayer"));
                        c.set("region.hidden_flags_from_info", l);
                    } catch (Exception e) {}
                    c.save();
                    c.close();
                }
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
}
