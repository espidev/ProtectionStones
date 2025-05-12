# ProtectionStones Database Implementation

## Overview

ProtectionStones now includes a database system for improved performance and data persistence. This implementation uses SQLite with HikariCP connection pooling for optimal performance.

## Features

- **Persistent Storage**: Region data is now stored in a local SQLite database for reliable recovery in case of server issues.
- **Connection Pooling**: Uses HikariCP for efficient database connection management.
- **Asynchronous Operations**: All database operations are performed asynchronously to avoid impacting server performance.
- **Cache System**: Implements intelligent caching to reduce database queries and improve response times.

## Tables Structure

The database consists of three main tables:

### Protected Blocks (ps_blocks)
- Stores information about each protection block placement
- Fields:
  - `id`: Primary key
  - `world`: Name of the world
  - `x`, `y`, `z`: Block coordinates
  - `block_type`: The material type of the protection block
  - `is_hidden`: Whether the block is hidden
  - `region_id`: Associated WorldGuard region ID

### Regions (ps_regions)
- Stores information about each protected region
- Fields:
  - `id`: Primary key (WorldGuard region ID)
  - `world`: Name of the world
  - `name`: Custom name of the region (null if not set)
  - `owner_uuid`: UUID of the region owner
  - `last_tax_payment`: Timestamp of last tax payment
  - `rent_last_paid`: Timestamp of last rent payment

### UUID Cache (ps_cached_uuids)
- Caches player UUID to name mappings
- Fields:
  - `uuid`: Player UUID (primary key)
  - `name`: Player name
  - `last_seen`: Timestamp of last player activity

## Optimization Benefits

1. **Reduced WorldGuard region lookups**: Using cached data significantly reduces the need for expensive region lookups.
2. **Faster startup**: The plugin can load data incrementally rather than all at once.
3. **Memory efficiency**: Data is stored in the database instead of keeping everything in memory.
4. **Better scaling**: Works efficiently even with thousands of regions.

## Developer Information

### Key Classes
- `DatabaseManager`: Core database operations and connection management
- `OptimizationManager`: Caching logic and performance optimizations
- `PSRegion`: Enhanced with database methods (syncToDB, removeFromDB, updateDB)

### Using the Database API
```java
// Save a block to database
DatabaseManager.saveBlockAsync(location, blockType, isHidden, regionId);

// Get region information
DatabaseManager.getRegionDataAsync(regionId)
    .thenAccept(data -> {
        // Handle region data
    });

// Update a region in the database
regionInstance.updateDB();
```

## Technical Details

- The database file is located at `plugins/ProtectionStones/database.db`
- All data is automatically synced from memory to database when changes occur
- Connection pool is sized with a maximum of 10 connections
- Database operations run on a dedicated thread pool to avoid blocking the main server thread 