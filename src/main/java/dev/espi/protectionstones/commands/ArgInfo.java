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

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.*;
import dev.espi.protectionstones.utils.UUIDCache;
import dev.espi.protectionstones.utils.WGUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class ArgInfo implements PSCommandArg {

    @Override
    public List<String> getNames() {
        return Collections.singletonList("info");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public List<String> getPermissionsToExecute() {
        return Arrays.asList("protectionstones.info");
    }

    @Override
    public HashMap<String, Boolean> getRegisteredFlags() {
        return null;
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args, HashMap<String, String> flags) {
        Player p = (Player) s;
        PSRegion r = PSRegion.fromLocationGroupUnsafe(p.getLocation());

        if (r == null)
            return PSL.NOT_IN_REGION.send(p);

        if (!p.hasPermission("protectionstones.info.others") && WGUtils.hasNoAccess(r.getWGRegion(), p, WorldGuardPlugin.inst().wrapPlayer(p), true))
            return PSL.NO_ACCESS.send(p);

        if (r.getTypeOptions() == null) {
            PSL.msg(p,
                    Component.text("This region is problematic, and the block type (", NamedTextColor.RED)
                            .append(Component.text(r.getType(), NamedTextColor.AQUA))
                            .append(Component.text(") is not configured. Please contact an administrator.", NamedTextColor.RED))
            );
            Bukkit.getLogger().info(ChatColor.RED + "This region is problematic, and the block type (" + r.getType() + ") is not configured.");
            return true;
        }

        if (args.length == 1) { // info of current region player is in
            if (!p.hasPermission("protectionstones.info"))
                return PSL.NO_PERMISSION_INFO.send(p);

            PSL.msg(p, PSL.INFO_HEADER.msg());

            // region: %region%, priority: %priority%
            StringBuilder sb = new StringBuilder();

            if (r.getName() == null) {
                PSL.INFO_REGION2.append(sb, r.getId());
            } else {
                PSL.INFO_REGION2.append(sb, r.getName() + " (" + r.getId() + ")");
            }

            if (!PSL.INFO_PRIORITY2.isEmpty()) {
                sb.append(", ").append(PSL.INFO_PRIORITY2.format(r.getWGRegion().getPriority()));
            }
            PSL.msg(p, MiniMessage.miniMessage().deserialize(sb.toString()));

            // type: %type%
            if (r instanceof PSGroupRegion) {
                PSL.INFO_TYPE2.send(p, r.getTypeOptions().alias + " " + PSL.INFO_MAY_BE_MERGED.msg());
                displayMerged(p, (PSGroupRegion) r);
            } else {
                PSL.INFO_TYPE2.send(p, r.getTypeOptions().alias);
            }

            displayEconomy(p, r);
            displayFlags(p, r);
            displayOwners(p, r.getWGRegion());
            displayMembers(p, r.getWGRegion());

            if (r.getParent() != null) {
                if (r.getName() != null) {
                    PSL.INFO_PARENT2.send(p, r.getParent().getName() + " (" + r.getParent().getId() + ")");
                } else {
                    PSL.INFO_PARENT2.send(p, r.getParent().getId());
                }
            }

            BlockVector3 min = r.getWGRegion().getMinimumPoint();
            BlockVector3 max = r.getWGRegion().getMaximumPoint();
            // only show x,z if it's at block limit
            //noinspection removal
            if (min.getBlockY() == WGUtils.MIN_BUILD_HEIGHT && max.getBlockY() == WGUtils.MAX_BUILD_HEIGHT) {
                PSL.INFO_BOUNDS_XZ.send(p,
                        min.getBlockX(), min.getBlockZ(),
                        max.getBlockX(), max.getBlockZ()
                );
            } else {
                PSL.INFO_BOUNDS_XYZ.send(p,
                        min.getBlockX(), min.getBlockY(), min.getBlockZ(),
                        max.getBlockX(), max.getBlockY(), max.getBlockZ()
                );
            }

        } else if (args.length == 2) { // get specific information on current region

            switch (args[1].toLowerCase()) {
                case "members":
                    if (!p.hasPermission("protectionstones.members"))
                        return PSL.NO_PERMISSION_MEMBERS.send(p);

                    displayMembers(p, r.getWGRegion());
                    break;
                case "owners":
                    if (!p.hasPermission("protectionstones.owners"))
                        return PSL.NO_PERMISSION_OWNERS.send(p);

                    displayOwners(p, r.getWGRegion());
                    break;
                case "flags":
                    if (!p.hasPermission("protectionstones.flags"))
                        return PSL.NO_PERMISSION_FLAGS.send(p);
                        displayFlags(p, r);
                    break;
                default:
                    PSL.INFO_HELP.send(p);
                    break;
            }
        } else {
            PSL.INFO_HELP.send(p);
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return null;
    }

    private static void displayMerged(Player p, PSGroupRegion r) {
        StringBuilder msg = new StringBuilder();
        for (PSMergedRegion pr : r.getMergedRegions()) {
            msg.append(pr.getId()).append(" (").append(pr.getTypeOptions().alias).append("), ");
        }
        PSL.INFO_MERGED2.send(p, msg);
    }

    private static void displayEconomy(Player p, PSRegion r) {
        if (r.forSale()) {
            PSL.INFO_AVAILABLE_FOR_SALE.send(p);
            PSL.INFO_SELLER2.send(p, UUIDCache.getNameFromUUID(r.getLandlord()));
            PSL.INFO_PRICE2.send(p, String.format("%.2f", r.getPrice()));
        }
        if (r.getRentStage() == PSRegion.RentStage.LOOKING_FOR_TENANT) {
            PSL.INFO_AVAILABLE_FOR_SALE.send(p);
        }
        if (r.getRentStage() == PSRegion.RentStage.RENTING) {
            PSL.INFO_TENANT2.send(p, UUIDCache.getNameFromUUID(r.getTenant()));
        }
        if (r.getRentStage() != PSRegion.RentStage.NOT_RENTING) {
            PSL.INFO_LANDLORD2.send(p, UUIDCache.getNameFromUUID(r.getLandlord()));
            PSL.INFO_RENT2.send(p, String.format("%.2f", r.getPrice()));
        }
    }

    private static void displayFlags(Player p, PSRegion r) {
        ProtectedRegion region = r.getWGRegion();
        PSProtectBlock typeOptions = r.getTypeOptions();

        StringBuilder flagDisp = new StringBuilder();
        String flagValue;
        // loop through all flags
        for (Flag<?> flag : WGUtils.getFlagRegistry().getAll()) {
            if (region.getFlag(flag) != null && !typeOptions.hiddenFlagsFromInfo.contains(flag.getName())) {
                flagValue = region.getFlag(flag).toString();
                RegionGroupFlag groupFlag = flag.getRegionGroupFlag();

                if (region.getFlag(groupFlag) != null) {
                    flagDisp.append(String.format("%s: -g %s %s, ", flag.getName(), region.getFlag(groupFlag), flagValue));
                } else {
                    flagDisp.append(String.format("%s: %s, ", flag.getName(), flagValue));
                }
                flagDisp.append(ChatColor.GRAY);
            }
        }

        if (flagDisp.length() > 2) {
            flagDisp = new StringBuilder(flagDisp.substring(0, flagDisp.length() - 2) + ".");
            PSL.INFO_FLAGS2.send(p, flagDisp);
        } else {
            PSL.INFO_FLAGS2.send(p, PSL.INFO_NO_FLAGS.msg());
        }
    }

    private static void displayOwners(Player p, ProtectedRegion region) {
        DefaultDomain owners = region.getOwners();
        StringBuilder msg = new StringBuilder();
        if (owners.size() == 0) {
            PSL.INFO_NO_OWNERS.append(msg);
        } else {
            for (UUID uuid : owners.getUniqueIds()) {
                String name = UUIDCache.getNameFromUUID(uuid);
                if (name == null) name = Bukkit.getOfflinePlayer(uuid).getName();
                msg.append(name).append(", ");
            }
            for (String name : owners.getPlayers()) { // legacy purposes
                msg.append(name).append(", ");
            }
            msg = new StringBuilder(msg.substring(0, msg.length() - 2));
        }
        PSL.INFO_OWNERS2.send(p, msg);
    }

    private static void displayMembers(Player p, ProtectedRegion region) {
        DefaultDomain members = region.getMembers();
        StringBuilder msg = new StringBuilder();
        if (members.size() == 0) {
            PSL.INFO_NO_MEMBERS.append(msg);
        } else {
            for (UUID uuid : members.getUniqueIds()) {
                String name = UUIDCache.getNameFromUUID(uuid);
                if (name == null) name = uuid.toString();
                msg.append(name).append(", ");
            }
            for (String name : members.getPlayers()) { // legacy purposes
                msg.append(name).append(", ");
            }
            msg = new StringBuilder(msg.substring(0, msg.length() - 2));
        }
        PSL.INFO_MEMBERS2.send(p, msg);
    }
}
