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

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public enum PSL {
    // messages.yml

    COOLDOWN("cooldown", ChatColor.GOLD + "Warning: " + ChatColor.GRAY + "Please wait for %time% seconds before placing again!"),
    NO_SUCH_COMMAND("no_such_command", ChatColor.RED + "No such command. please type /ps help for more info"),
    NO_ACCESS("no_access", ChatColor.RED + "You are not allowed to do that here."),
    NO_ROOM_IN_INVENTORY("no_room_in_inventory", ChatColor.RED + "You don't have enough room in your inventory."),

    COMMAND_REQUIRES_PLAYER_NAME("command_requires_player_name", ChatColor.RED + "This command requires a player name."),

    NO_PERMISSION_TOGGLE("no_permission_toggle", ChatColor.RED + "You don't have permission to use the toggle command."),
    NO_PERMISSION_CREATE("no_permission_create", ChatColor.RED + "You don't have permission to place a protection stone."),
    NO_PERMISSION_DESTROY("no_permission_destroy", ChatColor.RED + "You don't have permission to destroy a protection stone."),
    NO_PERMISSION_MEMBERS("no_permission_members", ChatColor.RED + "You don't have permission to use member commands."),
    NO_PERMISSION_OWNERS("no_permission_owners", ChatColor.RED + "You don't have permission to use owner commands."),
    NO_PERMISSION_ADMIN("no_permission_admin", ChatColor.RED + "You do not have permission to use that command."),
    NO_PERMISSION_COUNT("no_permission_count", ChatColor.RED + "You do not have permission to use that command."),
    NO_PERMISSION_COUNT_OTHERS("no_permission_count_others", ChatColor.RED + "You do not have permission to use that command."),
    NO_PERMISSION_FLAGS("no_permission_flags", ChatColor.RED + "You do not have permission to use flag commands."),
    NO_PERMISSION_PER_FLAG("no_permission_per_flag", ChatColor.RED + "You do not have permission to use that flag."),
    NO_PERMISSION_UNHIDE("no_permission_unhide", ChatColor.RED + "You do not have permission to unhide protection stones."),
    NO_PERMISSION_HIDE("no_permission_hide", ChatColor.RED + " You do not have permission to hide protection stones."),
    NO_PERMISSION_INFO("no_permission_info", ChatColor.RED + "You do not have permission to use the region info command."),
    NO_PERMISSION_PRIORITY("no_permission_priority", ChatColor.RED + "You do not have permission to use the priority command."),
    NO_PERMISSION_REGION("no_permission_region", ChatColor.RED + "You do not have permission to use region commands."),
    NO_PERMISSION_TP("no_permission_tp", ChatColor.RED + "You do not have permission to teleport to other players' protection stones."),
    NO_PERMISSION_HOME("no_permission_home", ChatColor.RED + "You do not have permission to teleport to your protection stones."),
    NO_PERMISSION_UNCLAIM("no_permission_unclaim", ChatColor.RED + "You do not have permission to use the unclaim command."),
    NO_PERMISSION_VIEW("no_permission_view", ChatColor.RED + "You do not have permission to use the view command."),

    ADDED_TO_REGION("psregion.added_to_region", ChatColor.YELLOW + "%player% has been added to this region."),
    REMOVED_FROM_REGION("psregion.removed_from_region", ChatColor.YELLOW + "%player% has been removed from region."),
    NOT_IN_REGION("psregion.not_in_region", ChatColor.RED + "You are not in a protection stone region!"),
    PLAYER_NOT_FOUND("psregion.player_not_found", ChatColor.RED + "Player not found."),
    NOT_PS_REGION("psregion.not_ps_region", ChatColor.RED + "Not a protection stones region."),
    REGION_DOES_NOT_EXIST("psregion.region_does_not_exist", ChatColor.RED + "Region does not exist."),
    NO_REGIONS_OWNED("psregion.no_regions_owned", ChatColor.RED + "You don't own any protected regions in this world!"),
    NO_REGION_PERMISSION("psregion.no_region_permission", ChatColor.RED + "You do not have permission to do this in this region."),
    PROTECTED("psregion.protected", ChatColor.AQUA + "This area is now protected."),
    NO_LONGER_PROTECTED("psregion.no_longer_protected", ChatColor.YELLOW + "This area is no longer protected."),
    CANT_PROTECT_THAT("psregion.cant_protect_that", ChatColor.RED + "You can't protect that area."),
    REACHED_REGION_LIMIT("psregion.reached_region_limit", ChatColor.RED + "You can not create any more protected regions."),
    WORLD_DENIED_CREATE("psregion.world_denied_create", ChatColor.RED + "You can not create protections in this world."),
    REGION_OVERLAP("psregion.region_overlap", ChatColor.RED + "You can not place a protection here as it overlaps another region."),

    // ps toggle
    TOGGLE_HELP("toggle.help", ChatColor.YELLOW + "/ps toggle"),
    TOGGLE_ON("toggle.toggle_on", ChatColor.YELLOW + "Protection stones placement turned on."),
    TOGGLE_OFF("toggle.toggle_off", ChatColor.YELLOW + "Protection stones placement turned off."),

    // ps count
    PERSONAL_REGION_COUNT("count.personal_region_count", ChatColor.YELLOW + "Your region count in this world: %num%"),
    OTHER_REGION_COUNT("count.other_region_count", ChatColor.YELLOW + "%player%'s region count in this world: %num%"),
    COUNT_HELP("count.count_help", ChatColor.YELLOW + "/ps count [player (optional)]"),

    // ps flag
    FLAG_HELP("flag.flag_help", ChatColor.YELLOW + "/ps flag [flagname] [value|null]"),
    FLAG_SET("flag.flag_set", ChatColor.YELLOW + "%flag% flag has been set."),
    FLAG_NOT_SET("flag.flag_not_set", ChatColor.YELLOW + "%flag% flag has " + ChatColor.RED + "not" + ChatColor.YELLOW + " been set."),

    // ps hide/unhide
    VISIBILITY_HELP("visibility.help", ChatColor.YELLOW + "/ps hide|unhide"),
    ALREADY_NOT_HIDDEN("visibility.already_not_hidden", ChatColor.YELLOW + "The protection stone doesn't appear hidden..."),
    ALREADY_HIDDEN("visibility.already_hidden", ChatColor.YELLOW + "The protection stone appears to already be hidden..."),

    // ps info
    INFO_HELP("info.info_help", ChatColor.YELLOW + "/ps info members|owners|flags"),
    INFO_HEADER("info.info_header", ChatColor.GRAY + "================ PS Info ================"),
    INFO_MEMBERS("info.info_members", ChatColor.BLUE + "Members:"),
    INFO_NO_MEMBERS("info.info_no_members", ChatColor.RED + "(no members)"),
    INFO_OWNERS("info.info_owners", ChatColor.BLUE + "Owners:"),
    INFO_NO_OWNERS("info.info_no_owners", ChatColor.RED + "(no owners)"),
    INFO_FLAGS("info.info_flags", ChatColor.BLUE + "Flags: " + ChatColor.YELLOW),

    // ps priority
    PRIORITY_HELP("priority.priority_help", ChatColor.YELLOW + "/ps priority [number|null]"),
    PRIORITY_INFO("priority.priority_info", ChatColor.YELLOW + "Priority: %priority%"),
    PRIORITY_SET("priority.priority_set", ChatColor.YELLOW + "Priority has been set."),
    PRIORITY_ERROR("priority.priority_error", ChatColor.RED + "Error parsing input, check it again?"),

    // ps region
    REGION_HELP("region.help", ChatColor.YELLOW + "/ps region [count|list|remove|disown] [playername]"),
    REGION_NOT_FOUND_FOR_PLAYER("region.not_found_for_player", ChatColor.YELLOW + "No regions found for %player% in this world."),
    REGION_LIST("region.list", ChatColor.YELLOW + "%player%'s regions in this world: %regions%"),
    REGION_REMOVE("region.remove", ChatColor.YELLOW + "%player%'s regions have been removed in this world."),
    REGION_ERROR_SEARCH("region.error_search", ChatColor.RED + "Error while searching for %player%'s regions. Please make sure you have entered the correct name."),

    // ps tp
    TP_HELP("tp.help", ChatColor.YELLOW + "/ps tp [player] [num]"),
    NUMBER_ABOVE_ZERO("tp.number_above_zero", ChatColor.RED + "Please enter a number above 0."),
    ONLY_HAS_REGIONS("tp.only_has_regions", ChatColor.RED + "%player% only has %num% protected regions in this world!"),
    TPING("tp.tping", ChatColor.GREEN + "Teleporting..."),
    TP_ERROR_NAME("tp.error_name", ChatColor.RED + "Error in teleporting to protected region! (parsing WG region name error)"),
    TP_ERROR_TP("tp.error_tp", ChatColor.RED + "Error in finding the region to teleport to!"),

    // ps home,
    HOME_HELP("home.help", ChatColor.YELLOW + "/ps home [num]\n" + ChatColor.GREEN + "To see your ps count, type /ps count. Use any number within the range to teleport to that ps"),
    HOME_ONLY("home.only", ChatColor.RED + "You only have %num% total regions in this world!"),

    // ps unclaim
    UNCLAIM_HELP("unclaim.help", ChatColor.YELLOW + "/ps unclaim"),
    UNCLAIM_CANT_FIND("unclaim.cant_find", ChatColor.RED + "We can't seem to find the protection stone! Please ask an admin to remove the region manually."),

    // ps view
    VIEW_HELP("view.help", ChatColor.YELLOW + "/ps view"),
    VIEW_GENERATING("view.generating", ChatColor.YELLOW + "Generating border..."),
    VIEW_GENERATE_DONE("view.generate_done", ChatColor.GREEN + "Done! The border will disappear after 30 seconds!"),
    VIEW_REMOVING("view.removing", ChatColor.YELLOW + "Removing border...\n" + ChatColor.GREEN + "If you still see ghost blocks, relog!"),

    // ps admin
    ADMIN_HELP("admin.help", ChatColor.YELLOW + "/ps admin { version | settings | hide | unhide |\n" + ChatColor.YELLOW + "cleanup | lastlogon | lastlogons | stats }"),
    ADMIN_CLEANUP_HELP("admin.cleanup_help", ChatColor.YELLOW + "/ps admin cleanup [remove|disown] [days]"),
    ADMIN_CLEANUP_HEADER("admin.cleanup_header", ChatColor.YELLOW + "Cleanup %arg% %days% days\n================"),
    ADMIN_CLEANUP_FOOTER("admin.cleanup_footer", ChatColor.YELLOW + "================\nCompleted %arg% cleanup."),
    ADMIN_HIDE_TOGGLED("admin.hide_toggled", ChatColor.YELLOW + "All protection stones have been %message% in this world."),
    ADMIN_LAST_LOGON("admin.last_logon", ChatColor.YELLOW + "%player% last played %days% days ago."),
    ADMIN_IS_BANNED("admin.is_banned", ChatColor.YELLOW + "%player% is banned."),
    ADMIN_ERROR_PARSING("admin.error_parsing", ChatColor.RED + "Error parsing days, are you sure it is a number?"),
    ADMIN_LASTLOGONS_HEADER("admin.lastlogons_header", ChatColor.YELLOW + "%days% Days Plus:\n================"),
    ADMIN_LASTLOGONS_LINE("admin.lastlogons_line", ChatColor.YELLOW + "%player% %time% days"),
    ADMIN_LASTLOGONS_FOOTER("admin.lastlogons_footer", ChatColor.YELLOW + "================\n%count% Total Players Shown\n%checked% Total Players Checked"),

    // ps reload
    RELOAD_HELP("reload.help", ChatColor.YELLOW + "/ps reload"),
    RELOAD_START("reload.start", ChatColor.AQUA + "Reloading config..."),
    RELOAD_COMPLETE("reload.complete", ChatColor.AQUA + "Completed config reload!"),

    // ps add/remove
    ADDREMOVE_HELP("addremove.help", ChatColor.YELLOW + "/ps add|remove [playername]"),
    ADDREMOVE_OWNER_HELP("addremove.owner_help", ChatColor.YELLOW + "/ps addowner|removeowner [playername]"),

    ;

    private final String key;
    private String msg;

    private static File conf = new File(ProtectionStones.getPlugin().getDataFolder(), "messages.yml");
    private static HashMap<String, String> keyToMsg = new HashMap<>();

    PSL(String path, String start) {
        this.key = path;
        this.msg = start;
    }

    public String msg() {
        String msgG = keyToMsg.get(key);
        return ChatColor.translateAlternateColorCodes('&', (msgG == null) ? msg : msgG);
    }

    public static void loadConfig() {
        keyToMsg.clear();
        if (!conf.exists()) {
            try {
                conf.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        YamlConfiguration yml = YamlConfiguration.loadConfiguration(conf);
        for (PSL psl : PSL.values()) {
            if (yml.getString(psl.key) == null) {
                yml.set(psl.key, psl.msg.replace('§', '&'));
            } else {
                keyToMsg.put(psl.key, yml.getString(psl.key));
            }
        }
        try {
            yml.save(conf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
