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
import dev.espi.ProtectionStones.utils.UUIDCache;
import dev.espi.ProtectionStones.utils.WGUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class ArgTp implements PSCommandArg {

    private static HashMap<UUID, Integer> waitCounter = new HashMap<>();
    private static HashMap<UUID, BukkitTask> taskCounter = new HashMap<>();

    // /ps tp, /ps home

    @Override
    public List<String> getNames() {
        return Collections.singletonList("tp");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args) {
        Player p = (Player) s;

        // preliminary checks
        if (!p.hasPermission("protectionstones.tp")) {
            PSL.msg(p, PSL.NO_PERMISSION_TP.msg());
            return true;
        } else if (args.length != 3) {
            PSL.msg(p, PSL.TP_HELP.msg());
            return true;
        }

        // get the region id the player wants to teleport to
        int regionNumber;
        try {
            regionNumber = Integer.parseInt(args[2]);
            if (regionNumber <= 0) {
                PSL.msg(p, PSL.NUMBER_ABOVE_ZERO.msg());
                return true;
            }
        } catch (NumberFormatException e) {
            PSL.msg(p, PSL.TP_VALID_NUMBER.msg());
            return true;
        }

        LocalPlayer rlp = null;
        // region checks, and set lp to offline player
        if (args[0].equalsIgnoreCase("tp")) {
            if (!UUIDCache.nameToUUID.containsKey(args[1])) {
                PSL.msg(p, PSL.PLAYER_NOT_FOUND.msg());
                return true;
            }
            try {
                rlp = WorldGuardPlugin.inst().wrapOfflinePlayer(Bukkit.getOfflinePlayer(UUIDCache.nameToUUID.get(args[1])));
            } catch (Exception e) {
                PSL.msg(p, PSL.REGION_ERROR_SEARCH.msg()
                        .replace("%player%", args[1]));
                return true;
            }

        }

        LocalPlayer lp = rlp;
        // run region search asynchronously to avoid blocking server thread
        Bukkit.getScheduler().runTaskAsynchronously(ProtectionStones.getInstance(), () -> {
            List<ProtectedRegion> regions = getRegionsPlayerHas(lp, WGUtils.getRegionManagerWithPlayer(p));

            // check if region was found
            if (regions.isEmpty()) {
                PSL.msg(p, PSL.REGION_NOT_FOUND_FOR_PLAYER.msg()
                        .replace("%player%", lp.getName()));
                return;
            } else if (regionNumber > regions.size()) {
                PSL.msg(p, PSL.ONLY_HAS_REGIONS.msg()
                        .replace("%player%", lp.getName())
                        .replace("%num%", "" + regionNumber));
                return;
            }

            PSRegion r = ProtectionStones.toPSRegion(p.getWorld(), regions.get(regionNumber-1));
            teleportPlayer(p, r);
        });

        return true;
    }

    // find regions that the player has
    static List<ProtectedRegion> getRegionsPlayerHas(LocalPlayer lp, RegionManager rgm) {
        List<ProtectedRegion> ret = new ArrayList<>();
        for (ProtectedRegion region : rgm.getRegions().values()) {
            if (ProtectionStones.isPSRegion(region) && region.getOwners().contains(lp)) {
                ret.add(region);
            }
        }
        return ret;
    }

    static void teleportPlayer(Player p, PSRegion r) {
        // teleport player
        if (r.getTypeOptions().tpWaitingSeconds == 0 || p.hasPermission("protectionstones.tp.bypasswait")) {
            // no teleport delay
            PSL.msg(p, PSL.TPING.msg());
            Bukkit.getScheduler().runTask(ProtectionStones.getInstance(), () -> p.teleport(r.getHome())); // run on main thread, not async
        } else if (!r.getTypeOptions().noMovingWhenTeleportWaiting) {
            // teleport delay, but doesn't care about moving
            p.sendMessage(PSL.TP_IN_SECONDS.msg().replace("%seconds%", "" + r.getTypeOptions().tpWaitingSeconds));

            Bukkit.getScheduler().runTaskLater(ProtectionStones.getInstance(), () -> {
                PSL.msg(p, PSL.TPING.msg());
                p.teleport(r.getHome());
            }, 20 * r.getTypeOptions().tpWaitingSeconds);

        } else {// delay and not allowed to move
            PSL.msg(p, PSL.TP_IN_SECONDS.msg().replace("%seconds%", "" + r.getTypeOptions().tpWaitingSeconds));
            Location l = p.getLocation().clone();
            UUID uuid = p.getUniqueId();

            // remove queued teleport if already running
            if (taskCounter.get(uuid) != null) removeUUIDTimer(uuid);

            // add teleport wait tasks to queue
            waitCounter.put(uuid, 0);
            taskCounter.put(uuid, Bukkit.getScheduler().runTaskTimer(ProtectionStones.getInstance(), () -> {
                        // cancel if the player is not on the server
                        if (Bukkit.getPlayer(uuid) == null) {
                            removeUUIDTimer(uuid);
                            return;
                        }

                        Player pl = Bukkit.getPlayer(uuid);
                        // increment seconds
                        waitCounter.put(uuid, waitCounter.get(uuid) + 1);
                        // if the player moved cancel it
                        if (l.getBlockX() != pl.getLocation().getBlockX() || l.getBlockY() != pl.getLocation().getBlockY() || l.getBlockZ() != pl.getLocation().getBlockZ()) {
                            PSL.msg(pl, PSL.TP_CANCELLED_MOVED.msg());
                            removeUUIDTimer(uuid);
                        } else if (waitCounter.get(uuid) == r.getTypeOptions().tpWaitingSeconds * 4) { // * 4 since this loops 4 times a second
                            // if the timer has passed, teleport and cancel
                            PSL.msg(pl, PSL.TPING.msg());
                            pl.teleport(r.getHome());
                            removeUUIDTimer(uuid);
                        }
                    }, 5, 5) // loop 4 times a second
            );
        }
    }

    private static void removeUUIDTimer(UUID uuid) {
        taskCounter.get(uuid).cancel();
        waitCounter.remove(uuid);
        taskCounter.remove(uuid);
    }
}
