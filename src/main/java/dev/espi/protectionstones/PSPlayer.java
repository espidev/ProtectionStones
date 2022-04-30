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

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.utils.MiscUtil;
import dev.espi.protectionstones.utils.WGUtils;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Wrapper for a Bukkit player that exposes ProtectionStones related methods.
 */

public class PSPlayer {

    // TODO implement
    public enum PlayerRegionRelationship {
        OWNER,
        MEMBER,
        LANDLORD,
        TENANT,
        NONMEMBER,
    }

    UUID uuid;

    Player p;

    /**
     * Adapt a UUID into a PSPlayer wrapper.
     *
     * @param uuid the uuid to wrap
     * @return the PSPlayer object
     */

    public static PSPlayer fromUUID(UUID uuid) {
        return new PSPlayer(checkNotNull(uuid));
    }

    /**
     * Adapt a Bukkit player into a PSPlayer wrapper.
     *
     * @param p the player to wrap
     * @return the PSPlayer object
     */

    public static PSPlayer fromPlayer(Player p) {
        return new PSPlayer(checkNotNull(p));
    }

    public static PSPlayer fromPlayer(OfflinePlayer p) {
        if (checkNotNull(p) instanceof Player) {
            return new PSPlayer((Player) p);
        } else {
            return new PSPlayer(p.getUniqueId());
        }
    }

    public PSPlayer(Player player) {
        this.p = player;
        this.uuid = player.getUniqueId();
    }

