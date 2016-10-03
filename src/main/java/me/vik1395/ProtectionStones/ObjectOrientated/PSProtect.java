/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.vik1395.ProtectionStones.ObjectOrientated;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;
import me.vik1395.ProtectionStones.Main;
import static me.vik1395.ProtectionStones.Main.uuid;
import org.bukkit.entity.Player;

/*
File: PSProtect.java
Function: The PSProtect is the main executive class, in which resides most of the
object-orientated functions to be used by the other classes to request or process
information. This is further seperated into a few 'sections' and can if required
later be seperated into multiple classes.

Authors: Vik1395, Dragoboss
Project: ProtectionStones

Copyright 2015-2016

Licensed under Creative CommonsAttribution-ShareAlike 4.0 International Public License (the "License");
You may not use this file except in compliance with the License.

You may obtain a copy of the License at http://creativecommons.org/licenses/by-sa/4.0/legalcode

You may find an abridged version of the License at http://creativecommons.org/licenses/by-sa/4.0/
 */
public class PSProtect {
    WorldGuardPlugin wg = (WorldGuardPlugin)Main.wgd;
        
    void copy(InputStream in, File file) throws IOException {
        try (OutputStream out = new FileOutputStream(file)) {
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        in.close();
    }
    public Map<String, ProtectedRegion> getRegionsOwner(Player p) {
        RegionManager rgm = wg.getRegionManager(p.getWorld());
        UUID playerid = p.getUniqueId();
        String playerName = p.getName();
        Map<String, ProtectedRegion> regions = rgm.getRegions();
        Map<String, ProtectedRegion> areOwned = null;
        for (String selected : regions.keySet()) {
            if (uuid) {
                if (regions.get(selected).getOwners().contains(playerid)) {
                    if (regions.get(selected).getId().startsWith("ps")) {
                        areOwned.put(selected, regions.get(selected));
                    }
                }
            } else {
                if (regions.get(selected).getOwners().contains(playerName)) {
                    if (regions.get(selected).getId().startsWith("ps")) {
                        areOwned.put(selected, regions.get(selected));
                    }
                }
            }
        }
        return areOwned;
    }

    /*
    Section: Block-Type Settings
    
    Function: The following functions are used to gain the setting for each Block-Type configured in the config.yml
    */
    public int RegionX(String StoneType) {
        String ConfigString = "Region." + StoneType + ".X Radius";
        int xradius = Main.plugin.getConfig().getInt(ConfigString);
        return xradius;
    }
    public int RegionY(String StoneType) {
        String ConfigString = "Region." + StoneType + ".Y Radius";
        int yradius = Main.plugin.getConfig().getInt(ConfigString);
        return yradius;   
    }
    public int RegionZ(String StoneType){
        String ConfigString = "Region." + StoneType + ".Z Radius";
        int zradius = Main.plugin.getConfig().getInt(ConfigString);
        return zradius;  
    }
    public Boolean AutoHide(String StoneType) {
        String ConfigString = "Region." + StoneType + ".Auto Hide";
        boolean autohide = Main.plugin.getConfig().getBoolean(ConfigString);
        return autohide;
    }
    public boolean NoDrop(String StoneType){
        String ConfigString = "Region." + StoneType + ".No Drop";
        boolean nodrop = Main.plugin.getConfig().getBoolean(ConfigString);
        return nodrop;
    }
    public boolean BlockPiston(String StoneType){
        String ConfigString = "Region." + StoneType + ".Block Piston";
        boolean blockpiston = Main.plugin.getConfig().getBoolean(ConfigString);
        return blockpiston;
    }
    public boolean SilkTouch(String StoneType){
        String ConfigString = "Region." + StoneType + ".Silk Touch";
        boolean silktouch = Main.plugin.getConfig().getBoolean(ConfigString);
        return silktouch;
    }
    public int DefaultPriority(String StoneType){
        String ConfigString = "Region." + StoneType + ".Priority";
        int priority = Main.plugin.getConfig().getInt(ConfigString);
        return priority;
    }
    public int Limit(String StoneType){
        String ConfigString = "Region." + StoneType + ".Limit";
        int limit = Main.plugin.getConfig().getInt(ConfigString);
        return limit;
    }
}
