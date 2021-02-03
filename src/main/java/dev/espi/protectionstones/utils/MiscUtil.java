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

import com.google.common.primitives.Ints;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MiscUtil {

    private static final String VERSION = Bukkit.getBukkitVersion().split("-")[0];

    public static String getUniqueIdIntArray(UUID uuid) {
        long least = uuid.getMostSignificantBits();
        long most = uuid.getLeastSignificantBits();

        int[] arr = new int[]{(int) (least >> 32), (int) least, (int) (most >> 32), (int) most};
        return String.format("[I; %d, %d, %d, %d]", arr[0], arr[1], arr[2], arr[3]);
    }

    public static String getVersionString() {
        return VERSION;
    }

    public static Duration parseRentPeriod(String period) throws NumberFormatException {
        Duration rentPeriod = Duration.ZERO;
        for (String s : period.split(" ")) {
            if (s.contains("w")) {
                rentPeriod = rentPeriod.plusDays(Long.parseLong(s.replace("w", "")) * 7);
            } else if (s.contains("d")) {
                rentPeriod = rentPeriod.plusDays(Long.parseLong(s.replace("d", "")));
            } else if (s.contains("h")) {
                rentPeriod = rentPeriod.plusHours(Long.parseLong(s.replace("h", "")));
            } else if (s.contains("m")) {
                rentPeriod = rentPeriod.plusMinutes(Long.parseLong(s.replace("m", "")));
            } else if (s.contains("s")) {
                rentPeriod = rentPeriod.plusSeconds(Long.parseLong(s.replace("s", "")));
            }
        }
        return rentPeriod;
    }

    public static int getPermissionNumber(Player player, String permission, int def) {
        return getPermissionNumber(
                player.getEffectivePermissions().stream().map(PermissionAttachmentInfo::getPermission).collect(Collectors.toList()),
                permission,
                def
        );
    }

    @SuppressWarnings("UnstableApiUsage")
    public static int getPermissionNumber(List<String> permissions, String perm, int def /* default */) {
        int n = -99999;

        for (String permission : permissions) {
            if (!permission.startsWith(perm)) {
                continue;
            }

            String value = permission.substring(perm.length());
            Integer number = Ints.tryParse(value);

            if (number == null) {
                continue;
            }

            n = Math.max(n, number);
        }

        return n == -99999 ? def : n;
    }

    public static String concatWithoutLast(List<String> list, String separator) {
        return list.size() <= 1 ? "" : String.join(separator, list.subList(0, list.size() - 2));
    }

    public static String describeDuration(Duration duration) {
        long days = duration.toDays();
        duration = duration.minusDays(days);
        long hours = duration.toHours();
        duration = duration.minusHours(hours);
        long minutes = duration.toMinutes();
        duration = duration.minusMinutes(minutes);
        long seconds = duration.toMillis() / 1000;

        final StringBuilder builder = new StringBuilder();

        if (days != 0) {
            builder.append(days).append('d');
        }

        if (hours != 0) {
            builder.append(hours).append('h');
        }

        if (minutes != 0) {
            builder.append(minutes).append('m');
        }

        if (seconds != 0) {
            builder.append(seconds).append('s');
        }

        return builder.toString();
    }

}
