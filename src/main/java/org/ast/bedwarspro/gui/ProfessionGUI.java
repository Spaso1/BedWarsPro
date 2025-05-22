package org.ast.bedwarspro.gui;

import org.ast.bedwarspro.BedWarsPro;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ProfessionGUI {
    public static void openProfessionGUI(BedWarsPro plugin, Player player) {
        plugin.asyncReadPlayFile();

        Inventory gui = getProfessionGUI(plugin, player);
        player.openInventory(gui);

    }

    public static Inventory getProfessionGUI(BedWarsPro plugin, Player player) {
        Inventory gui = Bukkit.createInventory(null, 9 * 3, "选择你的职业");

        ConfigurationSection professionsSection = plugin.getConfig().getConfigurationSection("professions");
        if (professionsSection == null) return gui;

        for (String key : professionsSection.getKeys(false)) {
            ConfigurationSection profession = professionsSection.getConfigurationSection(key);
            if (profession == null) continue;

            String name = profession.getString("name", "未知职业");
            Material icon = Material.getMaterial(profession.getString("icon", "STONE"));
            List<String> description = profession.getStringList("description");
            int cost = profession.getInt("cost", 3);

            ItemStack item = new ItemStack(icon != null ? icon : Material.STONE);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(name);
                meta.setLore(description);
                item.setItemMeta(meta);
            }

            gui.addItem(item);
        }

        return gui;
    }
}
