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
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.*;
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
    private static final List<String> FLAG_GROUPS = FlagHandler.FLAG_GROUPS;
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

        List<String> allowedFlags = new ArrayList<>(r.getTypeOptions().allowedFlags.keySet());

        // send actual flags
        for (int i = GUI_SIZE * page; i < Math.min(allowedFlags.size(), GUI_SIZE * page + GUI_SIZE); i++) {
            if (i >= r.getTypeOptions().allowedFlags.size()) {
                PSL.msg(p, ChatColor.WHITE + "");
            } else {
                String flag = allowedFlags.get(i);
                List<String> currentFlagGroups = r.getTypeOptions().allowedFlags.get(flag);
                TextComponent flagLine = new TextComponent();

                // calculate flag command
                String suggestedCommand = "/" + ProtectionStones.getInstance().getConfigOptions().base_command + " flag ";

                // match flag
                Flag<?> f = Flags.fuzzyMatchFlag(WGUtils.getFlagRegistry(), flag);
                if (f == null) continue;
                Object fValue = r.getWGRegion().getFlag(f);

                // check current flag's set group
                String groupfValue = "all";
                if (f.getRegionGroupFlag() != null && r.getWGRegion().getFlag(f.getRegionGroupFlag()) != null) {
                    groupfValue = r.getWGRegion().getFlag(f.getRegionGroupFlag()).toString()
                            .toLowerCase().replace("_", "");
                }

                // add flag group if there is one set for the flag (for use in click commands)
                String flagGroup = "";
                if (f.getRegionGroupFlag() != null && r.getWGRegion().getFlag(f.getRegionGroupFlag()) != null) {
                    flagGroup = "-g " + groupfValue + " ";
                }

                // replace § with & to prevent "illegal characters in chat" disconnection
                if (fValue instanceof String) {
                    fValue = ((String) fValue).replace("§", "&");
                }

                // add line based on flag type
                if (f instanceof StateFlag) { // allow/deny
                    boolean isGroupValueAll = groupfValue.equalsIgnoreCase("all") || groupfValue.isEmpty();

                    TextComponent allow = new TextComponent((fValue == StateFlag.State.ALLOW ? ChatColor.WHITE : ChatColor.DARK_GRAY) + "Allow"),
                            deny = new TextComponent((fValue == StateFlag.State.DENY ? ChatColor.WHITE : ChatColor.DARK_GRAY) + "Deny");

                    allow.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(PSL.FLAG_GUI_HOVER_SET.msg()).create()));
                    deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(PSL.FLAG_GUI_HOVER_SET.msg()).create()));

                    if (fValue == StateFlag.State.ALLOW) {
                        allow.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, suggestedCommand + flagGroup + page + ":" + flag + " none"));
                        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, suggestedCommand + flagGroup + page + ":" + flag + " deny"));
                    } else if (fValue == StateFlag.State.DENY) {
                        allow.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, suggestedCommand + flagGroup + page + ":" + flag + " allow"));
                        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, suggestedCommand + flagGroup + page + ":" + flag + " none"));
                    } else {
                        allow.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, suggestedCommand + flagGroup + page + ":" + flag + " allow"));
                        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, suggestedCommand + flagGroup + page + ":" + flag + " deny"));
                    }

                    // HACK: Prevent pvp flag value from being changed to none/null, if it is set to a value with the group flag set to all
                    if (flag.equalsIgnoreCase("pvp") && isGroupValueAll) {
                        if (fValue == StateFlag.State.DENY) {
                            deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(PSL.FLAG_PREVENT_EXPLOIT_HOVER.msg()).create()));
                        } else if (fValue == StateFlag.State.ALLOW) {
                            allow.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(PSL.FLAG_PREVENT_EXPLOIT_HOVER.msg()).create()));
                        }
                    }

                    flagLine.addExtra(allow);
                    flagLine.addExtra(" ");
                    flagLine.addExtra(deny);
                    flagLine.addExtra(getDots(5));
                } else if (f instanceof BooleanFlag) { // true/false
                    TextComponent allow = new TextComponent((fValue == Boolean.TRUE ? ChatColor.WHITE : ChatColor.DARK_GRAY) + "True"),
                            deny = new TextComponent((fValue == Boolean.FALSE ? ChatColor.WHITE : ChatColor.DARK_GRAY) + "False");

                    allow.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(PSL.FLAG_GUI_HOVER_SET.msg()).create()));
                    deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(PSL.FLAG_GUI_HOVER_SET.msg()).create()));
                    if (fValue == Boolean.TRUE) {
                        allow.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, suggestedCommand + flagGroup + page + ":" + flag + " none"));
                        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, suggestedCommand + flagGroup + page + ":" + flag + " false"));
                    } else if (fValue == Boolean.FALSE) {
                        allow.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, suggestedCommand + flagGroup + page + ":" + flag + " true"));
                        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, suggestedCommand + flagGroup + page + ":" + flag + " none"));
                    } else {
                        allow.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, suggestedCommand + flagGroup + page + ":" + flag + " true"));
                        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, suggestedCommand + flagGroup + page + ":" + flag + " false"));
                    }

                    flagLine.addExtra(allow);
                    flagLine.addExtra(" ");
                    flagLine.addExtra(deny);
                    flagLine.addExtra(getDots(5));
                } else { // text
                    TextComponent edit = new TextComponent(ChatColor.DARK_GRAY + "Edit");
                    edit.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(PSL.FLAG_GUI_HOVER_SET_TEXT.msg()
                            .replace("%value%", fValue == null ? "none" : fValue.toString())).create()));
                    edit.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestedCommand + flagGroup + flag + " "));
                    flagLine.addExtra(edit);
                    flagLine.addExtra(getDots(22));
                }

                // put group it applies to
                TextComponent groupChange = new TextComponent(ChatColor.DARK_GRAY + " [ " + ChatColor.WHITE + groupfValue + ChatColor.DARK_GRAY + " ]");

                String nextGroup;
                if (currentFlagGroups.contains(groupfValue)) { // if the current flag group is an allowed flag group
                    nextGroup = currentFlagGroups.get((currentFlagGroups.indexOf(groupfValue) + 1) % currentFlagGroups.size());
                } else { // otherwise, just take the first allowed flag group
                    nextGroup = currentFlagGroups.get(0);
                }

                // set hover and click task for flag group
                BaseComponent[] hover;
                if (fValue == null) {
                    hover = new ComponentBuilder(PSL.FLAG_GUI_HOVER_CHANGE_GROUP_NULL.msg()).create();
                } else {
                    hover = new ComponentBuilder(PSL.FLAG_GUI_HOVER_CHANGE_GROUP.msg().replace("%group%", nextGroup)).create();
                }
                if (!nextGroup.equals(groupfValue)) { // only display hover message if the group is not the same
                    groupChange.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
                }
                groupChange.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, suggestedCommand + "-g " + nextGroup + " " + page + ":" + flag + " " + fValue));

                flagLine.addExtra(groupChange);
                // send message
                flagLine.addExtra(getDots(40 - REGION_GROUP_KERNING_LENGTHS[FLAG_GROUPS.indexOf(groupfValue)]) + ChatColor.AQUA + " " + flag);

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
        PSRegion r = PSRegion.fromLocationGroup(p.getLocation());

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

            LinkedHashMap<String, List<String>> allowedFlags = r.getTypeOptions().allowedFlags;

            // check if flag is allowed and its group is also allowed
            if (allowedFlags.keySet().contains(flagName) && allowedFlags.get(flagName).contains(flags.getOrDefault("-g", "all")) && p.hasPermission("protectionstones.flags.edit." + flagName)) {
                StringBuilder value = new StringBuilder();
                for (int i = 2; i < args.length; i++) value.append(args[i]).append(" ");

                setFlag(r, p, args[1], value.toString().trim(), flags.getOrDefault("-g", ""));
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
            PSRegion r = PSRegion.fromLocationGroup(p.getLocation());
            if (r == null) return null;

            List<String> keywords = new ArrayList<>();
            if (args.length == 2) { // -g, or allowed flag names
                keywords.add("-g");
                for (String f : r.getTypeOptions().allowedFlags.keySet()) { // must allow the "all" group
                    if (r.getTypeOptions().allowedFlags.get(f).contains("all")) {
                        keywords.add(f);
                    }
                }
                return StringUtil.copyPartialMatches(args[1], keywords, new ArrayList<>());
            } else if (args.length == 3 && args[1].equals("-g")) { // -g options
                keywords.addAll(FlagHandler.FLAG_GROUPS);

                return StringUtil.copyPartialMatches(args[2], keywords, new ArrayList<>());
            } else if (args.length == 3) { // flag options
                keywords.addAll(Arrays.asList("null", "default"));

                Flag<?> f = Flags.fuzzyMatchFlag(WGUtils.getFlagRegistry(), args[1]);
                if (f instanceof StateFlag) {
                    keywords.addAll(Arrays.asList("allow", "deny"));
                } else if (f instanceof BooleanFlag) {
                    keywords.addAll(Arrays.asList("true", "false"));
                }

                return StringUtil.copyPartialMatches(args[2], keywords, new ArrayList<>());
            } else if (args.length == 4 && args[1].equals("-g")) { // -g option flag
                for (String f : r.getTypeOptions().allowedFlags.keySet()) {
                    if (r.getTypeOptions().allowedFlags.get(f).contains(args[2])) { // if the flag is allowed for this group
                        keywords.add(f);
                    }
                }
                return StringUtil.copyPartialMatches(args[3], keywords, new ArrayList<>());

            } else if (args.length == 5 && args[1].equals("-g")) { // -g option flag arg
                keywords.addAll(Arrays.asList("null", "default"));

                Flag<?> f = Flags.fuzzyMatchFlag(WGUtils.getFlagRegistry(), args[3]);
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

        Flag flag = Flags.fuzzyMatchFlag(WGUtils.getFlagRegistry(), flagName);
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

                if (flag.getRegionGroupFlag() != null) {
                    region.setFlag(flag.getRegionGroupFlag(), null);
                }

                PSL.msg(p, PSL.FLAG_SET.msg().replace("%flag%", flagName));

            } else if (value.equalsIgnoreCase("null") || value.equalsIgnoreCase("none")) { // null flag (remove)

                // HACK: pvp flag should never be allowed to set null when the flag group is restricted to all, since
                // the default is that nonmembers can be killed, but members cannot.
                boolean isGroupValueAll = groupValue.equalsIgnoreCase("all") || groupValue.isEmpty();
                if (r.getTypeOptions().regionFlags.get(flag) != null && isGroupValueAll && flagName.equalsIgnoreCase("pvp")) {
                    PSL.msg(p, PSL.FLAG_PREVENT_EXPLOIT.msg());
                    return;
                }

                region.setFlag(flag, null);

                if (flag.getRegionGroupFlag() != null) {
                    region.setFlag(flag.getRegionGroupFlag(), null);
                }

                PSL.msg(p, PSL.FLAG_SET.msg().replace("%flag%", flagName));

            } else { // custom set flag using WG internal
                FlagContext fc = FlagContext.create().setInput(value).build();
                region.setFlag(flag, flag.parseInput(fc));
                if (!groupValue.equals("") && flag.getRegionGroupFlag() != null) {
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
