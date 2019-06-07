package dev.espi.ProtectionStones.commands;

import dev.espi.ProtectionStones.PSL;
import dev.espi.ProtectionStones.ProtectionStones;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ArgToggle implements PSCommandArg {

    @Override
    public List<String> getNames() {
        return Collections.singletonList("toggle");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args) {
        Player p = (Player) s;
        if (p.hasPermission("protectionstones.toggle")) {
            if (!ProtectionStones.toggleList.contains(p.getUniqueId())) {
                ProtectionStones.toggleList.add(p.getUniqueId());
                p.sendMessage(PSL.TOGGLE_OFF.msg());
            } else {
                ProtectionStones.toggleList.remove(p.getUniqueId());
                p.sendMessage(PSL.TOGGLE_ON.msg());
            }
        } else {
            p.sendMessage(PSL.NO_PERMISSION_TOGGLE.msg());
        }
        return true;
    }
}
