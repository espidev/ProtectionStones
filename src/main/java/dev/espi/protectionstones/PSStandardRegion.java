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

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.RemovalStrategy;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.event.PSRemoveEvent;
import dev.espi.protectionstones.utils.MiscUtil;
import dev.espi.protectionstones.utils.Objs;
import dev.espi.protectionstones.utils.WGUtils;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents an instance of a standard PS region, that has not been merged or contains merged regions.
 */

public class PSStandardRegion extends PSRegion {
    private ProtectedRegion wgregion;

    PSStandardRegion(ProtectedRegion wgregion, RegionManager rgmanager, World world) {
        super(rgmanager, world);
        this.wgregion = checkNotNull(wgregion);
    }

    // ~~~~~~~~~~~ instance ~~~~~~~~~~~~~~~~

    @Override
    public String getId() {
        return wgregion.getId();
    }

    @Override
    public String getName() {
        return wgregion.getFlag(FlagHandler.PS_NAME);
    }

    @Override
    public void setName(String name) {
        HashMap<String, ArrayList<String>> m = ProtectionStones.regionNameToID.get(getWorld().getUID());
        if (m == null) { // if the world has not been added
            ProtectionStones.regionNameToID.put(getWorld().getUID(), new HashMap<>());
            m = ProtectionStones.regionNameToID.get(getWorld().getUID());
        }
        if (m.get(getName()) != null) {
            m.get(getName()).remove(getId());
        }
        if (name != null) {
            if (m.containsKey(name)) {
                m.get(name).add(getId());
            } else {
                m.put(name, new ArrayList<>(Collections.singletonList(getId())));
            }
        }
        wgregion.setFlag(FlagHandler.PS_NAME, name);
    }

    @Override
    public void setParent(PSRegion r) throws ProtectedRegion.CircularInheritanceException {
        wgregion.setParent(r == null ? null : r.getWGRegion());
    }

    @Override
    public PSRegion getParent() {
        return wgregion.getParent() == null ? null : fromWGRegion(world, wgregion.getParent());
    }

    @Override
    public Location getHome() {
        String oPos = wgregion.getFlag(FlagHandler.PS_HOME);
        if (oPos == null) return null;
        String[] pos = oPos.split(" ");
        double x = Double.parseDouble(pos[0]), y = Double.parseDouble(pos[1]), z = Double.parseDouble(pos[2]);
        float yaw = pos.length >= 4 ? Float.parseFloat(pos[3]) : 0, pitch = pos.length >= 4 ? Float.parseFloat(pos[4]) : 0;
        return new Location(world, x, y, z, yaw, pitch);
    }

    @Override
    public void setHome(double blockX, double blockY, double blockZ) {
        wgregion.setFlag(FlagHandler.PS_HOME, blockX + " " + blockY + " " + blockZ);
    }

    @Override
    public void setHome(double blockX, double blockY, double blockZ, float yaw, float pitch) {
        wgregion.setFlag(FlagHandler.PS_HOME, blockX + " " + blockY + " " + blockZ + " " + yaw + " " + pitch);
    }

    @Override
    public boolean forSale() {
        return wgregion.getFlag(FlagHandler.PS_FOR_SALE) != null && wgregion.getFlag(FlagHandler.PS_FOR_SALE);
    }

    @Override
    public void setSellable(boolean forSale, UUID landlord, double price) {
        if (!forSale) {
            wgregion.setFlag(FlagHandler.PS_LANDLORD, null);
            wgregion.setFlag(FlagHandler.PS_PRICE, null);
            wgregion.setFlag(FlagHandler.PS_FOR_SALE, null);
        } else {
            wgregion.setFlag(FlagHandler.PS_LANDLORD, landlord.toString());
            wgregion.setFlag(FlagHandler.PS_PRICE, price);
            wgregion.setFlag(FlagHandler.PS_FOR_SALE, true);
        }
    }

    @Override
    public void sell(UUID player) {
        PSPlayer.fromUUID(player).pay(PSPlayer.fromUUID(getLandlord()), getPrice());
        setSellable(false, null, 0);
        getWGRegion().getOwners().removeAll();
        getWGRegion().getMembers().removeAll();
        addOwner(player);
    }

    @Override
    public RentStage getRentStage() {
        if (getLandlord() == null && getTenant() == null) {
            return RentStage.NOT_RENTING;
        } else if (getTenant() == null && !forSale()) {
            return RentStage.LOOKING_FOR_TENANT;
        } else if (getPrice() != null && !forSale()) {
            return RentStage.RENTING;
        }
        return RentStage.NOT_RENTING;
    }

