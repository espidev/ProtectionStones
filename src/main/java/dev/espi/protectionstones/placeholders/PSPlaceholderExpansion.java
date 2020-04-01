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

package dev.espi.protectionstones.placeholders;

import dev.espi.protectionstones.ProtectionStones;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PSPlaceholderExpansion extends PlaceholderExpansion {

    // persist through reloads
    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister(){
        return true;
    }

    @Override
    public String getIdentifier() {
        return "protectionstones";
    }

    @Override
    public String getAuthor() {
        return ProtectionStones.getInstance().getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return ProtectionStones.getInstance().getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player p, String identifier) {
        String[] split = identifier.split("-");
        if (split.length > 0) {
            if (split[0].equals("config")) {
                return ConfigPlaceholders.resolveConfig(identifier);
            } else if (split[0].equals("currentregion")) {
                return RegionPlaceholders
            }
        }

        return null;
    }
}
