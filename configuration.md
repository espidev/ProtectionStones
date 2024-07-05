# Configuration

Listed below are the default configuration files, with comments.

<details>

<summary>Default Configuration (config.toml)</summary>

```toml
# Please do not change the config version unless you know what you are doing!
config_version = 16
uuidupdated = true
region_negative_min_max_updated = true

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
ps_view_cooldown = 3

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
# Set the number of regions of non-adjacent regions with the permission protectionstones.adjacent.x (default is 1, -1 to bypass)
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

# Whether when players join, by default they have protection block placement toggled off (equivalent to running /ps toggle)
default_protection_block_placement_off = false

# If you do not have LuckPerms, ProtectionStones is unable to determine the limits of offline players (since it depends
# on permissions), and so it requires players to be online. Set this to true if your server does not need limits (and so
# the check is unnecessary).
allow_addowner_for_offline_players_without_lp = false

# Whether or not members of a region can /ps home to the region.
allow_home_teleport_for_members = true

[admin]
    # Whether /ps admin cleanup remove should delete regions that have members, but don't have owners (after inactive
    # owners are removed).
    # Regions that have no owners or members will be deleted regardless.
    cleanup_delete_regions_with_members_but_no_owners = true

[economy]
    # Set limits on the price for renting. Set to -1.0 to disable.
    max_rent_price = -1.0
    min_rent_price = 1.0

    # Set limits on the period between rent payments, in seconds (86400 seconds = 1 day). Set to -1 to disable.
    max_rent_period = -1
    min_rent_period = 1

    # Set taxes on regions.
    # Taxes are configured in each individual block config.
    # Whether or not to enable the tax command.
    # If you already have regions, you may want to set each one to have an autopayer (player that automatically pays taxes).
    # This can be done with /ps admin settaxautopayers, which updates every region on the server with an autopayer from their owners list.
    tax_enabled = false

    # Notify players of outstanding tax payments for the regions they own.
    tax_message_on_join = true

```

</details>

<details>

<summary>Default Block Configuration (block1.toml)</summary>

