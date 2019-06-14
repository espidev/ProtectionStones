package dev.espi.ProtectionStones;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.ProtectionStones.event.PSRemoveEvent;
import dev.espi.ProtectionStones.utils.WGUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Represents an instance of a protectionstones protected region.
 */

public class PSRegion {
    private ProtectedRegion wgregion;
    private RegionManager rgmanager;
    private World world;

    PSRegion (ProtectedRegion wgregion, RegionManager rgmanager, World world) {
        this.wgregion = wgregion;
        this.rgmanager = rgmanager;
        this.world = world;
    }

    /**
     * @return gets the world that the region is in
     */
    public World getWorld() {
        return world;
    }

    /**
     * @return whether or not the protection block is hidden (/ps hide)
     */
    public boolean isHidden() {
        return this.getProtectBlock().getType().toString().equals(this.getType());
    }

    /**
     * This method returns the block that is supposed to contain the protection block.
     * Warning: If the protection stone is hidden, this will give the block that took its place!
     * @return returns the block that may contain the protection stone
     */
    public Block getProtectBlock() {
        PSLocation psl = WGUtils.parsePSRegionToLocation(wgregion.getId());
        return world.getBlockAt(psl.x, psl.y, psl.z);
    }

    /**
     * @return returns the type
     */
    public PSProtectBlock getTypeOptions() {
        return ProtectionStones.getBlockOptions(wgregion.getFlag(FlagHandler.PS_BLOCK_MATERIAL));
    }

    /**
     * @return returns the protect block type that the region is
     */
    public String getType() {
        return wgregion.getFlag(FlagHandler.PS_BLOCK_MATERIAL);
    }

    /**
     * @return returns a list of the owners of the protected region
     */
    public ArrayList<UUID> getOwners() {
        return new ArrayList<>(wgregion.getOwners().getUniqueIds());
    }

    /**
     * @return returns a list of the members of the protected region
     */
    public ArrayList<UUID> getMembers() {
        return new ArrayList<>(wgregion.getMembers().getUniqueIds());
    }

    /**
     * Deletes the region forever. Can be cancelled by event cancellation.
     * @param deleteBlock whether or not to also set the protection block to air (if not hidden)
     * @returns whether or not the region was able to be successfully removed
     */
    public boolean deleteRegion(boolean deleteBlock) {

        PSRemoveEvent event = new PSRemoveEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) { // if event was cancelled, prevent execution
            return false;
        }

        if (deleteBlock &&!this.isHidden()) {
            this.getProtectBlock().setType(Material.AIR);
        }
        rgmanager.removeRegion(wgregion.getId());
        return true;
    }

    /**
     * @return returns the WorldGuard region object directly
     */
    public ProtectedRegion getWGRegion() {
        return wgregion;
    }

    /**
     * @return returns the WorldGuard region manager that stores this region
     */
    public RegionManager getWGRegionManager() {
        return rgmanager;
    }
}
