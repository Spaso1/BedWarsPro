package org.ast.bedwarspro.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.HashMap;

public class TeleportConfirmListener implements Listener {

    private static final Map<String, String> pendingRequests = new HashMap<>();

    public static void requestTeleport(Player requester, Player target) {
        pendingRequests.put(target.getName(), requester.getName());
        target.sendMessage(" ");
        target.sendMessage("§7--------------------");
        target.sendMessage("§a[确认] 玩家 " + requester.getName() + " 想要传送到你身边");
        target.sendMessage("§a点击物品 §b(✔) §a确认传送");
        target.sendMessage("§7--------------------");
        target.sendMessage(" ");

        ItemStack confirmItem = new ItemStack(Material.getMaterial("INK_SACK"));
        ItemMeta meta = confirmItem.getItemMeta();
        meta.setDisplayName("§a✔ 确认传送");
        confirmItem.setItemMeta(meta);

        target.getInventory().addItem(confirmItem);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();

        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta.getDisplayName().equals("§a✔ 确认传送")) {
            event.setCancelled(true);

            String requesterName = pendingRequests.get(player.getName());
            if (requesterName == null) {
                player.sendMessage("§c没有待处理的传送请求。");
                return;
            }

            Player requester = Bukkit.getPlayer(requesterName);
            if (requester == null || !requester.isOnline()) {
                player.sendMessage("§c请求者已离线。");
                pendingRequests.remove(player.getName());
                return;
            }

            // 执行传送
            requester.teleport(player.getLocation());
            player.sendMessage("§a你已允许 §f" + requester.getName() + " §a传送到你身边");
            requester.sendMessage("§a你已成功传送到 §f" + player.getName() + " §a身边");

            // 清除请求
            pendingRequests.remove(player.getName());
        }
    }

}
