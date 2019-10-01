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

import dev.espi.protectionstones.ProtectionStones;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.UUID;

public class MiscUtil {
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

    public static String getProtectBlockType(ItemStack i) {
        if (i.getType() == Material.PLAYER_HEAD) {
            String ret = i.getType().toString() + ":" + ((SkullMeta) (i.getItemMeta())).getOwningPlayer().getUniqueId();
            if (ProtectionStones.isProtectBlockType(ret)) { // try returning uuid type first
                return ret;
            }
            return i.getType().toString() + ":" + ((SkullMeta) (i.getItemMeta())).getOwningPlayer().getName(); // return name if it doesn't exist
        }
        return i.getType().toString();
    }

    public static String getProtectBlockType(Block block) {
        if (block.getType() == Material.PLAYER_HEAD || block.getType() == Material.PLAYER_WALL_HEAD) {

            Skull s = (Skull) block.getState();
            if (s.hasOwner()) {
                Bukkit.getLogger().info(s.getOwningPlayer().getName()); // TODO
                String ret = Material.PLAYER_HEAD.toString() + ":" + s.getOwningPlayer().getUniqueId();
                if (ProtectionStones.isProtectBlockType(ret)) { // try returning uuid type first
                    return ret;
                }
                return Material.PLAYER_HEAD.toString() + ":" + s.getOwningPlayer().getName(); // return name if doesn't exist
            } else {
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
        if (name.length() == 180) {
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
        if (name.length() == 180) {
            return itemWithBase64(item, name);
        } else {
            ((SkullMeta)item.getItemMeta()).setOwningPlayer(getPlayerFromSkullType(psType));
            return item;
        }
    }

    private static ItemStack itemWithBase64(ItemStack item, String base64) {
        UUID hashAsId = new UUID(base64.hashCode(), base64.hashCode());
        return Bukkit.getUnsafe().modifyItemStack(item, "{SkullOwner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + base64 + "\"}]}}}");
    }

    private static void blockWithBase64(Block block, String base64) {
        UUID hashAsId = new UUID(base64.hashCode(), base64.hashCode());

        BlockData blockData = Bukkit.getServer().createBlockData(block.getType(), "{Owner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + base64 + "\"}]}}}");
        block.setBlockData(blockData, true);
    }
}
