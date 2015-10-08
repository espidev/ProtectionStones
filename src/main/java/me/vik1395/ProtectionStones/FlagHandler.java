package me.vik1395.ProtectionStones;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.google.common.base.Joiner;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class FlagHandler 
{
	WorldGuardPlugin wg = (WorldGuardPlugin)Main.wgd;
	
	public void setFlag(String[] args, ProtectedRegion region, Player p)
	{
		Flag<?> rawFlag =  DefaultFlag.fuzzyMatchFlag(args[1]);
		if(rawFlag instanceof StateFlag)
		{
			StateFlag flag = (StateFlag)rawFlag;
			
			if(args[2].equalsIgnoreCase("allow"))
			{
				region.setFlag(flag, StateFlag.State.ALLOW);
				p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append(args[1]).append(" flag has been set.").toString());
			}
			else if(args[2].equalsIgnoreCase("deny"))
			{
				region.setFlag(flag, StateFlag.State.DENY);
				p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append(args[1]).append(" flag has been set.").toString());
			}
		}
		
		else if(rawFlag instanceof DoubleFlag)
		{
			DoubleFlag flag = (DoubleFlag)rawFlag;
			region.setFlag(flag, Double.parseDouble(args[1]));
			p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append(args[1]).append(" flag has been set.").toString());
		}
		
		else if(rawFlag instanceof IntegerFlag)
		{
			IntegerFlag flag = (IntegerFlag)rawFlag;
			region.setFlag(flag, Integer.parseInt(args[1]));
			p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append(args[1]).append(" flag has been set.").toString());
		}
		
		else if(rawFlag instanceof StringFlag)
		{
			StringFlag flag = (StringFlag)rawFlag;
			String flagValue = Joiner.on(" ").join(args).substring(args[0].length()+args[1].length()+2);
			String msg = flagValue.replaceAll("%player%", p.getName());
			region.setFlag(flag, msg);
			p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append(args[1]).append(" flag has been set.").toString());
		}
		
		else if(rawFlag instanceof BooleanFlag)
		{
			BooleanFlag flag = (BooleanFlag)rawFlag;
			
			if(args[2].equalsIgnoreCase("true"))
			{
				region.setFlag(flag, true);
				p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append(args[1]).append(" flag has been set.").toString());
			}
			else if(args[2].equalsIgnoreCase("false"))
			{
				region.setFlag(flag, false);
				p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append(args[1]).append(" flag has been set.").toString());
			}
		}
	}
}
