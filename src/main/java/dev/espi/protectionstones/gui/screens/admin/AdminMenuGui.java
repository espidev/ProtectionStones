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

package dev.espi.protectionstones.gui.screens.admin;

import dev.espi.protectionstones.PSConfig;
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.commands.ArgAdmin;
import dev.espi.protectionstones.gui.BaseGui;
import dev.espi.protectionstones.gui.GuiItems;
import dev.espi.protectionstones.gui.GuiManager;
import dev.espi.protectionstones.gui.screens.common.ConfirmGui;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Inventory menu for /ps admin. Executes selected actions after confirmation. */
public class AdminMenuGui extends BaseGui {

    private record Action(Material icon, String title, List<String> lore, String commandSuffix, String requiresHelp) {}

    public AdminMenuGui(GuiManager gui) {
        super(gui, 45, ChatColor.DARK_GRAY + "Admin");
    }

    @Override
    protected void draw(Player viewer) {
        inv.clear();

        // Top row border
        for (int i = 0; i < 9; i++) inv.setItem(i, GuiItems.item(Material.BLACK_STAINED_GLASS_PANE, " "));

        String base = ProtectionStones.getInstance().getConfigOptions().base_command;
        List<Action> actions = new ArrayList<>();

        // Runnable (no-arg) admin actions
        actions.add(new Action(Material.PAPER, ChatColor.translateAlternateColorCodes('&',"&bVersion"),
                List.of("&7Show plugin/server versions", "&8", "&7Runs:", "&f/" + base + " admin version"),
                "version", null));

        actions.add(new Action(Material.BOOK, ChatColor.translateAlternateColorCodes('&',"&eStats"),
                List.of("&7Show PS region stats", "&8", "&7Runs:", "&f/" + base + " admin stats"),
                "stats", null));

        actions.add(new Action(Material.REDSTONE_TORCH, ChatColor.translateAlternateColorCodes('&',"&dToggle Debug"),
                List.of("&7Toggle debug mode", "&8", "&7Runs:", "&f/" + base + " admin debug"),
                "debug", null));

        actions.add(new Action(Material.ANVIL, ChatColor.translateAlternateColorCodes('&',"&cFix Regions"),
                List.of("&7Run legacy region fix/upgrade", "&8", "&cThis may modify region data.", "&8", "&7Runs:", "&f/" + base + " admin fixregions"),
                "fixregions", null));

        actions.add(new Action(Material.GOLD_INGOT, ChatColor.translateAlternateColorCodes('&',"&6Set Tax Autopayers"),
                List.of("&7Set missing tax autopayers", "&8", "&7Runs:", "&f/" + base + " admin settaxautopayers"),
                "settaxautopayers", null));

        // Commands that require additional args (show help instead)
        actions.add(new Action(Material.HOPPER, ChatColor.translateAlternateColorCodes('&',"&fCleanup"),
                List.of("&7Requires arguments", "&8", "&7Click to show usage"),
                null, ArgAdmin.getCleanupHelp()));

        actions.add(new Action(Material.NAME_TAG, ChatColor.translateAlternateColorCodes('&',"&fFlag"),
                List.of("&7Requires arguments", "&8", "&7Click to show usage"),
                null, ArgAdmin.getFlagHelp()));

        actions.add(new Action(Material.IRON_BLOCK, ChatColor.translateAlternateColorCodes('&',"&fChange Block"),
                List.of("&7Requires arguments", "&8", "&7Click to show usage"),
                null, ArgAdmin.getChangeBlockHelp()));

        actions.add(new Action(Material.BEACON, ChatColor.translateAlternateColorCodes('&',"&fChange Region Type"),
                List.of("&7Requires arguments", "&8", "&7Click to show usage"),
                null, ArgAdmin.getChangeRegionTypeHelp()));

        actions.add(new Action(Material.SLIME_BALL, ChatColor.translateAlternateColorCodes('&',"&fForce Merge"),
                List.of("&7Requires arguments", "&8", "&7Click to show usage"),
                null, ArgAdmin.getForceMergeHelp()));

        // Layout (simple grid)
        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34
        };
        for (int i = 0; i < actions.size() && i < slots.length; i++) {
            Action a = actions.get(i);
            inv.setItem(slots[i], GuiItems.item(a.icon(), a.title(), a.lore()));
        }

