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

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.util.profile.Profile;
import dev.espi.protectionstones.event.PSCreateEvent;
import dev.espi.protectionstones.event.PSRemoveEvent;
import dev.espi.protectionstones.utils.UUIDCache;
import dev.espi.protectionstones.utils.WGUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ListenerClass implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        UUIDCache.uuidToName.remove(e.getPlayer().getUniqueId());
        UUIDCache.uuidToName.put(e.getPlayer().getUniqueId(), e.getPlayer().getName());
        UUIDCache.nameToUUID.remove(e.getPlayer().getName());
        UUIDCache.nameToUUID.put(e.getPlayer().getName(), e.getPlayer().getUniqueId());
        WorldGuard.getInstance().getProfileCache().put(new Profile(e.getPlayer().getUniqueId(), e.getPlayer().getName()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent e) {
        BlockHandler.createPSRegion(e);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled()) return;

        Player p = e.getPlayer();
        Block pb = e.getBlock();

        String blockType = pb.getType().toString();
        PSProtectBlock blockOptions = ProtectionStones.getBlockOptions(blockType);

        // check if block broken is protection stone
        if (blockOptions == null) return;

        WorldGuardPlugin wg = WorldGuardPlugin.inst();

        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager rgm = regionContainer.get(BukkitAdapter.adapt(p.getWorld()));

        String id = WGUtils.createPSID(pb.getLocation());

        // check if that is actually a protection stone block (owns a region)
        if (rgm.getRegion(id) == null) {

            // prevent silk touching of protection stone blocks (that aren't holding a region)
            if (blockOptions.preventSilkTouch) {
                ItemStack left = e.getPlayer().getInventory().getItemInMainHand();
                ItemStack right = e.getPlayer().getInventory().getItemInOffHand();
                if (!left.containsEnchantment(Enchantment.SILK_TOUCH) && !right.containsEnchantment(Enchantment.SILK_TOUCH)) {
                    return;
                }
                e.setDropItems(false);
            }
            return;
        }

        // check for destroy permission
        if (!p.hasPermission("protectionstones.destroy")) {
            PSL.msg(p, PSL.NO_PERMISSION_DESTROY.msg());
            e.setCancelled(true);
            return;
        }

        // check if player is owner of region
        if (!rgm.getRegion(id).isOwner(wg.wrapPlayer(p)) && !p.hasPermission("protectionstones.superowner")) {
            PSL.msg(p, PSL.NO_REGION_PERMISSION.msg());
            e.setCancelled(true);
            return;
        }

        // return protection stone if no drop option is off
        if (!blockOptions.noDrop) {
            if (!p.getInventory().addItem(blockOptions.createItem()).isEmpty()) {
                // method will return not empty if item couldn't be added
                if (ProtectionStones.getInstance().getConfigOptions().dropItemWhenInventoryFull) {
                    PSL.msg(p, PSL.NO_ROOM_DROPPING_ON_FLOOR.msg());
                    p.getWorld().dropItem(pb.getLocation(), blockOptions.createItem());
                } else {
                    PSL.msg(p, PSL.NO_ROOM_IN_INVENTORY.msg());
                    e.setCancelled(true);
                    return;
                }
            }
        }

        // check if removing the region and firing region remove event blocked it
        if (!ProtectionStones.removePSRegion(p.getWorld(), id, p)) {
            return;
        }

        // remove block
        pb.setType(Material.AIR);

        PSL.msg(p, PSL.NO_LONGER_PROTECTED.msg());

        e.setDropItems(false);
        e.setExpToDrop(0);
    }

    private void pistonUtil(List<Block> pushedBlocks, BlockPistonEvent e) {
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager rgm = regionContainer.get(BukkitAdapter.adapt(e.getBlock().getWorld()));
        for (Block b : pushedBlocks) {
            PSProtectBlock cpb = ProtectionStones.getBlockOptions(b.getType().toString());
            if (cpb != null && rgm.getRegion(WGUtils.createPSID(b.getLocation())) != null && cpb.preventPistonPush) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager rgm = regionContainer.get(BukkitAdapter.adapt(e.getEntity().getWorld()));

        // loop through exploded blocks
        for (int i = 0; i < e.blockList().size(); i++) {
            Block b = e.blockList().get(i);

            if (ProtectionStones.isProtectBlockType(b.getType().toString())) {
                String id = WGUtils.createPSID(b.getLocation());
                if (rgm.getRegion(id) != null) {
                    if (ProtectionStones.getBlockOptions(b.getType().toString()).preventExplode) {
                        // remove block from exploded list if prevent_explode is enabled
                        e.blockList().remove(i);
                        i--;
                    } else if (ProtectionStones.getBlockOptions(b.getType().toString()).destroyRegionWhenExplode) {
                        // remove region from worldguard if destroy_region_when_explode is enabled
                        // check if removing the region and firing region remove event blocked it
                        if (!ProtectionStones.removePSRegion(e.getLocation().getWorld(), id)) {
                            return;
                        }
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
        String id = WGUtils.createPSID(event.getBlock().getLocation());
        ProtectedRegion r = WGUtils.getRegionManagerWithWorld(event.getBlock().getWorld()).getRegion(id);
        if (ProtectionStones.isPSRegion(r)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
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

        String act = sp[1];
        for (int i = 2; i < sp.length; i++) act += ": " + sp[i];

        act = act.replace("%player%", player).replace("%region%", region);

        switch (sp[0]) {
            case "player_command":
                if (s != null) Bukkit.getServer().dispatchCommand(s, act);
                break;
            case "console_command":
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), act);
                break;
            case "message":
                if (s != null) s.sendMessage(act.replace('&', '§'));
                break;
            case "global_message":
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage(act.replace('&', '§'));
                }
                break;
        }
    }

    @EventHandler
    public void onPSCreate(PSCreateEvent event) {
        if (!event.getRegion().getTypeOptions().eventsEnabled) return;

        // run custom commands (in config)
        for (String action : event.getRegion().getTypeOptions().regionCreateCommands) {
            execEvent(action, event.getPlayer(), event.getPlayer().getName(), event.getRegion().getName() == null ? event.getRegion().getID() : event.getRegion().getName() + "(" + event.getRegion().getID() + ")");
        }
    }

    @EventHandler
    public void onPSRemove(PSRemoveEvent event) {
        if (!event.getRegion().getTypeOptions().eventsEnabled) return;

        // run custom commands (in config)
        for (String action : event.getRegion().getTypeOptions().regionDestroyCommands) {
            if (event.getPlayer() == null) {
                execEvent(action, null, null, event.getRegion().getName() == null ? event.getRegion().getID() : event.getRegion().getName() + "(" + event.getRegion().getID() + ")");
            } else {
                execEvent(action, event.getPlayer(), event.getPlayer().getName(), event.getRegion().getName() == null ? event.getRegion().getID() : event.getRegion().getName() + "(" + event.getRegion().getID() + ")");
            }
        }
    }

}
