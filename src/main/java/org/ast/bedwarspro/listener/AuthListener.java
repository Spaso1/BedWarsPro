package org.ast.bedwarspro.listener;

import org.ast.bedwarspro.command.AuthCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashSet;
import java.util.Set;

import static org.ast.bedwarspro.command.AuthCommand.loggedInPlayers;

public class AuthListener implements Listener {
    private final AuthCommand authCommand;
    private final Set<Player> notifiedPlayers = new HashSet<>();

    public AuthListener(AuthCommand authCommand) {
        this.authCommand = authCommand;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!authCommand.isLoggedIn(player)) {
            event.setCancelled(true); // Prevent movement
            if (!notifiedPlayers.contains(player)) {
                event.getPlayer().sendMessage("§c使用/auth login <password>登录.");
                event.getPlayer().sendMessage("§c或使用/auth register <password>注册.");
                notifiedPlayers.add(player);
            }
        } else {
            notifiedPlayers.remove(player); // Remove from the set if the player logs in
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!authCommand.isLoggedIn(event.getPlayer())) {
            event.setCancelled(true); // Prevent interaction
            event.getPlayer().sendMessage("§c使用/auth login <password>登录.");
            event.getPlayer().sendMessage("§c或使用/auth register <password>注册.");
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!authCommand.isLoggedIn((org.bukkit.entity.Player) event.getWhoClicked())) {
            event.setCancelled(true); // Prevent inventory interaction
            event.getWhoClicked().sendMessage("§cYou must log in to interact with inventories.");
        }
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!authCommand.isLoggedIn(event.getPlayer())) {
            event.getPlayer().sendMessage("§c使用/auth login <password>登录.");
            event.getPlayer().sendMessage("§c或使用/auth register <password>注册.");
        }
    }
    //检测玩家退出服务器
    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (authCommand.isLoggedIn(player)) {
            loggedInPlayers.remove(player.getName());
        }
    }
}