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
import dev.espi.protectionstones.LegacyUpgrade;
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.ProtectionStones;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.*;

/*
 * To add new sub commands, add them here, and in ArgAdminHelp manually
 */

public class ArgAdmin implements PSCommandArg {

    // has to be a method, because the base command config option is not available until the plugin is loaded
    public static String getCleanupHelp() {
        return ChatColor.AQUA + "> " + ChatColor.GRAY + "/" + ProtectionStones.getInstance().getConfigOptions().base_command +
                " admin cleanup [remove|disown] [-t typealias (optional)] [days] [world (optional)]";
    }

    public static String getFlagHelp() {
        return ChatColor.AQUA + "> " + ChatColor.GRAY + "/" + ProtectionStones.getInstance().getConfigOptions().base_command +
                " admin flag [world] [flagname] [value|null|default]";
    }

    public static String getChangeBlockHelp() {
        return ChatColor.AQUA + "> " + ChatColor.GRAY + "/" + ProtectionStones.getInstance().getConfigOptions().base_command +
                " admin changeblock [world] [oldtypealias] [newtypealias]";
    }

    public static String getChangeRegionTypeHelp() {
        return ChatColor.AQUA + "> " + ChatColor.GRAY + "/" + ProtectionStones.getInstance().getConfigOptions().base_command +
                " admin changeregiontype [world] [oldtype] [newtype]";
    }

    public static String getForceMergeHelp() {
        return ChatColor.AQUA + "> " + ChatColor.GRAY + "/" + ProtectionStones.getInstance().getConfigOptions().base_command +
                " admin forcemerge [world]";
    }

    @Override
    public List<String> getNames() {
        return Collections.singletonList("admin");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return true;
    }

    @Override
    public List<String> getPermissionsToExecute() {
        return Collections.singletonList("protectionstones.admin");
    }

    @Override
    public HashMap<String, Boolean> getRegisteredFlags() {
        return null;
    }

    // /ps admin [arg]
    @Override
    public boolean executeArgument(CommandSender s, String[] args, HashMap<String, String> flags) {
        if (!s.hasPermission("protectionstones.admin")) {
            return PSL.msg(s, PSL.NO_PERMISSION_ADMIN.msg());
        }

        if (args.length < 2) {
            ArgAdminHelp.argumentAdminHelp(s, args);
            return true;
        }

        switch (args[1].toLowerCase()) {
            case "help":
                return ArgAdminHelp.argumentAdminHelp(s, args);
            case "version":
                s.sendMessage(ChatColor.AQUA + "ProtectionStones: " + ChatColor.GRAY + ProtectionStones.getInstance().getDescription().getVersion());
                s.sendMessage(ChatColor.AQUA + "Developers: " + ChatColor.GRAY + ProtectionStones.getInstance().getDescription().getAuthors());
                s.sendMessage(ChatColor.AQUA + "Bukkit:  " + ChatColor.GRAY + Bukkit.getVersion());
                s.sendMessage(ChatColor.AQUA + "WG: " + ChatColor.GRAY + WorldGuardPlugin.inst().getDescription().getVersion());
                break;
            case "hide":
                return ArgAdminHide.argumentAdminHide(s, args);
            case "unhide":
                return ArgAdminHide.argumentAdminHide(s, args);
            case "cleanup":
                return ArgAdminCleanup.argumentAdminCleanup(s, args);
            case "stats":
                return ArgAdminStats.argumentAdminStats(s, args);
            case "lastlogon":
                return ArgAdminLastlogon.argumentAdminLastLogon(s, args);
            case "lastlogons":
                return ArgAdminLastlogon.argumentAdminLastLogons(s, args);
            case "flag":
                return ArgAdminFlag.argumentAdminFlag(s, args);
            case "recreate":
                return ArgAdminRecreate.argumentAdminRecreate(s, args);
            case "changeblock":
                return ArgAdminChangeblock.argumentAdminChangeblock(s, args);
            case "changeregiontype":
                return ArgAdminChangeType.argumentAdminChangeType(s, args);
            case "forcemerge":
                return ArgAdminForceMerge.argumentAdminForceMerge(s, args);
            case "settaxautopayers":
                return ArgAdminSetTaxAutopayers.argumentAdminSetTaxAutopayers(s, args);
            case "fixregions":
                s.sendMessage(ChatColor.YELLOW + "Fixing...");
                LegacyUpgrade.upgradeRegions();
                s.sendMessage(ChatColor.YELLOW + "Done!");
                break;
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 2) {
            List<String> arg = Arrays.asList("version", "hide", "unhide", "cleanup", "stats", "lastlogon", "lastlogons", "flag", "recreate", "fixregions", "forcemerge", "changeblock", "changeregiontype", "settaxautopayers");
            return StringUtil.copyPartialMatches(args[1], arg, new ArrayList<>());
        } else if (args.length >= 3 && args[1].equals("forcemerge")) {
            return ArgAdminForceMerge.tabComplete(sender, alias, args);
        }
        return null;
    }

}
