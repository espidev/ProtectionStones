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

import me.vik1395.ProtectionStones.ProtectionStones;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

public class ArgBypass {

    // /ps bypass [player (optional)]
    public static boolean argumentBypass(Player p, String[] args) {
        if (!p.hasPermission("protectionstones.bypass")) {
            p.sendMessage(ChatColor.RED + "You don't have permission to use the bypass command!");
            return true;
        }
        // set p to other player if specified
        if (args.length > 1) {
            if (Bukkit.getPlayer(args[1]) == null) {
                p.sendMessage(ChatColor.RED + "Invalid player name.");
                return true;
            }
            p = Bukkit.getPlayer(args[1]);
        }

        boolean bool = false;
        if (!p.hasMetadata("psBypass")) {
            p.setMetadata("psBypass", new FixedMetadataValue(ProtectionStones.getPlugin(), true));
        } else {
            List<MetadataValue> values = p.getMetadata("psBypass");
            for (MetadataValue value : values) {
                if (value.asBoolean()) {
                    p.setMetadata("psBypass", new FixedMetadataValue(ProtectionStones.getPlugin(), false));
                } else {
                    p.setMetadata("psBypass", new FixedMetadataValue(ProtectionStones.getPlugin(), true));
                }
                bool = value.asBoolean();
            }
        }

        // TODO this command doesn't look finished, since there is no logic for using it... ~ EspiDev

        p.sendMessage(ChatColor.GREEN + "ProtectionStones PVP Teleport Bypass: " + ChatColor.DARK_GREEN + bool + " for " + p.getName());
        return true;
    }
}
