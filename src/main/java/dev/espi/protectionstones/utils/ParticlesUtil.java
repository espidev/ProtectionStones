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

package dev.espi.protectionstones.utils;

import dev.espi.protectionstones.ProtectionStones;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class ParticlesUtil {
    public static void persistRedstoneParticle(Player p, Location l, Particle.DustOptions d, int occ) {
        for (int i = 0; i < occ; i++) {
            Bukkit.getScheduler().runTaskLater(ProtectionStones.getInstance(), () -> {
                if (!p.isOnline()) return;

                // Stronger "glow marker" burst
                p.spawnParticle(Particle.DUST, l,
                        2,            // count (was 1)
                        0.10, 0.15, 0.10, // offset/spread (x,y,z)
                        0.0,
                        d
                );

                p.spawnParticle(Particle.GLOW, l,
                        2,
                        0.05, 0.08, 0.05,
                        0.0
                );

            }, i * 20L);
        }
    }
}
