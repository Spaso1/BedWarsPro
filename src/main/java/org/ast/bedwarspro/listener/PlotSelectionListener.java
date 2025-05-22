package org.ast.bedwarspro.listener;

import com.comphenix.protocol.wrappers.EnumWrappers;
import org.ast.bedwarspro.been.Plot;
import org.ast.bedwarspro.been.SerializableLocation;
import org.ast.bedwarspro.mannger.PlotManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlotSelectionListener implements Listener {
    private final PlotManager plotManager;

    public PlotSelectionListener(PlotManager plotManager) {
        this.plotManager = plotManager;
    }
    private final Map<Player, Location[]> playerSelections = new HashMap<>();
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!player.getWorld().getName().equals("bps_w")) {
            return; // Only works in the specified world
        }

        Location blockLocation = event.getBlock().getLocation();
        Plot plot = plotManager.getPlotAtLocation(blockLocation);

        if (plot != null && !plot.getOwner().equals(player.getName())) {
            // Check if the player is a co-owner
            if (!plot.getCoOwners().contains(player.getName())) {
                if (!plot.getPermissions().getOrDefault("allowBreak", false)) {
                    player.sendMessage("§cYou are not allowed to break blocks in this plot!");
                    showCuboid(player, plot.getLoc1(), plot.getLoc2());
                    event.setCancelled(true);
                }
            }
        }
    }
    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        List<Block> pushedBlocks = event.getBlocks();
        Location pistonLocation = event.getBlock().getLocation();

        for (Block block : pushedBlocks) {
            Location blockLocation = block.getLocation();
            Plot pistonPlot = plotManager.getPlotAtLocation(pistonLocation);
            Plot blockPlot = plotManager.getPlotAtLocation(blockLocation);

            // Cancel if the piston is outside a plot and pushes blocks inside a plot
            if (pistonPlot == null && blockPlot != null) {
                event.setCancelled(true);
                return;
            }

            // Cancel if the piston is in a different plot than the block being pushed
            if (pistonPlot != null && blockPlot != null && !pistonPlot.equals(blockPlot)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        List<Block> retractedBlocks = event.getBlocks();
        Location pistonLocation = event.getBlock().getLocation();

        for (Block block : retractedBlocks) {
            Location blockLocation = block.getLocation();
            Plot pistonPlot = plotManager.getPlotAtLocation(pistonLocation);
            Plot blockPlot = plotManager.getPlotAtLocation(blockLocation);

            // Cancel if the piston is outside a plot and pulls blocks from inside a plot
            if (pistonPlot == null && blockPlot != null) {
                event.setCancelled(true);
                return;
            }

            // Cancel if the piston is in a different plot than the block being pulled
            if (pistonPlot != null && blockPlot != null && !pistonPlot.equals(blockPlot)) {
                event.setCancelled(true);
                return;
            }
        }
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!player.getWorld().getName().equals("bps_w")) {
            return; // Only works in the specified world
        }

        Location blockLocation = event.getBlock().getLocation();
        Plot plot = plotManager.getPlotAtLocation(blockLocation);

        if (plot != null && !plot.getOwner().equals(player.getName())) {
            // Check if the player is a co-owner
            if (!plot.getCoOwners().contains(player.getName())) {
                if (!plot.getPermissions().getOrDefault("allowBuild", false)) {
                    player.sendMessage("§cYou are not allowed to place blocks in this plot!");
                    showCuboid(player, plot.getLoc1(), plot.getLoc2());
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!player.getWorld().getName().equals("bps_w")) {
            return; // Only works in the specified world
        }

        Plot plot = plotManager.getPlotAtLocation(player.getLocation());
        if (plot != null && !plot.getOwner().equals(player.getName())) {
            // Check if the player is a co-owner
            if (!plot.getCoOwners().contains(player.getName())) {
                sendActionBar(player, "§aWelcome to §e" + plot.getName() + "!");
            }
        }
    }

    // Method to send an action bar message using packets
    private void sendActionBar(Player player, String message) {
        try {
            Object chatComponent = Class.forName("net.minecraft.server.v1_8_R3.ChatComponentText")
                    .getConstructor(String.class)
                    .newInstance(message);
            Object packet = Class.forName("net.minecraft.server.v1_8_R3.PacketPlayOutChat")
                    .getConstructor(Class.forName("net.minecraft.server.v1_8_R3.IChatBaseComponent"), byte.class)
                    .newInstance(chatComponent, (byte) 2);
            Object playerConnection = player.getClass().getMethod("getHandle")
                    .invoke(player)
                    .getClass()
                    .getField("playerConnection")
                    .get(player.getClass().getMethod("getHandle").invoke(player));
            playerConnection.getClass().getMethod("sendPacket", Class.forName("net.minecraft.server.v1_8_R3.Packet"))
                    .invoke(playerConnection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.getWorld().getName().equals("bps_w")) {
            return; // Only works in the overworld
        }

        Location clickedBlock = event.getClickedBlock() != null ? event.getClickedBlock().getLocation() : null;


        Plot plot = plotManager.getPlotAtLocation(clickedBlock);

        if (plot != null && !plot.getOwner().equals(player.getName())) {
            // Check if the player is a co-owner
            if (!plot.getCoOwners().contains(player.getName())) {
                if (!plot.getPermissions().getOrDefault("allowUse", false)) {
                    player.sendMessage("§c你不被允许与其交互!");
                    showCuboid(player, plot.getLoc1(), plot.getLoc2());
                    event.setCancelled(true);
                }
            }
        }


        // Check if the player is holding a wooden hoe
        if (player.getItemInHand().getType() != Material.WOOD_HOE) {
            return;
        }

        if (clickedBlock == null) return;

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            // Define the first point
            //判断手上是不是,木头锄头
            playerSelections.putIfAbsent(player, new Location[2]);
            playerSelections.get(player)[0] = clickedBlock;
            player.sendMessage("§aFirst point set: " + formatLocation(clickedBlock));
            event.setCancelled(true);
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            // Define the second point
            playerSelections.putIfAbsent(player, new Location[2]);
            playerSelections.get(player)[1] = clickedBlock;
            player.sendMessage("§aSecond point set: " + formatLocation(clickedBlock));
            event.setCancelled(true);
            player.sendMessage("§a/plot create <name> - 创建一个新的地块");
            player.sendMessage("§a/plot list - List your plots");
            player.sendMessage("§a/plot own <name> add <Username> - 增加联合持有人");
            player.sendMessage("§a/plot own <name> remove <Username> - 删除联合持有人");
            player.sendMessage("§a/plot own <name> - 查看联合持有人");
            player.sendMessage("§a/plot rule <name> <permission> <true/false> - Change plot permissions");
            // If both points are set, show the cuboid and calculate block count
            Location[] points = playerSelections.get(player);
            if (points[0] != null && points[1] != null) {
                showCuboid(player, points[0], points[1]);
            }
        }
    }

    private void showCuboid(Player player, Location loc1, Location loc2) {
        World world = loc1.getWorld();
        if (world == null) return;

        int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        int minY = Math.min(loc1.getBlockY(), loc2.getBlockY()); // Define minY
        int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
        int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());

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
    private void showCuboid(Player player, SerializableLocation loc1, SerializableLocation loc2) {
        World world = player.getWorld();
        if (world == null) return;

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
    private String formatLocation(Location loc) {
        return "X: " + loc.getBlockX() + ", Y: " + loc.getBlockY() + ", Z: " + loc.getBlockZ();
    }

    public Map<Player, Location[]> getPlayerSelections() {
        return playerSelections;
    }
}