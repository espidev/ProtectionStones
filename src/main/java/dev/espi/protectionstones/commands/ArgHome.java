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

import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.utils.ChatUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class ArgHome implements PSCommandArg {

    private static HashMap<UUID, List<String>> tabCache = new HashMap<>();

    @Override
    public List<String> getNames() {
        return Collections.singletonList("home");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public List<String> getPermissionsToExecute() {
        return Arrays.asList("protectionstones.home");
    }

    @Override
    public HashMap<String, Boolean> getRegisteredFlags() {
        return null;
    }

    // tab completion
    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player)) return null;
        Player p = (Player) sender;

        if (args.length == 2) {

            // add to cache if not already
            if (tabCache.get(p.getUniqueId()) == null) {
                List<PSRegion> regions = ProtectionStones.getPlayerPSRegions(p.getWorld(), p.getUniqueId(), false);
                List<String> regionNames = new ArrayList<>();
                for (PSRegion r : regions) {
                    if (r.getName() != null) regionNames.add(r.getName());
                    regionNames.add(r.getID());
                }
                // cache home regions
                tabCache.put(p.getUniqueId(), regionNames);

                Bukkit.getScheduler().runTaskLater(ProtectionStones.getInstance(), () -> {
                    tabCache.remove(p.getUniqueId());
                }, 200); // remove cache after 10 seconds
            }

            return StringUtil.copyPartialMatches(args[1], tabCache.get(p.getUniqueId()), new ArrayList<>());
        }
        return null;
    }

    private void openHomeGUI(Player p) {
        List<PSRegion> regions = ProtectionStones.getPlayerPSRegions(p.getWorld(), p.getUniqueId(), false);
        PSL.msg(p, PSL.HOME_HEADER.msg());
        for (PSRegion r : regions) {
            String msg;
            if (r.getName() == null) {
                msg = ChatColor.GRAY + "> " + ChatColor.AQUA + r.getID();
            } else {
                msg = ChatColor.GRAY + "> " + ChatColor.AQUA + r.getName() + " (" + r.getID() + ")";
            }
            TextComponent tc = new TextComponent(msg);
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(PSL.HOME_CLICK_TO_TP.msg()).create()));
            tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + ProtectionStones.getInstance().getConfigOptions().base_command + " home " + r.getID()));
            p.spigot().sendMessage(tc);
        }
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args, HashMap<String, String> flags) {
        Player p = (Player) s;

        // prelim checks
        if (!p.hasPermission("protectionstones.home")) {
            PSL.msg(p, PSL.NO_PERMISSION_HOME.msg());
            return true;
        }
        if (args.length != 2 && args.length != 1) {
            PSL.msg(p, PSL.HOME_HELP.msg());
            return true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(ProtectionStones.getInstance(), () -> {
            if (args.length == 1) { // /ps home GUI
                openHomeGUI(p);
            } else {// /ps home [id]
                // get regions from the query
                List<PSRegion> regions = ProtectionStones.getPSRegions(p.getWorld(), args[1]);

                // remove regions not owned by the player
                for (int i = 0; i < regions.size(); i++) {
                    if (!regions.get(i).isOwner(p.getUniqueId())) {
                        regions.remove(i);
                        i--;
                    }
                }

                if (regions.isEmpty()) {
                    PSL.msg(s, PSL.REGION_DOES_NOT_EXIST.msg());
                    return;
                }
                if (regions.size() > 1) {
                    ChatUtils.displayDuplicateRegionAliases(p, regions);
                    return;
                }

                ArgTp.teleportPlayer(p, regions.get(0));
            }
        });

        return true;
    }
}
