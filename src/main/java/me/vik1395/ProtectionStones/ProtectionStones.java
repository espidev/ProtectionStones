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
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.vik1395.ProtectionStones.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

    public class ProtectionStones extends JavaPlugin {
    public static Plugin plugin, wgd;
    public static File psStoneData;
    public static File conf;
    public static FileConfiguration config;
    public static List<String> flags = new ArrayList<>();
    public static List<String> toggleList = new ArrayList<>();
    public static List<String> allowedFlags = new ArrayList<>();
    public static List<String> deniedWorlds = new ArrayList<>();
    public static Collection<String> mats = new HashSet<>();
    public static int priority;
    public Map<CommandSender, Integer> viewTaskList;
    public static Collection<UUID> pvpTPBypass = null;

    public static StoneTypeData StoneTypeData = new StoneTypeData();

    public static boolean isCooldownEnable = false;
    public static int cooldown = 0;
    public static String cooldownMessage = null;

    public static Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin("ProtectionStones");
    }

    public static RegionManager getRegionManagerWithPlayer(Player p) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(p.getWorld()));
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
    public static void removeDisownRegenPSRegion(UUID uuid, String arg, String region, RegionManager rgm, Player admin) {
        ProtectedRegion r = rgm.getRegion(region);
        switch (arg) {
            case "disown":
                DefaultDomain owners = r.getOwners();
                owners.removePlayer(uuid);
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


    @Override
    public void onEnable() {
        pvpTPBypass = Collections.EMPTY_LIST;
        viewTaskList = new HashMap<>();
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        plugin = this;
        conf = new File(this.getDataFolder() + "/config.yml");
        psStoneData = new File(this.getDataFolder() + "/hiddenpstones.yml");

        if (!(psStoneData.exists())) {
            try {
                ProtectionStones.psStoneData.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(ProtectionStones.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        getServer().getPluginManager().registerEvents(new ListenerClass(), this);
        if (getServer().getPluginManager().getPlugin("WorldGuard").isEnabled() && getServer().getPluginManager().getPlugin("WorldGuard").isEnabled()) {
            wgd = getServer().getPluginManager().getPlugin("WorldGuard");
        } else {
            getLogger().info("WorldGuard or WorldEdit not enabled! Disabling ProtectionStones...");
            getServer().getPluginManager().disablePlugin(this);
        }

        for (String material : this.getConfig().getString("Blocks").split(",")) {
            String[] split = material.split("-");

            if (split.length > 1 && split.length < 3) {
                if (!(Material.getMaterial(split[0]) == null)) {
                    mats.add(material.toUpperCase());
                } else {
                    Bukkit.getLogger().info("Unrecognized block: " + split[0] + ". Please make sure you have updated your block name for 1.13!");
                }
            } else {
                mats.add(split[0].toUpperCase());
            }
        }

        flags = getConfig().getStringList("Flags");
        allowedFlags = Arrays.asList((getConfig().getString("Allowed Flags").toLowerCase()).split(","));
        deniedWorlds = Arrays.asList((getConfig().getString("Worlds Denied").toLowerCase()).split(","));

        Config.initConfig();

        isCooldownEnable = getConfig().getBoolean("cooldown.enable");
        cooldown = getConfig().getInt("cooldown.cooldown") * 1000;
        cooldownMessage = getConfig().getString("cooldown.message");

        getLogger().info("ProtectionStones has successfully started!");
        getLogger().info("Created by Vik1395");
    }


    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (s instanceof Player) {
            Player p = (Player) s;
            if (cmd.getName().equalsIgnoreCase("ps")) {
                WorldGuardPlugin wg = (WorldGuardPlugin) ProtectionStones.wgd;
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
                    p.sendMessage(ChatColor.YELLOW + "/ps reclaim");//\\
                    p.sendMessage(ChatColor.YELLOW + "/ps priority {number|null}");//\\
                    p.sendMessage(ChatColor.YELLOW + "/ps region count|list|remove|regen|disown {playername}");//\\
                    p.sendMessage(ChatColor.YELLOW + "/ps admin {version|settings|hide|unhide|");//\\
                    p.sendMessage(ChatColor.YELLOW + "           cleanup|lastlogon|lastlogons|stats}");//\\
                    p.sendMessage(ChatColor.YELLOW + "/ps bypass");//\\
                    return true;
                }

                /*****************************************************************************************************/
                // Find the id of the current region the player is in and get WorldGuard player object for use later
                LocalPlayer localPlayer = wg.wrapPlayer(p);
                BlockVector3 v = BlockVector3.at(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());
                String currentPSID;
                RegionManager rgm = getRegionManagerWithPlayer(p);
                List<String> idList = rgm.getApplicableRegionsIDs(v);
                if (idList.size() == 1) {
                    currentPSID = idList.toString().substring(1, idList.toString().length() - 1);
                } else {
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
                ProtectedRegion rgn = rgm.getRegion(currentPSID);

                if (args[0].equalsIgnoreCase("toggle")) {
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
                    return true;
                } else if (args[0].equalsIgnoreCase("count")) {
                    return ArgCount.argumentCount(p, args);
                } else if (args[0].equalsIgnoreCase("region")) {
                    return ArgRegion.argumentRegion(p, args);
                } else if (args[0].equalsIgnoreCase("tp") || (args[0].equalsIgnoreCase("home"))) {
                    return ArgTp.argumentTp(p, args);
                } else if (args[0].equalsIgnoreCase("admin")) {
                    return ArgAdmin.argumentAdmin(p, args);
                } else if (args[0].equalsIgnoreCase("reclaim")) {
                    return ArgReclaim.argumentReclaim(p, args, currentPSID);
                } else if (args[0].equalsIgnoreCase("bypass")) {
                    return ArgBypass.argumentBypass(p, args);
                } else if (args[0].equalsIgnoreCase("add")) {
                    return ArgAddRemove.template(p, args, currentPSID, "add");
                } else if (args[0].equalsIgnoreCase("remove")) {
                    return ArgAddRemove.template(p, args, currentPSID, "remove");
                } else if (args[0].equalsIgnoreCase("addowner")) {
                    return ArgAddRemove.template(p, args, currentPSID, "addowner");
                } else if (args[0].equalsIgnoreCase("removeowner")) {
                    return ArgAddRemove.template(p, args, currentPSID, "removeowner");
                } else if (args[0].equalsIgnoreCase("view")) {
                    return ArgView.argumentView(p, args, currentPSID);
                } else if (args[0].equalsIgnoreCase("unhide")) {
                    return ArgHideUnhide.template(p, "unhide", currentPSID);
                } else if (args[0].equalsIgnoreCase("hide")) {
                    return ArgHideUnhide.template(p, "hide", currentPSID);
                }
                /*****************************************************************************************************/
                else if (args[0].equalsIgnoreCase("priority")) {
                    if (p.hasPermission("protectionstones.priority")) {
                        if (hasNoAccess(rgn, p, localPlayer, false)) {
                            p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You are not allowed to do that here.").toString());
                            return true;
                        }
                        if (args.length < 2) {
                            int priority = rgm.getRegion(id).getPriority();
                            p.sendMessage(ChatColor.YELLOW + "Priority: " + priority);
                            return true;
                        }
                        int priority = Integer.valueOf(Integer.parseInt(args[1])).intValue();
                        rgm.getRegion(id).setPriority(priority);
                        try {
                            rgm.save();
                        } catch (Exception e) {
                            System.out.println("[ProtectionStones] WorldGuard Error [" + e + "] during Region File Save");
                        }
                        p.sendMessage(ChatColor.YELLOW + "Priority has been set.");
                    } else {
                        p.sendMessage(ChatColor.RED + "You don't have permission to use Priority Commands");
                    }
                    return true;
                }
                /*****************************************************************************************************/
                else if (args[0].equalsIgnoreCase("flag")) {
                    if (p.hasPermission("protectionstones.flags")) {
                        if (hasNoAccess(rgn, p, localPlayer, false)) {
                            p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You are not allowed to do that here.").toString());
                            return true;
                        }
                        if (args.length >= 3) {
                            if (allowedFlags.contains(args[1].toLowerCase()) || p.hasPermission("protectionstones.flag." + args[1].toLowerCase()) || p.hasPermission("protectionstones.flag.*")) {
                                FlagHandler fh = new FlagHandler();
                                fh.setFlag(args, rgm.getRegion(id), p);
                            } else {
                                p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You don't have permission to set that flag").toString());
                            }
                        } else {
                            p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("Use:  /ps flag {flagname} {flagvalue}").toString());
                        }
                    } else {
                        p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You don't have permission to use flag commands").toString());
                    }
                    return true;
                }
                /*****************************************************************************************************/
                else if (args[0].equalsIgnoreCase("info")) {
                    if (hasNoAccess(rgn, p, localPlayer, true)) {
                        p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You are not allowed to do that here.").toString());
                        return true;
                    }
                    if (args.length == 1) {
                        if (p.hasPermission("protectionstones.info")) {
                            if (id != "") {
                                ProtectedRegion region = rgm.getRegion(id);
                                if (region != null) {
                                    p.sendMessage((new StringBuilder()).append(ChatColor.GRAY).append("================ PS Info ================").toString());
                                    p.sendMessage((new StringBuilder()).append(ChatColor.BLUE).append("Region:").append(ChatColor.YELLOW).append(id).append(ChatColor.BLUE).append(", Priority: ").append(ChatColor.YELLOW).append(rgm.getRegion(id).getPriority()).toString());
                                    String myFlag = "";
                                    String myFlagValue = "";
                                    for (Flag<?> flag : WorldGuard.getInstance().getFlagRegistry().getAll()) {
                                        if (region.getFlag(flag) != null) {
                                            myFlagValue = region.getFlag(flag).toString();
                                            RegionGroupFlag groupFlag = flag.getRegionGroupFlag();
                                            RegionGroup group = null;
                                            if (groupFlag != null) {
                                                group = region.getFlag(groupFlag);
                                            }
                                            if (group != null) {
                                                myFlag = (new StringBuilder(String.valueOf(myFlag))).append(flag.getName()).append(" -g ").append(region.getFlag(groupFlag)).append(" ").append(myFlagValue).append(", ").toString();
                                            } else {
                                                myFlag = (new StringBuilder(String.valueOf(myFlag))).append(flag.getName()).append(": ").append(myFlagValue).append(", ").toString();
                                            }
                                        }
                                    }

                                    if (myFlag.length() > 2) {
                                        myFlag = (new StringBuilder(String.valueOf(myFlag.substring(0, myFlag.length() - 2)))).append(".").toString();
                                        p.sendMessage((new StringBuilder()).append(ChatColor.BLUE).append("Flags: ").append(ChatColor.YELLOW).append(myFlag).toString());
                                    } else {
                                        p.sendMessage((new StringBuilder()).append(ChatColor.BLUE).append("Flags: ").append(ChatColor.RED).append("(none)").toString());
                                    }
                                    DefaultDomain owners = region.getOwners();
                                    String ownerNames = owners.getPlayers().toString();
                                    if (ownerNames != "[]") {
                                        ownerNames = ownerNames.substring(1, ownerNames.length() - 1);
                                        p.sendMessage((new StringBuilder()).append(ChatColor.BLUE).append("Owners: ").append(ChatColor.YELLOW).append(ownerNames).toString());
                                    } else {
                                        p.sendMessage((new StringBuilder()).append(ChatColor.BLUE).append("Owners: ").append(ChatColor.RED).append("(no owners)").toString());
                                    }
                                    DefaultDomain members = region.getMembers();
                                    String memberNames = members.getPlayers().toString();
                                    if (memberNames != "[]") {
                                        memberNames = memberNames.substring(1, memberNames.length() - 1);
                                        p.sendMessage((new StringBuilder()).append(ChatColor.BLUE).append("Members: ").append(ChatColor.YELLOW).append(memberNames).toString());
                                    } else {
                                        p.sendMessage((new StringBuilder()).append(ChatColor.BLUE).append("Members: ").append(ChatColor.RED).append("(no members)").toString());
                                    }
                                    BlockVector3 min = region.getMinimumPoint();
                                    BlockVector3 max = region.getMaximumPoint();
                                    p.sendMessage((new StringBuilder()).append(ChatColor.BLUE).append("Bounds: ").append(ChatColor.YELLOW).append("(").append(min.getBlockX()).append(",").append(min.getBlockY()).append(",").append(min.getBlockZ()).append(") -> (").append(max.getBlockX()).append(",").append(max.getBlockY()).append(",").append(max.getBlockZ()).append(")").toString());
                                    return true;
                                }
                                p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Region does not exist").toString());
                            } else {
                                p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("No region found").toString());
                            }
                        } else {
                            p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You don't have permission to use the region info command").toString());
                        }
                    } else if (args.length == 2) {
                        if (args[1].equalsIgnoreCase("members")) {
                            if (p.hasPermission("protectionstones.members")) {
                                DefaultDomain members = rgm.getRegion(id).getMembers();
                                String memberNames = members.getPlayers().toString();
                                if (memberNames != "[]") {
                                    memberNames = memberNames.substring(1, memberNames.length() - 1);
                                    p.sendMessage((new StringBuilder()).append(ChatColor.BLUE).append("Members: ").append(ChatColor.YELLOW).append(memberNames).toString());
                                } else {
                                    p.sendMessage((new StringBuilder()).append(ChatColor.BLUE).append("Members: ").append(ChatColor.RED).append("(no members)").toString());
                                }
                            } else {
                                p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You don't have permission to use Members Commands").toString());
                            }
                        } else if (args[1].equalsIgnoreCase("owners")) {
                            if (p.hasPermission("protectionstones.owners")) {
                                DefaultDomain owners = rgm.getRegion(id).getOwners();
                                String ownerNames = owners.getPlayers().toString();
                                if (ownerNames != "[]") {
                                    ownerNames = ownerNames.substring(1, ownerNames.length() - 1);
                                    p.sendMessage((new StringBuilder()).append(ChatColor.BLUE).append("Owners: ").append(ChatColor.YELLOW).append(ownerNames).toString());
                                } else {
                                    p.sendMessage((new StringBuilder()).append(ChatColor.BLUE).append("Owners: ").append(ChatColor.RED).append("(no owners)").toString());
                                }
                            } else {
                                p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You don't have permission to use Owners Commands").toString());
                            }
                        } else if (args[1].equalsIgnoreCase("flags")) {
                            if (p.hasPermission("protectionstones.flags")) {
                                String myFlag = "";
                                String myFlagValue = "";
                                WorldGuard.getInstance().getFlagRegistry().getAll();
                                for (Flag<?> flag : WorldGuard.getInstance().getFlagRegistry().getAll()) {
                                    if (rgm.getRegion(id).getFlag(flag) != null) {
                                        myFlagValue = rgm.getRegion(id).getFlag(flag).toString();
                                        myFlag = myFlag + flag.getName() + ": " + myFlagValue + ", ";
                                    }
                                }
                                if (myFlag.length() > 2) {
                                    myFlag = myFlag.substring(0, myFlag.length() - 2) + ".";
                                    p.sendMessage(ChatColor.BLUE + "Flags: " + ChatColor.YELLOW + myFlag);
                                } else {
                                    p.sendMessage(ChatColor.BLUE + "Flags: " + ChatColor.RED + "(none)");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "You don't have permission to use Flags Commands");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "Use:  /ps info members|owners|flags");
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "Use:  /ps info members|owners|flags");
                    }
                    return true;
                } else {
                    p.sendMessage(ChatColor.RED + "No such command. please type /ps help for more info");
                }
            }
        } else {
            s.sendMessage(ChatColor.RED + "PS cannot be used from Console");
        }
        return true;
    }

    public static boolean hasNoAccess(ProtectedRegion region, Player p, LocalPlayer lp, boolean canBeMember) {
        if (region == null) { // Region is not valid
            return true;
        }

        if (p.hasPermission("protectionstones.superowner") || region.isOwner(lp) || (canBeMember && region.isMember(lp))) {
            // Player has permission here
            return false;
        } else {
            // No permissions
            return true;
        }
    }

    public static Object getFlagValue(Flag<?> flag, Object value) {
        if (value == null) return null;

        String valueString = value.toString().trim();

        if ((flag instanceof StateFlag)) {
            if (valueString.equalsIgnoreCase("allow")) return StateFlag.State.ALLOW;
            if (valueString.equalsIgnoreCase("deny")) return StateFlag.State.DENY;
            return null;
        }
        return null;
    }

}