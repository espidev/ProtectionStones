/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.espi.protectionstones;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.commands.ArgHelp;
import dev.espi.protectionstones.commands.PSCommandArg;
import dev.espi.protectionstones.placeholders.PSPlaceholderExpansion;
import dev.espi.protectionstones.utils.BlockUtil;
import dev.espi.protectionstones.utils.RecipeUtil;
import dev.espi.protectionstones.utils.upgrade.LegacyUpgrade;
import dev.espi.protectionstones.utils.UUIDCache;
import dev.espi.protectionstones.utils.WGUtils;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandMap;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.luckperms.api.LuckPerms;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * The base class for the plugin. Some utilities are static, and others are instance methods, so they need to
 * be accessed through getInstance().
 */

public class ProtectionStones extends JavaPlugin {
    // change this when the config version goes up
    public static final int CONFIG_VERSION = 16;

    private boolean debug = false;
    private static ProtectionStones instance;
    public static File configLocation, blockDataFolder;
    public static CommentedFileConfig config;

    private static List<PSCommandArg> commandArgs = new ArrayList<>();
    private static ProtectionStones plugin;

    private PSEconomy economy;

    // all configuration file options are stored in here
    private PSConfig configOptions;
    static HashMap<String, PSProtectBlock> protectionStonesOptions = new HashMap<>();

    private BukkitAudiences adventure;


    // ps alias to id cache
    // <world-name, <alias, [ids]>>
    static HashMap<UUID, HashMap<String, ArrayList<String>>> regionNameToID = new HashMap<>();

    // vault economy integration
    private boolean vaultSupportEnabled = false;
    private Economy vaultEconomy;

    // luckperms integration
    private boolean luckPermsSupportEnabled = false;
    private LuckPerms luckPerms;

    private boolean placeholderAPISupportEnabled = false;

    // ps toggle/on/off list
    public static Set<UUID> toggleList = new HashSet<>();

    /* ~~~~~~~~~~ Instance methods ~~~~~~~~~~~~ */

