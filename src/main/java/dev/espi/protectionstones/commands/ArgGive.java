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

import dev.espi.protectionstones.PSProtectBlock;
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.ProtectionStones;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.*;

public class ArgGive implements PSCommandArg {

    @Override
    public List<String> getNames() {
        return Collections.singletonList("give");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return true;
    }

    @Override
    public List<String> getPermissionsToExecute() {
        return Collections.singletonList("protectionstones.give");
    }

    @Override
    public HashMap<String, Boolean> getRegisteredFlags() {
        return null;
    }

    @Override
    public boolean executeArgument(CommandSender p, String[] args, HashMap<String, String> flags) {
        if (!p.hasPermission("protectionstones.give"))
            return PSL.msg(p, PSL.NO_PERMISSION_GIVE.msg());

        if (args.length < 3)
            return PSL.msg(p, PSL.GIVE_HELP.msg());

        // check if player online
        if (Bukkit.getPlayer(args[2]) == null)
            return PSL.msg(p,
                    PSL.PLAYER_NOT_FOUND.msg()
                            .append(Component.text(" (" + args[2] + ")", NamedTextColor.GRAY))
            );


        // check if argument is valid block
        PSProtectBlock cp = ProtectionStones.getProtectBlockFromAlias(args[1]);
        if (cp == null)
            return PSL.msg(p, PSL.INVALID_BLOCK.msg());

        // check if item was able to be added (inventory not full)
        Player ps = Bukkit.getPlayer(args[2]);

        ItemStack item = cp.createItem();
        if (args.length >= 4 && NumberUtils.isNumber(args[3]))
            item.setAmount(Integer.parseInt(args[3]));

        if (!ps.getInventory().addItem(item).isEmpty()) {
            if (ProtectionStones.getInstance().getConfigOptions().dropItemWhenInventoryFull) {
                PSL.msg(ps, PSL.NO_ROOM_DROPPING_ON_FLOOR.msg());
                ps.getWorld().dropItem(ps.getLocation(), cp.createItem());
            } else {
                return PSL.msg(p, PSL.GIVE_NO_INVENTORY_ROOM.msg());
            }
        }

        return PSL.msg(p,
                PSL.GIVE_GIVEN.replaceAll(Map.of(
                        "%block%", args[1],
                        "%player%", Bukkit.getPlayer(args[2]).getDisplayName()
                ))
        );
    }

    // tab completion
    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        List<String> l = new ArrayList<>();
        if (args.length == 2) {
            for (PSProtectBlock b : ProtectionStones.getInstance().getConfiguredBlocks()) l.add(b.alias);
            return StringUtil.copyPartialMatches(args[1], l, new ArrayList<>());
        } else if (args.length == 3) {
            for (Player p : Bukkit.getOnlinePlayers()) l.add(p.getName());
            return StringUtil.copyPartialMatches(args[2], l, new ArrayList<>());
        }
        return null;
    }

}
