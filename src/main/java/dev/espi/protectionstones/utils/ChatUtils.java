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

package dev.espi.protectionstones.utils;

import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.PSRegion;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class ChatUtils {

    public static void displayDuplicateRegionAliases(Player player, List<PSRegion> regions) {
        final String list = regions.stream()
                .map(region -> region.getId() + " (" + region.getWorld().getName() + ")")
                .collect(Collectors.joining(", "));

        PSL.msg(player, PSL.SPECIFY_ID_INSTEAD_OF_ALIAS.msg().replace("%regions%", list));
    }

}
