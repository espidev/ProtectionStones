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

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.util.profile.Profile;
import dev.espi.protectionstones.event.PSCreateEvent;
import dev.espi.protectionstones.event.PSRemoveEvent;
import dev.espi.protectionstones.utils.UUIDCache;
import dev.espi.protectionstones.utils.WGUtils;
import lombok.var;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ListenerClass implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        UUIDCache.removeUUID(e.getPlayer().getUniqueId());
        UUIDCache.removeName(e.getPlayer().getName());
        UUIDCache.storeUUIDNamePair(e.getPlayer().getUniqueId(), e.getPlayer().getName());

        // allow worldguard to resolve all UUIDs to names
        Bukkit.getScheduler().runTaskAsynchronously(ProtectionStones.getInstance(), () -> {
            WorldGuard.getInstance().getProfileCache().put(new Profile(e.getPlayer().getUniqueId(), e.getPlayer().getName()));
        });

        PSPlayer psp = PSPlayer.fromPlayer(e.getPlayer());

        // tax join message
        if (ProtectionStones.getInstance().getConfigOptions().taxEnabled && ProtectionStones.getInstance().getConfigOptions().taxMessageOnJoin) {
            Bukkit.getScheduler().runTaskAsynchronously(ProtectionStones.getInstance(), () -> {
                int amount = 0;
                for (PSRegion psr : psp.getTaxEligibleRegions()) {
                    for (var tp : psr.getTaxPaymentsDue()) {
                        amount += tp.getAmount();
                    }
                }

                if (amount != 0) {
                    PSL.msg(psp, PSL.TAX_JOIN_MSG_PENDING_PAYMENTS.msg().replace("%money%", ""+amount));
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent e) {
        BlockHandler.createPSRegion(e);
    }

    // helper method for breaking protection blocks
    private boolean playerBreakProtection(Player p, PSRegion r) {
        PSProtectBlock blockOptions = r.getTypeOptions();

        // check for destroy permission
        if (!p.hasPermission("protectionstones.destroy")) {
            PSL.msg(p, PSL.NO_PERMISSION_DESTROY.msg());
            return false;
        }

        // check if player is owner of region
        if (!r.isOwner(p.getUniqueId()) && !p.hasPermission("protectionstones.superowner") || r.getRentStage() == PSRegion.RentStage.RENTING) {
            PSL.msg(p, PSL.NO_REGION_PERMISSION.msg());
            return false;
        }

        // return protection stone if no drop option is off
        if (!blockOptions.noDrop) {
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

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.isCancelled()) return;

        // shift-right click block with hand to break
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && !e.isBlockInHand() && e.getClickedBlock() != null && ProtectionStones.isProtectBlockType(e.getClickedBlock())) {
            PSProtectBlock ppb = ProtectionStones.getBlockOptions(e.getClickedBlock());
            if (ppb.allowShiftRightBreak) {
                if (e.getPlayer().isSneaking()) {
                    if (playerBreakProtection(e.getPlayer(), PSRegion.fromLocation(e.getClickedBlock().getLocation()))) { // successful
                        e.getClickedBlock().setType(Material.AIR);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled()) return;

        Player p = e.getPlayer();
        Block pb = e.getBlock();

        PSProtectBlock blockOptions = ProtectionStones.getBlockOptions(pb);

        // check if block broken is protection stone
        if (blockOptions == null) return;

        // check if that is actually a protection stone block (owns a region)
        if (!ProtectionStones.isProtectBlock(pb)) {
            // prevent silk touching of protection stone blocks (that aren't holding a region)
            if (blockOptions.preventSilkTouch) {
                ItemStack left = p.getInventory().getItemInMainHand();
                ItemStack right = p.getInventory().getItemInOffHand();
                if (!left.containsEnchantment(Enchantment.SILK_TOUCH) && !right.containsEnchantment(Enchantment.SILK_TOUCH)) {
                    return;
                }
                e.setDropItems(false);
            }
            return;
        }

        PSRegion r = PSRegion.fromLocation(pb.getLocation());

        // break protection
        if (playerBreakProtection(p, r)) { // successful
            e.setDropItems(false);
            e.setExpToDrop(0);
        } else { // unsuccessful
            e.setCancelled(true);
        }
    }

    private void pistonUtil(List<Block> pushedBlocks, BlockPistonEvent e) {
        for (Block b : pushedBlocks) {
            PSProtectBlock cpb = ProtectionStones.getBlockOptions(b);
            if (cpb != null && ProtectionStones.isProtectBlock(b) && cpb.preventPistonPush) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPhysicsEvent(BlockPhysicsEvent e) {
        if (ProtectionStones.isProtectBlock(e.getBlock())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBucketFill(PlayerBucketEmptyEvent e) {
        Block clicked = e.getBlockClicked();
        BlockFace bf = e.getBlockFace();
        Block check = clicked.getWorld().getBlockAt(clicked.getX() + e.getBlockFace().getModX(), clicked.getY() + bf.getModY(), clicked.getZ() + e.getBlockFace().getModZ());
        if (ProtectionStones.isProtectBlock(check)) {
            e.setCancelled(true);
            // fix for dumb head texture changing
            // Bukkit.getScheduler().runTask(ProtectionStones.getInstance(), () -> MiscUtil.setHeadType(ProtectionStones.getBlockOptions(check).type, check));
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent e) {
        if (ProtectionStones.isProtectBlock(e.getToBlock())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        // loop through exploded blocks
        for (int i = 0; i < e.blockList().size(); i++) {
            Block b = e.blockList().get(i);

            if (ProtectionStones.isProtectBlock(b)) {
                String id = WGUtils.createPSID(b.getLocation());

                PSProtectBlock blockOptions = ProtectionStones.getBlockOptions(b);

                // remove block from exploded list if prevent_explode is enabled
                e.blockList().remove(i);
                i--;

                // if allow explode
                if (!blockOptions.preventExplode) {
                    b.setType(Material.AIR); // manually set to air
                    // manually add drop
                    if (!blockOptions.noDrop) {
                        b.getWorld().dropItem(b.getLocation(), blockOptions.createItem());
                    }
                    // remove region from worldguard if destroy_region_when_explode is enabled
                    if (blockOptions.destroyRegionWhenExplode) {
                        ProtectionStones.removePSRegion(e.getLocation().getWorld(), id);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        pistonUtil(e.getBlocks(), e);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        pistonUtil(e.getBlocks(), e);
    }

    @EventHandler
    public void onSpongeAbsorb(SpongeAbsorbEvent event) {
        if (ProtectionStones.isProtectBlock(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) return;
        if (event.getCause() == TeleportCause.ENDER_PEARL || event.getCause() == TeleportCause.CHORUS_FRUIT) return;

        if (event.getPlayer().hasPermission("protectionstones.tp.bypassprevent")) return;

        WorldGuardPlugin wg = WorldGuardPlugin.inst();
        RegionManager rgm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(event.getTo().getWorld()));
        BlockVector3 v = BlockVector3.at(event.getTo().getX(), event.getTo().getY(), event.getTo().getZ());

        // check if player can teleport into region (no region with preventTeleportIn = true)
        ApplicableRegionSet regions = rgm.getApplicableRegions(v);
        if (regions.getRegions().isEmpty()) return;
        boolean foundNoTeleport = false;
        for (ProtectedRegion r : regions) {
            String f = r.getFlag(FlagHandler.PS_BLOCK_MATERIAL);
            if (f != null && ProtectionStones.getBlockOptions(f) != null && ProtectionStones.getBlockOptions(f).preventTeleportIn)
                foundNoTeleport = true;
            if (r.getOwners().contains(wg.wrapPlayer(event.getPlayer()))) return;
        }

        if (foundNoTeleport) {
            PSL.msg(event.getPlayer(), PSL.REGION_CANT_TELEPORT.msg());
            event.setCancelled(true);
        }
    }

    private void execEvent(String action, CommandSender s, String player, String region) {
        if (player == null) player = "";

        String[] sp = action.split(": ");
        if (sp.length == 0) return;

        StringBuilder act = new StringBuilder(sp[1]);
        for (int i = 2; i < sp.length; i++) act.append(": ").append(sp[i]);

        act = new StringBuilder(act.toString().replace("%player%", player).replace("%region%", region));

        switch (sp[0]) {
            case "player_command":
                if (s != null) Bukkit.getServer().dispatchCommand(s, act.toString());
                break;
            case "console_command":
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), act.toString());
                break;
            case "message":
                if (s != null) s.sendMessage(act.toString().replace('&', '§'));
                break;
            case "global_message":
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage(act.toString().replace('&', '§'));
                }
                break;
        }
    }

    @EventHandler
    public void onPSCreate(PSCreateEvent event) {
        if (event.isCancelled()) return;
        if (!event.getRegion().getTypeOptions().eventsEnabled) return;

        // run custom commands (in config)
        for (String action : event.getRegion().getTypeOptions().regionCreateCommands) {
            execEvent(action, event.getPlayer(), event.getPlayer().getName(), event.getRegion().getName() == null ? event.getRegion().getId() : event.getRegion().getName() + "(" + event.getRegion().getId() + ")");
        }
    }

    @EventHandler
    public void onPSRemove(PSRemoveEvent event) {
        if (event.isCancelled()) return;
        if (event.getRegion().getTypeOptions() == null) return;
        if (!event.getRegion().getTypeOptions().eventsEnabled) return;

        // run custom commands (in config)
        for (String action : event.getRegion().getTypeOptions().regionDestroyCommands) {
            if (event.getPlayer() == null) {
                execEvent(action, null, null, event.getRegion().getName() == null ? event.getRegion().getId() : event.getRegion().getName() + "(" + event.getRegion().getId() + ")");
            } else {
                execEvent(action, event.getPlayer(), event.getPlayer().getName(), event.getRegion().getName() == null ? event.getRegion().getId() : event.getRegion().getName() + "(" + event.getRegion().getId() + ")");
            }
        }
    }

}
