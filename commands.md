# Commands

Some commands have extra permissions. Please be sure to check out the [permissions](permissions.md) page for more details about them.

`/ps get [block]`

* **Permission:** `protectionstones.get`
* Get a protection stone. Can be charged with currency set in the config (requires Vault)

`/ps give [block] [player] [amount (optional)]`

* **Permission:** `protectionstones.give`
* Give a protection stone to another player as admin (free).

`/ps info members|owners|flags`

* **Permission:** `protectionstones.info`
* Use this command inside a ps region to see more information about it.

`/ps add|remove [playername]`

* **Permission:** `protectionstones.members`
* Use this command to add or remove a **member** of your protected region. Add -a (`/ps add -a [name]` to add to all regions you own.

`/ps addowner|removeowner [playername]`

* **Permission:** `protectionstones.owners`
* Use this command to add or remove an **owner** of your protected region.

`/ps flag [flag] [value|null|default]`

* **Permission:** `protectionstones.flags`
* Allows players to set their region flags. Use just `/ps flag` to use the GUI menu.

`/ps rent [lease|rent|stopleasing|stoprenting]`

* **Permission:** `protectionstones.rent`
* Allows players to lease out their own regions, and rent other regions.

`/ps tax`

* **Permission:** `protectionstones.tax`
* Allows players to pay and manage tax payments.

`/ps buy`

* **Permission:** `protectionstones.buysell`
* Buy the region you are currently in.

`/ps sell [price|stop]`

* **Permission:** `protectionstones.buysell`
* Sell the region you are currently in.

`/ps hide`

* **Permission:** `protectionstones.hide`
* Use this command to hide your protectionstones block.

`/ps unhide`

* **Permission:** `protectionstones.unhide`
* Use this command to unhide your protectionstones block.



<table data-full-width="true"><thead><tr><th width="381">Command</th><th width="257">Permission</th><th>Description</th></tr></thead><tbody><tr><td><code>/ps get [block]</code></td><td>protectionstones.get</td><td>Get a protection stone. Can be charged with currency set in the config (requires Vault)</td></tr><tr><td><code>/ps give [block] [player] [amount (optional)]</code></td><td>protectionstones.give</td><td>Give a protection stone to another player as admin (free).</td></tr><tr><td><code>/ps info members|owners|flags</code></td><td>protectionstones.info</td><td>Use this command inside a ps region to see more information about it.</td></tr><tr><td><code>/ps add|remove [playername]</code></td><td>protectionstones.members</td><td>Use this command to add or remove a member of your protected region. Add -a (<code>/ps add -a [name]</code> to add to all regions you own.</td></tr><tr><td><code>/ps addowner|removeowner [playername]</code></td><td>protectionstones.owners</td><td>Use this command to add or remove an owner of your protected region.</td></tr><tr><td><code>/ps flag [flag] [value|null|default]</code></td><td>protectionstones.flags</td><td>Allows players to set their region flags.</td></tr><tr><td><code>/ps rent [lease|rent|stopleasing|stoprenting]</code></td><td>protectionstones.rent</td><td>Allows players to lease out their own regions, and rent other regions.</td></tr><tr><td><code>/ps tax</code></td><td>protectionstones.tax</td><td>Allows players to pay and manage tax payments.</td></tr><tr><td><code>/ps buy</code></td><td>protectionstones.buysell</td><td>Buy the region you are currently in.</td></tr><tr><td><code>/ps sell [price|stop]</code></td><td>protectionstones.buysell</td><td>Sell the region you are currently in.</td></tr><tr><td><code>/ps hide</code></td><td>protectionstones.hide</td><td>Use this command to hide your protectionstones block.</td></tr><tr><td><code>/ps unhide</code></td><td>protectionstones.unhide</td><td>Use this command to unhide your protectionstones block.</td></tr><tr><td><code>/ps setparent [region|none]</code></td><td>protectionstones.setparent</td><td>Set the region you are in to inherit properties from another region you own [[https://worldguard.enginehub.org/en/latest/regions/priorities/]]</td></tr><tr><td><code>/ps name [name|none]</code></td><td>protectionstones.name</td><td>Nickname your region to help identify it easily.</td></tr><tr><td><code>/ps home [name/id (optional)]</code></td><td>protectionstones.home</td><td>Teleports you to one of your owned protected regions.</td></tr><tr><td><code>/ps sethome</code></td><td>protectionstones.sethome</td><td>Set the home location of an owned region.</td></tr><tr><td><code>/ps tp [id/player] [num (optional)]</code></td><td>protectionstones.tp</td><td>Teleports you to a region, or one of a given player's regions.</td></tr><tr><td><code>/ps toggle|on|off</code></td><td>protectionstones.toggle</td><td>Use this command to turn on or off placement of protection stones blocks for yourself.</td></tr><tr><td><code>/ps view</code></td><td>protectionstones.view</td><td>Use this command to view the region's boundaries.</td></tr><tr><td><code>/ps unclaim [id (optional)]</code></td><td>protectionstones.unclaim</td><td>Use this command to pickup a placed protection stone and remove the region.</td></tr><tr><td><code>/ps priority [number|null]</code></td><td>protectionstones.priority</td><td>Use this command to set your region's priority.</td></tr><tr><td><code>/ps region [list|remove|disown] [player]</code></td><td>protectionstones.region</td><td>This is an administrative command, not recommended to be given to players. Use this command to find information or edit other players' (or your own) protected regions.</td></tr><tr><td><code>/ps list [playername (optional)]</code></td><td>protectionstones.list</td><td>List the regions you or another player owns.</td></tr><tr><td><code>/ps count [playername (optional)]</code></td><td>protectionstones.count</td><td>Count the number you or another player owns.</td></tr><tr><td><code>/ps merge</code></td><td>protectionstones.merge</td><td>Merge the region you are in into a neighbouring region.</td></tr></tbody></table>

### Admin Commands

<table data-full-width="true"><thead><tr><th width="454">Command</th><th width="218" align="center">Permission</th><th>Description</th></tr></thead><tbody><tr><td><code>/ps admin help</code></td><td align="center">protectionstones.admin</td><td>Shows the help menu.</td></tr><tr><td><code>/ps admin version</code></td><td align="center">protectionstones.admin</td><td>Show the version number of the plugin.</td></tr><tr><td><code>/ps admin hide|unhide</code></td><td align="center">protectionstones.admin</td><td>Hide or unhide all of the protection stone blocks in the world you are in.</td></tr><tr><td><code>/ps admin cleanup remove [-t typealias (optional)] [days] [world (console)]</code></td><td align="center">protectionstones.admin</td><td>Removes regions where all of the owners have not joined within the past # of [days]. It is configurable in config.toml to prevent deleting the region if the members have been active.</td></tr><tr><td><code>/ps admin cleanup preview [-t typealias (optional)] [days] [world (console)]</code></td><td align="center">protectionstones.admin</td><td>Preview which regions would be deleted if the remove command was run. Dumps the region list to a file.</td></tr><tr><td><code>/ps admin flag [world] [flag] [value|null|default]</code></td><td align="center">protectionstones.admin</td><td>Set a flag for all protection stone regions in a world.</td></tr><tr><td><code>/ps admin lastlogon [player]</code></td><td align="center">protectionstones.admin</td><td>Get the last time a player logged on.</td></tr><tr><td><code>/ps admin lastlogons</code></td><td align="center">protectionstones.admin</td><td>List all of the last logons of each player.</td></tr><tr><td><code>/ps admin stats [player (optional)]</code></td><td align="center">protectionstones.admin</td><td>Show some statistics of the plugin.</td></tr><tr><td><code>/ps admin recreate</code></td><td align="center">protectionstones.admin</td><td>Recreate all PS regions using radius set in config.</td></tr><tr><td><code>/ps admin settaxautopayers</code></td><td align="center">protectionstones.admin</td><td>Add a tax autopayer for every region on the server that does not have one.</td></tr><tr><td><code>/ps admin forcemerge [world]</code></td><td align="center">protectionstones.admin</td><td>Merge overlapping PS regions together if they have the same owners, members and flags.</td></tr><tr><td><code>/ps admin changeblock [world] [fromblockalias] [toblockalias]</code></td><td align="center">protectionstones.admin</td><td>Change all of the PS blocks of a block type to a different block. Both blocks must be configured in config.</td></tr><tr><td><code>/ps admin changeregiontype [world] [fromblocktype] [toblocktype]</code></td><td align="center">protectionstones.admin</td><td>Change the internal type of all PS regions of a certain type. Useful for error correction.</td></tr><tr><td><code>/ps admin fixregions</code></td><td align="center">protectionstones.admin</td><td>Use this command to correct errors for all of the PS regions in a world. Useful if you have issues with the plugin.</td></tr><tr><td><code>/ps admin debug</code></td><td align="center">protectionstones.admin</td><td>Toggles debug mode in the plugin.</td></tr><tr><td><code>/ps reload</code></td><td align="center">protectionstones.admin</td><td>Reload the plugin with settings from the config.</td></tr></tbody></table>
