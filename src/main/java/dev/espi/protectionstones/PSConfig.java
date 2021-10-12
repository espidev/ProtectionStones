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

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.espi.protectionstones.utils.BlockUtil;
import dev.espi.protectionstones.utils.upgrade.ConfigUpgrades;
import dev.espi.protectionstones.utils.upgrade.LegacyUpgrade;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.apache.commons.io.IOUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the global config (config.toml) settings.
 */

public class PSConfig {

    // config options
    // config.toml will be loaded into these fields
    // uses autoboxing for primitives to allow for null (because of the way config upgrades work)
    @Path("config_version")
    public int configVersion;
    @Path("uuidupdated")
    public Boolean uuidupdated;
    @Path("region_negative_min_max_updated")
    public Boolean regionNegativeMinMaxUpdated;
    @Path("placing_cooldown")
    public Integer placingCooldown;
    @Path("allow_duplicate_region_names")
    public Boolean allowDuplicateRegionNames;
    @Path("async_load_uuid_cache")
    public Boolean asyncLoadUUIDCache;
    @Path("ps_view_cooldown")
    public Integer psViewCooldown;
    @Path("base_command")
    public String base_command;
    @Path("aliases")
    public List<String> aliases;
    @Path("drop_item_when_inventory_full")
    public Boolean dropItemWhenInventoryFull;
    @Path("regions_must_be_adjacent")
    public Boolean regionsMustBeAdjacent;
    @Path("allow_merging_regions")
    public Boolean allowMergingRegions;
    @Path("allow_merging_holes")
    public Boolean allowMergingHoles;
    @Path("default_protection_block_placement_off")
    public Boolean defaultProtectionBlockPlacementOff;
    @Path("allow_addowner_for_offline_players_without_lp")
    public Boolean allowAddownerForOfflinePlayersWithoutLp;
    @Path("allow_home_teleport_for_members")
    public Boolean allowHomeTeleportForMembers;

    @Path("admin.cleanup_delete_regions_with_members_but_no_owners")
    public Boolean cleanupDeleteRegionsWithMembersButNoOwners;

    @Path("economy.max_rent_price")
    public Double maxRentPrice;
    @Path("economy.min_rent_price")
    public Double minRentPrice;
    @Path("economy.max_rent_period")
    public Integer maxRentPeriod;
    @Path("economy.min_rent_period")
    public Integer minRentPeriod;
    @Path("economy.tax_enabled")
    public Boolean taxEnabled;
    @Path("economy.tax_message_on_join")
    public Boolean taxMessageOnJoin;

