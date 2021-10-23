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

package dev.espi.protectionstones.utils.upgrade;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.*;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class LegacyUpgrade {

    // upgrade to 1.17, upgrade regions with 0->256 to SHORT_MIN->SHORT_MAX
    public static void upgradeRegionsWithNegativeYValues() {
        ProtectionStones.getInstance().getLogger().info("Upgrading region y-values for 1.17...");
        for (RegionManager rgm : WGUtils.getAllRegionManagers().values()) {
            List<ProtectedRegion> newRegions = new ArrayList<>();

            // loop through each region
            for (var region : rgm.getRegions().values()) {
                int minY = region.getMinimumPoint().getBlockY(), maxY = region.getMaximumPoint().getBlockY();
                if (ProtectionStones.isPSRegion(region) && minY == 0 && maxY == 256) {
                    // clone region, and recreate with new min/max points
                    ProtectedRegion toAdd = null;
                    if (region instanceof ProtectedPolygonalRegion) { // convert merged region
                        toAdd = new ProtectedPolygonalRegion(region.getId(), region.getPoints(), WGUtils.MIN_BUILD_HEIGHT, WGUtils.MAX_BUILD_HEIGHT);
                    } else if (region instanceof ProtectedCuboidRegion) { // convert standard region
                        BlockVector3 minVec = BlockVector3.at(region.getMinimumPoint().getX(), WGUtils.MIN_BUILD_HEIGHT, region.getMinimumPoint().getZ()),
                                     maxVec = BlockVector3.at(region.getMaximumPoint().getX(), WGUtils.MAX_BUILD_HEIGHT, region.getMaximumPoint().getZ());
                        toAdd = new ProtectedCuboidRegion(region.getId(), minVec, maxVec);
                    }

                    if (toAdd != null) {
                        ProtectionStones.getInstance().getLogger().info("Updated region " + region.getId());
                        toAdd.copyFrom(region); // copy region settings
                        newRegions.add(toAdd);
                    }

                } else {
                    newRegions.add(region);
                }
            }

            rgm.setRegions(newRegions);
            try {
                rgm.save();
            } catch (StorageException e) {
                e.printStackTrace();
            }
        }
        // update config to mark that uuid upgrade has been done
        ProtectionStones.config.set("region_negative_min_max_updated", true);
        ProtectionStones.config.save();
        ProtectionStones.getInstance().getLogger().info("Finished!");
    }

    // for one day when we switch to proper base64 generation (no hashcode, use nameuuidfrombytes)
    // problem is, currently I don't know how to convert all items to use this uuid
    public static void fixBase64HeadRegions() {

        HashMap<String, String> oldToNew = new HashMap<>();

        for (PSProtectBlock b : ProtectionStones.getInstance().getConfiguredBlocks()) {
            if (b.type.startsWith("PLAYER_HEAD:") && b.type.split(":").length > 1) {
                String base64 = b.type.split(":")[1];
                oldToNew.put(new UUID(base64.hashCode(), base64.hashCode()).toString(), UUID.nameUUIDFromBytes(base64.getBytes()).toString());
            }
        }

        for (World world : Bukkit.getWorlds()) {
            RegionManager rm = WGUtils.getRegionManagerWithWorld(world);
            for (ProtectedRegion r : rm.getRegions().values()) {
                if (ProtectionStones.isPSRegion(r)) {
                    PSRegion psr = PSRegion.fromWGRegion(world, r);

                    if (psr instanceof PSGroupRegion) {
                        PSGroupRegion psgr = (PSGroupRegion) psr;
                        for (PSMergedRegion psmr : psgr.getMergedRegions()) {

                            String type = psmr.getType();
                            if (oldToNew.containsKey(type)) {
                                Set<String> flag = psmr.getGroupRegion().getWGRegion().getFlag(FlagHandler.PS_MERGED_REGIONS_TYPES);
                                String original = null;
                                for (String s : flag) {
                                    String[] spl = s.split(" ");
                                    String id = spl[0];
                                    if (id.equals(psmr.getId())) {
                                        original = s;
                                        break;
                                    }
                                }

                                if (original != null) {
                                    flag.remove(original);
                                    flag.add(psmr.getId() + " " + oldToNew.get(type));
                                }
                            }
                        }
                    }

                    if (oldToNew.containsKey(psr.getType())) {
                        psr.getWGRegion().setFlag(FlagHandler.PS_BLOCK_MATERIAL, oldToNew.get(psr.getType()));
                    }

                }
            }
            try {
                rm.save();
            } catch (StorageException e) {
                e.printStackTrace();
            }
        }
    }

    // check that all of the PS custom flags are in ps regions and upgrade if not
    // originally used for the v1 -> v2 transition
    public static void upgradeRegions() {

        YamlConfiguration hideFile = null;
        if (new File(ProtectionStones.getInstance().getDataFolder() + "/hiddenpstones.yml").exists()) {
            hideFile = YamlConfiguration.loadConfiguration(new File(ProtectionStones.getInstance().getDataFolder() + "/hiddenpstones.yml"));
        }
        for (World world : Bukkit.getWorlds()) {
            RegionManager rm = WGUtils.getRegionManagerWithWorld(world);
            for (String regionName : rm.getRegions().keySet()) {
                if (regionName.startsWith("ps") && !ProtectionStones.isPSRegion(rm.getRegion(regionName))) {
                    try {
                        PSLocation psl = WGUtils.parsePSRegionToLocation(regionName);
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
                            if (ProtectionStones.isProtectBlockType(material)) {
                                PSProtectBlock cpb = ProtectionStones.getBlockOptions(material);
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
    public static void convertToUUID() {
        Bukkit.getLogger().info("Updating PS regions to UUIDs...");
        for (World world : Bukkit.getWorlds()) {
            RegionManager rm = WGUtils.getRegionManagerWithWorld(world);

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
        ProtectionStones.config.set("uuidupdated", true);
        ProtectionStones.config.save();
        Bukkit.getLogger().info("Done!");
    }

    // upgrade from config < v2.0.0
    public static void upgradeFromV1V2() {
        Bukkit.getLogger().info(ChatColor.AQUA + "Upgrading configs from v1.x to v2.0+...");

        try {
            ProtectionStones.blockDataFolder.mkdir();
            Files.copy(PSConfig.class.getResourceAsStream("/config.toml"), Paths.get(ProtectionStones.configLocation.toURI()), StandardCopyOption.REPLACE_EXISTING);

            FileConfig fc = FileConfig.builder(ProtectionStones.configLocation).build();
            fc.load();

            File oldConfig = new File(ProtectionStones.getInstance().getDataFolder() + "/config.yml");
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(oldConfig);

            fc.set("uuidupdated", (yml.get("UUIDUpdated") != null) && yml.getBoolean("UUIDUpdated"));
            fc.set("placing_cooldown", (yml.getBoolean("cooldown.enable")) ? yml.getInt("cooldown.cooldown") : -1);

            // options from global scope
            List<String> worldsDenied = yml.getStringList("Worlds Denied");
            List<String> flags = yml.getStringList("Flags");
            List<String> allowedFlags = new ArrayList<>(Arrays.asList(yml.getString("Allowed Flags").split(",")));

            // upgrade blocks
            for (String type : yml.getConfigurationSection("Region").getKeys(false)) {
                File file = new File(ProtectionStones.blockDataFolder.getAbsolutePath() + "/" + type + ".toml");
                Files.copy(PSConfig.class.getResourceAsStream("/block1.toml"), Paths.get(file.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
                FileConfig b = FileConfig.builder(file).build();
                b.load();

                b.set("type", type);
                b.set("alias", type);
                b.set("description", yml.getInt("Region." + type + ".X Radius") + " radius protected area.");
                b.set("restrict_obtaining", false);
                b.set("world_list_type", "blacklist");
                b.set("worlds", worldsDenied);
                b.set("region.x_radius", yml.getInt("Region." + type + ".X Radius"));
                b.set("region.y_radius", yml.getInt("Region." + type + ".Y Radius"));
                b.set("region.z_radius", yml.getInt("Region." + type + ".Z Radius"));
                b.set("region.flags", flags);
                b.set("region.allowed_flags", allowedFlags);
                b.set("region.priority", yml.getInt("Region." + type + ".Priority"));
                b.set("block_data.display_name", "");
                b.set("block_data.lore", Arrays.asList());
                b.set("behaviour.auto_hide", yml.getBoolean("Region." + type + ".Auto Hide"));
                b.set("behaviour.no_drop", yml.getBoolean("Region." + type + ".No Drop"));
                b.set("behaviour.prevent_piston_push", yml.getBoolean("Region." + type + ".Block Piston"));
                // ignore silk touch option
                b.set("player.prevent_teleport_in", yml.getBoolean("Teleport To PVP.Block Teleport"));

                b.save();
                b.close();
            }

            fc.save();
            fc.close();

            oldConfig.renameTo(new File(ProtectionStones.getInstance().getDataFolder() + "/config.yml.old"));

        } catch (IOException e) {
            e.printStackTrace();
        }

        Bukkit.getLogger().info(ChatColor.GREEN + "Done!");
        Bukkit.getLogger().info(ChatColor.GREEN + "Please be sure to double check your configs with the new options!");

        Bukkit.getLogger().info(ChatColor.AQUA + "Updating PS Regions to new format...");
        LegacyUpgrade.upgradeRegions();
        Bukkit.getLogger().info(ChatColor.GREEN + "Done!");
    }
}
