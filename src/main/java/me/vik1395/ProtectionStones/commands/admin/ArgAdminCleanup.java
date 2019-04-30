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

package me.vik1395.ProtectionStones.commands.admin;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.vik1395.ProtectionStones.PSL;
import me.vik1395.ProtectionStones.ProtectionStones;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArgAdminCleanup {

    // /ps admin cleanup
    public static boolean argumentAdminCleanup(CommandSender p, String[] args) {
        WorldGuardPlugin wg = (WorldGuardPlugin) ProtectionStones.wgd;
        if (args.length < 3 || (!args[2].equalsIgnoreCase("remove") && !args[2].equalsIgnoreCase("disown"))) {
            PSL.msg(p, PSL.ADMIN_CLEANUP_HELP.msg());
            return true;
        }

        RegionManager rgm;
        World w;
        if (p instanceof Player) {
            rgm = ProtectionStones.getRegionManagerWithPlayer((Player) p);
            w = ((Player) p).getWorld();
        } else {
            if (args.length != 5) {
                PSL.msg(p, PSL.ADMIN_CONSOLE_WORLD.msg());
                return true;
            }
            if (Bukkit.getWorld(args[4]) == null) {
                PSL.msg(p, PSL.INVALID_WORLD.msg());
                return true;
            }
            w = Bukkit.getWorld(args[4]);
            rgm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(w));
        }

        Map<String, ProtectedRegion> regions = rgm.getRegions();

        Bukkit.getScheduler().runTaskAsynchronously(ProtectionStones.getPlugin(), () -> {
            if ((args[2].equalsIgnoreCase("remove")) || (args[2].equalsIgnoreCase("disown"))) {
                int days = (args.length > 3) ? Integer.parseInt(args[3]) : 30; // 30 days is default if days aren't specified

                PSL.msg(p, PSL.ADMIN_CLEANUP_HEADER.msg()
                        .replace("%arg%", args[2])
                        .replace("%days%", "" + days));

                // loop over offline players to delete old data
                for (OfflinePlayer op : Bukkit.getServer().getOfflinePlayers()) {
                    long lastPlayed = (System.currentTimeMillis() - op.getLastPlayed()) / 86400000L;
                    if (lastPlayed < days) continue; // skip if the player hasn't been gone for that long

                    LocalPlayer lp = wg.wrapOfflinePlayer(op);

                    List<String> opRegions = new ArrayList<>();
                    // add player's owned regions to list
                    boolean found = false;
                    for (String idname : regions.keySet()) {
                        try {
                            if (regions.get(idname).getOwners().contains(lp)) {
                                opRegions.add(idname);
                                found = true;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (!found) {

                        PSL.msg(p, PSL.REGION_NOT_FOUND_FOR_PLAYER.msg()
                                .replace("%player%", op.getName()));

                        continue;
                    }

                    // remove regions
                    p.sendMessage(ChatColor.YELLOW + args[2] + ": " + op.getName());
                    for (String region : opRegions) {
                        Bukkit.getScheduler().runTask(ProtectionStones.getPlugin(), ()-> ProtectionStones.removeDisownPSRegion(lp, args[2].toLowerCase(), region, rgm, w));
                    }
                }

                try {
                    rgm.save();
                } catch (Exception e) {
                    Bukkit.getLogger().severe("[ProtectionStones] WorldGuard Error [" + e + "] during Region File Save");
                }
                PSL.msg(p, PSL.ADMIN_CLEANUP_FOOTER.msg()
                        .replace("%arg%", args[2]));
            }
        });
        return true;
    }
}
