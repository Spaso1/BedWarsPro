package org.ast.bedwarspro;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.ast.bedwarspro.been.User;
import org.ast.bedwarspro.command.*;
import org.ast.bedwarspro.gui.*;
import org.ast.bedwarspro.listener.*;
import org.ast.bedwarspro.mannger.*;
import org.ast.bedwarspro.storage.InventoryStorage;
import org.ast.bedwarspro.task.HubClearTask;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.ast.bedwarspro.PlayerDamageListener.swordSaintDamageBuff;

public final class BedWarsPro extends JavaPlugin {
    private List<String> professionList = new ArrayList<>();
    private Map<String,String> userPro = new HashMap<>();
    private final File dataFolder = new File(getDataFolder(), "data");
    private final File settleDataFile = new File(dataFolder, "settle.json");
    private Map<String,ArrayList<String>> settleData = new HashMap<>();
    private Map<String, User> userData = new HashMap<>();
    public User getUser(String name) {
        if (userData.containsKey(name)) {
            return userData.get(name);
        }
        User user = new User(name);
        userData.put(name, user);
        return userData.get(name);
    }
    public void setUser(User user) {
        userData.put(user.getName(), user);
    }
    private final File userDataFile = new File(dataFolder, "user.json");

    private File playFile ;
    // 记录玩家已领取的奖励（页码-任务索引）
    public Map<String, Set<String>> claimedRewards = new HashMap<>();
    private final File claimedRewardsFile = new File(dataFolder, "claimed_rewards.json");

    private final Gson gson = new Gson();
    private final File userCoinsFile = new File(dataFolder, "userCoinsFile.json");
    //region 数据访问方法
    public boolean hasEnoughCoins(Player player, long amount) {
        return userData.get(player.getName()).getCoins() >= amount;
    }
    public void addCoins(Player player, long amount) {
        userData.get(player.getName()).setCoins(userData.get(player.getName()).getCoins() + amount);
    }
    public void deductCoins(Player player, long amount) {
        userData.get(player.getName()).setCoins(userData.get(player.getName()).getCoins() - amount);
    }
    public static Map<String,Boolean> white = new HashMap<>();

