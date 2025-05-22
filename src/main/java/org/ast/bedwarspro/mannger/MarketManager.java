package org.ast.bedwarspro.mannger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.ast.bedwarspro.been.MarketItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MarketManager {
    private static final List<MarketItem> marketItems = new ArrayList<>();
    private static final File marketFile = new File("plugins/BedWarsPro/market.json");
    private static final Gson gson = new Gson();

    public static void addItem(Player seller, ItemStack item, double price) {
        marketItems.add(new MarketItem(seller.getName(), item, price));
        seller.sendMessage("§aItem listed for sale at §e" + price + " coins!");
        saveMarketItems();
    }

    public static List<MarketItem> getMarketItems() {
        return marketItems;
    }

    public static void removeItem(MarketItem item) {
        marketItems.remove(item);
        saveMarketItems();
    }

    public static void saveMarketItems() {
        try (Writer writer = new FileWriter(marketFile)) {
            gson.toJson(marketItems, writer);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save market items: " + e.getMessage());
        }
    }

    public static void loadMarketItems() {
        if (!marketFile.exists()) return;

        try (Reader reader = new FileReader(marketFile)) {
            Type listType = new TypeToken<List<MarketItem>>() {}.getType();
            List<MarketItem> loadedItems = gson.fromJson(reader, listType);
            if (loadedItems != null) {
                marketItems.clear();
                marketItems.addAll(loadedItems);
            }
        } catch (com.google.gson.JsonSyntaxException e) {
            Bukkit.getLogger().severe("Invalid JSON format in market.json: " + e.getMessage());
            Bukkit.getLogger().severe("Deleting the corrupted file...");
            if (!marketFile.delete()) {
                Bukkit.getLogger().severe("Failed to delete corrupted market.json file.");
            }
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to load market items: " + e.getMessage());
        }
    }
}