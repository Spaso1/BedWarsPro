package org.ast.bedwarspro.mannger;

import org.ast.bedwarspro.BedWarsPro;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.*;
import org.bukkit.scheduler.BukkitRunnable;

public class HubWorldManager implements Listener {

    private final BedWarsPro plugin;
    private final String hubWorldName;

    public HubWorldManager(BedWarsPro plugin) {
        this.plugin = plugin;
        this.hubWorldName = plugin.getConfig().getString("hub.name", "world");
        startTasks();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void startTasks() {
        // 设置世界时间为正午 & 禁止天气
        new BukkitRunnable() {
            @Override
            public void run() {
                World world = Bukkit.getWorld(hubWorldName);
                if (world != null) {
                    world.setTime(6000L);
                    world.setStorm(false);
                    world.setThundering(false);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);

        // 每隔一段时间刷新记分板内容（如在线人数）
        new BukkitRunnable() {
            @Override
            public void run() {
                updateAllPlayersScoreboards();
            }
        }.runTaskTimer(plugin, 0L, 20L * 10); // 每 10 秒更新一次
    }

    // 当玩家加入时显示记分板
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (isInHubWorld(player)) {
            setCustomScoreboard(player);
        }
    }

    // 玩家离开时恢复默认记分板
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player.getScoreboard() != null && !player.getScoreboard().equals(Bukkit.getScoreboardManager().getMainScoreboard())) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    // 根据配置创建自定义记分板
    public void setCustomScoreboard(Player player) {
        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("scoreboard.enabled")) return;

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        String title = ChatColor.translateAlternateColorCodes('&', config.getString("scoreboard.title", "Server Info"));
        Objective objective = board.registerNewObjective("custom", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        java.util.List<String> lines = config.getStringList("scoreboard.lines");

        int scoreValue = lines.size();
        for (String line : lines) {
            Score score = objective.getScore(replacePlaceholders(player, line));
            score.setScore(scoreValue--);
        }

        player.setScoreboard(board);
    }

    // 刷新所有在大厅中的玩家的记分板
    public void updateAllPlayersScoreboards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isInHubWorld(player)) {
                setCustomScoreboard(player);
            }
        }
    }
    private String replacePlaceholders(Player player, String line) {
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        int maxPlayers = Bukkit.getMaxPlayers();
        String serverName = Bukkit.getServerName();
        double rating = getRating(player.getName());
        String profession = plugin.getUserPro().getOrDefault(player.getName(), "无");

        return ChatColor.translateAlternateColorCodes('&',
                line.replace("{online_players}", String.valueOf(onlinePlayers))
                        .replace("{max_players}", String.valueOf(maxPlayers))
                        .replace("{server_name}", serverName)
                        .replace("{username}", player.getName())
                        .replace("{rating}", String.format("%.2f", rating))
                        .replace("{profession}", profession)
        );
    }

    private double getRating(String name) {
        long addr  = plugin.getUser2addr().getOrDefault(name, 0L);
        double addr2k = addr / 40.0;
        int kill = plugin.getUser2kill().getOrDefault(name, 0);
        int play = plugin.getPlays().getOrDefault(name, 0);
        int death = plugin.getUser2kill().getOrDefault(name, 0);
        int k_d = kill - death;
        double kd = (double) kill / death;
        return (kd + addr2k) / death * k_d / (k_d +1);
    }

    // 判断玩家是否在大厅世界
    private boolean isInHubWorld(Player player) {
        return player.getWorld().getName().equalsIgnoreCase(hubWorldName);
    }
}
