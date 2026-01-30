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

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class GuiManager {
    private final Plugin plugin;
    private final GuiListener listener;

    // Track currently open GUI per player
    private final Map<UUID, BaseGui> openGuis = new ConcurrentHashMap<>();

    public GuiManager(Plugin plugin) {
        this.plugin = plugin;
        this.listener = new GuiListener(this);
    }

    public Plugin plugin() { return plugin; }

    public void register() {
        Bukkit.getPluginManager().registerEvents(listener, plugin);
    }

    public void unregister() {
        HandlerList.unregisterAll(listener);
        openGuis.clear();
    }

    void setOpenGui(Player player, BaseGui gui) {
        if (gui == null) openGuis.remove(player.getUniqueId());
        else openGuis.put(player.getUniqueId(), gui);
    }

    public BaseGui getOpenGui(Player player) {
        return openGuis.get(player.getUniqueId());
    }

    // Convenience
    public void open(Player player, BaseGui gui) {
        gui.open(player);
    }

    public void close(Player player) {
        player.closeInventory();
    }
}
