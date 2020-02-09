/*
 * Copyright 2019 ProtectionStones team and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.espi.protectionstones.commands;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.PSPlayer;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.utils.UUIDCache;
import dev.espi.protectionstones.utils.WGUtils;
import org.apache.commons.lang.StringUtils;
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
        if (!UUIDCache.nameToUUID.containsKey(args[1])) {
            return PSL.msg(p, PSL.PLAYER_NOT_FOUND.msg());
        }
        UUID addUuid = UUIDCache.nameToUUID.get(args[1]);

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
                            .replace("%player%", args[1])
                            .replace("%region%", r.getName() == null ? r.getID() : r.getName() + " (" + r.getID() + ")"));
                } else {
                    PSL.msg(p, PSL.ADDED_TO_REGION.msg().replace("%player%", args[1]));
                }
            } else if ((operationType.equals("remove") && r.getWGRegion().getMembers().contains(addUuid)) || (operationType.equals("removeowner") && r.getWGRegion().getOwners().contains(addUuid))) {
                if (flags.containsKey("-a")) {
                    PSL.msg(p, PSL.REMOVED_FROM_REGION_SPECIFIC.msg()
                            .replace("%player%", args[1])
                            .replace("%region%", r.getName() == null ? r.getID() : r.getName() + " (" + r.getID() + ")"));
                } else {
                    PSL.msg(p, PSL.REMOVED_FROM_REGION.msg().replace("%player%", args[1]));
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
                                names.add(UUIDCache.uuidToName.get(uuid));
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
