package dev.espi.ProtectionStones.commands;

import dev.espi.ProtectionStones.PSL;
import dev.espi.ProtectionStones.PSRegion;
import dev.espi.ProtectionStones.ProtectionStones;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ArgName implements PSCommandArg {
    @Override
    public List<String> getNames() {
        return Collections.singletonList("name");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args) {
        if (!s.hasPermission("protectionstones.name")) {
            PSL.msg(s, PSL.NO_PERMISSION_NAME.msg());
            return true;
        }
        Player p = (Player) s;
        PSRegion r = ProtectionStones.getPSRegion(p.getLocation());
        return true;
    }

}

