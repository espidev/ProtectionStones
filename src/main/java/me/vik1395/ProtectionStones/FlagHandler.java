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

package me.vik1395.ProtectionStones;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class FlagHandler {

    public static HashMap<Flag<?>, Object> defaultFlags = new HashMap<>();

    public static void initFlags() {
        defaultFlags.clear();

        for (String flagraw : ProtectionStones.flags) {
            String[] split = flagraw.split(" ");
            String settings = "";
            for (int i = 1; i < split.length; i++) settings += split[i] + " ";
            settings = settings.trim();

            Flag<?> flag = Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), split[0]);
            try {
                FlagContext fc = FlagContext.create().setInput(settings).build();
                defaultFlags.put(flag, flag.parseInput(fc));
            } catch (Exception e) {
                Bukkit.getLogger().info("Error parsing flag: " + split[0] + "\nError: ");
                e.printStackTrace();
            }
        }
    }

    public void setFlag(String[] args, ProtectedRegion region, Player p) {
        Flag flag;

        if (args[1].equalsIgnoreCase("-g")) {
            flag = Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), args[3]);
        } else {
            flag = Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), args[1]);
        }

        if (args[2].equalsIgnoreCase("default")) {
            region.setFlag(flag, flag.getDefault());
            region.setFlag(flag.getRegionGroupFlag(), null);
            p.sendMessage(PSL.FLAG_SET.msg().replace("%flag%", args[1]));
        } else {
            String settings = "";
            if (args[1].equalsIgnoreCase("-g")) {
                for (int i = 4; i < args.length; i++) settings += args[i] + " ";
            } else {
                for (int i = 2; i < args.length; i++) settings += args[i] + " ";
            }

            FlagContext fc = FlagContext.create().setInput(settings.trim()).build();
            try {
                region.setFlag(flag, flag.parseInput(fc));
                if (args[1].equalsIgnoreCase("-g")) {
                    region.setFlag(flag.getRegionGroupFlag(), flag.getRegionGroupFlag().detectValue(args[2]));
                }
            } catch (InvalidFlagFormat invalidFlagFormat) {
                invalidFlagFormat.printStackTrace();
                p.sendMessage(PSL.FLAG_NOT_SET.msg().replace("%flag%", args[1]));
                return;
            }
            p.sendMessage(PSL.FLAG_SET.msg().replace("%flag%", args[1]));
        }
    }

}