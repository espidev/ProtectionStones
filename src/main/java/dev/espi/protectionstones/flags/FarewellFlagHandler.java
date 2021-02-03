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
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;
import dev.espi.protectionstones.FlagHandler;
import dev.espi.protectionstones.utils.Strings;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

// farewell-action flag
public class FarewellFlagHandler extends FlagValueChangeHandler<String> {

    public static final FarewellFlagHandler.Factory FACTORY = new FarewellFlagHandler.Factory();

    public static class Factory extends Handler.Factory<FarewellFlagHandler> {
        @Override
        public FarewellFlagHandler create(Session session) {
            return new FarewellFlagHandler(session);
        }
    }

    protected FarewellFlagHandler(Session session) {
        super(session, FlagHandler.FAREWELL_ACTION);
    }

    @Override
    protected void onInitialValue(LocalPlayer localPlayer, ApplicableRegionSet applicableRegionSet, String s) {

    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    protected boolean onSetValue(LocalPlayer localPlayer, Location location, Location location1, ApplicableRegionSet applicableRegionSet, String currentValue, String lastValue, MoveType moveType) {
        if (lastValue == null || lastValue.equals(currentValue)) {
            return true;
        }

        final Player player = Bukkit.getPlayer(localPlayer.getUniqueId());

        if (player == null) {
            return true;
        }

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Strings.color(lastValue)));
        return true;
    }

    @Override
    protected boolean onAbsentValue(LocalPlayer localPlayer, Location location, Location location1, ApplicableRegionSet applicableRegionSet, String lastValue, MoveType moveType) {
        if (lastValue == null) {
            return true;
        }

        final Player player = Bukkit.getPlayer(localPlayer.getUniqueId());

        if (player == null) {
            return true;
        }

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Strings.color(lastValue)));
        return true;
    }

}
