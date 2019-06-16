/*
 * Copyright 2019 ProtectionStones team and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.espi.ProtectionStones.commands;

import dev.espi.ProtectionStones.PSL;
import dev.espi.ProtectionStones.ProtectionStones;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ArgToggle implements PSCommandArg {

    @Override
    public List<String> getNames() {
        return Collections.singletonList("toggle");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args) {
        Player p = (Player) s;
        if (p.hasPermission("protectionstones.toggle")) {
            if (!ProtectionStones.toggleList.contains(p.getUniqueId())) {
                ProtectionStones.toggleList.add(p.getUniqueId());
                p.sendMessage(PSL.TOGGLE_OFF.msg());
            } else {
                ProtectionStones.toggleList.remove(p.getUniqueId());
                p.sendMessage(PSL.TOGGLE_ON.msg());
            }
        } else {
            p.sendMessage(PSL.NO_PERMISSION_TOGGLE.msg());
        }
        return true;
    }
}
