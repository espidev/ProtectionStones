/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.vik1395.ProtectionStones.ObjectOrientated;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/*

Authors: Vik1395, Dragoboss
Project: ProtectionStones

Copyright 2015

Licensed under Creative CommonsAttribution-ShareAlike 4.0 International Public License (the "License");
You may not use this file except in compliance with the License.

You may obtain a copy of the License at http://creativecommons.org/licenses/by-sa/4.0/legalcode

You may find an abridged version of the License at http://creativecommons.org/licenses/by-sa/4.0/
 */
public class PSMessager {
    ProtectionStones instance = new ProtectionStones().instance();
    PSProtect protect = new PSProtect();
    File localeFile = new File(instance.getDataFolder() + "/locale.yml");
    
    
    public boolean saveLocaleFile() throws IOException {
        if (!(localeFile.exists())){
            protect.copy(instance.getResource("locale.yml"), localeFile);
        }
        return true;
    }
    public String send(CommandSender s, String msg, Boolean global, Player p, String extra){
        YamlConfiguration langFile = YamlConfiguration.loadConfiguration(localeFile);
        String message = langFile.getString(msg);
        message = message.replaceAll("%p", p.getName());
        message = message.replaceAll("%e", extra);
        
        if (global) {
            instance.getServer().broadcastMessage(message);
        } else {
            s.sendMessage(message);
        }
        return "done";
    }
}
