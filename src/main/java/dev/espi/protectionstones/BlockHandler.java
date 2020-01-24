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

package dev.espi.protectionstones;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.commands.ArgMerge;
import dev.espi.protectionstones.event.PSCreateEvent;
import dev.espi.protectionstones.utils.LimitUtil;
import dev.espi.protectionstones.utils.MiscUtil;
import dev.espi.protectionstones.utils.WGMerge;
import dev.espi.protectionstones.utils.WGUtils;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.*;

class BlockHandler {
    private static HashMap<Player, Double> lastProtectStonePlaced = new HashMap<>();

    private static String checkCooldown(Player p) {
        double currentTime = System.currentTimeMillis();
        if (lastProtectStonePlaced.containsKey(p)) {
            double cooldown = ProtectionStones.getInstance().getConfigOptions().placingCooldown; // seconds
            double lastPlace = lastProtectStonePlaced.get(p); // milliseconds

            if (lastPlace + cooldown * 1000 > currentTime) { // if cooldown has not been finished
                return String.format("%.1f", cooldown - ((currentTime - lastPlace) / 1000));
            }
            lastProtectStonePlaced.remove(p);
        }
        lastProtectStonePlaced.put(p, currentTime);
        return null;
    }

    private static boolean isFarEnoughFromOtherClaims(PSProtectBlock blockOptions, RegionManager rm, LocalPlayer lp, double bx, double by, double bz) {
        BlockVector3 min = WGUtils.getMinVector(bx, by, bz, blockOptions.distanceBetweenClaims, blockOptions.distanceBetweenClaims, blockOptions.distanceBetweenClaims);
        BlockVector3 max = WGUtils.getMaxVector(bx, by, bz, blockOptions.distanceBetweenClaims, blockOptions.distanceBetweenClaims, blockOptions.distanceBetweenClaims);

        ProtectedRegion td = new ProtectedCuboidRegion("regionRadiusTest" + (long) (bx + by + bz), true, min, max);
        td.setPriority(blockOptions.priority);
        return !WGUtils.overlapsStrongerRegion(rm, td, lp);
    }

    // create PS region from a block place event
    static void createPSRegion(BlockPlaceEvent e) {
        if (e.isCancelled()) return;
        Player p = e.getPlayer();
        Block b = e.getBlock();

        // check if the block is a protection stone
        if (!ProtectionStones.isProtectBlockType(b)) return;
        PSProtectBlock blockOptions = ProtectionStones.getBlockOptions(b);

        // check if the item was created by protection stones (stored in custom tag)
        // block must have restrictObtaining enabled for blocking place
        if (blockOptions.restrictObtaining && !ProtectionStones.isProtectBlockItem(e.getItemInHand(), true)) return;

        // check if player has toggled off placement of protection stones
        if (ProtectionStones.toggleList.contains(p.getUniqueId())) return;

        // check if player can place block in that area
        if (!WorldGuardPlugin.inst().createProtectionQuery().testBlockPlace(p, b.getLocation(), b.getType())) {
            PSL.msg(p, PSL.CANT_PROTECT_THAT.msg());
            e.setCancelled(true);
            return;
        }

        if (!createPSRegion(p, b.getLocation(), blockOptions)) {
            e.setCancelled(true);
        }
    }

