package dev.espi.ProtectionStones.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import dev.espi.ProtectionStones.PSLocation;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

public class WGUtils {
    public static RegionManager getRegionManagerWithPlayer(Player p) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(p.getWorld()));
    }

    public static RegionManager getRegionManagerWithWorld(World w){
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(w));
    }

    // Turn WG region name into a location (ex. ps138x35y358z)
    public static PSLocation parsePSRegionToLocation(String regionName) {
        int psx = Integer.parseInt(regionName.substring(2, regionName.indexOf("x")));
        int psy = Integer.parseInt(regionName.substring(regionName.indexOf("x") + 1, regionName.indexOf("y")));
        int psz = Integer.parseInt(regionName.substring(regionName.indexOf("y") + 1, regionName.length() - 1));
        return new PSLocation(psx, psy, psz);
    }

    // Find the id of the current region the player is in and get WorldGuard player object for use later
    public static String playerToPSID(Player p) {
        BlockVector3 v = BlockVector3.at(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());
        String currentPSID = "";
        RegionManager rgm = WGUtils.getRegionManagerWithPlayer(p);
        List<String> idList = rgm.getApplicableRegionsIDs(v);
        if (idList.size() == 1) {
            if (idList.get(0).startsWith("ps")) currentPSID = idList.get(0);
        } else {
            // Get nearest protection stone if in overlapping region
            double distanceToPS = 10000D, tempToPS;
            for (String currentID : idList) {
                if (currentID.substring(0, 2).equals("ps")) {
                    PSLocation psl = WGUtils.parsePSRegionToLocation(currentID);
                    Location psLocation = new Location(p.getWorld(), psl.x, psl.y, psl.z);
                    tempToPS = p.getLocation().distance(psLocation);
                    if (tempToPS < distanceToPS) {
                        distanceToPS = tempToPS;
                        currentPSID = currentID;
                    }
                }
            }
        }
        return currentPSID;
    }
}
