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

package me.vik1395.ProtectionStones;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public enum PSL {
    // messages.yml

    NO_SUCH_COMMAND("NO_SUCH_COMMAND", ChatColor.RED + "No such command. please type /ps help for more info");

    private final String key;
    private String msg;

    private static File conf = new File(ProtectionStones.getPlugin().getDataFolder(), "messages.yml");
    private static HashMap<String, String> keyToMsg = new HashMap<>();

    PSL(String path, String start) {
        this.key = path;
        this.msg = start;
    }

    public String msg() {
        String msgG = keyToMsg.get(key);
        return ChatColor.translateAlternateColorCodes('&', (msgG == null) ? msg : msgG);
    }

    public static void loadConfig() {
        keyToMsg.clear();
        if (!conf.exists()) {
            try {
                conf.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        YamlConfiguration yml = YamlConfiguration.loadConfiguration(conf);
        for (PSL psl : PSL.values()) {
            if (yml.getString(psl.key) == null) {
                yml.set(psl.key, psl.msg.replace('§', '&'));
            } else {
                keyToMsg.put(psl.key, yml.getString(psl.key));
            }
        }
        try {
            yml.save(conf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
