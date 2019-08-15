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

public class MiscUtil {
    public static int getPermissionNumber(Player p, String perm, int def) {
        int n = -999;
        for (PermissionAttachmentInfo pia : p.getEffectivePermissions()) {
            String permission = pia.getPermission();
            String[] sp = permission.split("\\.");
            if (permission.startsWith(perm) && sp.length > 0 && StringUtils.isNumeric(sp[sp.length-1])) {
                n = Math.max(n, Integer.parseInt(sp[sp.length-1]));
            }
        }
        return n == -999 ? def : n;
    }
}
