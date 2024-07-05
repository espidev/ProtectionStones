# API

Welcome to the ProtectionStones API page!

Please feel free to ask questions on the MOSS Discord: [https://discord.gg/PHpuzZS](https://discord.gg/PHpuzZS)

### Reference Documentation

You can access the JavaDocs [here.](https://jdps.espi.dev)

### Pulling the dependency

Also be sure to add the WorldGuard API as well [here](https://worldguard.enginehub.org/en/latest/developer/dependency/).

#### Maven

```
<dependency>
  <groupId>dev.espi</groupId>
  <artifactId>protectionstones</artifactId>
  <version>2.10.2</version>
  <scope>provided</scope>
</dependency>
```

#### Gradle

```
repository {
    mavenCentral()
}
...
dependencies {
    compile 'dev.espi:protectionstones:2.10.2'
}

```

### Getting started

The ProtectionStones API is mainly an addon for the [WorldGuard API](https://worldguard.enginehub.org/en/latest/developer/), providing developers access to ProtectionStones specific features as well.

#### Classes of interest

Be sure to check out the respective JavaDocs for each class.

**`ProtectionStones`**

This class provides access to base plugin information, such as config options and configured protection block types.

**`PSPlayer`**

A wrapper class for players, allowing for access to ProtectionStones related player information, such as the regions they own and the limits they have for each region type.

Some methods may be restricted if the player given is an offline player, or just a UUID.

```java
PSPlayer player = PSPlayer.fromPlayer(offlinePlayer);

player = PSPlayer.fromPlayer(player);

player = PSPlayer.fromPlayer(uuid);
```

**`PSRegion`**

The abstract base class that represents ProtectionStones regions.

This allows for access to all of the information related to regions.

You can obtain it in several ways:

```java
// returns the region from the location, and the PSMergedRegion if it is the source block of a merged region
PSRegion r1 = PSRegion.fromLocation(location); 

// returns the region from the location, which will always be a PSGroupRegion if this is a merged region
PSRegion r2 = PSRegion.fromLocationGroup(location);

// converts the WorldGuard region into a ProtectionStones region if it is one, and null otherwise
PSRegion r3 = PSRegion.fromWGRegion(world, protectedRegion);

// returns the region from the nickname set (/ps name), searching all worlds
PSRegion r4 = PSRegion.fromName(name);

// returns the region from the nickname set (/ps name)
PSRegion r5 = PSRegion.fromName(world, name);

// you can also obtain it from calling player.getPSRegions(world, canBeMember) if player is a PSPlayer

```

There are three types:

**`PSStandardRegion`**

Represents a standalone region that is not merged with others, and internally wraps a WorldGuard ProtectedRegion.

**`PSGroupRegion`**

Represents a region that consists of more than one block (merged regions), and internally wraps a single WorldGuard ProtectedRegion.

**`PSMergedRegion`**

Represents a merged region that is in a `PSGroupRegion`. These do not technically exist as a WorldGuard region, but ProtectionStones makes an abstraction so that they can be treated as one.

**`PSCreateEvent`**

Event that is called when a ProtectionStones region is created.

**`PSRemoveEvent`**

Event that is called when a ProtectionStones region is removed.
