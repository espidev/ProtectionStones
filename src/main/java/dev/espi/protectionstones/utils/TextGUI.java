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

import dev.espi.protectionstones.PSL;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;

import java.util.List;

public class TextGUI {

    // page starts at zero, but displays start at one
    public static void displayGUI(CommandSender s, String header, int currentPage, int guiSize, List<TextComponent> lines) {
        PSL.msg(s, header);

        for (int i = currentPage*guiSize; i < Math.min((currentPage+1) * guiSize, lines.size()); i++) {
            s.spigot().sendMessage(lines.get(i));
        }

        
    }

}
