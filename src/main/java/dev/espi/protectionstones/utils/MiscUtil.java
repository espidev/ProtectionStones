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

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.time.Duration;

public class MiscUtil {

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

    public static int getPermissionNumber(Player p, String perm, int def /* default */) {
        int n = -99999;
        for (PermissionAttachmentInfo pia : p.getEffectivePermissions()) {
            String permission = pia.getPermission();

            if (permission.startsWith(perm)) {
                String value = permission.substring(perm.length());
                if (StringUtils.isNumeric(value)) {
                    n = Math.max(n, Integer.parseInt(value));
                }
            }
        }
        return n == -99999 ? def : n;
    }

}
