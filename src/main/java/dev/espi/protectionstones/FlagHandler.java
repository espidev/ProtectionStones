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

package dev.espi.protectionstones;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.SessionManager;
import com.sk89q.worldguard.session.handler.ExitFlag;
import dev.espi.protectionstones.flags.FarewellFlagHandler;
import dev.espi.protectionstones.flags.GreetingFlagHandler;
import dev.espi.protectionstones.utils.WGUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.*;
import java.util.stream.Collectors;

public class FlagHandler {
    public static final List<String> FLAG_GROUPS = Arrays.asList("all", "members", "owners", "nonmembers", "nonowners");

    // Custom WorldGuard Flags
    public static final Flag<String> GREET_ACTION = new StringFlag("greeting-action");
    public static final Flag<String> FAREWELL_ACTION = new StringFlag("farewell-action");

    // Custom WorldGuard Flags used by ProtectionStones
    // Added to blocks on BlockPlaceEvent Listener
    // When adding flags, you may want to add them to the hidden_flags_from_info config option list
    public static final Flag<String> PS_HOME = new StringFlag("ps-home");
    public static final Flag<String> PS_BLOCK_MATERIAL = new StringFlag("ps-block-material");
    public static final Flag<String> PS_NAME = new StringFlag("ps-name");
    public static final Flag<Set<String>> PS_MERGED_REGIONS = new SetFlag<>("ps-merged-regions", new StringFlag("ps-merged-region"));
    public static final Flag<Set<String>> PS_MERGED_REGIONS_TYPES = new SetFlag<>("ps-merged-regions-types", new StringFlag("ps-merged-region-type")); // each entry: "[psID] [type]"

    public static final Flag<String> PS_LANDLORD = new StringFlag("ps-landlord");
    public static final Flag<String> PS_TENANT = new StringFlag("ps-tenant");
    public static final Flag<String> PS_RENT_PERIOD = new StringFlag("ps-rent-period");
    public static final Flag<Double> PS_PRICE = new DoubleFlag("ps-price");
    public static final Flag<Double> PS_RENT_LAST_PAID = new DoubleFlag("ps-rent-last-paid");
    public static final Flag<Boolean> PS_FOR_SALE = new BooleanFlag("ps-for-sale");
    public static final Flag<Set<String>> PS_RENT_SETTINGS = new SetFlag<>("ps-rent-settings", new StringFlag("ps-rent-setting")); // TODO
    public static final Flag<Set<String>> PS_TAX_PAYMENTS_DUE = new SetFlag<>("ps-tax-payments-due", new StringFlag("ps-tax-payment"));
    public static final Flag<Set<String>> PS_TAX_LAST_PAYMENT_ADDED = new SetFlag<>("ps-tax-last-payment-added", new StringFlag("ps-tax-last-payment-entry"));
    public static final Flag<String> PS_TAX_AUTOPAYER = new StringFlag("ps-tax-autopayer");

    // called on initial start
    static void registerFlags() {
        FlagRegistry registry = WGUtil.getFlagRegistry();
        try {
            registry.register(PS_HOME);
            registry.register(PS_BLOCK_MATERIAL);
            registry.register(PS_NAME);
            registry.register(PS_MERGED_REGIONS);
            registry.register(PS_MERGED_REGIONS_TYPES);

            registry.register(PS_LANDLORD);
            registry.register(PS_TENANT);
            registry.register(PS_RENT_PERIOD);
            registry.register(PS_PRICE);
            registry.register(PS_RENT_LAST_PAID);
            registry.register(PS_FOR_SALE);
            registry.register(PS_RENT_SETTINGS);
            registry.register(PS_TAX_PAYMENTS_DUE);
            registry.register(PS_TAX_LAST_PAYMENT_ADDED);
            registry.register(PS_TAX_AUTOPAYER);
        } catch (FlagConflictException e) {
            Bukkit.getLogger().severe("Flag conflict found! The plugin will not work properly! Please contact the developers of the plugin.");
            e.printStackTrace();
        }

        // extra custom flag registration
        try {
            registry.register(GREET_ACTION);
            registry.register(FAREWELL_ACTION);
        } catch (FlagConflictException ignored) {
            // ignore if flag conflict
        }
    }

    static void registerHandlers() {
        SessionManager sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();
        sessionManager.registerHandler(GreetingFlagHandler.FACTORY, ExitFlag.FACTORY);
        sessionManager.registerHandler(FarewellFlagHandler.FACTORY, ExitFlag.FACTORY);
    }

    // adds flag permissions for ALL registered WorldGuard flags
    // by default, all players have access to it
    static void initializePermissions() {
        for (Flag<?> flag : WGUtil.getFlagRegistry().getAll()) {
            Bukkit.getPluginManager().addPermission(new Permission("protectionstones.flags.edit." + flag.getName(),
                    "Given to all players by default. Remove if you do not want the player to have the ability to edit this flag with /ps flag.",
                    PermissionDefault.TRUE));
        }
    }

