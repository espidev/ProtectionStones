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

import dev.espi.protectionstones.PSGroupRegion;
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.PSPlayer;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.utils.UUIDCache;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class ArgCount implements PSCommandArg {

    // Only PS regions, not other regions
    static int[] countRegionsOfPlayer(UUID uuid, World w) {
        int[] count = {0, 0}; // total, including merged

        PSPlayer psp = PSPlayer.fromUUID(uuid);
        psp.getPSRegions(w, false).forEach(r -> {
            count[0]++;
            if (r instanceof PSGroupRegion) {
                count[1] += ((PSGroupRegion) r).getMergedRegions().size();
            }
        });

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
        return Arrays.asList("protectionstones.count", "protectionstones.count.others");
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
                if (!p.hasPermission("protectionstones.count")) {
                    PSL.msg(p, PSL.NO_PERMISSION_COUNT.msg());
                    return;
                }

                count = countRegionsOfPlayer(p.getUniqueId(), p.getWorld());
                PSL.msg(p, PSL.PERSONAL_REGION_COUNT.replace("%num%", "" + count[0]));
                if (count[1] != 0) {
                    PSL.msg(p, PSL.PERSONAL_REGION_COUNT_MERGED.replace("%num%", ""+count[1]));
                }

            } else if (args.length == 2) {

                if (!p.hasPermission("protectionstones.count.others")) {
                    PSL.msg(p, PSL.NO_PERMISSION_COUNT_OTHERS.msg());
                    return;
                }
                if (!UUIDCache.containsName(args[1])) {
                    PSL.msg(p, PSL.PLAYER_NOT_FOUND.msg());
                    return;
                }

                UUID countUuid = UUIDCache.getUUIDFromName(args[1]);
                count = countRegionsOfPlayer(countUuid, p.getWorld());

                PSL.msg(p, PSL.OTHER_REGION_COUNT.replaceAll(Map.of(
                        "%player%", UUIDCache.getNameFromUUID(countUuid),
                        "%num%", String.valueOf(count[0])
                )));

                if (count[1] != 0) {
                    PSL.msg(p, PSL.OTHER_REGION_COUNT_MERGED.replaceAll(Map.of(
                            "%player%", UUIDCache.getNameFromUUID(countUuid),
                            "%num%", String.valueOf(count[1])
                    )));
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
