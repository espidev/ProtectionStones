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
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.event.PSRemoveEvent;
import dev.espi.protectionstones.utils.WGUtils;
import lombok.val;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

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
    public String getID() {
        return wgregion.getId();
    }

    @Override
    public String getName() {
        return wgregion.getFlag(FlagHandler.PS_NAME);
    }

    @Override
    public void setName(String name) {
        HashMap<String, ArrayList<String>> m = ProtectionStones.regionNameToID.get(getWorld());
        if (m == null) { // if the world has not been added
            ProtectionStones.regionNameToID.put(getWorld(), new HashMap<>());
            m = ProtectionStones.regionNameToID.get(getWorld());
        }
        if (m.get(getName()) != null) {
            m.get(getName()).remove(getID());
        }
        if (name != null) {
            if (m.containsKey(name)) {
                m.get(name).add(getID());
            } else {
                m.put(name, new ArrayList<>(Collections.singletonList(getID())));
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
        return new Location(world, Double.parseDouble(pos[0]), Double.parseDouble(pos[1]), Double.parseDouble(pos[2]));
    }

    @Override
    public void setHome(double blockX, double blockY, double blockZ) {
        wgregion.setFlag(FlagHandler.PS_HOME, blockX + " " + blockY + " " + blockZ);
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
        getWGRegion().getOwners().addPlayer(player);
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
        getWGRegion().getOwners().removeAll();
        getWGRegion().getMembers().removeAll();
        getWGRegion().getOwners().addPlayer(tenant);
    }

    @Override
    public void removeRenting() {
        getWGRegion().getOwners().removeAll();
        getWGRegion().getMembers().removeAll();
        getWGRegion().getOwners().addPlayer(getLandlord());

        setLandlord(null);
        setTenant(null);
        setRentPeriod(null);
        setPrice(null);
        setRentLastPaid(null);

        ProtectionStones.getEconomy().getRentedList().remove(this);
    }


    @Override
    public Duration getTaxPaymentPeriod() {
        return Duration.ofSeconds(getTypeOptions().taxPaymentTime);
    }

    @Override
    public List<TaxPayment> getTaxPaymentsDue() {
        // taxes disabled
        if (getTypeOptions().taxPeriod == -1) return new ArrayList<>();

        Set<String> s = wgregion.getFlag(FlagHandler.PS_TAX_PAYMENTS_DUE);
        if (s == null) return new ArrayList<>();

        List<TaxPayment> taxPayments = new ArrayList<>();

        for (String payment : s) {
            try {
                // format: "timestamp amount"
                taxPayments.add(new TaxPayment(Long.parseLong(payment.split(" ")[0]), Double.parseDouble(payment.split(" ")[1])));
            } catch (Exception e) {
                e.printStackTrace();
                s.remove(payment);
            }
        }
        wgregion.setFlag(FlagHandler.PS_TAX_PAYMENTS_DUE, s);
        return taxPayments;
    }

    @Override
    public UUID getTaxAutopayer() {
        return wgregion.getFlag(FlagHandler.PS_TAX_AUTOPAYER) == null ? null : UUID.fromString(wgregion.getFlag(FlagHandler.PS_TAX_AUTOPAYER));
    }

    @Override
    public void setTaxAutopayer(UUID player) {
        wgregion.setFlag(FlagHandler.PS_TAX_AUTOPAYER, player == null ? null : player.toString());
    }

    @Override
    public EconomyResponse payTax(PSPlayer p, double amount) {
        List<TaxPayment> paymentList = getTaxPaymentsDue();
        Collections.sort(paymentList);

        double paymentAmount = 0;
        for (int i = 0; i < paymentList.size(); i++) {
            TaxPayment tp = paymentList.get(i);
            if (tp.amount > amount) {
                tp.amount -= amount;
                paymentAmount += amount;
                break;
            } else {
                amount -= tp.amount;
                paymentAmount += tp.amount;
                paymentList.remove(i);
                i--;
            }
        }

        Set<String> s = new HashSet<>();
        paymentList.forEach(taxPayment -> s.add(taxPayment.whenPaymentIsDue + " " + taxPayment.amount));
        wgregion.setFlag(FlagHandler.PS_TAX_PAYMENTS_DUE, s);
        return p.withdrawBalance(paymentAmount);
    }

    @Override
    public boolean isTaxPaymentLate() {
        if (getTypeOptions().taxPeriod == -1) return false;
        long currentTime = System.currentTimeMillis();
        for (TaxPayment tp : getTaxPaymentsDue()) {
            if (tp.whenPaymentIsDue < currentTime) return true;
        }
        return false;
    }

    @Override
    public void updateTaxPayments() {
        // taxes disabled
        if (getTypeOptions().taxPeriod == -1) return;

        long currentTime = System.currentTimeMillis();

        final Set<String> lastAdded = wgregion.getFlag(FlagHandler.PS_TAX_LAST_PAYMENT_ADDED) == null ? new HashSet<>() : wgregion.getFlag(FlagHandler.PS_TAX_LAST_PAYMENT_ADDED),
                payments = wgregion.getFlag(FlagHandler.PS_TAX_PAYMENTS_DUE) == null ? new HashSet<>() : wgregion.getFlag(FlagHandler.PS_TAX_PAYMENTS_DUE);

        if (lastAdded.isEmpty()) { // if the flags have not been initialized yet
            lastAdded.add(getID() + " " + currentTime);
            payments.add((currentTime + getTaxPaymentPeriod().toMillis()) + " " + getTaxRate());
        } else {
            List<String> remove = new ArrayList<>();

            boolean found = false;
            // loop through entries
            for (val last : lastAdded) {
                try {
                    String id = last.split(" ")[0];
                    if (!id.equals(getID())) { // remove entries that aren't this region (auto-fixes)
                        remove.add(last);
                        continue;
                    }
                    found = true;

                    long lastPaid = Long.parseLong(last.split(" ")[1]);
                    if (lastPaid + getTaxPeriod().toMillis() < currentTime) { // if there is a tax cycle
                        payments.add((currentTime + getTaxPaymentPeriod().toMillis()) + " " + getTaxRate() + " " + getID()); // add payment
                        remove.add(last); // remove this entry
                        lastAdded.add(getID() + " " + currentTime); // add new entry for next tax cycle
                    }
                } catch (Exception e) {
                    remove.add(last);
                }
            }

            if (!found) { // if no entry was found, add a tax payment
                payments.add((currentTime + getTaxPaymentPeriod().toMillis()) + " " + getTaxRate());
                lastAdded.add(getID() + " " + currentTime);
            }

            remove.forEach(lastAdded::remove); // remove entries
        }

        wgregion.setFlag(FlagHandler.PS_TAX_LAST_PAYMENT_ADDED, lastAdded);
        wgregion.setFlag(FlagHandler.PS_TAX_PAYMENTS_DUE, payments);
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
    public List<BlockVector2> getPoints() {
        return wgregion.getPoints();
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

        if (deleteBlock && !this.isHidden()) {
            this.getProtectBlock().setType(Material.AIR);
        }

        if (getName() != null) { // remove name from cache
            if (ProtectionStones.regionNameToID.get(getWorld()).containsKey(getName())) {
                if (ProtectionStones.regionNameToID.get(getWorld()).get(getName()).size() == 1) {
                    ProtectionStones.regionNameToID.get(getWorld()).remove(getName());
                } else {
                    ProtectionStones.regionNameToID.get(getWorld()).get(getName()).remove(getID());
                }
            }
        }
        rgmanager.removeRegion(wgregion.getId());
        return true;
    }

    @Override
    public ProtectedRegion getWGRegion() {
        return wgregion;
    }
}
