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

package dev.espi.protectionstones.flags;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.ProtectionStones;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PSMessageFlagHandler extends FlagValueChangeHandler<String> {

    private final boolean isFarewell;
    private final boolean isActionBar;

    public static class Factory extends Handler.Factory<PSMessageFlagHandler> {
        private final Flag<String> flag;
        private final boolean isFarewell;
        private final boolean isActionBar;

        public Factory(Flag<String> flag, boolean isFarewell, boolean isActionBar) {
            this.flag = flag;
            this.isFarewell = isFarewell;
            this.isActionBar = isActionBar;
        }

        @Override
        public PSMessageFlagHandler create(Session session) {
            return new PSMessageFlagHandler(session, flag, isFarewell, isActionBar);
        }
    }

    public PSMessageFlagHandler(Session session, Flag<String> flag, boolean isFarewell, boolean isActionBar) {
        super(session, flag);
        this.isFarewell = isFarewell;
        this.isActionBar = isActionBar;
    }

    @Override
    protected void onInitialValue(LocalPlayer localPlayer, ApplicableRegionSet applicableRegionSet, String s) {
    }

    private void sendMessage(LocalPlayer localPlayer, String message) {
        if (message == null || message.isEmpty())
            return;
        Player p = Bukkit.getPlayer(localPlayer.getUniqueId());
        if (p == null)
            return;

        var component = MiniMessage.miniMessage().deserialize(PSL.legacyToMiniMessage(message));
        var audience = ProtectionStones.getAdventure().player(p);

        if (isActionBar) {
            audience.sendActionBar(component);
        } else {
            audience.sendMessage(component);
        }
    }

    @Override
    protected boolean onSetValue(LocalPlayer localPlayer, Location from, Location to, ApplicableRegionSet toSet,
            String currentValue, String lastValue, MoveType moveType) {
        if (!isFarewell && currentValue != null && !currentValue.equals(lastValue)) {
            sendMessage(localPlayer, currentValue);
        } else if (isFarewell && lastValue != null && !lastValue.equals(currentValue)) {
            sendMessage(localPlayer, lastValue);
        }
        return true;
    }

    @Override
    protected boolean onAbsentValue(LocalPlayer localPlayer, Location from, Location to, ApplicableRegionSet toSet,
            String lastValue, MoveType moveType) {
        if (isFarewell && lastValue != null) {
            sendMessage(localPlayer, lastValue);
        }
        return true;
    }
}
