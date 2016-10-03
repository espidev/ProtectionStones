/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.vik1395.ProtectionStones.ObjectOrientated;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachmentInfo;

/*
File: PSPerm.java
Function: The PSPerm class contains enums of permission nodes and the functions
necessary for the rest of the plugin to check permissions of players.

Authors: Vik1395, Dragoboss
Project: ProtectionStones

Copyright 2015-2016

Licensed under Creative CommonsAttribution-ShareAlike 4.0 International Public License (the "License");
You may not use this file except in compliance with the License.

You may obtain a copy of the License at http://creativecommons.org/licenses/by-sa/4.0/legalcode

You may find an abridged version of the License at http://creativecommons.org/licenses/by-sa/4.0/
 */
public enum PSPerm {
// ~~~~~~~~~~~~~~~~~~~~~~~~~ ENUMS ~~~~~~~~~~~~~~~~~~~~~~~~ //
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ //
    CREATE (new Permission("ProtectionStones.create")),
    DESTROY (new Permission("ProtectionStones.destroy")),
    VIEW (new Permission("ProtectionStones.view")),
    INFO (new Permission("ProtectionStones.info")),
    HIDE (new Permission("ProtectionStones.hide")),
    UNHIDE (new Permission("ProtectionStones.unhide")),
    HOME (new Permission("ProtectionStones.home")),
    TP (new Permission("ProtectionStones.tp")),
    PRIORITY (new Permission("ProtectionStones.priority")),
    OWNERS (new Permission("ProtectionStones.owners")),
    MEMBERS (new Permission("ProtectionStones.members")),
    FLAGS (new Permission("ProtectionStones.flags")),
    TOGGLE (new Permission("ProtectionStones.toggle")),
    ADMIN (new Permission("ProtectionStones.admin"));
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ //
    
    // Constructor
    PSPerm(Permission permission) {
    }
    
    public boolean has(Player p, PSPerm perm) {
        boolean has = false;
        
        if (p.hasPermission(perm.name())) {
            has = true;
        }
        return has;
    }
    public int getLimit(Player p, Material block, String blockData) {
        int limit = -1;
        if (!(block == null)) {
            for(PermissionAttachmentInfo rawperm : p.getEffectivePermissions()) {
                String perms = rawperm.getPermission();
                String node = null;
                if (blockData != null) {
                    node = "protectionstones.limit." + block.name() + "-" + blockData;
                } else {
                    node = "protectionstones.limit." + block.name();
                }
                if(perms.startsWith(node)) {
                    try {
                        int allow = Integer.parseInt(perms.split("\\.")[3]);
                        if(allow>limit) {
                            if (!(has(p, PSPerm.ADMIN))) {
                                limit = allow;
                            } else {
                                limit = -1;
                            }
                        }
                    } catch (Exception er) {
                        limit = -1;
                    }
                }
            }
        } else {
            for(PermissionAttachmentInfo rawperm : p.getEffectivePermissions()) {
                String perms = rawperm.getPermission();
                if(perms.startsWith("protectionstones.limit")) {
                    try {
                        int allow = Integer.parseInt(perms.substring(23));
                        if(allow>limit) {
                            if (!(has(p, PSPerm.ADMIN))) {
                                limit = allow;
                            } else {
                                limit = -1;
                            }
                        }
                    } catch (Exception er) {
                        limit = -1;
                    }
                }
            }
        }
        return limit;
    }
}
