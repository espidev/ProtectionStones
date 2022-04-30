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

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.RemovalStrategy;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.*;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class ArgAdminRecreate {
    static boolean argumentAdminRecreate(CommandSender s, String[] args) {
        s.sendMessage(ChatColor.YELLOW + "Recreating...");

        HashMap<World, RegionManager> m = WGUtils.getAllRegionManagers();
        for (World w : m.keySet()) {
            RegionManager rgm = m.get(w);

            List<ProtectedRegion> toAdd = new ArrayList<>();

            for (ProtectedRegion r : rgm.getRegions().values()) {
                if (ProtectionStones.isPSRegion(r)) {
                    PSRegion wr = PSRegion.fromWGRegion(w, r);
                    if (wr instanceof PSGroupRegion) continue; // skip group regions for now TODO
                    PSProtectBlock blockOptions = wr.getTypeOptions();

                    if (blockOptions == null) {
                        Bukkit.getLogger().info("Region " + r.getId() + " in world " + w.getName() + " is not configured in the block config! Skipping...");
                        continue;
                    }

                    ProtectedRegion nr = WGUtils.getDefaultProtectedRegion(blockOptions, WGUtils.parsePSRegionToLocation(wr.getId()));
                    nr.copyFrom(r); // copy region data over
                    toAdd.add(nr);
                }
            }

            for (ProtectedRegion r : toAdd) {
                rgm.removeRegion(r.getId(), RemovalStrategy.UNSET_PARENT_IN_CHILDREN);
                rgm.addRegion(r);
            }
        }

        s.sendMessage(ChatColor.YELLOW + "Done.");
        return true;
    }
}
