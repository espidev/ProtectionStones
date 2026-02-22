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

package dev.espi.protectionstones.gui.screens.regions;

import dev.espi.protectionstones.PSGroupRegion;
import dev.espi.protectionstones.PSProtectBlock;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.ProtectionStones;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Shared region actions used by inventory GUIs. */
public final class RegionActions {
    private RegionActions() {}

    /**
     * Unclaims a region (returns protection stone items if configured, then deletes the region).
     * This mirrors the behavior in ArgUnclaim.
     */
    public static boolean unclaimRegion(PSRegion r, Player p) {
        PSProtectBlock cpb = r.getTypeOptions();
        if (cpb != null && !cpb.noDrop) {
            // return protection stone(s)
            List<ItemStack> items = new ArrayList<>();

            if (r instanceof PSGroupRegion) {
                for (PSRegion rp : ((PSGroupRegion) r).getMergedRegions()) {
                    if (rp.getTypeOptions() != null) items.add(rp.getTypeOptions().createItem());
                }
            } else {
                items.add(cpb.createItem());
            }

            for (ItemStack item : items) {
                if (!p.getInventory().addItem(item).isEmpty()) {
                    if (ProtectionStones.getInstance().getConfigOptions().dropItemWhenInventoryFull) {
                        PSL.msg(p, PSL.NO_ROOM_DROPPING_ON_FLOOR.msg());
                        p.getWorld().dropItem(p.getLocation(), item);
                    } else {
                        PSL.msg(p, PSL.NO_ROOM_IN_INVENTORY.msg());
                        return true;
                    }
                }
            }
        }

        // remove region (respect the same safeguards)
        if (!r.deleteRegion(true, p)) {
            if (!ProtectionStones.getInstance().getConfigOptions().allowMergingHoles) {
                PSL.msg(p, PSL.DELETE_REGION_PREVENTED_NO_HOLES.msg());
            }
            return true;
        }

        PSL.msg(p, PSL.NO_LONGER_PROTECTED.msg());
        return true;
    }
}
