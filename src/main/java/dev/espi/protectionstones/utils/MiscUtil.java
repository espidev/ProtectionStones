/*
 * Copyright 2019 ProtectionStones team and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
        int n = -999;
        for (PermissionAttachmentInfo pia : p.getEffectivePermissions()) {
            String permission = pia.getPermission();
            String[] sp = permission.split("\\.");
            if (permission.startsWith(perm) && sp.length > 0 && StringUtils.isNumeric(sp[sp.length - 1])) {
                n = Math.max(n, Integer.parseInt(sp[sp.length - 1]));
            }
        }
        return n == -999 ? def : n;
    }

}
