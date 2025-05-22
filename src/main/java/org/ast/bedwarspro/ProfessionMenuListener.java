package org.ast.bedwarspro;

import org.ast.bedwarspro.been.User;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

import static org.ast.bedwarspro.mannger.HubWorldManager.customTitles;

public class ProfessionMenuListener implements Listener {

    private final BedWarsPro plugin;
    private final Map<String,Integer> rank = new HashMap<>();
    public ProfessionMenuListener(BedWarsPro plugin) {
        this.rank.put("Reisor",0);
        this.rank.put("VIP",1);
        this.rank.put("MVP",2);
        this.rank.put("CREATE",10);
        this.rank.put("ADMIN",11);
        this.rank.put("DBA",12);
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (ChatColor.stripColor(event.getView().getTitle()).equals("选择你的职业")) {
            event.setCancelled(true); // 阻止物品被拿走

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            String professionName = clickedItem.getItemMeta().hasDisplayName()
                    ? clickedItem.getItemMeta().getDisplayName()
                    : "None";

            // 获取配置中的职业名和价格
            String professionKey = findConfigKeyByDisplayName(professionName);
            if (professionKey == null) return;

            int cost = plugin.getConfig().getInt("professions." + professionKey + ".cost", 3);
            String needRank = plugin.getConfig().getString("professions." + professionKey + ".need", "default");

            if (!needRank.equals("default")) {
                User user = plugin.getUser(player.getName());
                String playerRank = user.getRank().toLowerCase();
                //playerRank去除小写字母
                //判断needRank在rank里面的位置
                if (rank.get(playerRank) < rank.get(needRank)) {
                    player.sendMessage("§c你没有此职业！");
                    return;
                }
            }
            // 检查金币
            int goldCount = 0;
            for (org.bukkit.inventory.ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == org.bukkit.Material.GOLD_INGOT) {
                    goldCount += item.getAmount();
                }
            }

            if (goldCount >= cost) {
                // 扣除金币
                removeGold(player, cost);

                // 设置职业
                plugin.getUserPro().put(player.getName(), professionName);
                player.sendMessage("你已选择职业：" + professionName + "，花费了 " + cost + " 个金锭！");
                player.closeInventory();
            } else {
                player.sendMessage("你没有足够的金锭！需要 " + cost + " 个金锭。");
            }
        }
    }

    private String findConfigKeyByDisplayName(String displayName) {
        Map<String, String> professionMap = new HashMap<>();
        professionMap.put("§c战士", "warrior");
        professionMap.put("§b忍者", "ninja");
        professionMap.put("§e箭神", "archer");
        professionMap.put("§c剑圣", "swordsman");
        professionMap.put("§c刺客", "assassin");
        professionMap.put("§b神射手", "sharpshooter");
        professionMap.put("§c判官", "justice");
        return professionMap.get(displayName);
    }

    private void removeGold(Player player, int amount) {
        int goldToRemove = amount;
        for (org.bukkit.inventory.ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == org.bukkit.Material.GOLD_INGOT) {
                int currentAmount = item.getAmount();
                if (currentAmount <= goldToRemove) {
                    player.getInventory().remove(item);
                    goldToRemove -= currentAmount;
                } else {
                    item.setAmount(currentAmount - goldToRemove);
                    break;
                }
            }
        }
    }
}
