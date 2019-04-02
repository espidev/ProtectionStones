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

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.vik1395.ProtectionStones.commands.*;
import org.bstats.bukkit.Metrics;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProtectionStones extends JavaPlugin {
    public static Map<UUID, String> uuidToName = new HashMap<>();

    public static Plugin plugin, wgd;
    public static File psStoneData;
    public static File conf;

    public static Metrics metrics;

    public static FileConfiguration config;
    public static List<String> flags = new ArrayList<>();
    public static List<String> toggleList = new ArrayList<>();
    public static List<String> allowedFlags = new ArrayList<>();
    public static List<String> deniedWorlds = new ArrayList<>();
    public static HashMap<String, ConfigProtectBlock> protectionStonesOptions = new HashMap<>();
    public static Collection<String> mats = new HashSet<>();
    public static int priority;
    public Map<CommandSender, Integer> viewTaskList;

    public static boolean isCooldownEnable = false;
    public static int cooldown = 0;
    public static String cooldownMessage = null;

    public static Plugin getPlugin() {
        return plugin;
    }

    public static RegionManager getRegionManagerWithPlayer(Player p) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(p.getWorld()));
    }

    // Helper method to get the config options for a protection stone
    // Makes code look cleaner
    public static ConfigProtectBlock getProtectStoneOptions(String blockType) {
        return protectionStonesOptions.get(blockType);
    }

    // Turn WG region name into a location (ex. ps138x35y358z i think)
    public static PSLocation parsePSRegionToLocation(String regionName) {
        int psx = Integer.parseInt(regionName.substring(2, regionName.indexOf("x")));
        int psy = Integer.parseInt(regionName.substring(regionName.indexOf("x") + 1, regionName.indexOf("y")));
        int psz = Integer.parseInt(regionName.substring(regionName.indexOf("y") + 1, regionName.length() - 1));
        return new PSLocation(psx, psy, psz);
    }

    // Helper method to either remove, disown or regen a player's ps region
    // NOTE: be sure to save the region manager after
    public static void removeDisownRegenPSRegion(LocalPlayer lp, String arg, String region, RegionManager rgm, Player admin) {
        ProtectedRegion r = rgm.getRegion(region);
        switch (arg) {
            case "disown":
                DefaultDomain owners = r.getOwners();
                owners.removePlayer(lp);
                r.setOwners(owners);
                break;
            case "regen":
                Bukkit.dispatchCommand(admin, "region select " + region);
                Bukkit.dispatchCommand(admin, "/regen");
                rgm.removeRegion(region);
                break;
            case "remove":
                if (region.substring(0, 2).equals("ps")) {
                    PSLocation psl = ProtectionStones.parsePSRegionToLocation(region);
                    Block blockToRemove = admin.getWorld().getBlockAt(psl.x, psl.y, psl.z); //TODO getWorld might not work
                    blockToRemove.setType(Material.AIR);
                }
                rgm.removeRegion(region);
                break;
        }
    }


    // plugin enable
    @Override
    public void onEnable() {
        viewTaskList = new HashMap<>();
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        plugin = this;
        conf = new File(this.getDataFolder() + "/config.yml");
        psStoneData = new File(this.getDataFolder() + "/hiddenpstones.yml");

        // Metrics (bStats)
        metrics = new Metrics(this);

        // generate protection stones stored blocks file
        if (!psStoneData.exists()) {
            try {
                ProtectionStones.psStoneData.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(ProtectionStones.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // register event listeners
        getServer().getPluginManager().registerEvents(new ListenerClass(), this);

        // check that WorldGuard and WorldEdit are enabled (Worldguard will only be enabled if there's worldedit)
        if (getServer().getPluginManager().getPlugin("WorldGuard").isEnabled()) {
            wgd = getServer().getPluginManager().getPlugin("WorldGuard");
        } else {
            getLogger().info("WorldGuard or WorldEdit not enabled! Disabling ProtectionStones...");
            getServer().getPluginManager().disablePlugin(this);
        }

        // add block types
        for (String material : this.getConfig().getString("Blocks").split(",")) {
            if (Material.getMaterial(material) == null) {
                Bukkit.getLogger().info("Unrecognized block: " + material + ". Please make sure you have updated your block name for 1.13!");
            } else {
                mats.add(material.toUpperCase());
            }
        }

        // init config
        Config.initConfig();

        // init messages
        PSL.loadConfig();

        flags = getConfig().getStringList("Flags");
        allowedFlags = Arrays.asList((getConfig().getString("Allowed Flags").toLowerCase()).split(","));
        deniedWorlds = getConfig().getStringList("Worlds Denied");

        getLogger().info("Placing of Protection Stones is disabled in the following worlds (override with protectionstones.admin): ");
        for (String world : deniedWorlds) {
            getLogger().info("- " + world);
        }

        isCooldownEnable = getConfig().getBoolean("cooldown.enable");
        cooldown = getConfig().getInt("cooldown.cooldown") * 1000;
        cooldownMessage = getConfig().getString("cooldown.message");

        // uuid cache
        getLogger().info("Building UUID cache...");
        for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
            uuidToName.put(op.getUniqueId(), op.getName());
        }

        // check if uuids have been upgraded already
        getLogger().info("Checking if PS regions have been updated to UUIDs...");
        // Update to UUIDs
        if (!getConfig().contains("UUIDUpdated", true) || !getConfig().getBoolean("UUIDUpdated")) {
            getLogger().info("Updating PS regions to UUIDs...");
            for (World world : Bukkit.getWorlds()) {
                RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));

                // iterate over regions in world
                for (String regionName : rm.getRegions().keySet()) {
                    if (regionName.startsWith("ps")) {
                        ProtectedRegion region = rm.getRegion(regionName);

                        // convert owners with player names to UUIDs
                        List<String> owners = new ArrayList<>(), members = new ArrayList<>();
                        owners.addAll(region.getOwners().getPlayers());
                        members.addAll(region.getMembers().getPlayers());

                        // convert
                        for (String owner : owners) {
                            UUID uuid = nameToUUID(owner);
                            region.getOwners().removePlayer(owner);
                            region.getOwners().addPlayer(uuid);
                        }
                        for (String member : members) {
                            UUID uuid = nameToUUID(member);
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
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(conf, true));
                writer.write("\nUUIDUpdated: true");
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            getLogger().info("Done!");
        }

        getLogger().info("ProtectionStones has successfully started!");
    }

    private static UUID nameToUUID(String name) {
        return Bukkit.getOfflinePlayer(name).getUniqueId();
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (s instanceof Player) {
            Player p = (Player) s;
            if (cmd.getName().equalsIgnoreCase("ps")) {
                if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
                    p.sendMessage(ChatColor.YELLOW + "/ps info members|owners|flags");//\\
                    p.sendMessage(ChatColor.YELLOW + "/ps add|remove {playername}");//\\
                    p.sendMessage(ChatColor.YELLOW + "/ps addowner|removeowner {playername}");//\\
                    p.sendMessage(ChatColor.YELLOW + "/ps count [player]");//\\
                    p.sendMessage(ChatColor.YELLOW + "/ps flag {flagname} {setting|null}");//\\
                    p.sendMessage(ChatColor.YELLOW + "/ps home {num} - " + ChatColor.GREEN + "{num} has to be within the number of protected regions you own. Use /ps count to check");
                    p.sendMessage(ChatColor.YELLOW + "/ps tp {player} {num}");
                    p.sendMessage(ChatColor.YELLOW + "/ps hide|unhide");//\\
                    p.sendMessage(ChatColor.YELLOW + "/ps toggle");//\\
                    p.sendMessage(ChatColor.YELLOW + "/ps view");//\\
                    p.sendMessage(ChatColor.YELLOW + "/ps unclaim");//\\
                    p.sendMessage(ChatColor.YELLOW + "/ps priority {number|null}");//\\
                    p.sendMessage(ChatColor.YELLOW + "/ps region count|list|remove|regen|disown {playername}");//\\
                    p.sendMessage(ChatColor.YELLOW + "/ps admin { version | settings | hide | unhide |");//\\
                    p.sendMessage(ChatColor.YELLOW + "           cleanup | lastlogon | lastlogons | stats }");//\\
                    p.sendMessage(ChatColor.YELLOW + "/ps bypass");//\\
                    return true;
                }

                // Find the id of the current region the player is in and get WorldGuard player object for use later
                BlockVector3 v = BlockVector3.at(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());
                String currentPSID;
                RegionManager rgm = getRegionManagerWithPlayer(p);
                List<String> idList = rgm.getApplicableRegionsIDs(v);
                if (idList.size() == 1) {
                    currentPSID = idList.toString().substring(1, idList.toString().length() - 1);
                } else {
                    // Get nearest protection stone if in overlapping region
                    double distanceToPS = 10000D, tempToPS;
                    String namePSID = "";
                    for (String currentID : idList) {
                        if (currentID.substring(0, 2).equals("ps")) {
                            PSLocation psl = parsePSRegionToLocation(currentID);
                            Location psLocation = new Location(p.getWorld(), psl.x, psl.y, psl.z);
                            tempToPS = p.getLocation().distance(psLocation);
                            if (tempToPS < distanceToPS) {
                                distanceToPS = tempToPS;
                                namePSID = currentID;
                            }
                        }
                    }
                    currentPSID = namePSID;
                }

                switch (args[0].toLowerCase()) {
                    case "toggle":
                        if (p.hasPermission("protectionstones.toggle")) {
                            if (!toggleList.contains(p.getName())) {
                                toggleList.add(p.getName());
                                p.sendMessage(ChatColor.YELLOW + "ProtectionStone placement turned off");
                            } else {
                                toggleList.remove(p.getName());
                                p.sendMessage(ChatColor.YELLOW + "ProtectionStone placement turned on");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "You don't have permission to use the toggle command.");
                        }
                        break;
                    case "count":
                        return ArgCount.argumentCount(p, args);
                    case "region":
                        return ArgRegion.argumentRegion(p, args);
                    case "tp":
                        return ArgTp.argumentTp(p, args);
                    case "home":
                        return ArgTp.argumentTp(p, args);
                    case "admin":
                        return ArgAdmin.argumentAdmin(p, args);
                    case "unclaim":
                        return ArgUnclaim.argumentUnclaim(p, args, currentPSID);
                    case "bypass":
                        return ArgBypass.argumentBypass(p, args);
                    case "add":
                        return ArgAddRemove.template(p, args, currentPSID, "add");
                    case "remove":
                        return ArgAddRemove.template(p, args, currentPSID, "remove");
                    case "addowner":
                        return ArgAddRemove.template(p, args, currentPSID, "addowner");
                    case "removeowner":
                        return ArgAddRemove.template(p, args, currentPSID, "removeowner");
                    case "view":
                        return ArgView.argumentView(p, args, currentPSID);
                    case "unhide":
                        return ArgHideUnhide.template(p, "unhide", currentPSID);
                    case "hide":
                        return ArgHideUnhide.template(p, "hide", currentPSID);
                    case "priority":
                        return ArgPriority.argPriority(p, args, currentPSID);
                    case "flag":
                        return ArgFlag.argumentFlag(p, args, currentPSID);
                    case "info":
                        return ArgInfo.argumentInfo(p, args, currentPSID);
                    default:
                        p.sendMessage(PSL.NO_SUCH_COMMAND.msg());
                }
            }
        } else {
            s.sendMessage(ChatColor.RED + "PS cannot be used from the console.");
        }
        return true;
    }

    public static boolean hasNoAccess(ProtectedRegion region, Player p, LocalPlayer lp, boolean canBeMember) {
        // Region is not valid
        if (region == null) return true;

        return !p.hasPermission("protectionstones.superowner") && !region.isOwner(lp) && (!canBeMember || !region.isMember(lp));
    }

}