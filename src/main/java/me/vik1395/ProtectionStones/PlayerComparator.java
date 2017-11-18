/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.vik1395.ProtectionStones;

import java.util.Comparator;

import org.bukkit.OfflinePlayer;

/**
 * @author Dragoboss
 */
class PlayerComparator implements Comparator<OfflinePlayer> {

    @Override
    public int compare(OfflinePlayer o1, OfflinePlayer o2) {
        return o1.getName().compareTo(o2.getName());
    }
}
