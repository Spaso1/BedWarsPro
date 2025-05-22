package org.ast.bedwarspro.command;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.ast.bedwarspro.BedWarsPro;
import org.ast.bedwarspro.been.Plot;
import org.ast.bedwarspro.been.SerializableLocation;
import org.ast.bedwarspro.mannger.PlotManager;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class PlotCreateCommand implements CommandExecutor {

    private final Map<Player, Location[]> playerSelections;
    private final PlotManager plotManager;
    private final BedWarsPro plugin;
    public PlotCreateCommand(Map<Player, Location[]> playerSelections,PlotManager plotManager,BedWarsPro bedWarsPro) {
        this.playerSelections = playerSelections;
        this.plotManager = plotManager;
        this.plugin = bedWarsPro;

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以使用此命令！");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            player.sendMessage("§a/plot create <name> - 创建一个新的地块");
            player.sendMessage("§a/plot list - List your plots");
            player.sendMessage("§a/plot own <name> add <Username> - 增加联合持有人");
            player.sendMessage("§a/plot own <name> remove <Username> - 删除联合持有人");
            player.sendMessage("§a/plot own <name> - 查看联合持有人");
            player.sendMessage("§a/plot rule <name> <permission> <true/false> - Change plot permissions");
            player.sendMessage("§a/plot tp <name> - Teleport to the plot");
            return true;
        }

        Location[] points = playerSelections.get(player);
        if (args[0].equals("create")) {
            if (args.length < 2) {
                player.sendMessage("§c请提供一个名称！");
                return true;
            }
            if (points == null || points[0] == null || points[1] == null) {
                player.sendMessage("§c请先设置第一点和第二点！");
                return true;
            }
            //检查名称冲突
            for (Plot plot : plotManager.getPlayerPlots(player.getName())) {
                if (plot.getName().equalsIgnoreCase(args[1])) {
                    player.sendMessage("§c地块名称已存在！");
                    return true;
                }
            }
            createCuboid(args[1],player,new SerializableLocation(points[0]), new SerializableLocation(points[1]));

            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            List<Plot> plots = plotManager.getPlayerPlots(player.getName());
            if (plots.isEmpty()) {
                player.sendMessage("§c你没有任何地块！");
                return true;
            }

            player.sendMessage("§a你的地块列表：");
            for (Plot plot : plots) {
                TextComponent plotName = new TextComponent("§b" + plot.getName() + ",Block1:" + plot.getLoc1().toString() + ",Block2:" + plot.getLoc2().toString());
                plotName.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/plot tp " + plot.getName()));
                player.spigot().sendMessage(plotName);
                showCuboid(player, plot.getLoc1(), plot.getLoc2());
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("own") && args.length == 4) {
            String plotName = args[1];
            String action = args[2];
            String username = args[3];
            List<Plot> plots = plotManager.getPlayerPlots(player.getName());
            for (Plot plot : plots) {
                if (plot.getName().equalsIgnoreCase(plotName)) {
                    if (action.equalsIgnoreCase("add")) {
                        plot.getCoOwners().add(username);
                        player.sendMessage("§a" + username + " has been added as a co-owner of " + plotName);
                    } else if (action.equalsIgnoreCase("remove")) {
                        plot.getCoOwners().remove(username);
                        player.sendMessage("§a" + username + " has been removed from co-ownership of " + plotName);
                    }
                    break;
                }
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("own") && args.length == 2) {
            String plotName = args[1];
            List<Plot> plots = plotManager.getPlayerPlots(player.getName());
            for (Plot plot : plots) {
                if (plot.getName().equalsIgnoreCase(plotName)) {
                    player.sendMessage("§a" + plotName + " Co-Owners: " + String.join(", ", plot.getCoOwners()));
                    break;
                }
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("rule") && args.length == 4) {
            String plotName = args[1];
            String permission = args[2];
            boolean value = Boolean.parseBoolean(args[3]);
            List<Plot> plots = plotManager.getPlayerPlots(player.getName());//.forEach(plot -> plot.setPermission(permission, value));
            for (Plot plot : plots) {
                if (plot.getName().equalsIgnoreCase(plotName)) {
                    plot.setPermission(permission, value);
                    break;
                }
            }
            player.sendMessage("§a" + plotName + " Permission " + permission + " set to " + value);
            return true;
        }
        if (args[0].equalsIgnoreCase("tp") && args.length == 2) {
            String plotName = args[1];
            List<Plot> plots = plotManager.getPlayerPlots(player.getName());
            for (Plot plot : plots) {
                if (plot.getName().equalsIgnoreCase(plotName)) {
                    Location center = getPlotCenter(plot.getLoc1(), plot.getLoc2());
                    player.teleport(center);
                    player.sendMessage("§a已传送到地块 " + plotName + " 的中心点！");
                    return true;
                }
            }
            player.sendMessage("§c未找到地块 " + plotName + "！");
            return true;
        }
        return false;
    }

    private void createCuboid(String name,Player player, SerializableLocation loc1, SerializableLocation loc2) {
        //计算多少方块
        int blockCount = 0;
        int minX = (int) Math.min(loc1.getBlockX(), loc2.getBlockX());
        int maxX = (int) Math.max(loc1.getBlockX(), loc2.getBlockX());
        int minY = (int) Math.min(loc1.getBlockY(), loc2.getBlockY()); // Define minY
        int maxY = (int) Math.max(loc1.getBlockY(), loc2.getBlockY());
        int minZ = (int) Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int maxZ = (int) Math.max(loc1.getBlockZ(), loc2.getBlockZ());
        blockCount = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);

        if (blockCount>1000000000) {
            player.sendMessage("§c地块太大了！");
            return;
        }
        if (plugin.getUser(player.getName()).getCoins() >= blockCount){
            BedWarsPro plugin = BedWarsPro.getPlugin(BedWarsPro.class);
            plugin.deductCoins(player,Long.parseLong(String.valueOf(blockCount)));
            player.sendMessage("§a地块创建成功，扣除" + blockCount + "金币");
        }else {
            player.sendMessage("§c金币不足，无法创建地块！");
            return;
        }

        Plot plot = new Plot(name, player.getName(), loc1, loc2);
        plotManager.addPlot(plot);

        player.sendMessage("§aPlot created: " + name);
        playerSelections.remove(player);
    }
    private void showCuboid(Player player, SerializableLocation loc1, SerializableLocation loc2) {
        World world = player.getWorld();
        int minX = (int) Math.min(loc1.getBlockX(), loc2.getBlockX());
        int maxX = (int) Math.max(loc1.getBlockX(), loc2.getBlockX());
        int minY = (int) Math.min(loc1.getBlockY(), loc2.getBlockY()); // Define minY
        int maxY = (int) Math.max(loc1.getBlockY(), loc2.getBlockY());
        int minZ = (int) Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int maxZ = (int) Math.max(loc1.getBlockZ(), loc2.getBlockZ());

        int blockCount = 0;

        // Iterate over the cuboid boundary and display particle effects
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ) {
                        Location particleLocation = new Location(world, x + 0.5, y + 0.5, z + 0.5);
                        world.playEffect(particleLocation, Effect.HAPPY_VILLAGER, 0);
                    }
                    blockCount++;
                }
            }
        }

        player.sendMessage("§aCuboid created with §e" + blockCount + " §ablocks.");
    }
    private Location getPlotCenter(SerializableLocation loc1, SerializableLocation loc2) {
        int centerX = (int) ((loc1.getBlockX() + loc2.getBlockX()) / 2);
        int centerY = (int) (Math.min(loc1.getBlockY(), loc2.getBlockY()) + 1);
        int centerZ = (int) ((loc1.getBlockZ() + loc2.getBlockZ()) / 2);
        return new Location(Bukkit.getWorld(loc1.getWorldName()), centerX, centerY, centerZ);
    }
}