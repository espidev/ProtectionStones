package dev.espi.protectionstones.commands;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

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
    public boolean executeArgument(CommandSender s, String[] args) {
        if (!s.hasPermission("protectionstones.merge")) {
            PSL.msg(s, PSL.NO_PERMISSION_MERGE.msg());
            return true;
        }

        Player p = (Player) s;
        if (args.length == 1) { // GUI
            PSRegion r = PSRegion.fromLocation(p.getLocation());
            if (r == null) {
                PSL.msg(s, PSL.NOT_IN_REGION.msg());
                return true;
            }

        } else if (args.length == 3) { // /ps merge [region] [root]
            RegionManager rm = WGUtils.getRegionManagerWithPlayer(p);
            ProtectedRegion region = rm.getRegion(args[1]), root = rm.getRegion(args[2]);
            if (region == null || root == null) {
                PSL.msg(p, PSL.REGION_DOES_NOT_EXIST.msg());
            } else {
                WGUtils.mergeRegions(root, region);
                PSL.msg(p, PSL.MERGE_MERGED.msg());
            }
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
