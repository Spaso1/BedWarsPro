package org.ast.bedwarspro.listener;
import org.ast.bedwarspro.BedWarsPro;
import org.ast.bedwarspro.BedWarsPro;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class HubProtectionListener implements Listener {

    private final BedWarsPro plugin;

    public HubProtectionListener(BedWarsPro plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        handleProtection(event, event.getPlayer(), event.getBlock().getWorld());
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        handleProtection(event, event.getPlayer(), event.getBlock().getWorld());
    }

    private void handleProtection(BlockPlaceEvent event, Player player, World world) {
        FileConfiguration config = plugin.getConfig();
        if (!config.isConfigurationSection("hub")) return;

        String hubName = config.getString("hub.name", "");
        boolean isProtected = config.getBoolean("hub.protected", false);

        if (isProtected && world.getName().equals(hubName)) {
            if (!player.isOp() && !player.hasPermission("bedwarspro.hub.bypass")) {
                event.setCancelled(true);
                player.sendMessage("§c你没有权限在此世界破坏或放置方块！");
            }
        }
    }
    private void handleProtection(BlockBreakEvent event, Player player, World world) {
        FileConfiguration config = plugin.getConfig();
        if (!config.isConfigurationSection("hub")) return;

        String hubName = config.getString("hub.name", "");
        boolean isProtected = config.getBoolean("hub.protected", false);

        if (isProtected && world.getName().equals(hubName)) {
            if (!player.isOp() && !player.hasPermission("bedwarspro.hub.bypass")) {
                event.setCancelled(true);
                player.sendMessage("§c你没有权限在此世界破坏或放置方块！");
            }
        }
    }
}
