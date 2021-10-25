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
import dev.espi.protectionstones.PSLocation;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

class ArgAdminCleanup {

    private static File previewFile;
    private static FileWriter previewFileOutputStream;

    // /ps admin cleanup [remove/preview]
    static boolean argumentAdminCleanup(CommandSender p, String[] preParseArgs) {
        if (preParseArgs.length < 3 || !Arrays.asList("remove", "preview").contains(preParseArgs[2].toLowerCase())) {
            PSL.msg(p, ArgAdmin.getCleanupHelp());
            return true;
        }

        String cleanupOperation = preParseArgs[2].toLowerCase(); // [remove|preview]

        World w;
        String alias = null;

        List<String> args = new ArrayList<>();

        // determine if there is an alias flag selected, and remove [-t typealias] if there is
        for (int i = 3; i < preParseArgs.length; i++) {
            if (preParseArgs[i].equals("-t") && i != preParseArgs.length-1) {
                alias = preParseArgs[++i];
            } else {
                args.add(preParseArgs[i]);
            }
        }

        // the args array should consist of: [days, world (optional)]
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

        // create preview file
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
        Map<String, ProtectedRegion> regions = rgm.getRegions();

        // async cleanup task
        String finalAlias = alias;
        Bukkit.getScheduler().runTaskAsynchronously(ProtectionStones.getInstance(), () -> {
            int days = (args.size() > 0) ? Integer.parseInt(args.get(0)) : 30; // 30 days is default if days aren't specified

            PSL.msg(p, PSL.ADMIN_CLEANUP_HEADER.msg()
                    .replace("%arg%", cleanupOperation)
                    .replace("%days%", "" + days));

            HashSet<UUID> activePlayers = new HashSet<>();

            // loop over offline players and add to list if they haven't joined recently
            for (OfflinePlayer op : Bukkit.getServer().getOfflinePlayers()) {
                long lastPlayed = (System.currentTimeMillis() - op.getLastPlayed()) / 86400000L;
                try {
                    // a player is active if they have joined within the days
                    if (lastPlayed < days) {
                        activePlayers.add(op.getUniqueId());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // loop over all regions async and find regions to delete
            List<PSRegion> toDelete = new ArrayList<>();
            for (String regionId : regions.keySet()) {
                PSRegion r = PSRegion.fromWGRegion(w, regions.get(regionId));
                if (r == null) { // not a ps region (unconfigured types still count as ps regions)
                    continue;
                }

                // if an alias is specified, skip regions that aren't of the type
                if (finalAlias != null && (r.getTypeOptions() == null || !r.getTypeOptions().alias.equals(finalAlias))) {
                    continue;
                }

                long numOfActiveOwners = r.getOwners().stream().filter(activePlayers::contains).count();
                long numOfActiveMembers = r.getMembers().stream().filter(activePlayers::contains).count();

                // remove region if there are no owners left
                if (numOfActiveOwners == 0) {
                    if (ProtectionStones.getInstance().getConfigOptions().cleanupDeleteRegionsWithMembersButNoOwners || numOfActiveMembers == 0) {
                        toDelete.add(r);
                    }
                }
            }

            // start recursive iteration to delete a region each tick
            Iterator<PSRegion> deleteRegionsIterator = toDelete.iterator();
            regionLoop(deleteRegionsIterator, p, cleanupOperation.equalsIgnoreCase("remove"));
        });
        return true;
    }

    static private void regionLoop(Iterator<PSRegion> deleteRegionsIterator, CommandSender p, boolean isRemoveOperation) {
        if (deleteRegionsIterator.hasNext()) {
            Bukkit.getScheduler().runTaskLater(ProtectionStones.getInstance(), () ->
                    processRegion(deleteRegionsIterator, p, isRemoveOperation), 1);
        } else { // finished region iteration
            PSL.msg(p, PSL.ADMIN_CLEANUP_FOOTER.msg()
                    .replace("%arg%", isRemoveOperation ? "remove" : "preview"));

            // flush and close preview file
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

    // Process a region, and then iterate to the next region on the next tick.
    // This is to prevent the server from pausing for the entire duration of the cleanup.
    // (lag from loading chunks to remove protection blocks)
    static private void processRegion(Iterator<PSRegion> deleteRegionsIterator, CommandSender p, boolean isRemoveOperation) {
        PSRegion r = deleteRegionsIterator.next();

        if (isRemoveOperation) { // delete

            p.sendMessage(ChatColor.YELLOW + "Removed region " + r.getId() + " due to inactive owners.");

            // must be sync
            r.deleteRegion(true);
        } else { // preview

            p.sendMessage(ChatColor.YELLOW + "Found region " + r.getId() + " that can be deleted.");

            // adds region id to preview file
            try {
                previewFileOutputStream.write(r.getId() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // go to next region
        regionLoop(deleteRegionsIterator, p, isRemoveOperation);
    }
}
