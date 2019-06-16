package dev.espi.ProtectionStones;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.ProtectionStones.event.PSCreateEvent;
import dev.espi.ProtectionStones.utils.WGUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.HashMap;

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

    private static boolean hasPlayerPassedRegionLimit(Player p) {
        HashMap<PSProtectBlock, Integer> regionLimits = ProtectionStones.getPlayerRegionLimits(p);
        int maxPS = ProtectionStones.getPlayerGlobalRegionLimits(p);

        if (maxPS != -1 || !regionLimits.isEmpty()) { // only check if limit was found
            // count player's protection stones
            HashMap<String, Integer> regionFound = new HashMap<>();
            int total = 0;
            for (World w : Bukkit.getWorlds()) {
                RegionManager rgm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(w));
                for (ProtectedRegion r : rgm.getRegions().values()) {
                    if (ProtectionStones.isPSRegion(r)) {
                        String f = r.getFlag(FlagHandler.PS_BLOCK_MATERIAL);
                        total++;
                        int num = regionFound.containsKey(ProtectionStones.getBlockOptions(f).alias) ? regionFound.get(ProtectionStones.getBlockOptions(f).alias) + 1 : 1;
                        regionFound.put(ProtectionStones.getBlockOptions(f).alias, num);
                    }
                }
            }
            // check if player has passed region limit
            if (total >= maxPS && maxPS != 0) {
                return true;
            }

            for (PSProtectBlock ps : regionLimits.keySet()) {
                if (regionFound.containsKey(ps.type) && regionLimits.get(ps) <= regionFound.get(ps.type)) {
                    return true;
                }
            }
        }
        return false;
    }

    static void createPSRegion(BlockPlaceEvent e) {
        if (e.isCancelled()) return;
        Player p = e.getPlayer();
        Block b = e.getBlock();

        // check if the block is a protection stone
        if (!ProtectionStones.isProtectBlockType(b.getType().toString())) return;
        PSProtectBlock blockOptions = ProtectionStones.getBlockOptions(b.getType().toString());

        // check if the item was created by protection stones (stored in custom tag)
        // block must have restrictObtaining enabled for blocking place
        if (blockOptions.restrictObtaining && !ProtectionStones.isProtectBlockItem(e.getItemInHand(), true)) return;

        // check if player has toggled off placement of protection stones
        if (ProtectionStones.toggleList.contains(p.getUniqueId())) return;

        // check permission
        if (!p.hasPermission("protectionstones.create") || (!blockOptions.permission.equals("") && !p.hasPermission(blockOptions.permission))) {
            PSL.msg(p, PSL.NO_PERMISSION_CREATE.msg());
            e.setCancelled(true);
            return;
        }

        RegionManager rm = WGUtils.getRegionManagerWithPlayer(p);
        LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(p);

        // check if player can place block in that area
        if (!WorldGuardPlugin.inst().createProtectionQuery().testBlockPlace(p, b.getLocation(), b.getType())) {
            PSL.msg(p, PSL.CANT_PROTECT_THAT.msg());
            e.setCancelled(true);
            return;
        }

        // check cooldown
        if (ProtectionStones.getInstance().getConfigOptions().placingCooldown != -1) {
            String time = checkCooldown(p);
            if (time != null) {
                PSL.msg(p, PSL.COOLDOWN.msg().replace("%time%", time));
                e.setCancelled(true);
                return;
            }
        }

        // non-admin checks
        if (!p.hasPermission("protectionstones.admin")) {
            // check if player has limit on protection stones
            if (hasPlayerPassedRegionLimit(p)) {
                PSL.msg(p, PSL.REACHED_REGION_LIMIT.msg());
                e.setCancelled(true);
                return;
            }
            // check if in world blacklist or not in world whitelist
            if (blockOptions.worldListType.equalsIgnoreCase("blacklist")) {
                if (blockOptions.worlds.contains(p.getLocation().getWorld().getName())) {
                    PSL.msg(p, PSL.WORLD_DENIED_CREATE.msg());
                    e.setCancelled(true);
                    return;
                }
            } else if (blockOptions.worldListType.equalsIgnoreCase("whitelist")) {
                if (!blockOptions.worlds.contains(p.getLocation().getWorld().getName())) {
                    PSL.msg(p, PSL.WORLD_DENIED_CREATE.msg());
                    e.setCancelled(true);
                    return;
                }
            }

        } // end of non-admin checks

        // create region
        double bx = b.getLocation().getX();
        double by = b.getLocation().getY();
        double bz = b.getLocation().getZ();

        BlockVector3 min, max;
        String id = "ps" + (long) bx + "x" + (long) by + "y" + (long) bz + "z";

        // check for minimum distance between claims by using fake region
        if (blockOptions.distanceBetweenClaims != -1) {
            if (blockOptions.yRadius == -1) {
                min = BlockVector3.at(bx - blockOptions.distanceBetweenClaims, 0, bz - blockOptions.distanceBetweenClaims);
                max = BlockVector3.at(bx + blockOptions.distanceBetweenClaims, p.getWorld().getMaxHeight(), bz + blockOptions.distanceBetweenClaims);
            } else {
                min = BlockVector3.at(bx - blockOptions.distanceBetweenClaims, by - blockOptions.distanceBetweenClaims, bz - blockOptions.distanceBetweenClaims);
                max = BlockVector3.at(bx + blockOptions.distanceBetweenClaims, by + blockOptions.distanceBetweenClaims, bz + blockOptions.distanceBetweenClaims);
            }
            ProtectedRegion td = new ProtectedCuboidRegion("regionRadiusTest"+id, min, max);
            td.setPriority(blockOptions.priority);
            if (WGUtils.overlapsStrongerRegion(rm, td, lp)) {
                PSL.msg(p, PSL.REGION_TOO_CLOSE.msg().replace("%num%", ""+blockOptions.distanceBetweenClaims));
                e.setCancelled(true);
                return;
            }
            rm.removeRegion("regionRadiusTest"+id);
        }

        // create actual region
        if (blockOptions.yRadius == -1) {
            min = BlockVector3.at(bx - blockOptions.xRadius, 0, bz - blockOptions.zRadius);
            max = BlockVector3.at(bx + blockOptions.xRadius, p.getWorld().getMaxHeight(), bz + blockOptions.zRadius);
        } else {
            min = BlockVector3.at(bx - blockOptions.xRadius, by - blockOptions.yRadius, bz - blockOptions.zRadius);
            max = BlockVector3.at(bx + blockOptions.xRadius, by + blockOptions.yRadius, bz + blockOptions.zRadius);
        }

        ProtectedRegion region = new ProtectedCuboidRegion(id, min, max);
        region.getOwners().addPlayer(p.getUniqueId());
        region.setPriority(blockOptions.priority);
        rm.addRegion(region);

        // check if new region overlaps more powerful region
        if (WGUtils.overlapsStrongerRegion(rm, region, lp)) {
            rm.removeRegion(id);
            PSL.msg(p, PSL.REGION_OVERLAP.msg());
            e.setCancelled(true);
            return;
        }

        // fire event and check if cancelled
        PSCreateEvent event = new PSCreateEvent(ProtectionStones.getPSRegionFromWGRegion(p.getWorld(), region), p);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            e.setCancelled(true);
            rm.removeRegion(id);
            return;
        }

        // add corresponding flags to new region by cloning blockOptions default flags
        HashMap<Flag<?>, Object> flags = new HashMap<>(blockOptions.regionFlags);

        // replace greeting and farewell messages with player name
        FlagHandler.initDefaultFlagPlaceholders(flags, p);

        // set flags
        region.setFlags(flags);
        FlagHandler.initCustomFlagsForPS(region, b, blockOptions);

        p.sendMessage(PSL.PROTECTED.msg());

        // hide block if auto hide is enabled
        if (blockOptions.autoHide) b.setType(Material.AIR);
    }
}
