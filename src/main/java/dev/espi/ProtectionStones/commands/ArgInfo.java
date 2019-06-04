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

package dev.espi.ProtectionStones.commands;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.ProtectionStones.PSL;
import dev.espi.ProtectionStones.ProtectionStones;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ArgInfo {
    public static boolean argumentInfo(Player p, String[] args) {
        String psID = ProtectionStones.playerToPSID(p);

        WorldGuardPlugin wg = (WorldGuardPlugin) ProtectionStones.wgd;
        RegionManager rgm = ProtectionStones.getRegionManagerWithPlayer(p);

        if (psID.equals("")) {
            PSL.msg(p, PSL.NOT_IN_REGION.msg());
            return true;
        }
        ProtectedRegion region = rgm.getRegion(psID);
        if (region == null) {
            PSL.msg(p, PSL.REGION_DOES_NOT_EXIST.msg());
            return true;
        }

        if (ProtectionStones.hasNoAccess(rgm.getRegion(psID), p, wg.wrapPlayer(p), true)) {
            PSL.msg(p, PSL.NO_ACCESS.msg());
            return true;
        }

        if (args.length == 1) { // info of current region player is in
            if (!p.hasPermission("protectionstones.info")) {
                PSL.msg(p, PSL.NO_PERMISSION_INFO.msg());
                return true;
            }

            PSL.msg(p, PSL.INFO_HEADER.msg());
            PSL.msg(p, PSL.INFO_REGION.msg() + psID + ", " + PSL.INFO_PRIORITY.msg() + rgm.getRegion(psID).getPriority());


            displayFlags(p, region);
            displayOwners(p, region);
            displayMembers(p, region);

            BlockVector3 min = region.getMinimumPoint();
            BlockVector3 max = region.getMaximumPoint();
            PSL.msg(p, PSL.INFO_BOUNDS.msg() + "(" + min.getBlockX() + "," + min.getBlockY() + "," + min.getBlockZ() + ") -> (" + max.getBlockX() + "," + max.getBlockY() + "," + max.getBlockZ() + ")");

        } else if (args.length == 2) { // get specific information on current region

            switch (args[1].toLowerCase()) {
                case "members":
                    if (!p.hasPermission("protectionstones.members")) {
                        PSL.msg(p, PSL.NO_PERMISSION_MEMBERS.msg());
                        return true;
                    }
                    displayMembers(p, region);
                    break;
                case "owners":
                    if (!p.hasPermission("protectionstones.owners")) {
                        PSL.msg(p, PSL.NO_PERMISSION_OWNERS.msg());
                        return true;
                    }
                    displayOwners(p, region);
                    break;
                case "flags":
                    if (!p.hasPermission("protectionstones.flags")) {
                        PSL.msg(p, PSL.NO_PERMISSION_FLAGS.msg());
                        return true;
                    }
                    displayFlags(p, region);
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
                String name = ProtectionStones.uuidToName.get(uuid);
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
                String name = ProtectionStones.uuidToName.get(uuid);
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
