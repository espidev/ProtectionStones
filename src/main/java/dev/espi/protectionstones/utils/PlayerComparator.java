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

import org.bukkit.OfflinePlayer;

import java.util.Comparator;


public class PlayerComparator implements Comparator<OfflinePlayer> {

    @Override
    public int compare(OfflinePlayer o1, OfflinePlayer o2) {
        return o1.getName().compareTo(o2.getName());
    }
 }