    // create a PS region (no checks for items)
    static boolean createPSRegion(Player p, Location l, PSProtectBlock blockOptions) {
        // check permission
        if (!p.hasPermission("protectionstones.create") || (!blockOptions.permission.equals("") && !p.hasPermission(blockOptions.permission))) {
            PSL.msg(p, PSL.NO_PERMISSION_CREATE.msg());
            return false;
        }

        // check cooldown
        if (ProtectionStones.getInstance().getConfigOptions().placingCooldown != -1) {
            String time = checkCooldown(p);
            if (time != null) {
                PSL.msg(p, PSL.COOLDOWN.msg().replace("%time%", time));
                return false;
            }
        }

        // check if player reached region limit
        if (!LimitUtil.check(p, blockOptions)) {
            return false;
        }

        // non-admin checks
        if (!p.hasPermission("protectionstones.admin")) {
            // check if in world blacklist or not in world whitelist
            boolean containsWorld = blockOptions.worlds.contains(p.getLocation().getWorld().getName());

            if ((containsWorld && blockOptions.worldListType.equalsIgnoreCase("blacklist")) || (!containsWorld && blockOptions.worldListType.equalsIgnoreCase("whitelist"))) {
                if (blockOptions.preventBlockPlaceInRestrictedWorld) {
                    PSL.msg(p, PSL.WORLD_DENIED_CREATE.msg());
                    return false;
                } else {
                    return true;
                }
            }

        } // end of non-admin checks

        // check if player has enough money
        if (ProtectionStones.getInstance().isVaultSupportEnabled() && blockOptions.costToPlace != 0 && !ProtectionStones.getInstance().getVaultEconomy().has(p, blockOptions.costToPlace)) {
            PSL.msg(p, PSL.NOT_ENOUGH_MONEY.msg().replace("%price%", String.format("%.2f", blockOptions.costToPlace)));
            return true;
        }

        // debug message
        if (!ProtectionStones.getInstance().isVaultSupportEnabled() && blockOptions.costToPlace != 0) {
            Bukkit.getLogger().info("Vault is not enabled but there is a price set on the protection stone placement! It will not work!");
        }

        if (createActualRegion(p, l, blockOptions)) { // region creation successful

            // take money
            if (ProtectionStones.getInstance().isVaultSupportEnabled() && blockOptions.costToPlace != 0) {
                EconomyResponse er = ProtectionStones.getInstance().getVaultEconomy().withdrawPlayer(p, blockOptions.costToPlace);
                if (!er.transactionSuccess()) {
                    PSL.msg(p, er.errorMessage);
                    return true;
                }
                PSL.msg(p, PSL.PAID_MONEY.msg().replace("%price%", String.format("%.2f", blockOptions.costToPlace)));
            }

            return true;
        } else { // region creation failed
            return false;
        }
    }

    // create the actual WG region for PS region
    static boolean createActualRegion(Player p, Location l, PSProtectBlock blockOptions) {
        // create region
        double bx = l.getX(), bxo = blockOptions.xOffset;
        double by = l.getY(), bxy = blockOptions.yOffset;
        double bz = l.getZ(), bxz = blockOptions.zOffset;

        RegionManager rm = WGUtils.getRegionManagerWithPlayer(p);
        LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(p);

        String id = WGUtils.createPSID(bx, by, bz);

        // check for minimum distance between claims by using fake region
        if (blockOptions.distanceBetweenClaims != -1) {
            if (!isFarEnoughFromOtherClaims(blockOptions, rm, lp, bx + bxo, by + bxy, bz + bxz)) {
                PSL.msg(p, PSL.REGION_TOO_CLOSE.msg().replace("%num%", "" + blockOptions.distanceBetweenClaims));
                return false;
            }
        }

        // create actual region
        BlockVector3 min = WGUtils.getMinVector(bx + bxo, by + bxy, bz + bxz, blockOptions.xRadius, blockOptions.yRadius, blockOptions.zRadius);
        BlockVector3 max = WGUtils.getMaxVector(bx + bxo, by + bxy, bz + bxz, blockOptions.xRadius, blockOptions.yRadius, blockOptions.zRadius);

        ProtectedRegion region = new ProtectedCuboidRegion(id, min, max);
        region.getOwners().addPlayer(p.getUniqueId());
        region.setPriority(blockOptions.priority);
        rm.addRegion(region);

        // check if new region overlaps more powerful region
        if (!blockOptions.allowOverlapUnownedRegions && WGUtils.overlapsStrongerRegion(rm, region, lp)) {
            rm.removeRegion(id);
            PSL.msg(p, PSL.REGION_OVERLAP.msg());
            return false;
        }

        // add corresponding flags to new region by cloning blockOptions default flags
        HashMap<Flag<?>, Object> flags = new HashMap<>(blockOptions.regionFlags);

        // replace greeting and farewell messages with player name
        FlagHandler.initDefaultFlagPlaceholders(flags, p);

        // set flags
        region.setFlags(flags);
        FlagHandler.initCustomFlagsForPS(region, l, blockOptions);

        // check for player's number of adjacent region groups
        if (ProtectionStones.getInstance().getConfigOptions().regionsMustBeAdjacent) {
            if (MiscUtil.getPermissionNumber(p, "protectionstones.adjacent.", 1) >= 0 && !p.hasPermission("protectionstones.admin")) {
                HashMap<String, ArrayList<String>> adjGroups = WGUtils.getPlayerAdjacentRegionGroups(p, rm);

                int permNum = MiscUtil.getPermissionNumber(p, "protectionstones.adjacent.", 1);
                if (adjGroups.size() > permNum && permNum != -1) {
                    PSL.msg(p, PSL.REGION_NOT_ADJACENT.msg());
                    rm.removeRegion(id);
                    return false;
                }
            }
        }

        // fire event and check if cancelled
        PSCreateEvent event = new PSCreateEvent(PSRegion.fromWGRegion(p.getWorld(), region), p);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            rm.removeRegion(id);
            return false;
        }

