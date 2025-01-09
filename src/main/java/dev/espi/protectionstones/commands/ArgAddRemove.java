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
        String operationType = args[0].toLowerCase();

        if ((operationType.equals("add") || operationType.equals("remove")) && !p.hasPermission("protectionstones.members")) {
            return PSL.msg(p, PSL.NO_PERMISSION_MEMBERS.msg());
        } else if ((operationType.equals("addowner") || operationType.equals("removeowner")) && !p.hasPermission("protectionstones.owners")) {
            return PSL.msg(p, PSL.NO_PERMISSION_OWNERS.msg());
        }

        if (args.length < 2) {
            return PSL.msg(p, PSL.COMMAND_REQUIRES_PLAYER_NAME.msg());
        }
        if (!UUIDCache.containsName(args[1])) {
            return PSL.msg(p, PSL.PLAYER_NOT_FOUND.msg());
        }

        UUID addPlayerUuid = UUIDCache.getUUIDFromName(args[1]);
        String addPlayerName = UUIDCache.getNameFromUUID(addPlayerUuid);

        Bukkit.getServer().getScheduler().runTaskAsynchronously(ProtectionStones.getInstance(), () -> {
            List<PSRegion> regions;

            if (flags.containsKey("-a")) {
                if (operationType.equals("removeowner") && addPlayerUuid.equals(p.getUniqueId())) {
                    PSL.msg(p, PSL.CANNOT_REMOVE_YOURSELF_FROM_ALL_REGIONS.msg());
                    return;
                }
                regions = PSPlayer.fromPlayer(p).getPSRegions(p.getWorld(), false);
            } else {
                PSRegion r = PSRegion.fromLocationGroup(p.getLocation());
                if (r == null) {
                    PSL.msg(p, PSL.NOT_IN_REGION.msg());
                    return;
                } else if (WGUtils.hasNoAccess(r.getWGRegion(), p, WorldGuardPlugin.inst().wrapPlayer(p), false)) {
                    PSL.msg(p, PSL.NO_ACCESS.msg());
                    return;
                } else if (operationType.equals("removeowner") && addPlayerUuid.equals(p.getUniqueId()) && r.getOwners().size() == 1) {
                    PSL.msg(p, PSL.CANNOT_REMOVE_YOURSELF_LAST_OWNER.msg());
                    return;
                }
                regions = Collections.singletonList(r);
            }

            if (operationType.equals("addowner")) {
                if (determinePlayerSurpassedLimit(p, regions, PSPlayer.fromUUID(addPlayerUuid))) {
                    return;
                }
            }

            for (PSRegion r : regions) {
                if (operationType.equals("add") || operationType.equals("addowner")) {
                    if (flags.containsKey("-a")) {
                        PSL.msg(p, PSL.ADDED_TO_REGION_SPECIFIC.msg()
                                .replace("%player%", addPlayerName)
                                .replace("%region%", r.getName() == null ? r.getId() : r.getName() + " (" + r.getId() + ")"));
                    } else {
                        PSL.msg(p, PSL.ADDED_TO_REGION.msg().replace("%player%", addPlayerName));
                    }
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
                            if (p.canSee(pAdd)) {
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
        String err = LimitUtil.checkAddOwner(
                addedPlayer,
                regionsToBeAddedTo.stream()
                        .flatMap(r -> {
                            if (r instanceof PSGroupRegion) {
                                return ((PSGroupRegion) r).getMergedRegions().stream();
                            }
                            return Stream.of(r);
                        })
                        .map(PSRegion::getTypeOptions)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
        );
        if (err.equals("")) {
            return false;
        } else {
            PSL.msg(commandSender, err);
            return true;
        }
    }
}
