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

import me.vik1395.ProtectionStones.PSL;
import me.vik1395.ProtectionStones.PlayerComparator;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class ArgAdminLastlogon {
    // /ps admin lastlogon
    public static boolean argumentAdminLastLogon(CommandSender p, String[] args) {
        if (args.length < 3) {
            p.sendMessage(PSL.COMMAND_REQUIRES_PLAYER_NAME.msg());
            return true;
        }
        OfflinePlayer op = Bukkit.getOfflinePlayer(args[2]);

        String playerName = args[2];
        long lastPlayed = (System.currentTimeMillis() - op.getLastPlayed()) / 86400000L;

        p.sendMessage(PSL.ADMIN_LAST_LOGON.msg()
                .replace("%player%", playerName)
                .replace("%days%", "" +lastPlayed));

        if (op.isBanned()) {
            p.sendMessage(PSL.ADMIN_IS_BANNED.msg()
                    .replace("%player%", playerName));
        }

        return true;
    }

    // /ps admin lastlogons
    public static boolean argumentAdminLastLogons(CommandSender p, String[] args) {
        int days = 0;
        if (args.length > 2) {
            try {
                days = Integer.parseInt(args[2]);
            } catch (Exception e) {
                p.sendMessage(PSL.ADMIN_ERROR_PARSING.msg());
                return true;
            }
        }
        OfflinePlayer[] offlinePlayerList = Bukkit.getServer().getOfflinePlayers().clone();
        int playerCounter = 0;
        p.sendMessage(PSL.ADMIN_LASTLOGONS_HEADER.msg()
                .replace("%days%", "" + days));

        Arrays.sort(offlinePlayerList, new PlayerComparator());
        for (OfflinePlayer offlinePlayer : offlinePlayerList) {
            long lastPlayed = (System.currentTimeMillis() - offlinePlayer.getLastPlayed()) / 86400000L;
            if (lastPlayed >= days) {
                playerCounter++;
                p.sendMessage(PSL.ADMIN_LASTLOGONS_LINE.msg()
                        .replace("%player%", offlinePlayer.getName())
                        .replace("%time%", "" + lastPlayed));
            }
        }

        p.sendMessage(PSL.ADMIN_LASTLOGONS_FOOTER.msg()
                .replace("%count%", "" + playerCounter)
                .replace("%checked%", "" + offlinePlayerList.length));

        return true;
    }
}
