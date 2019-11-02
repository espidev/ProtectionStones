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

import dev.espi.protectionstones.PSProtectBlock;
import dev.espi.protectionstones.ProtectionStones;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.HashMap;
import java.util.UUID;

public class MiscUtil {
    static final int MAX_USERNAME_LENGTH = 16;
    public static HashMap<String, String> uuidToBase64Head = new HashMap<>();

    public static int getPermissionNumber(Player p, String perm, int def) {
        int n = -999;
        for (PermissionAttachmentInfo pia : p.getEffectivePermissions()) {
            String permission = pia.getPermission();
            String[] sp = permission.split("\\.");
            if (permission.startsWith(perm) && sp.length > 0 && StringUtils.isNumeric(sp[sp.length - 1])) {
                n = Math.max(n, Integer.parseInt(sp[sp.length - 1]));
            }
        }
        return n == -999 ? def : n;
    }

    public static ItemStack getProtectBlockItemFromType(String type) {
        if (type.startsWith(Material.PLAYER_HEAD.toString())) {
            return new ItemStack(Material.PLAYER_HEAD);
        } else {
            return new ItemStack(Material.getMaterial(type));
        }
    }

    public static String getProtectBlockType(ItemStack i) {
        if (i.getType() == Material.PLAYER_HEAD || i.getType() == Material.LEGACY_SKULL_ITEM) {
            SkullMeta sm = (SkullMeta) i.getItemMeta();

            // PLAYER_HEAD
            if (!sm.hasOwner()) {
                return Material.PLAYER_HEAD.toString();
            }

            // PLAYER_HEAD:base64
            if (ProtectionStones.getBlockOptions("PLAYER_HEAD:" + sm.getOwningPlayer().getUniqueId().toString()) != null) {
                return Material.PLAYER_HEAD.toString() + ":" + sm.getOwningPlayer().getUniqueId().toString();
            }

            /*
            // PLAYER_HEAD:base64
            if (i.getItemMeta().getCustomTagContainer().hasCustomTag(BASE64SKULL_TAG, ItemTagType.STRING)) {
                return Material.PLAYER_HEAD.toString() + ":" + i.getItemMeta().getCustomTagContainer().getCustomTag(BASE64SKULL_TAG, ItemTagType.STRING);
            }

            // PLAYER_HEAD:UUID
            String ret = i.getType().toString() + ":" + sm.getOwningPlayer().getUniqueId();
            if (ProtectionStones.isProtectBlockType(ret)) { // try returning uuid type first
                return ret;
            }*/

            // PLAYER_HEAD:name
            return Material.PLAYER_HEAD.toString() + ":" + sm.getOwningPlayer().getName(); // return name if it doesn't exist
        }
        return i.getType().toString();
    }

    public static String getProtectBlockType(Block block) {
        if (block.getType() == Material.PLAYER_HEAD || block.getType() == Material.PLAYER_WALL_HEAD) {

            Skull s = (Skull) block.getState();
            if (s.hasOwner()) {
                if (ProtectionStones.getBlockOptions("PLAYER_HEAD:" + s.getOwningPlayer().getUniqueId().toString()) != null) {
                    // PLAYER_HEAD:base64
                    return Material.PLAYER_HEAD.toString() + ":" + s.getOwningPlayer().getUniqueId().toString();
                } else {
                    // PLAYER_HEAD:name
                    return Material.PLAYER_HEAD.toString() + ":" + s.getOwningPlayer().getName(); // return name if doesn't exist
                }

            } else { // PLAYER_HEAD
                return Material.PLAYER_HEAD.toString();
            }
        } else if (block.getType() == Material.CREEPER_WALL_HEAD) {
            return Material.CREEPER_HEAD.toString();
        } else if (block.getType() == Material.DRAGON_WALL_HEAD) {
            return Material.DRAGON_HEAD.toString();
        } else if (block.getType() == Material.ZOMBIE_WALL_HEAD) {
            return Material.ZOMBIE_HEAD.toString();
        } else {
            return block.getType().toString();
        }
    }

    // for PSProtectBlock types
    public static OfflinePlayer getPlayerFromSkullType(String type) {
        if (type.split(":").length < 2) return null;
        String name = type.split(":")[1];
        try {
            if (name.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
                // UUID
                return Bukkit.getOfflinePlayer(UUID.fromString(name));
            }
        } catch (IllegalArgumentException e) {
            // if there is issue parsing to UUID
        }
        // name
        return Bukkit.getOfflinePlayer(name);
    }

    public static void setHeadType(String psType, Block b) {
        if (psType.split(":").length < 2) return;
        String name = psType.split(":")[1];
        if (name.length() > MAX_USERNAME_LENGTH) {
            blockWithBase64(b, name);
        } else {
            OfflinePlayer op = Bukkit.getOfflinePlayer(psType.split(":")[1]);
            Skull s = (Skull) b.getState();
            s.setOwningPlayer(op);
            s.update();
        }
    }

    public static ItemStack setHeadType(String psType, ItemStack item) {
        String name = psType.split(":")[1];
        if (name.length() > MAX_USERNAME_LENGTH) { // base 64 head
            return Bukkit.getUnsafe().modifyItemStack(item, "{SkullOwner:{Name:\"" + name + "\",Id:\"" + name + "\",Properties:{textures:[{Value:\"" + uuidToBase64Head.get(name) + "\"}]}}}");
        } else { // normal name head
            /*if (name.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
                String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + UUID.fromString(name).toString().replace("-", "");
                String texture = "";
                try {
                    InputStreamReader read = new InputStreamReader(new URL(url).openStream());
                    texture = new JsonParser().parse(read).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                item = Bukkit.getUnsafe().modifyItemStack(item, "{SkullOwner:{Name:\"" + name + "\",Id:\"" + name + "\",Properties:{textures:[{Value:\"" + texture + "\"}]}}}");
            } else {*/
            item = Bukkit.getUnsafe().modifyItemStack(item, "{SkullOwner:{Name:\"" + name + "\"}}");
            return item;
        }
    }

    private static void blockWithBase64(Block block, String uuid) { // TODO
        String base64 = uuidToBase64Head.get(uuid);

        // data command is a terrible idea and not cross-world

        String args = String.format(
                "%d %d %d %s",
                block.getX(),
                block.getY(),
                block.getZ(),
                "{Owner:{Name:\"" + uuid + "\",Id:\"" + uuid + "\",Properties:{textures:[{Value:\"" + base64 + "\"}]}}}"
        );
        Bukkit.getLogger().info(args); // TODO
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute in " + block.getWorld().getName() + " run data merge block " + args);
    }

    public static boolean isBase64PSHead(String type) {
        return type.startsWith("PLAYER_HEAD") && type.split(":").length > 1 && type.split(":")[1].length() > MAX_USERNAME_LENGTH;
    }

    public static String getUUIDFromBase64PS(PSProtectBlock b) {
        String base64 = b.type.split(":")[1];
        return new UUID(base64.hashCode(), base64.hashCode()).toString();
    }
}
