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

package dev.espi.protectionstones.commands;

import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.ProtectionStones;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
    public List<String> getPermissionsToExecute() {
        return Collections.singletonList("protectionstones.toggle");
    }

    @Override
    public HashMap<String, Boolean> getRegisteredFlags() {
        return null;
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args, HashMap<String, String> flags) {
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

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return null;
    }
}