    // Add the correct flags for the ps region
    static void initCustomFlagsForPS(ProtectedRegion region, Location l, PSProtectBlock cpb) {
        String home = l.getBlockX() + cpb.homeXOffset + " ";
        home += (l.getBlockY() + cpb.homeYOffset) + " ";
        home += (l.getBlockZ() + cpb.homeZOffset);
        region.setFlag(PS_HOME, home);

        region.setFlag(PS_BLOCK_MATERIAL, cpb.type);
    }

    public static List<String> getPlayerPlaceholderFlags() {
        return Arrays.asList("greeting", "greeting-title", "greeting-action", "farewell", "farewell-title", "farewell-action");
    }

    // Edit flags that require placeholders (variables)
    public static void initDefaultFlagPlaceholders(HashMap<Flag<?>, Object> flags, Player p) {
        for (Flag<?> f : getPlayerPlaceholderFlags().stream().map(WGUtil.getFlagRegistry()::get).collect(Collectors.toList())) {
            if (flags.get(f) != null) {
                String s = (String) flags.get(f);

                // apply placeholders
                if (ProtectionStones.getInstance().isPlaceholderAPISupportEnabled()) {
                    s = PlaceholderAPI.setPlaceholders(p, s);
                }
                flags.put(f, s.replaceAll("%player%", p.getName()));
            }
        }
    }

    // Initializes user defined default flags for block
    // also initializes allowed flags list
    static void initDefaultFlagsForBlock(PSProtectBlock b) {
        // initialize allowed flags list
        b.allowedFlags = new LinkedHashMap<>();
        for (String f : b.allowedFlagsRaw) {
            try {
                String[] spl = f.split(" ");
                if (spl[0].equals("-g")) {
                    String[] splGroups = spl[1].split(",");
                    List<String> groups = new ArrayList<>();
                    for (String g : splGroups) {
                        if (FLAG_GROUPS.contains(g)) groups.add(g);
                    }

                    b.allowedFlags.put(spl[2], groups);
                } else {
                    b.allowedFlags.put(f, FLAG_GROUPS);
                }
            } catch (Exception e) {
                ProtectionStones.getInstance().getLogger().warning("Skipping flag " + f + ". Did you configure the allowed_flags section correctly?");
                e.printStackTrace();
            }
        }

        // initialize default flags
        b.regionFlags = new HashMap<>();

        // loop through default flags
        for (String flagraw : b.flags) {
            String[] split = flagraw.split(" ");
            String settings = "", // flag settings (after flag name)
                    group = "", // -g group
                    flagName = split[0];
            boolean isEmpty = false; // whether or not it's the -e flag at beginning

            try {
                int startInd = 1;
                if (split[0].equals("-g")) { // if it's a group flag
                    group = split[1];
                    flagName = split[2];
                    startInd = 3; // ex. -g nonmembers tnt deny
                } else if (split[0].equals("-e")) { // if it's an empty flag
                    isEmpty = true;
                    flagName = split[1];
                    startInd = 2; // ex. -e deny-message
                }

                // get settings (after flag name)
                for (int i = startInd; i < split.length; i++) settings += split[i] + " ";
                settings = settings.trim();

                // if the setting is set to -e, change to empty flag
                if (settings.equals("-e")) {
                    isEmpty = true;
                }

                // determine worldguard flag object
                Flag<?> flag = Flags.fuzzyMatchFlag(WGUtil.getFlagRegistry(), flagName);
                FlagContext fc = FlagContext.create().setInput(settings).build();

                // warn if flag setting has already been set
                if (b.regionFlags.containsKey(flag)) {
                    ProtectionStones.getPluginLogger().warning(String.format("Duplicate default flags found (only one flag setting can be applied for each flag)! Overwriting the previous value set for %s with \"%s\" ...", flagName, flagraw));
                }

                // apply flag
                if (isEmpty) { // empty flag
                    b.regionFlags.put(flag, "");
                } else if (!group.equals("")) { // group flag

                    RegionGroup rGroup = flag.getRegionGroupFlag().detectValue(group);
                    if (rGroup == null) {
                        ProtectionStones.getPluginLogger().severe(String.format("Error parsing flag \"%s\", the group value is invalid!", flagraw));
                        continue;
                    }

                    b.regionFlags.put(flag, flag.parseInput(fc)); // add flag
                    b.regionFlags.put(flag.getRegionGroupFlag(), rGroup); // apply group

                } else { // normal flag
                    b.regionFlags.put(flag, flag.parseInput(fc));
                }

            } catch (Exception e) {
                ProtectionStones.getPluginLogger().warning("Error parsing flag: " + split[0] + "\nError: ");
                e.printStackTrace();
            }
        }
    }

}