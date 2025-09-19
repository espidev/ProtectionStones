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
import dev.espi.protectionstones.utils.upgrade.LegacyUpgrade;
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.ProtectionStones;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.*;

/*
 * To add new sub commands, add them here, and in ArgAdminHelp manually
 */

public class ArgAdmin implements PSCommandArg {

    // --- Cleanup ---
    public static Component getCleanupHelp() {
        return Component.text("> ", NamedTextColor.AQUA)
                .append(Component.text("/", NamedTextColor.GRAY))
                .append(Component.text(ProtectionStones.getInstance().getConfigOptions().base_command + " admin cleanup [remove|preview] [-t typealias (optional)] [days] [world (optional)]", NamedTextColor.GRAY));
    }

    // --- Flag ---
    public static Component getFlagHelp() {
        return Component.text("> ", NamedTextColor.AQUA)
                .append(Component.text("/", NamedTextColor.GRAY))
                .append(Component.text(ProtectionStones.getInstance().getConfigOptions().base_command + " admin flag [world] [flagname] [value|null|default]", NamedTextColor.GRAY));
    }

    // --- Change Block ---
    public static Component getChangeBlockHelp() {
        return Component.text("> ", NamedTextColor.AQUA)
                .append(Component.text("/", NamedTextColor.GRAY))
                .append(Component.text(ProtectionStones.getInstance().getConfigOptions().base_command + " admin changeblock [world] [oldtypealias] [newtypealias]", NamedTextColor.GRAY));
    }

    // --- Change Region Type ---
    public static Component getChangeRegionTypeHelp() {
        return Component.text("> ", NamedTextColor.AQUA)
                .append(Component.text("/", NamedTextColor.GRAY))
                .append(Component.text(ProtectionStones.getInstance().getConfigOptions().base_command + " admin changeregiontype [world] [oldtype] [newtype]", NamedTextColor.GRAY));
    }

    // --- Force Merge ---
    public static Component getForceMergeHelp() {
        return Component.text("> ", NamedTextColor.AQUA)
                .append(Component.text("/", NamedTextColor.GRAY))
                .append(Component.text(ProtectionStones.getInstance().getConfigOptions().base_command + " admin forcemerge [world]", NamedTextColor.GRAY));
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
                PSL.msg(s, Component.text("ProtectionStones: ", NamedTextColor.AQUA)
                        .append(Component.text(ProtectionStones.getInstance().getDescription().getVersion(), NamedTextColor.GRAY)));
                PSL.msg(s, Component.text("Developers: ", NamedTextColor.AQUA)
                        .append(Component.text(ProtectionStones.getInstance().getDescription().getAuthors().toString(), NamedTextColor.GRAY)));
                PSL.msg(s, Component.text("Bukkit: ", NamedTextColor.AQUA)
                        .append(Component.text(Bukkit.getVersion(), NamedTextColor.GRAY)));
                PSL.msg(s, Component.text("WG: ", NamedTextColor.AQUA)
                        .append(Component.text(WorldGuardPlugin.inst().getDescription().getVersion(), NamedTextColor.GRAY)));
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
                PSL.msg(s, Component.text("Fixing...", NamedTextColor.YELLOW));
                LegacyUpgrade.upgradeRegions();
                PSL.msg(s, Component.text("Done!", NamedTextColor.YELLOW));
                break;

            case "debug":
                if (ProtectionStones.getInstance().isDebug()) {
                    PSL.msg(s, Component.text("Debug mode is now off.", NamedTextColor.YELLOW));
                    ProtectionStones.getInstance().setDebug(false);
                } else {
                    PSL.msg(s, Component.text("Debug mode is now on.", NamedTextColor.YELLOW));
                    ProtectionStones.getInstance().setDebug(true);
                }
                break;
        }
        return true;
    }


    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 2) {
            List<String> arg = Arrays.asList("version", "hide", "unhide", "cleanup", "stats", "lastlogon", "lastlogons", "flag", "recreate", "fixregions", "debug", "forcemerge", "changeblock", "changeregiontype", "settaxautopayers");
            return StringUtil.copyPartialMatches(args[1], arg, new ArrayList<>());
        } else if (args.length >= 3 && args[1].equals("forcemerge")) {
            return ArgAdminForceMerge.tabComplete(sender, alias, args);
        }
        return null;
    }

}
