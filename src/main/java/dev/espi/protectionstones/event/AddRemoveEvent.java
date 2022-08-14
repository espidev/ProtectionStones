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

package dev.espi.protectionstones.event;

import dev.espi.protectionstones.PSRegion;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class AddRemoveEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private PSRegion region;
    private String operationType;
    private Player player;
    private String targetName;
    private UUID targetUuid;
    private boolean isCancelled = false;

    public AddRemoveEvent(PSRegion psr, String operationType, Player player, String targetName, UUID targetUuid) {
        this.region = checkNotNull(psr);
        this.operationType = operationType;
        this.player = checkNotNull(player);
        this.targetName = checkNotNull(targetName);
        this.targetUuid = checkNotNull(targetUuid);
    }

    /**
     * Returns the region being modified.
     * @return the region being modified
     */
    public PSRegion getRegion() {
        return region;
    }

    /**
     * Returns the operation type.
     * @return the operation type
     */
    public String getOperationType() {
        return operationType;
    }

    /**
     * Returns the player modifying the region.
     * @return the player modifying the region
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns name of the player who's being added or removed.
     * @return name of the player who's being added or removed
     */
    public String getTargetName() {
        return targetName;
    }

    /**
     * Returns UUID of the player who's being added or removed.
     * @return UUID of the player who's being added or removed
     */
    public UUID getTargetUuid() {
        return targetUuid;
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
