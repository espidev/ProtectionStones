package dev.espi.protectionstones.commands;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.utils.WGMerge;
import dev.espi.protectionstones.utils.WGUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class ArgMerge implements PSCommandArg {
    @Override
    public List<String> getNames() {
        return Arrays.asList("merge");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public List<String> getPermissionsToExecute() {
        return Arrays.asList("protectionstones.merge");
    }

    @Override
    public HashMap<String, Boolean> getRegisteredFlags() {
        return null;
    }

    public static List<TextComponent> getGUI(Player p, PSRegion r) {
        List<TextComponent> ret = new ArrayList<>();
        for (ProtectedRegion pr : r.getWGRegionManager().getApplicableRegions(r.getWGRegion()).getRegions()) {
            PSRegion psr = PSRegion.fromWGRegion(p.getWorld(), pr);
            if (psr != null && psr.getTypeOptions().allowMerging && !pr.getId().equals(r.getID()) && (psr.isOwner(p.getUniqueId()) || p.hasPermission("protectionstones.admin"))) {
                TextComponent tc = new TextComponent(ChatColor.AQUA + "> " + ChatColor.WHITE + pr.getId());

                if (psr.getName() != null) tc.addExtra(" (" + psr.getName() + ")");
                tc.addExtra(" (" + psr.getTypeOptions().alias + ")");

                tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + ProtectionStones.getInstance().getConfigOptions().base_command + " merge " + r.getID() + " " + pr.getId()));
                tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(PSL.MERGE_CLICK_TO_MERGE.msg().replace("%region%", pr.getId())).create()));
                ret.add(tc);
            }
        }
        return ret;
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args, HashMap<String, String> flags) {
        if (!s.hasPermission("protectionstones.merge")) {
            PSL.msg(s, PSL.NO_PERMISSION_MERGE.msg());
            return true;
        }

        if (!ProtectionStones.getInstance().getConfigOptions().allowMergingRegions) {
            PSL.msg(s, PSL.MERGE_DISABLED.msg());
            return true;
        }

        Player p = (Player) s;
        if (args.length == 1) { // GUI

            PSRegion r = PSRegion.fromLocation(p.getLocation());
            if (r == null) {
                PSL.msg(s, PSL.NOT_IN_REGION.msg());
                return true;
            }
            if (!r.getTypeOptions().allowMerging) {
                PSL.msg(s, PSL.MERGE_NOT_ALLOWED.msg());
                return true;
            }

            List<TextComponent> components = getGUI(p, r);
            if (components.isEmpty()) {
                PSL.msg(p, PSL.MERGE_NO_REGIONS.msg());
            } else {
                p.sendMessage(ChatColor.WHITE + ""); // send empty line
                PSL.msg(p, PSL.MERGE_HEADER.msg().replace("%region%", r.getID()));
                PSL.msg(p, PSL.MERGE_WARNING.msg());
                for (TextComponent tc : components) p.spigot().sendMessage(tc);
                p.sendMessage(ChatColor.WHITE + ""); // send empty line
            }

        } else if (args.length == 3) { // /ps merge [region] [root]
            RegionManager rm = WGUtils.getRegionManagerWithPlayer(p);
            ProtectedRegion region = rm.getRegion(args[1]), root = rm.getRegion(args[2]);
            LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(p);

            if (!ProtectionStones.isPSRegion(region) || !ProtectionStones.isPSRegion(root)) {
                PSL.msg(p, PSL.MULTI_REGION_DOES_NOT_EXIST.msg());
                return true;
            }
            if (!p.hasPermission("protectionstones.admin") && (!region.isOwner(lp) || !root.isOwner(lp))) {
                PSL.msg(p, PSL.NO_ACCESS.msg());
                return true;
            }
            if (!root.getIntersectingRegions(Collections.singletonList(region)).contains(region)) {
                PSL.msg(p, PSL.REGION_NOT_OVERLAPPING.msg());
                return true;
            }

            PSRegion aRegion = PSRegion.fromWGRegion(p.getWorld(), region), aRoot = PSRegion.fromWGRegion(p.getWorld(), root);
            if (!aRegion.getTypeOptions().allowMerging || !aRoot.getTypeOptions().allowMerging) {
                PSL.msg(p, PSL.MERGE_NOT_ALLOWED.msg());
                return true;
            }

            Bukkit.getScheduler().runTaskAsynchronously(ProtectionStones.getInstance(), () -> {
                WGMerge.mergeRegions(p.getWorld(), rm, aRoot, Arrays.asList(aRegion, aRoot));
                PSL.msg(p, PSL.MERGE_MERGED.msg());

                // show menu again if the new region still has overlapping regions
                Bukkit.getScheduler().runTask(ProtectionStones.getInstance(), () -> {
                    if (!getGUI(p, PSRegion.fromWGRegion(p.getWorld(), rm.getRegion(aRoot.getID()))).isEmpty()) {
                        Bukkit.dispatchCommand(p, ProtectionStones.getInstance().getConfigOptions().base_command + " merge");
                    }
                });
            });

        } else {
            PSL.msg(s, PSL.MERGE_HELP.msg());
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return null;
    }
}
