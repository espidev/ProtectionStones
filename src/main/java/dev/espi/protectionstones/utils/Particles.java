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

package dev.espi.protectionstones.utils;

import dev.espi.protectionstones.ProtectionStones;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class Particles {
    public static void persistRedstoneParticle(Player p, Location l, Particle.DustOptions d, int occ) {
        for (int i = 0; i < occ; i++) {
            Bukkit.getScheduler().runTaskLater(ProtectionStones.getInstance(), () -> {
                if (p.isOnline()) p.spawnParticle(Particle.REDSTONE, l, 1, d);
            }, i*10);
        }
    }
}
