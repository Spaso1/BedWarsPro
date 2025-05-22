package org.ast.bedwarspro.listener;

import org.ast.bedwarspro.BedWarsPro;
import org.ast.bedwarspro.mannger.GroupWorldManager;
import org.ast.bedwarspro.storage.InventoryStorage;
import org.ast.bedwarspro.storage.PlayerInventoryCache;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class InventorySyncListener implements Listener {
    private final BedWarsPro plugin = BedWarsPro.getPlugin(BedWarsPro.class);
    private final GroupWorldManager groupWorldManager;
    private final InventoryStorage inventoryStorage;

    public InventorySyncListener(GroupWorldManager groupWorldManager, InventoryStorage inventoryStorage) {
        this.groupWorldManager = groupWorldManager;
        this.inventoryStorage = inventoryStorage;
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        String fromWorld = event.getFrom().getName();
        String toWorld = player.getWorld().getName();

        String fromGroup = groupWorldManager.getGroupByWorld(fromWorld);
        String toGroup = groupWorldManager.getGroupByWorld(toWorld);

        // 保存原 group 背包
        if (fromGroup != null) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                inventoryStorage.saveInventory(uuid, fromGroup, player.getInventory().getContents());
                PlayerInventoryCache.save(uuid, fromGroup, player.getInventory().getContents());
            });
        }

        // 加载目标 group 背包
        if (toGroup != null) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                ItemStack[] cached = PlayerInventoryCache.get(uuid, toGroup);
                if (cached == null) {
                    cached = inventoryStorage.loadInventory(uuid, toGroup);
                }

                if (cached != null && cached.length > 0) {
                    ItemStack[] finalCached = cached;
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.getInventory().setContents(finalCached);
                    });
                } else {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.getInventory().clear();
                    });
                }
            });
        }
    }

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        String world = player.getWorld().getName();
        String group = groupWorldManager.getGroupByWorld(world);
        if (group == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerInventoryCache.save(player.getUniqueId(), group, player.getInventory().getContents());
            inventoryStorage.saveInventory(player.getUniqueId(), group, player.getInventory().getContents());
        });
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        String world = player.getWorld().getName();
        String group = groupWorldManager.getGroupByWorld(world);
        if (group == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerInventoryCache.save(player.getUniqueId(), group, player.getInventory().getContents());
            inventoryStorage.saveInventory(player.getUniqueId(), group, player.getInventory().getContents());
        });
    }

    @EventHandler
    public void onInventoryChange(InventoryClickEvent event) {
        HumanEntity clicker = event.getWhoClicked();
        if (!(clicker instanceof Player)) {
            return; // 非玩家点击，忽略
        }
        Player player = (Player) clicker;
        String world = player.getWorld().getName();
        String group = groupWorldManager.getGroupByWorld(world);
        if (group == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerInventoryCache.save(player.getUniqueId(), group, player.getInventory().getContents());
            inventoryStorage.saveInventory(player.getUniqueId(), group, player.getInventory().getContents());
        });
    }

}
