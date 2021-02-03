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
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.event.PSCreateEvent;
import dev.espi.protectionstones.event.PSRemoveEvent;
import dev.espi.protectionstones.utils.Permissions;
import dev.espi.protectionstones.utils.Strings;
import dev.espi.protectionstones.utils.UUIDCache;
import dev.espi.protectionstones.utils.WGUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.regex.Pattern;

public class ListenerClass implements Listener {

    /**
     * Placeholders for {@link this#execEvent(String, CommandSender, String, PSRegion)}
     */
    private final String[] ACTION_PLACEHOLDERS = new String[] {
            "%player%", "%world%", "%region%",
            "%block_x%", "%block_y%", "%block_z%"
    };

    private final Pattern ACTION_PATTERN = Pattern.compile(": ");
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUIDCache.removeUUID(player.getUniqueId());
        UUIDCache.removeName(player.getName());
        UUIDCache.storeUUIDNamePair(player.getUniqueId(), player.getName());

        ProtectionStones plugin = ProtectionStones.getInstance();

        // allow worldguard to resolve all UUIDs to names
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                UUIDCache.storeWGProfile(player.getUniqueId(), player.getName())
        );

        PSPlayer psp = PSPlayer.fromPlayer(player);

        // if by default, players should have protection block placement toggled off
        if (plugin.getConfigOptions().defaultProtectionBlockPlacementOff) {
            ProtectionStones.toggleList.add(player.getUniqueId());
        }

        // tax join message
        if (plugin.getConfigOptions().taxEnabled && plugin.getConfigOptions().taxMessageOnJoin) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                int amount = 0;

                for (PSRegion psRegion : psp.getTaxEligibleRegions()) {
                    for (PSRegion.TaxPayment taxPayment : psRegion.getTaxPaymentsDue()) {
                        amount += taxPayment.getAmount();
                    }
                }

                if (amount != 0) {
                    PSL.msg(psp, PSL.TAX_JOIN_MSG_PENDING_PAYMENTS.msg().replace("%money%", "" + amount));
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        BlockHandler.createPSRegion(event);
    }

    // helper method for breaking protection blocks
    private boolean playerBreakProtection(Player p, PSRegion r) {
        PSProtectBlock blockOptions = r.getTypeOptions();

        // check for destroy permission
        if (!p.hasPermission(Permissions.DESTROY)) {
            PSL.msg(p, PSL.NO_PERMISSION_DESTROY.msg());
            return false;
        }

        // check if player is owner of region
        if (!r.isOwner(p.getUniqueId()) && !p.hasPermission(Permissions.SUPER_OWNER)) {
            PSL.msg(p, PSL.NO_REGION_PERMISSION.msg());
            return false;
        }

        // cannot break region being rented (prevents splitting merged regions, and breaking as tenant owner)
        if (r.getRentStage() == PSRegion.RentStage.RENTING && !p.hasPermission(Permissions.SUPER_OWNER)) {
            PSL.msg(p, PSL.RENT_CANNOT_BREAK_WHILE_RENTING.msg());
            return false;
        }

        // return protection stone if no drop option is off
        if (blockOptions != null && !blockOptions.noDrop) {
            if (!p.getInventory().addItem(blockOptions.createItem()).isEmpty()) {
                // method will return not empty if item couldn't be added
                if (ProtectionStones.getInstance().getConfigOptions().dropItemWhenInventoryFull) {
                    PSL.msg(p, PSL.NO_ROOM_DROPPING_ON_FLOOR.msg());
                    p.getWorld().dropItem(r.getProtectBlock().getLocation(), blockOptions.createItem());
                } else {
                    PSL.msg(p, PSL.NO_ROOM_IN_INVENTORY.msg());
                    return false;
                }
            }
        }

        // check if removing the region and firing region remove event blocked it
        if (!r.deleteRegion(true, p)) {
            if (!ProtectionStones.getInstance().getConfigOptions().allowMergingHoles) { // side case if the removing creates a hole and those are prevented
                PSL.msg(p, PSL.DELETE_REGION_PREVENTED_NO_HOLES.msg());
            }
            return false;
        }

        PSL.msg(p, PSL.NO_LONGER_PROTECTED.msg());
        return true;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // shift-right click block with hand to break
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && !event.isBlockInHand()
                && event.getClickedBlock() != null && ProtectionStones.isProtectBlock(event.getClickedBlock())) {

            PSProtectBlock ppb = ProtectionStones.getBlockOptions(event.getClickedBlock());
            if (ppb.allowShiftRightBreak && event.getPlayer().isSneaking()) {
                if (playerBreakProtection(event.getPlayer(), PSRegion.fromLocation(event.getClickedBlock().getLocation()))) { // successful
                    event.getClickedBlock().setType(Material.AIR);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block pb = event.getBlock();

        PSProtectBlock blockOptions = ProtectionStones.getBlockOptions(pb);

        // check if block broken is protection stone type
        if (blockOptions == null) return;

        // check if that is actually a protection stone block (owns a region)
        if (!ProtectionStones.isProtectBlock(pb)) {
            // prevent silk touching of protection stone blocks (that aren't holding a region)
            if (blockOptions.preventSilkTouch) {
                ItemStack left = player.getInventory().getItemInMainHand();
                ItemStack right = player.getInventory().getItemInOffHand();
                if (!left.containsEnchantment(Enchantment.SILK_TOUCH) && !right.containsEnchantment(Enchantment.SILK_TOUCH)) {
                    return;
                }
                event.setDropItems(false);
            }
            return;
        }

        PSRegion r = PSRegion.fromLocation(pb.getLocation());

        // break protection
        if (playerBreakProtection(player, r)) { // successful
            event.setDropItems(false);
            event.setExpToDrop(0);
        } else { // unsuccessful
            event.setCancelled(true);
        }
    }

    /**
     * Prevent protect block item to be smelt
     * @param event event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
        Furnace furnace = (Furnace) event.getBlock().getState();
        PSProtectBlock options = ProtectionStones.getBlockOptions(event.getSource().getType().toString());

        if (options != null && !options.allowSmeltItem && ProtectionStones.isProtectBlockItem(event.getSource(), options.restrictObtaining)) {
            event.setCancelled(furnace.getCookTime() != 0);
        }
    }

    /**
     * Prevent protect block item to be smelt
     * @param event event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onFurnaceBurnItem(FurnaceBurnEvent event) {
        Furnace furnace = (Furnace) event.getBlock().getState();

        if (furnace.getInventory().getSmelting() == null) {
            return;
        }

        PSProtectBlock options = ProtectionStones.getBlockOptions(furnace.getInventory().getSmelting().getType().toString());
        event.setCancelled(options != null && !options.allowSmeltItem && ProtectionStones.isProtectBlockItem(furnace.getInventory().getSmelting(), options.restrictObtaining));
    }

    // -=-=-=- block changes to protection block related events -=-=-=-

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketEmptyEvent event) {
        Block clicked = event.getBlockClicked();
        BlockFace blockFace = event.getBlockFace();
        Block check = clicked.getWorld().getBlockAt(
                clicked.getX() + blockFace.getModX(),
                clicked.getY() + blockFace.getModY(),
                clicked.getZ() + blockFace.getModZ()
        );

        event.setCancelled(ProtectionStones.isProtectBlock(check));
        /*
        if (ProtectionStones.isProtectBlock(check)) {
            event.setCancelled(true);
            // fix for dumb head texture changing
            // Bukkit.getScheduler().runTask(ProtectionStones.getInstance(), () -> MiscUtil.setHeadType(ProtectionStones.getBlockOptions(check).type, check));
        }*/
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        event.setCancelled(ProtectionStones.isProtectBlock(event.getBlock()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(ProtectionStones.isProtectBlock(event.getBlock()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        event.setCancelled(ProtectionStones.isProtectBlock(event.getToBlock()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpongeAbsorb(SpongeAbsorbEvent event) {
        event.setCancelled(ProtectionStones.isProtectBlock(event.getBlock()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPhysicsEvent(BlockPhysicsEvent event) {
        event.setCancelled(ProtectionStones.isProtectBlock(event.getBlock()) || ProtectionStones.isProtectBlock(event.getSourceBlock()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event) {
        event.setCancelled(ProtectionStones.isProtectBlock(event.getBlock()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockDropItem(BlockDropItemEvent e) {
        // unfortunately, the below fix does not really work because Spigot only triggers for the source block, despite
        // what the documentation says: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/block/BlockDropItemEvent.html

        // we want to replace protection blocks that have their protection block broken (ex. signs, banners)
        // the block may not exist anymore, and so we have to recreate the isProtectBlock method here
        BlockState blockState = e.getBlockState();
        if (!ProtectionStones.isProtectBlockType(blockState.getType().toString())) return;

        RegionManager regionManager = WGUtils.getRegionManagerWithWorld(blockState.getWorld());
        if (regionManager == null) return;

        // check if the block is a source block
        ProtectedRegion br = regionManager.getRegion(WGUtils.createPSID(blockState.getLocation()));
        if (!ProtectionStones.isPSRegion(br) && PSMergedRegion.getMergedRegion(blockState.getLocation()) == null) return;

        PSRegion psRegion = PSRegion.fromLocation(blockState.getLocation());
        if (psRegion == null) return;

        // puts the block back
        psRegion.unhide();
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        pistonUtil(e.getBlocks(), e);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        pistonUtil(e.getBlocks(), e);
    }

    private void pistonUtil(List<Block> pushedBlocks, BlockPistonEvent e) {
        for (Block block : pushedBlocks) {
            PSProtectBlock cpb = ProtectionStones.getBlockOptions(block);

            if (cpb != null && ProtectionStones.isProtectBlock(block) && cpb.preventPistonPush) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent e) {
        explodeUtil(e.blockList(), e.getBlock().getLocation().getWorld());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        explodeUtil(e.blockList(), e.getLocation().getWorld());
    }

    private void explodeUtil(List<Block> blockList, World w) {
        // loop through exploded blocks
        for (int i = 0; i < blockList.size(); i++) {
            Block block = blockList.get(i);

            if (!ProtectionStones.isProtectBlock(block)) {
                continue;
            }

            PSProtectBlock blockOptions = ProtectionStones.getBlockOptions(block);

            // remove protection block from exploded list if prevent_explode is enabled
            blockList.remove(i);
            i--;

            // if allow explode
            if (!blockOptions.preventExplode) {
                block.setType(Material.AIR); // manually set to air
                // manually add drop
                if (!blockOptions.noDrop) {
                    block.getWorld().dropItem(block.getLocation(), blockOptions.createItem());
                }
                // remove region from worldguard if destroy_region_when_explode is enabled
                if (blockOptions.destroyRegionWhenExplode) {
                    ProtectionStones.removePSRegion(w, WGUtils.createPSID(block.getLocation()));
                }
            }
        }
    }

    // check player teleporting into region behaviour
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // we only want plugin triggered teleports, ignore natural teleportation
        if (event.getCause() == TeleportCause.ENDER_PEARL || event.getCause() == TeleportCause.CHORUS_FRUIT) return;

        if (event.getPlayer().hasPermission(Permissions.TP__BYPASS_PREVENT)) return;

        RegionManager rgm = WGUtils.getRegionManagerWithWorld(event.getTo().getWorld());

        // check if player can teleport into region (no region with preventTeleportIn = true)
        ApplicableRegionSet regions = rgm.getApplicableRegions(
               BlockVector3.at(event.getTo().getX(), event.getTo().getY(), event.getTo().getZ())
        );

        if (regions.getRegions().isEmpty()) return;

        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(event.getPlayer());
        boolean foundNoTeleport = false;

        for (ProtectedRegion region : regions) {
            String f = region.getFlag(FlagHandler.PS_BLOCK_MATERIAL);
            if (f != null && ProtectionStones.getBlockOptions(f) != null && ProtectionStones.getBlockOptions(f).preventTeleportIn)
                foundNoTeleport = true;
            if (region.getOwners().contains(localPlayer)) return;
        }

        if (foundNoTeleport) {
            PSL.msg(event.getPlayer(), PSL.REGION_CANT_TELEPORT.msg());
            event.setCancelled(true);
        }
    }

    // -=-=-=- player defined events -=-=-=-
    
    private void execEvent(String actionString, CommandSender s, String player, PSRegion region) {
        if (player == null) player = "";

        // split action_type: action
        String[] parts = ACTION_PATTERN.split(actionString, 2);
        if (parts.length == 0) return;

        String action = Strings.color(
                StringUtils.replaceEach( // Apache StringUtils#replace is better than String#replace
                        parts[1],
                        ACTION_PLACEHOLDERS,
                        createActionPlaceholdersReplacements(player, region)
                )
        );

        switch (parts[0].toLowerCase()) {
            case "player_command":
                if (s != null) Bukkit.getServer().dispatchCommand(s, action);
                break;
            case "console_command":
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), action);
                break;
            case "message":
                if (s != null) s.sendMessage(action);
                break;
            case "global_message":
                Bukkit.broadcastMessage(action);
                break;
            case "console_message":
                ProtectionStones.getPluginLogger().info(action);
                break;
        }
    }

    private String[] createActionPlaceholdersReplacements(String playerName, PSRegion region) {
        Block protection = region.getProtectBlock();

        return new String[] {
                playerName,
                region.getWorld().getName(),
                region.getName() == null ? region.getId() : region.getName() + " (" + region.getId() + ")",
                Integer.toString(protection.getX()),
                Integer.toString(protection.getY()),
                Integer.toString(protection.getZ())
        };
    }

    @EventHandler
    public void onPSCreate(PSCreateEvent event) {
        if (event.isCancelled()) return;
        if (!event.getRegion().getTypeOptions().eventsEnabled) return;

        // run on next tick (after the region is created to allow for edits to the region)
        Bukkit.getServer().getScheduler().runTask(ProtectionStones.getInstance(), () -> {
            // run custom commands (in config)
            for (String action : event.getRegion().getTypeOptions().regionCreateCommands) {
                execEvent(action, event.getPlayer(), event.getPlayer().getName(), event.getRegion());
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onPSRemove(PSRemoveEvent event) {
        if (event.getRegion().getTypeOptions() == null) return;
        if (!event.getRegion().getTypeOptions().eventsEnabled) return;

        // run custom commands (in config)
        for (String action : event.getRegion().getTypeOptions().regionDestroyCommands) {
            execEvent(action, event.getPlayer(), event.getPlayer() == null ? null : event.getPlayer().getName(), event.getRegion());
        }
    }

}