    @Override
    public UUID getLandlord() {
        return wgregion.getFlag(FlagHandler.PS_LANDLORD) == null ? null : UUID.fromString(wgregion.getFlag(FlagHandler.PS_LANDLORD));
    }

    @Override
    public void setLandlord(UUID landlord) {
        wgregion.setFlag(FlagHandler.PS_LANDLORD, landlord == null ? null : landlord.toString());
    }

    @Override
    public UUID getTenant() {
        return wgregion.getFlag(FlagHandler.PS_TENANT) == null ? null : UUID.fromString(wgregion.getFlag(FlagHandler.PS_TENANT));
    }

    @Override
    public void setTenant(UUID tenant) {
        wgregion.setFlag(FlagHandler.PS_TENANT, tenant == null ? null : tenant.toString());
    }

    @Override
    public String getRentPeriod() {
        return wgregion.getFlag(FlagHandler.PS_RENT_PERIOD);
    }

    @Override
    public void setRentPeriod(String s) {
        wgregion.setFlag(FlagHandler.PS_RENT_PERIOD, s);
    }

    @Override
    public Double getPrice() {
        return wgregion.getFlag(FlagHandler.PS_PRICE);
    }

    @Override
    public void setPrice(Double price) {
        wgregion.setFlag(FlagHandler.PS_PRICE, price);
    }

    @Override
    public void setRentLastPaid(Long timestamp) {
        wgregion.setFlag(FlagHandler.PS_RENT_LAST_PAID, timestamp == null ? null : timestamp.doubleValue());
    }

    @Override
    public Long getRentLastPaid() {
        return wgregion.getFlag(FlagHandler.PS_RENT_LAST_PAID) == null ? null : wgregion.getFlag(FlagHandler.PS_RENT_LAST_PAID).longValue();
    }

    @Override
    public void setRentable(UUID landlord, String rentPeriod, double rentPrice) {
        setLandlord(landlord);
        setTenant(null);
        setRentPeriod(rentPeriod);
        setPrice(rentPrice);
    }

    @Override
    public void rentOut(UUID landlord, UUID tenant, String rentPeriod, double rentPrice) {
        setLandlord(landlord);
        setTenant(tenant);
        setRentPeriod(rentPeriod);
        setPrice(rentPrice);
        setRentLastPaid(Instant.now().getEpochSecond());

        ProtectionStones.getEconomy().getRentedList().add(this);

        if (!getTypeOptions().landlordStillOwner) {
            getWGRegion().getOwners().removeAll();
            getWGRegion().getMembers().removeAll();
        }
        if (getTypeOptions().tenantRentRole.equals("member")) {
            addMember(tenant);
        } else if (getTypeOptions().tenantRentRole.equals("owner")) {
            addOwner(tenant);
        }
    }

    @Override
    public void removeRenting() {
        getWGRegion().getOwners().removeAll();
        getWGRegion().getMembers().removeAll();
        addOwner(getLandlord());

        setLandlord(null);
        setTenant(null);
        setRentPeriod(null);
        setPrice(null);
        setRentLastPaid(null);

        ProtectionStones.getEconomy().getRentedList().remove(this);
    }

    @Override
    public String getTaxPeriod() {
        return MiscUtil.describeDuration(Duration.ofSeconds(getTypeOptions().taxPeriod));
    }

    @Override
    public String getTaxPaymentPeriod() {
        return MiscUtil.describeDuration(Duration.ofSeconds(getTypeOptions().taxPaymentTime));
    }

    @Override
    public List<TaxPayment> getTaxPaymentsDue() {
        // taxes disabled
        if (getTypeOptions().taxPeriod == -1) return new ArrayList<>();

        Set<String> s = wgregion.getFlag(FlagHandler.PS_TAX_PAYMENTS_DUE);
        if (s == null) return new ArrayList<>();

        // convert to TaxPayment objects
        List<TaxPayment> taxPayments = s.stream()
                .map(TaxPayment::fromString)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // correct for any invalid entries
        setTaxPaymentsDue(taxPayments);
        return taxPayments;
    }

    @Override
    public void setTaxPaymentsDue(List<TaxPayment> taxPayments) {
        WGUtils.setFlagIfNeeded(wgregion, FlagHandler.PS_TAX_PAYMENTS_DUE, taxPayments.stream().map(TaxPayment::toFlagEntry).collect(Collectors.toSet()));
    }

