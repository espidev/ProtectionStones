package dev.espi.protectionstones.event;

import dev.espi.protectionstones.PSRegion;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Event that is called when a protection stones block is removed
 */
public class PSBreakProtectBlockEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private PSRegion region;
    private Player player;
    private boolean isCancelled = false;

    public PSBreakProtectBlockEvent(PSRegion psr, Player player) {
        this.region = checkNotNull(psr);
        this.player = player;
    }

    /**
     * Gets the player who triggered the event.
     *
     * @return The player.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the ProtectionStones item associated with the region.
     *
     * @return The ProtectionStones item.
     */
    public ItemStack getPSItem() {
        return Objects.requireNonNull(region.getTypeOptions()).createItem();
    }

    /**
     * Gets the ProtectionStones region associated with the event.
     *
     * @return The ProtectionStones region.
     */
    public PSRegion getRegion() {
        return region;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        isCancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
