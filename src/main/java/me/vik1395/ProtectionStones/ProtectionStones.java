/*
 * Copyright 2019 ProtectionStones team and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.vik1395.ProtectionStones;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

public class ProtectionStones extends JavaPlugin {
    // change this when the config version goes up
    static final int CONFIG_VERSION = 5;

    public static Map<UUID, String> uuidToName = new HashMap<>();
    public static Map<String, UUID> nameToUUID = new HashMap<>();

    public static Plugin plugin, wgd;
    static File configLocation, blockDataFolder;

    private static Metrics metrics;

    static FileConfig config;
    // all configuration file options are stored in here
    public static Config configOptions;
    // block options
    static HashMap<String, ConfigProtectBlock> protectionStonesOptions = new HashMap<>();

    // vault economy integration
    public static boolean isVaultEnabled = false;
    public static Economy vaultEconomy;

    static List<String> toggleList = new ArrayList<>();

    public static Plugin getPlugin() {
        return plugin;
    }

    public static RegionManager getRegionManagerWithPlayer(Player p) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(p.getWorld()));
    }

    // Helper method to get the config options for a protection stone
    // Makes code look cleaner
    public static ConfigProtectBlock getBlockOptions(String blockType) {
        return protectionStonesOptions.get(blockType);
    }

    // Check if block material name is valid protection block
    public static boolean isProtectBlock(String material) {
        return protectionStonesOptions.containsKey(material);
    }

    // Get block from name (including aliases)
    public static ConfigProtectBlock getProtectBlockFromName(String name) {
        for (ConfigProtectBlock cpb : ProtectionStones.protectionStonesOptions.values()) {
            if (cpb.alias.equalsIgnoreCase(name) || cpb.type.equalsIgnoreCase(name)) {
                return cpb;
            }
        }
        return null;
    }

    // Create protection stone item (for /ps get and /ps give, and unclaiming)
    public static ItemStack createProtectBlockItem(ConfigProtectBlock b) {
        ItemStack is = new ItemStack(Material.getMaterial(b.type));
        ItemMeta im = is.getItemMeta();

        if (!b.displayName.equals("")) {
            im.setDisplayName(ChatColor.translateAlternateColorCodes('&', b.displayName));
        }
        List<String> lore = new ArrayList<>();
        for (String s : b.lore) lore.add(ChatColor.translateAlternateColorCodes('&', s));
        im.setLore(lore);

        // add identifier for protection stone created items
        im.getCustomTagContainer().setCustomTag(new NamespacedKey(plugin, "isPSBlock"), ItemTagType.BYTE, (byte) 1);

        is.setItemMeta(im);
        return is;
    }

    // Turn WG region name into a location (ex. ps138x35y358z)
    public static PSLocation parsePSRegionToLocation(String regionName) {
        int psx = Integer.parseInt(regionName.substring(2, regionName.indexOf("x")));
        int psy = Integer.parseInt(regionName.substring(regionName.indexOf("x") + 1, regionName.indexOf("y")));
        int psz = Integer.parseInt(regionName.substring(regionName.indexOf("y") + 1, regionName.length() - 1));
        return new PSLocation(psx, psy, psz);
    }

    // Find the id of the current region the player is in and get WorldGuard player object for use later
    public static String playerToPSID(Player p) {
        BlockVector3 v = BlockVector3.at(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());
        String currentPSID = "";
        RegionManager rgm = getRegionManagerWithPlayer(p);
        List<String> idList = rgm.getApplicableRegionsIDs(v);
        if (idList.size() == 1) {
            if (idList.get(0).startsWith("ps")) currentPSID = idList.get(0);
        } else {
            // Get nearest protection stone if in overlapping region
            double distanceToPS = 10000D, tempToPS;
            for (String currentID : idList) {
                if (currentID.substring(0, 2).equals("ps")) {
                    PSLocation psl = parsePSRegionToLocation(currentID);
                    Location psLocation = new Location(p.getWorld(), psl.x, psl.y, psl.z);
                    tempToPS = p.getLocation().distance(psLocation);
                    if (tempToPS < distanceToPS) {
                        distanceToPS = tempToPS;
                        currentPSID = currentID;
                    }
                }
            }
        }
        return currentPSID;
    }

    // Helper method to either remove, disown or regen a player's ps region
    // NOTE: be sure to save the region manager after
    public static void removeDisownPSRegion(LocalPlayer lp, String arg, String region, RegionManager rgm, World w) {
        ProtectedRegion r = rgm.getRegion(region);
        switch (arg) {
            case "disown":
                DefaultDomain owners = r.getOwners();
                owners.removePlayer(lp);
                r.setOwners(owners);
                break;
            case "remove":
                if (region.substring(0, 2).equals("ps")) {
                    PSLocation psl = ProtectionStones.parsePSRegionToLocation(region);
                    Block blockToRemove = w.getBlockAt(psl.x, psl.y, psl.z);
                    blockToRemove.setType(Material.AIR);
                }
                rgm.removeRegion(region);
                break;
        }
    }


    // called on first start, and /ps reload
    public static void loadConfig(boolean isReload) {
        // init config
        Config.initConfig();

        // init messages
        PSL.loadConfig();

        // add command to Bukkit (using reflection)
        if (!isReload) {
            try {
                final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
                bukkitCommandMap.setAccessible(true);
                CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());

                PSCommand psc = new PSCommand(ProtectionStones.configOptions.base_command);
                for (String command : ProtectionStones.configOptions.aliases) { // add aliases
                    psc.getAliases().add(command);
                }
                commandMap.register(ProtectionStones.configOptions.base_command, psc); // register command
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLoad() {
        // register flags
        FlagHandler.registerFlags();
    }

    // plugin enable
    @Override
    public void onEnable() {
        TomlFormat.instance();

        plugin = this;
        configLocation = new File(this.getDataFolder() + "/config.toml");
        blockDataFolder = new File(this.getDataFolder() + "/blocks");

        // Metrics (bStats)
        metrics = new Metrics(this);

        // register event listeners
        getServer().getPluginManager().registerEvents(new ListenerClass(), this);

        // check that WorldGuard and WorldEdit are enabled (Worldguard will only be enabled if there's worldedit)
        if (getServer().getPluginManager().getPlugin("WorldGuard") != null && getServer().getPluginManager().getPlugin("WorldGuard").isEnabled()) {
            wgd = getServer().getPluginManager().getPlugin("WorldGuard");
        } else {
            getServer().getConsoleSender().sendMessage("WorldGuard or WorldEdit not enabled! Disabling ProtectionStones...");
            getServer().getPluginManager().disablePlugin(this);
        }


        // check if Vault is enabled (for economy support)_
        if (getServer().getPluginManager().getPlugin("Vault") != null && getServer().getPluginManager().getPlugin("Vault").isEnabled()) {
            RegisteredServiceProvider<Economy> econ = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (econ == null) {
                getServer().getLogger().info("No economy plugin found by Vault! There will be no economy support!");
            } else {
                vaultEconomy = econ.getProvider();
                isVaultEnabled = true;
            }
        } else {
            getServer().getLogger().info("Vault not enabled! There will be no economy support!");
        }

        // Load configuration
        loadConfig(false);

        // uuid cache
        getServer().getConsoleSender().sendMessage("Building UUID cache... (if slow change async-load-uuid-cache in the config to true)");
        if (configOptions.asyncLoadUUIDCache) { // async load
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
                    uuidToName.put(op.getUniqueId(), op.getName());
                    nameToUUID.put(op.getName(), op.getUniqueId());
                }
            });
        } else { // sync load
            for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
                uuidToName.put(op.getUniqueId(), op.getName());
                nameToUUID.put(op.getName(), op.getUniqueId());
            }
        }

        // check if uuids have been upgraded already
        getServer().getConsoleSender().sendMessage("Checking if PS regions have been updated to UUIDs...");

        // Update to UUIDs
        if (configOptions.uuidupdated == null || !configOptions.uuidupdated) {
            convertToUUID();
        }

        getServer().getConsoleSender().sendMessage(ChatColor.WHITE + "ProtectionStones has successfully started!");
    }

    public static boolean hasNoAccess(ProtectedRegion region, Player p, LocalPlayer lp, boolean canBeMember) {
        // Region is not valid
        if (region == null) return true;

        return !p.hasPermission("protectionstones.superowner") && !region.isOwner(lp) && (!canBeMember || !region.isMember(lp));
    }

    // check that all of the PS custom flags are in ps regions and upgrade if not
    public static void upgradeRegions() {

        YamlConfiguration hideFile = null;
        if (new File(plugin.getDataFolder() + "/hiddenpstones.yml").exists()) {
            hideFile = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + "/hiddenpstones.yml"));
        }
        for (World world : Bukkit.getWorlds()) {
            RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
            for (String regionName : rm.getRegions().keySet()) {
                if (regionName.startsWith("ps")) {
                    try {
                        PSLocation psl = parsePSRegionToLocation(regionName);
                        ProtectedRegion r = rm.getRegion(regionName);

                        // get material of ps
                        String entry = psl.x + "x" + psl.y + "y" + psl.z + "z", material;
                        if (hideFile != null && hideFile.contains(entry)) {
                            material = hideFile.getString(entry);
                        } else {
                            material = world.getBlockAt(psl.x, psl.y, psl.z).getType().toString();
                        }

                        if (r.getFlag(FlagHandler.PS_BLOCK_MATERIAL) == null) {
                            r.setFlag(FlagHandler.PS_BLOCK_MATERIAL, material);
                        }

                        if (r.getFlag(FlagHandler.PS_HOME) == null) {
                            if (ProtectionStones.isProtectBlock(material)) {
                                ConfigProtectBlock cpb = ProtectionStones.getBlockOptions(material);
                                r.setFlag(FlagHandler.PS_HOME, (psl.x + cpb.homeXOffset) + " " + (psl.y + cpb.homeYOffset) + " " + (psl.z + cpb.homeZOffset));
                            } else {
                                r.setFlag(FlagHandler.PS_HOME, psl.x + " " + psl.y + " " + psl.z);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                rm.save();
            } catch (Exception e) {
                Bukkit.getLogger().severe("[ProtectionStones] WorldGuard Error [" + e + "] during Region File Save");
            }
        }
    }

    // convert regions to use UUIDs instead of player names
    private static void convertToUUID() {
        Bukkit.getLogger().info("Updating PS regions to UUIDs...");
        for (World world : Bukkit.getWorlds()) {
            RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));

            // iterate over regions in world
            for (String regionName : rm.getRegions().keySet()) {
                if (regionName.startsWith("ps")) {
                    ProtectedRegion region = rm.getRegion(regionName);

                    // convert owners with player names to UUIDs
                    List<String> owners, members;
                    owners = new ArrayList<>(region.getOwners().getPlayers());
                    members = new ArrayList<>(region.getMembers().getPlayers());

                    // convert
                    for (String owner : owners) {
                        UUID uuid = Bukkit.getOfflinePlayer(owner).getUniqueId();
                        region.getOwners().removePlayer(owner);
                        region.getOwners().addPlayer(uuid);
                    }
                    for (String member : members) {
                        UUID uuid = Bukkit.getOfflinePlayer(member).getUniqueId();
                        region.getMembers().removePlayer(member);
                        region.getMembers().addPlayer(uuid);
                    }
                }
            }

            try {
                rm.save();
            } catch (Exception e) {
                Bukkit.getLogger().severe("[ProtectionStones] WorldGuard Error [" + e + "] during Region File Save");
            }
        }

        // update config to mark that uuid upgrade has been done
        config.set("uuidupdated", true);
        config.save();
        Bukkit.getLogger().info("Done!");
    }
}