package org.ast.bedwarspro.command;

import org.ast.bedwarspro.BedWarsPro;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HubCommand implements CommandExecutor {

    private final BedWarsPro plugin;

    public HubCommand(BedWarsPro plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("该指令只能由玩家执行");
            return true;
        }

        Player player = (Player) sender;

        String hubWorldName = plugin.getConfig().getString("hub.name", "world");
        World hubWorld = Bukkit.getWorld(hubWorldName);

        if (hubWorld == null) {
            player.sendMessage("§c大厅世界不存在或未加载！");
            return true;
        }

        player.teleport(hubWorld.getSpawnLocation());
        player.sendMessage("§a已传送至大厅世界 §b" + hubWorldName);
        return true;
    }

}
