package org.ast.bedwarspro.command;

import org.ast.bedwarspro.BedWarsPro;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class EnchantCommand implements CommandExecutor {

    private final BedWarsPro plugin;

    public EnchantCommand(BedWarsPro plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("该指令只能由玩家执行");
            return true;
        }

        Player player = (Player) commandSender;
        addEnchantBook(player, strings[0], Integer.parseInt(strings[1]));
        
        return false;
    }
    private void addEnchantBook(Player player, String enchantKey, int level) {
        org.bukkit.enchantments.Enchantment enchantment = org.bukkit.enchantments.Enchantment.getByName(enchantKey);
        if (enchantment == null) {
            player.sendMessage("§c无效的附魔类型");
            return;
        }
        BedWarsPro plugin = BedWarsPro.getPlugin(BedWarsPro.class);

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
        player.sendMessage("§a已成功为你的物品添加 §b" + ChatColor.stripColor(enchantKey));
    }
}
