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

package dev.espi.protectionstones.utils.upgrade;

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
                ProtectionStones.config.setComment("economy.tax_enabled", " Set taxes on regions.\n" +
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
            case 12:
                ProtectionStones.config.set("config_version", 13);
                ProtectionStones.config.set("default_protection_block_placement_off", false);
                ProtectionStones.config.setComment("default_protection_block_placement_off", " Whether when players join, by default they have protection block placement toggled off (equivalent to running /ps toggle)");
                ProtectionStones.config.set("default_allow_addowner_for_offline_players_without_lp", false);
                ProtectionStones.config.setComment("default_allow_addowner_for_offline_players_without_lp", " If you do not have LuckPerms, ProtectionStones is unable to determine the limits of offline players (since it depends\n" +
                        " on permissions), and so it requires players to be online. Set this to true if your server does not need limits (and so\n" +
                        " the check is unnecessary).");
                break;
            case 13:
                ProtectionStones.config.set("config_version", 14);
                ProtectionStones.config.set("admin.cleanup_delete_regions_with_members_but_no_owners", true);
                ProtectionStones.config.setComment("admin.cleanup_delete_regions_with_members_but_no_owners", "     Whether /ps admin cleanup remove should delete regions that have members, but don't have owners (after inactive\n" +
                        "     owners are removed).\n" +
                        "     Regions that have no owners or members will be deleted regardless.");
                break;
            case 14:
                ProtectionStones.config.set("config_version", 15);

                // fix incorrect value set
                if (ProtectionStones.config.get("allow_addowner_for_offline_players_without_lp") == null) {
                    Object value = ProtectionStones.config.get("default_allow_addowner_for_offline_players_without_lp");
                    ProtectionStones.config.removeComment("default_allow_addowner_for_offline_players_without_lp");
                    ProtectionStones.config.remove("default_allow_addowner_for_offline_players_without_lp");
                    ProtectionStones.config.set("allow_addowner_for_offline_players_without_lp", value == null ? false : (boolean) value);
                    ProtectionStones.config.setComment("allow_addowner_for_offline_players_without_lp", " If you do not have LuckPerms, ProtectionStones is unable to determine the limits of offline players (since it depends\n" +
                            " on permissions), and so it requires players to be online. Set this to true if your server does not need limits (and so\n" +
                            " the check is unnecessary).");
                }
                break;
            case 15:
                ProtectionStones.config.set("config_version", 16);
                ProtectionStones.config.set("allow_home_teleport_for_members", true);
                ProtectionStones.config.setComment("allow_home_teleport_for_members", " Whether or not members of a region can /ps home to the region.");
                break;
            case 16:
                ProtectionStones.config.set("config_version", 17);
                // Inventory GUI toggles (defaults preserve current behavior)
                ProtectionStones.config.set("gui.enabled", false);
                ProtectionStones.config.setComment("gui.enabled", " Whether to enable inventory-based GUIs for commands. If false, commands use legacy text-based output.");
                ProtectionStones.config.set("gui.commands.home", true);
                ProtectionStones.config.set("gui.commands.flag", true);
                ProtectionStones.config.set("gui.commands.add", true);
                ProtectionStones.config.set("gui.commands.remove", true);
                ProtectionStones.config.set("gui.commands.addowner", true);
                ProtectionStones.config.set("gui.commands.removeowner", true);
                ProtectionStones.config.setComment("gui.commands", " Per-command GUI toggles (only used if gui.enabled = true).\n Supported: home, flag, add, remove, addowner, removeowner");
                // Expand GUI toggles for additional commands
                ProtectionStones.config.set("gui.commands.list", true);
                ProtectionStones.config.set("gui.commands.info", true);
                ProtectionStones.config.set("gui.commands.tp", true);
                ProtectionStones.config.set("gui.commands.unclaim", true);
                ProtectionStones.config.set("gui.commands.priority", true);
                ProtectionStones.config.setComment("gui.commands", " Per-command GUI toggles (only used if gui.enabled = true).\n Supported: home, flag, add, remove, addowner, removeowner, list, info, tp, unclaim, priority");
                // Admin GUI toggle
                ProtectionStones.config.set("gui.commands.admin", true);
                ProtectionStones.config.setComment("gui.commands", " Per-command GUI toggles (only used if gui.enabled = true).\n Supported: home, flag, add, remove, addowner, removeowner, list, info, tp, unclaim, priority, admin");
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
