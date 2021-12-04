![ProtectionStones](/logo.png?raw=true)

[![Maven Central](https://img.shields.io/maven-central/v/dev.espi/protectionstones.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22dev.espi%22%20AND%20a:%22protectionstones%22)
![Open issues](https://img.shields.io/github/issues-raw/espidev/ProtectionStones)
![Closed issues](https://img.shields.io/github/issues-closed-raw/espidev/ProtectionStones)

[Spigot](https://www.spigotmc.org/resources/protectionstones-updated-for-1-13-1-16-wg7.61797/) | [Permissions](https://github.com/espidev/ProtectionStones/wiki/Permissions) | [Commands](https://github.com/espidev/ProtectionStones/wiki/Commands) | [Configuration](https://github.com/espidev/ProtectionStones/wiki/Configuration) | [Placeholders](https://github.com/espidev/ProtectionStones/wiki/Placeholders) | [Translations](https://github.com/espidev/ProtectionStones/wiki/Translations) | [API Information](https://github.com/espidev/ProtectionStones/wiki/API) | [Javadocs](https://jdps.espi.dev/) | [Dev Builds](https://ci.espi.dev/job/ProtectionStones/)

Get support for the plugin on the M.O.S.S. Discord! https://discord.gg/cqM96tcJRx

ProtectionStones is a grief prevention and land claiming plugin.

This plugin uses a specified type of minecraft block/blocks as a protection block. When a player placed a block of that type, they are able to protect a region around them. The size of the protected region is configurable in the plugins config file. You can also set which flags players can change and also the default flags to be set when a new region is created.

View the Spigot page (with FAQ and install instructions) [here](https://www.spigotmc.org/resources/protectionstones-updated-for-1-13-1-16-wg7.61797/).

Check the [wiki](https://github.com/espidev/ProtectionStones/wiki) for plugin reference information.

### Dependencies
* ProtectionStones 2.9.0
  * Spigot 1.17+
  * WorldGuard 7.0.6+
  * WorldEdit 7.2.6+
  * Vault (Optional)
  * PlaceholderAPI (Optional)
  * LuckPerms (Optional)

### Building
Make sure you have the Java 16 JDK installed, as well as Maven.

```
git clone https://github.com/espidev/ProtectionStones.git
cd ProtectionStones
mvn clean install
```

Compiling ProtectionStones will also produce a jar with JavaDocs, which can be useful if you need documentation for an older version.

### Usage Statistics
<img src="https://bstats.org/signatures/bukkit/protectionstones.svg">

View full usage statistics [here](https://bstats.org/plugin/bukkit/ProtectionStones/4071).

This plugin is licensed under the **GPLv3**, as is required by Bukkit plugins.
