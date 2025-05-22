package org.ast.bedwarspro.gui;

import org.ast.bedwarspro.been.MarketItem;
import org.ast.bedwarspro.mannger.MarketManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MarketGUI {
    private static final int ITEMS_PER_PAGE = 45; // 5 rows for items, 1 row for navigation

    public static void openMarket(Player player, int page) {
        List<MarketItem> items = MarketManager.getMarketItems();
        int totalPages = (int) Math.ceil((double) items.size() / ITEMS_PER_PAGE);

        // Ensure the page is within bounds
        page = Math.max(0, Math.min(page, totalPages - 1));

        Inventory market = Bukkit.createInventory(null, 54, "Market - Page " + (page + 1));

        // Add items for the current page
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, items.size());
        for (int i = startIndex; i < endIndex; i++) {
            MarketItem marketItem = items.get(i);
            ItemStack item = marketItem.toItemStack(); // Deserialize the item
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.add("§7Seller: §e" + marketItem.getSeller());
            lore.add("§7Price: §a" + marketItem.getPrice() + " coins");
            lore.add("§eClick to purchase!");
            meta.setLore(lore);
            item.setItemMeta(meta);
            market.setItem(i - startIndex, item);
        }

        // Add navigation items
        if (page > 0) {
            market.setItem(45, createNavigationItem("§aPrevious Page", "§7Page " + page, Material.ARROW));
        }
        if (page < totalPages - 1) {
            market.setItem(53, createNavigationItem("§aNext Page", "§7Page " + (page + 2), Material.ARROW));
        }

        player.openInventory(market);
    }

    private static ItemStack createNavigationItem(String displayName, String loreText, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        List<String> lore = new ArrayList<>();
        lore.add(loreText);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static void handleMarketClick(Player player, ItemStack clicked, int currentPage) {
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String displayName = meta.getDisplayName();
        if (displayName.equals("§aNext Page")) {
            openMarket(player, currentPage + 1);
        } else if (displayName.equals("§aPrevious Page")) {
            openMarket(player, currentPage - 1);
        } else {
            // Handle item purchase logic here
        }
    }
}