    @Override
    public List<LastRegionTaxPaymentEntry> getRegionLastTaxPaymentAddedEntries() {
        // taxes disabled
        if (getTypeOptions().taxPeriod == -1) return new ArrayList<>();

        Set<String> s = wgregion.getFlag(FlagHandler.PS_TAX_LAST_PAYMENT_ADDED);
        if (s == null) return new ArrayList<>();

        // convert string entries to object
        List<LastRegionTaxPaymentEntry> entries = s.stream()
                .map(LastRegionTaxPaymentEntry::fromString)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // correct for invalid entries
        setRegionLastTaxPaymentAddedEntries(entries);
        return entries;
    }

    @Override
    public void setRegionLastTaxPaymentAddedEntries(List<LastRegionTaxPaymentEntry> entries) {
        Set<String> set = entries.stream().map(LastRegionTaxPaymentEntry::toFlagEntry).collect(Collectors.toSet());
        WGUtils.setFlagIfNeeded(wgregion, FlagHandler.PS_TAX_LAST_PAYMENT_ADDED, set);
    }

    @Override
    public UUID getTaxAutopayer() {
        return wgregion.getFlag(FlagHandler.PS_TAX_AUTOPAYER) == null ? null : UUID.fromString(wgregion.getFlag(FlagHandler.PS_TAX_AUTOPAYER));
    }

    @Override
    public void setTaxAutopayer(UUID player) {
        WGUtils.setFlagIfNeeded(wgregion, FlagHandler.PS_TAX_AUTOPAYER, player == null ? null : player.toString());
    }

    @Override
    public EconomyResponse payTax(PSPlayer p, double amount) {
        List<TaxPayment> paymentList = getTaxPaymentsDue();
        Collections.sort(paymentList); // sort by date due

        double paymentAmount = 0;
        for (int i = 0; i < paymentList.size(); i++) {
            TaxPayment tp = paymentList.get(i);
            if (tp.amount > amount) { // if the amount cannot fully pay the tax
                tp.amount -= amount;
                paymentAmount += amount;
                break;
            } else { // if the amount being paid can fully pay off this tax
                amount -= tp.amount;
                paymentAmount += tp.amount;
                paymentList.remove(i);
                i--;
            }
        }

        // update with corrected tax payments
        setTaxPaymentsDue(paymentList);
        return p.withdrawBalance(paymentAmount);
    }

    @Override
    public boolean isTaxPaymentLate() {
        // check if taxes disabled for block
        if (getTypeOptions().taxPeriod == -1) return false;

        // update first
        updateTaxPayments();

        long currentTime = System.currentTimeMillis();
        // loop through pending tax payments and see if the payment due date has passed
        for (TaxPayment tp : getTaxPaymentsDue()) {
            if (tp.whenPaymentIsDue < currentTime)
                return true;
        }
        return false;
    }

    @Override
    public void updateTaxPayments() {
        // taxes disabled
        if (getTypeOptions().taxPeriod == -1) return;

        long currentTime = System.currentTimeMillis();

        List<TaxPayment> payments = Objs.replaceNull(getTaxPaymentsDue(), new ArrayList<>());
        List<LastRegionTaxPaymentEntry> lastAdded = Objs.replaceNull(getRegionLastTaxPaymentAddedEntries(), new ArrayList<>());

        lastAdded = lastAdded.stream()
                // remove entries that are not for this region
                .filter(e -> e.getRegionId().equals(getId()))
                // add payment if it is time for the next payment cycle
                .peek(e -> {
                    if (e.getLastPaymentAdded() + Duration.ofSeconds(getTypeOptions().taxPeriod).toMillis() < currentTime) {
                        e.setLastPaymentAdded(currentTime);
                        payments.add(new TaxPayment(currentTime + Duration.ofSeconds(getTypeOptions().taxPaymentTime).toMillis(), getTaxRate(), getId()));
                    }
                }).collect(Collectors.toList());

        // if no entry was found, add a tax payment
        if (lastAdded.isEmpty()) {
            lastAdded.add(new LastRegionTaxPaymentEntry(getId(), currentTime));
            payments.add(new TaxPayment(currentTime + Duration.ofSeconds(getTypeOptions().taxPaymentTime).toMillis(), getTaxRate(), getId()));
        }

        setTaxPaymentsDue(payments);
        setRegionLastTaxPaymentAddedEntries(lastAdded);
    }

    @Override
    public Block getProtectBlock() {
        PSLocation psl = WGUtils.parsePSRegionToLocation(wgregion.getId());
        return world.getBlockAt(psl.x, psl.y, psl.z);
    }

    @Override
    public PSProtectBlock getTypeOptions() {
        return ProtectionStones.getBlockOptions(getType());
    }

