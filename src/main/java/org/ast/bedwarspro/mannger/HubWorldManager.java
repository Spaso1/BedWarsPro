package org.ast.bedwarspro.mannger;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.ast.bedwarspro.BedWarsPro;
import org.ast.bedwarspro.been.TabGroup;
import org.ast.bedwarspro.gui.GameMenuGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.*;
import org.bukkit.scheduler.BukkitRunnable;
import sun.rmi.runtime.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ast.bedwarspro.BedWarsPro.white;
import static org.bukkit.Bukkit.getLogger;

public class HubWorldManager implements Listener {
    private FileConfiguration config;
    private boolean enabled;
    private String header;
    private String footer;
    private final Map<String, String> customTitles = new HashMap<>();
    private ProtocolManager protocolManager;

    private final BedWarsPro plugin;
    private final String hubWorldName;
    private int tabColumns;
    private final Map<String, TabGroup> tabGroups = new HashMap<>();

    public HubWorldManager(BedWarsPro plugin) {
        this.plugin = plugin;
        this.hubWorldName = plugin.getConfig().getString("hub.name", "world");
        startTasks();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        config = plugin.getConfig();
        // 检查ProtocolLib是否安装
        if (!Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
            getLogger().severe("ProtocolLib is required for this plugin!");
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        }
        boolean keepFullHealth = plugin.getConfig().getBoolean("hub.keep-full-health", true);
        boolean keepFullSaturation = plugin.getConfig().getBoolean("hub.keep-full-saturation", true);


        protocolManager = ProtocolLibrary.getProtocolManager();
        // 从配置中读取设置
        reloadConfigValues();

        // 启动定时任务更新Tab
        new BukkitRunnable() {
            @Override
            public void run() {
                if (enabled) {
                    updateAllPlayersTab();
                }
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (isInHubWorld(player)) {
                        if (keepFullHealth) {
                            player.setHealth(20.0);
                        }
                        if (keepFullSaturation) {
                            player.setFoodLevel(20);
                            player.setSaturation(20F);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 100L); // 每5秒更新一次(100 ticks)
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
        }.runTaskTimer(plugin, 0L, 20L);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updatePlayerVisibility(player);
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 20 * 30L); // 每30秒检查一次

    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction().name().contains("RIGHT_CLICK")) {
            ItemStack item = player.getItemInHand();

            // 判断是否是“闹钟”物品
            if (item != null && item.getType() == Material.WATCH) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName() && meta.getDisplayName().equals("§bMenu")) {
                    event.setCancelled(true);
                    player.openInventory(GameMenuGUI.createGameMenuGUI());
                }
            }
            if (!isInHubWorld(player)) {
                return;
            }
            if (event.hasBlock() &&
                    (event.getClickedBlock().getType().isSolid() ||
                            event.getClickedBlock().getType().name().contains("DOOR") ||
                            event.getClickedBlock().getType().name().contains("BUTTON") ||
                            event.getClickedBlock().getType().name().contains("PLATE"))) {
                if (white.containsKey(player.getName())) {
                    return;
                }
                event.setCancelled(true);
                player.sendMessage("§c大厅中不允许交互或破坏此类型方块");
            }
        }
    }

    // 当玩家加入时显示记分板
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.getWorld().getName().startsWith("bps")) {
            if (!player.getInventory().contains(Material.WATCH)) {
                ItemStack clock = new ItemStack(Material.WATCH);
                ItemMeta meta = clock.getItemMeta();
                meta.setDisplayName("§bMenu");
                clock.setItemMeta(meta);
                player.getInventory().addItem(clock);
            }
            setCustomScoreboard(player);
            updatePlayerTab(player);
            return;
        }

        clearAllMobsInHub();
        Bukkit.getScheduler().runTaskLater(plugin, () -> updatePlayerVisibility(player), 5L); // 延迟一下保证加载完成

        if (player.getWorld().getName().equalsIgnoreCase("world") || !isInHubWorld(player)) {
            World hubWorld = Bukkit.getWorld(hubWorldName);
            if (hubWorld != null) {
                player.teleport(hubWorld.getSpawnLocation());
                player.sendMessage("§a欢迎来到大厅！");
            } else {
                player.sendMessage("§c大厅世界未加载，请联系管理员");
            }
        }

        if (!player.getInventory().contains(Material.WATCH)) {
            ItemStack clock = new ItemStack(Material.WATCH);
            ItemMeta meta = clock.getItemMeta();
            meta.setDisplayName("§bMenu");
            clock.setItemMeta(meta);
            player.getInventory().addItem(clock);
        }

        setCustomScoreboard(player);
        updatePlayerTab(player);
        updateAllPlayersScoreboards(); // 更新所有玩家的记分板
    }


    @EventHandler
    public void onPlayerDamage(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();

            // 如果受害者在大厅世界，则阻止伤害事件
            if (isInHubWorld(victim)) {
                if (event.getDamager() instanceof Player) {
                    event.setCancelled(true); // 取消玩家之间的伤害
                }
            }
        }
    }
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getLocation().getWorld().getName().equalsIgnoreCase(hubWorldName)) {
            // 阻止所有非自然生成的生物生成
            if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER &&
                    event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onEntityExplode(org.bukkit.event.entity.EntityExplodeEvent event) {
        if (event.getEntity() != null &&
                event.getLocation().getWorld().getName().equalsIgnoreCase(hubWorldName)) {
            event.blockList().clear(); // 清空爆炸影响的方块列表
        }
    }

    @EventHandler
    public void onPlayerUseEgg(org.bukkit.event.player.PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (isInHubWorld(player)) {
            if (event.getAction().name().contains("RIGHT_CLICK") &&
                    event.hasItem() &&
                    (event.getItem().getType().name().endsWith("_SPAWN_EGG"))) {

                event.setCancelled(true);
                player.sendMessage("§c大厅中不允许使用刷怪蛋");
            }
        }
    }
    public void clearAllMobsInHub() {
        World hubWorld = Bukkit.getWorld(hubWorldName);
        if (hubWorld != null) {
            hubWorld.getLivingEntities().forEach(entity -> {
                if (!(entity instanceof Player)) {
                    entity.remove();
                }
            });
        }
    }

    @EventHandler
    public void onExplosionDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (event.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.ENTITY_EXPLOSION ||
                event.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {

            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                if (isInHubWorld(player)) {
                    event.setCancelled(true); // 取消爆炸对玩家的伤害
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(org.bukkit.event.player.PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getLocation().getWorld() != null &&
                player.getLocation().getWorld().getName().equalsIgnoreCase("world") &&
                !isInHubWorld(player)) {

            World hubWorld = Bukkit.getWorld(hubWorldName);
            if (hubWorld != null) {
                player.teleport(hubWorld.getSpawnLocation());
                player.sendMessage("§a检测到你在默认世界，已将你传送至大厅");
            }
        }
        if (!event.getTo().equals(event.getFrom())) {
            updatePlayerVisibility(event.getPlayer());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (white.containsKey(player.getName())) {
            return;
        }
        if (isInHubWorld(player)) {
            event.setCancelled(true);
            player.sendMessage("§c你不能在大厅中破坏方块！");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (white.containsKey(player.getName())) {
            return;
        }
        if (isInHubWorld(player)) {
            event.setCancelled(true);
            player.sendMessage("§c你不能在大厅中放置方块！");
        }
    }
    // 玩家离开时恢复默认记分板
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PersistentScoreboardManager.clear(player.getUniqueId());

        updateAllPlayersScoreboards(); // 新增这一行，让所有玩家更新记分板
    }
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (isInHubWorld(player)) {
            Bukkit.getScheduler().runTask(plugin, () -> setCustomScoreboard(player));
        }
    }

    // 根据配置创建自定义记分板
    public void setCustomScoreboard(Player player) {
        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("scoreboard.enabled")) return;

        Scoreboard board = player.getScoreboard();

        // 如果已经是我们设置的 Scoreboard，则跳过重建
        if (PersistentScoreboardManager.isPersistent(player)) {
            return;
        }

        // 否则新建一个
        board = Bukkit.getScoreboardManager().getNewScoreboard();

        String title = ChatColor.translateAlternateColorCodes('&', config.getString("scoreboard.title", "Server Info"));
        Objective objective = board.registerNewObjective(title, "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<String> lines = config.getStringList("scoreboard.lines");

        int scoreValue = lines.size();
        for (String line : lines) {
            Score score = objective.getScore(replacePlaceholders(player, line));
            score.setScore(scoreValue--);
        }

        PersistentScoreboardManager.setPersistentBoard(player, board); // ✅ 使用持久化管理器
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
                        .replace("{value}",plugin.getCoins(player) + "")
        );
    }

    private double getRating(String name) {
        try {
            if (!plugin.getUser2addr().containsKey(name)){
                return 1.0;
            }
            long addr  = plugin.getUser2addr().getOrDefault(name, 0L) + 1;
            double addr2k = addr / 40.0;
            int kill = plugin.getUser2kill().getOrDefault(name, 0) + 1;
            int play = plugin.getPlays().getOrDefault(name, 0) + 1;
            int death = plugin.getUser2death().getOrDefault(name, 0) + 1;
            int k_d = kill - death;
            double kd = (double) kill / death;
            return (kd + addr2k) / death * (k_d+1) / (k_d +2);
        }catch (Exception e) {
           // plugin.re();
            e.printStackTrace();
            return 1.0;
        }

    }

    // 判断玩家是否在大厅世界
    private boolean isInHubWorld(Player player) {
        if (player.getWorld().getName().startsWith("bps")){
            return true;
        }
        return player.getWorld().getName().equalsIgnoreCase(hubWorldName);
    }
    private void reloadConfigValues() {
        enabled = config.getBoolean("tab.enabled", true);
        header = translateColors(config.getString("tab.header", ""));
        footer = translateColors(config.getString("tab.footer", ""));

        // 读取列数配置
        tabColumns = config.getInt("tab.columns", 4);

        customTitles.clear();
        if (config.isConfigurationSection("tab.custom_titles")) {
            for (String playerName : config.getConfigurationSection("tab.custom_titles").getKeys(false)) {
                customTitles.put(playerName, translateColors(config.getString("tab.custom_titles." + playerName)));
            }
        }

        // 读取分组配置
        tabGroups.clear();
        if (config.isConfigurationSection("tab.groups")) {
            for (String groupName : config.getConfigurationSection("tab.groups").getKeys(false)) {
                String prefix = translateColors(config.getString("tab.groups." + groupName + ".prefix", ""));
                String worlds = config.getString("tab.groups." + groupName + ".worlds", "");
                tabGroups.put(groupName, new TabGroup(prefix, worlds));
            }
        }
    }
    private boolean shouldShowCustomTab(Player player) {
        String worldName = player.getWorld().getName();
        return worldName.equalsIgnoreCase(hubWorldName) || worldName.startsWith("bedworld");
    }

    private void sendTabHeaderFooter(Player player, String header, String footer) {
        try {
            // 修正数据包类型
            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);
            header = "§8§l▬▬▬▬▬▬▬▬▬ \n" +
                    "§r§f§l" + (header != null ? header : "") +
                    "§8§l▬▬▬▬▬▬▬▬▬ ";            // 设置头部和底部内容
            packet.getChatComponents().write(0, WrappedChatComponent.fromText(header != null ? header : ""));
            packet.getChatComponents().write(1, WrappedChatComponent.fromText(footer != null ? footer : ""));

            protocolManager.sendServerPacket(player, packet);
        } catch (Exception e) {
            getLogger().warning("发送Tab头尾数据包失败: " + e.getMessage());
        }
    }

    private void updateAllPlayersTab() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerTab(player);
        }
    }
    @EventHandler
    public void onFallDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        String worldName = player.getName();
        // 检查是否是忍者职业
        if (worldName.equals(hubWorldName)) {
            event.setCancelled(true); // 取消摔落伤害
        }
    }
    private void updatePlayerTab(Player player) {
        if (shouldShowCustomTab(player)) {
            // 更新自定义 Tab 信息
            String playerName = player.getName();
            Double rating = getRating(playerName);
            String ratingStr = String.format("%.2f", rating);

            if (player.getWorld().getName().startsWith("bedworld")) {
                player.setPlayerListName( playerName);
            }else {
                if (customTitles.containsKey(playerName)) {
                    player.setPlayerListName( customTitles.get(playerName) + " §b" + ratingStr + "§r " + ChatColor.RESET + "§l§a"+ playerName);
                } else {
                    player.setPlayerListName("§9Reisor§r §b" + ratingStr + "§r " + ChatColor.RESET + "§l§a"+ playerName);
                }
            }

            if (header != null || footer != null) {
                sendTabHeaderFooter(player, header, footer);
            }
        } else {
            restoreDefaultTab(player);
        }
    }

    private void updatePlayerVisibility(Player player) {
        String worldName = player.getWorld().getName();
        boolean isBedWorld = worldName.startsWith("bedworld");

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (isBedWorld) {
                // 如果当前玩家在 bedworld，则仅允许同世界玩家看到
                if (online.getWorld().getName().equals(worldName)) {
                    online.showPlayer(player);
                    player.showPlayer(online);
                } else {
                    online.hidePlayer(player);
                    player.hidePlayer(online);
                }
            } else {
                // 如果不在 bedworld，确保所有在线玩家都能看到他
                online.showPlayer(player);
                player.showPlayer(online);
                if (online.getWorld().getName().startsWith("bedworld")) {

                    String originalName = online.getName();

                    // 构建 BedWorld 玩家在大厅中应显示的名字
                    String bedWorldDisplayName = "[§dBedWars§r]§a" + originalName;
                    online.setPlayerListName(bedWorldDisplayName);
                }else{
                }
            }
        }
    }

    private void restoreDefaultTab(Player player) {
        try {
            // 清除玩家列表中的自定义名称
            player.setPlayerListName(player.getName());

            // 使用 ProtocolLib 发送空的 Header/Footer 数据包，恢复默认状态
            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);
            packet.getChatComponents().write(0, WrappedChatComponent.fromText(""));
            packet.getChatComponents().write(1, WrappedChatComponent.fromText(""));

            protocolManager.sendServerPacket(player, packet);
        } catch (Exception e) {
            getLogger().warning("恢复默认Tab失败: " + e.getMessage());
        }
    }

    private void updatePlayerGroups(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        // 为每个分组创建团队
        for (Map.Entry<String, TabGroup> entry : tabGroups.entrySet()) {
            String groupName = entry.getKey();
            TabGroup group = entry.getValue();

            Team team = scoreboard.registerNewTeam(groupName);
            team.setPrefix(group.getPrefix());
        }

        // 将在线玩家分配到相应团队
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            String worldName = onlinePlayer.getWorld().getName();

            // 跳过隐藏世界的玩家
            if (tabGroups.containsKey("hidden") && tabGroups.get("hidden").matches(worldName)) {
                continue;
            }

            // 查找匹配的分组
            for (Map.Entry<String, TabGroup> entry : tabGroups.entrySet()) {
                String groupName = entry.getKey();
                TabGroup group = entry.getValue();

                if (!groupName.equals("hidden") && group.matches(worldName)) {
                    Team team = scoreboard.getTeam(groupName);
                    if (team != null) {
                        team.addEntry(onlinePlayer.getName());
                    }
                    break;
                }
            }
        }

        // 应用记分板
        player.setScoreboard(scoreboard);
    }
    private String translateColors(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

}
