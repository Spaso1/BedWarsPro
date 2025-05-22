package org.ast.bedwarspro.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OfficialShopGUI {
    private static final Map<Material, Double> prices = new HashMap<>();

    static {
        prices.put(Material.LOG, 60.0);
        prices.put(Material.IRON_INGOT, 500.0);
        prices.put(Material.GOLD_INGOT, 700.0);
        prices.put(Material.EMERALD, 3000.0);
    }

    public static void openShop(Player player) {
        Inventory shop = Bukkit.createInventory(null, 27, "Official Shop");

        int slot = 0;
        for (Map.Entry<Material, Double> entry : prices.entrySet()) {
            ItemStack item = new ItemStack(entry.getKey());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§aSell " + entry.getKey().name());
            List<String> lore = new ArrayList<>();
            lore.add("§7Price: §a" + entry.getValue() + " coins");
            lore.add("§eClick to sell!");
            meta.setLore(lore);
            item.setItemMeta(meta);

            //设置数量64
            item.setAmount(64);
            shop.setItem(slot++, item);
        }

        player.openInventory(shop);
    }

    public static double getPrice(Material material) {
        return prices.getOrDefault(material, 0.0);
    }
}