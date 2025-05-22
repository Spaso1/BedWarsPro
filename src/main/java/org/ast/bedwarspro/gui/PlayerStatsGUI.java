package org.ast.bedwarspro.gui;

import org.ast.bedwarspro.BedWarsPro;
import org.ast.bedwarspro.been.User;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;

public class PlayerStatsGUI {

    public static Inventory createPlayerStatsGUI(Player player, BedWarsPro plugin) {
        // 创建一个 3x9 的 GUI（3 行）
        Inventory gui = Bukkit.createInventory(null, 9 * 3, "你的游戏数据");
        plugin.asyncReadPlayFile();

        // 获取玩家的游戏数据
        String playerName = player.getName();
        User user = plugin.getUser(playerName);
        int kills = user.getKills();
        int deaths = user.getDeaths();
        long addr = user.getAddr();
        int plays = user.getPlays();
        double rating = calculateRating(kills, deaths, addr, plays);

        // 设置击杀数物品
        ItemStack killItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta killMeta = killItem.getItemMeta();
        killMeta.setDisplayName("§a击杀数");
        ArrayList<String> killLore = new ArrayList<>();
        killLore.add("§7你总共击杀了 §f" + kills + " §7次");
        killMeta.setLore(killLore);
        killItem.setItemMeta(killMeta);

        gui.setItem(10, killItem); // 放在第 2 行第 2 列

        // 设置死亡数物品
        ItemStack deathItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 1);
        ItemMeta deathMeta = deathItem.getItemMeta();
        deathMeta.setDisplayName("§c死亡数");
        ArrayList<String> deathLore = new ArrayList<>();
        deathLore.add("§7你总共死亡了 §f" + deaths + " §7次");
        deathMeta.setLore(deathLore);
        deathItem.setItemMeta(deathMeta);
        gui.setItem(12, deathItem); // 放在第 2 行第 4 列

        // 设置 K/D 比率物品
        ItemStack kdRatioItem = new ItemStack(Material.PAPER);
        ItemMeta kdRatioMeta = kdRatioItem.getItemMeta();
        kdRatioMeta.setDisplayName("§bK/D 比率");
        ArrayList<String> kdLore = new ArrayList<>();
        kdLore.add("§7你的 K/D 比率为 §f" + String.format("%.2f",(double) kills /(double) deaths));
        kdRatioMeta.setLore(kdLore);
        kdRatioItem.setItemMeta(kdRatioMeta);
        gui.setItem(14, kdRatioItem); // 放在第 2 行第 6 列

        // 设置职业信息物品
        ItemStack professionItem = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta professionMeta = professionItem.getItemMeta();
        professionMeta.setDisplayName("§ePlay胜利次数");
        ArrayList<String> professionLore = new ArrayList<>();
        professionLore.add("§7你的当前胜利次数 §f" + plays);
        professionMeta.setLore(professionLore);
        professionItem.setItemMeta(professionMeta);
        gui.setItem(16, professionItem); // 放在第 2 行第 8 列

        return gui;
    }

    private static double calculateRating(int kills, int deaths, long addr, int plays) {
        if (deaths == 0) {
            return 1.0; // 默认值，避免除以零
        }
        double addr2k = addr / 40.0;
        double kd = (double) kills / deaths;
        int k_d = kills - deaths;
        return (kd + addr2k) / deaths * (k_d + 1) / (k_d + 2);
    }
}
