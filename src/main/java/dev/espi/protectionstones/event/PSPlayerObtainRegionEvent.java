package dev.espi.protectionstones.event;

import dev.espi.protectionstones.PSRegion;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import static com.google.common.base.Preconditions.checkNotNull;

public class PSPlayerObtainRegionEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private PSRegion region;
    private Player p = null;
    private boolean isCancelled = false;

    public PSPlayerObtainRegionEvent(PSRegion psr, Player player) {
        this.region = checkNotNull(psr);
        this.p = player;
    }

    public PSPlayerObtainRegionEvent(PSRegion psr) {
        this.region = checkNotNull(psr);
    }

    /**
     * Returns the player that obtained the protection region, if applicable
     * @return the player
     */
    public Player getPlayer() {
        return p;
    }

    /**
     * Returns the region being obtained.
     * @return the region being obtained
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

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
