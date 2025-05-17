package org.ast.bedwarspro.storage;

import org.ast.bedwarspro.BedWarsPro;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class InventoryStorage {
    private final BedWarsPro plugin = BedWarsPro.getPlugin(BedWarsPro.class);
    private final File dataFolder;

    public InventoryStorage(File dataFolder) {
        this.dataFolder = new File(dataFolder, "group-inventory");
        if (!this.dataFolder.exists()) {
            this.dataFolder.mkdirs();
        }
    }

    public void saveInventory2(UUID uuid, String group, ItemStack[] inventory) {
        File file = new File(dataFolder, uuid + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        List<Map<String, Object>> itemMaps = new ArrayList<>();
        for (ItemStack item : inventory) {
            if (item == null || item.getType() == Material.AIR) {
                itemMaps.add(null);
            } else {
                itemMaps.add(item.serialize());
            }
        }

        config.set("inventory." + group, itemMaps);

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void saveInventory(UUID uuid, String group, ItemStack[] inventory) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveInventory2(uuid, group, inventory));
    }

    public ItemStack[] loadInventory(UUID uuid, String group) {
        File file = new File(dataFolder, uuid + ".yml");
        if (!file.exists()) return null;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemMaps = (List<Map<String, Object>>) config.get("inventory." + group);

        if (itemMaps == null) return null;

        ItemStack[] result = new ItemStack[itemMaps.size()];
        for (int i = 0; i < itemMaps.size(); i++) {
            Map<String, Object> map = itemMaps.get(i);
            if (map == null) {
                result[i] = new ItemStack(Material.AIR);
            } else {
                result[i] = ItemStack.deserialize(map);
            }
        }

        return result;
    }
}
