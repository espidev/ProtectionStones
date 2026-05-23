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

package dev.espi.protectionstones.utils;

import dev.espi.protectionstones.ProtectionStones;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class BackupUtil {

    private static final long COOLDOWN_MILLIS = 30 * 60 * 1000L; // 30 minutes per world
    private static final int MAX_FILES = 48;                       // ~24 hours of history

    // tracks last backup timestamp per world name — ConcurrentHashMap for async safety
    private static final ConcurrentHashMap<String, Long> lastBackupTime = new ConcurrentHashMap<>();

    // called from async thread — rate-limited copy of WG regions file for the given world
    public static void backupWorldRegions(World world) {
        Plugin wgPlugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (wgPlugin == null) return;

        String worldName = world.getName();

        // atomic cooldown check: only proceed if 30 min have elapsed since last backup
        long now = System.currentTimeMillis();
        if (!tryAcquireBackup(worldName, now)) return;

        File wgRegionsFile = new File(wgPlugin.getDataFolder(),
                "worlds" + File.separator + worldName + File.separator + "regions.yml");
        if (!wgRegionsFile.exists()) return;

        File backupDir = new File(ProtectionStones.getInstance().getDataFolder(),
                "backup" + File.separator + worldName);
        backupDir.mkdirs();

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date(now));
        File dest = new File(backupDir, worldName + "_" + timestamp + ".yml");

        try {
            Files.copy(wgRegionsFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            ProtectionStones.getInstance().getLogger()
                    .warning("Failed to backup WorldGuard regions for world " + worldName + ": " + e.getMessage());
            return;
        }

        pruneOldBackups(backupDir, worldName);
    }

    // atomically check and claim the backup slot — returns true if this call should proceed
    private static boolean tryAcquireBackup(String worldName, long now) {
        boolean[] proceed = {false};
        lastBackupTime.compute(worldName, (k, last) -> {
            if (last == null || (now - last) >= COOLDOWN_MILLIS) {
                proceed[0] = true;
                return now;
            }
            return last;
        });
        return proceed[0];
    }

    // delete oldest backup files beyond MAX_FILES limit
    private static void pruneOldBackups(File backupDir, String worldName) {
        File[] files = backupDir.listFiles(
                (dir, name) -> name.startsWith(worldName + "_") && name.endsWith(".yml"));
        if (files == null || files.length <= MAX_FILES) return;

        Arrays.sort(files, Comparator.comparingLong(File::lastModified));
        for (int i = 0; i < files.length - MAX_FILES; i++) {
            files[i].delete();
        }
    }
}
