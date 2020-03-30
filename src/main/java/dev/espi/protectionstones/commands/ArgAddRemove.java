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
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.PSPlayer;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.utils.UUIDCache;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

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
        String operationType = args[0]; // add, remove, addowner, removeowner

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
        UUID addUuid = UUIDCache.getUUIDFromName(args[1]);

        List<PSRegion> regions;

        if (flags.containsKey("-a")) { // add or remove to all regions a player owns
            regions = PSPlayer.fromPlayer(p).getPSRegions(p.getWorld(), false);
        } else { // add or remove to one region (the region currently in)
            PSRegion r = PSRegion.fromLocationGroup(p.getLocation());

            if (r == null) {
                return PSL.msg(p, PSL.NOT_IN_REGION.msg());
            } else if (WGUtils.hasNoAccess(r.getWGRegion(), p, WorldGuardPlugin.inst().wrapPlayer(p), false)) {
                return PSL.msg(p, PSL.NO_ACCESS.msg());
            }
            regions = Collections.singletonList(r);
        }

        // apply to regions
        for (PSRegion r : regions) {

            if (operationType.equals("add") || operationType.equals("addowner")) {
                if (flags.containsKey("-a")) {
                    PSL.msg(p, PSL.ADDED_TO_REGION_SPECIFIC.msg()
                            .replace("%player%", UUIDCache.getNameFromUUID(addUuid))
                            .replace("%region%", r.getName() == null ? r.getId() : r.getName() + " (" + r.getId() + ")"));
                } else {
                    PSL.msg(p, PSL.ADDED_TO_REGION.msg().replace("%player%", UUIDCache.getNameFromUUID(addUuid)));
                }
            } else if ((operationType.equals("remove") && r.getWGRegion().getMembers().contains(addUuid)) || (operationType.equals("removeowner") && r.getWGRegion().getOwners().contains(addUuid))) {
                if (flags.containsKey("-a")) {
                    PSL.msg(p, PSL.REMOVED_FROM_REGION_SPECIFIC.msg()
                            .replace("%player%", UUIDCache.getNameFromUUID(addUuid))
                            .replace("%region%", r.getName() == null ? r.getId() : r.getName() + " (" + r.getId() + ")"));
                } else {
                    PSL.msg(p, PSL.REMOVED_FROM_REGION.msg().replace("%player%", UUIDCache.getNameFromUUID(addUuid)));
                }
            }

            switch (operationType) {
                case "add":
                    r.getWGRegion().getMembers().addPlayer(addUuid);
                    break;
                case "remove":
                    r.getWGRegion().getMembers().removePlayer(addUuid);
                    break;
                case "addowner":
                    r.getWGRegion().getOwners().addPlayer(addUuid);
                    break;
                case "removeowner":
                    r.getWGRegion().getOwners().removePlayer(addUuid);
                    break;
            }
        }
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

                return StringUtil.copyPartialMatches(args[args.length-1], ret, new ArrayList<>());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
