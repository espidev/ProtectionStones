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

package dev.espi.protectionstones;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum PSL {
    // messages.yml

    COOLDOWN("cooldown", ChatColor.GOLD + "Warning: " + ChatColor.GRAY + "Please wait for %time% seconds before placing again!"),
    NO_SUCH_COMMAND("no_such_command", ChatColor.RED + "No such command. please type /ps help for more info"),
    NO_ACCESS("no_access", ChatColor.RED + "You are not allowed to do that here."),
    NO_ROOM_IN_INVENTORY("no_room_in_inventory", ChatColor.RED + "You don't have enough room in your inventory."),
    NO_ROOM_DROPPING_ON_FLOOR("no_room_dropping_on_floor", ChatColor.RED + "You don't have enough room in your inventory. Dropping item on floor."),
    INVALID_BLOCK("invalid_block", ChatColor.RED + "Invalid protection block."),
    NOT_ENOUGH_MONEY("not_enough_money", ChatColor.RED + "You don't have enough money! The price is %price%."),
    PAID_MONEY("paid_money", ChatColor.AQUA + "You've paid $%price%."),
    INVALID_WORLD("invalid_world", ChatColor.RED + "Invalid world."),
    MUST_BE_PLAYER("must_be_player", ChatColor.RED + "You must be a player to execute this command."),
    GO_BACK_PAGE("go_back_page", "Go back a page."),
    GO_NEXT_PAGE("go_next_page", "Go to next page."),

    HELP("help", ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "=====" + ChatColor.RESET + " PS Help " + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "=====\n" + ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps help"),
    HELP_NEXT("help_next", ChatColor.GRAY + "Do /ps help %page% to go to the next page!"),

    COMMAND_REQUIRES_PLAYER_NAME("command_requires_player_name", ChatColor.RED + "This command requires a player name."),

    NO_PERMISSION_TOGGLE("no_permission_toggle", ChatColor.RED + "You don't have permission to use the toggle command."),
    NO_PERMISSION_CREATE("no_permission_create", ChatColor.RED + "You don't have permission to place a protection block."),
    NO_PERMISSION_CREATE_SPECIFIC("no_permission_create_specific", ChatColor.RED + "You don't have permission to place this protection block type."),
    NO_PERMISSION_DESTROY("no_permission_destroy", ChatColor.RED + "You don't have permission to destroy a protection block."),
    NO_PERMISSION_MEMBERS("no_permission_members", "&cYou don't have permission to use member commands."),
    NO_PERMISSION_OWNERS("no_permission_owners", "&cYou don't have permission to use owner commands."),
    NO_PERMISSION_ADMIN("no_permission_admin", ChatColor.RED + "You do not have permission to use that command."),
    NO_PERMISSION_COUNT("no_permission_count", ChatColor.RED + "You do not have permission to use that command."),
    NO_PERMISSION_COUNT_OTHERS("no_permission_count_others", ChatColor.RED + "You do not have permission to use that command."),
    NO_PERMISSION_FLAGS("no_permission_flags", "&cYou do not have permission to use flag commands."),
    NO_PERMISSION_PER_FLAG("no_permission_per_flag", ChatColor.RED + "You do not have permission to use that flag."),
    NO_PERMISSION_RENT("no_permission_rent", ChatColor.RED + "You do not have permission for renting."),
    NO_PERMISSION_TAX("no_permission_tax", ChatColor.RED + "You do not have permission to use the tax command."),
    NO_PERMISSION_BUYSELL("no_permission_buysell", ChatColor.RED + "You do not have permission to buy and sell regions."),
    NO_PERMISSION_UNHIDE("no_permission_unhide", ChatColor.RED + "You do not have permission to unhide protection blocks."),
    NO_PERMISSION_HIDE("no_permission_hide", ChatColor.RED + "You do not have permission to hide protection blocks."),
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
    NO_PERMISSION_LIST("no_permission_list", ChatColor.RED + "You do not have permission to use the list command."),
    NO_PERMISSION_LIST_OTHERS("no_permission_list_others", ChatColor.RED + "You do not have permission to use the list command for others."),
    NO_PERMISSION_NAME("no_permission_name", ChatColor.RED + "You do not have permission to use the name command."),
    NO_PERMISSION_SETPARENT("no_permission_setparent", ChatColor.RED + "You do not have permission to use the setparent command."),
    NO_PERMISSION_SETPARENT_OTHERS("no_permission_setparent_others", ChatColor.RED + "You do not have permission to inherit from regions you don't own."),
    NO_PERMISSION_MERGE("no_permission_merge", ChatColor.RED + "You do not have permission to use /ps merge."),

    ADDED_TO_REGION("psregion.added_to_region", ChatColor.AQUA + "%player%" + ChatColor.GRAY + " has been added to this region."),
    ADDED_TO_REGION_SPECIFIC("psregion.added_to_region_specific", ChatColor.AQUA + "%player%" + ChatColor.GRAY + " has been added to region %region%."),
    REMOVED_FROM_REGION("psregion.removed_from_region", ChatColor.AQUA + "%player%" + ChatColor.GRAY + " has been removed from region."),
    REMOVED_FROM_REGION_SPECIFIC("psregion.removed_from_region_specific", ChatColor.AQUA + "%player%" + ChatColor.GRAY + " has been removed from region %region%."),
    NOT_IN_REGION("psregion.not_in_region", ChatColor.RED + "You are not in a protection stones region!"),
    PLAYER_NOT_FOUND("psregion.player_not_found", ChatColor.RED + "Player not found."),
    NOT_PS_REGION("psregion.not_ps_region", ChatColor.RED + "Not a protection stones region."),
    REGION_DOES_NOT_EXIST("psregion.region_does_not_exist", ChatColor.RED + "Region does not exist."),
    NO_REGIONS_OWNED("psregion.no_regions_owned", ChatColor.RED + "You don't own any protected regions in this world!"),
    NO_REGION_PERMISSION("psregion.no_region_permission", ChatColor.RED + "You do not have permission to do this in this region."),
    PROTECTED("psregion.protected", ChatColor.AQUA + "This area is now protected."),
    NO_LONGER_PROTECTED("psregion.no_longer_protected", ChatColor.YELLOW + "This area is no longer protected."),
    CANT_PROTECT_THAT("psregion.cant_protect_that", ChatColor.RED + "You can't protect that area."),
    REACHED_REGION_LIMIT("psregion.reached_region_limit", ChatColor.RED + "You can not have any more protected regions (%limit%)."),
    REACHED_PER_BLOCK_REGION_LIMIT("psregion.reached_per_block_region_limit", ChatColor.RED + "You can not have any more regions of this type (%limit%)."),
    WORLD_DENIED_CREATE("psregion.world_denied_create", ChatColor.RED + "You can not create protections in this world."),
    REGION_OVERLAP("psregion.region_overlap", ChatColor.RED + "You can not place a protection block here as it overlaps another region."),
    REGION_TOO_CLOSE("psregion.region_too_close", ChatColor.RED + "Your protection block must be a minimum of %num% blocks from the edge of other regions!"),
    REGION_CANT_TELEPORT("psregion.cant_teleport", ChatColor.RED + "Your teleportation was blocked by a protection region!"),
    SPECIFY_ID_INSTEAD_OF_ALIAS("psregion.specify_id_instead_of_alias", ChatColor.GRAY + "There were multiple regions found with this name! Please use an ID instead.\n Regions with this name: " + ChatColor.AQUA + "%regions%"),
    REGION_NOT_ADJACENT("psregion.region_not_adjacent", ChatColor.RED + "You've passed the limit of non-adjacent regions! Try putting your protection block closer to other regions you already own."),
    REGION_NOT_OVERLAPPING("psregion.not_overlapping", ChatColor.RED + "These regions don't overlap each other!"),
    MULTI_REGION_DOES_NOT_EXIST("psregion.multi_region_does_not_exist", "One of these regions don't exist!"),
    NO_REGION_HOLES("psregion.no_region_holes", ChatColor.RED + "Unprotected area detected inside region! This is not allowed!"),
    DELETE_REGION_PREVENTED_NO_HOLES("psregion.delete_region_prevented", ChatColor.GRAY + "The region could not be removed, possibly because it creates a hole in the existing region."),
    NOT_OWNER("psregion.not_owner", ChatColor.RED + "You are not an owner of this region!"),
    CANNOT_MERGE_RENTED_REGION("psregion.cannot_merge_rented_region", ChatColor.RED + "Cannot merge regions because region %region% is in the process of being rented out!"),
    NO_PERMISSION_REGION_TYPE("psregion.no_permission_region_type", ChatColor.RED + "You do not have permission to have this region type."),
    REGION_HIDDEN("psregion.hidden", ChatColor.GRAY + "The protection block is now hidden."),
    MUST_BE_PLACED_IN_EXISTING_REGION("psregion.must_be_placed_in_existing_region", ChatColor.RED + "This must be placed inside of an existing region!"),
    REGION_ALREADY_IN_LOCATION_IS_HIDDEN("psregion.already_in_location_is_hidden", ChatColor.RED + "A region already exists in this location (is the protection block hidden?)"),
    CANNOT_REMOVE_YOURSELF_LAST_OWNER("psregion.cannot_remove_yourself_last_owner", ChatColor.RED + "You cannot remove yourself as you are the last owner."),
    CANNOT_REMOVE_YOURSELF_FROM_ALL_REGIONS("psregion.cannot_remove_yourself_all_regions", ChatColor.RED + "You cannot remove yourself from all of your regions at once, for safety reasons."),

    // ps toggle
    TOGGLE_HELP("toggle.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps toggle|on|off"),
    TOGGLE_HELP_DESC("toggle.help_desc", "Use this command to turn on or off placement of protection blocks."),
    TOGGLE_ON("toggle.toggle_on", ChatColor.AQUA + "Protection block placement turned on."),
    TOGGLE_OFF("toggle.toggle_off", ChatColor.AQUA + "Protection block placement turned off."),

    // ps count
    COUNT_HELP("count.count_help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps count [player (optional)]"),
    COUNT_HELP_DESC("count.count_help_desc", "Count the number of regions you own or another player."),
    PERSONAL_REGION_COUNT("count.personal_region_count", ChatColor.GRAY + "Your region count in this world: " + ChatColor.AQUA + "%num%"),
    PERSONAL_REGION_COUNT_MERGED("count.personal_region_count_merged", ChatColor.GRAY + "- Including each merged region: " + ChatColor.AQUA + "%num%"),
    OTHER_REGION_COUNT("count.other_region_count", ChatColor.GRAY + "%player%'s region count in this world: " + ChatColor.AQUA + "%num%"),
    OTHER_REGION_COUNT_MERGED("count.other_region_count_merged", ChatColor.GRAY + "- Including each merged region: " + ChatColor.AQUA + "%num%"),

    // ps flag
    FLAG_HELP("flag.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps flag [flagname] [value|null|default]"),
    FLAG_HELP_DESC("flag.help_desc", "Use this command to set a flag in your protected region."),
    FLAG_SET("flag.flag_set", ChatColor.AQUA + "%flag%" + ChatColor.GRAY + " flag has been set."),
    FLAG_NOT_SET("flag.flag_not_set", ChatColor.AQUA + "%flag%" + ChatColor.GRAY + " flag has " + ChatColor.RED + "not" + ChatColor.GRAY + " been set. Check your values again."),
    FLAG_PREVENT_EXPLOIT("flag.flag_prevent_exploit", ChatColor.RED + "This has been disabled to prevent exploits."),
    FLAG_PREVENT_EXPLOIT_HOVER("flag.flag_prevent_exploit_hover", ChatColor.RED + "Disabled for security reasons."),
    FLAG_GUI_HEADER("flag.gui_header", ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "=====" + ChatColor.RESET + " Flags (click to change) " + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "====="),
    FLAG_GUI_HOVER_SET("flag.gui_hover_set", ChatColor.AQUA + "Click to set."),
    FLAG_GUI_HOVER_SET_TEXT("flag.gui_hover_set_text", ChatColor.AQUA + "Click to change." + ChatColor.WHITE + "\nCurrent value:\n%value%"),
    FLAG_GUI_HOVER_CHANGE_GROUP("flag.hover_change_group", "Click to set this flag to apply to only %group%."),
    FLAG_GUI_HOVER_CHANGE_GROUP_NULL("flag.hover_change_group_null", ChatColor.RED + "You must set this flag to a value before changing the group."),

    // ps rent
    RENT_HELP("rent.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps rent"),
    RENT_HELP_DESC("rent.help_desc", "Use this command to manage rents (buying and selling)."),
    RENT_HELP_HEADER("rent.help_header", ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "=====" + ChatColor.RESET + " Rent Help " + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "====="),
    RENT_ALREADY_RENTING("rent.already_renting", ChatColor.RED + "The region is already being rented out! You must stop leasing the region first."),
    RENT_NOT_RENTED("rent.not_rented", ChatColor.RED + "This region is not being rented."),
    RENT_LEASE_SUCCESS("rent.lease_success", ChatColor.AQUA + "Region leasing terms set:\n" + ChatColor.AQUA + "Price: " + ChatColor.GRAY + "%price%\n" + ChatColor.AQUA + "Payment Term: " + ChatColor.GRAY + "%period%"),
    RENT_STOPPED("rent.stopped", ChatColor.AQUA + "Leasing stopped."),
    RENT_EVICTED("rent.evicted", ChatColor.GRAY + "Evicted tenant %tenant%."),
    RENT_NOT_RENTING("rent.not_renting", ChatColor.RED + "This region is not being rented out to tenants."),
    RENT_PAID_LANDLORD("rent.paid_landlord", ChatColor.AQUA + "%tenant%" + ChatColor.GRAY + " has paid " + ChatColor.AQUA + "$%price%" + ChatColor.GRAY + " for renting out " + ChatColor.AQUA + "%region%" + ChatColor.GRAY + "."),
    RENT_PAID_TENANT("rent.paid_tenant", ChatColor.GRAY + "Paid " + ChatColor.AQUA + "$%price%" + ChatColor.GRAY + " to " + ChatColor.AQUA + "%landlord%" + ChatColor.GRAY + " for region " + ChatColor.AQUA + "%region%" + ChatColor.GRAY + "."),
    RENT_RENTING_LANDLORD("rent.renting_landlord", ChatColor.AQUA + "%player%" + ChatColor.GRAY + " is now renting out region " + ChatColor.AQUA + "%region%" + ChatColor.GRAY + "."),
    RENT_RENTING_TENANT("rent.renting_tenant", ChatColor.GRAY + "You are now renting out region " + ChatColor.AQUA + "%region%" + ChatColor.GRAY + " for " + ChatColor.AQUA + "%price%" + ChatColor.GRAY + " per " + ChatColor.AQUA + "%period%" + ChatColor.GRAY + "."),
    RENT_NOT_TENANT("rent.not_tenant", ChatColor.RED + "You are not the tenant of this region!"),
    RENT_TENANT_STOPPED_LANDLORD("rent.tenant_stopped_landlord", ChatColor.AQUA + "%player%" + ChatColor.GRAY + " has stopped renting out region " + ChatColor.AQUA + "%region%" + ChatColor.GRAY + ". It is now available for others to rent."),
    RENT_TENANT_STOPPED_TENANT("rent.tenant_stopped_tenant", ChatColor.AQUA + "You have stopped renting out region %region%."),
    RENT_BEING_SOLD("rent.being_sold", ChatColor.RED + "The region is being sold! Do /ps sell stop first."),
    RENT_EVICT_NO_MONEY_TENANT("rent.evict_no_money_tenant", ChatColor.GRAY + "You have been " + ChatColor.RED + "evicted" + ChatColor.GRAY + " from region " + ChatColor.AQUA + "%region%" + ChatColor.GRAY + " because you do not have enough money (%price%) to pay for rent."),
    RENT_EVICT_NO_MONEY_LANDLORD("rent.evict_no_money_landlord", ChatColor.AQUA + "%tenant%" + ChatColor.GRAY + " has been " + ChatColor.RED + "evicted" + ChatColor.GRAY + " from region " + ChatColor.AQUA + "%region%" + ChatColor.GRAY + " because they are unable to afford rent."),
    RENT_CANNOT_RENT_OWN_REGION("rent.cannot_rent_own_region", ChatColor.RED + "You cannot rent your own region!"),
    RENT_REACHED_LIMIT("rent.reached_limit", ChatColor.RED + "You've reached the limit of regions you are allowed to rent!"),
    RENT_PRICE_TOO_LOW("rent.price_too_low", ChatColor.RED + "The rent price is too low (must be larger than %price%)."),
    RENT_PRICE_TOO_HIGH("rent.price_too_high", ChatColor.RED + "The rent price is too high (must be lower than %price%)."),
    RENT_PERIOD_TOO_SHORT("rent.period_too_short", ChatColor.RED + "The rent period is too short (must be longer than %period% seconds)."),
    RENT_PERIOD_TOO_LONG("rent.period_too_long", ChatColor.RED + "The rent period is too long (must be shorter than %period% seconds)."),
    RENT_PERIOD_INVALID("rent.period_invalid", ChatColor.RED + "Invalid period format! Example: 24h for once a day."),
    RENT_CANNOT_BREAK_WHILE_RENTING("rent.cannot_break_while_renting", ChatColor.RED + "You cannot break the region when it is being rented out."),

    // ps tax
    TAX_HELP("tax.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps tax"),
    TAX_HELP_DESC("tax.help_desc", "Use this command to manage and pay taxes."),
    TAX_HELP_HEADER("tax.help_header", ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "=====" + ChatColor.RESET + " Taxes Help " + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "====="),
    TAX_DISABLED_REGION("tax.disabled_region", ChatColor.RED + "Taxes are disabled for this region."),
    TAX_SET_AS_AUTOPAYER("tax.set_as_autopayer", ChatColor.GRAY + "Taxes for region " + ChatColor.AQUA + "%region%" + ChatColor.GRAY + " will now be automatically paid by you."),
    TAX_SET_NO_AUTOPAYER("tax.set_no_autopayer", ChatColor.GRAY + "Taxes for region " + ChatColor.AQUA + "%region%" + ChatColor.GRAY + " now have to be manually paid for."),
    TAX_PAID("tax.paid", ChatColor.GRAY + "Paid " + ChatColor.AQUA + "$%amount%" + ChatColor.GRAY + " in taxes for region " + ChatColor.AQUA + "%region%" + ChatColor.GRAY + "."),
    TAX_INFO_HEADER("tax.info_header", ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "=====" + ChatColor.RESET + " Tax Info (click for more info) " + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "====="),
    TAX_JOIN_MSG_PENDING_PAYMENTS("tax.join_msg_pending_payments", ChatColor.GRAY + "You have " + ChatColor.AQUA + "$%money%" + ChatColor.GRAY + " in tax payments due on your regions!\nView them with /ps tax info."),
    TAX_PLAYER_REGION_INFO("tax.player_region_info", ChatColor.GRAY + "> " + ChatColor.AQUA + "%region%" + ChatColor.GRAY + " - " + ChatColor.DARK_AQUA + "$%money% due"),
    TAX_PLAYER_REGION_INFO_AUTOPAYER("tax.player_region_info_autopayer", ChatColor.GRAY + "> " + ChatColor.AQUA + "%region%" + ChatColor.GRAY + " - " + ChatColor.DARK_AQUA + "$%money% due" + ChatColor.GRAY + " (you autopay)"),
    TAX_CLICK_TO_SHOW_MORE_INFO("tax.click_to_show_more_info", "Click to show more information."),
    TAX_REGION_INFO_HEADER("tax.region_info_header", ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "=====" + ChatColor.RESET + " %region% Tax Info " + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "====="),
    TAX_REGION_INFO("tax.region_info", ChatColor.BLUE + "Tax Rate: " + ChatColor.GRAY + "$%taxrate% (sum of all merged regions)" + "\n"
            + ChatColor.BLUE + "Time between tax cycles: " + ChatColor.GRAY + "%taxperiod%" + "\n"
            + ChatColor.BLUE + "Time to pay taxes after cycle: " + ChatColor.GRAY + "%taxpaymentperiod%" + "\n"
            + ChatColor.BLUE + "Tax Autopayer: " + ChatColor.GRAY + "%taxautopayer%" + "\n"
            + ChatColor.BLUE + "Taxes Owed: " + ChatColor.GRAY + "$%taxowed%"),
    TAX_NEXT("tax.next_page", ChatColor.GRAY + "Do /ps tax info -p %page% to go to the next page!"),

    // ps buy
    BUY_HELP("buy.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps buy"),
    BUY_HELP_DESC("buy.help_desc", "Buy the region you are currently in."),
    BUY_NOT_FOR_SALE("buy.not_for_sale", ChatColor.RED + "This region is not for sale."),
    BUY_STOP_SELL("buy.stop_sell", ChatColor.GRAY + "The region is now not for sale."),
    BUY_SOLD_BUYER("buy.sold_buyer", ChatColor.GRAY + "Bought region " + ChatColor.AQUA + "%region%" + ChatColor.GRAY + " for " + ChatColor.AQUA + "$%price%" + ChatColor.GRAY + " from " + ChatColor.AQUA + "%player%" + ChatColor.GRAY + "."),
    BUY_SOLD_SELLER("buy.sold_seller", ChatColor.GRAY + "Sold region " + ChatColor.AQUA + "%region%" + ChatColor.GRAY + " for " + ChatColor.AQUA + "$%price%" + ChatColor.GRAY + " to " + ChatColor.AQUA + "%player%" + ChatColor.GRAY + "."),

    // ps sell
    SELL_HELP("sell.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps sell [price|stop]"),
    SELL_HELP_DESC("sell.help_desc", "Sell the region you are currently in."),
    SELL_RENTED_OUT("sell.rented_out", ChatColor.RED + "The region is being rented out! You must stop renting it out to sell."),
    SELL_FOR_SALE("sell.for_sale", ChatColor.GRAY + "The region is now for sale for " + ChatColor.AQUA + "$%price%" + ChatColor.GRAY + "."),

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
    INFO_TYPE2("info.type2", "&9Type: &7%type%", "%type%"),
    INFO_MAY_BE_MERGED("info.may_be_merged", "(may be merged with other types)"),
    INFO_MERGED2("info.merged2", ChatColor.BLUE + "Merged regions: " + ChatColor.GRAY + "%merged%", "%merged%"),
    INFO_MEMBERS2("info.members2", "&9Members: &7%members%", "%members%"),
    INFO_NO_MEMBERS("info.no_members", ChatColor.RED + "(no members)"),
    INFO_OWNERS2("info.owners2", "&9Owners: &7%owners%", "%owners%"),
    INFO_NO_OWNERS("info.no_owners", ChatColor.RED + "(no owners)"),
    INFO_FLAGS2("info.flags2", "&9Flags: &7%flags%", "%flags%"),
    INFO_NO_FLAGS("info.no_flags", "(none)"),
    INFO_REGION2("info.region2", "&9Region: &b%region%", "%region%"),
    INFO_PRIORITY2("info.priority2", "&9Priority: &b%priority%", "%priority%"),
    INFO_PARENT2("info.parent2", "&9Parent: &b%parentregion%", "%parentregion%"),
    INFO_BOUNDS_XYZ("info.bounds_xyz", "&9Bounds: &b(%minx%,%miny%,%minz%) -> (%maxx%,%maxy%,%maxz%)",
            "%minx%", "%miny%", "%minz%", "%maxx%", "%maxy%", "%maxz%"
    ),
    INFO_BOUNDS_XZ("info.bounds_xz", "&9Bounds: &b(%minx%, %minz%) -> (%maxx%, %maxz%)",
            "%minx%", "%minz%", "%maxx%", "%maxz%"
    ),
    INFO_SELLER2("info.seller2", "&9Seller: &7%seller%", "%seller%"),
    INFO_PRICE2("info.price2", "&9Price: &7%price%", "%price%"),
    INFO_TENANT2("info.tenant2", "&9Tenant: &7%tenant%", "%tenant%"),
    INFO_LANDLORD2("info.landlord2", "&9Landlord: &7%landlord%", "%landlord%"),
    INFO_RENT2("info.rent2", "&9Rent: &7%rent%", "%rent%"),
    INFO_AVAILABLE_FOR_SALE("info.available_for_sale", ChatColor.AQUA + "Region available for sale!"),
    INFO_AVAILABLE_FOR_RENT("info.available_for_rent", ChatColor.AQUA + "Region available for rent!"),

    // ps priority
    PRIORITY_HELP("priority.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps priority [number|null]"),
    PRIORITY_HELP_DESC("priority.help_desc", "Use this command to set your region's priority."),
    PRIORITY_INFO("priority.info", ChatColor.GRAY + "Priority: %priority%"),
    PRIORITY_SET("priority.set", ChatColor.YELLOW + "Priority has been set."),
    PRIORITY_ERROR("priority.error", ChatColor.RED + "Error parsing input, check it again?"),

    // ps region
    REGION_HELP("region.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps region [list|remove|disown] [playername]"),
    REGION_HELP_DESC("region.help_desc", "Use this command to find information or edit other players' (or your own) protected regions."),
    REGION_NOT_FOUND_FOR_PLAYER("region.not_found_for_player", ChatColor.GRAY + "No regions found for %player% in this world."),
    REGION_LIST("region.list", ChatColor.GRAY + "%player%'s regions in this world: " + ChatColor.AQUA + "%regions%"),
    REGION_REMOVE("region.remove", ChatColor.YELLOW + "%player%'s regions have been removed in this world, and removed from regions %player% partially owned."),
    REGION_DISOWN("region.remove", ChatColor.YELLOW + "%player% has been removed as owner from all regions on this world."),
    REGION_ERROR_SEARCH("region.error_search", ChatColor.RED + "Error while searching for %player%'s regions. Please make sure you have entered the correct name."),

    // ps tp
    TP_HELP("tp.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps tp [id/player] [num (optional)]"),
    TP_HELP_DESC("tp.help_desc", "Teleports you to one of a given player's regions."),
    NUMBER_ABOVE_ZERO("tp.number_above_zero", ChatColor.RED + "Please enter a number above 0."),
    TP_VALID_NUMBER("tp.valid_number", ChatColor.RED + "Please enter a valid number."),
    ONLY_HAS_REGIONS("tp.only_has_regions", ChatColor.RED + "%player% only has %num% protected regions in this world!"),
    TPING("tp.tping", ChatColor.GREEN + "Teleporting..."),
    TP_ERROR_NAME("tp.error_name", ChatColor.RED + "Error in teleporting to protected region! (parsing WG region name error)"),
    TP_ERROR_TP("tp.error_tp", ChatColor.RED + "Error in finding the region to teleport to!"),
    TP_IN_SECONDS("tp.in_seconds", ChatColor.GRAY + "Teleporting in " + ChatColor.AQUA + "%seconds%" + ChatColor.GRAY + " seconds."),
    TP_CANCELLED_MOVED("tp.cancelled_moved", ChatColor.RED + "Teleport cancelled. You moved!"),

    // ps home
    HOME_HELP("home.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps home [name/id]"),
    HOME_HELP_DESC("home.help_desc", "Teleports you to one of your protected regions."),
    HOME_HEADER("home.header", ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "=====" + ChatColor.RESET + " Homes (click to teleport) " + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "====="),
    HOME_CLICK_TO_TP("home.click_to_tp", "Click to teleport!"),
    HOME_NEXT("home.next_page", ChatColor.GRAY + "Do /ps home -p %page% to go to the next page!"),

    // ps unclaim
    UNCLAIM_HELP("unclaim.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps unclaim"),
    UNCLAIM_HELP_DESC("unclaim.help_desc", "Use this command to pickup a placed protection stone and remove the region."),

    // ps view
    VIEW_HELP("view.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps view"),
    VIEW_HELP_DESC("view.help_desc", "Use this command to view the borders of a protected region."),
    VIEW_COOLDOWN("view.cooldown", ChatColor.RED + "Please wait a while before using /ps view again."),
    VIEW_GENERATING("view.generating", ChatColor.GRAY + "Generating border..."),
    VIEW_GENERATE_DONE("view.generate_done", ChatColor.GREEN + "Done! The border will disappear after 30 seconds!"),
    VIEW_REMOVING("view.removing", ChatColor.AQUA + "Removing border...\n" + ChatColor.GREEN + "If you still see ghost blocks, relog!"),

    // ps admin
    ADMIN_HELP("admin.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps admin"),
    ADMIN_HELP_DESC("admin.help_desc", "Do /ps admin help for more information."),
    ADMIN_CLEANUP_HEADER("admin.cleanup_header", ChatColor.YELLOW + "Cleanup %arg% %days% days\n================"),
    ADMIN_CLEANUP_FOOTER("admin.cleanup_footer", ChatColor.YELLOW + "================\nCompleted %arg% cleanup."),
    ADMIN_HIDE_TOGGLED("admin.hide_toggled", ChatColor.YELLOW + "All protection stones have been %message% in this world."),
    ADMIN_LAST_LOGON("admin.last_logon", ChatColor.YELLOW + "%player% last played %days% days ago."),
    ADMIN_IS_BANNED("admin.is_banned", ChatColor.YELLOW + "%player% is banned."),
    ADMIN_ERROR_PARSING("admin.error_parsing", ChatColor.RED + "Error parsing days, are you sure it is a number?"),
    ADMIN_CONSOLE_WORLD("admin.console_world", ChatColor.RED + "Please specify the world as the last parameter."),
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
    ADDREMOVE_PLAYER_REACHED_LIMIT("addremove.player_reached_limit", ChatColor.RED + "This player has reached their region limit."),
    ADDREMOVE_PLAYER_NEEDS_TO_BE_ONLINE("addremove.player_needs_to_be_online", ChatColor.RED + "The player needs to be online to add them."),

    // ps get
    GET_HELP("get.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps get [block]"),
    GET_HELP_DESC("get.help_desc", "Use this command to get or purchase a protection block."),
    GET_GOTTEN("get.gotten", ChatColor.AQUA + "Added protection block to inventory!"),
    GET_NO_PERMISSION_BLOCK("get.no_permission_block", ChatColor.RED + "You don't have permission to get this block."),
    GET_HEADER("get.header", ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "=====" + ChatColor.RESET + " Protect Blocks (click to get) " + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "====="),
    GET_GUI_BLOCK("get.gui_block", ChatColor.GRAY + "> " + ChatColor.AQUA + "%alias% " + ChatColor.GRAY + "- %description% (" + ChatColor.WHITE + "$%price%" + ChatColor.GRAY + ")"),
    GET_GUI_HOVER("get.gui_hover", "Click to buy a %alias%!"),

    // ps give
    GIVE_HELP("give.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps give [block] [player] [amount (optional)]"),
    GIVE_HELP_DESC("give.help_desc", "Use this command to give a player a protection block."),
    GIVE_GIVEN("give.given", ChatColor.GRAY + "Gave " + ChatColor.AQUA + "%block%" + ChatColor.GRAY + " to " + ChatColor.AQUA + "%player%" + ChatColor.GRAY + "."),
    GIVE_NO_INVENTORY_ROOM("give.no_inventory_room", ChatColor.RED + "The player does not have enough inventory room."),

    // ps sethome
    SETHOME_HELP("sethome.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps sethome"),
    SETHOME_HELP_DESC("sethome.help_desc", "Use this command to set the home of a region to where you are right now."),
    SETHOME_SET("sethome.set", ChatColor.GRAY + "The home for " + ChatColor.AQUA + "%psid%" + ChatColor.GRAY + " has been set to your location."),

    // ps list
    LIST_HELP("list.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps list [player (optional)]"),
    LIST_HELP_DESC("list.help_desc", "Use this command to list the regions you, or another player owns."),
    LIST_HEADER("list.header", ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "=====" + ChatColor.RESET + " %player%'s Regions " + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "====="),
    LIST_OWNER("list.owner", ChatColor.GRAY + "Owner of:"),
    LIST_MEMBER("list.member", ChatColor.GRAY + "Member of:"),
    LIST_NO_REGIONS("list.no_regions", ChatColor.GRAY + "You currently do not own and are not a member of any regions."),
    LIST_NO_REGIONS_PLAYER("list.no_regions_player", ChatColor.AQUA + "%player% " + ChatColor.GRAY + "does not own and is not a member of any regions."),

    // ps name
    NAME_HELP("name.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps name [name|none]"),
    NAME_HELP_DESC("name.help_desc", "Use this command to give a nickname to your region, to make identifying your region easier."),
    NAME_REMOVED("name.removed", ChatColor.GRAY + "Removed the name for %id%."),
    NAME_SET_NAME("name.set_name", ChatColor.GRAY + "Set the name of %id% to " + ChatColor.AQUA + "%name%" + ChatColor.GRAY + "."),
    NAME_TAKEN("name.taken", ChatColor.GRAY + "The region name " + ChatColor.AQUA + "%name%" + ChatColor.GRAY + " has already been taken! Try another one."),

    // ps setparent
    SETPARENT_HELP("setparent.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps setparent [region|none]"),
    SETPARENT_HELP_DESC("setparent.help_desc", "Use this command to allow this region to inherit properties from another region (owners, members, flags, etc.)."),
    SETPARENT_SUCCESS("setparent.success", ChatColor.GRAY + "Successfully set the parent of " + ChatColor.AQUA + "%id%" + ChatColor.GRAY + " to " + ChatColor.AQUA + "%parent%" + ChatColor.GRAY + "."),
    SETPARENT_SUCCESS_REMOVE("setparent.success_remove", ChatColor.GRAY + "Successfully removed the parent of " + ChatColor.AQUA + "%id%" + ChatColor.GRAY + "."),
    SETPARENT_CIRCULAR_INHERITANCE("setparent.circular_inheritance", ChatColor.RED + "Detected circular inheritance (the parent already inherits from this region?). Parent not set."),

    // ps merge
    MERGE_HELP("merge.help", ChatColor.AQUA + "> " + ChatColor.GRAY + "/ps merge"),
    MERGE_HELP_DESC("merge.help_desc", "Use this command to merge the region you are in with other overlapping regions."),
    MERGE_DISABLED("merge.disabled", "Merging regions is disabled in the config!"),
    MERGE_MERGED("merge.merged", ChatColor.AQUA + "Regions were successfully merged!"),
    MERGE_HEADER("merge.header", ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "=====" + ChatColor.RESET + " Merge %region% (click to merge) " + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "====="),
    MERGE_WARNING("merge.warning", ChatColor.GRAY + "Note: This will delete all of the settings for the current region!"),
    MERGE_NOT_ALLOWED("merge.not_allowed", ChatColor.RED + "You are not allowed to merge this protection region type."),
    MERGE_INTO("merge.into", ChatColor.AQUA + "This region overlaps other regions you can merge into!"),
    MERGE_NO_REGIONS("merge.no_region", ChatColor.GRAY + "There are no overlapping regions to merge into."),
    MERGE_CLICK_TO_MERGE("merge.click_to_merge", "Click to merge with %region%!"),
    MERGE_AUTO_MERGED("merge.auto_merged", ChatColor.GRAY + "Region automatically merged with " + ChatColor.AQUA + "%region%" + ChatColor.GRAY + "."),

    ;

    private final String path;
    private final String defaultMessage;

    private final String[] placeholders;
    private final int placeholdersCount;
    private String message;
    private boolean isEmpty;

    private static final File conf = new File(ProtectionStones.getInstance().getDataFolder(), "messages.yml");

    PSL(String path, String defaultMessage, String... placeholders) {
        this.path = path;
        this.defaultMessage = defaultMessage;

        this.placeholders = placeholders;
        this.placeholdersCount = placeholders.length;
        this.message = defaultMessage;
        this.isEmpty = message.isEmpty();
    }

    public String msg() {
        return message;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    @Nullable
    public String format(final Object... args) {
        if (isEmpty) {
            return null;
        }

        if (this.placeholdersCount == 0) {
            return this.message;
        }

        if (this.placeholdersCount != args.length) {
            throw new IllegalArgumentException("Expected " + this.placeholdersCount + " arguments but got " + args.length);
        }

        return StringUtils.replaceEach(
                this.message,
                this.placeholders,
                Arrays.stream(args).filter(Objects::nonNull).map(Object::toString).toArray(String[]::new)
        );
    }

    public boolean send(@NotNull final CommandSender receiver, @NotNull final Object... args) {
        final String msg = this.format(args);

        if (msg != null) {
            receiver.sendMessage(msg);
        }

        return true;
    }

    public void append(@NotNull final StringBuilder builder, @NotNull final Object... args) {
        final String msg = this.format(args);

        if (msg != null) {
            builder.append(msg);
        }
    }

    // Sends a message to a commandsender if the string is not empty

    public static boolean msg(CommandSender p, String str) {
        if (str != null && !str.isEmpty() && p != null) {
            p.sendMessage(str);
        }
        return true;
    }

    public static boolean msg(PSPlayer p, String str) {
        return msg(p.getPlayer(), str);
    }

    public static void loadConfig() {
        YamlConfiguration yml = new YamlConfiguration();

        if (!conf.exists()) {
            try {
                conf.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            yml.load(conf); // can throw error
            for (PSL psl : PSL.values()) {

                // fix message if need be
                if (yml.getString(psl.path) == null) { // if msg not found in config
                    yml.set(psl.path, psl.defaultMessage);
                } else {
                    // psl upgrade conversions
                    if (psl == PSL.REACHED_REGION_LIMIT && yml.getString(psl.path).equals("&cYou can not create any more protected regions.")) {
                        yml.set(psl.path, psl.defaultMessage);
                    } else if (psl == PSL.REACHED_PER_BLOCK_REGION_LIMIT && yml.getString(psl.path).equals("&cYou can not create any more regions of this type.")) {
                        yml.set(psl.path, psl.defaultMessage);
                    }
                }

                // load message
                psl.message = applyInGameColours(yml.getString(psl.path));
                psl.isEmpty = psl.message.isEmpty();
            }
            try {
                yml.save(conf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) { // prevent bad messages.yml file from resetting the file
            e.printStackTrace();
        }
    }

    // match all %#123abc#% format for hex
    private static final Pattern hexPatternLong = Pattern.compile("(?<!\\\\\\\\)(%#[a-fA-F0-9]{8}%)"),
            hexPatternShort = Pattern.compile("(?<!\\\\\\\\)(%#[a-fA-F0-9]{6}%)");

    private static String applyInGameColours(String msg) {

        Matcher matcher = hexPatternLong.matcher(msg);
        while (matcher.find()) {
            String color = msg.substring(matcher.start() + 1, matcher.end() - 1);
            msg = msg.replace(msg.substring(matcher.start(), matcher.end()), "" + net.md_5.bungee.api.ChatColor.of(color));
        }

        matcher = hexPatternShort.matcher(msg);
        while (matcher.find()) {
            String color = msg.substring(matcher.start() + 1, matcher.end() - 1);
            msg = msg.replace(msg.substring(matcher.start(), matcher.end()), "" + net.md_5.bungee.api.ChatColor.of(color));
        }

        return msg.replace('&', '§');
    }
}
