/*
 * Copyright 2019 ProtectionStones team and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.espi.ProtectionStones.commands;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.ProtectionStones.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArgTp {

    private static HashMap<UUID, Integer> waitCounter = new HashMap<>();
    private static HashMap<UUID, BukkitTask> taskCounter = new HashMap<>();

    // /ps tp, /ps home
    public static boolean argumentTp(Player p, String[] args) {
        WorldGuardPlugin wg = (WorldGuardPlugin) ProtectionStones.wgd;
        RegionManager rgm = ProtectionStones.getRegionManagerWithPlayer(p);

        String rgnumParse;
        int rgnum; // index: index in playerRegions for selected region, rgnum: index specified by player to teleport to
        Map<Integer, String> playerRegions = new HashMap<>();

        // preliminary checks
        if (args[0].equalsIgnoreCase("tp")) { // argument tp
            if (!p.hasPermission("protectionstones.tp")) {
                PSL.msg(p, PSL.NO_PERMISSION_TP.msg());
                return true;
            } else if (args.length != 3) {
                PSL.msg(p, PSL.TP_HELP.msg());
                return true;
            }
            rgnumParse = args[2];
        } else { // argument home
            if (!p.hasPermission("protectionstones.home")) {
                PSL.msg(p, PSL.NO_PERMISSION_HOME.msg());
                return true;
            } else if (args.length != 2) {
                PSL.msg(p, PSL.HOME_HELP.msg());
                return true;
            }
            rgnumParse = args[1];
        }

        try {
            rgnum = Integer.parseInt(rgnumParse);
        } catch (NumberFormatException e) {
            PSL.msg(p, PSL.TP_VALID_NUMBER.msg());
            return true;
        }

        if (rgnum <= 0) {
            PSL.msg(p, PSL.NUMBER_ABOVE_ZERO.msg());
            return true;
        }

        LocalPlayer lp = wg.wrapPlayer(p);

        // region checks, and set lp to offline player (for /ps tp)
        if (args[0].equalsIgnoreCase("tp")) {

            if (!ProtectionStones.nameToUUID.containsKey(args[1])) {
                PSL.msg(p, PSL.PLAYER_NOT_FOUND.msg());
                return true;
            }

            try {
                lp = wg.wrapOfflinePlayer(Bukkit.getOfflinePlayer(ProtectionStones.nameToUUID.get(args[1])));
            } catch (Exception e) {
                PSL.msg(p, PSL.REGION_ERROR_SEARCH.msg()
                        .replace("%player%", args[1]));
                return true;
            }

        }

        // run region search asynchronously to avoid blocking server thread
        LocalPlayer finalLp = lp;
        Bukkit.getScheduler().runTaskAsynchronously(ProtectionStones.getPlugin(), () -> {
            int index = 0;

            // find regions that the player has
            for (String region : rgm.getRegions().keySet()) {
                if (region.startsWith("ps")) {
                    if (rgm.getRegions().get(region).getOwners().contains(finalLp)) {
                        index++;
                        playerRegions.put(index, region);
                    }
                }
            }

            // check if region was found
            if (args[0].equalsIgnoreCase("tp")) {
                if (index <= 0) {
                    PSL.msg(p, PSL.REGION_NOT_FOUND_FOR_PLAYER.msg()
                            .replace("%player%", finalLp.getName()));
                    return;
                } else if (rgnum > index) {
                    PSL.msg(p, PSL.ONLY_HAS_REGIONS.msg()
                            .replace("%player%", finalLp.getName())
                            .replace("%num%", "" + index));
                    return;
                }
            } else if (args[0].equalsIgnoreCase("home")) {
                if (index <= 0) {
                    PSL.msg(p, PSL.NO_REGIONS_OWNED.msg());
                    return;
                } else if (rgnum > index) {
                    PSL.msg(p, PSL.HOME_ONLY.msg().replace("%num%", "" + index));
                    return;
                }
            }

            ProtectedRegion r = rgm.getRegion(playerRegions.get(rgnum));
            ConfigProtectBlock cpb = ProtectionStones.getBlockOptions(r.getFlag(FlagHandler.PS_BLOCK_MATERIAL));

            // if the region does not have the ps-home flag, add it
            if (r.getFlag(FlagHandler.PS_HOME) == null) {
                PSLocation psl = ProtectionStones.parsePSRegionToLocation(r.getId());
                String home = psl.x + cpb.homeXOffset + " ";
                home += (psl.y + cpb.homeYOffset) + " ";
                home += (psl.z + cpb.homeZOffset);
                r.setFlag(FlagHandler.PS_HOME, home);
            }

            // get flag ps-home for ps teleport location
            String[] pos = r.getFlag(FlagHandler.PS_HOME).split(" ");

            if (pos.length != 3) {
                PSL.msg(p, PSL.TP_ERROR_NAME.msg());
                return;
            }

            // teleport player
            Location tploc = new Location(p.getWorld(), Integer.parseInt(pos[0]), Integer.parseInt(pos[1]), Integer.parseInt(pos[2]));

            if (cpb.tpWaitingSeconds == 0 || p.hasPermission("protectionstones.tp.bypasswait")) { // no delay
                PSL.msg(p, PSL.TPING.msg());
                Bukkit.getScheduler().runTask(ProtectionStones.getPlugin(), () -> p.teleport(tploc)); // run on main thread, not async
            } else if (!cpb.noMovingWhenTeleportWaiting) { // delay
                p.sendMessage(PSL.TP_IN_SECONDS.msg().replace("%seconds%", "" + cpb.tpWaitingSeconds));

                Bukkit.getScheduler().runTaskLater(ProtectionStones.getPlugin(), () -> {
                    PSL.msg(p, PSL.TPING.msg());
                    p.teleport(tploc);
                }, 20 * cpb.tpWaitingSeconds);

            } else {// delay and not allowed to move
                PSL.msg(p, PSL.TP_IN_SECONDS.msg().replace("%seconds%", "" + cpb.tpWaitingSeconds));
                Location l = p.getLocation().clone();
                UUID uuid = p.getUniqueId();

                // remove queued teleport if already running
                if (taskCounter.get(uuid) != null) {
                    taskCounter.get(uuid).cancel();
                    waitCounter.remove(uuid);
                    taskCounter.remove(uuid);
                }

                // add teleport wait tasks to queue
                waitCounter.put(uuid, 0);
                taskCounter.put(uuid, Bukkit.getScheduler().runTaskTimer(ProtectionStones.getPlugin(), () -> {
                            // cancel if the player is not on the server
                            if (Bukkit.getPlayer(uuid) == null) {
                                taskCounter.get(uuid).cancel();
                                waitCounter.remove(uuid);
                                taskCounter.remove(uuid);
                                return;
                            }

                            Player pl = Bukkit.getPlayer(uuid);
                            // increment seconds
                            waitCounter.put(uuid, waitCounter.get(uuid) + 1);
                            // if the player moved cancel it
                            if (l.getX() != pl.getLocation().getX() || l.getY() != pl.getLocation().getY() || l.getZ() != pl.getLocation().getZ()) {
                                PSL.msg(pl, PSL.TP_CANCELLED_MOVED.msg());
                                taskCounter.get(uuid).cancel();
                                waitCounter.remove(uuid);
                                taskCounter.remove(uuid);
                            } else if (waitCounter.get(uuid) == cpb.tpWaitingSeconds * 4) { // * 4 since this loops 4 times a second
                                // if the timer has passed, teleport and cancel
                                PSL.msg(pl, PSL.TPING.msg());
                                pl.teleport(tploc);
                                taskCounter.get(uuid).cancel();
                                waitCounter.remove(uuid);
                                taskCounter.remove(uuid);
                            }
                        }, 5, 5) // loop 4 times a second
                );
            }
        });

        return true;
    }
}