    static void initConfig() {

        // check if using config v1 or v2 (config.yml -> config.toml)
        if (new File(ProtectionStones.getInstance().getDataFolder() + "/config.yml").exists() && !ProtectionStones.configLocation.exists()) {
            LegacyUpgrade.upgradeFromV1V2();
        }

        // check if config files exist
        try {
            if (!ProtectionStones.getInstance().getDataFolder().exists()) {
                ProtectionStones.getInstance().getDataFolder().mkdir();
            }
            if (!ProtectionStones.blockDataFolder.exists()) {
                ProtectionStones.blockDataFolder.mkdir();
                Files.copy(PSConfig.class.getResourceAsStream("/block1.toml"), Paths.get(ProtectionStones.blockDataFolder.getAbsolutePath() + "/block1.toml"), StandardCopyOption.REPLACE_EXISTING);
            }
            if (!ProtectionStones.configLocation.exists()) {
                Files.copy(PSConfig.class.getResourceAsStream("/config.toml"), Paths.get(ProtectionStones.configLocation.toURI()), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            Logger.getLogger(ProtectionStones.class.getName()).log(Level.SEVERE, null, ex);
        }

        // keep in mind that there is /ps reload, so clear arrays before adding config options!
        // clear data (for /ps reload)
        ProtectionStones.protectionStonesOptions.clear();

        // create config object
        if (ProtectionStones.config == null) {
            ProtectionStones.config = CommentedFileConfig.builder(ProtectionStones.configLocation).sync().build();
        }

        // loop upgrades until the config has been updated to the latest version
        do {
            ProtectionStones.config.load(); // load latest settings

            // load config into configOptions object
            ProtectionStones.getInstance().setConfigOptions(new ObjectConverter().toObject(ProtectionStones.config, PSConfig::new));

            // upgrade config if need be (v3+)
            boolean leaveLoop = ConfigUpgrades.doConfigUpgrades();
            if (leaveLoop) break; // leave loop if config version is correct

            // save config if upgrading
            ProtectionStones.config.save();
        } while (true);

        // load protection stones to options map
        if (ProtectionStones.blockDataFolder.listFiles().length == 0) {
            ProtectionStones.getPluginLogger().warning("The blocks folder is empty! You do not have any protection blocks configured!");
        } else {

            // temp file to load in default ps block config
            File tempFile;
            try {
                tempFile = File.createTempFile("psconfigtemp", ".toml");
                try (FileOutputStream out = new FileOutputStream(tempFile)) {
                    IOUtils.copy(PSConfig.class.getResourceAsStream("/block1.toml"), out);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            CommentedFileConfig template = CommentedFileConfig.of(tempFile);
            template.load();

            // iterate over block files and load into map
            ProtectionStones.getPluginLogger().info("Protection Stone Blocks:");
            for (File file : ProtectionStones.blockDataFolder.listFiles()) {

                CommentedFileConfig c = CommentedFileConfig.builder(file).sync().build();
                c.load();

                // check to make sure all options are not null
                boolean updated = false;
                for (String str : template.valueMap().keySet()) {
                    if (c.get(str) == null) {
                        c.set(str, template.get(str));
                        c.setComment(str, template.getComment(str));
                        updated = true;
                    } else if (c.get(str) instanceof CommentedConfig) {
                        // no DFS for now (since there's only 2 layers of config)
                        CommentedConfig template2 = template.get(str);
                        CommentedConfig c2 = c.get(str);
                        for (String str2 : template2.valueMap().keySet()) {
                            if (c2.get(str2) == null) {
                                c2.add(str2, template2.get(str2));
                                c2.setComment(str2, template2.getComment(str2));
                                updated = true;
                            }
                        }
                    }
                }
                if (updated) c.save();

                // convert toml data into object
                PSProtectBlock b = new ObjectConverter().toObject(c, PSProtectBlock::new);

                // check if material is valid, and is not a player head (since player heads also have the player name after)
                if (Material.getMaterial(b.type) == null && !(b.type.startsWith(Material.PLAYER_HEAD.toString()))) {
                    ProtectionStones.getPluginLogger().warning("Unrecognized material: " + b.type);
                    ProtectionStones.getPluginLogger().warning("Block will not be added. Please fix this in your config.");
                    continue;
                }

                // check for duplicates
                if (ProtectionStones.isProtectBlockType(b.type)) {
                    ProtectionStones.getPluginLogger().warning("Duplicate block type found! Ignoring the extra block " + b.type);
                    continue;
                }
                if (ProtectionStones.getProtectBlockFromAlias(b.alias) != null) {
                    ProtectionStones.getPluginLogger().warning("Duplicate block alias found! Ignoring the extra block " + b.alias);
                    continue;
                }

                ProtectionStones.getPluginLogger().info("- " + b.type + " (" + b.alias + ")");
                FlagHandler.initDefaultFlagsForBlock(b); // process flags for block and set regionFlags field

                // for PLAYER_HEAD:base64, we need to change the entry to link to a UUID hash instead of storing the giant base64
                if (BlockUtil.isBase64PSHead(b.type)) {
                    String nuuid = BlockUtil.getUUIDFromBase64PS(b);

                    BlockUtil.uuidToBase64Head.put(nuuid, b.type.split(":")[1]);
                    b.type = "PLAYER_HEAD:" + nuuid;
                }

                ProtectionStones.protectionStonesOptions.put(b.type, b); // add block
            }

            // cleanup temp file
            template.close();
            tempFile.delete();

            // setup crafting recipes for all blocks
            setupRecipes();
        }

    }

    static void removePSRecipes() {
        // remove previous protectionstones recipes (/ps reload)
        Iterator<Recipe> iter = Bukkit.getServer().recipeIterator();
        while (iter.hasNext()) {
            try {
                Recipe r = iter.next();
                if (r instanceof ShapedRecipe && (((ShapedRecipe) r).getKey().getNamespace().equalsIgnoreCase("protectionstones"))) {
                    iter.remove();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static void setupRecipes() {
        for (PSProtectBlock b : ProtectionStones.getInstance().getConfiguredBlocks()) {
            // add custom recipes to Bukkit
            if (b.allowCraftWithCustomRecipe) {
                setupRecipe(b);
            }
        }
    }

    private static void setupRecipe(PSProtectBlock b) {
        // create item
        ItemStack item = b.createItem();
        item.setAmount(b.recipeAmount);

        // create recipe
        // key must adhere to [a-z0-9/._-]
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(ProtectionStones.getInstance(), b.type.replaceAll("[+/=:]", "")), item);
        HashMap<String, Character> items = new HashMap<>();
        List<String> recipeLine = new ArrayList<>();
        char id = 'a';
        for (int i = 0; i < b.customRecipe.size(); i++) {
            recipeLine.add("");
            for (String mat : b.customRecipe.get(i)) {
                if (mat.equals("")) {
                    recipeLine.set(i, recipeLine.get(i) + " ");
                } else {
                    if (items.get(mat) == null) {
                        items.put(mat, id++);
                    }
                    recipeLine.set(i, recipeLine.get(i) + items.get(mat));
                }
            }
        }

        // recipe
        recipe.shape(recipeLine.toArray(new String[0]));
        for (String mat : items.keySet()) {
            if (Material.matchMaterial(mat) != null) { // general material type

                recipe.setIngredient(items.get(mat), Material.matchMaterial(mat));

            } else if (mat.startsWith("PROTECTION_STONES:")) { // ProtectionStones block

                // format PROTECTION_STONES:alias
                String alias = mat.substring(mat.indexOf(":") + 1);
                PSProtectBlock use = ProtectionStones.getProtectBlockFromAlias(alias);
                if (use != null && use.createItem() != null) {
                    recipe.setIngredient(items.get(mat), new RecipeChoice.ExactChoice(use.createItem()));
                } else {
                    ProtectionStones.getPluginLogger().warning("Unable to resolve material " + mat + " for the crafting recipe for " + b.alias + ".");
                }

            } else {
                ProtectionStones.getPluginLogger().warning("Unable to find material " + mat + " for the crafting recipe for " + b.alias + ".");
            }
        }
        try {
            Bukkit.addRecipe(recipe);
        } catch (IllegalStateException e) {
            ProtectionStones.getPluginLogger().warning("Reloading custom recipes does not work right now, you have to restart the server for updated recipes.");
        }
    }

}
