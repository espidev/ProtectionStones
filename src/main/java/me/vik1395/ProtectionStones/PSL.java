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
    INVALID_BLOCK("invalid_block", ChatColor.RED + "Invalid protection block."),
    NOT_ENOUGH_MONEY("not_enough_money", ChatColor.RED + "You don't have enough money! The price is %price%."),

    HELP("help", ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "=====" + ChatColor.RESET + " PS Help " + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "=====\n" + ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps help"),

    COMMAND_REQUIRES_PLAYER_NAME("command_requires_player_name", ChatColor.RED + "This command requires a player name."),

    NO_PERMISSION_TOGGLE("no_permission_toggle", ChatColor.RED + "You don't have permission to use the toggle command."),
    NO_PERMISSION_CREATE("no_permission_create", ChatColor.RED + "You don't have permission to place a protection block."),
    NO_PERMISSION_DESTROY("no_permission_destroy", ChatColor.RED + "You don't have permission to destroy a protection block."),
    NO_PERMISSION_MEMBERS("no_permission_members", ChatColor.RED + "You don't have permission to use member commands."),
    NO_PERMISSION_OWNERS("no_permission_owners", ChatColor.RED + "You don't have permission to use owner commands."),
    NO_PERMISSION_ADMIN("no_permission_admin", ChatColor.RED + "You do not have permission to use that command."),
    NO_PERMISSION_COUNT("no_permission_count", ChatColor.RED + "You do not have permission to use that command."),
    NO_PERMISSION_COUNT_OTHERS("no_permission_count_others", ChatColor.RED + "You do not have permission to use that command."),
    NO_PERMISSION_FLAGS("no_permission_flags", ChatColor.RED + "You do not have permission to use flag commands."),
    NO_PERMISSION_PER_FLAG("no_permission_per_flag", ChatColor.RED + "You do not have permission to use that flag."),
    NO_PERMISSION_UNHIDE("no_permission_unhide", ChatColor.RED + "You do not have permission to unhide protection blocks."),
    NO_PERMISSION_HIDE("no_permission_hide", ChatColor.RED + " You do not have permission to hide protection blocks."),
    NO_PERMISSION_INFO("no_permission_info", ChatColor.RED + "You do not have permission to use the region info command."),
    NO_PERMISSION_PRIORITY("no_permission_priority", ChatColor.RED + "You do not have permission to use the priority command."),
    NO_PERMISSION_REGION("no_permission_region", ChatColor.RED + "You do not have permission to use region commands."),
    NO_PERMISSION_TP("no_permission_tp", ChatColor.RED + "You do not have permission to teleport to other players' protection blocks."),
    NO_PERMISSION_HOME("no_permission_home", ChatColor.RED + "You do not have permission to teleport to your protection blocks."),
    NO_PERMISSION_UNCLAIM("no_permission_unclaim", ChatColor.RED + "You do not have permission to use the unclaim command."),
    NO_PERMISSION_VIEW("no_permission_view", ChatColor.RED + "You do not have permission to use the view command."),
    NO_PERMISSION_GIVE("no_permission_give", ChatColor.RED + "You do not have permission to use the give command."),
    NO_PERMISSION_GET("no_permission_get", ChatColor.RED + "You do not have permission to use the get command."),
    NO_PERMISSION_SETHOME("no_permission_sethome", ChatColor.RED + "You do not have permission to use the sethome command."),

    ADDED_TO_REGION("psregion.added_to_region",  ChatColor.AQUA + "%player%" + ChatColor.GRAY + " has been added to this region."),
    REMOVED_FROM_REGION("psregion.removed_from_region", ChatColor.AQUA + "%player%" + ChatColor.GRAY + " has been removed from region."),
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
    REGION_OVERLAP("psregion.region_overlap", ChatColor.RED + "You can not place a protection block here as it overlaps another region."),
    REGION_CANT_TELEPORT("psregion.cant_teleport", ChatColor.RED + "Your teleportation was blocked by a protection region!"),

    // ps toggle
    TOGGLE_HELP("toggle.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps toggle"),
    TOGGLE_HELP_DESC("toggle.help_desc", "Use this command to turn on or off placement of protection blocks."),
    TOGGLE_ON("toggle.toggle_on", ChatColor.AQUA + "Protection block placement turned on."),
    TOGGLE_OFF("toggle.toggle_off", ChatColor.AQUA + "Protection block placement turned off."),

    // ps count
    COUNT_HELP("count.count_help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps count [player (optional)]"),
    COUNT_HELP_DESC("count.count_help_desc", "Count the number of regions you own or another player."),
    PERSONAL_REGION_COUNT("count.personal_region_count", ChatColor.GRAY + "Your region count in this world: " + ChatColor.AQUA + "%num%"),
    OTHER_REGION_COUNT("count.other_region_count", ChatColor.GRAY + "%player%'s region count in this world: " + ChatColor.AQUA + "%num%"),

    // ps flag
    FLAG_HELP("flag.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps flag [flagname] [value|null]"),
    FLAG_HELP_DESC("flag.help_desc", "Use this command to set a flag in your protected region."),
    FLAG_SET("flag.flag_set", ChatColor.AQUA + "%flag%" + ChatColor.GRAY + " flag has been set."),
    FLAG_NOT_SET("flag.flag_not_set", ChatColor.AQUA + "%flag%" + ChatColor.GRAY + " flag has " + ChatColor.RED + "not" + ChatColor.GRAY + " been set."),

    // ps hide/unhide
    VISIBILITY_HIDE_HELP("visibility.hide_help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps hide"),
    VISIBILITY_HIDE_HELP_DESC("visibility.hide_help_desc", "Use this command to hide or unhide your protection block."),
    VISIBILITY_UNHIDE_HELP("visibility.unhide_help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps unhide"),
    VISIBILITY_UNHIDE_HELP_DESC("visibility.unhide_help_desc", "Use this command to hide or unhide your protection block."),
    ALREADY_NOT_HIDDEN("visibility.already_not_hidden", ChatColor.GRAY + "The protection stone doesn't appear hidden..."),
    ALREADY_HIDDEN("visibility.already_hidden", ChatColor.GRAY + "The protection stone appears to already be hidden..."),

    // ps info
    INFO_HELP("info.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps info members|owners|flags"),
    INFO_HELP_DESC("info.help_desc", "Use this command inside a ps region to see more information about it."),
    INFO_HEADER("info.header", ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "=====" + ChatColor.RESET + " PS Info " + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "====="),
    INFO_MEMBERS("info.members", ChatColor.BLUE + "Members: " + ChatColor.GRAY),
    INFO_NO_MEMBERS("info.no_members", ChatColor.RED + "(no members)"),
    INFO_OWNERS("info.owners", ChatColor.BLUE + "Owners: " + ChatColor.GRAY),
    INFO_NO_OWNERS("info.no_owners", ChatColor.RED + "(no owners)"),
    INFO_FLAGS("info.flags", ChatColor.BLUE + "Flags: " + ChatColor.GRAY),
    INFO_REGION("info.region", ChatColor.BLUE + "Region: " + ChatColor.AQUA),
    INFO_PRIORITY("info.priority", ChatColor.BLUE + "Priority: " + ChatColor.AQUA),
    INFO_BOUNDS("info.bounds", ChatColor.BLUE + "Bounds: " + ChatColor.AQUA),

    // ps priority
    PRIORITY_HELP("priority.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps priority [number|null]"),
    PRIORITY_HELP_DESC("priority.help_desc", "Use this command to set your region's priority."),
    PRIORITY_INFO("priority.info", ChatColor.GRAY + "Priority: %priority%"),
    PRIORITY_SET("priority.set", ChatColor.YELLOW + "Priority has been set."),
    PRIORITY_ERROR("priority.error", ChatColor.RED + "Error parsing input, check it again?"),

    // ps region
    REGION_HELP("region.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps region [count|list|remove|disown] [playername]"),
    REGION_HELP_DESC("region.help_desc", "Use this command to find information or edit other players' (or your own) protected regions."),
    REGION_NOT_FOUND_FOR_PLAYER("region.not_found_for_player", ChatColor.GRAY + "No regions found for %player% in this world."),
    REGION_LIST("region.list", ChatColor.GRAY + "%player%'s regions in this world: " + ChatColor.AQUA + "%regions%"),
    REGION_REMOVE("region.remove", ChatColor.YELLOW + "%player%'s regions have been removed in this world."),
    REGION_ERROR_SEARCH("region.error_search", ChatColor.RED + "Error while searching for %player%'s regions. Please make sure you have entered the correct name."),

    // ps tp
    TP_HELP("tp.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps tp [player] [num]"),
    TP_HELP_DESC("tp.help_desc", "Teleports you to one of a given player's regions."),
    NUMBER_ABOVE_ZERO("tp.number_above_zero", ChatColor.RED + "Please enter a number above 0."),
    ONLY_HAS_REGIONS("tp.only_has_regions", ChatColor.RED + "%player% only has %num% protected regions in this world!"),
    TPING("tp.tping", ChatColor.GREEN + "Teleporting..."),
    TP_ERROR_NAME("tp.error_name", ChatColor.RED + "Error in teleporting to protected region! (parsing WG region name error)"),
    TP_ERROR_TP("tp.error_tp", ChatColor.RED + "Error in finding the region to teleport to!"),
    TP_IN_SECONDS("tp.in_seconds", ChatColor.GRAY + "Teleporting in " + ChatColor.AQUA + "%seconds%" + ChatColor.GRAY + " seconds."),
    TP_CANCELLED_MOVED("tp.cancelled_moved", ChatColor.RED + "Teleport cancelled. You moved!"),

    // ps home,
    HOME_HELP("home.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps home [num]"),
    HOME_HELP_DESC("home.help_desc", "Teleports you to one of your protected regions."),
    HOME_ONLY("home.only", ChatColor.RED + "You only have %num% total regions in this world!"),

    // ps unclaim
    UNCLAIM_HELP("unclaim.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps unclaim"),
    UNCLAIM_HELP_DESC("unclaim.help_desc", "Use this command to pickup a placed protection stone and remove the region."),

    // ps view
    VIEW_HELP("view.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps view"),
    VIEW_HELP_DESC("view.help_desc", "Use this command to view the borders of a protected region."),
    VIEW_GENERATING("view.generating", ChatColor.GRAY + "Generating border..."),
    VIEW_GENERATE_DONE("view.generate_done", ChatColor.GREEN + "Done! The border will disappear after 30 seconds!"),
    VIEW_REMOVING("view.removing", ChatColor.AQUA + "Removing border...\n" + ChatColor.GREEN + "If you still see ghost blocks, relog!"),

    // ps admin
    ADMIN_HELP("admin.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps admin { version | settings | hide | unhide |\n" + ChatColor.GRAY + "         cleanup | lastlogon | lastlogons | stats | fixregions}"),
    ADMIN_HELP_DESC("admin.help_desc", "This is an admin command showing different stats and allowing to override other player's regions."),
    ADMIN_CLEANUP_HELP("admin.cleanup_help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps admin cleanup [remove|disown] [days]"),
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
    RELOAD_HELP("reload.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps reload"),
    RELOAD_HELP_DESC("reload.help_desc", "Reload settings from the config."),
    RELOAD_START("reload.start", ChatColor.AQUA + "Reloading config..."),
    RELOAD_COMPLETE("reload.complete", ChatColor.AQUA + "Completed config reload!"),

    // ps add/remove
    ADDREMOVE_HELP("addremove.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps add|remove [playername]"),
    ADDREMOVE_HELP_DESC("addremove.help_desc", "Use this command to add or remove a member of your protected region."),
    ADDREMOVE_OWNER_HELP("addremove.owner_help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps addowner|removeowner [playername]"),
    ADDREMOVE_OWNER_HELP_DESC("addremove.owner_help_desc", "Use this command to add or remove an owner of your protected region."),

    // ps get
    GET_HELP("get.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps get [block]"),
    GET_HELP_DESC("get.help_desc", "Use this command to get or purchase a protection block."),
    GET_GOTTEN("get.gotten", ChatColor.AQUA + "Added protection block to inventory!"),

    // ps give
    GIVE_HELP("give.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps give [block] [player]"),
    GIVE_HELP_DESC("give.help_desc", "Use this command to give a player a protection block."),
    GIVE_GIVEN("give.given", ChatColor.GRAY + "Gave " + ChatColor.AQUA + "%block%" + ChatColor.GRAY + " to " + ChatColor.AQUA + "%player%" + ChatColor.GRAY + "."),

    // ps sethome
    SETHOME_HELP("sethome.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps sethome"),
    SETHOME_HELP_DESC("sethome.help_desc", "Use this command to set the home of a region to where you are right now."),
    SETHOME_SET("sethome.set", ChatColor.GRAY + "The home for %psid% has been set to your location."),

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
