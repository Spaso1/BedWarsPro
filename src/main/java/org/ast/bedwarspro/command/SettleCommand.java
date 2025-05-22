package org.ast.bedwarspro.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SettleCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        String cmd = strings[0];
        Player player = (Player) commandSender;

        if (cmd.equals("create")) {

        }
        if (cmd.equals("remove")) {

        }

        if (cmd.equals("list")) {

        }
        if (cmd.equals("tp")) {

        }
        //显示帮助
        commandSender.sendMessage("§b§l地标");
        commandSender.sendMessage("§7/settle create <名称>");
        commandSender.sendMessage("§7/settle remove <名称>");
        commandSender.sendMessage("§7/settle list");
        commandSender.sendMessage("§7/settle tp <名称>");

        return false;
    }
}
