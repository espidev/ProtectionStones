package dev.espi.ProtectionStones.event;

import dev.espi.ProtectionStones.PSRegion;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event that is called when a protectionstones region is created, either by a player, or by the plugin.
 */

public class PSCreateEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private PSRegion region;
    private Player p = null;
    private boolean isCancelled = false;

    public PSCreateEvent(PSRegion psr, Player player) {
        this.region = psr;
        this.p = player;
    }

    public PSCreateEvent(PSRegion psr) {
        this.region = psr;
    }

    /**
     * Returns the player that created the protection region, if applicable
     * @return the player, or null if the region was not created because of a player
     */
    public Player getPlayer() {
        return p;
    }

    /**
     * Returns the region being created.
     * @return the region being created
     */
    public PSRegion getRegion() {
        return region;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
