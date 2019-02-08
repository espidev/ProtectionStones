package me.vik1395.ProtectionStones.commands;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.vik1395.ProtectionStones.ProtectionStones;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ArgRegion {
    public static boolean argumentRegion(Player p, String[] args) {
        WorldGuardPlugin wg = (WorldGuardPlugin) ProtectionStones.wgd;
        RegionManager rgm = ProtectionStones.getRegionManagerWithPlayer(p);

        if (args.length < 3) {
            p.sendMessage(ChatColor.YELLOW + "/ps region {count|list|remove|regen|disown} {playername}");
            return true;
        }
        if (!p.hasPermission("protectionstones.region")) {
            p.sendMessage(ChatColor.RED + "You don't have permission to use Region Commands");
        }

        OfflinePlayer p2 = Bukkit.getOfflinePlayer(args[2]);

        if (args[1].equalsIgnoreCase("count")) {// count player's regions
            int count = ArgCount.countRegionsOfPlayer(wg.wrapPlayer(p2.getPlayer()).getUniqueId(), rgm); // TODO check if rgm needs to be p2's
            p.sendMessage(ChatColor.YELLOW + args[2] + "'s region count: " + count);

        } else if (args[1].equalsIgnoreCase("list")) { // list player's regions
            String name = args[2].toLowerCase();
            UUID playerid = Bukkit.getOfflinePlayer(args[2]).getUniqueId();

            StringBuilder regionMessage = new StringBuilder();
            boolean found = false;
            for (String s : rgm.getRegions().keySet()) {
                if (s.startsWith("ps") && rgm.getRegions().get(s).getOwners().contains(playerid)) {
                    regionMessage.append(s).append(", ");
                    found = true;
                }
            }

            if (!found) {
                p.sendMessage(ChatColor.YELLOW + "No regions found for " + name);
            } else {
                regionMessage = new StringBuilder(regionMessage.substring(0, regionMessage.length() - 2) + ".");
                p.sendMessage(ChatColor.YELLOW + args[2] + "'s regions: " + regionMessage);
            }
        } else if ((args[1].equalsIgnoreCase("remove")) || (args[1].equalsIgnoreCase("regen")) || (args[1].equalsIgnoreCase("disown"))) {

            RegionManager mgr = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(p.getWorld()));
            Map<String, ProtectedRegion> regions = mgr.getRegions();
            String name = args[2].toLowerCase();
            UUID playerid = Bukkit.getOfflinePlayer(name).getUniqueId();
            List<String> regionIDList = new ArrayList<>();
            int index = 0;
            for (String idname : regions.keySet()) {
                if (idname.startsWith("ps") && regions.get(idname).getOwners().contains(playerid)) {
                    regionIDList.add(idname);
                    index++;
                }
            }
                    if (index == 0) {
                        p.sendMessage(ChatColor.YELLOW + "No regions found for " + args[2]);
                    } else {
                        for (String s : regionIDList) {
                            if (args[1].equalsIgnoreCase("disown")) {
                                DefaultDomain owners = rgm.getRegion(s).getOwners();
                                owners.removePlayer(name);
                                owners.removePlayer(playerid);
                                rgm.getRegion(s).setOwners(owners);
                            } else {
                                if (args[1].equalsIgnoreCase("regen")) {
                                    Bukkit.dispatchCommand(p, "region select " + s);
                                    Bukkit.dispatchCommand(p, "/regen");
                                } else if (s.substring(0, 2).equals("ps")) {
                                    int psx = Integer.parseInt(s.substring(2, s.indexOf("x")));
                                    int psy = Integer.parseInt(s.substring(s.indexOf("x") + 1, s.indexOf("y")));
                                    int psz = Integer.parseInt(s.substring(s.indexOf("y") + 1, s.length() - 1));
                                    Block blockToRemove = p.getWorld().getBlockAt(psx, psy, psz);
                                    blockToRemove.setType(Material.AIR);
                                }
                                mgr.removeRegion(s);
                            }
                        }
                        p.sendMessage(ChatColor.YELLOW + name + "'s regions have been removed");
                        try {
                            rgm.save();
                        } catch (Exception e) {
                            System.out.println("[ProtectionStones] WorldGuard Error [" + e + "] during Region File Save");
                        }
                    }
                }
        return true;
    }
}
