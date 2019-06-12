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

package dev.espi.ProtectionStones;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.ProtectionStones.commands.PSCommandArg;
import dev.espi.ProtectionStones.utils.UUIDCache;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.*;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

public class ProtectionStones extends JavaPlugin {
    // change this when the config version goes up
    static final int CONFIG_VERSION = 5;

    static File configLocation, blockDataFolder;
    static FileConfig config;

    private static List<PSCommandArg> commandArgs = new ArrayList<>();
    private static ProtectionStones plugin;

    // all configuration file options are stored in here
    private PSConfig configOptions;
    static HashMap<String, PSProtectBlock> protectionStonesOptions = new HashMap<>();

    // vault economy integration
    private boolean vaultSupportEnabled = false;
    private Economy vaultEconomy;

    public static List<UUID> toggleList = new ArrayList<>();

    /**
     * @return the plugin instance that is currently being used
     */
    public static ProtectionStones getInstance() {
        return plugin;
    }

    /**
     * Gets the config options for the protection block type specified.
     * @param blockType the material type name (Bukkit) of the protect block to get the options for
     * @return the config options for the protect block specified (null if not found)
     */
    public static PSProtectBlock getBlockOptions(String blockType) {
        return protectionStonesOptions.get(blockType);
    }

    /**
     * @param material material type to check (Bukkit material name)
     * @return whether or not that material is being used for a protection block
     */
    public static boolean isProtectBlockType(String material) {
        return protectionStonesOptions.containsKey(material);
    }

    // Get block from name (including aliases)
    public static PSProtectBlock getProtectBlockFromAlias(String name) {
        for (PSProtectBlock cpb : ProtectionStones.protectionStonesOptions.values()) {
            if (cpb.alias.equalsIgnoreCase(name) || cpb.type.equalsIgnoreCase(name)) {
                return cpb;
            }
        }
        return null;
    }

    /**
     * Add a command argument to /ps.
     * @param psca PSCommandArg object to be added
     */
    public void addCommandArgument(PSCommandArg psca) {
        commandArgs.add(psca);
    }

    /**
     * @return the list of command arguments for /ps
     */
    public List<PSCommandArg> getCommandArguments() {
        return commandArgs;
    }

    /**
     * @return whether vault support is enabled
     */
    public boolean isVaultSupportEnabled() {
        return vaultSupportEnabled;
    }

    /**
     * @return returns this instance's vault economy hook
     */
    public Economy getVaultEconomy() {
        return vaultEconomy;
    }

    /**
     * @return returns the config options of this instance of ProtectionStones
     */
    public PSConfig getConfigOptions() {
        return configOptions;
    }

    /**
     * @param conf config object to replace current config
     */
    public void setConfigOptions(PSConfig conf) {
        this.configOptions = conf;
    }

    // Create protection stone item (for /ps get and /ps give, and unclaiming)
    public static ItemStack createProtectBlockItem(PSProtectBlock b) {
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

    // called on first start, and /ps reload
    public static void loadConfig(boolean isReload) {
        // init config
        PSConfig.initConfig();

        // init messages
        PSL.loadConfig();

        // add command to Bukkit (using reflection)
        if (!isReload) {
            try {
                final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
                bukkitCommandMap.setAccessible(true);
                CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());

                PSCommand psc = new PSCommand(getInstance().configOptions.base_command);
                for (String command : getInstance().configOptions.aliases) { // add aliases
                    psc.getAliases().add(command);
                }
                commandMap.register(getInstance().configOptions.base_command, psc); // register command
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLoad() {
        // register WG flags
        FlagHandler.registerFlags();
    }

    @Override
    public void onEnable() {
        TomlFormat.instance();

        plugin = this;
        configLocation = new File(this.getDataFolder() + "/config.toml");
        blockDataFolder = new File(this.getDataFolder() + "/blocks");

        // Metrics (bStats)
        new Metrics(this);

        // load command arguments
        PSCommand.addDefaultArguments();

        // register event listeners
        getServer().getPluginManager().registerEvents(new ListenerClass(), this);

        // check that WorldGuard and WorldEdit are enabled (WorldGuard will only be enabled if there's WorldEdit)
        if (getServer().getPluginManager().getPlugin("WorldGuard") == null || !getServer().getPluginManager().getPlugin("WorldGuard").isEnabled()) {
            getServer().getConsoleSender().sendMessage("WorldGuard or WorldEdit not enabled! Disabling ProtectionStones...");
            getServer().getPluginManager().disablePlugin(this);
        }


        // check if Vault is enabled (for economy support)
        if (getServer().getPluginManager().getPlugin("Vault") != null && getServer().getPluginManager().getPlugin("Vault").isEnabled()) {
            RegisteredServiceProvider<Economy> econ = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (econ == null) {
                getServer().getLogger().info("No economy plugin found by Vault! There will be no economy support!");
            } else {
                vaultEconomy = econ.getProvider();
                vaultSupportEnabled = true;
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
                    UUIDCache.uuidToName.put(op.getUniqueId(), op.getName());
                    UUIDCache.nameToUUID.put(op.getName(), op.getUniqueId());
                }
            });
        } else { // sync load
            for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
                UUIDCache.uuidToName.put(op.getUniqueId(), op.getName());
                UUIDCache.nameToUUID.put(op.getName(), op.getUniqueId());
            }
        }

        // check if uuids have been upgraded already
        getServer().getConsoleSender().sendMessage("Checking if PS regions have been updated to UUIDs...");

        // Update to UUIDs
        if (configOptions.uuidupdated == null || !configOptions.uuidupdated) LegacyUpgrade.convertToUUID();

        getServer().getConsoleSender().sendMessage(ChatColor.WHITE + "ProtectionStones has successfully started!");
    }

    public static boolean hasNoAccess(ProtectedRegion region, Player p, LocalPlayer lp, boolean canBeMember) {
        // Region is not valid
        if (region == null) return true;

        return !p.hasPermission("protectionstones.superowner") && !region.isOwner(lp) && (!canBeMember || !region.isMember(lp));
    }

}