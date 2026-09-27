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
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

class ArgAdminCleanup {

    private static File previewFile;
    private static FileWriter previewFileOutputStream;

    static boolean argumentAdminCleanup(CommandSender p, String[] preParseArgs) {
        if (preParseArgs.length < 3 || !Arrays.asList("remove", "preview").contains(preParseArgs[2].toLowerCase())) {
            PSL.msg(p, ArgAdmin.getCleanupHelp());
            return true;
        }

        String cleanupOperation = preParseArgs[2].toLowerCase();

        World w;
        String alias = null;

        List<String> args = new ArrayList<>();

        for (int i = 3; i < preParseArgs.length; i++) {
            if (preParseArgs[i].equals("-t") && i != preParseArgs.length - 1) {
                alias = preParseArgs[++i];
            } else {
                args.add(preParseArgs[i]);
            }
        }

        if (args.size() > 1 && Bukkit.getWorld(args.get(1)) != null) {
            w = Bukkit.getWorld(args.get(1));
        } else {
            if (p instanceof Player) {
                w = ((Player) p).getWorld();
            } else {
                PSL.msg(p, args.size() > 1 ? PSL.INVALID_WORLD.msg() : PSL.ADMIN_CONSOLE_WORLD.msg());
                return true;
            }
        }

        int days;
        try {
            days = (args.size() > 0) ? Integer.parseInt(args.get(0)) : 30;
        } catch (Exception e) {
            PSL.msg(p, PSL.ADMIN_ERROR_PARSING.msg());
            return true;
        }

        if (cleanupOperation.equals("preview")) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd H-m-s");
            previewFile = new File(ProtectionStones.getInstance().getDataFolder().getAbsolutePath() + "/" + LocalDateTime.now().format(formatter) + " cleanup preview.txt");
            try {
                previewFile.createNewFile();
                previewFileOutputStream = new FileWriter(previewFile);
            } catch (IOException e) {
                e.printStackTrace();
                p.sendMessage(ChatColor.RED + "Internal error, please check the console logs.");
                return true;
            }
        }

        RegionManager rgm = WGUtils.getRegionManagerWithWorld(w);
        if (rgm == null) {
            PSL.msg(p, PSL.INVALID_WORLD.msg());
            return true;
        }

        Map<String, ProtectedRegion> regions = new HashMap<>(rgm.getRegions());
        String finalAlias = alias;
        int finalDays = days;

        Bukkit.getScheduler().runTaskAsynchronously(ProtectionStones.getInstance(), () -> {
            PSL.msg(p, PSL.ADMIN_CLEANUP_HEADER.msg()
                    .replace("%arg%", cleanupOperation)
                    .replace("%days%", "" + finalDays));

            HashSet<UUID> relevantPlayers = new HashSet<>();
            List<PSRegion> psRegions = new ArrayList<>();

            for (ProtectedRegion protectedRegion : regions.values()) {
                PSRegion r = PSRegion.fromWGRegion(w, protectedRegion);
                if (r == null) {
                    continue;
                }

                if (finalAlias != null && (r.getTypeOptions() == null || !r.getTypeOptions().alias.equals(finalAlias))) {
                    continue;
                }

                psRegions.add(r);
                relevantPlayers.addAll(r.getOwners());
                relevantPlayers.addAll(r.getMembers());
            }

            HashSet<UUID> activePlayers = new HashSet<>();
            long now = System.currentTimeMillis();

            for (UUID uuid : relevantPlayers) {
                try {
                    OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
                    long lastPlayed = (now - op.getLastPlayed()) / 86400000L;
                    if (lastPlayed < finalDays) {
                        activePlayers.add(uuid);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            List<PSRegion> toDelete = new ArrayList<>();
            for (PSRegion r : psRegions) {
                long numOfActiveOwners = r.getOwners().stream().filter(activePlayers::contains).count();
                long numOfActiveMembers = r.getMembers().stream().filter(activePlayers::contains).count();

                if (numOfActiveOwners == 0) {
                    if (ProtectionStones.getInstance().getConfigOptions().cleanupDeleteRegionsWithMembersButNoOwners || numOfActiveMembers == 0) {
                        toDelete.add(r);
                    }
                }
            }

            Iterator<PSRegion> deleteRegionsIterator = toDelete.iterator();
            regionLoop(deleteRegionsIterator, p, cleanupOperation.equalsIgnoreCase("remove"));
        });
        return false;
    }

    static private void regionLoop(Iterator<PSRegion> deleteRegionsIterator, CommandSender p, boolean isRemoveOperation) {
        if (deleteRegionsIterator.hasNext()) {
            Bukkit.getScheduler().runTaskLater(ProtectionStones.getInstance(), () ->
                    processRegion(deleteRegionsIterator, p, isRemoveOperation), 1);
        } else {
            PSL.msg(p, PSL.ADMIN_CLEANUP_FOOTER.msg()
                    .replace("%arg%", isRemoveOperation ? "remove" : "preview"));

            if (!isRemoveOperation) {
                try {
                    p.sendMessage(ChatColor.YELLOW + "Dumped the list regions that can be deleted in " + previewFile.getName() + " (in the plugin folder).");
                    previewFileOutputStream.flush();
                    previewFileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static private void processRegion(Iterator<PSRegion> deleteRegionsIterator, CommandSender p, boolean isRemoveOperation) {
        PSRegion r = deleteRegionsIterator.next();

        if (isRemoveOperation) {
            p.sendMessage(ChatColor.YELLOW + "Removed region " + r.getId() + " due to inactive owners.");
            r.deleteRegion(true);
        } else {
            p.sendMessage(ChatColor.YELLOW + "Found region " + r.getId() + " that can be deleted.");

            try {
                previewFileOutputStream.write(r.getId() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        regionLoop(deleteRegionsIterator, p, isRemoveOperation);
    }
}
