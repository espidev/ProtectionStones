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
import me.vik1395.ProtectionStones.commands.*;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bstats.bukkit.Metrics;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.List;

public class ProtectionStones extends JavaPlugin {
    public static Map<UUID, String> uuidToName = new HashMap<>();
    public static Map<String, UUID> nameToUUID = new HashMap<>();

    public static Plugin plugin, wgd;
    public static File configLocation, blockDataFolder;

    public static Metrics metrics;

    // all configuration file options are stored in here
    public static Config configOptions;
    public static FileConfig config;
    // block options
    public static HashMap<String, ConfigProtectBlock> protectionStonesOptions = new HashMap<>();

    public static List<String> toggleList = new ArrayList<>();

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
        im.getCustomTagContainer().setCustomTag(new NamespacedKey(plugin, "isPSBlock"), ItemTagType.STRING, "true");

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

    // Helper method to either remove, disown or regen a player's ps region
    // NOTE: be sure to save the region manager after
    public static void removeDisownPSRegion(LocalPlayer lp, String arg, String region, RegionManager rgm, Player admin) {
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
                    Block blockToRemove = admin.getWorld().getBlockAt(psl.x, psl.y, psl.z); //TODO getWorld might not work
                    blockToRemove.setType(Material.AIR);
                }
                rgm.removeRegion(region);
                break;
        }
    }


    // called on first start, and /ps reload
    public static void loadConfig() {
        // init config
        Config.initConfig();

        // init messages
        PSL.loadConfig();
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
        if (getServer().getPluginManager().getPlugin("WorldGuard").isEnabled()) {
            wgd = getServer().getPluginManager().getPlugin("WorldGuard");
        } else {
            getServer().getConsoleSender().sendMessage("WorldGuard or WorldEdit not enabled! Disabling ProtectionStones...");
            getServer().getPluginManager().disablePlugin(this);
        }

        // Load configuration
        loadConfig();

        // register permissions
        Bukkit.getPluginManager().addPermission(new Permission("protectionstones.create"));
        Bukkit.getPluginManager().addPermission(new Permission("protectionstones.destroy"));
        Bukkit.getPluginManager().addPermission(new Permission("protectionstones.unclaim"));
        Bukkit.getPluginManager().addPermission(new Permission("protectionstones.view"));
        Bukkit.getPluginManager().addPermission(new Permission("protectionstones.info"));
        Bukkit.getPluginManager().addPermission(new Permission("protectionstones.get"));
        Bukkit.getPluginManager().addPermission(new Permission("protectionstones.give"));
        Bukkit.getPluginManager().addPermission(new Permission("protectionstones.count"));
        Bukkit.getPluginManager().addPermission(new Permission("protectionstones.count.others"));
        Bukkit.getPluginManager().addPermission(new Permission("protectionstones.hide"));
        Bukkit.getPluginManager().addPermission(new Permission("protectionstones.unhide"));
        Bukkit.getPluginManager().addPermission(new Permission("protectionstones.sethome"));
        Bukkit.getPluginManager().addPermission(new Permission("protectionstones.home"));
        Bukkit.getPluginManager().addPermission(new Permission("protectionstones.tp"));
        Bukkit.getPluginManager().addPermission(new Permission("protectionstones.tp.bypassprevent"));
        Bukkit.getPluginManager().addPermission(new Permission("protectionstones.tp.bypasswait"));
        Bukkit.getPluginManager().addPermission(new Permission("protectionstones.priority"));
        Bukkit.getPluginManager().addPermission(new Permission("protectionstones.owners"));
        Bukkit.getPluginManager().addPermission(new Permission("protectionstones.members"));
        Bukkit.getPluginManager().addPermission(new Permission("protectionstones.flags"));
        Bukkit.getPluginManager().addPermission(new Permission("protectionstones.toggle"));
        Bukkit.getPluginManager().addPermission(new Permission("protectionstones.region"));
        Bukkit.getPluginManager().addPermission(new Permission("protectionstones.admin"));
        Bukkit.getPluginManager().addPermission(new Permission("protectionstones.bypass"));
        Bukkit.getPluginManager().addPermission(new Permission("protectionstones.superowner"));

        // uuid cache
        getServer().getConsoleSender().sendMessage("Building UUID cache...");
        for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
            uuidToName.put(op.getUniqueId(), op.getName());
            nameToUUID.put(op.getName(), op.getUniqueId());
        }

        // check if uuids have been upgraded already
        getServer().getConsoleSender().sendMessage("Checking if PS regions have been updated to UUIDs...");

        // Update to UUIDs
        if (config.get("uuidupdated") == null || !(boolean) config.get("uuidupdated")) {
            convertToUUID();
        }

        getServer().getConsoleSender().sendMessage(ChatColor.WHITE + "ProtectionStones has successfully started!");
    }

    private static void sendWithPerm(Player p, String msg, String desc, String cmd, String... permission) {
        for (String perm : permission) {
            if (p.hasPermission(perm)) {
                TextComponent m = new TextComponent(msg);
                m.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, cmd));
                m.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(desc).create()));
                p.spigot().sendMessage(m);
                break;
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            return ArgReload.argumentReload(s, args);
        }

        if (s instanceof Player) {
            Player p = (Player) s;
                if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
                    p.sendMessage(PSL.HELP.msg());
                    sendWithPerm(p, PSL.INFO_HELP.msg(), PSL.INFO_HELP_DESC.msg(), "/ps info","protectionstones.info");
                    sendWithPerm(p, PSL.ADDREMOVE_HELP.msg(), PSL.ADDREMOVE_HELP_DESC.msg(), "/ps","protectionstones.members");
                    sendWithPerm(p, PSL.ADDREMOVE_OWNER_HELP.msg(), PSL.ADDREMOVE_OWNER_HELP_DESC.msg(), "/ps", "protectionstones.owners");
                    sendWithPerm(p, PSL.GET_HELP.msg(), PSL.GET_HELP_DESC.msg(), "/ps get", "protectionstones.get");
                    sendWithPerm(p, PSL.GIVE_HELP.msg(), PSL.GIVE_HELP_DESC.msg(), "/ps give", "protectionstones.give");
                    sendWithPerm(p, PSL.COUNT_HELP.msg(), PSL.COUNT_HELP_DESC.msg(), "/ps count", "protectionstones.count", "protectionstones.count.others");
                    sendWithPerm(p, PSL.FLAG_HELP.msg(), PSL.FLAG_HELP_DESC.msg(), "/ps flag", "protectionstones.flags");
                    sendWithPerm(p, PSL.HOME_HELP.msg(), PSL.HOME_HELP_DESC.msg(), "/ps home", "protectionstones.home");
                    sendWithPerm(p, PSL.SETHOME_HELP.msg(), PSL.SETHOME_HELP_DESC.msg(), "/ps sethome", "protectionstones.sethome");
                    sendWithPerm(p, PSL.TP_HELP.msg(), PSL.TP_HELP_DESC.msg(), "/ps tp", "protectionstones.tp");
                    sendWithPerm(p, PSL.VISIBILITY_HIDE_HELP.msg(), PSL.VISIBILITY_HIDE_HELP_DESC.msg(), "/ps hide", "protectionstones.hide");
                    sendWithPerm(p, PSL.VISIBILITY_UNHIDE_HELP.msg(), PSL.VISIBILITY_UNHIDE_HELP_DESC.msg(), "/ps unhide", "protectionstones.unhide");
                    sendWithPerm(p, PSL.TOGGLE_HELP.msg(), PSL.TOGGLE_HELP_DESC.msg(), "/ps toggle","protectionstones.toggle");
                    sendWithPerm(p, PSL.VIEW_HELP.msg(), PSL.VIEW_HELP_DESC.msg(), "/ps view","protectionstones.view");
                    sendWithPerm(p, PSL.UNCLAIM_HELP.msg(), PSL.UNCLAIM_HELP_DESC.msg(), "/ps unclaim", "protectionstones.unclaim");
                    sendWithPerm(p, PSL.PRIORITY_HELP.msg(), PSL.PRIORITY_HELP_DESC.msg(), "/ps priority","protectionstones.priority");
                    sendWithPerm(p, PSL.REGION_HELP.msg(), PSL.REGION_HELP_DESC.msg(), "/ps region", "protectionstones.region");
                    sendWithPerm(p, PSL.ADMIN_HELP.msg(), PSL.ADMIN_HELP_DESC.msg(), "/ps admin", "protectionstones.admin");
                    sendWithPerm(p, PSL.RELOAD_HELP.msg(), PSL.RELOAD_HELP_DESC.msg(), "/ps reload", "protectionstones.admin");
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
                                p.sendMessage(PSL.TOGGLE_OFF.msg());
                            } else {
                                toggleList.remove(p.getName());
                                p.sendMessage(PSL.TOGGLE_ON.msg());
                            }
                        } else {
                            p.sendMessage(PSL.NO_PERMISSION_TOGGLE.msg());
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
                    case "get":
                        return ArgGet.argumentGet(p, args);
                    case "give":
                        return ArgGive.argumentGive(p, args);
                    case "sethome":
                        return ArgSethome.argumentSethome(p, args, currentPSID);
                    default:
                        p.sendMessage(PSL.NO_SUCH_COMMAND.msg());
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
                        continue;
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