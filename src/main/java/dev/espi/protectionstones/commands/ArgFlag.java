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
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.utils.WGUtils;
import net.md_5.bungee.api.chat.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
    public List<String> getPermissionsToExecute() {
        return Collections.singletonList("protectionstones.flags");
    }

    @Override
    public HashMap<String, Boolean> getRegisteredFlags() {
        HashMap<String, Boolean> m = new HashMap<>();
        m.put("-g", true); // group
        return m;
    }

    private static final int GUI_SIZE = 18;
    private static final List<String> REGION_GROUPS = Arrays.asList("all", "members", "owners", "nonmembers", "nonowners");
    private static final int[] REGION_GROUP_KERNING_LENGTHS = {2, 17, 14, 26, 23};

    private String getDots(int num) {
        StringBuilder str = new StringBuilder(" " + ChatColor.DARK_GRAY);
        for (int i = 0; i < num; i++) str.append(".");
        return str.toString();
    }

    // flag gui that has ability to use pages
    private boolean openFlagGUI(Player p, PSRegion r, int page) {
        // add blank space if gui not long enough
        for (int i = 0; i < (GUI_SIZE * page + GUI_SIZE) - (Math.min(r.getTypeOptions().allowedFlags.size(), GUI_SIZE * page + GUI_SIZE) - GUI_SIZE * page); i++) {
            PSL.msg(p, ChatColor.WHITE + "");
        }

        PSL.msg(p, PSL.FLAG_GUI_HEADER.msg());

        // send actual flags
        for (int i = GUI_SIZE * page; i < Math.min(r.getTypeOptions().allowedFlags.size(), GUI_SIZE * page + GUI_SIZE); i++) {
            if (i >= r.getTypeOptions().allowedFlags.size()) {
                PSL.msg(p, ChatColor.WHITE + "");
            } else {
                String flag = r.getTypeOptions().allowedFlags.get(i);
                TextComponent flagLine = new TextComponent();

                // calculate flag command
                String suggestedCommand = "/" + ProtectionStones.getInstance().getConfigOptions().base_command + " flag ";
                Flag<?> f = Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), flag);
                // null check
                if (f == null) continue;
                Object fValue = r.getWGRegion().getFlag(f);

                // add line based on flag type
                if (f instanceof StateFlag) {
                    TextComponent allow = new TextComponent((fValue == StateFlag.State.ALLOW ? ChatColor.WHITE : ChatColor.DARK_GRAY) + "Allow"),
                            deny = new TextComponent((fValue == StateFlag.State.DENY ? ChatColor.WHITE : ChatColor.DARK_GRAY) + "Deny");

                    allow.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(PSL.FLAG_GUI_HOVER_SET.msg()).create()));
                    deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(PSL.FLAG_GUI_HOVER_SET.msg()).create()));
                    if (fValue == StateFlag.State.ALLOW) {
                        allow.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, suggestedCommand + page + ":" + flag + " none"));
                        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, suggestedCommand + page + ":" + flag + " deny"));
                    } else if (fValue == StateFlag.State.DENY) {
                        allow.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, suggestedCommand + page + ":" + flag + " allow"));
                        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, suggestedCommand + page + ":" + flag + " none"));
                    } else {
                        allow.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, suggestedCommand + page + ":" + flag + " allow"));
                        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, suggestedCommand + page + ":" + flag + " deny"));
                    }

                    flagLine.addExtra(allow);
                    flagLine.addExtra(" ");
                    flagLine.addExtra(deny);
                    flagLine.addExtra(getDots(5));
                } else if (f instanceof BooleanFlag) {
                    TextComponent allow = new TextComponent((fValue == Boolean.TRUE ? ChatColor.WHITE : ChatColor.DARK_GRAY) + "True"),
                            deny = new TextComponent((fValue == Boolean.FALSE ? ChatColor.WHITE : ChatColor.DARK_GRAY) + "False");

                    allow.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(PSL.FLAG_GUI_HOVER_SET.msg()).create()));
                    deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(PSL.FLAG_GUI_HOVER_SET.msg()).create()));
                    if (fValue == Boolean.TRUE) {
                        allow.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, suggestedCommand + page + ":" + flag + " none"));
                        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, suggestedCommand + page + ":" + flag + " false"));
                    } else if (fValue == Boolean.FALSE) {
                        allow.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, suggestedCommand + page + ":" + flag + " true"));
                        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, suggestedCommand + page + ":" + flag + " none"));
                    } else {
                        allow.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, suggestedCommand + page + ":" + flag + " true"));
                        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, suggestedCommand + page + ":" + flag + " false"));
                    }

                    flagLine.addExtra(allow);
                    flagLine.addExtra(" ");
                    flagLine.addExtra(deny);
                    flagLine.addExtra(getDots(5));
                } else {
                    TextComponent edit = new TextComponent(ChatColor.DARK_GRAY + "Edit");
                    edit.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(PSL.FLAG_GUI_HOVER_SET_TEXT.msg()
                            .replace("%value%", fValue == null ? "none" : fValue.toString())).create()));
                    edit.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestedCommand + flag + " "));
                    flagLine.addExtra(edit);
                    flagLine.addExtra(getDots(22));
                }

                // put group it applies to
                String groupfValue = r.getWGRegion().getFlag(f.getRegionGroupFlag()) == null ? "all" : r.getWGRegion().getFlag(f.getRegionGroupFlag()).toString().
                        toLowerCase().
                        replace("_", "");
                TextComponent groupChange = new TextComponent(" [ " + ChatColor.WHITE + groupfValue + ChatColor.DARK_GRAY + " ]");
                String nextGroup = REGION_GROUPS.get((REGION_GROUPS.indexOf(groupfValue) + 1) % REGION_GROUPS.size());

                BaseComponent[] hover;
                if (fValue == null) {
                    hover = new ComponentBuilder(PSL.FLAG_GUI_HOVER_CHANGE_GROUP_NULL.msg()).create();
                } else {
                    hover = new ComponentBuilder(PSL.FLAG_GUI_HOVER_CHANGE_GROUP.msg().replace("%group%", nextGroup)).create();
                }
                groupChange.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
                groupChange.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, suggestedCommand + "-g " + nextGroup + " " + page + ":" + flag + " " + fValue));

                flagLine.addExtra(groupChange);
                // send message
                flagLine.addExtra(getDots(40 - REGION_GROUP_KERNING_LENGTHS[REGION_GROUPS.indexOf(groupfValue)]) + ChatColor.AQUA + " " + flag);

                p.spigot().sendMessage(flagLine);
            }
        }

        // create footer
        TextComponent backPage = new TextComponent(ChatColor.AQUA + " <<"), nextPage = new TextComponent(ChatColor.AQUA + ">> ");
        backPage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(PSL.GO_BACK_PAGE.msg()).create()));
        nextPage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(PSL.GO_NEXT_PAGE.msg()).create()));
        backPage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + ProtectionStones.getInstance().getConfigOptions().base_command + " flag " + (page - 1)));
        nextPage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + ProtectionStones.getInstance().getConfigOptions().base_command + " flag " + (page + 1)));

        TextComponent footer = new TextComponent(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "=====" + ChatColor.RESET);
        // add back page button if the page isn't 0
        if (page != 0) footer.addExtra(backPage);
        // add page number
        footer.addExtra(new TextComponent(ChatColor.WHITE + " " + (page + 1) + " "));
        // add forward page button if the page isn't last
        if (page * GUI_SIZE + GUI_SIZE < r.getTypeOptions().allowedFlags.size()) footer.addExtra(nextPage);
        footer.addExtra(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "=====");

        p.spigot().sendMessage(footer);
        return true;
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args, HashMap<String, String> flags) {
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

        // /ps flag GUI
        if (args.length == 1) return openFlagGUI(p, r, 0);
        // go to GUI page
        if (args.length == 2 && StringUtils.isNumeric(args[1])) {
            return openFlagGUI(p, r, Integer.parseInt(args[1]));
        }

        if (args.length < 3) {
            PSL.msg(p, PSL.FLAG_HELP.msg());
            return true;
        }

        // beyond 2 args (set flag)
        try {
            String flagName = args[1].toLowerCase();
            String gui = "";
            String[] flagSplit = flagName.split(":");
            if (flagSplit.length == 2) { // check if there is a GUI that needs to be reshown
                gui = flagSplit[0];
                flagName = flagSplit[1];
            }

            if (r.getTypeOptions().allowedFlags.contains(flagName)) {
                String value = "";
                for (int i = 2; i < args.length; i++) value += args[i] + " ";
                setFlag(r, p, args[1], value.trim(), flags.getOrDefault("-g", ""));
                // reshow GUI
                if (!gui.equals("")) {
                    Bukkit.dispatchCommand(p, ProtectionStones.getInstance().getConfigOptions().base_command + " flag " + gui);
                }
            } else {
                PSL.msg(p, PSL.NO_PERMISSION_PER_FLAG.msg());
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            PSL.msg(p, PSL.FLAG_HELP.msg());
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
        // correct the flag if gui flags are there
        String[] flagSplit = flagName.split(":");
        if (flagSplit.length == 2) flagName = flagSplit[1];

        Flag flag = Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), flagName);
        ProtectedRegion region = r.getWGRegion();

        try {
            if (value.equalsIgnoreCase("default")) { // get default from config, or from WG

                HashMap<Flag<?>, Object> flags = new HashMap<>(r.getTypeOptions().regionFlags);
                FlagHandler.initDefaultFlagPlaceholders(flags, (Player) p);
                if (flags.get(flag) != null) {
                    region.setFlag(flag, flags.get(flag));
                } else {
                    region.setFlag(flag, flag.getDefault());
                }
                region.setFlag(flag.getRegionGroupFlag(), null);
                PSL.msg(p, PSL.FLAG_SET.msg().replace("%flag%", flagName));

            } else if (value.equalsIgnoreCase("null")) { // null flag (remove)

                region.setFlag(flag, null);
                region.setFlag(flag.getRegionGroupFlag(), null);
                PSL.msg(p, PSL.FLAG_SET.msg().replace("%flag%", flagName));

            } else { // custom set flag using WG internal
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
