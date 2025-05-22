package org.ast.bedwarspro.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class GameMenuGUI {

    public static Inventory createGameMenuGUI() {
        // 创建 6x9 的 GUI
        Inventory gui = Bukkit.createInventory(null, 54, "小游戏菜单");

        // 填充蓝色玻璃板到顶部和底部
        ItemStack blueGlass = new ItemStack(Material.NOTE_BLOCK);
        ItemMeta meta = blueGlass.getItemMeta();
        meta.setDisplayName(" ");
        blueGlass.setItemMeta(meta);

        for (int i = 0; i < 9; i++) {
            gui.setItem(i, blueGlass); // 第一行
            gui.setItem(53 - i, blueGlass); // 最后一行
        }
        ItemStack backIcon = new ItemStack(Material.WATCH);
        ItemMeta backMeta = backIcon.getItemMeta();
        backMeta.setDisplayName("§d大厅");
        ArrayList<String> lor = new ArrayList<>();
        lor.add("§7点击回到大厅");
        backMeta.setLore(lor);
        backIcon.setItemMeta(backMeta);
        gui.setItem(9, backIcon);


        // 示例：添加游戏图标（从第 2 行第 2 列开始）
        ItemStack gameIcon = new ItemStack(Material.DIAMOND);
        ItemMeta iconMeta = gameIcon.getItemMeta();
        iconMeta.setDisplayName("§aBedWars");
        ArrayList<String> lore = new ArrayList<>();
        lore.add("§7点击进入小游戏");
        iconMeta.setLore(lore);
        gameIcon.setItemMeta(iconMeta);
        gui.setItem(10, gameIcon);



        ItemStack survivalIcon = new ItemStack(Material.GRASS); // 可替换为你喜欢的图标
        meta = survivalIcon.getItemMeta();
        meta.setDisplayName("§a生存");
        lore = new ArrayList<>();
        lore.add("§7点击进入生存世界选择界面");
        meta.setLore(lore);
        survivalIcon.setItemMeta(meta);

        gui.setItem(28, survivalIcon); // 第 12 个槽位（第二行第四个）

        return gui;
    }

    // 处理点击事件的方法
    public static void handleItemClick(Player player, String gameName) {
        if (gameName.contains("BedWars")) {
            player.performCommand("bw autojoin");
        }else if (gameName.contains("生存")){
            Inventory gui = Bukkit.createInventory(null, 3 * 9, "生存世界菜单");

            addWorldItem(gui, 10, Material.GRASS, "主世界", "bps_w");
            addWorldItem(gui, 12, Material.NETHERRACK, "地狱", "bps_w_n");
            addWorldItem(gui, 14, Material.ENDER_STONE, "末地", "bps_w_theEnd");
            addWorldItem(gui, 16, Material.LOG, "资源世界", "bps_w_s");

            player.openInventory(gui);
        }else if (gameName.contains("大厅")){
            player.performCommand("lobby");
        }
    }
    private static void addWorldItem(Inventory gui, int slot, Material material, String name, String worldName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§a" + name);
        ArrayList<String> lore = new ArrayList<>();
        lore.add("§7点击传送到该世界");
        meta.setLore(lore);
        item.setItemMeta(meta);

        gui.setItem(slot, item);
    }



}
