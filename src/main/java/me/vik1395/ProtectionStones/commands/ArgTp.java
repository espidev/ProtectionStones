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

package me.vik1395.ProtectionStones.commands;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.vik1395.ProtectionStones.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
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

        int index = 0, rgnum; // index: index in playerRegions for selected region, rgnum: index specified by player to teleport to
        Map<Integer, String> playerRegions = new HashMap<>();

        // preliminary checks
        if (args[0].equalsIgnoreCase("tp")) { // argument tp
            if (!p.hasPermission("protectionstones.tp")) {
                p.sendMessage(PSL.NO_PERMISSION_TP.msg());
                return true;
            } else if (args.length != 3) {
                p.sendMessage(PSL.TP_HELP.msg());
                return true;
            }
            rgnum = Integer.parseInt(args[2]);
        } else { // argument home
            if (!p.hasPermission("protectionstones.home")) {
                p.sendMessage(PSL.NO_PERMISSION_HOME.msg());
                return true;
            } else if (args.length != 2) {
                p.sendMessage(PSL.HOME_HELP.msg());
                return true;
            }
            rgnum = Integer.parseInt(args[1]);
        }

        if (rgnum <= 0) {
            p.sendMessage(PSL.NUMBER_ABOVE_ZERO.msg());
            return true;
        }

        // region checks
        if (args[0].equalsIgnoreCase("tp")) {

            if (!ProtectionStones.nameToUUID.containsKey(args[1])) {
                p.sendMessage(PSL.PLAYER_NOT_FOUND.msg());
                return true;
            }

            LocalPlayer lp;
            try {
                lp = wg.wrapOfflinePlayer(Bukkit.getOfflinePlayer(ProtectionStones.nameToUUID.get(args[1])));
            } catch (Exception e) {
                p.sendMessage(PSL.REGION_ERROR_SEARCH.msg()
                        .replace("%player%", args[1]));
                return true;
            }

            // find regions that the player has
            for (String region : rgm.getRegions().keySet()) {
                if (region.startsWith("ps")) {
                    if (rgm.getRegions().get(region).getOwners().contains(lp)) {
                        index++;
                        playerRegions.put(index, region);
                    }
                }
            }

            if (index <= 0) {
                p.sendMessage(PSL.REGION_NOT_FOUND_FOR_PLAYER.msg()
                        .replace("%player%", lp.getName()));
                return true;
            } else if (rgnum > index) {
                p.sendMessage(PSL.ONLY_HAS_REGIONS.msg()
                        .replace("%player%", lp.getName())
                        .replace("%num%", "" + index));
                return true;
            }
        } else if (args[0].equalsIgnoreCase("home")) {
            // find regions that the player has
            for (String region : rgm.getRegions().keySet()) {
                if (region.startsWith("ps")) {
                    if (rgm.getRegions().get(region).getOwners().contains(wg.wrapPlayer(p))) {
                        index++;
                        playerRegions.put(index, region);
                    }
                }
            }

            if (index <= 0) {
                p.sendMessage(PSL.NO_REGIONS_OWNED.msg());
            }
            if (rgnum > index) {
                p.sendMessage(PSL.HOME_ONLY.msg().replace("%num%", "" + index));
                return true;
            }
        }

        if (!(rgnum <= index)) {
            p.sendMessage(PSL.TP_ERROR_TP.msg());
            return true;
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
            p.sendMessage(PSL.TP_ERROR_NAME.msg());
            return true;
        }

        // teleport player
        Location tploc = new Location(p.getWorld(), Integer.parseInt(pos[0]), Integer.parseInt(pos[1]), Integer.parseInt(pos[2]));

        if (cpb.tpWaitingSeconds == 0 || p.hasPermission("protectionstones.tp.bypasswait")) { // no delay
            p.sendMessage(PSL.TPING.msg());
            p.teleport(tploc);
        } else if (!cpb.noMovingWhenTeleportWaiting) { // delay

            p.sendMessage(PSL.TP_IN_SECONDS.msg().replace("%seconds%", "" + cpb.tpWaitingSeconds));
            Bukkit.getScheduler().runTaskLater(ProtectionStones.getPlugin(), () -> {
                p.sendMessage(PSL.TPING.msg());
                p.teleport(tploc);
            }, 20 * cpb.tpWaitingSeconds);

        } else {// delay and not allowed to move
            p.sendMessage(PSL.TP_IN_SECONDS.msg().replace("%seconds%", "" + cpb.tpWaitingSeconds));
            Location l = p.getLocation().clone();
            UUID uuid = p.getUniqueId();

            // remove queued teleport if already running
            if (taskCounter.get(uuid) != null) {
                taskCounter.get(uuid).cancel();
                waitCounter.remove(uuid);
                taskCounter.remove(uuid);
            }

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
                            pl.sendMessage(PSL.TP_CANCELLED_MOVED.msg());
                            taskCounter.get(uuid).cancel();
                            waitCounter.remove(uuid);
                            taskCounter.remove(uuid);
                        } else if (waitCounter.get(uuid) == cpb.tpWaitingSeconds*4) { // * 4 since this loops 4 times a second
                            // if the timer has passed, teleport and cancel
                            pl.sendMessage(PSL.TPING.msg());
                            pl.teleport(tploc);
                            taskCounter.get(uuid).cancel();
                            waitCounter.remove(uuid);
                            taskCounter.remove(uuid);
                        }
                    }, 5, 5) // loop 4 times a second
            );
        }
        return true;
    }
}
