package org.ast.bedwarspro.listener;

import org.ast.bedwarspro.BedWarsPro;
import org.ast.bedwarspro.been.User;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class ChatListener implements Listener {
    private final BedWarsPro plugin;

    public ChatListener(BedWarsPro plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        String message = event.getMessage();
        World world = sender.getWorld();

        // 获取大厅世界名称（来自 config.yml）
        String hubWorldName = plugin.getConfig().getString("hub.name", "lobbyReisa");

        List<Player> recipients = new ArrayList<>();

        for (Player player : sender.getServer().getOnlinePlayers()) {
            if (canSeeMessage(sender, player, hubWorldName)) {
                recipients.add(player);
            }
        }

        // 设置自定义格式
        User user = plugin.getUser(sender.getName());
        String title = user.getUse_title();

        String formattedMessage = ChatColor.translateAlternateColorCodes('&',
                "[" + title + "&r] &b" + sender.getName() + ": %r") + message;

        event.setFormat(null); // 使用自定义接收者列表时建议设为 null
        event.getRecipients().clear();
        event.getRecipients().addAll(recipients);
        sender.sendMessage(formattedMessage);
    }

    /**
     * 判断目标玩家是否能接收到发送者的消息
     */
    private boolean canSeeMessage(Player sender, Player target, String hubWorldName) {
        World senderWorld = sender.getWorld();
        World targetWorld = target.getWorld();

        String senderWorldName = senderWorld.getName();
        String targetWorldName = targetWorld.getName();

        // 如果是同一个玩家，总是能看到
        if (sender.equals(target)) {
            return true;
        }

        // 如果发送者在 bedworld* 的世界中
        if (senderWorldName.startsWith("bedworld")) {
            return senderWorldName.equals(targetWorldName); // 只有同个 bedworld 子世界可见
        }

        // 发送者在 bps* 或大厅世界（lobbyReisa）
        if (senderWorldName.startsWith("bps") || senderWorldName.equals(hubWorldName)) {
            // 接收者必须也在 bps* 或大厅世界
            return targetWorldName.startsWith("bps") || targetWorldName.equals(hubWorldName);
        }

        // 默认允许看到（其他世界不做隔离）
        return true;
    }
}
