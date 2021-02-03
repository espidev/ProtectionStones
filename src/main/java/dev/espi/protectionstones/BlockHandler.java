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

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.commands.ArgMerge;
import dev.espi.protectionstones.event.PSCreateEvent;
import dev.espi.protectionstones.utils.*;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class BlockHandler {
    
    private static final HashMap<Player, Double> lastProtectStonePlaced = new HashMap<>();

    private static String checkCooldown(Player player) {
        double currentTime = System.currentTimeMillis();
        Double lastPlace = lastProtectStonePlaced.get(player);

        if (lastPlace == null) {
            lastProtectStonePlaced.put(player, currentTime);
            return null;
        }

        double cooldown = ProtectionStones.getInstance().getConfigOptions().placingCooldown; // seconds

        if (lastPlace + cooldown * 1000 > currentTime) { // if cooldown has not been finished
            return String.format("%.1f", cooldown - ((currentTime - lastPlace) / 1000));
        }

        return null;
    }

    private static boolean isFarEnoughFromOtherClaims(PSProtectBlock blockOptions, World world, LocalPlayer localPlayer, double bx, double by, double bz) {
        BlockVector3 min = WGUtils.getMinVector(bx, by, bz, blockOptions.distanceBetweenClaims, blockOptions.distanceBetweenClaims, blockOptions.distanceBetweenClaims);
        BlockVector3 max = WGUtils.getMaxVector(bx, by, bz, blockOptions.distanceBetweenClaims, blockOptions.distanceBetweenClaims, blockOptions.distanceBetweenClaims);

        ProtectedRegion td = new ProtectedCuboidRegion("regionRadiusTest" + (long) (bx + by + bz), true, min, max);
        td.setPriority(blockOptions.priority);
        return !WGUtils.overlapsStrongerRegion(world, td, localPlayer);
    }

    // create PS region from a block place event
    public static void createPSRegion(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // check if the block is a protection stone
        if (!ProtectionStones.isProtectBlockType(block)) return;
        PSProtectBlock blockOptions = ProtectionStones.getBlockOptions(block);

        // check if the item was created by protection stones (stored in custom tag)
        // block must have restrictObtaining enabled for blocking place
        if (blockOptions.restrictObtaining && !ProtectionStones.isProtectBlockItem(event.getItemInHand(), true)) return;

        // check if player has toggled off placement of protection stones
        if (ProtectionStones.toggleList.contains(player.getUniqueId())) return;

        // check if player can place block in that area
        if (!WorldGuardPlugin.inst().createProtectionQuery().testBlockPlace(player, block.getLocation(), block.getType())) {
            PSL.msg(player, PSL.CANT_PROTECT_THAT.msg());
            event.setCancelled(true);
            return;
        }

        // check if it is in a WorldGuard region
        RegionManager rgm = WGUtils.getRegionManagerWithPlayer(player);
        if (!blockOptions.allowPlacingInWild && rgm.getApplicableRegions(BlockVector3.at(block.getLocation().getX(), block.getLocation().getY(), block.getLocation().getZ())).size() == 0) {
            PSL.msg(player, PSL.MUST_BE_PLACED_IN_EXISTING_REGION.msg());
            event.setCancelled(true);
            return;
        }

        // create region, and cancel if it fails
        if (!createPSRegion(player, block.getLocation(), blockOptions)) {
            event.setCancelled(true);
        }
    }

    // create a PS region (no checks for items)
    public static boolean createPSRegion(Player player, Location location, PSProtectBlock blockOptions) {
        // check permission
        if (!player.hasPermission(Permissions.CREATE) || (!blockOptions.permission.isEmpty() && !player.hasPermission(blockOptions.permission))) {
            PSL.msg(player, PSL.NO_PERMISSION_CREATE.msg());
            return false;
        }

        // check cooldown
        if (ProtectionStones.getInstance().getConfigOptions().placingCooldown != -1) {
            String time = checkCooldown(player);
            if (time != null) {
                PSL.msg(player, PSL.COOLDOWN.msg().replace("%time%", time));
                return false;
            }
        }

        // check if player reached region limit
        if (!LimitUtil.check(player, blockOptions)) {
            return false;
        }

        // non-admin checks
        if (!player.hasPermission(Permissions.ADMIN)) {
            // check if in world blacklist or not in world whitelist
            boolean containsWorld = blockOptions.worlds.contains(player.getLocation().getWorld().getName());

            if ((containsWorld && blockOptions.worldListType.equalsIgnoreCase("blacklist")) || (!containsWorld && blockOptions.worldListType.equalsIgnoreCase("whitelist"))) {
                if (blockOptions.preventBlockPlaceInRestrictedWorld) {
                    PSL.msg(player, PSL.WORLD_DENIED_CREATE.msg());
                    return false;
                } else {
                    return true;
                }
            }

        } // end of non-admin checks

        // check if player has enough money
        if (ProtectionStones.getInstance().isVaultSupportEnabled() && blockOptions.costToPlace != 0 && !ProtectionStones.getInstance().getVaultEconomy().has(player, blockOptions.costToPlace)) {
            PSL.msg(player, PSL.NOT_ENOUGH_MONEY.msg().replace("%price%", String.format("%.2f", blockOptions.costToPlace)));
            return false;
        }

        // debug message
        if (!ProtectionStones.getInstance().isVaultSupportEnabled() && blockOptions.costToPlace != 0) {
            ProtectionStones.getPluginLogger().info("Vault is not enabled but there is a price set on the protection stone placement! It will not work!");
        }

        if (createActualRegion(player, location, blockOptions)) { // region creation successful

            // take money
            if (ProtectionStones.getInstance().isVaultSupportEnabled() && blockOptions.costToPlace != 0) {
                EconomyResponse er = ProtectionStones.getInstance().getVaultEconomy().withdrawPlayer(player, blockOptions.costToPlace);
                if (!er.transactionSuccess()) {
                    PSL.msg(player, er.errorMessage);
                    return true;
                }
                PSL.msg(player, PSL.PAID_MONEY.msg().replace("%price%", String.format("%.2f", blockOptions.costToPlace)));
            }

            return true;
        }

        // region creation failed
        return false;
    }

    // create the actual WG region for PS region
    public static boolean createActualRegion(Player player, Location location, PSProtectBlock blockOptions) {
        // create region
        double bx = location.getX(), bxo = blockOptions.xOffset;
        double by = location.getY(), bxy = blockOptions.yOffset;
        double bz = location.getZ(), bxz = blockOptions.zOffset;

        RegionManager regionManager = WGUtils.getRegionManagerWithPlayer(player);
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

        String id = WGUtils.createPSID(bx, by, bz);

        // if the region's id already exists, possibly placing block where a region is hidden
        if (regionManager.hasRegion(id)) {
            PSL.msg(player, PSL.REGION_ALREADY_IN_LOCATION_IS_HIDDEN.msg());
            return false;
        }

        // check for minimum distance between claims by using fake region
        if (blockOptions.distanceBetweenClaims != -1 && !player.hasPermission(Permissions.SUPER_OWNER)) {
            if (!isFarEnoughFromOtherClaims(blockOptions, player.getWorld(), localPlayer, bx + bxo, by + bxy, bz + bxz)) {
                PSL.msg(player, PSL.REGION_TOO_CLOSE.msg().replace("%num%", "" + blockOptions.distanceBetweenClaims));
                return false;
            }
        }

        // create actual region
        BlockVector3 min = WGUtils.getMinVector(bx + bxo, by + bxy, bz + bxz, blockOptions.xRadius, blockOptions.yRadius, blockOptions.zRadius);
        BlockVector3 max = WGUtils.getMaxVector(bx + bxo, by + bxy, bz + bxz, blockOptions.xRadius, blockOptions.yRadius, blockOptions.zRadius);

        ProtectedRegion region = new ProtectedCuboidRegion(id, min, max);
        region.getOwners().addPlayer(player.getUniqueId());
        region.setPriority(blockOptions.priority);
        regionManager.addRegion(region); // added to the region manager, be careful in implementing checks

        // check if new region overlaps more powerful region
        if (!blockOptions.allowOverlapUnownedRegions && !player.hasPermission(Permissions.SUPER_OWNER) && WGUtils.overlapsStrongerRegion(player.getWorld(), region, localPlayer)) {
            regionManager.removeRegion(id);
            PSL.msg(player, PSL.REGION_OVERLAP.msg());
            return false;
        }

        // add corresponding flags to new region by cloning blockOptions default flags
        HashMap<Flag<?>, Object> flags = new HashMap<>(blockOptions.regionFlags);

        // replace greeting and farewell messages with player name
        FlagHandler.initDefaultFlagPlaceholders(flags, player);

        // set flags
        region.setFlags(flags);
        FlagHandler.initCustomFlagsForPS(region, location, blockOptions);

        // check for player's number of adjacent region groups
        if (ProtectionStones.getInstance().getConfigOptions().regionsMustBeAdjacent) {
            if (MiscUtil.getPermissionNumber(player, Permissions.ADJACENT, 1) >= 0 && !player.hasPermission(Permissions.ADMIN)) {
                HashMap<String, ArrayList<String>> adjGroups = WGUtils.getPlayerAdjacentRegionGroups(player, regionManager);

                int permNum = MiscUtil.getPermissionNumber(player, Permissions.ADJACENT, 1);
                if (adjGroups.size() > permNum && permNum != -1) {
                    PSL.msg(player, PSL.REGION_NOT_ADJACENT.msg());
                    regionManager.removeRegion(id);
                    return false;
                }
            }
        }

        // fire event and check if cancelled
        PSCreateEvent event = new PSCreateEvent(PSRegion.fromWGRegion(player.getWorld(), region), player);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            regionManager.removeRegion(id);
            return false;
        }

        PSL.msg(player, PSL.PROTECTED.msg());

        // hide block if auto hide is enabled
        if (blockOptions.autoHide) {
            PSL.msg(player, PSL.REGION_HIDDEN.msg());
            // run on next tick so placing tile entities don't complain
            Bukkit.getScheduler().runTask(ProtectionStones.getInstance(), () -> location.getBlock().setType(Material.AIR));
        }

        if (blockOptions.startWithTaxAutopay) {
            // set tax auto-pay (even if taxing is not enabled)
            region.setFlag(FlagHandler.PS_TAX_AUTOPAYER, player.getUniqueId().toString());
        }

        // show merge menu
        if (ProtectionStones.getInstance().getConfigOptions().allowMergingRegions && blockOptions.allowMerging && player.hasPermission(Permissions.MERGE)) {
            PSRegion r = PSRegion.fromWGRegion(player.getWorld(), region);
            if (r != null) playerMergeTask(player, r);
        }

        return true;
    }

    // merge behaviour after a region is created
    private static void playerMergeTask(Player player, PSRegion psRegion) {
        boolean showGUI = true;

        // auto merge to nearest region if only one exists
        if (psRegion.getTypeOptions().autoMerge) {
            PSRegion mergeTo = null;

            for (PSRegion psr : psRegion.getMergeableRegions(player)) {
                if (mergeTo == null) {
                    mergeTo = psr;
                    showGUI = false;
                } else {
                    showGUI = true;
                    break;
                }
            }

            // actually do auto merge
            if (!showGUI) {
                PSRegion finalMergeTo = mergeTo;

                Bukkit.getScheduler().runTaskAsynchronously(ProtectionStones.getInstance(), () -> {
                    try {
                        WGMerge.mergeRealRegions(player.getWorld(), psRegion.getWGRegionManager(), finalMergeTo, Arrays.asList(finalMergeTo, psRegion));
                        PSL.msg(player, PSL.MERGE_AUTO_MERGED.msg().replace("%region%", finalMergeTo.getId()));
                    } catch (WGMerge.RegionHoleException e) {
                        PSL.msg(player, PSL.NO_REGION_HOLES.msg()); // TODO github issue #120, prevent holes even if showGUI is true
                    } catch (WGMerge.RegionCannotMergeWhileRentedException ignored) {
                        // don't need to tell player that you can't merge
                    }
                });
            }
        }

        if (!showGUI) {
            return;
        }

        TextComponent[] gui = ArgMerge.getGUI(player, psRegion).toArray(new TextComponent[0]);

        if (gui.length == 0) {
            return;
        }

        player.sendMessage(""); // send empty line
        PSL.msg(player, PSL.MERGE_INTO.msg());
        PSL.msg(player, PSL.MERGE_HEADER.msg().replace("%region%", psRegion.getId()));
        player.spigot().sendMessage(gui);
        player.sendMessage(""); // send empty line
    }

}