```toml
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

# Description of the protection block type
# Shows up in /ps get menu
description = "64 block radius protection zone."

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

# Whether or not to allow the block to be placed in the wild.
# If set to false, the protection block can only be placed in existing regions.
allow_placing_in_wild = true

# Whether or not to allow the block to bypass the WorldGuard passthrough flag.
# This allows the protection block to be placed even if WorldGuard prevents block placing in the wild.
placing_bypasses_wg_passthrough = true

[region]
    # Minimum distance between claims (that aren't owned by the same owner), measured from the protection block to the edge of another region
    # You will probably have to change this between blocks, since the region sizes will be different
    # Set to -1 for no minimum, but will still check for overlapping regions
    distance_between_claims = -1

    # Protection radius of block (radius of 64 -> 129 x 129 region)
    # Set y_radius to -1 if you want it to protect for all y levels.
    # y_radius must be -1 if you are allowing the region to be merged ("allow_merging" option)
    x_radius = 64
    y_radius = -1
    z_radius = 64

    # Enables "chunk snapping mode", where the region boundaries will be determined by the chunk the block is in, and the
    # chunk_radius (how many chunks away from the center chunk).
    # Allows players to not have to worry about the exact placement of their block, and removes the issue of messy overlapping
    # regions (as they all conform to chunk boundaries).
    # Set to -1 to disable, and any number larger than or equal to 1 to enable.
    # Note: If enabled, x_radius and z_radius will be ignored!
    chunk_radius = -1

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
        "wither-damage deny",
        "ghast-fireball deny",
    ]

    # List all the flags that can be set by region owners.
    # If you want to whitelist the groups that can be set as well, use -g (ex. "-g all,members pvp" restricts it to no group flag, and members group)
    # "-g all pvp" - Prevents players setting the group to nonmembers, and being invulnerable to attacks.
    allowed_flags = [
        "-g all pvp",
        "greeting",
        "greeting-title",
        "greeting-action",
        "farewell",
        "farewell-title",
        "farewell-action",
        "mob-spawning",
        "creeper-explosion",
        "wither-damage",
        "ghast-fireball",
    ]

    # Which flags to hide from /ps info
    hidden_flags_from_info = [
        "ps-name",
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
    # This is dangerous, so think about this carefully if you set it to true.
    allow_overlap_unowned_regions = false

    # Whether or not to allow players to create other regions that overlap this region.
    # "owner" - only allow owners to overlap this (default)
    # "member" - allow members and owners to overlap this region type. (useful for city plots)
    # "all" - allow all players to overlap this region type.
    # "none" - no players, not even the owners of the region can overlap it
    # allow_overlap_unowned_regions does not need to be true for this to work.
    allow_other_regions_to_overlap = "owner"

    # Whether or not to allow this regions created with this block to merge with other regions
    # allow_merging_regions must be set to true in config.toml
    allow_merging = true

    # Allowed types of regions to merge into (referred to by alias)
    # Be sure to add the alias of this current region type to allow merging with it ex. ["64"]
    # Add "all" if you want to allow this region to merge into any region
    allowed_merging_into_types = [
        "all"
    ]

[block_data]
    # Name given to protection block when obtained with /ps give or /ps get
    # Also affects custom crafted items (see custom_recipe)
    # Leave as '' for no name
    display_name = "&a&m<---&r&b 64 Radius Protection Block &r&a&m--->"

    # Lore given to protection block when obtained with /ps give or /ps get
    # Also affects custom crafted items (see custom_recipe)
    # Leave as [] for no lore
    lore = [
        "&6(⌐■_■)ノ♪ Nobody's going to touch my stuff!",
    ]

    # Whether the item should have an "glow/enchant" effect look when in a player's inventory.
    enchanted_effect = false

    # Add price when using /ps get
    # Must have compatible economy plugin (requires Vault, ie. Essentials)
    # Must be a decimal (ex. not 10, but 10.0)
    price = 0.0

    # Whether or not to allow crafting this item using a custom recipe
    # Useful to allow crafting the item when restrict_obtaining is set to true
    allow_craft_with_custom_recipe = false
    # Specify the custom crafting recipe below
    # You must fill the item spots with names from here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html
    # You can also use other protection stone items as ingredients in the recipe, in the format PROTECTION_STONES:alias
    # Make sure that you set allow_use_in_crafting for that block to true, or else you can't use it in crafting
    # If you want air, you can just leave the spot as ""
    custom_recipe = [
        ["", "STONE", ""],
        ["STONE", "EMERALD", "STONE"],
        ["", "STONE", ""]
    ]
    # Amount of the protection item to give when crafted
    recipe_amount = 1

    # The custom model data of the block item, useful for resource packs. Set to -1 to disable.
    custom_model_data = -1

[economy]
    # Taxes must be enabled in config.toml first (tax_enabled)
    # The amount to tax the region per tax cycle.
    # Must be a decimal (ex. not 10, but 10.0)
    tax_amount = 0.0

    # The amount of seconds between tax cycles. Set to -1 to disable taxes.
    tax_period = -1

    # Amount of time to pay taxes in seconds after tax cycle before there is punishment.
    tax_payment_time = 86400

    # Automatically set the player that created the region as the taxpayer.
    start_with_tax_autopay = true

    # What role tenants should be added as (for rents). It can either be "owner" or "member".
    tenant_rent_role = "member"

    # Should the landlords of rented out regions still be an owner while it is rented out?
    landlord_still_owner = false

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

    # Allow protect block item to be smelt in furnaces
    allow_smelt_item = false

    # Allows the protection block to be used in crafting recipes
    # You may want it set to false to prevent players decomposing its elements
    allow_use_in_crafting = false

[player]
    # Whether or not to allow breaking the protection block with a shift-right click
    # Useful if the protection block is unbreakable (bedrock, command block), etc.
    allow_shift_right_break = false

    # Whether or not to prevent teleporting into a protected region if the player doesn't own it (except with ender pearl and chorus fruit)
    # Does not prevent entry, use the flag "entry deny" for preventing entry.
    # Bypass with protectionstones.tp.bypassprevent
    prevent_teleport_in = false

    # Can't move for x seconds before teleporting with /ps home or /ps tp. Can be disabled with 0.
    # Option to teleport only if player stands still.
    # Can override with permission protectionstones.tp.bypasswait
    no_moving_when_tp_waiting = true
    tp_waiting_seconds = 0

    # Whether or not to prevent obtaining this block through /ps get.
    # Ignored with protectionstones.admin
    prevent_ps_get = false

    # Whether or not to prevent this region type from showing up in /ps home, and allowing teleport.
    # Note: admins can still use /ps tp to this region type
    prevent_ps_home = false

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
    # console_message - Send message to console (colour support with &)
    # ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    # Whether or not to enable event tracking (API events will still be enabled)
    enable = false

    # Execute commands when a region is created (ex. player place protection block)
    # Variables: %player%, %world%, %region%, %block_x%, %block_y%, %block_z%
    on_region_create = [
        'global_message: &l%player% created the region %region%!',
    ]

    # Execute commands when a region is destroyed (ex. when player destroy protection block)
    # Variables: %player%, %world%, %region%, %block_x%, %block_y%, %block_z%
    on_region_destroy = [
        'console_command: say %player% has destroyed region %region%!',
    ]

```

