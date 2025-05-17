package org.ast.bedwarspro.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class PlayerStatsGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // 如果是玩家点击的，并且打开的是 "你的游戏数据" 界面
        if (event.getView().getTitle().equals("你的游戏数据")) {
            // 阻止任何操作（包括点击、拖动、移动）
            event.setCancelled(true);
        }
    }
}
