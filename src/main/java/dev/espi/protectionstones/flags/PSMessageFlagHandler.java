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

    private final boolean isActionBar;

    public static class Factory extends Handler.Factory<PSMessageFlagHandler> {
        private final Flag<String> flag;
        private final boolean isActionBar;

        public Factory(Flag<String> flag, boolean isActionBar) {
            this.flag = flag;
            this.isActionBar = isActionBar;
        }

        @Override
        public PSMessageFlagHandler create(Session session) {
            return new PSMessageFlagHandler(session, flag, isActionBar);
        }
    }

    public PSMessageFlagHandler(Session session, Flag<String> flag, boolean isActionBar) {
        super(session, flag);
        this.isActionBar = isActionBar;
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
    protected void onInitialValue(LocalPlayer localPlayer, ApplicableRegionSet applicableRegionSet, String s) {
    }

    @Override
    protected boolean onSetValue(LocalPlayer localPlayer, Location location, Location location1,
            ApplicableRegionSet applicableRegionSet, String currentValue, String lastValue, MoveType moveType) {
        if (currentValue != null && !currentValue.equals(lastValue)) {
            sendMessage(localPlayer, currentValue);
        }
        return true;
    }

    @Override
    protected boolean onAbsentValue(LocalPlayer localPlayer, Location location, Location location1,
            ApplicableRegionSet applicableRegionSet, String lastValue, MoveType moveType) {
        return true;
    }
}
