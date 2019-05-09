ProtectionStones is a grief prevention and land claiming plugin.

This plugin uses a specified type of minecraft block/blocks as a protection block. When a player placed a block of that type, they are able to protect a region around them. The size of the protected region is configurable in the plugins config file. You can also set which flags players can change and also the default flags to be set when a new region is created.

This plugin is based off the original ProtectionStones plugin by AxelDios.

The original ProtectionStones plugin (OUTDATED): http://dev.bukkit.org/bukkit-plugins/protectionstones/

## Dependencies
* ProtectionStones 2.0.4
  * WorldGuard 7.0+
  * WorldEdit 7.0+
  * Vault (Optional)
  
## Default Configuration (config.toml)

    config_version = 4
    uuidupdated = true
    # Please do not change the config version unless you know what you are doing!
    
    # ---------------------------------------------------------------------------------------
    # Protection Stones Config
    # Block configs have been moved to the blocks folder.
    # To make new blocks, copy the default "block1.toml" and make another file (ex. "block2.toml")
    # Does your config look messy? It's probably because of gradual config updates. Consider using the default configs.
    # If you need the default configs again, you can get it from here: https://github.com/espidev/ProtectionStones/tree/master/src/main/resources
    # ---------------------------------------------------------------------------------------
    
    
    # Cooldown between placing protection blocks (in seconds). -1 to disable.
    placing_cooldown = -1
    
    # Base command for protection stones (change if conflicting with other commands)
    base_command = "ps"
    
    # Aliases for the command
    aliases = [
        "pstone",
        "protectionstone",
        "protectionstones"
    ]

## Default Configuration (block1.toml)

    # Define your protection block below
    # Use block type from here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html
    type = "EMERALD_ORE"
    
    # Another way to refer to the protection stone
    # Can be used for /ps give and /ps get
    # Must be one word (no spaces)
    alias = "64"
    
    # Whether or not to restrict obtaining of the protection stone to only /ps get and /ps give.
    # Other ways to obtain this block (ex. mining) will not work as a protection stone.
    # Useful to allow the protection block to only be obtained from a shop or command.
    # Set to "false" if you want to allow players to obtain a protection stone naturally
    restrict_obtaining = true
    
    # Enable or disable the use of this protection stone in specific worlds
    # "blacklist" mode prevents this protect block from being used in the worlds in "worlds"
    # "whitelist" mode allows this protect block to only be used in the worlds in "worlds"
    # Can override with protectionstones.admin permission
    world_list_type = "blacklist"
    worlds = [
        "exampleworld1",
        "exampleworld2"
    ]
    
    [region]
        # Protection radius of block
        # Set y_radius to -1 if you want it to protect from sky to bedrock. If this doesn't appear to work set it to 256.
        x_radius = 64
        y_radius = -1
        z_radius = 64
    
        # How many blocks to offset the default location of /ps home from the protection block
        home_x_offset = 0
        home_y_offset = 1
        home_z_offset = 0
    
        # Specify the default flags to be set when a new protected region is created.
        flags = [
            "pvp deny",
            "greeting &lEntering &b&l%player%'s &r&lprotected area",
            "farewell &lLeaving &b&l%player%'s &r&lprotected area",
            "creeper-explosion deny",
        ]
    
        # List all the flags that can be set by region owners. Separate them with a comma, no space.
        allowed_flags = [
            "use",
            "pvp",
            "greeting",
            "farewell",
            "mob-spawning",
            "creeper-explosion",
        ]
    
        # Default priority type for this block type protection stone
        priority = 0
    
    [block_data]
        # Name given to protection block when obtained with /ps give or /ps get
        # Leave as '' for no name
        display_name = "&a&m<---&r&b 64x64 Protection Stone &r&a&m--->"
    
        # Lore given to protection block when obtained with /ps give or /ps get
        # Leave as [] for no lore
        lore = [
            "&6(⌐■_■)ノ♪ Nobody's going to touch my stuff!",
        ]
    
        # Add price when using /ps get
        # Must have compatible economy plugin (requires Vault, ie. Essentials)
        price = 0.0
    
    [behaviour]
        # Hide protection stone right away when placed?
        auto_hide = false
    
        # Disable returning the block when removed/unclaimed?
        no_drop = false
    
        # Prevents piston pushing of the block. Recommended to keep as true.
        prevent_piston_push = true
    
        # Silk Touch: if true, ore-blocks that are also configured by ProtectionStones will disallow Silk Touch drops
        # This was the old behaviour to prevent natural obtaining of the protection stone.
        # Recommended to keep false if "Restrict Obtaining" (the new way) is true
        prevent_silk_touch = false
    
    [player]
        # Whether or not to prevent teleporting into a protected region if the player doesn't own it (except with ender pearl and chorus fruit)
        # Bypass with protectionstones.tp.bypasstp
        prevent_teleport_in = false
    
        # Can't move for x seconds before teleporting with /ps home or /ps tp. Can be disabled with 0.
        # Option to teleport only if player stands still.
        # Can override with permission protectionstones.tp.bypasswait
        no_moving_when_tp_waiting = true
        tp_waiting_seconds = 0
    
        # Extra permission required to place this specific protection block (you still need protectionstones.create)
        # '' for no extra permission
        permission = ''



