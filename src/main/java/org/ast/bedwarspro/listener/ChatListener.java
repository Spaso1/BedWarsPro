package org.ast.bedwarspro.listener;

import org.ast.bedwarspro.BedWarsPro;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.ChatColor;
import org.ast.bedwarspro.been.User;

public class ChatListener implements Listener {
    private BedWarsPro plugin = BedWarsPro.getPlugin(BedWarsPro.class);
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // 获取发送消息的玩家
        User user = plugin.getUser(event.getPlayer().getName()); // 假设你有这个方法获取用户信息
        String title = user.getTitle().isEmpty() ? "" : user.getTitle().get(0); // 取第一个称号

        // 设置聊天消息格式：[称号] &b用户名: 消息
        event.setFormat(ChatColor.translateAlternateColorCodes('&', "[" + title + "] &b" + event.getPlayer().getName() + ": ") + "%2$s");
    }
}
