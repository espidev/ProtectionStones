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

package dev.espi.protectionstones.utils.pre113;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ActionBar {

    public static void sendActionBar(Player player, String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);

        String v = Bukkit.getServer().getClass().getPackage().getName();
        v = v.substring(v.lastIndexOf(".") + 1);

        try {
            if (!(v.equalsIgnoreCase("v1_8_R1") || (v.contains("v1_7_")))) {
                Class<?> c1 = Class.forName("org.bukkit.craftbukkit." + v + ".entity.CraftPlayer");
                Object p = c1.cast(player);
                Object ppoc;
                Class<?> c4 = Class.forName("net.minecraft.server." + v + ".PacketPlayOutChat");
                Class<?> c5 = Class.forName("net.minecraft.server." + v + ".Packet");

                Class<?> c2 = Class.forName("net.minecraft.server." + v + ".ChatComponentText");
                Class<?> c3 = Class.forName("net.minecraft.server." + v + ".IChatBaseComponent");
                Object o = c2.getConstructor(new Class<?>[]{String.class}).newInstance(message);
                ppoc = c4.getConstructor(new Class<?>[]{c3, byte.class}).newInstance(o, (byte) 2);

                Method getHandle = c1.getDeclaredMethod("getHandle");
                Object handle = getHandle.invoke(p);

                Field fieldConnection = handle.getClass().getDeclaredField("playerConnection");
                Object playerConnection = fieldConnection.get(handle);

                Method sendPacket = playerConnection.getClass().getDeclaredMethod("sendPacket", c5);
                sendPacket.invoke(playerConnection, ppoc);
            } else {
                Class<?> c1 = Class.forName("org.bukkit.craftbukkit." + v + ".entity.CraftPlayer");
                Object p = c1.cast(player);
                Object ppoc;
                Class<?> c4 = Class.forName("net.minecraft.server." + v + ".PacketPlayOutChat");
                Class<?> c5 = Class.forName("net.minecraft.server." + v + ".Packet");

                Class<?> c2 = Class.forName("net.minecraft.server." + v + ".ChatSerializer");
                Class<?> c3 = Class.forName("net.minecraft.server." + v + ".IChatBaseComponent");
                Method m3 = c2.getDeclaredMethod("a", String.class);
                Object cbc = c3.cast(m3.invoke(c2, "{\"text\": \"" + message + "\"}"));
                ppoc = c4.getConstructor(new Class<?>[]{c3, byte.class}).newInstance(cbc, (byte) 2);

                Method getHandle = c1.getDeclaredMethod("getHandle");
                Object handle = getHandle.invoke(p);

                Field fieldConnection = handle.getClass().getDeclaredField("playerConnection");
                Object playerConnection = fieldConnection.get(handle);

                Method sendPacket = playerConnection.getClass().getDeclaredMethod("sendPacket", c5);
                sendPacket.invoke(playerConnection, ppoc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
