# Placeholders

These are the placeholders provided by ProtectionStones for PlaceholderAPI.

## Region Placeholders

There are two ways to refer to a region:

```
%protectionstones_currentregion_...% - current region the player is in
%protectionstones_region_[identifier]_...% - search a region by name or id
```

Here are all of the possible options (replace the `...`, ex. `%protectionstones_currentregion_owners%`):

<table data-full-width="false"><thead><tr><th></th><th>Description</th></tr></thead><tbody><tr><td><code>owners</code></td><td>List of owners, separated by comma and space.</td></tr><tr><td><code>members</code></td><td>List of members, separated by comma and space.</td></tr><tr><td><code>name</code></td><td>The name of the region, or the id if there isn't one set.</td></tr><tr><td><code>id</code></td><td>The id of the region.</td></tr><tr><td><code>type</code></td><td>The block type of the region.</td></tr><tr><td><code>alias</code></td><td>The region's alias type.</td></tr><tr><td><code>is_hidden</code></td><td>Whether or not the protection block is hidden.</td></tr><tr><td><code>home_location</code></td><td>The location of the region's home.</td></tr><tr><td><code>is_for_sale</code></td><td>Whether or not the region is for sale.</td></tr><tr><td><code>rent_stage</code></td><td>The rent stage of the region.</td></tr><tr><td><code>landlord</code></td><td>The region's landlord if it is being rented out.</td></tr><tr><td><code>rent_period</code></td><td>The period for rent payments.</td></tr><tr><td><code>rent_amount</code></td><td>The amount tenants pay per rent period.</td></tr><tr><td><code>sale_price</code></td><td>The price as to which the region is on sale for.</td></tr><tr><td><code>tax_owed</code></td><td>How much tax is unpaid for this region.</td></tr><tr><td><code>tax_autopayer</code></td><td>The region's configured autopayer.</td></tr><tr><td><code>flags</code></td><td>The list of flags of the region.</td></tr><tr><td><code>flags_[flag]</code></td><td>Where [flag] is the name of the flag you want to get the value of.</td></tr></tbody></table>

## Config Placeholders

All of the config options are available to use as placeholders.

I will not be listing them all here, because they are the same names as what they are in the config [here](https://github.com/espidev/ProtectionStones/wiki/Configuration).

In order to refer to them, you need to add the section names behind the config option.

For example, to refer to the config setting for the price of a protection block, you add the section name behind: `%protectionstones_config_block_[alias]_block_data_price%`

### Global config placeholders

How to refer: `%protectionstones_config_...%`

Special formatted placeholders:

```
%protectionstones_config_economy_max_rent_period_pretty%
%protectionstones_config_economy_min_rent_period_pretty%
```

### Block config placeholders

Ways to refer:

```
%protectionstones_config_block_[alias]_...%
%protectionstones_currentregion_config_...%
%protectionstones_region_[identifier]_config_...%
```

Special formatted placeholders (replace the `...`):

```
economy_tax_period_pretty
economy_tax_payment_time_pretty
```

## Player Placeholders

Refer to the current player with: `%protectionstones_currentplayer_...%`

Replace `...` with the following:

<table data-full-width="true"><thead><tr><th></th><th>Description</th></tr></thead><tbody><tr><td><code>global_region_limit</code></td><td>The limit of regions the current player can have, or -1 if there is no limit.</td></tr><tr><td><code>region_limit_[alias]</code></td><td>The limit for a specific block type (replace [alias] with the alias of the type).</td></tr><tr><td><code>total_tax_owed</code></td><td>The total amount of tax money owed by the player.</td></tr><tr><td><code>num_of_owned_regions</code></td><td>The number of regions the player owns.</td></tr><tr><td><code>num_of_owned_regions_[world]</code></td><td>The number of regions the player owns in a world (replace [world] with the world name).</td></tr><tr><td><code>num_of_accessible_regions</code></td><td>The number of regions the player owns or is a member of.</td></tr><tr><td><code>num_of_accessible_regions_[world]</code></td><td>The number of regions the player owns or is a member of in a world (replace [world] with the world name).</td></tr><tr><td><code>owned_regions_ids_[world]</code></td><td>Lists the IDs of all of the regions the player owns in a world, separated by ", ".</td></tr><tr><td><code>accessible_regions_ids_[world]</code></td><td>Lists the IDs of all of the regions the player owns or is a member of in a world, separated by ", ".</td></tr><tr><td><code>owned_regions_names_[world]</code></td><td>Lists the name or IDs of all of the regions the player owns in a world, separated by ", ".</td></tr><tr><td><code>accessible_regions_names_[world]</code></td><td>Lists the name or IDs of all of the regions the player owns or is a member of in a world, separated by ", ".</td></tr><tr><td><code>protection_placing_enabled</code></td><td>Whether the player has protection block placing enabled (using /ps on</td></tr></tbody></table>
