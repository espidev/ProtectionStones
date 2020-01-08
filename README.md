# ProtectionStones
[![Maven Central](https://img.shields.io/maven-central/v/dev.espi/protectionstones.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22dev.espi%22%20AND%20a:%22protectionstones%22)

This project was originally based off of https://github.com/vik1395/ProtectionStones-Minecraft.

ProtectionStones is a grief prevention and land claiming plugin.

This plugin uses a specified type of minecraft block/blocks as a protection block. When a player placed a block of that type, they are able to protect a region around them. The size of the protected region is configurable in the plugins config file. You can also set which flags players can change and also the default flags to be set when a new region is created.

This plugin is based off the original ProtectionStones plugin by AxelDios.

The current Spigot page: https://www.spigotmc.org/resources/protectionstones-updated-for-1-13-2-1-14-wg7.61797/

The original ProtectionStones plugin (OUTDATED): http://dev.bukkit.org/bukkit-plugins/protectionstones/

## Dependencies
* ProtectionStones 2.6.4
  * WorldGuard 7.0+
  * WorldEdit 7.0+
  * Vault (Optional)
  
## Default Configuration (config.toml)

    # Please do not change the config version unless you know what you are doing!
    config_version = 12
    uuidupdated = true
    
    # ---------------------------------------------------------------------------------------
    # Protection Stones Config
    # Block configs have been moved to the blocks folder.
    # To make new blocks, copy the default "block1.toml" and make another file (ex. "block2.toml")
    # Does your config look messy? It's probably because of gradual config updates. Consider using the default configs.
    # If you need the default configs again, you can get it from here: https://github.com/espidev/ProtectionStones/tree/master/src/main/resources
    # ---------------------------------------------------------------------------------------
    
    # Cooldown between placing protection blocks (in seconds). -1 to disable.
    placing_cooldown = -1
    
    # Set to true to not block server startup for loading the UUID cache.
    # /ps add and /ps remove will not work for offline players until the cache is finished loading.
    async_load_uuid_cache = false
    
    # Whether or not to allow regions to have identical names (from /ps name).
    # If this is set to true, players will have to use numbers after the name if they encounter duplicates.
    allow_duplicate_region_names = false
    
    # Time in seconds between /ps view attempts.
    # Can prevent lag from spamming the command.
    ps_view_cooldown = 10
    
    # Base command for protection stones (change if conflicting with other commands)
    base_command = "ps"
    
    # Aliases for the command
    aliases = [
        "pstone",
        "protectionstone",
        "protectionstones"
    ]
    
    # Whether or not to drop items on the ground if the inventory is full (ex. during /ps unclaim)
    # If set to false, the event will be prevented from happening, and say that inventory is full
    drop_item_when_inventory_full = true
    
    # Whether or not regions placed have to be either next to or overlapping existing regions the player already owns.
    # This can make the world cleaner and have less scattered regions.
    # Set the number of regions of non-adjacent regions with the permission protectionstones.region.adjacent.x (default is 1, -1 to bypass)
    # Also can bypass with protectionstones.admin
    regions_must_be_adjacent = false
    
    # Whether or not to give players the option to merge new regions with ones they already own (overlapping)
    # to create a new large region. Can merge any regions with protectionstones.admin
    # Requires the permission protectionstones.merge to use (with /ps merge)
    # NOTE: Due to the limitations of WorldGuard, merged regions will ignore y_radius and go from bedrock to sky
    # since polygon regions can only be 2D, not 3D
    allow_merging_regions = true
    
    # Whether or not to allow merged regions to have holes in them (merging a bunch of regions in a circle with the inside not protected).
    # This is only checked during the merge process, it will not unmerge regions with holes already.
    allow_merging_holes = true
    
    [economy]
        # Set limits on the price for renting. Set to -1 to disable.
        max_rent_price = -1.0
        min_rent_price = 1.0
    
        # Set limits on the period between rent payments, in seconds (86400 seconds = 1 day). Set to -1 to disable.
        max_rent_period = -1
        min_rent_period = 1
    
        # Set taxes on regions. (THIS FEATURE IS STILL BEING DEVELOPED, ONLY USE FOR TESTING!)
        # Taxes are configured in each individual block config.
        # Whether or not to enable the tax command.
        tax_enabled = false
    
        # Notify players of outstanding tax payments for the regions they own.
        tax_message_on_join = true

## Default Configuration (block1.toml)

    # Define your protection block below
    # Use block type from here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html
    # --------------------------------------------------------------------------------------------------
    # If you want to use player heads, you can use "PLAYER_HEAD:player_name" (ex. "PLAYER_HEAD:Notch")
    # To use custom player heads, you need the base64 value of the head. On minecraft-heads.com, you will find this value in the Other section under "Value:".
    # To use UUIDs for player heads, go to https://sessionserver.mojang.com/session/minecraft/profile/PUT-UUID-HERE and copy the value from the "value" field not including quotes.
    # When you have the value, you can set the type to "PLAYER_HEAD:value"
    type = "EMERALD_ORE"
    
    # Another way to refer to the protection stone
    # Can be used for /ps give and /ps get
    # Must be one word (no spaces)
    alias = "64"
    
    # Whether or not to restrict obtaining of the protection stone to only /ps get and /ps give and custom crafting recipes.
    # Other ways to obtain this block (ex. mining) will not work as a protection stone.
    # Useful to allow the protection block to only be obtained from a shop or command.
    # Set to "false" if you want to allow players to obtain a protection stone naturally
    restrict_obtaining = true
    
    # Enable or disable the use of this protection stone in specific worlds
    # "blacklist" mode prevents this protect block from being used in the worlds in "worlds"
    # "whitelist" mode allows this protect block to only be used in the worlds in "worlds"
    # Can be overriden with protectionstones.admin permission (including OP)!
    world_list_type = "blacklist"
    worlds = [
        "exampleworld1",
        "exampleworld2"
    ]
    
    # Whether or not to actually restrict the protection stone from being placed when the world is restricted (in blacklist/whitelist)
    # The block will place normally, without PS behaviour.
    prevent_block_place_in_restricted_world = true
    
    [region]
        # Minimum distance between claims (that aren't owned by the same owner), measured from the protection block to the edge of another region
        # You will probably have to change this between blocks, since the region sizes will be different
        # Set to -1 for no minimum, but will still check for overlapping regions
        distance_between_claims = -1
    
        # Protection radius of block
        # Set y_radius to -1 if you want it to protect from sky to bedrock. If this doesn't appear to work set it to 256.
        # Turn "allow_merging_regions" in config.toml to false if editing the y_radius to not be -1
        x_radius = 64
        y_radius = -1
        z_radius = 64
    
        # Offset the protection block
        # If you would like to make the protection block not be at the center of new regions, you can offset it here
        # ex. x_offset = 64, y_offset = 0, z_offset = 64 would make it at the corner of a created region
        x_offset = 0
        y_offset = 0
        z_offset = 0
    
        # How many blocks to offset the default location of /ps home from the protection block
        home_x_offset = 0.0
        home_y_offset = 1.0
        home_z_offset = 0.0
    
        # Specify the default flags to be set when a new protected region is created.
        # Can use -g [group] before the flag to set group flags (ex. -g members pvp deny).
        # Can use PlaceholderAPI placeholders in string flags (ex. greeting, farewell).
        flags = [
            "pvp deny",
            "tnt deny",
            "greeting &lEntering &b&l%player%'s &f&lprotected area",
            "farewell &lLeaving &b&l%player%'s &f&lprotected area",
            "greeting-action &lEntering &b&l%player%'s &f&lprotected area",
            "farewell-action &lLeaving &b&l%player%'s &f&lprotected area",
            "creeper-explosion deny",
        ]
    
        # List all the flags that can be set by region owners.
        allowed_flags = [
            "pvp",
            "greeting",
            "greeting-title",
            "greeting-action",
            "farewell",
            "farewell-title",
            "farewell-action",
            "mob-spawning",
            "creeper-explosion",
        ]
    
        # Which flags to hide from /ps info
        hidden_flags_from_info = [
            "ps-merged-regions",
            "ps-merged-regions-types",
            "ps-block-material",
            "ps-price",
            "ps-landlord",
            "ps-tenant",
            "ps-rent-period",
            "ps-rent-last-paid",
            "ps-for-sale",
            "ps-rent-settings",
            "ps-tax-payments-due",
            "ps-tax-last-payment-added",
            "ps-tax-autopayer"
        ]
    
        # Default priority type for this block type protection stone
        priority = 0
    
        # Whether or not to allow creation of regions that overlap other regions you don't own
        allow_overlap_unowned_regions = false
    
        # Whether or not to allow this regions created with this block to merge with other regions
        # allow_merging_regions must be set to true in config.toml
        allow_merging = true
    
    [block_data]
        # Name given to protection block when obtained with /ps give or /ps get
        # Also affects custom crafted items (see custom_recipe)
        # Leave as '' for no name
        display_name = "&a&m<---&r&b 64x64 Protection Stone &r&a&m--->"
    
        # Lore given to protection block when obtained with /ps give or /ps get
        # Also affects custom crafted items (see custom_recipe)
        # Leave as [] for no lore
        lore = [
            "&6(⌐■_■)ノ♪ Nobody's going to touch my stuff!",
        ]
    
        # Add price when using /ps get
        # Must have compatible economy plugin (requires Vault, ie. Essentials)
        price = 0.0
    
        # Whether or not to allow crafting this item using a custom recipe
        # Useful to allow crafting the item when restrict_obtaining is set to true
        allow_craft_with_custom_recipe = false
        # Specify the custom crafting recipe below
        # You must fill the item spots with names from here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html
        # If you want air, you can just leave the spot as ""
        custom_recipe = [
            ["", "STONE", ""],
            ["STONE", "EMERALD", "STONE"],
            ["", "STONE", ""]
        ]
        # Amount of the protection item to give when crafted
        recipe_amount = 1
    
    [economy]
        # Taxes must be enabled in config.toml first (tax-enabled)
        # The amount to tax the region per tax cycle.
        tax_amount = 0.0
    
        # The amount of seconds between tax cycles. Set to -1 to disable taxes.
        tax_period = -1
    
        # Amount of time to pay taxes in seconds after tax cycle before there is punishment.
        tax_payment_time = 86400
    
        # Automatically set the player that created the region as the taxpayer.
        start_with_tax_autopay = true
    
    [behaviour]
        # Hide protection stone right away when placed?
        auto_hide = false
    
        # Whether or not to automatically merge into other regions when placed if there is only one overlapping and allow_merging is true
        auto_merge = false
    
        # Disable returning the block when removed/unclaimed?
        no_drop = false
    
        # Prevents piston pushing of the block. Recommended to keep as true.
        prevent_piston_push = true
    
        # Prevents the block from being destroyed when exploded.
        # Recommended to keep true to prevent players from exploiting more protection stones with /ps unhide (when the block is destroyed)
        prevent_explode = true
    
        # Destroys the protection stone region when block is exploded. Can be useful for PVP/Factions servers.
        # prevent_explode must be false for this to work.
        destroy_region_when_explode = false
    
        # Silk Touch: if true, ore-blocks that are also configured by ProtectionStones will disallow Silk Touch drops
        # This was the old behaviour to prevent natural obtaining of the protection stone.
        # Recommended to keep false if "Restrict Obtaining" (the new way) is true
        prevent_silk_touch = false
    
        # Set cost for when a protection block is placed (separate from /ps get cost)
        cost_to_place = 0.0
    
    [player]
        # Whether or not to allow breaking the protection block with a shift-right click
        # Useful if the protection block is unbreakable (bedrock, command block), etc.
        allow_shift_right_break = false
    
        # Whether or not to prevent teleporting into a protected region if the player doesn't own it (except with ender pearl and chorus fruit)
        # Does not prevent entry, use the flag "entry deny" for preventing entry.
        # Bypass with protectionstones.tp.bypasstp
        prevent_teleport_in = false
    
        # Can't move for x seconds before teleporting with /ps home or /ps tp. Can be disabled with 0.
        # Option to teleport only if player stands still.
        # Can override with permission protectionstones.tp.bypasswait
        no_moving_when_tp_waiting = true
        tp_waiting_seconds = 0
    
        # Whether or not to prevent obtaining this block through /ps get.
        # Ignored with protectionstones.admin
        prevent_ps_get = false
    
        # Extra permission required to place this specific protection block (you still need protectionstones.create)
        # Also applies to /ps get (you still need protectionstones.get)
        # '' for no extra permission
        permission = ''
    
    [event]
    
        # Events section
        # ~~~~~~~~~~~~~~
        # For each line on events, it is the format 'type: action'
        # The following are accepted types:
        # player_command - Execute command by player that caused event (won't execute if not applicable)
        # console_command - Execute command by console
        # message - Send message to player or console if applicable (colour support with &)
        # global_message - Send message to all players and console (colour support with &)
    
        # ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        # Whether or not to enable event tracking (API events will still be enabled)
        enable = false
    
        # Execute commands when a region is created (ex. player place protection block)
        on_region_create = [
            'global_message: &l%player% created the region %region%!',
        ]
    
        # Execute commands when a region is destroyed (ex. when player destroy protection block)
        on_region_destroy = [
            'console_command: say %player% has destroyed region %region%!',
        ]


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

This plugin is licensed under the **Apache 2.0 License**.