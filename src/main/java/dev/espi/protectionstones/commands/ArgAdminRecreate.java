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

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.*;
import dev.espi.protectionstones.utils.WGUtils;
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

                    double bx = wr.getProtectBlock().getLocation().getX(), bxo = blockOptions.xOffset;
                    double by = wr.getProtectBlock().getLocation().getY(), bxy = blockOptions.yOffset;
                    double bz = wr.getProtectBlock().getLocation().getZ(), bxz = blockOptions.zOffset;
                    BlockVector min = WGUtils.getMinVector(bx + bxo, by + bxy, bz + bxz, blockOptions.xRadius, blockOptions.yRadius, blockOptions.zRadius);
                    BlockVector max = WGUtils.getMaxVector(bx + bxo, by + bxy, bz + bxz, blockOptions.xRadius, blockOptions.yRadius, blockOptions.zRadius);

                    ProtectedRegion nr = new ProtectedCuboidRegion(r.getId(), min, max);
                    WGUtils.copyRegionValues(r, nr);
                    toAdd.add(nr);
                }
            }

            for (ProtectedRegion r : toAdd) {
                rgm.removeRegion(r.getId());
                rgm.addRegion(r);
            }
        }

        s.sendMessage(ChatColor.YELLOW + "Done.");
        return true;
    }
}
