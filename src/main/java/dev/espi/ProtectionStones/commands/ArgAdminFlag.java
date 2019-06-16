package dev.espi.ProtectionStones.commands;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.ProtectionStones.ProtectionStones;
import dev.espi.ProtectionStones.utils.WGUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

public class ArgAdminFlag {
    static boolean argumentAdminFlag(CommandSender p, String[] args) {
        String flag, value = "", gee = "";
        if (args[2].equalsIgnoreCase("-g")) {
            flag = args[4];
            for (int i = 5; i < args.length; i++) value += args[i] + " ";
            gee = args[3];
        } else {
            flag = args[2];
            for (int i = 3; i < args.length; i++) value += args[i] + " ";
        }

        // TODO async
        for (World w : Bukkit.getWorlds()) {
            RegionManager rgm = WGUtils.getRegionManagerWithWorld(w);
            for (ProtectedRegion r : rgm.getRegions().values()) {
                if (ProtectionStones.isPSRegion(r)) {
                    ArgFlag.setFlag(r, p, flag, value.trim(), gee);
                }
            }
        }
        return true;
    }
}
