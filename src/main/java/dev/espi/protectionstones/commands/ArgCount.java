/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.espi.protectionstones.commands;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.PSGroupRegion;
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.utils.Permissions;
import dev.espi.protectionstones.utils.UUIDCache;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class ArgCount implements PSCommandArg {

    // Only PS regions, not other regions
    static int[] countRegionsOfPlayer(UUID uuid, World w) {
        int[] count = {0, 0}; // total, including merged
        try {
            RegionManager rgm = WGUtils.getRegionManagerWithWorld(w);
            for (ProtectedRegion pr : rgm.getRegions().values()) {
                if (ProtectionStones.isPSRegion(pr)) {
                    PSRegion r = PSRegion.fromWGRegion(w, pr);

                    if (r.isOwner(uuid)) {
                        count[0]++;
                        if (r instanceof PSGroupRegion) {
                            count[1] += ((PSGroupRegion) r).getMergedRegions().size();
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return count;
    }

    @Override
    public List<String> getNames() {
        return Collections.singletonList("count");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public List<String> getPermissionsToExecute() {
        return Arrays.asList(Permissions.COUNT, Permissions.COUNT__OTHERS);
    }

    @Override
    public HashMap<String, Boolean> getRegisteredFlags() {
        return null;
    }

    // /ps count
    @Override
    public boolean executeArgument(CommandSender s, String[] args, HashMap<String, String> flags) {
        Player p = (Player) s;
        Bukkit.getScheduler().runTaskAsynchronously(ProtectionStones.getInstance(), () -> {
            int[] count;

            if (args.length == 1) {
                if (!p.hasPermission(Permissions.COUNT)) {
                    PSL.msg(p, PSL.NO_PERMISSION_COUNT.msg());
                    return;
                }

                count = countRegionsOfPlayer(p.getUniqueId(), p.getWorld());
                PSL.msg(p, PSL.PERSONAL_REGION_COUNT.msg().replace("%num%", "" + count[0]));
                if (count[1] != 0) {
                    PSL.msg(p, PSL.PERSONAL_REGION_COUNT_MERGED.msg().replace("%num%", ""+count[1]));
                }

            } else if (args.length == 2) {

                if (!p.hasPermission(Permissions.COUNT__OTHERS)) {
                    PSL.msg(p, PSL.NO_PERMISSION_COUNT_OTHERS.msg());
                    return;
                }
                if (!UUIDCache.containsName(args[1])) {
                    PSL.msg(p, PSL.PLAYER_NOT_FOUND.msg());
                    return;
                }

                UUID countUuid = UUIDCache.getUUIDFromName(args[1]);
                count = countRegionsOfPlayer(countUuid, p.getWorld());

                PSL.msg(p, PSL.OTHER_REGION_COUNT.msg()
                        .replace("%player%", UUIDCache.getNameFromUUID(countUuid))
                        .replace("%num%", "" + count[0]));
                if (count[1] != 0) {
                    PSL.msg(p, PSL.OTHER_REGION_COUNT_MERGED.msg()
                            .replace("%player%", UUIDCache.getNameFromUUID(countUuid))
                            .replace("%num%", "" + count[1]));
                }
            } else {
                PSL.msg(p, PSL.COUNT_HELP.msg());
            }
        });
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return null;
    }

}
