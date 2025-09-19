package dev.espi.protectionstones;/*
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

import dev.espi.protectionstones.utils.ChatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum PSL {
    // messages.yml

    // --- General ---
    COOLDOWN("cooldown", "<gold>Warning: <gray>Please wait for %time% seconds before placing again!", "%time%"),
    NO_SUCH_COMMAND("no_such_command", "<red>No such command. please type /ps help for more info"),
    NO_ACCESS("no_access", "<red>You are not allowed to do that here."),
    NO_ROOM_IN_INVENTORY("no_room_in_inventory", "<red>You don't have enough room in your inventory."),
    NO_ROOM_DROPPING_ON_FLOOR("no_room_dropping_on_floor", "<red>You don't have enough room in your inventory. Dropping item on floor."),
    INVALID_BLOCK("invalid_block", "<red>Invalid protection block."),
    NOT_ENOUGH_MONEY("not_enough_money", "<red>You don't have enough money! The price is %price%.", "%price%"),
    PAID_MONEY("paid_money", "<aqua>You've paid $%price%.", "%price%"),
    INVALID_WORLD("invalid_world", "<red>Invalid world."),
    MUST_BE_PLAYER("must_be_player", "<red>You must be a player to execute this command."),
    GO_BACK_PAGE("go_back_page", "Go back a page."),
    GO_NEXT_PAGE("go_next_page", "Go to next page."),
    PAGE_DOES_NOT_EXIST("page_does_not_exist", "<red>Page does not exist."),

    HELP("help", "<dark_gray><st>=====</st><reset> PS Help <dark_gray><st>=====</st>\n<aqua>> <gray>/ps help"),
    HELP_NEXT("help_next", "<gray>Do /ps help %page% to go to the next page!", "%page%"),

    COMMAND_REQUIRES_PLAYER_NAME("command_requires_player_name", "<red>This command requires a player name."),

    NO_PERMISSION_TOGGLE("no_permission_toggle", "<red>You don't have permission to use the toggle command."),
    NO_PERMISSION_CREATE("no_permission_create", "<red>You don't have permission to place a protection block."),
    NO_PERMISSION_CREATE_SPECIFIC("no_permission_create_specific", "<red>You don't have permission to place this protection block type."),
    NO_PERMISSION_DESTROY("no_permission_destroy", "<red>You don't have permission to destroy a protection block."),
    NO_PERMISSION_MEMBERS("no_permission_members", "<red>You don't have permission to use member commands."),
    NO_PERMISSION_OWNERS("no_permission_owners", "<red>You don't have permission to use owner commands."),
    NO_PERMISSION_ADMIN("no_permission_admin", "<red>You do not have permission to use that command."),
    NO_PERMISSION_COUNT("no_permission_count", "<red>You do not have permission to use that command."),
    NO_PERMISSION_COUNT_OTHERS("no_permission_count_others", "<red>You do not have permission to use that command."),
    NO_PERMISSION_FLAGS("no_permission_flags", "<red>You do not have permission to use flag commands."),
    NO_PERMISSION_PER_FLAG("no_permission_per_flag", "<red>You do not have permission to use that flag."),
    NO_PERMISSION_RENT("no_permission_rent", "<red>You do not have permission for renting."),
    NO_PERMISSION_TAX("no_permission_tax", "<red>You do not have permission to use the tax command."),
    NO_PERMISSION_BUYSELL("no_permission_buysell", "<red>You do not have permission to buy and sell regions."),
    NO_PERMISSION_UNHIDE("no_permission_unhide", "<red>You do not have permission to unhide protection blocks."),
    NO_PERMISSION_HIDE("no_permission_hide", "<red>You do not have permission to hide protection blocks."),
    NO_PERMISSION_INFO("no_permission_info", "<red>You do not have permission to use the region info command."),
    NO_PERMISSION_PRIORITY("no_permission_priority", "<red>You do not have permission to use the priority command."),
    NO_PERMISSION_REGION("no_permission_region", "<red>You do not have permission to use region commands."),
    NO_PERMISSION_TP("no_permission_tp", "<red>You do not have permission to teleport to other players' protection blocks."),
    NO_PERMISSION_HOME("no_permission_home", "<red>You do not have permission to teleport to your protection blocks."),
    NO_PERMISSION_UNCLAIM("no_permission_unclaim", "<red>You do not have permission to use the unclaim command."),
    NO_PERMISSION_UNCLAIM_REMOTE("no_permission_unclaim_remote", "<red>You do not have permission to use the unclaim remote command."),
    NO_PERMISSION_VIEW("no_permission_view", "<red>You do not have permission to use the view command."),
    NO_PERMISSION_GIVE("no_permission_give", "<red>You do not have permission to use the give command."),
    NO_PERMISSION_GET("no_permission_get", "<red>You do not have permission to use the get command."),
    NO_PERMISSION_SETHOME("no_permission_sethome", "<red>You do not have permission to use the sethome command."),
    NO_PERMISSION_LIST("no_permission_list", "<red>You do not have permission to use the list command."),
    NO_PERMISSION_LIST_OTHERS("no_permission_list_others", "<red>You do not have permission to use the list command for others."),
    NO_PERMISSION_NAME("no_permission_name", "<red>You do not have permission to use the name command."),
    NO_PERMISSION_SETPARENT("no_permission_setparent", "<red>You do not have permission to use the setparent command."),
    NO_PERMISSION_SETPARENT_OTHERS("no_permission_setparent_others", "<red>You do not have permission to inherit from regions you don't own."),
    NO_PERMISSION_MERGE("no_permission_merge", "<red>You do not have permission to use /ps merge."),

    // --- Region ---
    ADDED_TO_REGION("psregion.added_to_region", "<aqua>%player%<gray> has been added to this region.", "%player%"),
    ADDED_TO_REGION_SPECIFIC("psregion.added_to_region_specific", "<aqua>%player%<gray> has been added to region <aqua>%region%<gray>.", "%player%", "%region%"),
    REMOVED_FROM_REGION("psregion.removed_from_region", "<aqua>%player%<gray> has been removed from region.", "%player%"),
    REMOVED_FROM_REGION_SPECIFIC("psregion.removed_from_region_specific", "<aqua>%player%<gray> has been removed from region <aqua>%region%<gray>.", "%player%", "%region%"),
    NOT_IN_REGION("psregion.not_in_region", "<red>You are not in a protection stones region!"),
    PLAYER_NOT_FOUND("psregion.player_not_found", "<red>Player not found."),
    NOT_PS_REGION("psregion.not_ps_region", "<red>Not a protection stones region."),
    REGION_DOES_NOT_EXIST("psregion.region_does_not_exist", "<red>Region does not exist."),
    NO_REGIONS_OWNED("psregion.no_regions_owned", "<red>You don't own any protected regions in this world!"),
    NO_REGION_PERMISSION("psregion.no_region_permission", "<red>You do not have permission to do this in this region."),
    PROTECTED("psregion.protected", "<aqua>This area is now protected."),
    NO_LONGER_PROTECTED("psregion.no_longer_protected", "<yellow>This area is no longer protected."),
    CANT_PROTECT_THAT("psregion.cant_protect_that", "<red>You can't protect that area."),
    REACHED_REGION_LIMIT("psregion.reached_region_limit", "<red>You can not have any more protected regions (<aqua>%limit%</aqua><red>).", "%limit%"),
    REACHED_PER_BLOCK_REGION_LIMIT("psregion.reached_per_block_region_limit", "<red>You can not have any more regions of this type (<aqua>%limit%</aqua><red>).", "%limit%"),
    WORLD_DENIED_CREATE("psregion.world_denied_create", "<red>You can not create protections in this world."),
    REGION_OVERLAP("psregion.region_overlap", "<red>You can not place a protection block here as it overlaps another region."),
    REGION_TOO_CLOSE("psregion.region_too_close", "<red>Your protection block must be a minimum of <aqua>%num%</aqua> blocks from the edge of other regions!", "%num%"),
    REGION_CANT_TELEPORT("psregion.cant_teleport", "<red>Your teleportation was blocked by a protection region!"),
    SPECIFY_ID_INSTEAD_OF_ALIAS("psregion.specify_id_instead_of_alias", "<gray>There were multiple regions found with this name! Please use an ID instead.\n Regions with this name: <aqua>%regions%</aqua>", "%regions%"),
    REGION_NOT_ADJACENT("psregion.region_not_adjacent", "<red>You've passed the limit of non-adjacent regions! Try putting your protection block closer to other regions you already own."),
    REGION_NOT_OVERLAPPING("psregion.not_overlapping", "<red>These regions don't overlap each other!"),
    MULTI_REGION_DOES_NOT_EXIST("psregion.multi_region_does_not_exist", "One of these regions don't exist!"),
    NO_REGION_HOLES("psregion.no_region_holes", "<red>Unprotected area detected inside region! This is not allowed!"),
    DELETE_REGION_PREVENTED_NO_HOLES("psregion.delete_region_prevented", "<gray>The region could not be removed, possibly because it creates a hole in the existing region."),
    NOT_OWNER("psregion.not_owner", "<red>You are not an owner of this region!"),
    CANNOT_MERGE_RENTED_REGION("psregion.cannot_merge_rented_region", "<red>Cannot merge regions because region <aqua>%region%</aqua> is in the process of being rented out!", "%region%"),
    NO_PERMISSION_REGION_TYPE("psregion.no_permission_region_type", "<red>You do not have permission to have this region type."),
    REGION_HIDDEN("psregion.hidden", "<gray>The protection block is now hidden."),
    MUST_BE_PLACED_IN_EXISTING_REGION("psregion.must_be_placed_in_existing_region", "<red>This must be placed inside of an existing region!"),
    REGION_ALREADY_IN_LOCATION_IS_HIDDEN("psregion.already_in_location_is_hidden", "<red>A region already exists in this location (is the protection block hidden?)"),
    CANNOT_REMOVE_YOURSELF_LAST_OWNER("psregion.cannot_remove_yourself_last_owner", "<red>You cannot remove yourself as you are the last owner."),
    CANNOT_REMOVE_YOURSELF_FROM_ALL_REGIONS("psregion.cannot_remove_yourself_all_regions", "<red>You cannot remove yourself from all of your regions at once, for safety reasons."),

    // --- Toggle ---
    TOGGLE_HELP("toggle.help", "<aqua>> <gray>/ps toggle|on|off"),
    TOGGLE_HELP_DESC("toggle.help_desc", "Use this command to turn on or off placement of protection blocks."),
    TOGGLE_ON("toggle.toggle_on", "<aqua>Protection block placement turned on."),
    TOGGLE_OFF("toggle.toggle_off", "<aqua>Protection block placement turned off."),

    // --- Count ---
    COUNT_HELP("count.count_help", "<aqua>> <gray>/ps count [player (optional)]"),
    COUNT_HELP_DESC("count.count_help_desc", "Count the number of regions you own or another player."),
    PERSONAL_REGION_COUNT("count.personal_region_count", "<gray>Your region count in this world: <aqua>%num%</aqua>", "%num%"),
    PERSONAL_REGION_COUNT_MERGED("count.personal_region_count_merged", "<gray>- Including each merged region: <aqua>%num%</aqua>", "%num%"),
    OTHER_REGION_COUNT("count.other_region_count", "<gray>%player%'s region count in this world: <aqua>%num%</aqua>", "%player%", "%num%"),
    OTHER_REGION_COUNT_MERGED("count.other_region_count_merged", "<gray>- Including each merged region: <aqua>%num%</aqua>", "%num%"),

    // --- Flag ---
    FLAG_HELP("flag.help", "<aqua>> <gray>/ps flag [flagname] [value|null|default]"),
    FLAG_HELP_DESC("flag.help_desc", "Use this command to set a flag in your protected region."),
    FLAG_SET("flag.flag_set", "<aqua>%flag%<gray> flag has been set.", "%flag%"),
    FLAG_NOT_SET("flag.flag_not_set", "<aqua>%flag%<gray> flag has <red>not</red><gray> been set. Check your values again.", "%flag%"),
    FLAG_PREVENT_EXPLOIT("flag.flag_prevent_exploit", "<red>This has been disabled to prevent exploits."),
    FLAG_PREVENT_EXPLOIT_HOVER("flag.flag_prevent_exploit_hover", "<red>Disabled for security reasons."),
    FLAG_GUI_HEADER("flag.gui_header", "<dark_gray><st>=====</st><reset> Flags (click to change) <dark_gray><st>=====</st>"),
    FLAG_GUI_HOVER_SET("flag.gui_hover_set", "<aqua>Click to set."),
    FLAG_GUI_HOVER_SET_TEXT("flag.gui_hover_set_text", "<aqua>Click to change.</aqua><white>\nCurrent value:\n%value%</white>", "%value%"),
    FLAG_GUI_HOVER_CHANGE_GROUP("flag.hover_change_group", "Click to set this flag to apply to only %group%.", "%group%"),
    FLAG_GUI_HOVER_CHANGE_GROUP_NULL("flag.hover_change_group_null", "<red>You must set this flag to a value before changing the group."),

    // --- Rent ---
    RENT_HELP("rent.help", "<aqua>> <gray>/ps rent"),
    RENT_HELP_DESC("rent.help_desc", "Use this command to manage rents (buying and selling)."),
    RENT_HELP_HEADER("rent.help_header", "<dark_gray><st>=====</st><reset> Rent Help <dark_gray><st>=====</st>"),
    RENT_ALREADY_RENTING("rent.already_renting", "<red>The region is already being rented out! You must stop leasing the region first."),
    RENT_NOT_RENTED("rent.not_rented", "<red>This region is not being rented."),
    RENT_LEASE_SUCCESS("rent.lease_success", "<aqua>Region leasing terms set:\n</aqua><aqua>Price: </aqua><gray>%price%</gray>\n<aqua>Payment Term: </aqua><gray>%period%</gray>", "%price%", "%period%"),
    RENT_STOPPED("rent.stopped", "<aqua>Leasing stopped."),
    RENT_EVICTED("rent.evicted", "<gray>Evicted tenant %tenant%.", "%tenant%"),
    RENT_NOT_RENTING("rent.not_renting", "<red>This region is not being rented out to tenants."),
    RENT_PAID_LANDLORD("rent.paid_landlord", "<aqua>%tenant%</aqua><gray> has paid </gray><aqua>$%price%</aqua><gray> for renting out </gray><aqua>%region%</aqua><gray>.", "%tenant%", "%price%", "%region%"),
    RENT_PAID_TENANT("rent.paid_tenant", "<gray>Paid </gray><aqua>$%price%</aqua><gray> to </gray><aqua>%landlord%</aqua><gray> for region </gray><aqua>%region%</aqua><gray>.", "%price%", "%landlord%", "%region%"),
    RENT_RENTING_LANDLORD("rent.renting_landlord", "<aqua>%player%</aqua><gray> is now renting out region </gray><aqua>%region%</aqua><gray>.", "%player%", "%region%"),
    RENT_RENTING_TENANT("rent.renting_tenant", "<gray>You are now renting out region </gray><aqua>%region%</aqua><gray> for </gray><aqua>%price%</aqua><gray> per </gray><aqua>%period%</aqua><gray>.", "%region%", "%price%", "%period%"),
    RENT_NOT_TENANT("rent.not_tenant", "<red>You are not the tenant of this region!"),
    RENT_TENANT_STOPPED_LANDLORD("rent.tenant_stopped_landlord", "<aqua>%player%</aqua><gray> has stopped renting out region </gray><aqua>%region%</aqua><gray>. It is now available for others to rent.", "%player%", "%region%"),
    RENT_TENANT_STOPPED_TENANT("rent.tenant_stopped_tenant", "<aqua>You have stopped renting out region %region%.", "%region%"),
    RENT_BEING_SOLD("rent.being_sold", "<red>The region is being sold! Do /ps sell stop first."),
    RENT_EVICT_NO_MONEY_TENANT("rent.evict_no_money_tenant", "<gray>You have been </gray><red>evicted</red><gray> from region </gray><aqua>%region%</aqua><gray> because you do not have enough money (%price%) to pay for rent.", "%region%", "%price%"),
    RENT_EVICT_NO_MONEY_LANDLORD("rent.evict_no_money_landlord", "<aqua>%tenant%</aqua><gray> has been </gray><red>evicted</red><gray> from region </gray><aqua>%region%</aqua><gray> because they are unable to afford rent.", "%tenant%", "%region%"),
    RENT_CANNOT_RENT_OWN_REGION("rent.cannot_rent_own_region", "<red>You cannot rent your own region!"),
    RENT_REACHED_LIMIT("rent.reached_limit", "<red>You've reached the limit of regions you are allowed to rent!"),
    RENT_PRICE_TOO_LOW("rent.price_too_low", "<red>The rent price is too low (must be larger than %price%).", "%price%"),
    RENT_PRICE_TOO_HIGH("rent.price_too_high", "<red>The rent price is too high (must be lower than %price%).", "%price%"),
    RENT_PERIOD_TOO_SHORT("rent.period_too_short", "<red>The rent period is too short (must be longer than %period% seconds).", "%period%"),
    RENT_PERIOD_TOO_LONG("rent.period_too_long", "<red>The rent period is too long (must be shorter than %period% seconds).", "%period%"),
    RENT_PERIOD_INVALID("rent.period_invalid", "<red>Invalid period format! Example: 24h for once a day."),
    RENT_CANNOT_BREAK_WHILE_RENTING("rent.cannot_break_while_renting", "<red>You cannot break the region when it is being rented out."),

    // --- Tax ---
    TAX_HELP("tax.help", "<aqua>> <gray>/ps tax"),
    TAX_HELP_DESC("tax.help_desc", "Use this command to manage and pay taxes."),
    TAX_HELP_HEADER("tax.help_header", "<dark_gray><st>=====</st><reset> Taxes Help <dark_gray><st>=====</st>"),
    TAX_DISABLED_REGION("tax.disabled_region", "<red>Taxes are disabled for this region."),
    TAX_SET_AS_AUTOPAYER("tax.set_as_autopayer", "<gray>Taxes for region </gray><aqua>%region%</aqua><gray> will now be automatically paid by you.", "%region%"),
    TAX_SET_NO_AUTOPAYER("tax.set_no_autopayer", "<gray>Taxes for region </gray><aqua>%region%</aqua><gray> now have to be manually paid for.", "%region%"),
    TAX_PAID("tax.paid", "<gray>Paid </gray><aqua>$%amount%</aqua><gray> in taxes for region </gray><aqua>%region%</aqua><gray>.", "%amount%", "%region%"),
    TAX_INFO_HEADER("tax.info_header", "<dark_gray><st>=====</st><reset> Tax Info (click for more info) <dark_gray><st>=====</st>"),
    TAX_JOIN_MSG_PENDING_PAYMENTS("tax.join_msg_pending_payments", "<gray>You have </gray><aqua>$%money%</aqua><gray> in tax payments due on your regions!\nView them with /ps tax info.", "%money%"),
    TAX_PLAYER_REGION_INFO("tax.player_region_info", "<gray>> </gray><aqua>%region%</aqua><gray> - </gray><dark_aqua>$%money% due", "%region%", "%money%"),
    TAX_PLAYER_REGION_INFO_AUTOPAYER("tax.player_region_info_autopayer", "<gray>> </gray><aqua>%region%</aqua><gray> - </gray><dark_aqua>$%money% due</dark_aqua><gray> (you autopay)", "%region%", "%money%"),
    TAX_CLICK_TO_SHOW_MORE_INFO("tax.click_to_show_more_info", "Click to show more information."),
    TAX_REGION_INFO_HEADER("tax.region_info_header", "<dark_gray><st>=====</st><reset> %region% Tax Info <dark_gray><st>=====</st>", "%region%"),
    TAX_REGION_INFO("tax.region_info", "<blue>Tax Rate: </blue><gray>$%taxrate% (sum of all merged regions)</gray>\n"
            + "<blue>Time between tax cycles: </blue><gray>%taxperiod%</gray>\n"
            + "<blue>Time to pay taxes after cycle: </blue><gray>%taxpaymentperiod%</gray>\n"
            + "<blue>Tax Autopayer: </blue><gray>%taxautopayer%</gray>\n"
            + "<blue>Taxes Owed: </blue><gray>$%taxowed%</gray>", "%taxrate%", "%taxperiod%", "%taxpaymentperiod%", "%taxautopayer%", "%taxowed%"),
    TAX_NEXT("tax.next_page", "<gray>Do /ps tax info -p %page% to go to the next page!", "%page%"),

    // --- Buy ---
    BUY_HELP("buy.help", "<aqua>> <gray>/ps buy"),
    BUY_HELP_DESC("buy.help_desc", "Buy the region you are currently in."),
    BUY_NOT_FOR_SALE("buy.not_for_sale", "<red>This region is not for sale."),
    BUY_STOP_SELL("buy.stop_sell", "<gray>The region is now not for sale."),
    BUY_SOLD_BUYER("buy.sold_buyer", "<gray>Bought region </gray><aqua>%region%</aqua><gray> for </gray><aqua>$%price%</aqua><gray> from </gray><aqua>%player%</aqua><gray>.", "%region%", "%price%", "%player%"),
    BUY_SOLD_SELLER("buy.sold_seller", "<gray>Sold region </gray><aqua>%region%</aqua><gray> for </gray><aqua>$%price%</aqua><gray> to </gray><aqua>%player%</aqua><gray>.", "%region%", "%price%", "%player%"),

    // --- Sell ---
    SELL_HELP("sell.help", "<aqua>> <gray>/ps sell [price|stop]"),
    SELL_HELP_DESC("sell.help_desc", "Sell the region you are currently in."),
    SELL_RENTED_OUT("sell.rented_out", "<red>The region is being rented out! You must stop renting it out to sell."),
    SELL_FOR_SALE("sell.for_sale", "<gray>The region is now for sale for </gray><aqua>$%price%</aqua><gray>.", "%price%"),

    // --- Hide/Unhide ---
    VISIBILITY_HIDE_HELP("visibility.hide_help", "<aqua>> <gray>/ps hide"),
    VISIBILITY_HIDE_HELP_DESC("visibility.hide_help_desc", "Use this command to hide or unhide your protection block."),
    VISIBILITY_UNHIDE_HELP("visibility.unhide_help", "<aqua>> <gray>/ps unhide"),
    VISIBILITY_UNHIDE_HELP_DESC("visibility.unhide_help_desc", "Use this command to hide or unhide your protection block."),
    ALREADY_NOT_HIDDEN("visibility.already_not_hidden", "<gray>The protection stone doesn't appear hidden..."),
    ALREADY_HIDDEN("visibility.already_hidden", "<gray>The protection stone appears to already be hidden..."),

    // --- Info ---
    INFO_HELP("info.help", "<aqua>> <gray>/ps info members|owners|flags"),
    INFO_HELP_DESC("info.help_desc", "Use this command inside a ps region to see more information about it."),
    INFO_HEADER("info.header", "<dark_gray><st>=====</st><reset> PS Info <dark_gray><st>=====</st>"),
    INFO_TYPE2("info.type2", "<blue>Type: </blue><gray>%type%</gray>", "%type%"),
    INFO_MAY_BE_MERGED("info.may_be_merged", "(may be merged with other types)"),
    INFO_MERGED2("info.merged2", "<blue>Merged regions: </blue><gray>%merged%</gray>", "%merged%"),
    INFO_MEMBERS2("info.members2", "<blue>Members: </blue><gray>%members%</gray>", "%members%"),
    INFO_NO_MEMBERS("info.no_members", "<red>(no members)"),
    INFO_OWNERS2("info.owners2", "<blue>Owners: </blue><gray>%owners%</gray>", "%owners%"),
    INFO_NO_OWNERS("info.no_owners", "<red>(no owners)"),
    INFO_FLAGS2("info.flags2", "<blue>Flags: </blue><gray>%flags%</gray>", "%flags%"),
    INFO_NO_FLAGS("info.no_flags", "(none)"),
    INFO_REGION2("info.region2", "<blue>Region: </blue><aqua>%region%</aqua>", "%region%"),
    INFO_PRIORITY2("info.priority2", "<blue>Priority: </blue><aqua>%priority%</aqua>", "%priority%"),
    INFO_PARENT2("info.parent2", "<blue>Parent: </blue><aqua>%parentregion%</aqua>", "%parentregion%"),
    INFO_BOUNDS_XYZ("info.bounds_xyz", "<blue>Bounds: </blue><aqua>(%minx%,%miny%,%minz%) -> (%maxx%,%maxy%,%maxz%)</aqua>",
            "%minx%", "%miny%", "%minz%", "%maxx%", "%maxy%", "%maxz%"),
    INFO_BOUNDS_XZ("info.bounds_xz", "<blue>Bounds: </blue><aqua>(%minx%, %minz%) -> (%maxx%, %maxz%)</aqua>",
            "%minx%", "%minz%", "%maxx%", "%maxz%"),
    INFO_SELLER2("info.seller2", "<blue>Seller: </blue><gray>%seller%</gray>", "%seller%"),
    INFO_PRICE2("info.price2", "<blue>Price: </blue><gray>%price%</gray>", "%price%"),
    INFO_TENANT2("info.tenant2", "<blue>Tenant: </blue><gray>%tenant%</gray>", "%tenant%"),
    INFO_LANDLORD2("info.landlord2", "<blue>Landlord: </blue><gray>%landlord%</gray>", "%landlord%"),
    INFO_RENT2("info.rent2", "<blue>Rent: </blue><gray>%rent%</gray>", "%rent%"),
    INFO_AVAILABLE_FOR_SALE("info.available_for_sale", "<aqua>Region available for sale!"),
    INFO_AVAILABLE_FOR_RENT("info.available_for_rent", "<aqua>Region available for rent!"),

    // --- Priority ---
    PRIORITY_HELP("priority.help", "<aqua>> <gray>/ps priority [number|null]"),
    PRIORITY_HELP_DESC("priority.help_desc", "Use this command to set your region's priority."),
    PRIORITY_INFO("priority.info", "<gray>Priority: %priority%", "%priority%"),
    PRIORITY_SET("priority.set", "<yellow>Priority has been set."),
    PRIORITY_ERROR("priority.error", "<red>Error parsing input, check it again?"),

    // --- Region (admin over players) ---
    REGION_HELP("region.help", "<aqua>> <gray>/ps region [list|remove|disown] [playername]"),
    REGION_HELP_DESC("region.help_desc", "Use this command to find information or edit other players' (or your own) protected regions."),
    REGION_NOT_FOUND_FOR_PLAYER("region.not_found_for_player", "<gray>No regions found for %player% in this world.", "%player%"),
    REGION_LIST("region.list", "<gray>%player%'s regions in this world: </gray><aqua>%regions%</aqua>", "%player%", "%regions%"),
    REGION_REMOVE("region.remove", "<yellow>%player%'s regions have been removed in this world, and they have been removed from regions they co-owned.", "%player%"),
    REGION_DISOWN("region.disown", "<yellow>%player% has been removed as owner from all regions on this world.", "%player%"),
    REGION_ERROR_SEARCH("region.error_search", "<red>Error while searching for %player%'s regions. Please make sure you have entered the correct name.", "%player%"),

    // --- TP ---
    TP_HELP("tp.help", "<aqua>> <gray>/ps tp [id/player] [num (optional)]"),
    TP_HELP_DESC("tp.help_desc", "Teleports you to one of a given player's regions."),
    NUMBER_ABOVE_ZERO("tp.number_above_zero", "<red>Please enter a number above 0."),
    TP_VALID_NUMBER("tp.valid_number", "<red>Please enter a valid number."),
    ONLY_HAS_REGIONS("tp.only_has_regions", "<red>%player% only has %num% protected regions in this world!", "%player%", "%num%"),
    TPING("tp.tping", "<green>Teleporting..."),
    TP_ERROR_NAME("tp.error_name", "<red>Error in teleporting to protected region! (parsing WG region name error)"),
    TP_ERROR_TP("tp.error_tp", "<red>Error in finding the region to teleport to!"),
    TP_IN_SECONDS("tp.in_seconds", "<gray>Teleporting in </gray><aqua>%seconds%</aqua><gray> seconds.</gray>", "%seconds%"),
    TP_CANCELLED_MOVED("tp.cancelled_moved", "<red>Teleport cancelled. You moved!"),

    // --- Home ---
    HOME_HELP("home.help", "<aqua>> <gray>/ps home [name/id]"),
    HOME_HELP_DESC("home.help_desc", "Teleports you to one of your protected regions."),
    HOME_HEADER("home.header", "<dark_gray><st>=====</st><reset> Homes (click to teleport) <dark_gray><st>=====</st>"),
    HOME_CLICK_TO_TP("home.click_to_tp", "Click to teleport!"),
    HOME_NEXT("home.next_page", "<gray>Do /ps home -p %page% to go to the next page!", "%page%"),

    // --- Unclaim ---
    UNCLAIM_HELP("unclaim.help", "<aqua>> <gray>/ps unclaim"),
    UNCLAIM_HELP_DESC("unclaim.help_desc", "Use this command to pickup a placed protection stone and remove the region."),
    UNCLAIM_HEADER("unclaim.header", "<dark_gray><st>=====</st><reset> Unclaim (click to unclaim) <dark_gray><st>=====</st>"),

    // --- View ---
    VIEW_HELP("view.help", "<aqua>> <gray>/ps view"),
    VIEW_HELP_DESC("view.help_desc", "Use this command to view the borders of a protected region."),
    VIEW_COOLDOWN("view.cooldown", "<red>Please wait a while before using /ps view again."),
    VIEW_GENERATING("view.generating", "<gray>Generating border..."),
    VIEW_GENERATE_DONE("view.generate_done", "<green>Done! The border will disappear after 30 seconds!"),
    VIEW_REMOVING("view.removing", "<aqua>Removing border...\n</aqua><green>If you still see ghost blocks, relog!"),

    // --- Admin ---
    ADMIN_HELP("admin.help", "<aqua>> <gray>/ps admin"),
    ADMIN_HELP_DESC("admin.help_desc", "Do /ps admin help for more information."),
    ADMIN_CLEANUP_HEADER("admin.cleanup_header", "<yellow>Cleanup %arg% %days% days\n================", "%arg%", "%days%"),
    ADMIN_CLEANUP_FOOTER("admin.cleanup_footer", "<yellow>================\nCompleted %arg% cleanup.", "%arg%"),
    ADMIN_HIDE_TOGGLED("admin.hide_toggled", "<yellow>All protection stones have been %message% in this world.", "%message%"),
    ADMIN_LAST_LOGON("admin.last_logon", "<yellow>%player% last played %days% days ago.", "%player%", "%days%"),
    ADMIN_IS_BANNED("admin.is_banned", "<yellow>%player% is banned.", "%player%"),
    ADMIN_ERROR_PARSING("admin.error_parsing", "<red>Error parsing days, are you sure it is a number?"),
    ADMIN_CONSOLE_WORLD("admin.console_world", "<red>Please specify the world as the last parameter."),
    ADMIN_LASTLOGONS_HEADER("admin.lastlogons_header", "<yellow>%days% Days Plus:\n================", "%days%"),
    ADMIN_LASTLOGONS_LINE("admin.lastlogons_line", "<yellow>%player% %time% days", "%player%", "%time%"),
    ADMIN_LASTLOGONS_FOOTER("admin.lastlogons_footer", "<yellow>================\n%count% Total Players Shown\n%checked% Total Players Checked", "%count%", "%checked%"),

    // --- Reload ---
    RELOAD_HELP("reload.help", "<aqua>> <gray>/ps reload"),
    RELOAD_HELP_DESC("reload.help_desc", "Reload settings from the config."),
    RELOAD_START("reload.start", "<aqua>Reloading config..."),
    RELOAD_COMPLETE("reload.complete", "<aqua>Completed config reload!"),

    // --- Add/Remove ---
    ADDREMOVE_HELP("addremove.help", "<aqua>> <gray>/ps add|remove [playername]"),
    ADDREMOVE_HELP_DESC("addremove.help_desc", "Use this command to add or remove a member of your protected region."),
    ADDREMOVE_OWNER_HELP("addremove.owner_help", "<aqua>> <gray>/ps addowner|removeowner [playername]"),
    ADDREMOVE_OWNER_HELP_DESC("addremove.owner_help_desc", "Use this command to add or remove an owner of your protected region."),
    ADDREMOVE_PLAYER_REACHED_LIMIT("addremove.player_reached_limit", "<red>This player has reached their region limit."),
    ADDREMOVE_PLAYER_NEEDS_TO_BE_ONLINE("addremove.player_needs_to_be_online", "<red>The player needs to be online to add them."),

    // --- Get ---
    GET_HELP("get.help", "<aqua>> <gray>/ps get [block]"),
    GET_HELP_DESC("get.help_desc", "Use this command to get or purchase a protection block."),
    GET_GOTTEN("get.gotten", "<aqua>Added protection block to inventory!"),
    GET_NO_PERMISSION_BLOCK("get.no_permission_block", "<red>You don't have permission to get this block."),
    GET_HEADER("get.header", "<dark_gray><st>=====</st><reset> Protect Blocks (click to get) <dark_gray><st>=====</st>"),
    GET_GUI_BLOCK("get.gui_block", "<gray>> </gray><aqua>%alias%</aqua><gray> - %description% (</gray><white>$%price%</white><gray>)", "%alias%", "%description%", "%price%"),
    GET_GUI_HOVER("get.gui_hover", "Click to buy a %alias%!", "%alias%"),

    // --- Give ---
    GIVE_HELP("give.help", "<aqua>> <gray>/ps give [block] [player] [amount (optional)]"),
    GIVE_HELP_DESC("give.help_desc", "Use this command to give a player a protection block."),
    GIVE_GIVEN("give.given", "<gray>Gave </gray><aqua>%block%</aqua><gray> to </gray><aqua>%player%</aqua><gray>.", "%block%", "%player%"),
    GIVE_NO_INVENTORY_ROOM("give.no_inventory_room", "<red>The player does not have enough inventory room."),

    // --- Sethome ---
    SETHOME_HELP("sethome.help", "<aqua>> <gray>/ps sethome"),
    SETHOME_HELP_DESC("sethome.help_desc", "Use this command to set the home of a region to where you are right now."),
    SETHOME_SET("sethome.set", "<gray>The home for </gray><aqua>%psid%</aqua><gray> has been set to your location.", "%psid%"),

    // --- List ---
    LIST_HELP("list.help", "<aqua>> <gray>/ps list [player (optional)]"),
    LIST_HELP_DESC("list.help_desc", "Use this command to list the regions you, or another player owns."),
    LIST_HEADER("list.header", "<dark_gray><st>=====</st><reset> %player%'s Regions <dark_gray><st>=====</st>", "%player%"),
    LIST_OWNER("list.owner", "<gray>Owner of:"),
    LIST_MEMBER("list.member", "<gray>Member of:"),
    LIST_NO_REGIONS("list.no_regions", "<gray>You currently do not own and are not a member of any regions."),
    LIST_NO_REGIONS_PLAYER("list.no_regions_player", "<aqua>%player%</aqua><gray> does not own and is not a member of any regions.", "%player%"),

    // --- Name ---
    NAME_HELP("name.help", "<aqua>> <gray>/ps name [name|none]"),
    NAME_HELP_DESC("name.help_desc", "Use this command to give a nickname to your region, to make identifying your region easier."),
    NAME_REMOVED("name.removed", "<gray>Removed the name for %id%.", "%id%"),
    NAME_SET_NAME("name.set_name", "<gray>Set the name of %id% to </gray><aqua>%name%</aqua><gray>.", "%id%", "%name%"),
    NAME_TAKEN("name.taken", "<gray>The region name </gray><aqua>%name%</aqua><gray> has already been taken! Try another one.", "%name%"),

    // --- Setparent ---
    SETPARENT_HELP("setparent.help", "<aqua>> <gray>/ps setparent [region|none]"),
    SETPARENT_HELP_DESC("setparent.help_desc", "Use this command to allow this region to inherit properties from another region (owners, members, flags, etc.)."),
    SETPARENT_SUCCESS("setparent.success", "<gray>Successfully set the parent of </gray><aqua>%id%</aqua><gray> to </gray><aqua>%parent%</aqua><gray>.", "%id%", "%parent%"),
    SETPARENT_SUCCESS_REMOVE("setparent.success_remove", "<gray>Successfully removed the parent of </gray><aqua>%id%</aqua><gray>.", "%id%"),
    SETPARENT_CIRCULAR_INHERITANCE("setparent.circular_inheritance", "<red>Detected circular inheritance (the parent already inherits from this region?). Parent not set."),

    // --- Merge ---
    MERGE_HELP("merge.help", "<aqua>> <gray>/ps merge"),
    MERGE_HELP_DESC("merge.help_desc", "Use this command to merge the region you are in with other overlapping regions."),
    MERGE_DISABLED("merge.disabled", "Merging regions is disabled in the config!"),
    MERGE_MERGED("merge.merged", "<aqua>Regions were successfully merged!"),
    MERGE_HEADER("merge.header", "<dark_gray><st>=====</st><reset> Merge %region% (click to merge) <dark_gray><st>=====</st>", "%region%"),
    MERGE_WARNING("merge.warning", "<gray>Note: This will delete all of the settings for the current region!"),
    MERGE_NOT_ALLOWED("merge.not_allowed", "<red>You are not allowed to merge this protection region type."),
    MERGE_INTO("merge.into", "<aqua>This region overlaps other regions you can merge into!"),
    MERGE_NO_REGIONS("merge.no_region", "<gray>There are no overlapping regions to merge into."),
    MERGE_CLICK_TO_MERGE("merge.click_to_merge", "Click to merge with %region%!", "%region%"),
    MERGE_AUTO_MERGED("merge.auto_merged", "<gray>Region automatically merged with </gray><aqua>%region%</aqua><gray>.", "%region%"),
    ;

    // ===== FIELDS =====
    private final String path;
    private final String defaultMessage;
    private final String[] placeholders;
    private final int placeholdersCount;

    private Component message;
    private boolean isEmpty;

    // NOTE: if your enum constants are declared ABOVE these fields (usual for enums),
    // do NOT reference them in the constructor. That's why we call MiniMessage.miniMessage() inline there.
    private static final File conf =
            new File(ProtectionStones.getInstance().getDataFolder(), "messages.yml");
    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    // &#RRGGBB -> <#RRGGBB>
    private static final Pattern HEX_HASH = Pattern.compile("(?<!\\\\)&#([a-fA-F0-9]{6})");
    // §x -> &x  (so legacy ampersand parser can handle them)
    private static final Pattern SECTION = Pattern.compile("§([0-9a-frk-orA-FRK-OR])");

    PSL(String path, String defaultMessage, String... placeholders) {
        this.path = path;
        this.defaultMessage = defaultMessage;
        this.placeholders = placeholders;
        this.placeholdersCount = placeholders.length;
        this.message = MiniMessage.miniMessage().deserialize(defaultMessage);
        this.isEmpty = defaultMessage.isEmpty();
    }

    public String path() { return path; }
    public String def() { return defaultMessage; }
    public String[] placeholders() { return placeholders; }

    public Component msg() { return message; }
    public boolean isEmpty() { return isEmpty; }

    @Nullable
    public Component format(final Object... args) {
        if (isEmpty) return null;
        if (this.placeholdersCount == 0) return this.message;

        if (this.placeholdersCount != args.length) {
            throw new IllegalArgumentException("Expected " + this.placeholdersCount + " arguments but got " + args.length);
        }

        Component formatted = this.message;
        for (int i = 0; i < placeholdersCount; i++) {
            final String ph  = this.placeholders[i];            // effectively final
            final String rep = Objects.toString(args[i], "");   // effectively final
            formatted = formatted.replaceText(b -> b.matchLiteral(ph).replacement(rep));
        }
        return formatted;
    }

    // Convenience: replace a single placeholder in THIS message and return a new Component
    public Component replace(final String placeholder, final String value) {
        if (isEmpty) return null;
        final String ph  = Objects.toString(placeholder, "");
        final String rep = Objects.toString(value, "");
        return this.message.replaceText(b -> b.matchLiteral(ph).replacement(rep));
    }

    // Convenience: replace many placeholders in THIS message (map keys are the literals like "%price%")
    public Component replaceAll(final Map<String, String> replacements) {
        if (isEmpty) return null;
        Component out = this.message;
        for (Map.Entry<String, String> e : replacements.entrySet()) {
            final String ph  = Objects.toString(e.getKey(), "");
            final String rep = Objects.toString(e.getValue(), "");
            out = out.replaceText(b -> b.matchLiteral(ph).replacement(rep));
        }
        return out;
    }

    public boolean send(@NotNull final CommandSender receiver, @NotNull final Object... args) {
        final Component c = this.format(args);
        if (c != null && receiver != null) {
            ProtectionStones.getInstance().audiences().sender(receiver).sendMessage(c);
        }
        return true;
    }

    public void append(@NotNull final StringBuilder builder, @NotNull final Object... args) {
        final Component c = this.format(args);
        if (c != null) {
            builder.append(MINI.serialize(c));
        }
    }

    // Static helpers
    public static boolean msg(CommandSender p, Component comp) {
        return ChatUtil.send(p, comp);
    }
    // actionbar msgs
    public static boolean action(Player p, String comp) {
        return ChatUtil.sendActionBar(p, comp);
    }

    // ====== CONFIG LOAD / AUTO-MIGRATE ======
    public static void loadConfig() {
        final YamlConfiguration yml = new YamlConfiguration();

        if (!conf.exists()) {
            try { conf.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }

        try {
            yml.load(conf);
            boolean updated = false;

            for (PSL psl : PSL.values()) {
                String raw = yml.getString(psl.path);

                if (raw == null) {
                    // first time: write default MiniMessage
                    yml.set(psl.path, psl.defaultMessage);
                    psl.message = MINI.deserialize(psl.defaultMessage);
                    raw = psl.defaultMessage;
                    updated = true;
                } else {
                    // migrate legacy to MiniMessage if needed
                    String upgraded = upgradeToMini(raw);
                    if (!Objects.equals(upgraded, raw)) {
                        yml.set(psl.path, upgraded);
                        raw = upgraded;
                        updated = true;
                    }
                    psl.message = MINI.deserialize(raw);
                }

                psl.isEmpty = raw.isEmpty();
            }

            if (updated) yml.save(conf);
        } catch (Exception e) {
            // don't wipe the file on error
            e.printStackTrace();
        }
    }

    private static String upgradeToMini(String input) {
        String s = input;

        // 1) Convert §x codes to &x so the legacy deserializer can read them
        Matcher m = SECTION.matcher(s);
        if (m.find()) {
            s = m.replaceAll("&$1");
        }

        // 2) Convert '&#RRGGBB' to MiniMessage '<#RRGGBB>'
        Matcher hex = HEX_HASH.matcher(s);
        if (hex.find()) {
            s = hex.replaceAll("<#$1>");
        }

        // 3) If any &-style legacy codes remain, convert to Components and serialize to MiniMessage
        if (s.indexOf('&') >= 0) {
            Component comp = LEGACY.deserialize(s);
            s = MINI.serialize(comp);
        }

        return s;
    }
}
