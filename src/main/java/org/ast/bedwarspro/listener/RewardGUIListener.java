package org.ast.bedwarspro.listener;

import org.ast.bedwarspro.BedWarsPro;
import org.ast.bedwarspro.gui.RewardGUI;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;

public class RewardGUIListener implements Listener {

    private final BedWarsPro plugin;

    public RewardGUIListener(BedWarsPro plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("奖励页面")) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null) return;
        try {
            ItemMeta meta = clicked.getItemMeta();

            if (meta.hasEnchant(Enchantment.DURABILITY)) return;
            String displayName = meta.getDisplayName();
            if (displayName.contains("下一页")) {
                plugin.saveData();
                int page = Integer.parseInt(meta.getLore().get(0).replace("§7",""));
                player.openInventory(RewardGUI.getRewardGUI(player, plugin,page-1));
                return;
            }
            if (displayName.contains("上一页")) {
                plugin.saveData();
                int page = Integer.parseInt(meta.getLore().get(0).replace("§7",""));
                player.openInventory(RewardGUI.getRewardGUI(player, plugin,page-1));
                return;
            }

            if (clicked.getType() != Material.STAINED_GLASS_PANE) return;

            if (!displayName.contains("/")) return;

            String[] parts = displayName.split("/");
            int target = Integer.parseInt(parts[1].trim());
            String rewardType = "";
            int nowpage = Integer.parseInt(meta.getLore().get(1).replace("§7",""));

            if (displayName.startsWith("§a击杀进度:")) {
                int current = Integer.parseInt(parts[0].replace("§a击杀进度: §f", "").trim());
                if (current >= target) {
                    plugin.deductCoins(player, -(target * 10));
                    rewardType = "kill:" + target;
                }
            } else if (displayName.startsWith("§c造成伤害:")) {
                long current = Long.parseLong(parts[0].replace("§c造成伤害: §f", "").trim());
                if (current >= target) {
                    plugin.deductCoins(player, -(target * 2));
                    rewardType = "damage:" + target;
                }
            } else if (displayName.startsWith("§b胜利次数:")) {
                int current = Integer.parseInt(parts[0].replace("§b胜利次数: §f", "").trim());
                if (current >= target) {
                    plugin.deductCoins(player, -(target * 15));
                    rewardType = "win:" + target;
                }
            }


            if (!rewardType.isEmpty()) {
                plugin.claimedRewards.computeIfAbsent(player.getName(), k -> new HashSet<>()).add(rewardType);
                plugin.saveData();
                player.openInventory(RewardGUI.getRewardGUI(player, plugin,nowpage)); // 刷新界面
            }
        }catch (Exception e) {}
    }
}
