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
import dev.espi.protectionstones.utils.TextGUI;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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


        if (!p.hasPermission("protectionstones.unclaim")) {
            PSL.msg(p, PSL.NO_PERMISSION_UNCLAIM.msg());
            return true;
        }

        if (args.length >= 2) { // /ps unclaim [list|region-id] (unclaim remote region)

            if (!p.hasPermission("protectionstones.unclaim.remote")) {
                PSL.msg(p, PSL.NO_PERMISSION_UNCLAIM_REMOTE.msg());
                return true;
            }

            PSPlayer psp = PSPlayer.fromPlayer(p);

            // list of regions that the player owns
            List<PSRegion> regions = psp.getPSRegionsCrossWorld(psp.getPlayer().getWorld(), false);

            if (args[1].equalsIgnoreCase("list")) {
                displayPSRegions(s, regions, args.length == 2 ? 0 : tryParseInt(args[2]) - 1);
            } else {
                for (PSRegion psr : regions) {
                    if (psr.getId().equalsIgnoreCase(args[1])) {
                        // cannot break region being rented (prevents splitting merged regions, and breaking as tenant owner)
                        if (psr.getRentStage() == PSRegion.RentStage.RENTING && !p.hasPermission("protectionstones.superowner")) {
                            PSL.msg(p, PSL.RENT_CANNOT_BREAK_WHILE_RENTING.msg());
                            return false;
                        }
                        return unclaimBlock(psr, p);
                    }
                }
                PSL.msg(p, PSL.REGION_DOES_NOT_EXIST.msg());
            }

            return true;
        } else { // /ps unclaim (no arguments, unclaim current region)
            PSRegion r = PSRegion.fromLocationGroupUnsafe(p.getLocation()); // allow unclaiming unconfigured regions

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
                return true;
            }
            return unclaimBlock(r, p);
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return null;
    }

    private int tryParseInt(String arg) {
        int i = 1;
        try {
            i = Integer.parseInt(arg);
        } catch (NumberFormatException ignore) {
            //ignore
        }
        return i;
    }

    private void displayPSRegions(CommandSender s, List<PSRegion> regions, int page) {
        List<TextComponent> entries = new ArrayList<>();
        for (PSRegion rs : regions) {
            String msg;
            if (rs.getName() == null) {
                msg = ChatColor.GRAY + "> " + ChatColor.AQUA + rs.getId();
            } else {
                msg = ChatColor.GRAY + "> " + ChatColor.AQUA + rs.getName() + " (" + rs.getId() + ")";
            }
            TextComponent tc = new TextComponent(ChatColor.AQUA + " [-] " + msg);
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to unclaim " + rs.getId()).create()));
            tc.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + ProtectionStones.getInstance().getConfigOptions().base_command + " unclaim " + rs.getId()));
            entries.add(tc);
        }
        TextGUI.displayGUI(s, PSL.UNCLAIM_HEADER.msg(), "/" + ProtectionStones.getInstance().getConfigOptions().base_command + " unclaim list %page%", page, 17, entries, true);
    }

    private boolean unclaimBlock(PSRegion r, Player p) {
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
}