    @Override
    public String getType() {
        return wgregion.getFlag(FlagHandler.PS_BLOCK_MATERIAL);
    }

    @Override
    public void setType(PSProtectBlock type) {
        super.setType(type);
        getWGRegion().setFlag(FlagHandler.PS_BLOCK_MATERIAL, type.type);
    }

    @Override
    public boolean isOwner(UUID uuid) {
        return wgregion.getOwners().contains(uuid);
    }

    @Override
    public boolean isMember(UUID uuid) {
        return wgregion.getMembers().contains(uuid);
    }

    @Override
    public ArrayList<UUID> getOwners() {
        return new ArrayList<>(wgregion.getOwners().getUniqueIds());
    }

    @Override
    public ArrayList<UUID> getMembers() {
        return new ArrayList<>(wgregion.getMembers().getUniqueIds());
    }

    @Override
    public void addOwner(UUID uuid) {
        if (uuid == null) return;
        wgregion.getOwners().addPlayer(uuid);
    }

    @Override
    public void addMember(UUID uuid) {
        if (uuid == null) return;
        wgregion.getMembers().addPlayer(uuid);
    }

    @Override
    public void removeOwner(UUID uuid) {
        if (uuid == null) return;
        // remove tax autopayer if the player is the autopayer
        if (getTaxAutopayer() != null && getTaxAutopayer().equals(uuid)) {
            setTaxAutopayer(null);
        }
        if (getLandlord() != null && getLandlord().equals(uuid)) {
            // remove rents if the player is the landlord
            if (getRentStage() == RentStage.LOOKING_FOR_TENANT || getRentStage() == RentStage.RENTING) {

                if (getTenant() != null) {
                    PSPlayer tenant = PSPlayer.fromUUID(getTenant());
                    if (tenant.getOfflinePlayer().isOnline()) {
                        PSL.msg(Bukkit.getPlayer(getTenant()), PSL.RENT_TENANT_STOPPED_TENANT.msg()
                                .replace("%region%", getName() != null ? getName() : getId()));
                    }
                }

                removeRenting(); // this needs to be called before removing the player, since it adds the player back
            }
            setLandlord(null); // in case the player was selling the region
        }
        if (wgregion.getOwners().contains(uuid))
            wgregion.getOwners().removePlayer(uuid);
    }

    @Override
    public void removeMember(UUID uuid) {
        if (uuid == null) return;
        if (wgregion.getMembers().contains(uuid))
            wgregion.getMembers().removePlayer(uuid);
    }

    @Override
    public List<BlockVector2> getPoints() {
        return wgregion.getPoints();
    }

    @Override
    public List<PSRegion> getMergeableRegions(Player p) {
        return WGUtils.findOverlapOrAdjacentRegions(getWGRegion(), getWGRegionManager(), getWorld())
                .stream()
                .map(r -> PSRegion.fromWGRegion(getWorld(), r))
                .filter(r -> r != null && r.getTypeOptions() != null && !r.getId().equals(getId()))
                .filter(r -> r.getTypeOptions().allowMerging)
                .filter(r -> r.isOwner(p.getUniqueId()) || p.hasPermission("protectionstones.admin"))
                .filter(r -> WGUtils.canMergeRegionTypes(getTypeOptions(), r))
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteRegion(boolean deleteBlock) {
        return deleteRegion(deleteBlock, null);
    }

    @Override
    public boolean deleteRegion(boolean deleteBlock, Player cause) {
        PSRemoveEvent event = new PSRemoveEvent(this, cause);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) { // if event was cancelled, prevent execution
            return false;
        }

        // set the physical block to air
        if (deleteBlock && !this.isHidden()) {
            this.getProtectBlock().setType(Material.AIR);
        }

        // remove name from cache
        if (getName() != null) {
            HashMap<String, ArrayList<String>> rIds = ProtectionStones.regionNameToID.get(getWorld().getUID());
            if (rIds != null && rIds.containsKey(getName())) {
                if (rIds.get(getName()).size() == 1) {
                    rIds.remove(getName());
                } else {
                    rIds.get(getName()).remove(getId());
                }
            }
        }

        // remove region from WorldGuard
        // specify UNSET_PARENT_IN_CHILDREN removal strategy so that region children don't get deleted
        rgmanager.removeRegion(wgregion.getId(), RemovalStrategy.UNSET_PARENT_IN_CHILDREN);

        return true;
    }

    @Override
    public ProtectedRegion getWGRegion() {
        return wgregion;
    }
}
