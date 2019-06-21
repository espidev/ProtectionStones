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

package dev.espi.ProtectionStones.utils;

import dev.espi.ProtectionStones.PSL;
import dev.espi.ProtectionStones.PSRegion;
import org.bukkit.entity.Player;

import java.util.List;

public class ChatUtils {
    public static void displayDuplicateRegionAliases(Player p, List<PSRegion> r) {
        StringBuilder rep = new StringBuilder(r.get(0).getID());

        for (int i = 1; i < r.size(); i++) {
            rep.append(", ").append(r.get(i).getID());
        }

        PSL.msg(p, PSL.SPECIFY_ID_INSTEAD_OF_ALIAS.msg().replace("%regions%", rep.toString()));
    }
}
