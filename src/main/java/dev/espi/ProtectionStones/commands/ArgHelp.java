package dev.espi.ProtectionStones.commands;

import dev.espi.ProtectionStones.PSL;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ArgHelp implements PSCommandArg{
    @Override
    public List<String> getNames() {
        return Collections.singletonList("help");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args) {
        Player p = (Player) s;
        p.sendMessage(PSL.HELP.msg());
        sendWithPerm(p, PSL.INFO_HELP.msg(), PSL.INFO_HELP_DESC.msg(), "/ps info", "protectionstones.info");
        sendWithPerm(p, PSL.ADDREMOVE_HELP.msg(), PSL.ADDREMOVE_HELP_DESC.msg(), "/ps", "protectionstones.members");
        sendWithPerm(p, PSL.ADDREMOVE_OWNER_HELP.msg(), PSL.ADDREMOVE_OWNER_HELP_DESC.msg(), "/ps", "protectionstones.owners");
        sendWithPerm(p, PSL.GET_HELP.msg(), PSL.GET_HELP_DESC.msg(), "/ps get", "protectionstones.get");
        sendWithPerm(p, PSL.GIVE_HELP.msg(), PSL.GIVE_HELP_DESC.msg(), "/ps give", "protectionstones.give");
        sendWithPerm(p, PSL.COUNT_HELP.msg(), PSL.COUNT_HELP_DESC.msg(), "/ps count", "protectionstones.count", "protectionstones.count.others");
        sendWithPerm(p, PSL.FLAG_HELP.msg(), PSL.FLAG_HELP_DESC.msg(), "/ps flag", "protectionstones.flags");
        sendWithPerm(p, PSL.HOME_HELP.msg(), PSL.HOME_HELP_DESC.msg(), "/ps home", "protectionstones.home");
        sendWithPerm(p, PSL.SETHOME_HELP.msg(), PSL.SETHOME_HELP_DESC.msg(), "/ps sethome", "protectionstones.sethome");
        sendWithPerm(p, PSL.TP_HELP.msg(), PSL.TP_HELP_DESC.msg(), "/ps tp", "protectionstones.tp");
        sendWithPerm(p, PSL.VISIBILITY_HIDE_HELP.msg(), PSL.VISIBILITY_HIDE_HELP_DESC.msg(), "/ps hide", "protectionstones.hide");
        sendWithPerm(p, PSL.VISIBILITY_UNHIDE_HELP.msg(), PSL.VISIBILITY_UNHIDE_HELP_DESC.msg(), "/ps unhide", "protectionstones.unhide");
        sendWithPerm(p, PSL.TOGGLE_HELP.msg(), PSL.TOGGLE_HELP_DESC.msg(), "/ps toggle", "protectionstones.toggle");
        sendWithPerm(p, PSL.VIEW_HELP.msg(), PSL.VIEW_HELP_DESC.msg(), "/ps view", "protectionstones.view");
        sendWithPerm(p, PSL.UNCLAIM_HELP.msg(), PSL.UNCLAIM_HELP_DESC.msg(), "/ps unclaim", "protectionstones.unclaim");
        sendWithPerm(p, PSL.PRIORITY_HELP.msg(), PSL.PRIORITY_HELP_DESC.msg(), "/ps priority", "protectionstones.priority");
        sendWithPerm(p, PSL.REGION_HELP.msg(), PSL.REGION_HELP_DESC.msg(), "/ps region", "protectionstones.region");
        sendWithPerm(p, PSL.ADMIN_HELP.msg(), PSL.ADMIN_HELP_DESC.msg(), "/ps admin", "protectionstones.admin");
        sendWithPerm(p, PSL.RELOAD_HELP.msg(), PSL.RELOAD_HELP_DESC.msg(), "/ps reload", "protectionstones.admin");
        return true;
    }

    private static void sendWithPerm(Player p, String msg, String desc, String cmd, String... permission) {
        if (msg.equals("")) return;
        for (String perm : permission) {
            if (p.hasPermission(perm)) {
                TextComponent m = new TextComponent(msg);
                m.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, cmd));
                m.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(desc).create()));
                p.spigot().sendMessage(m);
                break;
            }
        }
    }
}
