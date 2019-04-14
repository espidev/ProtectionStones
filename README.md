ProtectionStones is a grief prevention and land claiming plugin.

This plugin uses a specified type of minecraft block/blocks as a protection block. When a player placed a block of that type, they are able to protect a region around them. The size of the protected region is configurable in the plugins config file. You can also set which flags players can change and also the default flags to be set when a new region is created.

This plugin is based off the original ProtectionStones plugin by AxelDios.

The original ProtectionStones plugin (OUTDATED): http://dev.bukkit.org/bukkit-plugins/protectionstones/

**Dependencies**
-------------
* ProtectionStones 1.7.0
  * WorldGuard 7.0+
  * WorldEdit 7.0+
  
**Configuration**
-------------

    ConfVer: 1
    UUIDUpdated: true
    
    # Protection Stones Configuration Page
    
    # Please do not edit the ConfVer number unless told to do so in update message on spigotmc.org
    # Also, do not change UUIDUpdated to false unless you want the plugin to upgrade old protection stone regions from names to UUIDs
    
    # Specify the block you want to use to protect regions. Use names from https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html
    # You can specify multiple block types, divided by comma's (NO SPACES!)
    # Be sure to also add the block to the Region section below!
    Blocks: END_STONE
    # If you define multiple block types be sure to define their specs below
    
    # Specify the default flags to be set when a new protected region is created..
    Flags:
      - pvp deny
      - greeting &lEntering &b&l%player%'s &r&lprotected area
      - farewell &lLeaving &b&l%player%'s &r&lprotected area
    
    # List all the flags that can be set by region owners. Separate them with a comma, no space.
    Allowed Flags: use,pvp,greeting,farewell,mob-spawning
    
    # Disable the use of ALL protection stones in certain worlds.
    # Can be overriden with the permission protectionstones.admin
    Worlds Denied:
        - exampleworld1
        - exampleworld2
    
    # Protected Region Configuration, defined per block type (refer to list defined above)
    Region:
      # Default block type
      END_STONE:
        X Radius: 20
        # Set Y to -1 if you want it to protect from sky to bedrock. If this doesn't appear to work set it to 256.
        Y Radius: -1
        Z Radius: 20
        # Hide pstone right away when placed?
        Auto Hide: false
        # Disable returning the block when the pstone is removed/unclaimed?
        No Drop: false
        # Block Piston prevents pushing of pstones if true; recommend to set to true if "No Drop" is false, as it can be abused to gain more pstones.
        Block Piston: true
        # Silk Touch: if true, ore-blocks that are also configured by ProtectionStones will disallow Silk Touch drops
        Silk Touch: false
        # Default priority type for this block type pstone
        Priority: 0
    
    # Section for blocking/showing warning when people enter PVP flagged PStones
    Teleport to PVP:
        # Display warning if they walk into PVP flagged PStone
        Display Warning: false
        # Block teleport if they tp to PVP flagged PStone (can bypass with /ps bypass)
        Block Teleport: false
    
    cooldown:
      enable: false
      cooldown: 10


Commands
Aliases in case of command conflicts: /ps, /protectionstone, /protectionstones, /pstone

    /ps info members|owners|flags - Use this command inside a ps region to see more information about it.
    /ps add|remove {playername} - Use this command to add or remove a member of your protected region.
    /ps addowner|removeowner {playername} - Use this command to add or remove an owner of your protected region.
    /ps flag {flagname} {setting|null} - Use this command to set a flag in your protected region.
    /ps hide|unhide - Use this command to hide or unhide your protectionstones block.
    /ps home {num} - Teleports you to one of your protected regions.
    /ps tp {player} {num} - Teleports you to one of a given player's regions.
    /ps toggle - Use this command to turn on or off placement of protection stones blocks.
    /ps view - Use this command to view the borders of a protected region.
    /ps unclaim - Use this command to pickup a placed protection stone and remove the region.
    /ps priority {number|null} - Use this command to set your region's priority.
    /ps region count|list|remove|disown {playername} - Use this command to find
    information or edit other players' (or your own) protected regions.
    /ps count [playername (optional)] - Count the number of regions you own or another player.
    /ps admin {version|settings|hide|unhide|cleanup|lastlogon|lastlogons|stats} - This is an admin command showing different stats and allowing to override other player's regions.
    /ps reload - Reload settings from the config.

**Permissions**
-----------

    protectionstones.create - Protect a region by placing a ProtectionStones block.
    protectionstones.destroy - Allow players to remove their own protected regions (block break).
    protectionstones.unclaim - Allow players to unclaim their region using /ps unclaim.
    protectionstones.view - Allows players the use of /ps view.
    protectionstones.info - Allows players the use of /ps info.
    protectionstones.count - Allows players the use of /ps count.
    protectionstones.count.others - Allows players the use of /ps count [player].
    protectionstones.hide - Allow players to hide their ProtectionStones block.
    protectionstones.unhide - Allow players to unhide their ProtectionStones block.
    protectionstones.home - Access to the /ps home command.
    protectionstones.tp - Access to /ps tp command.
    protectionstones.priority - Allows players to set their region's priority.
    protectionstones.owners - Allows players to add or remove region owners. Allows players to use /ps info owners command.
    protectionstones.members - Allows players to add or remove region members. Allows players to use /ps info members command.
    protectionstones.flags - Allows players to set their region flags.
    protectionstones.toggle - Allows players to toggle ProtectionStones placement.
    protectionstones.region - Allows players to use the /ps region commands.
    protectionstones.admin - This permission allows users to override all ProtectionStones regions and use /ps admin and /ps reload.
    protectionstones.limit.x - Replace x with a limit for players' protected regions.
    If you don't want a limit, do not give this permission. x can only be replaced with an integer number.
    protectionstones.bypass - Access to the /ps bypass command.
    protectionstones.superowner - Allows players to override region permissions.

This plugin is licensed under the **Apache 2.0 License**.
