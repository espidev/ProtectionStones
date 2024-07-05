# Commands

Some commands have extra permissions. Please be sure to check out the [permissions](permissions.md) page for more details about them.

### Obtaining Blocks

<mark style="color:purple;">**`/ps get [block]`**</mark>

* **Permission:** protectionstones.get
* Get a protection stone. Can be charged with currency set in the config (requires Vault)

<mark style="color:purple;">**`/ps give [block] [player] [amount (optional)]`**</mark>

* **Permission:** protectionstones.give
* Give a protection stone to another player as admin (free). Use this command in kit plugins to distribute protection blocks.

### Player Utilities

<mark style="color:purple;">**`/ps home [name/id (optional)]`**</mark>

* **Permission:** protectionstones.home
* Teleports you to one of your owned protected regions.

<mark style="color:purple;">**`/ps tp [id/player] [num (optional)]`**</mark>

* **Permission:** protectionstones.tp
* Teleports you to a region, or one of a given player's regions.

<mark style="color:purple;">**`/ps toggle|on|off`**</mark>

* **Permission:** protectionstones.toggle
* Turn on or off placement of protection stones blocks for yourself.

### Getting Information

<mark style="color:purple;">**`/ps info members|owners|flags`**</mark>

* **Permission:** protectionstones.info and protectionstones.info.others
* Use this command inside a ps region to see more information about it.

<mark style="color:purple;">**`/ps view`**</mark>

* **Permission:** protectionstones.view and protectionstones.view.others
* View the region's boundaries with particles.

<mark style="color:purple;">**`/ps list [playername (optional)]`**</mark>

* **Permission:** protectionstones.list and protectionstones.list.others
* List the regions you or another player owns.

<mark style="color:purple;">**`/ps count [playername (optional)]`**</mark>

* **Permission:** protectionstones.count and protectionstones.count.others
* Count the number you or another player owns.

### Managing Your Region

<mark style="color:purple;">**`/ps add|remove [playername]`**</mark>

* **Permission:** `protectionstones.members`
* Use this command to add or remove a **member** of your protected region. Add -a (`/ps add -a [name]` to add to all regions you own.

<mark style="color:purple;">**`/ps addowner|removeowner [playername]`**</mark>

* **Permission:** `protectionstones.owners`
* Use this command to add or remove an **owner** of your protected region.

<mark style="color:purple;">**`/ps flag [flag] [value|null|default]`**</mark>

* **Permission:** `protectionstones.flags`
* Allows players to set their region flags. Use just `/ps flag` to use the GUI menu.

<mark style="color:purple;">**`/ps name [name|none]`**</mark>

* **Permission:** protectionstones.name
* Set a name on your region to help identify it easily.

<mark style="color:purple;">**`/ps sethome`**</mark>

* **Permission:** protectionstones.sethome
* Set the home location of an owned region.

<mark style="color:purple;">**`/ps merge`**</mark>

* **Permission:** protectionstones.merge
* Merge the region you are in into a neighbouring region.

<mark style="color:purple;">**`/ps unclaim [id (optional)]`**</mark>

* **Permission:** protectionstones.unclaim and protectionstones.unclaim.remote
* Use this command to pickup a placed protection stone and remove the region.

<mark style="color:purple;">**`/ps hide`**</mark>

* **Permission:** protectionstones.hide
* Use this command to hide your protectionstones block.

<mark style="color:purple;">**`/ps unhide`**</mark>

* **Permission:** protectionstones.unhide
* Use this command to unhide your protectionstones block.

### Economy

<mark style="color:purple;">**`/ps rent [lease|rent|stopleasing|stoprenting]`**</mark>

* **Permission:** protectionstones.rent
* Allows players to lease out their own regions, and rent other regions.

<mark style="color:purple;">**`/ps tax`**</mark>

* **Permission:** protectionstones.tax
* Allows players to pay and manage tax payments.

<mark style="color:purple;">**`/ps buy`**</mark>

* **Permission:** protectionstones.buysell
* Buy the region you are currently in.

<mark style="color:purple;">**`/ps sell [price|stop]`**</mark>

* **Permission:** protectionstones.buysell
* Sell the region you are currently in.

### Other Commands

<mark style="color:purple;">**`/ps setparent [region|none]`**</mark>

* **Permission:** protectionstones.setparent
* Set the region you are in to inherit properties from another region you own, see: [https://worldguard.enginehub.org/en/latest/regions/priorities/](https://worldguard.enginehub.org/en/latest/regions/priorities/)

<mark style="color:purple;">**`/ps priority [number|null]`**</mark>

* **Permission:** protectionstones.priority
* Use this command to set your region's WorldGuard [priority](https://worldguard.enginehub.org/en/latest/regions/priorities/).

<mark style="color:purple;">**`/ps region [list|remove|disown] [player]`**</mark>

* **Permission:** protectionstones.region
* This is an administrative command, not recommended to be given to players. Use this command to find information or edit other players' (or your own) protected regions.

### Admin Commands

All of the following commands require the protectionstones.admin permission.

<mark style="color:purple;">**`/ps reload`**</mark>

* Reload the plugin with settings from the config.

<mark style="color:purple;">**`/ps admin help`**</mark>

* Shows the help menu.

<mark style="color:purple;">**`/ps admin version`**</mark>

* Shows the version number of the plugin.

<mark style="color:purple;">**`/ps admin debug`**</mark>

* Toggles debug mode in the plugin, more messages will be printed to the server console.

<mark style="color:purple;">**`/ps admin hide|unhide`**</mark>

* Hide or unhide all of the protection stone blocks in the world you are in.

<mark style="color:purple;">**`/ps admin cleanup remove [-t typealias (optional)] [days] [world (console)]`**</mark>

* Removes regions where all of the owners have not joined within the past # of \[days]. It is configurable in config.toml to prevent deleting the region if the members have been active.

<mark style="color:purple;">**`/ps admin cleanup preview [-t typealias (optional)] [days] [world (console)]`**</mark>

* Preview which regions would be deleted if the remove command was run. Dumps the region list to a file.

<mark style="color:purple;">**`/ps admin flag [world] [flag] [value|null|default]`**</mark>

* Set a flag for all protection stone regions in a world.

<mark style="color:purple;">**`/ps admin lastlogon [player]`**</mark>

* Get the last time a player logged on.

<mark style="color:purple;">**`/ps admin lastlogons`**</mark>

* List all of the last logons of each player.

<mark style="color:purple;">**`/ps admin stats [player (optional)]`**</mark>

* Show some plugin statistics.

<mark style="color:purple;">**`/ps admin recreate`**</mark>

* Recreate all PS regions using radius set in config.

<mark style="color:purple;">**`/ps admin settaxautopayers`**</mark>

* Add a tax autopayer for every region on the server that does not have one.

<mark style="color:purple;">**`/ps admin forcemerge [world]`**</mark>

* Merge overlapping PS regions together if they have the same owners, members and flags.

<mark style="color:purple;">**`/ps admin changeblock [world] [fromblockalias] [toblockalias]`**</mark>

* Change all of the PS blocks of a block type to a different block. Both blocks must be configured in config.

<mark style="color:purple;">**`/ps admin changeregiontype [world] [fromblocktype] [toblocktype]`**</mark>

* Change the internal type of all PS regions of a certain type. Useful for error correction.

<mark style="color:purple;">**`/ps admin fixregions`**</mark>

* Use this command to correct errors for all of the PS regions in a world. Useful if you have issues with the plugin.

