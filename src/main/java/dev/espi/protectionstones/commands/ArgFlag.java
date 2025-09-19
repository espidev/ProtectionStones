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
import dev.espi.protectionstones.utils.MiscUtil;
import dev.espi.protectionstones.utils.WGUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static net.kyori.adventure.text.Component.space;

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
            final List<String> allowedFlags = new ArrayList<>(r.getTypeOptions().allowedFlags.keySet());

            // ensure the page is valid and in range
            if (page < 0 || (page * GUI_SIZE) > allowedFlags.size()) {
                PSL.msg(p, PSL.PAGE_DOES_NOT_EXIST.msg());
                return true;
            }

            // add blank space if gui not long enough
            final int rowsStart = GUI_SIZE * page;
            final int rowsEnd = Math.min(allowedFlags.size(), rowsStart + GUI_SIZE);
            final int blanks = (rowsStart + GUI_SIZE) - (rowsEnd - rowsStart);
            for (int i = 0; i < blanks; i++) {
                PSL.msg(p, Component.text(" "));
            }

            PSL.msg(p, PSL.FLAG_GUI_HEADER.msg());

            // send actual flags
            for (int i = rowsStart; i < Math.min(allowedFlags.size(), rowsStart + GUI_SIZE); i++) {
                if (i >= allowedFlags.size()) {
                    PSL.msg(p, Component.text(" "));
                    continue;
                }

                final String flagKey = allowedFlags.get(i);
                final List<String> currentFlagGroups = r.getTypeOptions().allowedFlags.get(flagKey);

                // calculate flag command
                final String suggestedCommand = "/" + ProtectionStones.getInstance().getConfigOptions().base_command + " flag ";

                // match flag
                final Flag<?> f = Flags.fuzzyMatchFlag(WGUtils.getFlagRegistry(), flagKey);
                if (f == null) continue;

                Object fValue = r.getWGRegion().getFlag(f);

                // sanitize § -> & in String flag values to avoid "illegal characters" kicks
                if (fValue instanceof String) {
                    fValue = ((String) fValue).replace("§", "&");
                }

                // current region group for this flag
                String groupfValue = "all";
                if (f.getRegionGroupFlag() != null && r.getWGRegion().getFlag(f.getRegionGroupFlag()) != null) {
                    groupfValue = r.getWGRegion().getFlag(f.getRegionGroupFlag()).toString()
                            .toLowerCase().replace("_", "");
                }

                // if a group is set, include it in click commands
                final String flagGroupArg = (f.getRegionGroupFlag() != null && r.getWGRegion().getFlag(f.getRegionGroupFlag()) != null)
                        ? "-g " + groupfValue + " "
                        : "";

                // build the line
                Component flagLine = Component.empty();

                final boolean isGroupValueAll = groupfValue.equalsIgnoreCase("all") || groupfValue.isEmpty();

                if (f instanceof StateFlag) {
                    // allow/deny widgets
                    final boolean isAllow = fValue == StateFlag.State.ALLOW;
                    final boolean isDeny = fValue == StateFlag.State.DENY;

                    Component allow = Component.text("Allow", isAllow ? NamedTextColor.WHITE : NamedTextColor.DARK_GRAY);
                    Component deny = Component.text("Deny", isDeny ? NamedTextColor.WHITE : NamedTextColor.DARK_GRAY);

                    allow = allow.hoverEvent(HoverEvent.showText(PSL.FLAG_GUI_HOVER_SET.msg()));
                    deny = deny.hoverEvent(HoverEvent.showText(PSL.FLAG_GUI_HOVER_SET.msg()));

                    if (isAllow) {
                        allow = allow.clickEvent(ClickEvent.runCommand(suggestedCommand + flagGroupArg + page + ":" + flagKey + " none"));
                        deny = deny.clickEvent(ClickEvent.runCommand(suggestedCommand + flagGroupArg + page + ":" + flagKey + " deny"));
                    } else if (isDeny) {
                        allow = allow.clickEvent(ClickEvent.runCommand(suggestedCommand + flagGroupArg + page + ":" + flagKey + " allow"));
                        deny = deny.clickEvent(ClickEvent.runCommand(suggestedCommand + flagGroupArg + page + ":" + flagKey + " none"));
                    } else {
                        allow = allow.clickEvent(ClickEvent.runCommand(suggestedCommand + flagGroupArg + page + ":" + flagKey + " allow"));
                        deny = deny.clickEvent(ClickEvent.runCommand(suggestedCommand + flagGroupArg + page + ":" + flagKey + " deny"));
                    }

                    flagLine = flagLine.append(allow)
                            .append(space())
                            .append(deny)
                            .append(dots(5));
                } else if (f instanceof BooleanFlag) {
                    // true/false widgets
                    final boolean isTrue = fValue == Boolean.TRUE;
                    final boolean isFalse = fValue == Boolean.FALSE;

                    Component t = Component.text("True", isTrue ? NamedTextColor.WHITE : NamedTextColor.DARK_GRAY);
                    Component fC = Component.text("False", isFalse ? NamedTextColor.WHITE : NamedTextColor.DARK_GRAY);

                    t = t.hoverEvent(HoverEvent.showText(PSL.FLAG_GUI_HOVER_SET.msg()));
                    fC = fC.hoverEvent(HoverEvent.showText(PSL.FLAG_GUI_HOVER_SET.msg()));

                    if (isTrue) {
                        t = t.clickEvent(ClickEvent.runCommand(suggestedCommand + flagGroupArg + page + ":" + flagKey + " none"));
                        fC = fC.clickEvent(ClickEvent.runCommand(suggestedCommand + flagGroupArg + page + ":" + flagKey + " false"));
                    } else if (isFalse) {
                        t = t.clickEvent(ClickEvent.runCommand(suggestedCommand + flagGroupArg + page + ":" + flagKey + " true"));
                        fC = fC.clickEvent(ClickEvent.runCommand(suggestedCommand + flagGroupArg + page + ":" + flagKey + " none"));
                    } else {
                        t = t.clickEvent(ClickEvent.runCommand(suggestedCommand + flagGroupArg + page + ":" + flagKey + " true"));
                        fC = fC.clickEvent(ClickEvent.runCommand(suggestedCommand + flagGroupArg + page + ":" + flagKey + " false"));
                    }

                    flagLine = flagLine.append(t)
                            .append(space())
                            .append(fC)
                            .append(dots(5));
                } else {
                    // text flag -> edit widget
                    final String currentVal = (fValue == null ? "none" : fValue.toString());
                    Component edit = Component.text("Edit", NamedTextColor.DARK_GRAY)
                            .hoverEvent(HoverEvent.showText(
                                    PSL.FLAG_GUI_HOVER_SET_TEXT.replace("%value%", currentVal)
                            ))
                            .clickEvent(ClickEvent.suggestCommand(suggestedCommand + flagGroupArg + flagKey + " "));
                    flagLine = flagLine.append(edit)
                            .append(dots(22));
                }

                // group switcher [ group ]
                Component groupChange = Component.text(" [ ", NamedTextColor.DARK_GRAY)
                        .append(Component.text(groupfValue, NamedTextColor.WHITE))
                        .append(Component.text(" ]", NamedTextColor.DARK_GRAY));

                // figure out next group
                final String nextGroup;
                if (currentFlagGroups.contains(groupfValue)) {
                    nextGroup = currentFlagGroups.get((currentFlagGroups.indexOf(groupfValue) + 1) % currentFlagGroups.size());
                } else {
                    nextGroup = currentFlagGroups.get(0);
                }

                // hover/click for group change
                // special-case pvp+all prevention
                if (flagKey.equalsIgnoreCase("pvp") && isGroupValueAll) {
                    groupChange = groupChange
                            .hoverEvent(HoverEvent.showText(PSL.FLAG_PREVENT_EXPLOIT_HOVER.msg()));
                    // no click event (disabled on purpose)
                } else {
                    if (fValue == null) {
                        groupChange = groupChange.hoverEvent(HoverEvent.showText(PSL.FLAG_GUI_HOVER_CHANGE_GROUP_NULL.msg()));
                    } else {
                        groupChange = groupChange
                                .hoverEvent(HoverEvent.showText(PSL.FLAG_GUI_HOVER_CHANGE_GROUP.replace("%group%", nextGroup)))
                                .clickEvent(ClickEvent.runCommand(suggestedCommand + "-g " + nextGroup + " " + page + ":" + flagKey + " " + (fValue == null ? "none" : fValue)));
                    }
                }

                // append group and trailing dots + flag name
                flagLine = flagLine.append(groupChange);

                // keep your kerning/dots layout
                final int kerning = 40 - REGION_GROUP_KERNING_LENGTHS[FLAG_GROUPS.indexOf(groupfValue)];
                flagLine = flagLine.append(dots(Math.max(0, kerning)))
                        .append(space())
                        .append(Component.text(flagKey, NamedTextColor.AQUA));

                // send
                ProtectionStones.getInstance().audiences().player(p).sendMessage(flagLine);
            }

            return true;
        }

    // helpers
    private static Component dots(final int n) {
        return (n <= 0) ? Component.empty() : Component.text(".".repeat(n), NamedTextColor.DARK_GRAY);
    }
    private static Component space() {
        return Component.text(" ");
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
        if (args.length == 2) {
            if (MiscUtil.isValidInteger(args[1])) {
                return openFlagGUI(p, r, Integer.parseInt(args[1]));
            }

            PSL.msg(p, PSL.FLAG_HELP.msg());
            return true;
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

                PSL.msg(p, PSL.FLAG_SET.replace("%flag%", flagName));

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

                PSL.msg(p, PSL.FLAG_SET.replace("%flag%", flagName));

            } else { // custom set flag using WG internal
                FlagContext fc = FlagContext.create().setInput(value).build();
                region.setFlag(flag, flag.parseInput(fc));
                if (!groupValue.equals("") && flag.getRegionGroupFlag() != null) {
                    region.setFlag(flag.getRegionGroupFlag(), flag.getRegionGroupFlag().detectValue(groupValue));
                }
                PSL.msg(p, PSL.FLAG_SET.replace("%flag%", flagName));
            }

        } catch (InvalidFlagFormat invalidFlagFormat) {
            //invalidFlagFormat.printStackTrace();
            PSL.msg(p, PSL.FLAG_NOT_SET.replace("%flag%", flagName));
        }
    }

}
