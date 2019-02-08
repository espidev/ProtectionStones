/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.vik1395.ProtectionStones;

/**
 * @author Dragoboss
 */
public class StoneTypeData {
    public int RegionX(String StoneType) {
        String ConfigString = "Region." + StoneType + ".X Radius";
        int xradius = ProtectionStones.plugin.getConfig().getInt(ConfigString);
        return xradius;
    }

    public int RegionY(String StoneType) {
        String ConfigString = "Region." + StoneType + ".Y Radius";
        int yradius = ProtectionStones.plugin.getConfig().getInt(ConfigString);
        return yradius;   
    }

    public int RegionZ(String StoneType) {
        String ConfigString = "Region." + StoneType + ".Z Radius";
        int zradius = ProtectionStones.plugin.getConfig().getInt(ConfigString);
        return zradius;  
    }

    public Boolean AutoHide(String StoneType) {
        String ConfigString = "Region." + StoneType + ".Auto Hide";
        boolean autohide = ProtectionStones.plugin.getConfig().getBoolean(ConfigString);
        return autohide;
    }

    public boolean NoDrop(String StoneType) {
        String ConfigString = "Region." + StoneType + ".No Drop";
        boolean nodrop = ProtectionStones.plugin.getConfig().getBoolean(ConfigString);
        return nodrop;
    }

    public boolean BlockPiston(String StoneType) {
        String ConfigString = "Region." + StoneType + ".Block Piston";
        boolean blockpiston = ProtectionStones.plugin.getConfig().getBoolean(ConfigString);
        return blockpiston;
    }

    public boolean SilkTouch(String StoneType) {
        String ConfigString = "Region." + StoneType + ".Silk Touch";
        boolean silktouch = ProtectionStones.plugin.getConfig().getBoolean(ConfigString);
        return silktouch;
    }

    public int DefaultPriority(String StoneType) {
        String ConfigString = "Region." + StoneType + ".Priority";
        int priority = ProtectionStones.plugin.getConfig().getInt(ConfigString);
        return priority;
    }
}