        PSL.msg(p, PSL.PROTECTED.msg());

        // hide block if auto hide is enabled
        if (blockOptions.autoHide) {
            PSL.msg(p, PSL.REGION_HIDDEN.msg());
            // run on next tick so placing tile entities don't complain
            Bukkit.getScheduler().runTask(ProtectionStones.getInstance(), () -> l.getBlock().setType(Material.AIR));
        }

        if (blockOptions.startWithTaxAutopay) {
            // set tax auto-pay (even if taxing is not enabled)
            region.setFlag(FlagHandler.PS_TAX_AUTOPAYER, p.getUniqueId().toString());
        }

        // show merge menu
        if (ProtectionStones.getInstance().getConfigOptions().allowMergingRegions && blockOptions.allowMerging && p.hasPermission("protectionstones.merge")) {
            boolean showGUI = true;

            PSRegion r = PSRegion.fromWGRegion(p.getWorld(), region);

            // auto merge to nearest region if only one exists
            if (blockOptions.autoMerge) {
                PSRegion mergeTo = null;

                showGUI = true;
                for (ProtectedRegion pr : r.getWGRegionManager().getApplicableRegions(r.getWGRegion()).getRegions()) {
                    PSRegion psr = PSRegion.fromWGRegion(p.getWorld(), pr);
                    if (psr != null && psr.getTypeOptions().allowMerging && !pr.getId().equals(r.getID()) && (psr.isOwner(p.getUniqueId()) || p.hasPermission("protectionstones.admin"))) {
                        if (mergeTo == null) {
                            mergeTo = psr;
                            showGUI = false;
                        } else {
                            showGUI = true;
                            break;
                        }
                    }
                }

                // actually do auto merge
                if (!showGUI) {
                    PSRegion finalMergeTo = mergeTo;
                    Bukkit.getScheduler().runTaskAsynchronously(ProtectionStones.getInstance(), () -> {
                        try {
                            WGMerge.mergeRegions(p.getWorld(), rm, finalMergeTo, Arrays.asList(finalMergeTo, r));
                            PSL.msg(p, PSL.MERGE_AUTO_MERGED.msg().replace("%region%", finalMergeTo.getID()));
                        } catch (WGMerge.RegionHoleException | WGMerge.RegionCannotMergeWhileRentedException e) {
                            // don't need to tell player that you can't merge
                        }
                    });
                }
            }

            // show merge gui
            if (showGUI) {
                if (r != null) {
                    List<TextComponent> tc = ArgMerge.getGUI(p, r);
                    if (!tc.isEmpty()) { // if there are regions you can merge into
                        p.sendMessage(ChatColor.WHITE + ""); // send empty line
                        PSL.msg(p, PSL.MERGE_INTO.msg());
                        PSL.msg(p, PSL.MERGE_HEADER.msg().replace("%region%", r.getID()));
                        for (TextComponent t : tc) p.spigot().sendMessage(t);
                        p.sendMessage(ChatColor.WHITE + ""); // send empty line
                    }
                }
            }

        }

        return true;
    }
}
