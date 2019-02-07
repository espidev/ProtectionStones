An updated source from the original developer which can be found here .

ProtectionStonesx is based off the original ProtectionStones plugin at Bukkit by AxelDios and was recoded by vik1395,Dragoboss. It works very similar to the original plugin and most of the code has been derived from it. The main reason I have made this is because the plugin is dead and and has been very glitchy in 1.8, especially with the release on worldguard and worldedit 6.0.

This plugin uses a specified type of minecraft block/blocks as a protection block. When a player placed a block of that type, they are able to protect a region around them. The size of the protected region is configurable in the plugins config file. You can also set which flags players can change and also the default flags to be set when a new region is created.

The original ProtectionStones plugin: http://dev.bukkit.org/bukkit-plugins/protectionstones/

**Dependencies**
-------------
* ProtectionStones 2.0.0
  * WorldGuard 7.0+
  * WorldEdit 7.0+
  
**Configuration**
-------------

    ConfVer: 1
    #Protection Stones Configuration Page
    #Protection Stones for MC 1.10+ is brought to you by Dragoboss and Jerzean
    #Please do not edit the ConfVer number unless told to do so in update message on spigotmc.org
    
    #Specify the block you want to use to protect regions. Use names from https://goo.gl/EBM8w5
    #You can specify multiple block types, divided by comma's (NO SPACES!)
    #If you wish to have sub-block-types as pstones, you can use -#. # is the number of
    #subtype similar to the number you'd use in a /give command. I.E. stone:2 --> STONE-2
    Blocks: ENDER_STONE
    #If you define multiple block types be sure to define their specs below
    
    #Specify the default flags to be set when a new protected region is created..
    Flags:
      - use deny
      - pvp deny
      - greeting Entering %player%'s protected area
      - farewell Leaving %player%'s protected area
    
    #List all the flags that can be set by region owners. Separate them with a comma, no space.
    Allowed Flags: use,pvp,greeting,farewell,mob-spawning
    
    #Toggle UUID support for protected regions.
    #NOTE: This does NOT convert existing PStones to UUID Format!!
    UUID: true
    
    #Disable the use of pStones in certain worlds.
    Worlds Denied:
        - exampleworld1
        - exampleworld2
    
    #Protected Region Configuration, defined per block type (refer to list defined above)
    Region:
      #Default block type
      ENDER_STONE:
        X Radius: 20
        #Set Y to 0 if you want it to protect from sky to bedrock.
        Y Radius: 0
        Z Radius: 20
        #Hide pstone right away when placed?
        Auto Hide: false
        #Disable returning the block when the pstone is removed/reclaimed?
        No Drop: false
        #Silk Touch: if true, ore-blocks that are also configured by ProtectionStones will disallow Silk Touch drops
        Silk Touch: false
        #Block Piston pushing of pstones by default; recommend to set to true if "No Drop" is false, as it can be abused to gain more pstones.
        Block Piston: true
        #Default priority type for this block type pstone
        Priority: 0
      #STONE-1:    # the "-1" part would mean 'Granite' to be used as pstone, but not regular stone
        #X Radius: 10
        #Y Radius: 10
        #Z Radius: 10
        #Auto Hide: false
        #No Drop: false
        #Block Piston: true
        #Silk Touch: false
        #Priority: 
    # Section for blocking/showing warning when people enter PVP flagged PStones
    Teleport to PVP:
      #Display warning if they walk into PVP flagged PStone
      Display Warning: false
      #Block teleport if they tp to PVP flagged PStone (can bypass with /ps bypass)
      Block Teleport: false

Commands
Aliases in case of command conflicts: /ps, /protectionstone, /protectionstones, /pstone

    /ps info members|owners|flags - Use this command inside a ps region to see more information about it.
    /ps add|remove {playername} - Use this command to add or remove a member of your protected region.
    /ps addowner|removeowner {playername} - Use this command to add or remove an owner of your protected region.
    /ps flag {flagname} {setting|null} - Use this command to set a flag in your protected region.
    /ps hide|unhide - Use this command to hide or unhide your protectionstones block.
    /ps home {num} - Teleports you to one of your protected regions.
    /ps tp {player} {num} - Teleports you to one of a given player's regions.
    /ps toggle - Use this command to turn on or off ProtectionStones blocks.
    /ps view - Use this command to view the borders of your protected region.
    /ps reclaim - Use this command to pickup a placed ProtectionStone and remove the region.
    /ps priority {number|null} - Use this command to set your region's priority.
    /ps region count|list|remove|regen|disown {playername} - Use this command to find
    information or edit other players' (or your own) protected regions.
    /ps admin {version|settings|hide|unhide|cleanup|lastlogon|lastlogons|stats} - This is an admin command showing different stats and allowing to override other player's regions.
    /ps bypass
    

**Permissions**
-----------

The permissions are very similar to the old plugin.

    protectionstones.create - Protect a region by placing a ProtectionStones block.
    protectionstones.destroy - Allow players to remove their own protected regions.
    protectionstones.reclaim - Allow players to reclaim their stones and remove their regions.
    protectionstones.view - Allows players the use of /ps view command.
    protectionstones.info - Allows players use of /ps info command.
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
    protectionstones.admin - This permission allows users to override all ProtectionStones regions.
    protectionstones.limit.x - Replace x with a limit for players' protected regions.
    If you don't want a limit, do not give this permission. x can only be replaced with an integer number.
    protectionstones.bypass - Acces to the /ps bypass command.

This plugin is licensed under **CC Attribution-NonCommercial-ShareAlike 4.0 International**. In very basic terms, Do whatever you want with the code of this plugin, as long as you give credits to the author and/or the plugin itself.