    /**
     * Add a command argument to /ps.
     *
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
     * @return whether PlaceholderAPI support is enabled
     */
    public boolean isPlaceholderAPISupportEnabled() {
        return placeholderAPISupportEnabled;
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
     * @return the instance's {@link PSEconomy} instance
     */
    public PSEconomy getPSEconomy() {
        return economy;
    }

    public boolean isLuckPermsSupportEnabled() {
        return luckPermsSupportEnabled;
    }

    public LuckPerms getLuckPerms() {
        return luckPerms;
    }

    /**
     * @param debug whether the plugin should be in debug mode
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * @return whether the plugin is in debug mode
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * Print a debug message (only prints if the plugin is in debug mode).
     * @param msg the message to print
     */
    public void debug(String msg) {
        if (debug) {
            getLogger().info("[DEBUG] " + msg);
        }
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

    /**
     * Returns the list of PSProtectBlocks configured through the config.
     *
     * @return the list of PSProtectBlocks configured
     */
    public List<PSProtectBlock> getConfiguredBlocks() {
        List<PSProtectBlock> l = new ArrayList<>();
        for (PSProtectBlock b : protectionStonesOptions.values()) {
            if (!l.contains(b)) l.add(b);
        }
        return l;
    }


    /* ~~~~~~~~~~ Static methods ~~~~~~~~~~~~~~ */

    /**
     * @return the plugin instance that is currently being used
     */
    public static ProtectionStones getInstance() {
        return plugin;
    }

    /**
     * @return the plugin's logger
     */
    public static Logger getPluginLogger() {
        return plugin.getLogger();
    }

    /**
     * @return the PSEconomy object adapter
     */
    public static PSEconomy getEconomy() {
        return getInstance().getPSEconomy();
    }

    /**
     * Get the protection block config options for the block specified.
     *
     * @param block the block to get the block options of
     * @return the config options for the protect block specified (null if not found)
     */

    public static PSProtectBlock getBlockOptions(Block block) {
        if (block == null) return null;
        return getBlockOptions(BlockUtil.getProtectBlockType(block));
    }

    /**
     * Get the protection block config options for the item specified.
     *
     * If the options has restrict-obtaining enabled, and the item does not contain the required NBT tag, null will
     * be returned.
     *
     * @param item the item to get the block options of
     * @return the config options for the protect item specified (null if not found)
     */

    public static PSProtectBlock getBlockOptions(ItemStack item) {
        if (!isProtectBlockItem(item)) return null;
        return getBlockOptions(BlockUtil.getProtectBlockType(item));
    }

    /**
     * Gets the config options for the protection block type specified. It is recommended to use the block parameter overloaded
     * method instead if possible, since it deals better with heads.
     *
     * @param blockType the material type name (Bukkit) of the protect block to get the options for, or "PLAYER_HEAD name" for heads
     * @return the config options for the protect block specified (null if not found)
     */
    public static PSProtectBlock getBlockOptions(String blockType) {
        return protectionStonesOptions.get(blockType);
    }

    public static boolean isProtectBlockType(Block b) {
        return getBlockOptions(b) != null;
    }

    /**
     * Get whether or not a material is used as a protection block. It is recommended to use the block
     * parameter overloaded method if possible since player heads have a different format.
     *
     * @param material material type to check (Bukkit material name), or "PLAYER_HEAD name" for heads
     * @return whether or not that material is being used for a protection block
     */
    public static boolean isProtectBlockType(String material) {
        return protectionStonesOptions.containsKey(material);
    }

    /**
     * Check whether or not a given block is a protection block, and actually protects a region.
     * @param b the block to look at
     * @return whether or not the block is a protection block responsible for a region.
     */

    public static boolean isProtectBlock(Block b) {
        if (!isProtectBlockType(b)) return false;
        RegionManager rgm = WGUtils.getRegionManagerWithWorld(b.getWorld());
        if (rgm == null) return false;
        return rgm.getRegion(WGUtils.createPSID(b.getLocation())) != null || PSMergedRegion.getMergedRegion(b.getLocation()) != null;
    }

    /**
     * Check if a WorldGuard {@link ProtectedRegion} is a ProtectionStones region, and is configured in the config.
     *
     * @param r the region to check
     * @return true if the WorldGuard region is a ProtectionStones region, and false if it isn't
     */
    public static boolean isPSRegion(ProtectedRegion r) {
        return isPSRegionFormat(r) && getBlockOptions(r.getFlag(FlagHandler.PS_BLOCK_MATERIAL)) != null;
    }

    /**
     * Check if a WorldGuard {@link ProtectedRegion} has the format of a ProtectionStones region, but is not necessarily configured
     * in the config.
     *
     * @param r the region to check
     * @return true if the WorldGuard region is a ProtectionStones region, and false if it isn't
     */
    public static boolean isPSRegionFormat(ProtectedRegion r) {
        return r != null && r.getId().startsWith("ps") && r.getFlag(FlagHandler.PS_BLOCK_MATERIAL) != null;
    }

    /**
     * Check if a ProtectionStones name is already used by a region globally (from /ps name)
     *
     * @param name the name to search for
     * @return whether or not there is a region with this name
     */

    public static boolean isPSNameAlreadyUsed(String name) {
        for (UUID worldUid : regionNameToID.keySet()) {
            RegionManager rgm = WGUtils.getRegionManagerWithWorld(Bukkit.getWorld(worldUid));

            List<String> l = regionNameToID.get(worldUid).get(name);
            if (l == null) continue;
            for (int i = 0; i < l.size(); i++) { // remove outdated cache
                if (rgm.getRegion(l.get(i)) == null) {
                    l.remove(i);
                    i--;
                }
            }
            if (!l.isEmpty()) return true;
        }
        return false;
    }

    /**
     * Get protection stone regions using an ID or alias.
     *
     * @param w          the world to search in (only if it is an id; aliases/names are global)
     * @param identifier id or alias of the region
     * @return a list of psregions that match the id or alias; will be empty if no regions were found
     */

    public static List<PSRegion> getPSRegions(World w, String identifier) {
        RegionManager rgm = WGUtils.getRegionManagerWithWorld(w);
        if (rgm == null) return new ArrayList<>();

        PSRegion r = PSRegion.fromWGRegion(w, rgm.getRegion(identifier));
        if (r != null) { // return id based query
            return new ArrayList<>(Collections.singletonList(r));
        } else { // return alias based query
            List<PSRegion> regions = new ArrayList<>();
            PSRegion.fromName(identifier).values().forEach(regions::addAll);
            return regions;
        }
    }

    /**
     * Removes a protection stone region given its ID, and the region manager it is stored in
     * Note: Does not remove the PS block.
     *
     * @param w    the world that the region is in
     * @param psID the worldguard region ID of the region
     * @return whether or not the event was cancelled
     */

    public static boolean removePSRegion(World w, String psID) {
        PSRegion r = PSRegion.fromWGRegion(checkNotNull(w), checkNotNull(WGUtils.getRegionManagerWithWorld(w).getRegion(psID)));
        return r != null && r.deleteRegion(false);
    }

    /**
     * Removes a protection stone region given its ID, and the region manager it is stored in, with a player as its cause
     * Note: Does not remove the PS block, and does not check if the player (cause) has permission to do this.
     *
     * @param w     the world that the region is in
     * @param psID  the worldguard region ID of the region
     * @param cause the player that caused the removal
     * @return whether or not the event was cancelled
     */

    public static boolean removePSRegion(World w, String psID, Player cause) {
        PSRegion r = PSRegion.fromWGRegion(checkNotNull(w), checkNotNull(WGUtils.getRegionManagerWithWorld(w).getRegion(psID)));
        return r != null && r.deleteRegion(false, cause);
    }

    /**
     * Get the config options for a protect block based on its alias
     *
     * @param name the alias of the protection block
     * @return the protect block options, or null if it wasn't found
     */

    public static PSProtectBlock getProtectBlockFromAlias(String name) {
        if (name == null) return null;
        for (PSProtectBlock cpb : ProtectionStones.protectionStonesOptions.values()) {
            if (cpb.alias.equalsIgnoreCase(name) || cpb.type.equalsIgnoreCase(name)) return cpb;
        }
        return null;
    }

    /**
     * Check if an item is a valid protection block, and if checkNBT is true, check if it was created by
     * ProtectionStones. Be aware that blocks may have restrict-obtaining off, meaning that it is ignored whether or not
     * the item is created by ProtectionStones (in this case have checkNBT false).
     *
     * @param item     the item to check
     * @param checkNBT whether or not to check if the plugin signed off on the item (restrict-obtaining)
     * @return whether or not the item is a valid protection block item, and was created by protection stones
     */

    public static boolean isProtectBlockItem(ItemStack item, boolean checkNBT) {
        if (item == null) return false;
        // check basic item
        if (!ProtectionStones.isProtectBlockType(BlockUtil.getProtectBlockType(item))) return false;
        // check for player heads
        if (!checkNBT) return true; // if not checking nbt, you only need to check type

        boolean tag = false;

        // otherwise, check if the item was created by protection stones (stored in custom tag)
        if (item.getItemMeta() != null) {
            CustomItemTagContainer tagContainer = item.getItemMeta().getCustomTagContainer();
            try { // check if tag byte is 1
                Byte isPSBlock = tagContainer.getCustomTag(new NamespacedKey(ProtectionStones.getInstance(), "isPSBlock"), ItemTagType.BYTE);
                tag = isPSBlock != null && isPSBlock == 1;
            } catch (IllegalArgumentException es) {
                try { // some nbt data may be using a string (legacy nbt from ps version 2.0.0 -> 2.0.6)
                    String isPSBlock = tagContainer.getCustomTag(new NamespacedKey(ProtectionStones.getInstance(), "isPSBlock"), ItemTagType.STRING);
                    tag = isPSBlock != null && isPSBlock.equals("true");
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        return tag; // whether or not the nbt tag was found
    }

    /**
     * Check if an item is a valid protection block, and if the block type has restrict-obtaining on, check if it was
     * created by ProtectionStones (custom NBT tag). Be aware that blocks may have restrict-obtaining
     * off, meaning that it ignores whether or not the item is created by ProtectionStones.
     *
     * @param item     the item to check
     * @return whether or not the item is a valid protection block item, and was created by protection stones
     */

    public static boolean isProtectBlockItem(ItemStack item) {
        if (item == null) return false;
        PSProtectBlock b = ProtectionStones.getBlockOptions(BlockUtil.getProtectBlockType(item));
        if (b == null) return false;
        return isProtectBlockItem(item, b.restrictObtaining);
    }

    /**
     * Get a protection block item from a protect block config object.
     *
     * @param b the config options for the protection block
     * @return the item with NBT and other metadata to signify that it was created by protection stones
     */

    public static ItemStack createProtectBlockItem(PSProtectBlock b) {
        ItemStack is = BlockUtil.getProtectBlockItemFromType(b.type);

        // add enchant effect if enabled
        if (b.enchantedEffect) {
            is.addUnsafeEnchantment(Enchantment.LURE, 1);
        }

        ItemMeta im = is.getItemMeta();

        // add skull metadata, must be before others since it resets item metadata
        if (im instanceof SkullMeta && is.getType().equals(Material.PLAYER_HEAD) && b.type.split(":").length > 1) {
            is = BlockUtil.setHeadType(b.type, is);
            im = is.getItemMeta();
        }

        // set custom model data
        if (b.customModelData != -1) {
            im.setCustomModelData(b.customModelData);
        }

        // add display name and lore
        if (!b.displayName.equals("")) {
            im.setDisplayName(ChatColor.translateAlternateColorCodes('&', b.displayName));
        }
        List<String> lore = new ArrayList<>();
        for (String s : b.lore) lore.add(ChatColor.translateAlternateColorCodes('&', s));
        im.setLore(lore);

        // hide enchant name (cannot call addUnsafeEnchantment here)
        if (b.enchantedEffect) {
            im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        // add identifier for protection stone created items
        im.getCustomTagContainer().setCustomTag(new NamespacedKey(plugin, "isPSBlock"), ItemTagType.BYTE, (byte) 1);

        is.setItemMeta(im);

        return is;
    }

    // called on first start, and /ps reload
    public static void loadConfig(boolean isReload) {
        // remove old ps crafting recipes
        RecipeUtil.removePSRecipes();

        // init config
        PSConfig.initConfig();

        // init messages
        PSL.loadConfig();

        // init help menu
        ArgHelp.initHelpMenu();

        // load economy
        if (ProtectionStones.getInstance().economy != null) ProtectionStones.getInstance().economy.stop();
        ProtectionStones.getInstance().economy = new PSEconomy();

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

            } catch (Exception | NoSuchMethodError e) {
                ProtectionStones.getPluginLogger().severe("Unable to load plugin commands!");
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onLoad() {
        // register WG flags
        FlagHandler.registerFlags();
    }

    public @NotNull BukkitAudiences audiences() {
        if (adventure == null) {
            throw new IllegalStateException("Tried to access Adventure audiences while plugin is disabled!");
        }
        return adventure;
    }

    @Override

    public void onDisable() {
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    @Override
    public void onEnable() {
        this.adventure = BukkitAudiences.create(this);
        FlagHandler.registerHandlers(); // register custom WG flag handlers

        TomlFormat.instance();
        Config.setInsertionOrderPreserved(true); // make sure that config upgrades aren't a complete mess

        plugin = this;
        configLocation = new File(this.getDataFolder() + "/config.toml");
        blockDataFolder = new File(this.getDataFolder() + "/blocks");

        // metrics (bStats)
        // https://bstats.org/plugin/bukkit/ProtectionStones/4071
        new Metrics(this, 4071);

        // load command arguments
        PSCommand.addDefaultArguments();

        // register event listeners
        getServer().getPluginManager().registerEvents(new ListenerClass(), this);

        // check that WorldGuard and WorldEdit are enabled (WorldGuard will only be enabled if there's WorldEdit)
        if (getServer().getPluginManager().getPlugin("WorldGuard") == null || !getServer().getPluginManager().getPlugin("WorldGuard").isEnabled()) {
            getLogger().severe("WorldGuard or WorldEdit not enabled! Disabling ProtectionStones...");
            getServer().getPluginManager().disablePlugin(this);
        }

        // check if Vault is enabled (for economy support)
        if (getServer().getPluginManager().getPlugin("Vault") != null && getServer().getPluginManager().getPlugin("Vault").isEnabled()) {
            RegisteredServiceProvider<Economy> econ = getServer().getServicesManager().getRegistration(Economy.class);
            if (econ == null) {
                getLogger().warning("No economy plugin found by Vault! There will be no economy support!");
                vaultSupportEnabled = false;
            } else {
                vaultEconomy = econ.getProvider();
                vaultSupportEnabled = true;
            }
        } else {
            getLogger().warning("Vault not enabled! There will be no economy support!");
        }

        // check for PlaceholderAPI
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null && getServer().getPluginManager().getPlugin("PlaceholderAPI").isEnabled()) {
            getLogger().info("PlaceholderAPI support enabled!");
            placeholderAPISupportEnabled = true;
            new PSPlaceholderExpansion().register();
        } else {
            getLogger().info("PlaceholderAPI not found! There will be no PlaceholderAPI support.");
        }

        // check for LuckPerms
        if (getServer().getPluginManager().getPlugin("LuckPerms") != null && getServer().getPluginManager().getPlugin("LuckPerms").isEnabled()) {
            try {
                luckPermsSupportEnabled = true;
                luckPerms = getServer().getServicesManager().load(LuckPerms.class);
                getLogger().info("LuckPerms support enabled!");
            } catch (NoClassDefFoundError err) { // incompatible luckperms api
                getLogger().warning("Incompatible LuckPerms version found! Please upgrade your LuckPerms!");
            }
        }

        // load configuration
        loadConfig(false);

        // register protectionstones.flags.edit.[flag] permission
        FlagHandler.initializePermissions();

        // build up region cache
        getLogger().info("Building region cache...");

        HashMap<World, RegionManager> regionManagers = WGUtils.getAllRegionManagers();
        for (World w : regionManagers.keySet()) {
            RegionManager rgm = regionManagers.get(w);
            HashMap<String, ArrayList<String>> m = new HashMap<>();
            for (ProtectedRegion r : rgm.getRegions().values()) {
                String name = r.getFlag(FlagHandler.PS_NAME);
                if (isPSRegion(r) && name != null) {
                    if (m.containsKey(name)) {
                        m.get(name).add(r.getId());
                    } else {
                        m.put(name, new ArrayList<>(Collections.singletonList(r.getId())));
                    }
                }
            }
            regionNameToID.put(w.getUID(), m);
        }

        // uuid cache
        getLogger().info("Building UUID cache... (if slow change async-load-uuid-cache in the config to true)");
        if (configOptions.asyncLoadUUIDCache) { // async load
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
                    UUIDCache.storeUUIDNamePair(op.getUniqueId(), op.getName());
                }
            });
        } else { // sync load
            for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
                UUIDCache.storeUUIDNamePair(op.getUniqueId(), op.getName());
            }
        }

        // check if UUIDs have been upgraded already
        getLogger().info("Checking if PS regions have been updated to UUIDs...");

        // update to UUIDs
        if (configOptions.uuidupdated == null || !configOptions.uuidupdated)
            LegacyUpgrade.convertToUUID();

        if (configOptions.regionNegativeMinMaxUpdated == null || !configOptions.regionNegativeMinMaxUpdated)
            LegacyUpgrade.upgradeRegionsWithNegativeYValues();

        getLogger().info(ChatColor.WHITE + "ProtectionStones has successfully started!");
    }

}