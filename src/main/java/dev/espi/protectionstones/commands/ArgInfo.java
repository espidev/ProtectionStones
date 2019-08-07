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

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.utils.UUIDCache;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
    public boolean executeArgument(CommandSender s, String[] args) {
        Player p = (Player) s;
        PSRegion r = PSRegion.fromLocation(p.getLocation());

        if (r == null) {
            PSL.msg(p, PSL.NOT_IN_REGION.msg());
            return true;
        }
        if (!p.hasPermission("protectionstones.info.others") && WGUtils.hasNoAccess(r.getWGRegion(), p, WorldGuardPlugin.inst().wrapPlayer(p), true)) {
            PSL.msg(p, PSL.NO_ACCESS.msg());
            return true;
        }

        if (args.length == 1) { // info of current region player is in
            if (!p.hasPermission("protectionstones.info")) {
                PSL.msg(p, PSL.NO_PERMISSION_INFO.msg());
                return true;
            }

            PSL.msg(p, PSL.INFO_HEADER.msg());
            if (r.getName() == null) {
                PSL.msg(p, PSL.INFO_REGION.msg() + r.getID() + ", " + PSL.INFO_PRIORITY.msg() + r.getWGRegion().getPriority());
            } else {
                PSL.msg(p, PSL.INFO_REGION.msg() + r.getName() + " (" + r.getID() + "), " + PSL.INFO_PRIORITY.msg() + r.getWGRegion().getPriority());
            }

            displayFlags(p, r.getWGRegion());
            displayOwners(p, r.getWGRegion());
            displayMembers(p, r.getWGRegion());

            if (r.getParent() != null) {
                if (r.getName() != null) {
                    PSL.msg(p, PSL.INFO_PARENT.msg() + r.getParent().getName() + " (" + r.getParent().getID() + ")");
                } else {
                    PSL.msg(p, PSL.INFO_PARENT.msg() + r.getParent().getID());
                }
            }

            BlockVector3 min = r.getWGRegion().getMinimumPoint();
            BlockVector3 max = r.getWGRegion().getMaximumPoint();
            PSL.msg(p, PSL.INFO_BOUNDS.msg() + "(" + min.getBlockX() + "," + min.getBlockY() + "," + min.getBlockZ() + ") -> (" + max.getBlockX() + "," + max.getBlockY() + "," + max.getBlockZ() + ")");

        } else if (args.length == 2) { // get specific information on current region

            switch (args[1].toLowerCase()) {
                case "members":
                    if (!p.hasPermission("protectionstones.members")) {
                        PSL.msg(p, PSL.NO_PERMISSION_MEMBERS.msg());
                        return true;
                    }
                    displayMembers(p, r.getWGRegion());
                    break;
                case "owners":
                    if (!p.hasPermission("protectionstones.owners")) {
                        PSL.msg(p, PSL.NO_PERMISSION_OWNERS.msg());
                        return true;
                    }
                    displayOwners(p, r.getWGRegion());
                    break;
                case "flags":
                    if (!p.hasPermission("protectionstones.flags")) {
                        PSL.msg(p, PSL.NO_PERMISSION_FLAGS.msg());
                        return true;
                    }
                    displayFlags(p, r.getWGRegion());
                    break;
                default:
                    PSL.msg(p, PSL.INFO_HELP.msg());
                    break;
            }
        } else {
            PSL.msg(p, PSL.INFO_HELP.msg());
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return null;
    }

    private static void displayFlags(Player p, ProtectedRegion region) {
        StringBuilder myFlag = new StringBuilder();
        String myFlagValue;
        for (Flag<?> flag : WorldGuard.getInstance().getFlagRegistry().getAll()) {
            if (region.getFlag(flag) != null) {
                myFlagValue = region.getFlag(flag).toString();
                RegionGroupFlag groupFlag = flag.getRegionGroupFlag();

                if (region.getFlag(groupFlag) != null) {
                    myFlag.append(flag.getName()).append(" -g ").append(region.getFlag(groupFlag)).append(" ").append(myFlagValue).append(", ");
                } else {
                    myFlag.append(flag.getName()).append(": ").append(myFlagValue).append(", ");
                }
            }
        }

        if (myFlag.length() > 2) {
            myFlag = new StringBuilder(myFlag.substring(0, myFlag.length() - 2) + ".");
            PSL.msg(p, PSL.INFO_FLAGS.msg() + myFlag);
        } else {
            PSL.msg(p, PSL.INFO_FLAGS.msg() + "(none)");
        }
    }

    private static void displayOwners(Player p, ProtectedRegion region) {
        DefaultDomain owners = region.getOwners();
        StringBuilder send = new StringBuilder(PSL.INFO_OWNERS.msg());
        if (owners.size() == 0) {
            send.append(PSL.INFO_NO_OWNERS.msg());
            PSL.msg(p, send.toString());
        } else {
            for (UUID uuid : owners.getUniqueIds()) {
                String name = UUIDCache.uuidToName.get(uuid);
                if (name == null) name = Bukkit.getOfflinePlayer(uuid).getName();
                send.append(name).append(", ");
            }
            for (String name : owners.getPlayers()) { // legacy purposes
                send.append(name).append(", ");
            }
            PSL.msg(p, send.substring(0, send.length() - 2));
        }
    }

    private static void displayMembers(Player p, ProtectedRegion region) {
        DefaultDomain members = region.getMembers();
        StringBuilder send = new StringBuilder(PSL.INFO_MEMBERS.msg());
        if (members.size() == 0) {
            send.append(PSL.INFO_NO_MEMBERS.msg());
            PSL.msg(p, send.toString());
        } else {
            for (UUID uuid : members.getUniqueIds()) {
                String name = UUIDCache.uuidToName.get(uuid);
                if (name == null) name = uuid.toString();
                send.append(name).append(", ");
            }
            for (String name : members.getPlayers()) { // legacy purposes
                send.append(name).append(", ");
            }
            PSL.msg(p, send.substring(0, send.length() - 2));
        }
    }
}