## Commands
Aliases in case of command conflicts: /ps, /protectionstone, /protectionstones, /pstone

    /ps get [block] - Get a protection stone. Can be charged with currency set in the config (requires Vault)
    /ps give [block] [player] - Give a protection stone to another player as admin (free).
    /ps info members|owners|flags - Use this command inside a ps region to see more information about it.
    /ps add|remove [playername] - Use this command to add or remove a member of your protected region.
    /ps addowner|removeowner [playername] - Use this command to add or remove an owner of your protected region.
    /ps flag [flagname] [setting|default] - Use this command to set a flag in your protected region.
    /ps hide|unhide - Use this command to hide or unhide your protectionstones block.
    /ps home [num] - Teleports you to one of your protected regions.
    /ps sethome - Set the home location of an owned region.
    /ps tp [player] [num] - Teleports you to one of a given player's regions.
    /ps toggle - Use this command to turn on or off placement of protection stones blocks.
    /ps view - Use this command to view the borders of a protected region.
    /ps unclaim - Use this command to pickup a placed protection stone and remove the region.
    /ps priority [number|null] - Use this command to set your region's priority.
    /ps region count|list|remove|disown [playername] - Use this command to find
    information or edit other players' (or your own) protected regions.
    /ps count [playername (optional)] - Count the number of regions you own or another player.
    /ps admin [version|settings|hide|unhide|cleanup|lastlogon|lastlogons|stats|fixregions] - This is an admin command showing different stats and allowing to override other player's regions.
    /ps reload - Reload settings from the config.

## Permissions

    protectionstones.create - Protect a region by placing a ProtectionStones block.
    protectionstones.destroy - Allow players to remove their own protected regions (block break).
    protectionstones.unclaim - Allow players to unclaim their region using /ps unclaim.
    protectionstones.get - Allows players the use of /ps get.
    protectionstones.view - Allows players the use of /ps view.
    protectionstones.info - Allows players the use of /ps info.
    protectionstones.count - Allows players the use of /ps count.
    protectionstones.count.others - Allows players the use of /ps count [player].
    protectionstones.hide - Allow players to hide their ProtectionStones block.
    protectionstones.unhide - Allow players to unhide their ProtectionStones block.
    protectionstones.home - Access to the /ps home command.
    protectionstones.sethome - Access to /ps sethome.
    protectionstones.tp - Access to /ps tp command.
    protectionstones.tp.bypasswait - Bypass the wait time set in the config for /ps home and /ps tp
    protectionstones.tp.bypassprevent - Bypass prevent_teleport_in option in config
    protectionstones.priority - Allows players to set their region's priority.
    protectionstones.owners - Allows players to add or remove region owners. Allows players to use /ps info owners command.
    protectionstones.members - Allows players to add or remove region members. Allows players to use /ps info members command.
    protectionstones.flags - Allows players to set their region flags.
    protectionstones.toggle - Allows players to toggle ProtectionStones placement.
    protectionstones.region - Allows players to use the /ps region commands.
    protectionstones.give - Allows players the use of /ps give (give protectionstones to others as admin).
    protectionstones.admin - This permission allows users to override all ProtectionStones regions and use /ps admin and /ps reload.
    protectionstones.limit.x - Replace x with a limit for players' protected regions.
    If you don't want a limit, do not give this permission. x can only be replaced with an integer number.
    protectionstones.superowner - Allows players to override region permissions.

This plugin is licensed under the **Apache 2.0 License**.
