/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
                ProtectionStones.config.set("economy.max_rent_price", -1.0);
                ProtectionStones.config.set("economy.min_rent_price", 1.0);
                ProtectionStones.config.setComment("economy.max_rent_price", " Set limits on the price for renting. Set to -1.0 to disable.");
                ProtectionStones.config.set("economy.max_rent_period", -1);
                ProtectionStones.config.set("economy.min_rent_period", 1);
                ProtectionStones.config.setComment("economy.max_rent_period", " Set limits on the period between rent payments, in seconds (86400 seconds = 1 day). Set to -1 to disable.");
                ProtectionStones.config.set("economy.tax_enabled", false);
                ProtectionStones.config.set("economy.tax_message_on_join", true);
                ProtectionStones.config.setComment("economy.tax_enabled", " Set taxes on regions. (THIS FEATURE IS STILL BEING DEVELOPED, ONLY USE FOR TESTING!)\n" +
                        " Taxes are configured in each individual block config.\n" +
                        " Whether or not to enable the tax command.");

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
