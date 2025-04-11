# Frequently Asked Questions

Here are answers to some frequently asked questions!

#### When I add a new block config, an error occurs!

Check the console logs for an error (you can bring up the value for /ps reload). Most likely you made a configuration mistake, you can read the TOML spec [here](https://github.com/toml-lang/toml), or see the default configuration files. For some numerical values, you need to add the decimal (`100.0` instead of just `100` ), see the default config for what to copy.

#### When I get the protection block from the creative inventory, it doesn't work (nothing happens when I place it)!

This is due to the `restrict_obtaining` block config option, which is enabled by default. This restricts obtaining protection blocks to just /ps get and /ps give (and plugins that use the API), since they attach NBT data to the block. That means that when the block is obtained through natural means, it won't work as a protection block. You can **disable this behavior** by setting `restrict_obtaining = false`.

#### How can I add protection blocks in a kit?

The `restrict_obtaining` block config option restricts obtaining protection blocks to just /ps get and /ps give (and plugins that use the API), since they attach NBT data to the block. You need to add a command (/ps give) to give the block to the player in the kit. For EssentialsX, check the kit section [here](https://essentialsx.net/wiki/Improvements.html).

#### How can I sell protection blocks in a shop?

There are many ways to sell protection blocks, with the easiest being setting a price on /ps get (requires Vault, configured in the block config). You can also use **external shop plugins** to sell the protection block, either through **running a command** (/ps give block %player% ...) or giving an item that was obtained through /ps get or /ps give.

#### Why don't players see the region merge menu?

Be sure to give players the `protectionstones.merge` permission.

#### Why do some flags apply to members of the region as well? (ex. block-break deny)

By default flags apply to everyone, including members of the region. You have to use region groups (using -g) to change this (ex. set the group to apply to only nonmembers). You can read about this [here](http://worldguard.enginehub.org/en/latest/regions/flags).

#### How do I change the block type of a region after players have already created regions with it?

You can use the command `/ps admin changeblock [world] [fromblock] [toblock]` to do this. Create a new config (block.toml file) in the blocks folder with the changed block type, while keeping the old one still loaded. Do `/ps reload`, and run the command to do the conversion. After it completes, you can remove the block config with the old type.

#### Why does \[x] flag not work?

Flags are handled by WorldGuard, and some of their behaviours may not be what you expect. You can read more about WorldGuard flags here: [https://worldguard.enginehub.org/en/latest/regions/flags/](https://worldguard.enginehub.org/en/latest/regions/flags/)

#### How can I allow PS regions to overlap or be created inside manually created WorldGuard regions?

You can use the concept of [priorities](https://worldguard.enginehub.org/en/latest/regions/priorities/) in WorldGuard, and set the manually created WG region to a lower priority than the ProtectionStones regions.

#### I found a bug! How do I get it fixed?

Please file this issue on either [GitHub](https://github.com/espidev/ProtectionStones/issues), or go on the Discord support channel.

#### I want a new feature!

Please file this issue on either [GitHub](https://github.com/espidev/ProtectionStones/issues), or go on the Discord support channel. The feature may not be aligned with the goals of the plugin, so it might also be worth making another plugin that uses the developer API instead.

#### Is there a developer API?

Yes there is! For the most part, you can simply just use the WorldGuard API, but there are some useful features in the ProtectionStones API that add on to it. See the documentation [here](https://espidev.gitbook.io/protectionstones/api).
