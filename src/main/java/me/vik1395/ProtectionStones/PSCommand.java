package me.vik1395.ProtectionStones;

import me.vik1395.ProtectionStones.commands.*;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PSCommand extends Command {

    protected PSCommand(String name) {
        super(name);
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

    @Override
    public boolean execute(CommandSender s, String label, String[] args) {
        try {

            // can be used from console
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                return ArgReload.argumentReload(s, args);
            } else if (args.length > 0 && args[0].equalsIgnoreCase("admin")) {
                return ArgAdmin.argumentAdmin(s, args);
            }

            if (s instanceof Player) {
                Player p = (Player) s;
                if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
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

                switch (args[0].toLowerCase()) {
                    case "toggle":
                        if (p.hasPermission("protectionstones.toggle")) {
                            if (!ProtectionStones.toggleList.contains(p.getName())) {
                                ProtectionStones.toggleList.add(p.getName());
                                p.sendMessage(PSL.TOGGLE_OFF.msg());
                            } else {
                                ProtectionStones.toggleList.remove(p.getName());
                                p.sendMessage(PSL.TOGGLE_ON.msg());
                            }
                        } else {
                            p.sendMessage(PSL.NO_PERMISSION_TOGGLE.msg());
                        }
                        break;
                    case "count":
                        return ArgCount.argumentCount(p, args);
                    case "region":
                        return ArgRegion.argumentRegion(p, args);
                    case "tp":
                        return ArgTp.argumentTp(p, args);
                    case "home":
                        return ArgTp.argumentTp(p, args);
                    case "unclaim":
                        return ArgUnclaim.argumentUnclaim(p, args);
                    case "add":
                        return ArgAddRemove.template(p, args, "add");
                    case "remove":
                        return ArgAddRemove.template(p, args, "remove");
                    case "addowner":
                        return ArgAddRemove.template(p, args, "addowner");
                    case "removeowner":
                        return ArgAddRemove.template(p, args, "removeowner");
                    case "view":
                        return ArgView.argumentView(p, args);
                    case "unhide":
                        return ArgHideUnhide.template(p, "unhide");
                    case "hide":
                        return ArgHideUnhide.template(p, "hide");
                    case "priority":
                        return ArgPriority.argPriority(p, args);
                    case "flag":
                        return ArgFlag.argumentFlag(p, args);
                    case "info":
                        return ArgInfo.argumentInfo(p, args);
                    case "get":
                        return ArgGet.argumentGet(p, args);
                    case "give":
                        return ArgGive.argumentGive(p, args);
                    case "sethome":
                        return ArgSethome.argumentSethome(p, args);
                    default:
                        PSL.msg(p, PSL.NO_SUCH_COMMAND.msg());
                }
            } else {
                s.sendMessage(ChatColor.RED + "You can only use /ps reload and /ps admin from console.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
