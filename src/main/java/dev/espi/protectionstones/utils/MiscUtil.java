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
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.UUID;

public class MiscUtil {
    private final static int BASE64LENGTH = 180;
    private final static NamespacedKey BASE64SKULL_TAG = new NamespacedKey(ProtectionStones.getInstance(), "base64Skull");

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

    // WARNING: base64 items currently use custom tags in order to identify skull owners without NMS
    public static String getProtectBlockType(ItemStack i) {
        if (i.getType() == Material.PLAYER_HEAD || i.getType() == Material.LEGACY_SKULL_ITEM) {

            SkullMeta sm = (SkullMeta) i.getItemMeta();

            // PLAYER_HEAD
            if (!sm.hasOwner()) {
                // PLAYER_HEAD:base64
                if (i.getItemMeta().getCustomTagContainer().hasCustomTag(BASE64SKULL_TAG, ItemTagType.STRING)) {
                    return Material.PLAYER_HEAD.toString() + ":" + i.getItemMeta().getCustomTagContainer().getCustomTag(BASE64SKULL_TAG, ItemTagType.STRING);
                }
                return Material.PLAYER_HEAD.toString();
            }

            // PLAYER_HEAD:UUID
            String ret = i.getType().toString() + ":" + sm.getOwningPlayer().getUniqueId();
            if (ProtectionStones.isProtectBlockType(ret)) { // try returning uuid type first
                return ret;
            }
            // PLAYER_HEAD:name
            return i.getType().toString() + ":" + sm.getOwningPlayer().getName(); // return name if it doesn't exist
        }
        return i.getType().toString();
    }

    public static String getProtectBlockType(Block block) {
        if (block.getType() == Material.PLAYER_HEAD || block.getType() == Material.PLAYER_WALL_HEAD) {

            Skull s = (Skull) block.getState();
            if (s.hasOwner()) {
                // PLAYER_HEAD:UUID or PLAYER_HEAD:base64
                String ret = Material.PLAYER_HEAD.toString() + ":" + s.getOwningPlayer().getUniqueId();
                if (ProtectionStones.isProtectBlockType(ret)) { // try returning uuid type first
                    return ret;
                }

                // PLAYER_HEAD:name
                return Material.PLAYER_HEAD.toString() + ":" + s.getOwningPlayer().getName(); // return name if doesn't exist
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
        if (name.length() == BASE64LENGTH) {
            blockWithBase64(b, name);
        } else {
            OfflinePlayer op = getPlayerFromSkullType(psType);
            Skull s = (Skull) b.getState();
            s.setOwningPlayer(op);
            s.update();
        }
    }

    public static ItemStack setHeadType(String psType, ItemStack item) {
        String name = psType.split(":")[1];
        if (name.length() == BASE64LENGTH) {
            return itemWithBase64(item, name);
        } else {
            ((SkullMeta) item.getItemMeta()).setOwningPlayer(getPlayerFromSkullType(psType));
            return item;
        }
    }

    // WARNING: base64 items currently use custom tags in order to identify skull owners without NMS
    private static ItemStack itemWithBase64(ItemStack item, String base64) {
        UUID hashAsId = new UUID(base64.hashCode(), base64.hashCode());
        item = Bukkit.getUnsafe().modifyItemStack(item, "{SkullOwner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + base64 + "\"}]}}}");
        ItemMeta im = item.getItemMeta();
        im.getCustomTagContainer().setCustomTag(BASE64SKULL_TAG, ItemTagType.STRING, base64);
        item.setItemMeta(im);
        return item;
    }

    private static void blockWithBase64(Block block, String base64) { // TODO
        UUID hashAsId = new UUID(base64.hashCode(), base64.hashCode());

        //BlockData blockData = Bukkit.getServer().createBlockData(block.getType(), "{Owner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + base64 + "\"}]}}}");
        //block.setBlockData(blockData, true);
        Bukkit.getLogger().info(block.getBlockData().getAsString(false)); // TODO

        // data command is a terrible idea and not cross-world
        String args = String.format(
                "%d %d %d %s",
                block.getX(),
                block.getY(),
                block.getZ(),
                "{Owner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + base64 + "\"}]}}}"
        );
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "data merge block " + args);
    }

    public static boolean isBase64PSHead(String type) {
        return type.startsWith("PLAYER_HEAD") && type.split(":").length > 1 && type.split(":")[1].length() == BASE64LENGTH;
    }

    public static String getUUIDFromBase64PS(PSProtectBlock b) {
        String base64 = b.type.split(":")[1];
        return new UUID(base64.hashCode(), base64.hashCode()).toString();
    }
}