    //endregion
    public void re() {
        loadData();
    }
    // 加载数据
    public void loadData() {
        try {
            if (!dataFolder.exists()) dataFolder.mkdirs();

            if (!settleDataFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(settleDataFile))) {
                    Gson gson = new Gson();
                    settleData = gson.fromJson(reader, new TypeToken<Map<String, ArrayList<String>>>(){}.getType());
                }
            }
            if (userDataFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(userDataFile))) {
                    Gson gson = new Gson();
                    String json = reader.lines().reduce("", String::concat);
                    if (json.isEmpty()) {
                        System.out.println("File is empty or invalid.");
                    } else {
                        userData = gson.fromJson(json, new TypeToken<Map<String, User>>() {}.getType());
                        for (Map.Entry<String, User> entry : userData.entrySet()) {
                            User user = entry.getValue();
                            System.out.println("User: " + user.getName() + ", Coins: " + user.getCoins());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            getLogger().severe("加载用户数据失败！");
            e.printStackTrace();
        }
    }

    private boolean readPlayFile() {
        Yaml yaml = new Yaml();
        try (InputStream in = java.nio.file.Files.newInputStream(playFile.toPath())) {
            // 先加载整个文件为 Map
            Map<String, Object> root = yaml.load(in);

            if (root == null) {
                getLogger().warning("playFile Empty");
                return true;
            }

            // 获取 data 节点
            Object dataObj = root.get("data");
            if (!(dataObj instanceof Map)) {
                getLogger().severe("playFile 中缺少有效的 'data' 字段");
                return true;
            }

            Map<String, Object> data = (Map<String, Object>) dataObj;

            // 构建结果 map: name -> wins
            Map<String, Integer> playsMap = new HashMap<>();

            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String uuid = entry.getKey();
                Object playerData = entry.getValue();

                if (!(playerData instanceof Map)) {
                    continue; // 忽略非 Map 类型数据
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> stats = (Map<String, Object>) playerData;

                Object nameObj = stats.get("name");
                Object winsObj = stats.get("wins");

                if (nameObj instanceof String && winsObj instanceof Integer) {
                    String name = (String) nameObj;
                    int wins = (Integer) winsObj;
                    playsMap.put(name, wins);
                }
            }
        } catch (Exception e) {
            getLogger().severe("读取 playFile 出错！");
            e.printStackTrace();
        }
        return false;
    }

    public Map<String, ArrayList<String>> getSettleData() {
        return settleData;
    }

    public void setSettleData(Map<String, ArrayList<String>> settleData) {
        this.settleData = settleData;
    }

    public void asyncReadPlayFile() {
        Bukkit.getScheduler().runTaskAsynchronously(this, this::readPlayFile);
    }
    public void saveData() {
        Bukkit.getScheduler().runTaskAsynchronously(this, this::asyncSaveData);
    }
    // 保存数据
    public void asyncSaveData() {
        try {
            if (!dataFolder.exists()) dataFolder.mkdirs();
            try {
                try (FileWriter writer = new FileWriter(userDataFile)) {
                    writer.write(gson.toJson(userData));
                    writer.flush();
                    writer.close();
                    getLogger().info("已保存用户数据！");
                }
                try (FileWriter writer = new FileWriter(settleDataFile)) {
                    writer.write(gson.toJson(settleData));
                    writer.flush();
                    writer.close();
                    getLogger().info("已保存地标！");
                }
            }catch (IOException e) {}

        } catch (Exception e) {
            getLogger().severe("保存用户数据失败！");
            e.printStackTrace();
        }
    }


    @Override
    public void onEnable() {
        getServer().getPluginManager().addPermission(new Permission("bedwarspro.use.bp", PermissionDefault.TRUE));
        getServer().getPluginManager().addPermission(new Permission("bedwarspro.use.bps", PermissionDefault.TRUE));
        getServer().getPluginManager().addPermission(new Permission("bedwarspro.use.lobby", PermissionDefault.TRUE));
        getServer().getPluginManager().addPermission(new Permission("bedwarspro.use.plot", PermissionDefault.TRUE));
        getServer().getPluginManager().addPermission(new Permission("bedwarspro.use.auth", PermissionDefault.TRUE));
        getServer().getPluginManager().addPermission(new Permission("bedwarspro.use.settle", PermissionDefault.TRUE));
        loadData();

// Register custom enchantment
        // Register event listener
        Bukkit.getPluginManager().registerEvents(new SmeltingListener(), this);
        getLogger().info("Custom enchantment '熔炼' has been registered!");

        MarketManager.loadMarketItems();

// 在 BedWarsPro.java 的 onEnable 方法中添加：
        GroupWorldManager groupWorldManager = new GroupWorldManager(this);
        InventoryStorage inventoryStorage = new InventoryStorage(this.getDataFolder());
// 在 onEnable 方法中添加
        Bukkit.getPluginManager().registerEvents(new RewardGUIListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(), this);

        Bukkit.getPluginManager().registerEvents(new InventorySyncListener(groupWorldManager, inventoryStorage), this);
// 注册事件监听器
        PlotManager plotManager = new PlotManager(getDataFolder());
        PlotSelectionListener selectionListener = new PlotSelectionListener(plotManager);

        getServer().getPluginManager().registerEvents(selectionListener, this);
        getCommand("settle").setExecutor(new SettleCommand());
        getCommand("plot").setExecutor(new PlotCreateCommand(selectionListener.getPlayerSelections(), plotManager,this));
        getServer().getScheduler().runTaskTimer(this, plotManager::savePlots, 20L * 60 * 5, 20L * 60 * 5); // 每5分钟保存一次

        AuthManager authManager = new AuthManager(getDataFolder());
        AuthCommand authCommand = new AuthCommand(authManager);
        getServer().getPluginManager().registerEvents(new AuthListener(authCommand), this);
        getCommand("auth").setExecutor(authCommand);
        //getCommand("hack").setExecutor(new HackCommand());

        white = new HashMap<>();
        loadWhiteList();
        white.put("Spasol",true);
        white.put("LovelyBoyBZ",true);

        JavaPlugin bedWarsPlugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin("BedWars");
        if (bedWarsPlugin == null) {
            throw new IllegalStateException("BedWars 插件未加载，无法初始化 playFile！");
        }

        // 构建目标目录 plugins/BedWars/database/
        File databaseFolder = new File(bedWarsPlugin.getDataFolder(), "database");

        // 创建目录（如果不存在）
        if (!databaseFolder.exists()) {
            databaseFolder.mkdirs();
        }

        // 设置 playFile 路径为 plugins/BedWars/database/plays.json
        this.playFile = new File(databaseFolder, "bw_stats_players.yml");

        saveDefaultConfig();
        professionList = getConfig().getStringList("professions");
        getLogger().info("BedWars plugin has started!");
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ProfessionMenuListener(this), this);
        getServer().getPluginManager().registerEvents(new HubWorldManager(this), this);
        //getServer().getPluginManager().registerEvents(new ReisaClickerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GameMenuClickListener(), this);
// 在 BedWarsPro.java 的 onEnable() 中添加
        this.getCommand("bps").setExecutor(new BPSuivialCommand());
        this.getCommand("enchant").setExecutor(new EnchantCommand(this));

        Bukkit.getPluginManager().registerEvents(new TeleportConfirmListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerStatsGUIListener(), this);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            swordSaintDamageBuff.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }, 20L, 20L); // 每秒检查一次
        // 注册 /hub 指令
        // 加载击杀和地址数据
        // 启动定时清除任务
        if (getConfig().getBoolean("clear.enabled")) {
            int intervalMinutes = getConfig().getInt("clear.interval_minutes", 1);
            Bukkit.getScheduler().runTaskTimer(this, new HubClearTask(this), 0L, intervalMinutes * 20L * 60L);
        }
    }

    private void loadWhiteList() {
        File whiteListFile = new File(getDataFolder(), "whiteList");

        if (!whiteListFile.exists()) {
            try {
                // 如果文件不存在，创建一个空文件（可选）
                whiteListFile.createNewFile();
                try (FileWriter writer = new FileWriter(whiteListFile)) {
                    writer.write("Spasol\nLovelyBoyBZ\n"); // 可选默认内容
                }
            } catch (IOException e) {
                getLogger().severe("无法创建 whiteList 文件！");
                e.printStackTrace();
            }
            return;
        }

        try {
            List<String> lines = Files.readLines(whiteListFile, StandardCharsets.UTF_8);
            for (String line : lines) {
                String name = line.trim();
                if (!name.isEmpty()) {
                    white.put(name, true);
                }
            }
            getLogger().info("成功加载 " + white.size() + " 个白名单玩家");
        } catch (IOException e) {
            getLogger().severe("读取 whiteList 文件失败！");
            e.printStackTrace();
        }
    }

    public Map<String, String> getUserPro() {
        return userPro;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        for (String c: args) {
            System.out.println(c);
        }


        if (command.getName().equalsIgnoreCase("lobby")) {
            Player player = (Player) sender;

            String hubWorldName = this.getConfig().getString("hub.name", "world");
            World hubWorld = Bukkit.getWorld(hubWorldName);

            if (hubWorld == null) {
                player.sendMessage("§c大厅世界不存在或未加载！");
                return true;
            }

            player.teleport(hubWorld.getSpawnLocation());
            player.sendMessage("§a已传送至大厅世界 §b" + hubWorldName);
            return true;
        }
        if (command.getName().equalsIgnoreCase("bp")) {

            if (args.length >= 2 && args[0].equalsIgnoreCase("set")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    String profession = args[1];
                    if (!professionList.contains(profession)) {
                        return false;
                    }
                    // Check if the player has at least 3 gold ingots
                    int goldCount = 0;
                    for (org.bukkit.inventory.ItemStack item : player.getInventory().getContents()) {
                        if (item != null && item.getType() == org.bukkit.Material.GOLD_INGOT) {
                            goldCount += item.getAmount();
                        }
                    }

                    if (goldCount >= 3) {
                        // Remove 3 gold ingots from the player's inventory
                        int goldToRemove = 3;
                        for (org.bukkit.inventory.ItemStack item : player.getInventory().getContents()) {
                            if (item != null && item.getType() == org.bukkit.Material.GOLD_INGOT) {
                                int amount = item.getAmount();
                                if (amount <= goldToRemove) {
                                    player.getInventory().remove(item);
                                    goldToRemove -= amount;
                                } else {
                                    item.setAmount(amount - goldToRemove);
                                    break;
                                }
                            }
                        }
                        userPro.put(player.getName(),profession);
                        // Activate the profession (you can customize this logic)
                        player.sendMessage("Profession " + profession + " activated!");
                    } else {
                        player.sendMessage("You need at least 3 gold ingots to activate a profession!");
                    }
                } else {
                    //获取第三项用户名作为player
                    Player player = Bukkit.getPlayer(args[2]);
                    System.out.println(args[2]);
                    String profession = args[1];
                    if (!professionList.contains(profession)) {
                        return false;
                    }
                    // Check if the player has at least 3 gold ingots
                    int goldCount = 0;
                    for (org.bukkit.inventory.ItemStack item : player.getInventory().getContents()) {
                        if (item != null && item.getType() == org.bukkit.Material.GOLD_INGOT) {
                            goldCount += item.getAmount();
                        }
                    }

                    if (goldCount >= 3) {
                        // Remove 3 gold ingots from the player's inventory
                        int goldToRemove = 3;
                        for (org.bukkit.inventory.ItemStack item : player.getInventory().getContents()) {
                            if (item != null && item.getType() == org.bukkit.Material.GOLD_INGOT) {
                                int amount = item.getAmount();
                                if (amount <= goldToRemove) {
                                    player.getInventory().remove(item);
                                    goldToRemove -= amount;
                                } else {
                                    item.setAmount(amount - goldToRemove);
                                    break;
                                }
                            }
                        }
                        userPro.put(player.getName(),profession);
                        // Activate the profession (you can customize this logic)
                        player.sendMessage("Profession " + profession + " activated!");
                    } else {
                        player.sendMessage("You need at least 3 gold ingots to activate a profession!");
                    }
                }
                return true;

            } if (args.length >= 1 && args[0].equalsIgnoreCase("open")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    Inventory gui = ProfessionGUI.getProfessionGUI(this, player);
                    player.openInventory(gui);
                } else {
                    sender.sendMessage("Only players can use this command.");
                }
                return true;
            } else if (args.length >= 1 && args[0].equalsIgnoreCase("menu")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    Inventory gui = PlayerStatsGUI.createPlayerStatsGUI(player, this);
                    player.openInventory(gui);
                } else {
                    sender.sendMessage("Only players can use this command.");
                }
                return true;
            } else  if (args.length >= 1 && args[0].equalsIgnoreCase("coin")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    Inventory gui = RewardGUI.getRewardGUI(player, this,0); // 默认打开第一页
                    player.openInventory(gui);
                } else {
                    sender.sendMessage("Only players can use this command.");
                }
                return true;
            }else if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (!player.getName().equals("Spasol")) {
                        player.sendMessage("§c你没有权限使用此命令");
                        return true;
                    }
                    reloadConfig();
                    player.sendMessage("§a配置文件已重载");
                } else {
                    reloadConfig();
                }
            }
        }
        return false;
    }
    @Override
    public void onDisable() {
        MarketManager.saveMarketItems();
        saveData(); // 关闭前保存一次
    }
    public FileConfiguration getGameConfig() {
        return getConfig();
    }

    public Map<String, User> getUserData() {
        return userData;
    }
}