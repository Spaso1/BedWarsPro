package org.ast.bedwarspro.task;

import org.ast.bedwarspro.BedWarsPro;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

public class HubClearTask implements Runnable {

    private final BedWarsPro plugin;

    public HubClearTask(BedWarsPro plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("clear.enabled")) return;

        String hubWorldName = config.getString("hub.name", "world");
        World hubWorld = plugin.getServer().getWorld(hubWorldName);

        if (hubWorld == null) {
            plugin.getLogger().warning("无法找到大厅世界: " + hubWorldName);
            return;
        }

        for (Entity entity : hubWorld.getEntities()) {
            if (entity instanceof Item) {
                entity.remove();
            }
        }

        plugin.getLogger().info("已清除大厅掉落物");

        plugin.saveData();
    }
}
