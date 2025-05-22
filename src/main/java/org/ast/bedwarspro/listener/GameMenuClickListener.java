package org.ast.bedwarspro.listener;

import org.ast.bedwarspro.BedWarsPro;
import org.ast.bedwarspro.been.MarketItem;
import org.ast.bedwarspro.gui.GameMenuGUI;
import org.ast.bedwarspro.gui.MarketGUI;
import org.ast.bedwarspro.gui.OfficialShopGUI;
import org.ast.bedwarspro.mannger.MarketManager;
import org.bukkit.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Random;

public class GameMenuClickListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!event.getAction().toString().contains("RIGHT_CLICK")) return;

        if (player.getItemInHand() != null &&
                player.getItemInHand().getType() == Material.getMaterial("CLOCK")) {

            event.setCancelled(true);
            player.openInventory(createGameMenuGUI());
        }
    }
    // 创建 GUI（可选）
    private Inventory createGameMenuGUI() {
        return GameMenuGUI.createGameMenuGUI(); // 假设你已有这个类
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null || event.getCurrentItem() == null) return;

        String title = event.getView().getTitle();
        if (title.equals("小游戏菜单")) {
            event.setCancelled(true); // 防止玩家拿走物品

            if (event.getCurrentItem().getType() != Material.getMaterial("GLASS")) {
                String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
                GameMenuGUI.handleItemClick((org.bukkit.entity.Player) event.getWhoClicked(), itemName);
            }
        }
        // ✅ 安全方式获取玩家
        HumanEntity clicker = event.getWhoClicked();
        if (!(clicker instanceof Player)) {
            event.setCancelled(true); // 或直接 return;
            return;
        }
        Player player = (Player) clicker;

        if ("生存世界菜单".equals(title)) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

            switch (event.getRawSlot()) {
                case 10:
                    teleportToWorld(player, "bps_w"); // 主世界
                    break;
                case 12:
                    teleportToWorld(player, "bps_w_n"); // 地狱
                    break;
                case 14:
                    teleportToWorld(player, "bps_w_theEnd"); // 末地
                case 16:
                    teleportToWorld(player, "bps_w_s"); // 末地
                    break;
            }
        }
        if ("附魔商店".equals(title)) {
            event.setCancelled(true); // 阻止拿走物品

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() != Material.ENCHANTED_BOOK) return;

            ItemMeta meta = clicked.getItemMeta();
            if (meta == null || !meta.hasLore()) return;

            for (String loreLine : meta.getLore()) {
                if (loreLine.startsWith("§8enchant_key:")) {
                    String enchantKey = loreLine.replace("§8enchant_key:", "");
                    int level = 10;
                    long cost = Long.parseLong(meta.getLore().get(meta.getLore().indexOf(loreLine) + 2).replace("§8cost:", ""));

                    org.bukkit.enchantments.Enchantment enchantment = org.bukkit.enchantments.Enchantment.getByName(enchantKey);
                    if (enchantment == null) {
                        player.sendMessage("§c无效的附魔类型");
                        return;
                    }
                    BedWarsPro plugin = BedWarsPro.getPlugin(BedWarsPro.class);
                    // 检查金币
                    if (!plugin.hasEnoughCoins(player, cost)) {
                        player.sendMessage("§c你的金币不足");
                        return;
                    }

                    plugin.deductCoins(player, cost);

                    ItemStack handItem = player.getItemInHand();
                    if (handItem == null || handItem.getType() == Material.AIR) {
                        player.sendMessage("§c请手持一个物品");
                        return;
                    }

                    // 添加附魔（强制覆盖已有同名附魔）
                    if (handItem.containsEnchantment(enchantment)) {
                        handItem.removeEnchantment(enchantment);
                    }
                    handItem.addUnsafeEnchantment(enchantment, level);
                    player.updateInventory();
                    player.sendMessage("§a已成功为你的物品添加 §b" + ChatColor.stripColor(meta.getDisplayName()));
                    break;
                }
            }
        }
        // Check if the inventory is the Market GUI
        ItemStack clickedItem = event.getCurrentItem();
        if (title.startsWith("Market - Page")) {
            event.setCancelled(true); // Prevent item movement

            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            ItemMeta meta = clickedItem.getItemMeta();
            if (meta == null || !meta.hasLore()) return;

            List<String> lore = meta.getLore();
            if (lore.size() < 2) return;

            // Extract seller and price from lore
            String seller = lore.get(0).replace("§7Seller: §e", "");
            double price;
            try {
                price = Double.parseDouble(lore.get(1).replace("§7Price: §a", "").replace(" coins", ""));
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid price format!");
                return;
            }

            // Find the corresponding MarketItem
            MarketItem marketItem = MarketManager.getMarketItems().stream()
                    .filter(item -> item.getSeller().equals(seller) && item.getPrice() == price)
                    .findFirst()
                    .orElse(null);

            if (marketItem == null) {
                player.sendMessage("§cThis item is no longer available!");
                return;
            }

            // Check if the player has enough coins
            BedWarsPro plugin = BedWarsPro.getPlugin(BedWarsPro.class);
            long playerCoins = plugin.userCoins.getOrDefault(player.getName(), 0L);
            if (playerCoins < price) {
                player.sendMessage("§cYou do not have enough coins!");
                return;
            }

            // Deduct coins and transfer the item
            plugin.userCoins.put(player.getName(), playerCoins - (long) price);
            plugin.userCoins.put(seller, plugin.userCoins.getOrDefault(seller, 0L) + (long) price);

            player.sendMessage("§aYou purchased " + clickedItem.getType() + " for §e" + price + " coins!");
            MarketManager.removeItem(marketItem);

            // Add the purchased item to the player's inventory
            ItemStack purchasedItem = marketItem.toItemStack();
            player.getInventory().addItem(purchasedItem);

            // Reopen the market GUI
            int currentPage = Integer.parseInt(title.split(" ")[3]) - 1;
            MarketGUI.openMarket(player, currentPage);
        }

        // Check if the inventory is the Official Shop GUI
        if (title.equals("Official Shop")) {
            event.setCancelled(true); // Prevent item movement
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            // Handle selling logic
            Material material = clickedItem.getType();
            double price = OfficialShopGUI.getPrice(material);

            if (price > 0) {
                // Example: Add coins and remove the item from the player's inventory
                player.sendMessage("§aYou sold " + material.name() + " for §e" + price + " coins!");

                // Calculate the total amount of the material in the player's inventory
                int totalAmount = player.getInventory().all(material).values().stream()
                        .mapToInt(ItemStack::getAmount)
                        .sum();

                if (totalAmount < 64) {
                    player.sendMessage("§c你没有足够的物品");
                    return;
                }

                // Remove 64 items of the material
                player.getInventory().removeItem(new ItemStack(material, 64));

                // Add coins to the player's balance
                BedWarsPro plugin = BedWarsPro.getPlugin(BedWarsPro.class);
                plugin.userCoins.put(player.getName(), (long) (price + plugin.userCoins.getOrDefault(player.getName(), 0L)));
            }
        }
    }
    private boolean isLocationClear(World world, int x, int y, int z) {
        // 检查以 (x, y, z) 为中心的 2x2x2 范围内是否有方块
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 0; dy <= 2; dy++) {
                    if (world.getBlockAt(x + dx, y + dy, z + dz).getType() != Material.AIR) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void teleportToWorld(Player player, String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage("§c目标世界未加载");
            return;
        }
        if (worldName.equals("bps_w_n")) {
            Location safeLocation = new Location(world, 0, 60,0);
            safeLocation.setY(Math.max(safeLocation.getY(), 30)); // 至少 Y=30
            player.teleport(safeLocation);
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 200, 1)); // 增强可视性
            player.sendMessage("§a已传送到地狱世界！");
            return;
        }
        if (worldName.equals("bps_w_theEnd")) {
            //直接传送到0,80,0
            player.teleport(new Location(world, 0, 60, 0));
            return;
        }

        int attempts = 0;
        int maxAttempts = 20;
        int range = 500;
        int yMin = 80;

        Random random = new Random();

        while (attempts < maxAttempts) {
            int randomX = random.nextInt(range * 2) - range + 2000;
            int randomZ = random.nextInt(range * 2) - range;
            // 获取地形高度
            int spawnX = world.getSpawnLocation().getBlockX();
            int spawnZ = world.getSpawnLocation().getBlockZ();
            int groundY = world.getHighestBlockYAt(spawnX + randomX, spawnZ + randomZ);
            // 计算最终 Y 值，确保至少 yMin 高度
            int finalY = groundY;
            boolean flag = true;
            int y = 1;
            while (flag) {
                //判断脚下方块是不是AIR
                if (world.getBlockAt(spawnX + randomX, finalY - y, spawnZ + randomZ).getType() == Material.AIR) {
                    finalY -= y;
                    y++;
                }else{
                    flag = false;
                }
            }
            // 检查是否空旷
            if (isLocationClear(world, spawnX + randomX, finalY, spawnZ + randomZ)) {
                Location targetLocation = new Location(world, spawnX + randomX, finalY, spawnZ + randomZ);
                player.teleport(targetLocation);
                player.sendMessage("§a你已传送到世界 §f" + worldName + " §a中的空旷位置！");

                // 添加抗性提升效果（Resistance V，持续 3 秒）
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 600, 10)); // 60 ticks = 3s, 等级 5
                return;
            }
            attempts++;
        }

        // 如果没找到合适位置，传送到出生点
        player.teleport(world.getSpawnLocation());
        player.sendMessage("§e未能找到空旷位置，已传送到默认出生点。");

        // 同样给予抗性提升
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 60, 4));
    }
}
