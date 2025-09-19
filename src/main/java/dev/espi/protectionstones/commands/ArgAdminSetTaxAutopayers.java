/*
 * Copyright 2019 ProtectionStones team and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package dev.espi.protectionstones.commands;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.utils.WGUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

class ArgAdminSetTaxAutopayers {
    static boolean argumentAdminSetTaxAutopayers(CommandSender s, String[] args) {
        if (!ProtectionStones.getInstance().getConfigOptions().taxEnabled) {
            return PSL.msg(s, Component.text("Taxes are disabled! Enable it in the config.", NamedTextColor.RED));
        }

        PSL.msg(s,
                Component.text(
                        "Scanning through regions, and setting tax autopayers for regions that don't have one...",
                        NamedTextColor.GRAY
                )
        );

        Bukkit.getScheduler().runTaskAsynchronously(ProtectionStones.getInstance(), () -> {
            WGUtils.getAllRegionManagers().forEach((w, rgm) -> {
                for (ProtectedRegion r : rgm.getRegions().values()) {
                    PSRegion psr = PSRegion.fromWGRegion(w, r);

                    if (psr != null && psr.getTypeOptions() != null && psr.getTypeOptions().taxPeriod != -1 && psr.getTaxAutopayer() == null) {
                        if (psr.getOwners().size() >= 1) {
                            PSL.msg(s,
                                    Component.text("Configured tax autopayer to be ", NamedTextColor.GRAY)
                                            .append(Component.text(psr.getOwners().get(0).toString(), NamedTextColor.AQUA))
                                            .append(Component.text(" for region ", NamedTextColor.GRAY))
                                            .append(Component.text(psr.getId(), NamedTextColor.AQUA))
                            );

                            psr.setTaxAutopayer(psr.getOwners().get(0));
                        }
                    }

                }
            });
            PSL.msg(s, Component.text("Complete!", NamedTextColor.GREEN));

        });

        return true;
    }
}
