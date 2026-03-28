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

package dev.espi.protectionstones.gui;

import org.bukkit.entity.Player;

import java.util.List;

public abstract class PaginatedGui<T> extends BaseGui {
    protected int page = 0;

    protected PaginatedGui(GuiManager gui, int size, String title) {
        super(gui, size, title);
    }

    /** Items for current viewer. */
    protected abstract List<T> entries(Player viewer);

    /** Render a single entry into a slot. */
    protected abstract void renderEntry(Player viewer, int slot, T entry);

    /** Slots used for entries (e.g., 0-44 for a 54-size GUI). */
    protected int entrySlots() { return size - 9; }

    protected int prevButtonSlot() { return size - 9; }
    protected int nextButtonSlot() { return size - 1; }
}
