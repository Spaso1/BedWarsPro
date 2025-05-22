package org.ast.bedwarspro.gui;

import org.ast.bedwarspro.BedWarsPro;
import org.ast.bedwarspro.been.User;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import static org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS;

public class RewardGUI {
    public static  final int MAXPAGE = 10;
    public static int PAGE = 0;
    // 固定显示 3 行 × 3 列 = 9 个任务
    public static final int ROWS = 3;
    public static final int MAX_ROWS = 6;
    public static final int COLS = 7;
    public static final int TASK_COUNT = ROWS * COLS;

    private static final Material PANE_MATERIAL = Material.valueOf("STAINED_GLASS_PANE");

    /**
     * 获取奖励界面（单页）
     */
    public static Inventory getRewardGUI(Player player, BedWarsPro plugin,int page) {
        plugin.asyncReadPlayFile();

        Inventory gui = Bukkit.createInventory(null, 9 * MAX_ROWS, "奖励页面");
        User user = plugin.getUser(player.getName());
        int kills = user.getKills();
        long damage = user.getAddr();
        int wins = user.getPlays();
        Set<String> claimedRewards = plugin.claimedRewards.getOrDefault(player.getName(), new HashSet<>());

        addTasks(gui, kills, damage, wins, claimedRewards,page);
        return gui;
    }

    private static void addTasks(Inventory gui, int kills, long damage, int wins, Set<String> claimed,int page) {
        for (int row = 0; row < ROWS; row++) {
            if (row==0) {
                ItemStack killProgress = new ItemStack(Material.DIAMOND, 1);
                ItemMeta meta = killProgress.getItemMeta();
                meta.setDisplayName("§a击杀进度");
                meta.setLore(Arrays.asList("§7目标完成可领取金币"));
                killProgress.setItemMeta(meta);
                gui.setItem(0, killProgress);
            }
            if (row==1) {
                ItemStack damageProgress = new ItemStack(Material.EMERALD, 1);
                ItemMeta meta = damageProgress.getItemMeta();
                meta.setDisplayName("§a造成伤害");
                meta.setLore(Arrays.asList("§7目标完成可领取金币"));
                damageProgress.setItemMeta(meta);
                gui.setItem(9, damageProgress);
            }
            if (row==2) {
                ItemStack winProgress = new ItemStack(Material.IRON_INGOT, 1);
                ItemMeta meta = winProgress.getItemMeta();
                meta.setDisplayName("§b胜利次数");
                meta.setLore(Arrays.asList("§7目标完成可领取金币"));
                winProgress.setItemMeta(meta);
                gui.setItem(18, winProgress);
            }
            for (int col = 0; col < COLS; col++) {
                int index = row * COLS + col;
                int taskId = col + 1 + page*COLS;

                int killTarget = 3 * taskId;
                int damageTarget = 400 * taskId;
                int winTarget = 3 * taskId;

                int killTargetNeed = 3 * (taskId *taskId);
                int damageTargetNeed = 400 * taskId+index*(index+1);
                int winTargetNeed = 3 * taskId;

                int slot = row * 9 + (col + 1); // 每行第一个为 1~9

                switch (row) {
                    case 0:
                        buildTask(gui, slot, "§a击杀进度:", kills, killTargetNeed, "kill:" + killTargetNeed, claimed,killTarget,page);
                        break;
                    case 1:
                        buildTask(gui, slot, "§c造成伤害:", damage, damageTargetNeed, "damage:" + damageTargetNeed, claimed,damageTarget,page);
                        break;
                    case 2:
                        buildTask(gui, slot, "§b胜利次数:", wins, winTargetNeed, "win:" + winTargetNeed, claimed,winTarget,page);
                        break;
                }
            }
        }
        {
            ItemStack nextPage = new ItemStack(Material.GOLD_INGOT, (page + 1));
            ItemMeta meta = nextPage.getItemMeta();
            meta.setDisplayName("§a第" + (page + 1) + "页");
            //设置页面index
            List<String> lore = new ArrayList<>();
            lore.add("§7" + (page + 1));
            meta.setLore(lore);
            nextPage.setItemMeta(meta);
            gui.setItem(MAX_ROWS * 9 - 5, nextPage);
        }
        //添加翻页物品
        if (page < MAXPAGE - 1) {
            ItemStack nextPage = new ItemStack(Material.ARROW, 1);
            ItemMeta meta = nextPage.getItemMeta();
            meta.setDisplayName("§a下一页");
            //设置页面index
            List<String> lore = new ArrayList<>();
            lore.add("§7" + (page + 2));
            meta.setLore(lore);
            nextPage.setItemMeta(meta);
            gui.setItem(MAX_ROWS*9 - 1, nextPage);
        }
        if (page > 0) {
            ItemStack prevPage = new ItemStack(Material.ARROW, 1);
            ItemMeta meta = prevPage.getItemMeta();
            meta.setDisplayName("§a上一页");
            List<String> lore = new ArrayList<>();
            lore.add("§7" + (page));
            meta.setLore(lore);
            prevPage.setItemMeta(meta);
            gui.setItem(MAX_ROWS*9 - 9, prevPage);
        }
    }

    /**
     * 构建单个任务条目
     */
    private static void buildTask(Inventory gui, int slot, String prefix, double current, double target,
                                  String key, Set<String> claimed,double coin,int page) {
        boolean isClaimed = claimed.contains(key);

        ItemStack item = new ItemStack(PANE_MATERIAL, 1, (byte) (isClaimed ? 5 : (current >= target ? 5 : 14)));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(prefix + " §f" + (int) current + "/" + (int) target);
        List<String> lore = new ArrayList<>();
        lore.add("§7目标完成可领取金币");
        lore.add(page + "");
        if (prefix.contains("造成伤害")) {
            lore.add("§7奖励: " + ((int) coin * 1));
        }else {
            lore.add("§7奖励: " + ((int) coin * 10));
        }
        if (isClaimed) {
            lore.add("§e✔ 已领取");
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(HIDE_ENCHANTS);
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        gui.setItem(slot, item);
    }
}
