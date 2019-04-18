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

import me.vik1395.ProtectionStones.PSL;
import me.vik1395.ProtectionStones.ProtectionStones;
import org.bukkit.command.CommandSender;

public class ArgReload {
    public static boolean argumentReload(CommandSender p, String[] args) {
        if (!p.hasPermission("protectionstones.admin")) {
            p.sendMessage(PSL.NO_PERMISSION_ADMIN.msg());
            return true;
        }
        p.sendMessage(PSL.RELOAD_START.msg());
        ProtectionStones.loadConfig();
        p.sendMessage(PSL.RELOAD_COMPLETE.msg());
        return true;
    }
}
