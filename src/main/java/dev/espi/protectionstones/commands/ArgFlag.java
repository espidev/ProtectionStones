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
 * See the License for the specific  language governing permissions and
 * limitations under the License.
 */

package dev.espi.protectionstones.commands;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.FlagHandler;
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class ArgFlag implements PSCommandArg {

    @Override
    public List<String> getNames() {
        return Collections.singletonList("flag");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args) {
        Player p = (Player) s;
        PSRegion r = PSRegion.fromLocation(p.getLocation());

        if (!p.hasPermission("protectionstones.flags")) {
            PSL.msg(p, PSL.NO_PERMISSION_FLAGS.msg());
            return true;
        }
        if (r == null) {
            PSL.msg(p, PSL.NOT_IN_REGION.msg());
            return true;
        }
        if (WGUtils.hasNoAccess(r.getWGRegion(), p, WorldGuardPlugin.inst().wrapPlayer(p), false)) {
            PSL.msg(p, PSL.NO_ACCESS.msg());
            return true;
        }

        if (args.length < 3) {
            PSL.msg(p, PSL.FLAG_HELP.msg());
        } else {
            if (r.getTypeOptions().allowedFlags.contains((args[1].equals("-g") ? args[3].toLowerCase() : args[1].toLowerCase()))) {
                String flag, value = "", gee = "";
                if (args[1].equalsIgnoreCase("-g")) {
                    if (args.length < 5) {
                        PSL.msg(p, PSL.FLAG_HELP.msg());
                        return true;
                    }
                    flag = args[3];
                    for (int i = 4; i < args.length; i++) value += args[i] + " ";
                    gee = args[2];
                } else {
                    flag = args[1];
                    for (int i = 2; i < args.length; i++) value += args[i] + " ";
                }
                setFlag(r, p, flag, value.trim(), gee);
            } else {
                PSL.msg(p, PSL.NO_PERMISSION_PER_FLAG.msg());
            }
        }
        return true;
    }

    // tab completion
    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            PSRegion r = PSRegion.fromLocation(p.getLocation());
            if (r == null) return null;

            List<String> keywords = new ArrayList<>();
            if (args.length == 2) { // -g, or allowed flag names
                keywords.add("-g");
                keywords.addAll(r.getTypeOptions().allowedFlags);
                return StringUtil.copyPartialMatches(args[1], keywords, new ArrayList<>());
            } else if (args.length == 3 && args[1].equals("-g")) { // -g options
                keywords.addAll(Arrays.asList("all", "members", "owners", "nonmembers", "nonowners"));

                return StringUtil.copyPartialMatches(args[2], keywords, new ArrayList<>());
            } else if (args.length == 3) { // flag options
                keywords.addAll(Arrays.asList("null", "default"));

                Flag<?> f = Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), args[1]);
                if (f instanceof StateFlag) {
                    keywords.addAll(Arrays.asList("allow", "deny"));
                } else if (f instanceof BooleanFlag) {
                    keywords.addAll(Arrays.asList("true", "false"));
                }

                return StringUtil.copyPartialMatches(args[2], keywords, new ArrayList<>());
            } else if (args.length == 4 && args[1].equals("-g")) { // -g option flag name

                keywords.addAll(r.getTypeOptions().allowedFlags);
                return StringUtil.copyPartialMatches(args[3], keywords, new ArrayList<>());

            } else if (args.length == 5 && args[1].equals("-g")) { // -g option flag arg
                keywords.addAll(Arrays.asList("null", "default"));

                Flag<?> f = Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), args[3]);
                if (f instanceof StateFlag) {
                    keywords.addAll(Arrays.asList("allow", "deny"));
                } else if (f instanceof BooleanFlag) {
                    keywords.addAll(Arrays.asList("true", "false"));
                }

                return StringUtil.copyPartialMatches(args[4], keywords, new ArrayList<>());
            }
        }
        return null;
    }

    // /ps flag logic (utilizing WG internal /region flag logic)
    static void setFlag(PSRegion r, CommandSender p, String flagName, String value, String groupValue) {
        Flag flag = Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), flagName);

        ProtectedRegion region = r.getWGRegion();

        try {
            if (value.equalsIgnoreCase("default")) {

                HashMap<Flag<?>, Object> flags = new HashMap<>(r.getTypeOptions().regionFlags);
                FlagHandler.initDefaultFlagPlaceholders(flags, (Player) p);
                if (flags.get(flag) != null) {
                    region.setFlag(flag, flags.get(flag));
                } else {
                    region.setFlag(flag, flag.getDefault());
                }
                region.setFlag(flag.getRegionGroupFlag(), null);
                PSL.msg(p, PSL.FLAG_SET.msg().replace("%flag%", flagName));
            } else if (value.equalsIgnoreCase("null")) {

                region.setFlag(flag, null);
                region.setFlag(flag.getRegionGroupFlag(), null);
                PSL.msg(p, PSL.FLAG_SET.msg().replace("%flag%", flagName));

            } else {
                FlagContext fc = FlagContext.create().setInput(value).build();
                region.setFlag(flag, flag.parseInput(fc));
                if (!groupValue.equals("")) {
                    region.setFlag(flag.getRegionGroupFlag(), flag.getRegionGroupFlag().detectValue(groupValue));
                }
                PSL.msg(p, PSL.FLAG_SET.msg().replace("%flag%", flagName));
            }

        } catch (InvalidFlagFormat invalidFlagFormat) {
            //invalidFlagFormat.printStackTrace();
            PSL.msg(p, PSL.FLAG_NOT_SET.msg().replace("%flag%", flagName));
        }
    }

}
