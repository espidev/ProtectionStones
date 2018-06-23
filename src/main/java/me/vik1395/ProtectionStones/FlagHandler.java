package me.vik1395.ProtectionStones;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.google.common.base.Joiner;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.Arrays;

public class FlagHandler {
    WorldGuardPlugin wg = (WorldGuardPlugin)Main.wgd;

    public void setFlag(String[] args, ProtectedRegion region, Player p) {
        Flag<?> rawFlag = DefaultFlag.fuzzyMatchFlag(wg.getFlagRegistry(), args[1]);
        if(rawFlag instanceof StateFlag) {
            StateFlag flag = (StateFlag)rawFlag;    
            if (args[2].equalsIgnoreCase("default")) {
                region.setFlag(flag, flag.getDefault());
                region.setFlag(flag.getRegionGroupFlag(), null);
                p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append(args[1]).append(" flag has been set.").toString());
            } else {
                RegionGroup group = null;
                if (Arrays.toString(args).contains("-g")) {
                    int i = 0;
                    for (String s: args) {
                        i++;
                        if (s.equalsIgnoreCase("-g")) {
                            group = getRegionGroup(args[i]);
                        }
                    }
                }
                if(Arrays.toString(args).contains("allow")) {
                        region.setFlag(flag, StateFlag.State.ALLOW);
                        if (group != null) {
                            region.setFlag(flag.getRegionGroupFlag(), group);
                        }
                        p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append(args[1]).append(" flag has been set.").toString());
                } else if(Arrays.toString(args).contains("deny")) {
                    region.setFlag(flag, StateFlag.State.DENY);
                    if (group != null) {
                        region.setFlag(flag.getRegionGroupFlag(), group);
                    }
                    p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append(args[1]).append(" flag has been set.").toString());
                } else {
                    if (group != null) {
                        region.setFlag(flag.getRegionGroupFlag(), group);
                        p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append(args[1]).append(" flag has been set.").toString());
                    } else {
                        p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append(args[1]).append(" flag has ").append(ChatColor.RED).append("not").append(ChatColor.YELLOW).append(" been set.").toString());
                    }
                }
            }
        } else if(rawFlag instanceof DoubleFlag) {
            DoubleFlag flag = (DoubleFlag)rawFlag;
            if (args[2].equalsIgnoreCase("default")) {
                region.setFlag(flag, flag.getDefault());
                region.setFlag(flag.getRegionGroupFlag(), null);
            } else {
                region.setFlag(flag, Double.parseDouble(args[1]));
            }
            p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append(args[1]).append(" flag has been set.").toString());
        } else if(rawFlag instanceof IntegerFlag) {
            IntegerFlag flag = (IntegerFlag)rawFlag;
            if (args[2].equalsIgnoreCase("default")) {
                region.setFlag(flag, flag.getDefault());
                region.setFlag(flag.getRegionGroupFlag(), null);
            } else {
                region.setFlag(flag, Integer.parseInt(args[1]));
            }
            p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append(args[1]).append(" flag has been set.").toString());
        } else if(rawFlag instanceof StringFlag) {
            StringFlag flag = (StringFlag)rawFlag;
            if (args[2].equalsIgnoreCase("default")) {
                region.setFlag(flag, flag.getDefault());
                region.setFlag(flag.getRegionGroupFlag(), null);
            } else {
                String flagValue = Joiner.on(" ").join(args).substring(args[0].length()+args[1].length()+2);
                String msg = flagValue.replaceAll("%player%", p.getName());
                region.setFlag(flag, msg);
            }
            p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append(args[1]).append(" flag has been set.").toString());
        } else if(rawFlag instanceof BooleanFlag) {
            BooleanFlag flag = (BooleanFlag)rawFlag;
            if (args[2].equalsIgnoreCase("default")) {
                region.setFlag(flag, flag.getDefault());
                region.setFlag(flag.getRegionGroupFlag(), null);
                p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append(args[1]).append(" flag has been set.").toString());
            } else {
                if(args[2].equalsIgnoreCase("true")) {
                    region.setFlag(flag, true);
                    p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append(args[1]).append(" flag has been set.").toString());
                } else if(args[2].equalsIgnoreCase("false")) {
                    region.setFlag(flag, false);
                    p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append(args[1]).append(" flag has been set.").toString());
                }
            }
        }/* else if(rawFlag instanceof LocationFlag){ //
            System.out.print("LocationFlag!!");
            // NOT PROPERLY IMPLEMENTED YET
        } else if(rawFlag instanceof SetFlag){
            SetFlag flag = (SetFlag) rawFlag;
            if (args[2].equalsIgnoreCase("default")) {
                region.setFlag(flag, flag.getDefault());
                region.setFlag(flag.getRegionGroupFlag(), null);
            } else {
                region.setFlag(flag, args[2]);
            }
            p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append(args[1]).append(" flag has been set.").toString());
        }         NOT PROPERLY IMPLEMENTED YET      */
    }

    private RegionGroup getRegionGroup(String arg) {
        if (arg.equalsIgnoreCase("member") || arg.equalsIgnoreCase("members")) {
            return RegionGroup.MEMBERS;
        } else if (arg.equalsIgnoreCase("nonmembers") || arg.equalsIgnoreCase("nonmember") 
                || arg.equalsIgnoreCase("nomember")|| arg.equalsIgnoreCase("nomembers")
                || arg.equalsIgnoreCase("non_members") || arg.equalsIgnoreCase("non_member") 
                || arg.equalsIgnoreCase("no_member")|| arg.equalsIgnoreCase("no_members")) {
            return RegionGroup.NON_MEMBERS;
        } else if (arg.equalsIgnoreCase("nonowners") || arg.equalsIgnoreCase("nonowner") 
                || arg.equalsIgnoreCase("noowner")|| arg.equalsIgnoreCase("noowners")
                || arg.equalsIgnoreCase("non_owners") || arg.equalsIgnoreCase("non_owner") 
                || arg.equalsIgnoreCase("no_owner")|| arg.equalsIgnoreCase("no_owners")) {
            return RegionGroup.NON_OWNERS;
        } else if (arg.equalsIgnoreCase("owner") || arg.equalsIgnoreCase("owners")) {
            return RegionGroup.OWNERS;
        } else if (arg.equalsIgnoreCase("none") || arg.equalsIgnoreCase("noone")) {
            return RegionGroup.NONE;
        } else if (arg.equalsIgnoreCase("all") || arg.equalsIgnoreCase("everyone")) {
            return RegionGroup.ALL;
        } else if (arg.endsWith("empty")) {
            return null;
        }
        
        return null;
    }
}
