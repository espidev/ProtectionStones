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

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import dev.espi.protectionstones.*;
import dev.espi.protectionstones.utils.LimitUtil;
import dev.espi.protectionstones.utils.UUIDCache;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArgAddRemove implements PSCommandArg {

    @Override
    public List<String> getNames() {
        return Arrays.asList("add", "remove", "addowner", "removeowner");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public List<String> getPermissionsToExecute() {
        return Arrays.asList("protectionstones.members", "protectionstones.owners");
    }

    @Override
    public HashMap<String, Boolean> getRegisteredFlags() {
        HashMap<String, Boolean> m = new HashMap<>();
        m.put("-a", false);
        return m;
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args, HashMap<String, String> flags) {
        Player p = (Player) s;
        String operationType = args[0].toLowerCase(); // add, remove, addowner, removeowner

        // check permission
        if ((operationType.equals("add") || operationType.equals("remove")) && !p.hasPermission("protectionstones.members")) {
            return PSL.msg(p, PSL.NO_PERMISSION_MEMBERS.msg());
        } else if ((operationType.equals("addowner") || operationType.equals("removeowner")) && !p.hasPermission("protectionstones.owners")) {
            return PSL.msg(p, PSL.NO_PERMISSION_OWNERS.msg());
        }

        // determine player to be added or removed
        if (args.length < 2) {
            return PSL.msg(p, PSL.COMMAND_REQUIRES_PLAYER_NAME.msg());
        }
        if (!UUIDCache.containsName(args[1])) {
            return PSL.msg(p, PSL.PLAYER_NOT_FOUND.msg());
        }

        // user being added
        UUID addPlayerUuid = UUIDCache.getUUIDFromName(args[1]);
        String addPlayerName = UUIDCache.getNameFromUUID(addPlayerUuid);

        // getting player regions is slow, so run it async
        Bukkit.getServer().getScheduler().runTaskAsynchronously(ProtectionStones.getInstance(), () -> {
            List<PSRegion> regions;

            // obtain region list that player is being added to or removed from
            if (flags.containsKey("-a")) { // add or remove to all regions a player owns

                // don't let players remove themself from all of their regions
                if (operationType.equals("removeowner") && addPlayerUuid.equals(p.getUniqueId())) {
                    PSL.msg(p, PSL.CANNOT_REMOVE_YOURSELF_FROM_ALL_REGIONS.msg());
                    return;
                }

                regions = PSPlayer.fromPlayer(p).getPSRegions(p.getWorld(), false);
            } else { // add or remove to one region (the region currently in)
                PSRegion r = PSRegion.fromLocationGroup(p.getLocation());

                if (r == null) {
                    PSL.msg(p, PSL.NOT_IN_REGION.msg());
                    return;
                } else if (WGUtils.hasNoAccess(r.getWGRegion(), p, WorldGuardPlugin.inst().wrapPlayer(p), false)) {
                    PSL.msg(p, PSL.NO_ACCESS.msg());
                    return;
                } else if (operationType.equals("removeowner") && addPlayerUuid.equals(p.getUniqueId()) && r.getOwners().size() == 1) {
                    // don't let users remove themself if they are the last owner of the region
                    PSL.msg(p, PSL.CANNOT_REMOVE_YOURSELF_LAST_OWNER.msg());
                    return;
                }

                regions = Collections.singletonList(r);
            }

            // check that the player is not over their limit if they are being set owner
            if (operationType.equals("addowner")) {
                if (determinePlayerSurpassedLimit(p, regions, PSPlayer.fromUUID(addPlayerUuid))) {
                    return;
                }
            }

            // apply operation to regions
            for (PSRegion r : regions) {

                if (operationType.equals("add") || operationType.equals("addowner")) {
                    if (flags.containsKey("-a")) {
                        PSL.msg(p, PSL.ADDED_TO_REGION_SPECIFIC.msg()
                                .replace("%player%", addPlayerName)
                                .replace("%region%", r.getName() == null ? r.getId() : r.getName() + " (" + r.getId() + ")"));
                    } else {
                        PSL.msg(p, PSL.ADDED_TO_REGION.msg().replace("%player%", addPlayerName));
                    }

                    // add to WorldGuard profile cache
                    Bukkit.getScheduler().runTaskAsynchronously(ProtectionStones.getInstance(), () -> UUIDCache.storeWGProfile(addPlayerUuid, addPlayerName));

                } else if ((operationType.equals("remove") && r.isMember(addPlayerUuid))
                        || (operationType.equals("removeowner") && r.isOwner(addPlayerUuid))) {

                    if (flags.containsKey("-a")) {
                        PSL.msg(p, PSL.REMOVED_FROM_REGION_SPECIFIC.msg()
                                .replace("%player%", addPlayerName)
                                .replace("%region%", r.getName() == null ? r.getId() : r.getName() + " (" + r.getId() + ")"));
                    } else {
                        PSL.msg(p, PSL.REMOVED_FROM_REGION.msg().replace("%player%", addPlayerName));
                    }
                }

                switch (operationType) {
                    case "add" -> r.addMember(addPlayerUuid);
                    case "remove" -> r.removeMember(addPlayerUuid);
                    case "addowner" -> r.addOwner(addPlayerUuid);
                    case "removeowner" -> r.removeOwner(addPlayerUuid);
                }
            }
        });
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player)) return null;
        Player p = (Player) sender;

        List<String> ret = new ArrayList<>();

        if (args.length == 2) {
            ret.add("-a");
        }

        try {
            if (args.length == 2 || (args.length == 3 && args[1].equals("-a"))) {

                switch (args[0].toLowerCase()) {
                    case "add":
                    case "addowner":
                        List<String> names = new ArrayList<>();
                        for (Player pAdd : Bukkit.getOnlinePlayers()) {
                            if (p.canSee(pAdd)) { // check if the player is not hidden
                                names.add(pAdd.getName());
                            }
                        }
                        ret.addAll(names);
                        break;
                    case "remove":
                    case "removeowner":
                        PSRegion r = PSRegion.fromLocationGroup(p.getLocation());
                        if (r != null) {
                            names = new ArrayList<>();
                            for (UUID uuid : args[0].equalsIgnoreCase("remove") ? r.getMembers() : r.getOwners()) {
                                names.add(UUIDCache.getNameFromUUID(uuid));
                            }
                            ret.addAll(names);
                        }
                        break;
                }

                try {
                    return StringUtil.copyPartialMatches(args[args.length - 1], ret, new ArrayList<>());
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean determinePlayerSurpassedLimit(Player commandSender, List<PSRegion> regionsToBeAddedTo, PSPlayer addedPlayer) {

        if (addedPlayer.getPlayer() == null && !ProtectionStones.getInstance().isLuckPermsSupportEnabled()) { // offline player
            if (ProtectionStones.getInstance().getConfigOptions().allowAddownerForOfflinePlayersWithoutLp) {
                // bypass config option
                return false;
            } else {
                // we need luckperms to determine region limits for offline players, so if luckperms isn't detected, prevent the action
                PSL.msg(commandSender, PSL.ADDREMOVE_PLAYER_NEEDS_TO_BE_ONLINE.msg());
                return true;
            }
        }

        // find total region amounts after player is added to the regions, and their existing total
        String err = LimitUtil.checkAddOwner(addedPlayer, regionsToBeAddedTo.stream()
                .flatMap(r -> {
                    if (r instanceof PSGroupRegion) {
                        return ((PSGroupRegion) r).getMergedRegions().stream();
                    }
                    return Stream.of(r);
                })
                .map(PSRegion::getTypeOptions)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        if (err.equals("")) {
            return false;
        } else {
            PSL.msg(commandSender, err);
            return true;
        }
    }
}
