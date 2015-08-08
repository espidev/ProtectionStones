package me.vik1395.ProtectionStones;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/*

Author: Vik1395
Project: ProtectionStones

Copyright 2015

Licensed under Creative CommonsAttribution-ShareAlike 4.0 International Public License (the "License");
You may not use this file except in compliance with the License.

You may obtain a copy of the License at http://creativecommons.org/licenses/by-sa/4.0/legalcode

You may find an abridged version of the License at http://creativecommons.org/licenses/by-sa/4.0/
 */

public class Main extends JavaPlugin
{
	public static Plugin plugin, wgd;
	public static List<String> flags = new ArrayList<String>();
	public static List<String> exworlds = new ArrayList<String>();
	public static List<String> toggleList = new ArrayList<String>();
	public static List<String> allowedFlags = new ArrayList<String>();
	public static Material mat;
	public static boolean hide, nodrop, blockpiston, silktouch, uuid;
	public static int x, y, z, priority;
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<CommandSender, Integer> viewTaskList = new HashMap();
	
	public void onEnable()
	{
		saveDefaultConfig();
		getConfig().options().copyDefaults(true);
		plugin = this;
		getServer().getPluginManager().registerEvents(new ListenerClass(), this);
		if(getServer().getPluginManager().getPlugin("WorldGuard").isEnabled() && getServer().getPluginManager().getPlugin("WorldGuard").isEnabled())
		{
			wgd = getServer().getPluginManager().getPlugin("WorldGuard");
		}
		else
		{
			getLogger().info("WorldGuard or WorldEdit not enabled! Disabling ProtectionStones...");
			getServer().getPluginManager().disablePlugin(this);
		}
		
		mat = Material.getMaterial(getConfig().getString("Block"));
		flags = getConfig().getStringList("Flags");
		x = getConfig().getInt("Region.X Radius");
		y = getConfig().getInt("Region.Y Radius");
		z = getConfig().getInt("Region.Z Radius");
		hide = getConfig().getBoolean("Region.Auto Hide");
		nodrop = getConfig().getBoolean("Region.No Drop");
		silktouch = getConfig().getBoolean("Region.Silk Touch");
		blockpiston = getConfig().getBoolean("Region.Block Piston");
		exworlds = getConfig().getStringList("Exclude Worlds");
		priority = getConfig().getInt("Priority");
		allowedFlags = Arrays.asList((getConfig().getString("Allowed Flags").toLowerCase()).split(","));
		
		getLogger().info("ProtectionStones has successfully started!");
		getLogger().info("Created by Vik1395");
	}
	
