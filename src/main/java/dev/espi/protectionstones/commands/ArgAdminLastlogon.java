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

import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.utils.PlayerComparator;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

class ArgAdminLastlogon {
    // /ps admin lastlogon
    static boolean argumentAdminLastLogon(CommandSender p, String[] args) {
        if (args.length < 3) {
            p.sendMessage(PSL.COMMAND_REQUIRES_PLAYER_NAME.msg());
            return true;
        }
        OfflinePlayer op = Bukkit.getOfflinePlayer(args[2]);

        String playerName = args[2];
        long lastPlayed = (System.currentTimeMillis() - op.getLastPlayed()) / 86400000L;

        PSL.msg(p, PSL.ADMIN_LAST_LOGON.msg()
                .replace("%player%", playerName)
                .replace("%days%", "" +lastPlayed));

        if (op.isBanned()) {
            PSL.msg(p, PSL.ADMIN_IS_BANNED.msg()
                    .replace("%player%", playerName));
        }

        return true;
    }

    // /ps admin lastlogons
    static boolean argumentAdminLastLogons(CommandSender p, String[] args) {
        int days = 0;
        if (args.length > 2) {
            try {
                days = Integer.parseInt(args[2]);
            } catch (Exception e) {
                PSL.msg(p, PSL.ADMIN_ERROR_PARSING.msg());
                return true;
            }
        }
        OfflinePlayer[] offlinePlayerList = Bukkit.getServer().getOfflinePlayers().clone();
        int playerCounter = 0;
        PSL.msg(p, PSL.ADMIN_LASTLOGONS_HEADER.msg()
                .replace("%days%", "" + days));

        Arrays.sort(offlinePlayerList, new PlayerComparator());
        for (OfflinePlayer offlinePlayer : offlinePlayerList) {
            long lastPlayed = (System.currentTimeMillis() - offlinePlayer.getLastPlayed()) / 86400000L;
            if (lastPlayed >= days) {
                playerCounter++;
                PSL.msg(p, PSL.ADMIN_LASTLOGONS_LINE.msg()
                        .replace("%player%", offlinePlayer.getName())
                        .replace("%time%", "" + lastPlayed));
            }
        }

        PSL.msg(p, PSL.ADMIN_LASTLOGONS_FOOTER.msg()
                .replace("%count%", "" + playerCounter)
                .replace("%checked%", "" + offlinePlayerList.length));

        return true;
    }

}
