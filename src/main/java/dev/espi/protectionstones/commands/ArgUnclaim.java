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

import dev.espi.protectionstones.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ArgUnclaim implements PSCommandArg {

    // /ps unclaim

    @Override
    public List<String> getNames() {
        return Collections.singletonList("unclaim");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public List<String> getPermissionsToExecute() {
        return Collections.singletonList("protectionstones.unclaim");
    }

    @Override
    public HashMap<String, Boolean> getRegisteredFlags() {
        return null;
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args, HashMap<String, String> flags) {
        Player p = (Player) s;
        PSRegion r = PSRegion.fromLocationGroupUnsafe(p.getLocation()); // allow unclaiming unconfigured regions

        if (!p.hasPermission("protectionstones.unclaim")) {
            PSL.msg(p, PSL.NO_PERMISSION_UNCLAIM.msg());
            return true;
        }
        if (r == null) {
            PSL.msg(p, PSL.NOT_IN_REGION.msg());
            return true;
        }

        if (!r.isOwner(p.getUniqueId()) && !p.hasPermission("protectionstones.superowner")) {
            PSL.msg(p, PSL.NO_REGION_PERMISSION.msg());
            return true;
        }

        // cannot break region being rented (prevents splitting merged regions, and breaking as tenant owner)
        if (r.getRentStage() == PSRegion.RentStage.RENTING && !p.hasPermission("protectionstones.superowner")) {
            PSL.msg(p, PSL.RENT_CANNOT_BREAK_WHILE_RENTING.msg());
            return false;
        }

        PSProtectBlock cpb = r.getTypeOptions();
        if (cpb != null && !cpb.noDrop) {
            // return protection stone
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
                    // method will return not empty if item couldn't be added
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

        // remove region
        // check if removing the region and firing region remove event blocked it
        if (!r.deleteRegion(true, p)) {
            if (!ProtectionStones.getInstance().getConfigOptions().allowMergingHoles) { // side case if the removing creates a hole and those are prevented
                PSL.msg(p, PSL.DELETE_REGION_PREVENTED_NO_HOLES.msg());
            }
            return true;
        }

        PSL.msg(p, PSL.NO_LONGER_PROTECTED.msg());

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return null;
    }
}