	@Override
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args)
	{
		if(s instanceof Player)
		{
			if(cmd.getName().equalsIgnoreCase("ps"))
			{
				Player p = (Player)s;
				WorldGuardPlugin wg = (WorldGuardPlugin) Main.wgd;
				RegionManager rgm = wg.getRegionManager(p.getWorld());
				if (args.length==0 || args[0].equalsIgnoreCase("help")) 
				{
					p.sendMessage(ChatColor.YELLOW + "/ps info members|owners|flags");//\\
			        p.sendMessage(ChatColor.YELLOW + "/ps add|remove {playername}");//\\
			        p.sendMessage(ChatColor.YELLOW + "/ps addowner|removeowner {playername}");//\\
			        p.sendMessage(ChatColor.YELLOW + "/ps flag {flagname} {setting|null}");//\\
			        p.sendMessage(ChatColor.YELLOW + "/ps tp {num} - " + ChatColor.GREEN +"{num} has to be within the number of protected regions you own");
			        p.sendMessage(ChatColor.YELLOW + "/ps hide|unhide");//\\
			        p.sendMessage(ChatColor.YELLOW + "/ps toggle");//\\
			        p.sendMessage(ChatColor.YELLOW + "/ps view");//\\
			        p.sendMessage(ChatColor.YELLOW + "/ps priority {number|null}");//\\
			        p.sendMessage(ChatColor.YELLOW + "/ps region count|list|remove|regen|disown {playername}");//\\
			        p.sendMessage(ChatColor.YELLOW + "/ps admin {version|settings|hide|unhide|");//\\
			        p.sendMessage(ChatColor.YELLOW + "           cleanup|lastlogon|lastlogons|stats}");//\\
			        return true;
				}
				
				else if (args[0].equalsIgnoreCase("toggle")) 
				{
					if (p.hasPermission("protectionstones.toggle")) 
			        {
						if(toggleList!=null)
						{
				            if (!toggleList.contains(p.getName())) 
				            {
				            	toggleList.add(p.getName());
				            	p.sendMessage(ChatColor.YELLOW + "ProtectionStone placement turned off");
				            } 
				            else 
				            {
				            	toggleList.remove(p.getName());
				            	p.sendMessage(ChatColor.YELLOW + "ProtectionStone placement turned on");
				            }
						}
						else
						{
							toggleList.add(p.getName());
			            	p.sendMessage(ChatColor.YELLOW + "ProtectionStone placement turned off");
						}
			        } 
			        else
			        {
			        	p.sendMessage(ChatColor.RED + "You don't have permission to use the toggle command");
			        }
			        return true;
				}
			/*****************************************************************************************************/
				double x = p.getLocation().getX();
		        double y = p.getLocation().getY();
		        double z = p.getLocation().getZ();
		        Vector v = new Vector(x, y, z);
		        String id = "";
		        List<String> idList = rgm.getApplicableRegionsIDs(v);
		        if(idList.size() == 1)
		        {
		            id = idList.toString();
		            id = id.substring(1, id.length() - 1);
		        } 
		        else
		        {
		            double distanceToPS = 10000D;
		            double tempToPS = 0.0D;
		            String namePSID = "";
		            for(Iterator<String> iterator4 = idList.iterator(); iterator4.hasNext();)
		            {
		                String currentID = (String)iterator4.next();
		                if(currentID.substring(0, 2).equals("ps"))
		                {
		                    int indexX = currentID.indexOf("x");
		                    int indexY = currentID.indexOf("y");
		                    int indexZ = currentID.length() - 1;
		                    double psx = Double.parseDouble(currentID.substring(2, indexX));
		                    double psy = Double.parseDouble(currentID.substring(indexX + 1, indexY));
		                    double psz = Double.parseDouble(currentID.substring(indexY + 1, indexZ));
		                    Location psLocation = new Location(p.getWorld(), psx, psy, psz);
		                    tempToPS = p.getLocation().distance(psLocation);
		                    if(tempToPS < distanceToPS)
		                    {
		                        distanceToPS = tempToPS;
		                        namePSID = currentID;
		                    }
		                }
		            }
		            id = namePSID;
		        }
		        LocalPlayer localPlayer = wg.wrapPlayer(p);
		        if(rgm.getRegion(id) != null)
		        {
		        	if(rgm.getRegion(id).isOwner(localPlayer) || p.hasPermission("protectionstones.superowner"))
		            {
		        		if(args[0].equalsIgnoreCase("add"))
		        		{
		                    if(p.hasPermission("protectionstones.members"))
		                    {
		                        if(args.length < 2)
		                        {
		                            p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("This command requires a player name.").toString());
		                            return true;
		                        } 
		                        else
		                        {
		                            String playerName = args[1];
		                            UUID uid = Bukkit.getPlayer(playerName).getUniqueId();
		                            DefaultDomain members = rgm.getRegion(id).getMembers();
		                            members.addPlayer(playerName);
		                            if(uuid)
		                            {
		                            	members.addPlayer(uid);
		                            }
		                            rgm.getRegion(id).setMembers(members);
		                            try 
						            {
						            	rgm.save();
						            } 
						            catch (Exception e) 
						            {
						                System.out.println("[ProtectionStones] WorldGuard Error [" + e + "] during Region File Save");
						            }
		                            p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append(playerName).append(" has been added to your region.").toString());
		                            return true;
		                        }
		                    } 
		                    else
		                    {
		                        p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You don't have permission to use Members Commands").toString());
		                        return true;
		                    }
		        		}
			        	/***************/
		        		else if(args[0].equalsIgnoreCase("remove"))
		                {
		                    if(p.hasPermission("protectionstones.members"))
		                    {
		                        if(args.length < 2)
		                        {
		                            p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("This command requires a player name.").toString());
		                            return true;
		                        }
		                        String playerName = args[1];
	                            UUID uid = Bukkit.getPlayer(playerName).getUniqueId();
		                        DefaultDomain members = rgm.getRegion(id).getMembers();
		                        members.removePlayer(playerName);
		                        if(uuid)
	                            {
	                            	members.removePlayer(uid);
	                            }
		                        rgm.getRegion(id).setMembers(members);
		                        try 
					            {
					            	rgm.save();
					            } 
					            catch (Exception e) 
					            {
					                System.out.println("[ProtectionStones] WorldGuard Error [" + e + "] during Region File Save");
					            }
		                        p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append(playerName).append(" has been removed from region.").toString());
		                    } 
		                    else
		                    {
		                        p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You don't have permission to use Members Commands").toString());
		                    }
		                    return true;
		                }
		                /***************/
		                if(args[0].equalsIgnoreCase("addowner"))
		                {
		                    if(p.hasPermission("protectionstones.owners"))
		                    {
		                        if(args.length < 2)
		                        {
		                            p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("This command requires a player name.").toString());
		                            return true;
		                        } 
		                        else
		                        {
		                            String playerName = args[1];
		                            UUID uid = Bukkit.getPlayer(playerName).getUniqueId();
		                            DefaultDomain owners = rgm.getRegion(id).getOwners();
		                            owners.addPlayer(playerName);
			                        if(uuid)
		                            {
		                            	owners.addPlayer(uid);
		                            }
		                            rgm.getRegion(id).setOwners(owners);
		                            try 
						            {
						            	rgm.save();
						            } 
						            catch (Exception e) 
						            {
						                System.out.println("[ProtectionStones] WorldGuard Error [" + e + "] during Region File Save");
						            }
		                            p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append(playerName).append(" has been added to your region.").toString());
		                            return true;
		                        }
		                    } 
		                    else
		                    {
		                        p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You don't have permission to use Owners Commands").toString());
		                        return true;
		                    }
		                }
		                /***************/
		                if(args[0].equalsIgnoreCase("removeowner"))
		                {
		                    if(p.hasPermission("protectionstones.owners"))
		                    {
		                        if(args.length < 2)
		                        {
		                            p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("This command requires a player name.").toString());
		                            return true;
		                        }
		                        String playerName = args[1];
	                            UUID uid = Bukkit.getPlayer(playerName).getUniqueId();
		                        DefaultDomain owners = rgm.getRegion(id).getOwners();
		                        owners.removePlayer(playerName);
		                        if(uuid)
	                            {
	                            	owners.addPlayer(uid);
	                            }
		                        rgm.getRegion(id).setOwners(owners);
		                        try 
					            {
					            	rgm.save();
					            } 
					            catch (Exception e) 
					            {
					                System.out.println("[ProtectionStones] WorldGuard Error [" + e + "] during Region File Save");
					            }
		                        p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append(playerName).append(" has been removed from region.").toString());
		                    } 
		                    else
		                    {
		                        p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You don't have permission to use Owners Commands").toString());
		                    }
		                    return true;
		                }
		                /***************/
		                else if(args[0].equalsIgnoreCase("view"))
						{
							if (p.hasPermission("protectionstones.view")) 
							{
								if (!this.viewTaskList.isEmpty()) 
								{
									int playerTask = 0;
					                try 
					                {
					                	playerTask = ((Integer)this.viewTaskList.get(p)).intValue();
					                } 
					                catch (Exception e) 
					                {
					                	playerTask = 0;
					                }
					                if ((playerTask != 0) && (Bukkit.getScheduler().isQueued(playerTask))) 
					                {
					                	return true;
					                }
					            }
								Vector minVector = rgm.getRegion(id).getMinimumPoint();
					            Vector maxVector = rgm.getRegion(id).getMaximumPoint();
					            final int minX = minVector.getBlockX();
					            final int minY = minVector.getBlockY();
					            final int minZ = minVector.getBlockZ();
					            final int maxX = maxVector.getBlockX();
					            final int maxY = maxVector.getBlockY();
					            final int maxZ = maxVector.getBlockZ();
					            double px = p.getLocation().getX();
					            double py = p.getLocation().getY();
					            double pz = p.getLocation().getZ();
					            Vector playerVector = new Vector(px, py, pz);
					            final int playerY = playerVector.getBlockY();
					            final World theWorld = p.getWorld();

					            final Material bm1 = getBlock(theWorld, minX, playerY, minZ);
					            final Material bm2 = getBlock(theWorld, maxX, playerY, minZ);
					            final Material bm3 = getBlock(theWorld, minX, playerY, maxZ);
					            final Material bm4 = getBlock(theWorld, maxX, playerY, maxZ);
					            final Material bm5 = getBlock(theWorld, minX, maxY, minZ);
					            final Material bm6 = getBlock(theWorld, maxX, maxY, minZ);
					            final Material bm7 = getBlock(theWorld, minX, maxY, maxZ);
					            final Material bm8 = getBlock(theWorld, maxX, maxY, maxZ);
					            final Material bm9 = getBlock(theWorld, minX, minY, minZ);
					            final Material bm10 = getBlock(theWorld, maxX, minY, minZ);
					            final Material bm11 = getBlock(theWorld, minX, minY, maxZ);
					            final Material bm12 = getBlock(theWorld, maxX, minY, maxZ);

					            setBlock(theWorld, minX, playerY, minZ, Material.GLASS);
					            setBlock(theWorld, maxX, playerY, minZ, Material.GLASS);
					            setBlock(theWorld, minX, playerY, maxZ, Material.GLASS);
					            setBlock(theWorld, maxX, playerY, maxZ, Material.GLASS);

					            setBlock(theWorld, minX, maxY, minZ, Material.GLASS);
					            setBlock(theWorld, maxX, maxY, minZ, Material.GLASS);
					            setBlock(theWorld, minX, maxY, maxZ, Material.GLASS);
					            setBlock(theWorld, maxX, maxY, maxZ, Material.GLASS);

					            setBlock(theWorld, minX, minY, minZ, Material.GLASS);
					            setBlock(theWorld, maxX, minY, minZ, Material.GLASS);
					            setBlock(theWorld, minX, minY, maxZ, Material.GLASS);
					            setBlock(theWorld, maxX, minY, maxZ, Material.GLASS);

					            int taskID = getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() 
					            {
					                public void run() 
					                {
					                	Main.this.setBlock(theWorld, minX, playerY, minZ, bm1);
					                	Main.this.setBlock(theWorld, maxX, playerY, minZ, bm2);
					                	Main.this.setBlock(theWorld, minX, playerY, maxZ, bm3);
					                	Main.this.setBlock(theWorld, maxX, playerY, maxZ, bm4);
					                	Main.this.setBlock(theWorld, minX, maxY, minZ, bm5);
					                	Main.this.setBlock(theWorld, maxX, maxY, minZ, bm6);
					                	Main.this.setBlock(theWorld, minX, maxY, maxZ, bm7);
					                	Main.this.setBlock(theWorld, maxX, maxY, maxZ, bm8);
					                    Main.this.setBlock(theWorld, minX, minY, minZ, bm9);
					                    Main.this.setBlock(theWorld, maxX, minY, minZ, bm10);
					                    Main.this.setBlock(theWorld, minX, minY, maxZ, bm11);
					                    Main.this.setBlock(theWorld, maxX, minY, maxZ, bm12);
					                }
					            }, 600L);
					            
					            this.viewTaskList.put(p, Integer.valueOf(taskID));
					        } 
							else 
							{
								p.sendMessage(ChatColor.RED + "You don't have permission to use that command");
					        }
					        return true;
						}
		                /***************/
		                if(args[0].equalsIgnoreCase("unhide"))
		                {
		                    if(p.hasPermission("protectionstones.unhide"))
		                    {
		                        if(id.substring(0, 2).equals("ps"))
		                        {
		                            int indexX = id.indexOf("x");
		                            int indexY = id.indexOf("y");
		                            int indexZ = id.length() - 1;
		                            int psx = Integer.parseInt(id.substring(2, indexX));
		                            int psy = Integer.parseInt(id.substring(indexX + 1, indexY));
		                            int psz = Integer.parseInt(id.substring(indexY + 1, indexZ));
		                            //Vector minVector = rgm.getRegion(id).getMinimumPoint();
		                            //Vector maxVector = rgm.getRegion(id).getMaximumPoint();
		                            //int minX = minVector.getBlockX();
		                            //int maxX = maxVector.getBlockX();
		                            //int size = (maxX - minX) / 2;

		                            Block blockToUnhide = p.getWorld().getBlockAt(psx, psy, psz);
	                                blockToUnhide.setType(mat);
	                                
		                        } 
		                        else
		                        {
		                            p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Not a ProtectionStones Region").toString());
		                        }
		                    } 
		                    else
		                    {
		                        p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You don't have permission to use that command").toString());
		                    }
		                    return true;
		                }
		                /***************/
		                if(args[0].equalsIgnoreCase("hide"))
		                {
		                    if(p.hasPermission("protectionstones.hide"))
		                    {
		                        if(id.substring(0, 2).equals("ps"))
		                        {
		                            int indexX = id.indexOf("x");
		                            int indexY = id.indexOf("y");
		                            int indexZ = id.length() - 1;
		                            int psx = Integer.parseInt(id.substring(2, indexX));
		                            int psy = Integer.parseInt(id.substring(indexX + 1, indexY));
		                            int psz = Integer.parseInt(id.substring(indexY + 1, indexZ));
		                            Block blockToHide = p.getWorld().getBlockAt(psx, psy, psz);
		                            blockToHide.setType(Material.AIR);
		                        } 
		                        else
		                        {
		                            p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Not a ProtectionStones Region").toString());
		                        }
		                    } 
		                    else
		                    {
		                        p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You don't have permission to use that command").toString());
		                    }
		                    return true;
		                }
		                /***************/
		        		else if(args[0].equalsIgnoreCase("priority"))
						{
							if (p.hasPermission("protectionstones.priority")) 
							{
								if (args.length < 2) 
								{
					                int priority = rgm.getRegion(id).getPriority();
					                p.sendMessage(ChatColor.YELLOW + "Priority: " + priority);
					                return true;
					            }
								int priority = Integer.valueOf(Integer.parseInt(args[1])).intValue();
					            rgm.getRegion(id).setPriority(priority);
					            try 
					            {
					            	rgm.save();
					            } 
					            catch (Exception e) 
					            {
					                System.out.println("[ProtectionStones] WorldGuard Error [" + e + "] during Region File Save");
					            }
					            p.sendMessage(ChatColor.YELLOW + "Priority has been set.");
					        } 
							else 
							{
								p.sendMessage(ChatColor.RED + "You don't have permission to use Priority Commands");
					        }
					            return true;
						}
		                /***************/
		                if(args[0].equalsIgnoreCase("flag"))
		                {
		                    if(p.hasPermission("protectionstones.flags"))
		                    {
		                    	if(args.length>=3)
		                    	{
		                    		if(allowedFlags.contains(args[1].toLowerCase()))
		                    		{
		                    			FlagHandler fh= new FlagHandler();
		                    			fh.setFlag(args, rgm.getRegion(id), p);
		                    		}
		                        	else
		                            {
		                                p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You don't have permission to set that flag").toString());
		                            }
		                        } 
		                    	else
		                        {
		                            p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("Use:  /ps flag {flagname} {flagvalue}").toString());
		                        }
		                    } else
		                    {
		                        p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You don't have permission to use flag commands").toString());
		                    }
		                    return true;
		                }
		                /***************/
		                if(args[0].equalsIgnoreCase("info"))
		                {
			                if(args.length == 1)
		                    {
		                        if(p.hasPermission("protectionstones.info"))
		                        {
		                            if(id != "")
		                            {
		                                ProtectedRegion region = rgm.getRegion(id);
		                                if(region != null)
		                                {
		                                    p.sendMessage((new StringBuilder()).append(ChatColor.GRAY).append("================ PS Info ================").toString());
		                                    p.sendMessage((new StringBuilder()).append(ChatColor.BLUE).append("Region:").append(ChatColor.YELLOW).append(id).append(ChatColor.BLUE).append(", Priority: ").append(ChatColor.YELLOW).append(rgm.getRegion(id).getPriority()).toString());
		                                    String myFlag = "";
		                                    String myFlagValue = "";
		                                    int n = DefaultFlag.flagsList.length;
		                                    for(int i = 0; i < n; i++)
		                                    {
		                                        Flag<?> flag = DefaultFlag.flagsList[i];
		                                        if(region.getFlag(flag) != null)
		                                        {
		                                            myFlagValue = region.getFlag(flag).toString();
		                                            myFlag = (new StringBuilder(String.valueOf(myFlag))).append(flag.getName()).append(": ").append(myFlagValue).append(", ").toString();
		                                        }
		                                    }
	
		                                    if(myFlag.length() > 2)
		                                    {
		                                        myFlag = (new StringBuilder(String.valueOf(myFlag.substring(0, myFlag.length() - 2)))).append(".").toString();
		                                        p.sendMessage((new StringBuilder()).append(ChatColor.BLUE).append("Flags: ").append(ChatColor.YELLOW).append(myFlag).toString());
		                                    } else
		                                    {
		                                        p.sendMessage((new StringBuilder()).append(ChatColor.BLUE).append("Flags: ").append(ChatColor.RED).append("(none)").toString());
		                                    }
		                                    DefaultDomain owners = region.getOwners();
		                                    String ownerNames = owners.getPlayers().toString();
		                                    if(ownerNames != "[]")
		                                    {
		                                        ownerNames = ownerNames.substring(1, ownerNames.length() - 1);
		                                        p.sendMessage((new StringBuilder()).append(ChatColor.BLUE).append("Owners: ").append(ChatColor.YELLOW).append(ownerNames).toString());
		                                    } else
		                                    {
		                                        p.sendMessage((new StringBuilder()).append(ChatColor.BLUE).append("Owners: ").append(ChatColor.RED).append("(no owners)").toString());
		                                    }
		                                    DefaultDomain members = region.getMembers();
		                                    String memberNames = members.getPlayers().toString();
		                                    if(memberNames != "[]")
		                                    {
		                                        memberNames = memberNames.substring(1, memberNames.length() - 1);
		                                        p.sendMessage((new StringBuilder()).append(ChatColor.BLUE).append("Members: ").append(ChatColor.YELLOW).append(memberNames).toString());
		                                    } else
		                                    {
		                                        p.sendMessage((new StringBuilder()).append(ChatColor.BLUE).append("Members: ").append(ChatColor.RED).append("(no members)").toString());
		                                    }
		                                    BlockVector min = region.getMinimumPoint();
		                                    BlockVector max = region.getMaximumPoint();
		                                    p.sendMessage((new StringBuilder()).append(ChatColor.BLUE).append("Bounds: ").append(ChatColor.YELLOW).append("(").append(min.getBlockX()).append(",").append(min.getBlockY()).append(",").append(min.getBlockZ()).append(") -> (").append(max.getBlockX()).append(",").append(max.getBlockY()).append(",").append(max.getBlockZ()).append(")").toString());
		                                    return true;
		                                }
		                                p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Region does not exist").toString());
		                            } 
		                            else
		                            {
		                                p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("No region found").toString());
		                            }
		                        } 
		                        else
		                        {
		                            p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You don't have permission to use the region info command").toString());
		                        }
		                    } 
			                else
		                    if(args.length == 2)
		                    {
		                        if(args[1].equalsIgnoreCase("members"))
		                        {
		                            if(p.hasPermission("protectionstones.members"))
		                            {
		                                DefaultDomain members = rgm.getRegion(id).getMembers();
		                                String memberNames = members.getPlayers().toString();
		                                if(memberNames != "[]")
		                                {
		                                    memberNames = memberNames.substring(1, memberNames.length() - 1);
		                                    p.sendMessage((new StringBuilder()).append(ChatColor.BLUE).append("Members: ").append(ChatColor.YELLOW).append(memberNames).toString());
		                                }
		                                else
		                                {
		                                    p.sendMessage((new StringBuilder()).append(ChatColor.BLUE).append("Members: ").append(ChatColor.RED).append("(no members)").toString());
		                                }
		                            } 
		                            else
		                            {
		                                p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You don't have permission to use Members Commands").toString());
		                            }
		                        } 
		                        else
		                        if(args[1].equalsIgnoreCase("owners"))
		                        {
		                            if(p.hasPermission("protectionstones.owners"))
		                            {
		                                DefaultDomain owners = rgm.getRegion(id).getOwners();
		                                String ownerNames = owners.getPlayers().toString();
		                                if(ownerNames != "[]")
		                                {
		                                    ownerNames = ownerNames.substring(1, ownerNames.length() - 1);
		                                    p.sendMessage((new StringBuilder()).append(ChatColor.BLUE).append("Owners: ").append(ChatColor.YELLOW).append(ownerNames).toString());
		                                } 
		                                else
		                                {
		                                    p.sendMessage((new StringBuilder()).append(ChatColor.BLUE).append("Owners: ").append(ChatColor.RED).append("(no owners)").toString());
		                                }
		                            } 
		                            else
		                            {
		                                p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You don't have permission to use Owners Commands").toString());
		                            }
		                        } 
		                        else
		                        if(args[1].equalsIgnoreCase("flags"))
		                        {
		                            if(p.hasPermission("protectionstones.flags"))
		                            {
		                                String myFlag = "";
		                                String myFlagValue = "";
		                                int n = DefaultFlag.flagsList.length;
		                                for(int i = 0; i < n; i++)
		                                {
		                                    Flag<?> flag = DefaultFlag.flagsList[i];
		                                    if(rgm.getRegion(id).getFlag(flag) != null)
		                                    {
		                                        myFlagValue = rgm.getRegion(id).getFlag(flag).toString();
		                                        myFlag = (new StringBuilder(String.valueOf(myFlag))).append(flag.getName()).append(": ").append(myFlagValue).append(", ").toString();
		                                    }
		                                }
	
		                                if(myFlag.length() > 2)
		                                {
		                                    myFlag = (new StringBuilder(String.valueOf(myFlag.substring(0, myFlag.length() - 2)))).append(".").toString();
		                                    p.sendMessage((new StringBuilder()).append(ChatColor.BLUE).append("Flags: ").append(ChatColor.YELLOW).append(myFlag).toString());
		                                } 
		                                else
		                                {
		                                    p.sendMessage((new StringBuilder()).append(ChatColor.BLUE).append("Flags: ").append(ChatColor.RED).append("(none)").toString());
		                                }
		                            } 
		                            else
		                            {
		                                p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You don't have permission to use Flags Commands").toString());
		                            }
		                        } 
		                        else
		                        {
		                            p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("Use:  /ps info members|owners|flags").toString());
		                        }
		                    } 
		                    else
		                    {
		                        p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("Use:  /ps info members|owners|flags").toString());
		                    }
		                    return true;
			            }
		            }
		        }
			/*****************************************************************************************************/
				if(args[0].equalsIgnoreCase("region"))
				{
					if (args.length >= 3) 
					{
						if (p.hasPermission("protectionstones.region")) 
						{
							Player p2 = Bukkit.getPlayer(args[2]);
							if (args[1].equalsIgnoreCase("count")) 
							{
								LocalPlayer playerName = null;
								int count = 0;
								try 
								{
									playerName = wg.wrapPlayer(p2);
									count = rgm.getRegionCountOfPlayer(playerName);
				                }
								catch (Exception e) {}
								p.sendMessage(ChatColor.YELLOW + args[2] + "'s region count: " + count);
								return true;
				            }
							String idname;
				            if (args[1].equalsIgnoreCase("list")) 
				            {
				            	Map<String, ProtectedRegion> regions = rgm.getRegions();
				            	String name = args[2].toLowerCase();
				            	int size = regions.size();
				            	String[] regionIDList = new String[size];
				            	String regionMessage = "";
				            	int index = 0;
				            	for (Iterator<String> playerCount = regions.keySet().iterator(); playerCount.hasNext(); ) 
				            	{ 
				            		idname = (String)playerCount.next();
				                	try 
				                	{
				                		if (((ProtectedRegion)regions.get(idname)).getOwners().getPlayers().contains(name)) 
				                		{
				                			regionIDList[index] = idname;
				                			regionMessage = regionMessage + regionIDList[index] + ", ";
				                			index++;
				                		}
				                	}
				                	catch (Exception localException6){} 
				                }
				            	if (index == 0) 
				            	{
				            		p.sendMessage(ChatColor.YELLOW + "No regions found for " + name);
				            	} 
				            	else 
				            	{
					                regionMessage = regionMessage.substring(0, regionMessage.length() - 2) + ".";
					                p.sendMessage(ChatColor.YELLOW + args[2] + "'s regions: " + regionMessage);
				            	}
				              return true;
				            }
				            if ((args[1].equalsIgnoreCase("remove")) || (args[1].equalsIgnoreCase("regen")) || (args[1].equalsIgnoreCase("disown"))) 
				            {
				            	RegionManager mgr = wg.getRegionManager(p.getWorld());
				            	Map<String, ProtectedRegion> regions = mgr.getRegions();
				            	String name = args[2].toLowerCase();
				            	int size = regions.size();
				            	String[] regionIDList = new String[size];
				            	int index = 0;
				            	for (String idname1 : regions.keySet())
				            	{
				            		try 
				            		{
				            			if(((ProtectedRegion)regions.get(idname1)).getOwners().getPlayers().contains(name)) 
				            			{
				            				regionIDList[index] = idname1;
				            				index++;
				            			}
				            		}
				            		catch (Exception localException7){}
				            	}
				            	if (index == 0) 
				            	{
				            		p.sendMessage(ChatColor.YELLOW + "No regions found for " + name);
				            	} 
				            	else 
				            	{
				            		for (int i = 0; i < index; i++) 
				            		{
				            			if (args[1].equalsIgnoreCase("disown"))
				            			{
				            				DefaultDomain owners = rgm.getRegion(regionIDList[i]).getOwners();
				            				owners.removePlayer(name);
				            				rgm.getRegion(regionIDList[i]).setOwners(owners);
				            			} 
				            			else 
				            			{
				            				if (args[1].equalsIgnoreCase("regen"))
				            				{
				            					if (this.getServer().getPluginManager().getPlugin("WorldEdit") != null)
				            					{
				            						Bukkit.dispatchCommand(p, "region select " + regionIDList[i]);
				            						Bukkit.dispatchCommand(p, "/regen");
				            					}
				            				}
				            				else if (regionIDList[i].substring(0, 2).equals("ps")) 
				            				{
				            					int indexX = regionIDList[i].indexOf("x");
					            				int indexY = regionIDList[i].indexOf("y");
					            				int indexZ = regionIDList[i].length() - 1;
					            				int psx = Integer.parseInt(regionIDList[i].substring(2, indexX));
					            				int psy = Integer.parseInt(regionIDList[i].substring(indexX + 1, indexY));
					            				int psz = Integer.parseInt(regionIDList[i].substring(indexY + 1, indexZ));
					            				Block blockToRemove = p.getWorld().getBlockAt(psx, psy, psz);
					            				blockToRemove.setType(Material.AIR);
				            				}
				            				mgr.removeRegion(regionIDList[i]);
				            			}
				            		}
				            		p.sendMessage(ChatColor.YELLOW + name + "'s regions have been removed");
				            		try 
				            		{
				            			rgm.save();
				            		} 
				            		catch (Exception e) 
				            		{
				            			System.out.println("[ProtectionStones] WorldGuard Error [" + e + "] during Region File Save");
				            		}
				            	}
				            	return true;
				            }
				        } 
						else 
						{
				            p.sendMessage(ChatColor.RED + "You don't have permission to use Region Commands");
				        }
				    } 
					else 
					{
				          p.sendMessage(ChatColor.YELLOW + "/ps region {count|list|remove|regen|disown} {playername}");
				          return true;
					}
				}
			/*****************************************************************************************************/
				if (args[0].equalsIgnoreCase("tp")) 
	            {
					if(p.hasPermission("protectionstones.tp"))
					{
						if(args.length!=2)
						{
							p.sendMessage(ChatColor.RED + "Usage: /ps tp [num]");
							p.sendMessage(ChatColor.YELLOW + "To see your ps count, type /ps count. Use any number within the range to teleport to that ps");
							return true;
						}
		            	Map<String, ProtectedRegion> regions = rgm.getRegions();
		            	String name = p.getName().toLowerCase();
		            	int size = regions.size();
		            	int rgnum = Integer.parseInt(args[1]);
		            	String[] regionIDList = new String[size];
		            	int index = 0, count = 0, tpx, tpy, tpz;
		            	String idname;
		            	try 
						{
							LocalPlayer lp = wg.wrapPlayer(p);
							count = rgm.getRegionCountOfPlayer(lp);
		                }
						catch (Exception e) {}	
		            	
		            	if(rgnum<=0)
		            	{
		            		p.sendMessage(ChatColor.RED + "Please enter a number above 0.");
		            		return true;
		            	}
		            	
		            	if(rgnum>count)
		            	{
		            		p.sendMessage(ChatColor.RED + "You only have " + count + " protected regions!");
		            		return true;
		            	}
		            	
		            	for (Iterator<String> playerCount = regions.keySet().iterator(); playerCount.hasNext(); ) 
		            	{
		            		idname = (String)playerCount.next();
		                	try 
		                	{
		                		if (((ProtectedRegion)regions.get(idname)).getOwners().getPlayers().contains(name)) 
		                		{
		                			regionIDList[index] = idname;
		                			if(index == (rgnum-1))
		                			{
		                				String[] pos = idname.split("x|y|z");
		                				if(pos.length==3)
		                				{
			                				pos[0] = pos[0].substring(2);
			                				p.sendMessage(ChatColor.GREEN + "Teleporting...");
			                				tpx = Integer.parseInt(pos[0]);
			                				tpy = Integer.parseInt(pos[1])+1;
			                				tpz = Integer.parseInt(pos[2]);
			                				Location tploc = new Location(p.getWorld(), tpx, tpy, tpz);
			                				p.teleport(tploc);
		                				}
		                				else
		                				{
		                					p.sendMessage(ChatColor.RED + "Error in teleporting to protected region!");
		                				}
		                				return true;
		                			}
		                			index++;
		                		}
		                	}
		                	catch (Exception localException6){} 
		                }
                		p.sendMessage(ChatColor.RED + "Could not find protected region!");
					}
					else
					{
						p.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
					}
	              return true;
	            }
			/*****************************************************************************************************/
				else if(args[0].equalsIgnoreCase("admin"))
				{
					if(!p.hasPermission("protectionstones.admin")) 
			        {
						p.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
			        }
					else
					{
						if(args.length<2)
						{
							p.sendMessage(ChatColor.RED + "Correct usage: /ps admin {version|settings|hide|unhide|");
					        p.sendMessage(ChatColor.RED + "                          cleanup|lastlogon|lastlogons|stats}");
						}
						else if(args.length==2)
						{
							if(args[1].equalsIgnoreCase("version"))
							{
								p.sendMessage(ChatColor.YELLOW + "ProtectionStones " + getDescription().getVersion());
								p.sendMessage(ChatColor.YELLOW + "CraftBukkit  " + Bukkit.getVersion());
							}
							else if(args[1].equalsIgnoreCase("settings"))
							{
								p.sendMessage(getConfig().saveToString().split("\n"));
							}
							Block blockToChange;
							if ((args[1].equalsIgnoreCase("hide")) || (args[1].equalsIgnoreCase("unhide"))) 
							{
								RegionManager mgr = wg.getRegionManager(p.getWorld());
				                Map<String, ProtectedRegion> regions = mgr.getRegions();
				                int regionSize = regions.size();
				                String[] regionIDList = new String[regionSize];
				                String blockMaterial = "AIR";
				                String hMessage = "hidden";
				                int index = 0;
				                for (String idname : regions.keySet()) 
				                {
				                	try 
				                	{
				                		if (idname.substring(0, 2).equals("ps")) 
				                		{
				                			regionIDList[index] = idname;
				                			index++;
				                		}
				                	}
				                	catch (Exception e){}
				                }
				                if (index == 0)
				                {
				                	p.sendMessage(ChatColor.YELLOW + "No ProtectionStones Regions Found");
				                }
				                else 
				                {
				                	for (int i = 0; i < index; i++) 
				                	{
				                		int indexX = regionIDList[i].indexOf("x");
				                		int indexY = regionIDList[i].indexOf("y");
				                		int indexZ = regionIDList[i].length() - 1;
				                		int psx = Integer.parseInt(regionIDList[i].substring(2, indexX));
				                		int psy = Integer.parseInt(regionIDList[i].substring(indexX + 1, indexY));
				                		int psz = Integer.parseInt(regionIDList[i].substring(indexY + 1, indexZ));
				                		blockToChange = p.getWorld().getBlockAt(psx, psy, psz);
				                		blockMaterial = "AIR";
				                		if (args[1].equalsIgnoreCase("unhide"))
				                		{
				                			//Vector minVector = rgm.getRegion(regionIDList[i]).getMinimumPoint();
					                        //Vector maxVector = rgm.getRegion(regionIDList[i]).getMaximumPoint();
					                        blockMaterial = Main.mat.name();
					                        //int minX = minVector.getBlockX();
					                        //int maxX = maxVector.getBlockX();
					                        //int size = (maxX - minX) / 2;
					                    }
				                		blockToChange.setType(Material.getMaterial(blockMaterial));
				                	}
				                }
				                
				                if (args[1].equalsIgnoreCase("unhide")) 
				                {
				                  hMessage = "unhidden";
				                }
				                p.sendMessage(ChatColor.YELLOW + "All ProtectionStones have been " + hMessage);
				            }
							else if (args[1].equalsIgnoreCase("cleanup")) 
							{
								if (args.length >= 3) 
								{
									if ((args[2].equalsIgnoreCase("remove")) || (args[2].equalsIgnoreCase("regen")) || (args[2].equalsIgnoreCase("disown"))) 
									{
										int days = 30;
					                    if (args.length > 3)
					                    {
					                    	days = Integer.parseInt(args[3]);
					                    }
					                    p.sendMessage(ChatColor.YELLOW + "Cleanup " + args[2] + " " + days + " days");
					                    p.sendMessage(ChatColor.YELLOW + "================");
					                    RegionManager mgr = wg.getRegionManager(p.getWorld());
					                    Map<String, ProtectedRegion> regions = mgr.getRegions();
					                    int size = regions.size();
					                    String name = "";
					                    int index = 0;
					                    String[] regionIDList = new String[size];
					                    OfflinePlayer[] offlinePlayerList = getServer().getOfflinePlayers();
					                    int playerCount = offlinePlayerList.length;
					                    for (int iii = 0; iii < playerCount; iii++) 
					                    {
					                    	long lastPlayed = (System.currentTimeMillis() - offlinePlayerList[iii].getLastPlayed()) / 86400000L;
					                    	if (lastPlayed >= days) 
					                    	{
					                    		index = 0;
					                    		name = offlinePlayerList[iii].getName().toLowerCase();
					                    		for (String idname : regions.keySet())
					                    		{
					                    			try
					                    			{
					                    				if (((ProtectedRegion)regions.get(idname)).getOwners().getPlayers().contains(name)) 
					                    				{
					                    					regionIDList[index] = idname;
					                    					index++;
					                    				}
					                    			}
					                    			catch (Exception e){}
					                    		}
					                    		if (index == 0) 
					                    		{
					                    			p.sendMessage(ChatColor.YELLOW + "No regions found for " + name);
					                    		} 
					                    		else 
					                    		{
					                    			p.sendMessage(ChatColor.YELLOW + args[2] + ": " + name);
					                    			for (int i = 0; i < index; i++)
					                    			{
					                    				if (args[2].equalsIgnoreCase("disown"))
					                    				{
					                    					DefaultDomain owners = rgm.getRegion(regionIDList[i]).getOwners();
					                    					owners.removePlayer(name);
					                    					rgm.getRegion(regionIDList[i]).setOwners(owners);
					                    				} 
					                    				else 
					                    				{
					                    					if (args[2].equalsIgnoreCase("regen"))
					                    					{
					                    						if (this.getServer().getPluginManager().getPlugin("WorldEdit") != null)
					                    						{
					                    							Bukkit.dispatchCommand(p, "region select " + regionIDList[i]);
					                    							Bukkit.dispatchCommand(p, "/regen");
					                    						}

					                    					}
					                    					else if (regionIDList[i].substring(0, 2).equals("ps")) 
					                    					{
					                    						int indexX = regionIDList[i].indexOf("x");
					                    						int indexY = regionIDList[i].indexOf("y");
					                    						int indexZ = regionIDList[i].length() - 1;
					                    						int psx = Integer.parseInt(regionIDList[i].substring(2, indexX));
					                    						int psy = Integer.parseInt(regionIDList[i].substring(indexX + 1, indexY));
					                    						int psz = Integer.parseInt(regionIDList[i].substring(indexY + 1, indexZ));
					                    						Block blockToRemove = p.getWorld().getBlockAt(psx, psy, psz);
					                    						blockToRemove.setType(Material.AIR);
					                    					}

					                    					mgr.removeRegion(regionIDList[i]);
					                    				}
					                    			}
					                    		}
					                    	}
					                    }
					                    try
					                    {
					                    	rgm.save();
					                    } 
					                    catch (Exception e) 
					                    {
					                    	System.out.println("[ProtectionStones] WorldGuard Error [" + e + "] during Region File Save");
					                    }
					                    p.sendMessage(ChatColor.YELLOW + "================");
					                    p.sendMessage(ChatColor.YELLOW + "Completed " + args[2] + " cleanup");
					                    return true;
				                    }
				                    
				                }
								else
								{
									p.sendMessage(ChatColor.YELLOW + "/ps admin cleanup {remove|regen|disown} {days}");
									return true;
								}
							}
							else if(args[1].equalsIgnoreCase("lastlogon"))
							{
								if (args.length > 2) 
								{
									String playerName = args[2];
									if (Bukkit.getPlayer(playerName).getFirstPlayed() > 0L) 
									{
										long lastPlayed = (System.currentTimeMillis() - Bukkit.getPlayer(playerName).getLastPlayed()) / 86400000L;
										p.sendMessage(ChatColor.YELLOW + playerName + " last played " + lastPlayed + " days ago.");
										if (Bukkit.getPlayer(playerName).isBanned())
										{
											p.sendMessage(ChatColor.YELLOW + playerName + " is banned.");
										}
					                }
					                else 
					                {
					                	p.sendMessage(ChatColor.YELLOW + "Player name not found.");
					                }
								} 
								else
								{
									p.sendMessage(ChatColor.YELLOW + "A player name is required.");
					            }
							}
							else if(args[1].equalsIgnoreCase("lastlogons"))
							{
								int days = 0;
								if (args.length > 2) 
								{
									days = Integer.parseInt(args[2]);
					            }
					            OfflinePlayer[] offlinePlayerList = getServer().getOfflinePlayers();
					            int playerCount = offlinePlayerList.length;
					            int playerCounter = 0;
					            p.sendMessage(ChatColor.YELLOW + "" + days + " Days Plus:");
					            p.sendMessage(ChatColor.YELLOW + "================");
					            for (int iii = 0; iii < playerCount; iii++) 
					            {
					            	long lastPlayed = (System.currentTimeMillis() - offlinePlayerList[iii].getLastPlayed()) / 86400000L;
					                if (lastPlayed >= days) 
					                {
					                	playerCounter++;
					                    p.sendMessage(ChatColor.YELLOW + offlinePlayerList[iii].getName() + " " + lastPlayed + " days");
					                }
					            }
					            p.sendMessage(ChatColor.YELLOW + "================");
					            p.sendMessage(ChatColor.YELLOW + "" + playerCounter + " Total Players Shown");
					            p.sendMessage(ChatColor.YELLOW + "" + playerCount + " Total Players Checked");
							}
							else if(args[1].equalsIgnoreCase("stats"))
							{
								if (args.length > 2) 
								{
									String playerName = args[2];
					                if (Bukkit.getPlayer(playerName).getFirstPlayed() > 0L)
					                {
					                	p.sendMessage(ChatColor.YELLOW + playerName + ":");
					                	p.sendMessage(ChatColor.YELLOW + "================");
					                	long firstPlayed = (System.currentTimeMillis() - Bukkit.getPlayer(playerName).getFirstPlayed()) / 86400000L;
					                	p.sendMessage(ChatColor.YELLOW + "First played " + firstPlayed + " days ago.");
					                	long lastPlayed = (System.currentTimeMillis() - Bukkit.getPlayer(playerName).getLastPlayed()) / 86400000L;
					                	p.sendMessage(ChatColor.YELLOW + "Last played " + lastPlayed + " days ago.");
					                	String banMessage = "Not Banned";
					                	if (Bukkit.getPlayer(playerName).isBanned()) 
					                	{
					                		banMessage = "Banned";
					                	}
					                	p.sendMessage(ChatColor.YELLOW + banMessage);
					                	int count = 0;
					                	try 
					                	{
					                		LocalPlayer thePlayer = null;
					                		thePlayer = wg.wrapPlayer(Bukkit.getPlayer(args[2]));
					                		count = rgm.getRegionCountOfPlayer(thePlayer);
					                	}
					                	catch (Exception localException1) {}
					                	p.sendMessage(ChatColor.YELLOW + "Regions: " + count);
					                	p.sendMessage(ChatColor.YELLOW + "================");
					                } 
					                else 
					                {
					                	p.sendMessage(ChatColor.YELLOW + "Player name not found.");
					                }
					                return true;
					            }
					            p.sendMessage(ChatColor.YELLOW + "World:");
					            p.sendMessage(ChatColor.YELLOW + "================");
					            int count = 0;
					            try 
					            {
					                count = rgm.size();
					            }
					            catch (Exception localException2) {}
					            p.sendMessage(ChatColor.YELLOW + "Regions: " + count);
					            p.sendMessage(ChatColor.YELLOW + "================");
							}
						}
					}
				}
				else
				{
					p.sendMessage(ChatColor.RED + "No such command. please type /ps help for more info");
				}
			}
		}
		return true;
	}
	
	public static Object getFlagValue(Flag<?> flag, Object value)
	{
		if (value == null) return null;
		
		String valueString = value.toString().trim();
		
		if ((flag instanceof StateFlag)) 
		{
			if (valueString.equalsIgnoreCase("allow")) return StateFlag.State.ALLOW;
			if (valueString.equalsIgnoreCase("deny")) return StateFlag.State.DENY;
			return null;
	    }
		return null;
	}
	
	protected void setBlock(World theWorld, int x, int y, int z, Material mat) 
	{
		Block blockToChange = theWorld.getBlockAt(x, y, z);
	    blockToChange.setType(mat);
	}
	
	protected Material getBlock(World theWorld, int x, int y, int z) 
	{
	    Block blockToReturn = theWorld.getBlockAt(x, y, z);
	    return blockToReturn.getType();
	}
}
