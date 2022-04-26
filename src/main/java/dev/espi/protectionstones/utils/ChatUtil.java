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

public class ChatUtil {
    public static void displayDuplicateRegionAliases(Player p, List<PSRegion> r) {
        StringBuilder rep = new StringBuilder(r.get(0).getId() + " (" + r.get(0).getWorld().getName() + ")");

        for (int i = 1; i < r.size(); i++) {
            rep.append(String.format(", %s (%s)", r.get(i).getId(), r.get(i).getWorld().getName()));
        }

        PSL.msg(p, PSL.SPECIFY_ID_INSTEAD_OF_ALIAS.msg().replace("%regions%", rep.toString()));
    }
}
