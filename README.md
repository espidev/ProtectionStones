# ProtectionStones
[![Maven Central](https://img.shields.io/maven-central/v/dev.espi/protectionstones.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22dev.espi%22%20AND%20a:%22protectionstones%22)

This project was originally based off of https://github.com/vik1395/ProtectionStones-Minecraft.

ProtectionStones is a grief prevention and land claiming plugin.

This plugin uses a specified type of minecraft block/blocks as a protection block. When a player placed a block of that type, they are able to protect a region around them. The size of the protected region is configurable in the plugins config file. You can also set which flags players can change and also the default flags to be set when a new region is created.

This plugin is based off the original ProtectionStones plugin by AxelDios.

The current Spigot page: https://www.spigotmc.org/resources/protectionstones-updated-for-1-13-2-1-14-wg7.61797/

The original ProtectionStones plugin (OUTDATED): http://dev.bukkit.org/bukkit-plugins/protectionstones/

## Dependencies
* ProtectionStones 2.6.10
  * WorldGuard 7.0+
  * WorldEdit 7.0+
  * Vault (Optional)
  
## Default Configuration
Find it in the src/main/resources folder.

## Commands
Aliases in case of command conflicts: /ps, /protectionstone, /protectionstones, /pstone

    /ps get [block] - Get a protection stone. Can be charged with currency set in the config (requires Vault)
    /ps give [block] [player] - Give a protection stone to another player as admin (free).
    /ps info members|owners|flags - Use this command inside a ps region to see more information about it.
    /ps add|remove [playername] - Use this command to add or remove a member of your protected region.
    /ps addowner|removeowner [playername] - Use this command to add or remove an owner of your protected region.
    /ps flag [flagname] [setting|default] - Use this command to set a flag in your protected region.
    /ps rent - Use this command to manage rents (buying and selling).
    /ps tax - Use this command to manage taxes.
    /ps buy - Buy the region you are currently in.
    /ps sell [price|stop] - Sell the region you are currently in.
    /ps hide|unhide - Use this command to hide or unhide your protectionstones block.
    /ps setparent [region|none] - Set the region you are in to inherit properties from another region you own (https://worldguard.enginehub.org/en/latest/regions/priorities/)
    /ps name [name|none] - Nickname your region to help identify it easily.
    /ps home [name/id (optional)] - Teleports you to one of your protected regions.
    /ps sethome - Set the home location of an owned region.
    /ps tp [player] [num] or /ps tp [name/id] - Teleports you to a region, or one of a given player's regions.
    /ps toggle - Use this command to turn on or off placement of protection stones blocks.
    /ps view - Use this command to view the borders of a protected region.
    /ps unclaim - Use this command to pickup a placed protection stone and remove the region.
    /ps priority [number|null] - Use this command to set your region's priority.
    /ps region [list|remove|disown] [playername] - Use this command to find
    information or edit other players' (or your own) protected regions.
    /ps list [playername (optional)] - List the regions you or another player owns.
    /ps count [playername (optional)] - Count the number of regions you own or another player.
    /ps merge - Open the merge menu to merge regions with ones you own.
    /ps admin [help|version|settings|hide|unhide|cleanup|flag|lastlogon|lastlogons|stats|forcemerge|recreate|changeblock|fixregions] - This is an admin command showing different stats and allowing to override other player's regions.
    /ps reload - Reload settings from the config.

## Permissions

    protectionstones.create - Protect a region by placing a ProtectionStones block.
    protectionstones.destroy - Allow players to remove their own protected regions (block break).
    protectionstones.merge - Allows players to merge their regions with other regions they own.
    protectionstones.unclaim - Allow players to unclaim their region using /ps unclaim.
    protectionstones.view - Allows players the use of /ps view.
    protectionstones.info - Allows players the use of /ps info.
    protectionstones.info.others - Allows players the use of /ps info in unowned regions.
    protectionstones.get - Allows players the use of /ps get.
    protectionstones.give - Allows players the use of /ps give (give protectionstones to others as admin).
    protectionstones.count - Allows players the use of /ps count.
    protectionstones.count.others - Allows players the use of /ps count [player].
    protectionstones.list - Allows players the use of /ps list.
    protectionstones.list.others - Allows players to do /ps list [player].
    protectionstones.hide - Allow players to hide their ProtectionStones block.
    protectionstones.unhide - Allow players to unhide their ProtectionStones block.
    protectionstones.setparent - Allow access to /ps setparent.
    protectionstones.setparent.others - Allow players to set their region to inherit properties from other regions they don't own.
    protectionstones.name - Access to the /ps name command.
    protectionstones.home - Access to the /ps home command.
    protectionstones.sethome - Access to /ps sethome.
    protectionstones.tp - Access to /ps tp command.
    protectionstones.tp.bypasswait - Bypass the wait time set in the config for /ps home and /ps tp
    protectionstones.tp.bypassprevent - Bypass prevent_teleport_in option in config
    protectionstones.priority - Allows players to set their region's priority.
    protectionstones.owners - Allows players to add or remove region owners. Allows players to use /ps info owners command.
    protectionstones.members - Allows players to add or remove region members. Allows players to use /ps info members command.
    protectionstones.flags - Allows players to set their region flags.
    protectionstones.rent - Allows players access to /ps rent.
    protectionstones.rent.limit.x - Replace x with a limit for rented regions per player.
    protectionstones.buysell - Allows players access to /ps buy and /ps sell.
    protectionstones.tax - Allows players to access /ps tax commands.
    protectionstones.toggle - Allows players to toggle ProtectionStones placement.
    protectionstones.region - Allows players to use the /ps region commands.
    protectionstones.adjacent.x - Sets the number of non-adjacent regions players can have if "regions_must_be_adjacent" is true. Unlimited with -1.
    protectionstones.admin - This permission allows users to override all ProtectionStones regions and use /ps admin and /ps reload.
    protectionstones.limit.x - Replace x with a limit for players' protected regions.
    protectionstones.limit.alias.x - Replace alias with the protection block alias and x with the limit of that protection block the player can place.
    If you don't want a limit, do not give this permission. x can only be replaced with an integer number.
    protectionstones.superowner - Allows players to override region permissions, and use ps commands without being the owner of a region.

This plugin is licensed under the **GPLv3**, as is required by Bukkit plugins.
