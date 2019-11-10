/*
 * Copyright 2019 ProtectionStones team and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.espi.protectionstones.commands;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
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
        PSRegion r = PSRegion.fromLocation(p.getLocation());

        WorldGuardPlugin wg = WorldGuardPlugin.inst();
        if (!p.hasPermission("protectionstones.unclaim")) {
            PSL.msg(p, PSL.NO_PERMISSION_UNCLAIM.msg());
            return true;
        }
        if (r == null) {
            PSL.msg(p, PSL.NOT_IN_REGION.msg());
            return true;
        }

        if (!r.getWGRegion().isOwner(wg.wrapPlayer(p)) && !p.hasPermission("protectionstones.superowner") || r.getRentStage() == PSRegion.RentStage.RENTING) {
            PSL.msg(p, PSL.NO_REGION_PERMISSION.msg());
            return true;
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
