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
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.utils.MiscUtil;
import dev.espi.protectionstones.utils.WGUtils;
import lombok.val;
import lombok.var;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PSEconomy {
    private List<PSRegion> rentedList = new ArrayList<>();
    private static int rentRunner = -1, taxRunner = -1;

    public PSEconomy() {
        if (!ProtectionStones.getInstance().isVaultSupportEnabled()) {
            ProtectionStones.getInstance().getLogger().warning("Vault is not enabled! Economy functions (renting & buying) will not work!");
            return;
        }
        // find regions that are being rented out (called on startup or reload)
        loadRentList();

        // start rent
        rentRunner = Bukkit.getScheduler().runTaskTimerAsynchronously(ProtectionStones.getInstance(), this::updateRents, 0, 200).getTaskId();

        // start taxes
        if (ProtectionStones.getInstance().getConfigOptions().taxEnabled)
            taxRunner = Bukkit.getScheduler().runTaskTimerAsynchronously(ProtectionStones.getInstance(), this::updateTaxes, 0, 200).getTaskId();
    }

    private void updateRents() {

        for (int i = 0; i < rentedList.size(); i++) {
            try {
                PSRegion r = rentedList.get(i);
                if (r.getTypeOptions() == null) continue;
                if (r.getRentStage() != PSRegion.RentStage.RENTING) {
                    // remove entry if it isn't in renting stage.
                    rentedList.remove(i);
                    i--;
                    continue;
                }

                Duration rentPeriod = MiscUtil.parseRentPeriod(r.getRentPeriod());
                // if tenant needs to pay
                if (Instant.now().getEpochSecond() > (r.getRentLastPaid() + rentPeriod.getSeconds())) {
                    doRentPayment(r);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void updateTaxes() {
        HashMap<World, RegionManager> regions = WGUtils.getAllRegionManagers();
        for (World w : regions.keySet()) {
            RegionManager rgm = regions.get(w);
            for (ProtectedRegion r : rgm.getRegions().values()) {
                if (ProtectionStones.isPSRegion(r)) {
                    PSRegion psr = PSRegion.fromWGRegion(w, r);
                    processTaxes(psr);
                }
            }
        }
    }

    /**
     * Stops the economy cycle. Used for reloads when creating a new PSEconomy.
     */
    public void stop() {
        if (rentRunner != -1) {
            Bukkit.getScheduler().cancelTask(rentRunner);
            rentRunner = -1;
        }
        if (taxRunner != -1) {
            Bukkit.getScheduler().cancelTask(taxRunner);
            taxRunner = -1;
        }
    }

    /**
     * Load list of regions that are rented into memory.
     */
    public void loadRentList() {
        rentedList = new ArrayList<>();

        var managers = WGUtils.getAllRegionManagers();

        for (World w : managers.keySet()) {
            RegionManager rgm = managers.get(w);
            for (ProtectedRegion pr : rgm.getRegions().values()) {
                if (ProtectionStones.isPSRegion(pr)) {
                    rentedList.add(PSRegion.fromWGRegion(w, pr));
                }
            }
        }
    }

    /**
     * Process taxes for a region.
     *
     * @param r the region to process taxes for
     */
    public static void processTaxes(PSRegion r) {
        if (r.getTypeOptions() != null && r.getTypeOptions().taxPeriod != -1) { // taxes are enabled
            Bukkit.getScheduler().runTask(ProtectionStones.getInstance(), () -> {
                r.updateTaxPayments(); // process payments
                if (!r.getTaxPaymentsDue().isEmpty() && r.getTaxAutopayer() != null) { // check auto-payment
                    PSPlayer psp = PSPlayer.fromUUID(r.getTaxAutopayer());
                    val res = r.payTax(psp, psp.getBalance());

                    if (psp.getPlayer() != null) {
                        PSL.msg(psp.getPlayer(), PSL.TAX_PAID.msg()
                                .replace("%amount%", "" + res.amount)
                                .replace("%region%", r.getName() == null ? r.getID() : r.getName() + "(" + r.getID() + ")"));
                    }
                }

                if (r.isTaxPaymentLate()) { // late tax payment punishment
                    r.deleteRegion(true); // TODO
                }
            });
        }
    }

    /**
     * Process a rent payment for a region.
     * It does not do any checks, it is expected to check if the rent time has passed before this function is called.
     *
     * @param r the region to perform the rent payment
     */
    public static void doRentPayment(PSRegion r) {
        PSPlayer tenant = PSPlayer.fromPlayer(Bukkit.getOfflinePlayer(r.getTenant()));
        PSPlayer landlord = PSPlayer.fromPlayer(Bukkit.getOfflinePlayer(r.getLandlord()));

        // not enough money for rent
        if (!tenant.hasAmount(r.getPrice())) {
            if (tenant.getOfflinePlayer().isOnline()) {
                PSL.msg(Bukkit.getPlayer(r.getTenant()), PSL.RENT_EVICT_NO_MONEY_TENANT.msg()
                        .replace("%region%", r.getName() != null ? r.getName() : r.getID())
                        .replace("%price%", String.format("%.2f", r.getPrice())));
            }
            if (landlord.getOfflinePlayer().isOnline()) {
                PSL.msg(Bukkit.getPlayer(r.getLandlord()), PSL.RENT_EVICT_NO_MONEY_LANDLORD.msg()
                        .replace("%region%", r.getName() != null ? r.getName() : r.getID())
                        .replace("%tenant%", tenant.getName()));
            }
            r.removeRenting();
            return;
        }

        // send payment messages
        if (tenant.getOfflinePlayer().isOnline()) {
            PSL.msg(Bukkit.getPlayer(r.getTenant()), PSL.RENT_PAID_TENANT.msg()
                    .replace("%price%", String.format("%.2f", r.getPrice()))
                    .replace("%landlord%", landlord.getName())
                    .replace("%region%", r.getName() != null ? r.getName() : r.getID()));
        }
        if (landlord.getOfflinePlayer().isOnline()) {
            PSL.msg(Bukkit.getPlayer(r.getLandlord()), PSL.RENT_PAID_LANDLORD.msg()
                    .replace("%price%", String.format("%.2f", r.getPrice()))
                    .replace("%tenant%", tenant.getName())
                    .replace("%region%", r.getName() != null ? r.getName() : r.getID()));
        }

        // update money must be run in main thread
        Bukkit.getScheduler().runTask(ProtectionStones.getInstance(), () -> tenant.pay(landlord, r.getPrice()));
        r.setRentLastPaid(Instant.now().getEpochSecond());
        try { // must save region to persist last paid
            r.getWGRegionManager().saveChanges();
        } catch (StorageException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get list of rented regions.
     *
     * @return the list of rented regions
     */
    public List<PSRegion> getRentedList() {
        return rentedList;
    }


}