package me.vik1395.ProtectionStones.commands;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.vik1395.ProtectionStones.FlagHandler;
import me.vik1395.ProtectionStones.PSL;
import me.vik1395.ProtectionStones.ProtectionStones;
import org.bukkit.entity.Player;

public class ArgSethome {
    public static boolean argumentSethome(Player p, String[] args) {
        String psID = ProtectionStones.playerToPSID(p);

        WorldGuardPlugin wg = (WorldGuardPlugin) ProtectionStones.wgd;
        RegionManager rgm = ProtectionStones.getRegionManagerWithPlayer(p);
        if (!p.hasPermission("protectionstones.sethome")) {
            p.sendMessage(PSL.NO_PERMISSION_SETHOME.msg());
            return true;
        }
        if (psID.equals("") || !rgm.hasRegion(psID)) {
            p.sendMessage(PSL.NOT_IN_REGION.msg());
            return true;
        }
        ProtectedRegion r = rgm.getRegion(psID);
        if (ProtectionStones.hasNoAccess(r, p, wg.wrapPlayer(p), false)) {
            p.sendMessage(PSL.NO_ACCESS.msg());
            return true;
        }

        r.setFlag(FlagHandler.PS_HOME, p.getLocation().getBlockX() + " " + p.getLocation().getBlockY() + " " + p.getLocation().getBlockZ());
        p.sendMessage(PSL.SETHOME_SET.msg().replace("%psid%", psID));
        return true;
    }
}