        inv.setItem(44, GuiItems.item(Material.BARRIER, "&cClose"));
    }

    @Override
    protected void onClick(Player viewer, InventoryClickEvent e) {
        int raw = e.getRawSlot();
        if (raw == 44) {
            gui.close(viewer);
            return;
        }

        String base = ProtectionStones.getInstance().getConfigOptions().base_command;

        // Map raw slot to action index using same slot array
        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34
        };

        int idx = -1;
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == raw) { idx = i; break; }
        }
        if (idx == -1) return;

        // Keep this list in sync with draw()
        List<Action> actions = new ArrayList<>();
        actions.add(new Action(Material.PAPER, ChatColor.translateAlternateColorCodes('&',"&bVersion"),
                List.of("&7Show plugin/server versions", "&8", "&7Runs:", "&f/" + base + " admin version"),
                "version", null));
        actions.add(new Action(Material.BOOK, ChatColor.translateAlternateColorCodes('&',"&eStats"),
                List.of("&7Show PS region stats", "&8", "&7Runs:", "&f/" + base + " admin stats"),
                "stats", null));
        actions.add(new Action(Material.REDSTONE_TORCH, ChatColor.translateAlternateColorCodes('&',"&dToggle Debug"),
                List.of("&7Toggle debug mode", "&8", "&7Runs:", "&f/" + base + " admin debug"),
                "debug", null));
        actions.add(new Action(Material.ANVIL, ChatColor.translateAlternateColorCodes('&',"&cFix Regions"),
                List.of("&7Run legacy region fix/upgrade", "&8", "&cThis may modify region data.", "&8", "&7Runs:", "&f/" + base + " admin fixregions"),
                "fixregions", null));
        actions.add(new Action(Material.GOLD_INGOT, ChatColor.translateAlternateColorCodes('&',"&6Set Tax Autopayers"),
                List.of("&7Set missing tax autopayers", "&8", "&7Runs:", "&f/" + base + " admin settaxautopayers"),
                "settaxautopayers", null));
        actions.add(new Action(Material.HOPPER, ChatColor.translateAlternateColorCodes('&',"&fCleanup"),
                List.of("&7Requires arguments", "&8", "&7Click to show usage"),
                null, ArgAdmin.getCleanupHelp()));
        actions.add(new Action(Material.NAME_TAG, ChatColor.translateAlternateColorCodes('&',"&fFlag"),
                List.of("&7Requires arguments", "&8", "&7Click to show usage"),
                null, ArgAdmin.getFlagHelp()));
        actions.add(new Action(Material.IRON_BLOCK, ChatColor.translateAlternateColorCodes('&',"&fChange Block"),
                List.of("&7Requires arguments", "&8", "&7Click to show usage"),
                null, ArgAdmin.getChangeBlockHelp()));
        actions.add(new Action(Material.BEACON, ChatColor.translateAlternateColorCodes('&',"&fChange Region Type"),
                List.of("&7Requires arguments", "&8", "&7Click to show usage"),
                null, ArgAdmin.getChangeRegionTypeHelp()));
        actions.add(new Action(Material.SLIME_BALL, ChatColor.translateAlternateColorCodes('&',"&fForce Merge"),
                List.of("&7Requires arguments", "&8", "&7Click to show usage"),
                null, ArgAdmin.getForceMergeHelp()));

        if (idx >= actions.size()) return;
        Action a = actions.get(idx);

        if (a.requiresHelp() != null) {
            PSL.msg(viewer, a.requiresHelp());
            return;
        }

        if (a.commandSuffix() == null) return;

        String full = base + " admin " + a.commandSuffix();

        Supplier<BaseGui> back = () -> new AdminMenuGui(gui);
        gui.open(viewer, new ConfirmGui(
                gui,
                "Confirm Admin",
                a.icon(),
                "&cRun: &f/" + full,
                new String[]{"&7Are you sure?", "&8", "&7This will execute an admin command."},
                p -> Bukkit.dispatchCommand(p, full),
                back,
                back
        ));
    }
}