    public PSPlayer(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Get the wrapped player's uuid.
     * @return the uuid
     */

    public UUID getUuid() {
        return this.uuid;
    }

    /**
     * Get the wrapped Bukkit player.
     * It may return null if the object wraps a UUID that does not exist.
     *
     * @return the player, or null
     */

    public Player getPlayer() {
        if (p == null) return Bukkit.getPlayer(uuid);
        return p;
    }

    /**
     * Get the wrapped Bukkit offline player.
     * Safer to use than getPlayer (this does not return a null).
     * It may return an empty player if the object wraps a UUID that does not exist.
     *
     * @return the offline player
     */

    public OfflinePlayer getOfflinePlayer() {
        if (p == null) return Bukkit.getOfflinePlayer(uuid);
        return p;
    }

    public String getName() {
        return getOfflinePlayer().getName();
    }

    /**
     * Get if the player has a certain amount of money.
     * Vault must be enabled!
     *
     * @param amount the amount to check
     * @return whether the player has this amount of money
     */

    public boolean hasAmount(double amount) {
        if (!ProtectionStones.getInstance().isVaultSupportEnabled()) return false;
        return ProtectionStones.getInstance().getVaultEconomy().has(getOfflinePlayer(), amount);
    }

    /**
     * Get the player's balance.
     * Vault must be enabled!
     *
     * @return the amount of money the player has
     */

    public double getBalance() {
        if (!ProtectionStones.getInstance().isVaultSupportEnabled()) return 0;
        return ProtectionStones.getInstance().getVaultEconomy().getBalance(getOfflinePlayer());
    }

    /**
     * Add a certain amount to the player's bank account.
     * Vault must be enabled! Must be run on main thread!
     *
     * @param amount the amount to add
     * @return the {@link EconomyResponse} that is given by Vault
     */

    public EconomyResponse depositBalance(double amount) {
        if (ProtectionStones.getInstance().getVaultEconomy() == null) return null;
        return ProtectionStones.getInstance().getVaultEconomy().depositPlayer(getOfflinePlayer(), amount);
    }

    /**
     * Withdraw a certain amount from the player's bank account.
     * Vault must be enabled! Must be run on main thread!
     *
     * @param amount the amount to withdraw
     * @return the {@link EconomyResponse} that is given by Vault
     */

    public EconomyResponse withdrawBalance(double amount) {
        if (ProtectionStones.getInstance().getVaultEconomy() == null) return null;
        return ProtectionStones.getInstance().getVaultEconomy().withdrawPlayer(getOfflinePlayer(), amount);
    }

    /**
     * Pay another player a certain amount of money.
     * Vault must be enabled! Must be run on main thread!
     *
     * @param payee  the player to pay
     * @param amount the amount to pay
     */

    public void pay(PSPlayer payee, double amount) {
        withdrawBalance(amount);
        payee.depositBalance(amount);
    }

    static class CannotAccessOfflinePlayerPermissionsException extends RuntimeException {}

    /**
     * Get a player's permission limits for each protection block (protectionstones.limit.alias.x)
     * Protection blocks that aren't specified in the player's permissions will not be returned in the map.
     * If LuckPerms support isn't enabled and the player is not online, then the method will throw a CannotAccessOfflinePlayerPermissionsException.
     *
     * @return a hashmap containing a psprotectblock object to an integer, which is the number of protection regions of that type the player is allowed to place
     */

    public HashMap<PSProtectBlock, Integer> getRegionLimits() {
        HashMap<PSProtectBlock, Integer> regionLimits = new HashMap<>();

        List<String> permissions;

        if (getPlayer() != null) {
            permissions = getPlayer().getEffectivePermissions().stream().map(PermissionAttachmentInfo::getPermission).collect(Collectors.toList());
        } else if (getOfflinePlayer().getPlayer() != null) {
            permissions = getOfflinePlayer().getPlayer().getEffectivePermissions().stream().map(PermissionAttachmentInfo::getPermission).collect(Collectors.toList());
        } else if (ProtectionStones.getInstance().isLuckPermsSupportEnabled()) {
            // use luckperms to obtain all of an offline player's permissions (vault and spigot api are unable to do this)
            try {
                permissions = MiscUtil.getLuckPermsUserPermissions(getUuid());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                throw new CannotAccessOfflinePlayerPermissionsException();
            }
        } else {
            throw new CannotAccessOfflinePlayerPermissionsException();
        }

        for (String perm : permissions) {
            if (perm.startsWith("protectionstones.limit")) {
                String[] spl = perm.split("\\.");

                if (spl.length == 4 && ProtectionStones.getProtectBlockFromAlias(spl[2]) != null) {
                    PSProtectBlock block = ProtectionStones.getProtectBlockFromAlias(spl[2]);
                    int limit = Integer.parseInt(spl[3]);
                    if (regionLimits.get(block) == null || regionLimits.get(block) < limit) { // only use max limit
                        regionLimits.put(block, limit);
                    }
                }
            }
        }
        return regionLimits;
    }

    /**
     * Get a player's total protection limit from permission (protectionstones.limit.x)
     * If there is no attached Player object to this PSPlayer, and LuckPerms is not enabled, this throws a CannotAccessOfflinePlayerPermissionsException.
     *
     * @return the number of protection regions the player can have, or -1 if there is no limit set.
     */

    public int getGlobalRegionLimits() {
        if (getPlayer() != null) {
            return MiscUtil.getPermissionNumber(getPlayer(), "protectionstones.limit.", -1);
        } else if (ProtectionStones.getInstance().isLuckPermsSupportEnabled()) {
            // use LuckPerms to obtain all of an offline player's permissions (vault and spigot api are unable to do this)
            try {
                List<String> permissions = MiscUtil.getLuckPermsUserPermissions(getUuid());
                return MiscUtil.getPermissionNumber(permissions, "protectionstones.limit.", -1);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                throw new CannotAccessOfflinePlayerPermissionsException();
            }
        } else {
            throw new CannotAccessOfflinePlayerPermissionsException();
        }
    }

    /**
     * Get the list of regions that a player can pay money for taxes to.
     * Note: this should be run asynchronously, as it can be very slow with large amounts of regions.
     *
     * @return the regions that the player owes tax money to
     */
    public List<PSRegion> getTaxEligibleRegions() {
        HashMap<World, RegionManager> m = WGUtils.getAllRegionManagers();
        List<PSRegion> ret = new ArrayList<>();

        for (World w : m.keySet()) {
            RegionManager rgm = m.get(w);
            for (ProtectedRegion r : rgm.getRegions().values()) {
                PSRegion psr = PSRegion.fromWGRegion(w, r);

                if (psr != null && psr.isOwner(getUuid()) && psr.getTypeOptions() != null && psr.getTypeOptions().taxPeriod != -1) {
                    ret.add(psr);
                }
            }
        }
        return ret;
    }

    /**
     * Get the list of regions that a player owns, or is a member of. It is recommended to run this asynchronously
     * since the query can be slow.
     *
     * @param w           world to search for regions in
     * @param canBeMember whether or not to add regions where the player is a member, not owner
     * @return list of regions that the player owns (or is a part of if canBeMember is true)
     */

    public List<PSRegion> getPSRegions(World w, boolean canBeMember) {
        RegionManager rgm = WGUtils.getRegionManagerWithWorld(w);
        if (rgm == null) return new ArrayList<>();

        return rgm.getRegions().values().stream()
                .filter(ProtectionStones::isPSRegion)
                .filter(r -> r.getOwners().contains(uuid) || (canBeMember && r.getMembers().contains(uuid)))
                .map(r -> PSRegion.fromWGRegion(w, r))
                .collect(Collectors.toList());
    }

    /**
     * Get the list of regions that a player owns, or is a member of. It is recommended to run this asynchronously
     * since the query can be slow.
     *
     * Note: Regions that the player owns that are named will be cross-world, otherwise this only searches in one world.
     *
     * @param w           world to search for regions in
     * @param canBeMember whether or not to add regions where the player is a member, not owner
     * @return list of regions that the player owns (or is a part of if canBeMember is true)
     */

    public List<PSRegion> getPSRegionsCrossWorld(World w, boolean canBeMember) {
        List<PSRegion> regions = getPSRegions(w, canBeMember);
        // set entry format: "worldName regionId"
        Set<String> regionIdAdded = regions.stream().map(r -> w.getName() + " " + r.getId()).collect(Collectors.toSet());

        // obtain cross-world named worlds
        ProtectionStones.regionNameToID.forEach((rw, rs) -> {
            World world = Bukkit.getWorld(rw);
            RegionManager rm = WGUtils.getRegionManagerWithWorld(world);
            if (rm != null) {
                rs.values().forEach(rIds -> rIds.forEach(rId -> {

                    ProtectedRegion r = rm.getRegion(rId);
                    if (r != null && r.getOwners().contains(uuid) && ProtectionStones.isPSRegion(r)) {
                        // check if it has already been added
                        String setId = world.getName() + " " + r.getId();
                        if (!world.getName().equals(w.getName()) || !regionIdAdded.contains(setId)) {
                            regions.add(PSRegion.fromWGRegion(world, r));
                            regionIdAdded.add(setId);
                        }
                    }
                }));
            }
        });

        return regions;
    }

    /**
     * Get the list of homes a player owns. It is recommended to run this asynchronously.
     *
     * Note: Regions that the player owns that are named will be cross-world, otherwise this only searches in one world.
     *
     * @param w world to search for regions in
     * @return list of regions that are the player's homes
     */

    public List<PSRegion> getHomes(World w) {
        return getPSRegionsCrossWorld(w, ProtectionStones.getInstance().getConfigOptions().allowHomeTeleportForMembers)
                .stream()
                .filter(r -> r.getTypeOptions() != null && !r.getTypeOptions().preventPsHome)
                .collect(Collectors.toList());
    }

}
