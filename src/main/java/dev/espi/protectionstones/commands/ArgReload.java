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

import java.util.Collections;
import java.util.List;

public class ArgReload implements PSCommandArg {

    // /ps reload

    @Override
    public List<String> getNames() {
        return Collections.singletonList("reload");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return true;
    }

    @Override
    public boolean executeArgument(CommandSender p, String[] args) {
        if (!p.hasPermission("protectionstones.admin")) {
            PSL.msg(p, PSL.NO_PERMISSION_ADMIN.msg());
            return true;
        }
        PSL.msg(p, PSL.RELOAD_START.msg());
        ProtectionStones.loadConfig(true);
        PSL.msg(p, PSL.RELOAD_COMPLETE.msg());
        return true;
    }

}
