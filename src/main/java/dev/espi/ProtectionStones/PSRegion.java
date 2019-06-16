package dev.espi.ProtectionStones;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.ProtectionStones.event.PSRemoveEvent;
import dev.espi.ProtectionStones.utils.WGUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents an instance of a protectionstones protected region.
 */

public class PSRegion {
    private ProtectedRegion wgregion;
    private RegionManager rgmanager;
    private World world;

    PSRegion(ProtectedRegion wgregion, RegionManager rgmanager, World world) {
        this.wgregion = checkNotNull(wgregion);
        this.rgmanager = checkNotNull(rgmanager);
        this.world = checkNotNull(world);
    }

    /**
     * @return gets the world that the region is in
     */
    public World getWorld() {
        return world;
    }

    /**
     * Get the WorldGuard ID of the region. Note that this is not guaranteed to be unique between worlds.
     *
     * @return the id of the region
     */
    public String getID() {
        return wgregion.getId();
    }

    /**
     * Get the location of the set home the region has (for /ps tp).
     *
     * @return the location of the home, or null if the ps_home flag is not set.
     */
    public Location getHome() {
        String oPos = wgregion.getFlag(FlagHandler.PS_HOME);
        if (oPos == null) return null;
        String[] pos = oPos.split(" ");
        return new Location(world, Integer.parseInt(pos[0]), Integer.parseInt(pos[1]), Integer.parseInt(pos[2]));
    }

    /**
     * Set the home of the region (internally changes the flag).
     *
     * @param blockX block x location
     * @param blockY block y location
     * @param blockZ block z location
     */
    public void setHome(int blockX, int blockY, int blockZ) {
        wgregion.setFlag(FlagHandler.PS_HOME, blockX + " " + blockY + " " + blockZ);
    }

    /**
     * @return whether or not the protection block is hidden (/ps hide)
     */
    public boolean isHidden() {
        return !this.getProtectBlock().getType().toString().equals(this.getType());
    }

    /**
     * Hides the protection block, if it is not hidden.
     *
     * @return whether or not the block was hidden
     */
    public boolean hide() {
        if (!isHidden()) {
            getProtectBlock().setType(Material.AIR);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Unhides the protection block, if it is hidden.
     *
     * @return whether or not the block was unhidden
     */
    public boolean unhide() {
        if (isHidden()) {
            getProtectBlock().setType(Material.getMaterial(getType()));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Toggle whether or not the protection block is hidden.
     */
    public void toggleHide() {
        if (!hide()) unhide();
    }

    /**
     * This method returns the block that is supposed to contain the protection block.
     * Warning: If the protection stone is hidden, this will give the block that took its place!
     *
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
     *
     * @param deleteBlock whether or not to also set the protection block to air (if not hidden)
     * @return whether or not the region was able to be successfully removed
     */
    public boolean deleteRegion(boolean deleteBlock) {

        PSRemoveEvent event = new PSRemoveEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) { // if event was cancelled, prevent execution
            return false;
        }

        if (deleteBlock && !this.isHidden()) {
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