</details>

<details>

<summary>Default messages.yml</summary>

```yml
cooldown: '§6Warning: §7Please wait for %time% seconds before placing again!'
no_such_command: §cNo such command. please type /ps help for more info
no_access: §cYou are not allowed to do that here.
no_room_in_inventory: §cYou don't have enough room in your inventory.
no_room_dropping_on_floor: §cYou don't have enough room in your inventory. Dropping
  item on floor.
invalid_block: §cInvalid protection block.
not_enough_money: §cYou don't have enough money! The price is %price%.
paid_money: §bYou've paid $%price%.
invalid_world: §cInvalid world.
must_be_player: §cYou must be a player to execute this command.
go_back_page: Go back a page.
go_next_page: Go to next page.
help: |-
  §8§m=====§r PS Help §8§m=====
  §b> §7/ps help
help_next: §7Do /ps help %page% to go to the next page!
command_requires_player_name: §cThis command requires a player name.
no_permission_toggle: §cYou don't have permission to use the toggle command.
no_permission_create: §cYou don't have permission to place a protection block.
no_permission_create_specific: §cYou don't have permission to place this protection
  block type.
no_permission_destroy: §cYou don't have permission to destroy a protection block.
no_permission_members: '&cYou don''t have permission to use member commands.'
no_permission_owners: '&cYou don''t have permission to use owner commands.'
no_permission_admin: §cYou do not have permission to use that command.
no_permission_count: §cYou do not have permission to use that command.
no_permission_count_others: §cYou do not have permission to use that command.
no_permission_flags: '&cYou do not have permission to use flag commands.'
no_permission_per_flag: §cYou do not have permission to use that flag.
no_permission_rent: §cYou do not have permission for renting.
no_permission_tax: §cYou do not have permission to use the tax command.
no_permission_buysell: §cYou do not have permission to buy and sell regions.
no_permission_unhide: §cYou do not have permission to unhide protection blocks.
no_permission_hide: §cYou do not have permission to hide protection blocks.
no_permission_info: §cYou do not have permission to use the region info command.
no_permission_priority: §cYou do not have permission to use the priority command.
no_permission_region: §cYou do not have permission to use region commands.
no_permission_tp: §cYou do not have permission to teleport to other players' protection
  blocks.
no_permission_home: §cYou do not have permission to teleport to your protection blocks.
no_permission_unclaim: §cYou do not have permission to use the unclaim command.
no_permission_unclaim_remote: §cYou do not have permission to use the unclaim remote
  command.
no_permission_view: §cYou do not have permission to use the view command.
no_permission_give: §cYou do not have permission to use the give command.
no_permission_get: §cYou do not have permission to use the get command.
no_permission_sethome: §cYou do not have permission to use the sethome command.
no_permission_list: §cYou do not have permission to use the list command.
no_permission_list_others: §cYou do not have permission to use the list command for
  others.
no_permission_name: §cYou do not have permission to use the name command.
no_permission_setparent: §cYou do not have permission to use the setparent command.
no_permission_setparent_others: §cYou do not have permission to inherit from regions
  you don't own.
no_permission_merge: §cYou do not have permission to use /ps merge.
psregion:
  added_to_region: §b%player%§7 has been added to this region.
  added_to_region_specific: §b%player%§7 has been added to region %region%.
  removed_from_region: §b%player%§7 has been removed from region.
  removed_from_region_specific: §b%player%§7 has been removed from region %region%.
  not_in_region: §cYou are not in a protection stones region!
  player_not_found: §cPlayer not found.
  not_ps_region: §cNot a protection stones region.
  region_does_not_exist: §cRegion does not exist.
  no_regions_owned: §cYou don't own any protected regions in this world!
  no_region_permission: §cYou do not have permission to do this in this region.
  protected: §bThis area is now protected.
  no_longer_protected: §eThis area is no longer protected.
  cant_protect_that: §cYou can't protect that area.
  reached_region_limit: §cYou can not have any more protected regions (%limit%).
  reached_per_block_region_limit: §cYou can not have any more regions of this type
    (%limit%).
  world_denied_create: §cYou can not create protections in this world.
  region_overlap: §cYou can not place a protection block here as it overlaps another
    region.
  region_too_close: §cYour protection block must be a minimum of %num% blocks from
    the edge of other regions!
  cant_teleport: §cYour teleportation was blocked by a protection region!
  specify_id_instead_of_alias: |-
    §7There were multiple regions found with this name! Please use an ID instead.
     Regions with this name: §b%regions%
  region_not_adjacent: §cYou've passed the limit of non-adjacent regions! Try putting
    your protection block closer to other regions you already own.
  not_overlapping: §cThese regions don't overlap each other!
  multi_region_does_not_exist: One of these regions don't exist!
  no_region_holes: §cUnprotected area detected inside region! This is not allowed!
  delete_region_prevented: §7The region could not be removed, possibly because it
    creates a hole in the existing region.
  not_owner: §cYou are not an owner of this region!
  cannot_merge_rented_region: §cCannot merge regions because region %region% is in
    the process of being rented out!
  no_permission_region_type: §cYou do not have permission to have this region type.
  hidden: §7The protection block is now hidden.
  must_be_placed_in_existing_region: §cThis must be placed inside of an existing region!
  already_in_location_is_hidden: §cA region already exists in this location (is the
    protection block hidden?)
  cannot_remove_yourself_last_owner: §cYou cannot remove yourself as you are the last
    owner.
  cannot_remove_yourself_all_regions: §cYou cannot remove yourself from all of your
    regions at once, for safety reasons.
toggle:
  help: §b> §7/ps toggle|on|off
  help_desc: Use this command to turn on or off placement of protection blocks.
  toggle_on: §bProtection block placement turned on.
  toggle_off: §bProtection block placement turned off.
count:
  count_help: §b> §7/ps count [player (optional)]
  count_help_desc: Count the number of regions you own or another player.
  personal_region_count: '§7Your region count in this world: §b%num%'
  personal_region_count_merged: '§7- Including each merged region: §b%num%'
  other_region_count: '§7%player%''s region count in this world: §b%num%'
  other_region_count_merged: '§7- Including each merged region: §b%num%'
flag:
  help: §b> §7/ps flag [flagname] [value|null|default]
  help_desc: Use this command to set a flag in your protected region.
  flag_set: §b%flag%§7 flag has been set.
  flag_not_set: §b%flag%§7 flag has §cnot§7 been set. Check your values again.
  flag_prevent_exploit: §cThis has been disabled to prevent exploits.
  flag_prevent_exploit_hover: §cDisabled for security reasons.
  gui_header: §8§m=====§r Flags (click to change) §8§m=====
  gui_hover_set: §bClick to set.
  gui_hover_set_text: |-
    §bClick to change.§f
    Current value:
    %value%
  hover_change_group: Click to set this flag to apply to only %group%.
  hover_change_group_null: §cYou must set this flag to a value before changing the
    group.
rent:
  help: §b> §7/ps rent
  help_desc: Use this command to manage rents (buying and selling).
  help_header: §8§m=====§r Rent Help §8§m=====
  already_renting: §cThe region is already being rented out! You must stop leasing
    the region first.
  not_rented: §cThis region is not being rented.
  lease_success: |-
    §bRegion leasing terms set:
    §bPrice: §7%price%
    §bPayment Term: §7%period%
  stopped: §bLeasing stopped.
  evicted: §7Evicted tenant %tenant%.
  not_renting: §cThis region is not being rented out to tenants.
  paid_landlord: §b%tenant%§7 has paid §b$%price%§7 for renting out §b%region%§7.
  paid_tenant: §7Paid §b$%price%§7 to §b%landlord%§7 for region §b%region%§7.
  renting_landlord: §b%player%§7 is now renting out region §b%region%§7.
  renting_tenant: §7You are now renting out region §b%region%§7 for §b%price%§7 per
    §b%period%§7.
  not_tenant: §cYou are not the tenant of this region!
  tenant_stopped_landlord: §b%player%§7 has stopped renting out region §b%region%§7.
    It is now available for others to rent.
  tenant_stopped_tenant: §bYou have stopped renting out region %region%.
  being_sold: §cThe region is being sold! Do /ps sell stop first.
  evict_no_money_tenant: §7You have been §cevicted§7 from region §b%region%§7 because
    you do not have enough money (%price%) to pay for rent.
  evict_no_money_landlord: §b%tenant%§7 has been §cevicted§7 from region §b%region%§7
    because they are unable to afford rent.
  cannot_rent_own_region: §cYou cannot rent your own region!
  reached_limit: §cYou've reached the limit of regions you are allowed to rent!
  price_too_low: §cThe rent price is too low (must be larger than %price%).
  price_too_high: §cThe rent price is too high (must be lower than %price%).
  period_too_short: §cThe rent period is too short (must be longer than %period% seconds).
  period_too_long: §cThe rent period is too long (must be shorter than %period% seconds).
  period_invalid: '§cInvalid period format! Example: 24h for once a day.'
  cannot_break_while_renting: §cYou cannot break the region when it is being rented
    out.
tax:
  help: §b> §7/ps tax
  help_desc: Use this command to manage and pay taxes.
  help_header: §8§m=====§r Taxes Help §8§m=====
  disabled_region: §cTaxes are disabled for this region.
  set_as_autopayer: §7Taxes for region §b%region%§7 will now be automatically paid
    by you.
  set_no_autopayer: §7Taxes for region §b%region%§7 now have to be manually paid for.
  paid: §7Paid §b$%amount%§7 in taxes for region §b%region%§7.
  info_header: §8§m=====§r Tax Info (click for more info) §8§m=====
  join_msg_pending_payments: |-
    §7You have §b$%money%§7 in tax payments due on your regions!
    View them with /ps tax info.
  player_region_info: §7> §b%region%§7 - §3$%money% due
  player_region_info_autopayer: §7> §b%region%§7 - §3$%money% due§7 (you autopay)
  click_to_show_more_info: Click to show more information.
  region_info_header: §8§m=====§r %region% Tax Info §8§m=====
  region_info: |-
    §9Tax Rate: §7$%taxrate% (sum of all merged regions)
    §9Time between tax cycles: §7%taxperiod%
    §9Time to pay taxes after cycle: §7%taxpaymentperiod%
    §9Tax Autopayer: §7%taxautopayer%
    §9Taxes Owed: §7$%taxowed%
  next_page: §7Do /ps tax info -p %page% to go to the next page!
buy:
  help: §b> §7/ps buy
  help_desc: Buy the region you are currently in.
  not_for_sale: §cThis region is not for sale.
  stop_sell: §7The region is now not for sale.
  sold_buyer: §7Bought region §b%region%§7 for §b$%price%§7 from §b%player%§7.
  sold_seller: §7Sold region §b%region%§7 for §b$%price%§7 to §b%player%§7.
sell:
  help: §b> §7/ps sell [price|stop]
  help_desc: Sell the region you are currently in.
  rented_out: §cThe region is being rented out! You must stop renting it out to sell.
  for_sale: §7The region is now for sale for §b$%price%§7.
visibility:
  hide_help: §b> §7/ps hide
  hide_help_desc: Use this command to hide or unhide your protection block.
  unhide_help: §b> §7/ps unhide
  unhide_help_desc: Use this command to hide or unhide your protection block.
  already_not_hidden: §7The protection stone doesn't appear hidden...
  already_hidden: §7The protection stone appears to already be hidden...
info:
  help: §b> §7/ps info members|owners|flags
  help_desc: Use this command inside a ps region to see more information about it.
  header: §8§m=====§r PS Info §8§m=====
  type2: '&9Type: &7%type%'
  may_be_merged: (may be merged with other types)
  merged2: '§9Merged regions: §7%merged%'
  members2: '&9Members: &7%members%'
  no_members: §c(no members)
  owners2: '&9Owners: &7%owners%'
  no_owners: §c(no owners)
  flags2: '&9Flags: &7%flags%'
  no_flags: (none)
  region2: '&9Region: &b%region%'
  priority2: '&9Priority: &b%priority%'
  parent2: '&9Parent: &b%parentregion%'
  bounds_xyz: '&9Bounds: &b(%minx%,%miny%,%minz%) -> (%maxx%,%maxy%,%maxz%)'
  bounds_xz: '&9Bounds: &b(%minx%, %minz%) -> (%maxx%, %maxz%)'
  seller2: '&9Seller: &7%seller%'
  price2: '&9Price: &7%price%'
  tenant2: '&9Tenant: &7%tenant%'
  landlord2: '&9Landlord: &7%landlord%'
  rent2: '&9Rent: &7%rent%'
  available_for_sale: §bRegion available for sale!
  available_for_rent: §bRegion available for rent!
priority:
  help: §b> §7/ps priority [number|null]
  help_desc: Use this command to set your region's priority.
  info: '§7Priority: %priority%'
  set: §ePriority has been set.
  error: §cError parsing input, check it again?
region:
  help: §b> §7/ps region [list|remove|disown] [playername]
  help_desc: Use this command to find information or edit other players' (or your
    own) protected regions.
  not_found_for_player: §7No regions found for %player% in this world.
  list: '§7%player%''s regions in this world: §b%regions%'
  remove: §e%player%'s regions have been removed in this world, and removed from regions
    %player% partially owned.
  error_search: §cError while searching for %player%'s regions. Please make sure you
    have entered the correct name.
tp:
  help: §b> §7/ps tp [id/player] [num (optional)]
  help_desc: Teleports you to one of a given player's regions.
  number_above_zero: §cPlease enter a number above 0.
  valid_number: §cPlease enter a valid number.
  only_has_regions: §c%player% only has %num% protected regions in this world!
  tping: §aTeleporting...
  error_name: §cError in teleporting to protected region! (parsing WG region name
    error)
  error_tp: §cError in finding the region to teleport to!
  in_seconds: §7Teleporting in §b%seconds%§7 seconds.
  cancelled_moved: §cTeleport cancelled. You moved!
home:
  help: §b> §7/ps home [name/id]
  help_desc: Teleports you to one of your protected regions.
  header: §8§m=====§r Homes (click to teleport) §8§m=====
  click_to_tp: Click to teleport!
  next_page: §7Do /ps home -p %page% to go to the next page!
unclaim:
  help: §b> §7/ps unclaim
  help_desc: Use this command to pickup a placed protection stone and remove the region.
  header: §8§m=====§r Unclaim (click to unclaim) §8§m=====
view:
  help: §b> §7/ps view
  help_desc: Use this command to view the borders of a protected region.
  cooldown: §cPlease wait a while before using /ps view again.
  generating: §7Generating border...
  generate_done: §aDone! The border will disappear after 30 seconds!
  removing: |-
    §bRemoving border...
    §aIf you still see ghost blocks, relog!
admin:
  help: §b> §7/ps admin
  help_desc: Do /ps admin help for more information.
  cleanup_header: |-
    §eCleanup %arg% %days% days
    ================
  cleanup_footer: |-
    §e================
    Completed %arg% cleanup.
  hide_toggled: §eAll protection stones have been %message% in this world.
  last_logon: §e%player% last played %days% days ago.
  is_banned: §e%player% is banned.
  error_parsing: §cError parsing days, are you sure it is a number?
  console_world: §cPlease specify the world as the last parameter.
  lastlogons_header: |-
    §e%days% Days Plus:
    ================
  lastlogons_line: §e%player% %time% days
  lastlogons_footer: |-
    §e================
    %count% Total Players Shown
    %checked% Total Players Checked
reload:
  help: §b> §7/ps reload
  help_desc: Reload settings from the config.
  start: §bReloading config...
  complete: §bCompleted config reload!
addremove:
  help: §b> §7/ps add|remove [playername]
  help_desc: Use this command to add or remove a member of your protected region.
  owner_help: §b> §7/ps addowner|removeowner [playername]
  owner_help_desc: Use this command to add or remove an owner of your protected region.
  player_reached_limit: §cThis player has reached their region limit.
  player_needs_to_be_online: §cThe player needs to be online to add them.
get:
  help: §b> §7/ps get [block]
  help_desc: Use this command to get or purchase a protection block.
  gotten: §bAdded protection block to inventory!
  no_permission_block: §cYou don't have permission to get this block.
  header: §8§m=====§r Protect Blocks (click to get) §8§m=====
  gui_block: §7> §b%alias% §7- %description% (§f$%price%§7)
  gui_hover: Click to buy a %alias%!
give:
  help: §b> §7/ps give [block] [player] [amount (optional)]
  help_desc: Use this command to give a player a protection block.
  given: §7Gave §b%block%§7 to §b%player%§7.
  no_inventory_room: §cThe player does not have enough inventory room.
sethome:
  help: §b> §7/ps sethome
  help_desc: Use this command to set the home of a region to where you are right now.
  set: §7The home for §b%psid%§7 has been set to your location.
list:
  help: §b> §7/ps list [player (optional)]
  help_desc: Use this command to list the regions you, or another player owns.
  header: §8§m=====§r %player%'s Regions §8§m=====
  owner: '§7Owner of:'
  member: '§7Member of:'
  no_regions: §7You currently do not own and are not a member of any regions.
  no_regions_player: §b%player% §7does not own and is not a member of any regions.
name:
  help: §b> §7/ps name [name|none]
  help_desc: Use this command to give a nickname to your region, to make identifying
    your region easier.
  removed: §7Removed the name for %id%.
  set_name: §7Set the name of %id% to §b%name%§7.
  taken: §7The region name §b%name%§7 has already been taken! Try another one.
setparent:
  help: §b> §7/ps setparent [region|none]
  help_desc: Use this command to allow this region to inherit properties from another
    region (owners, members, flags, etc.).
  success: §7Successfully set the parent of §b%id%§7 to §b%parent%§7.
  success_remove: §7Successfully removed the parent of §b%id%§7.
  circular_inheritance: §cDetected circular inheritance (the parent already inherits
    from this region?). Parent not set.
merge:
  help: §b> §7/ps merge
  help_desc: Use this command to merge the region you are in with other overlapping
    regions.
  disabled: Merging regions is disabled in the config!
  merged: §bRegions were successfully merged!
  header: §8§m=====§r Merge %region% (click to merge) §8§m=====
  warning: '§7Note: This will delete all of the settings for the current region!'
  not_allowed: §cYou are not allowed to merge this protection region type.
  into: §bThis region overlaps other regions you can merge into!
  no_region: §7There are no overlapping regions to merge into.
  click_to_merge: Click to merge with %region%!
  auto_merged: §7Region automatically merged with §b%region%§7.

```

</details>
