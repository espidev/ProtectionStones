package dev.espi.ProtectionStones.event;

import dev.espi.ProtectionStones.PSRegion;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event that is called when a protection stones region is removed
 */

public class PSRemoveEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private PSRegion region;
    private Player p = null;
    private boolean isCancelled = false;

    public PSRemoveEvent(PSRegion psr, Player player) {
        this.region = psr;
        this.p = player;
    }

    public PSRemoveEvent(PSRegion psr) {
        this.region = psr;
    }

    /**
     * Returns the player that removed the protect block, if applicable
     * @return the player, or null if the region was not removed because of a player
     */
    public Player getPlayer() {
        return p;
    }

    /**
     * Returns the region being removed.
     * @return the region being removed
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
