package org.ast.bedwarspro.command;

import org.ast.bedwarspro.mannger.AuthManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class AuthCommand implements CommandExecutor {
    private final AuthManager authManager;
    public static final Map<String, Boolean> loggedInPlayers = new HashMap<>();

    public AuthCommand(AuthManager authManager) {
        this.authManager = authManager;
    }

    public AuthManager getAuthManager() {
        return authManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length < 2) {
            return true;
        }

        String action = args[0].toLowerCase();
        String password = args[1];
        String username = player.getName();

        if (action.equals("register") || action.equals("reg") || action.equals("r")) {
            if (authManager.isRegistered(username)) {
                player.sendMessage("§cYou are already registered.");
                return true;
            }
            if (authManager.register(username, password)) {
                player.sendMessage("§b登录完成!");
                loggedInPlayers.put(username, true);
            } else {
                player.sendMessage("§c注册失败,账号已存在");
            }
        } else if (action.equals("login") || action.equals("log") || action.equals("l")) {
            if (loggedInPlayers.getOrDefault(username, false)) {
                player.sendMessage("§cYou are already logged in.");
                return true;
            }
            if (authManager.login(username, password)) {
                loggedInPlayers.put(username, true);
                player.sendMessage("§a登录成功!");
            } else {
                player.sendMessage("§cInvalid username or password.");
            }
        } else {
            player.sendMessage("§cInvalid action. Use register or login.");
        }
        return true;
    }

    public boolean isLoggedIn(Player player) {
        return loggedInPlayers.getOrDefault(player.getName(), false);
    }
}