package org.ast.bedwarspro;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.ast.bedwarspro.command.BPSuivialCommand;
import org.ast.bedwarspro.gui.PlayerStatsGUI;
import org.ast.bedwarspro.gui.ProfessionGUI;
import org.ast.bedwarspro.listener.GameMenuClickListener;
import org.ast.bedwarspro.listener.InventorySyncListener;
import org.ast.bedwarspro.listener.PlayerStatsGUIListener;
import org.ast.bedwarspro.listener.TeleportConfirmListener;
import org.ast.bedwarspro.mannger.GroupWorldManager;
import org.ast.bedwarspro.mannger.HubWorldManager;
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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ast.bedwarspro.PlayerDamageListener.swordSaintDamageBuff;

public final class BedWarsPro extends JavaPlugin {
    private List<String> professionList = new ArrayList<>();
    private Map<String,String> userPro = new HashMap<>();

    private Map<String, Integer> user2kill;
    private Map<String, Long> user2addr;
    private Map<String, Integer> plays;
    private Map<String, Integer> user2death;

    private final File dataFolder = new File(getDataFolder(), "data");
    private final File killFile = new File(dataFolder, "user2kill.json");
    private final File addrFile = new File(dataFolder, "user2addr.json");
    private final File user2deathFile = new File(dataFolder, "user2death.json");

    private final File playFile = new File(dataFolder, "plays.json");

    private final Gson gson = new Gson();
    public Map<String, Long> userCoins = new HashMap<>(); // 金币存储
    private final File userCoinsFile = new File(dataFolder, "userCoinsFile.json");

    public boolean hasEnoughCoins(Player player, long amount) {
        return userCoins.getOrDefault(player.getName(), 0L) >= amount;
    }
    public void deductCoins(Player player, long amount) {
        userCoins.put(player.getName(), userCoins.getOrDefault(player.getName(), 0L) - amount);
    }
    public long getCoins(Player player) {
        return userCoins.getOrDefault(player.getName(), 0L);
    }
    public Map<String, Integer> getUser2death() {
        return user2death;
    }

    public void setUser2death(Map<String, Integer> user2death) {
        this.user2death = user2death;
    }

    public Map<String, Integer> getUser2kill() {
        return user2kill;
    }

    public Map<String, Integer> getPlays() {
        return plays;
    }

    public void setPlays(Map<String, Integer> plays) {
        this.plays = plays;
    }

    public void setUser2kill(Map<String, Integer> user2kill) {
        this.user2kill = user2kill;
    }

    public Map<String, Long> getUser2addr() {
        return user2addr;
    }
    public static Map<String,Boolean> white = new HashMap<>();

    public void setUser2addr(Map<String, Long> user2addr) {
        this.user2addr = user2addr;
    }
    public void re() {
        user2kill = new HashMap<>();
        user2addr = new HashMap<>();
        this.plays = new HashMap<>();
        user2death = new HashMap<>();

    }
    // 加载数据
    public void loadData() {
        try {
            if (!dataFolder.exists()) dataFolder.mkdirs();
            if (killFile.exists()) {
                Type mapType = new TypeToken<Map<String, Integer>>(){}.getType();
                Map<String, Integer> kills = gson.fromJson(new FileReader(killFile), mapType);
                if (kills != null && !kills.isEmpty()) {
                    user2kill = (kills);
                    System.out.println("Yes:" + kills.size());
                } else {
                    System.out.println("Content:" + gson.toJson(user2kill));
                    System.out.println("NullError:" + killFile.getName());
                }
            }

            if (addrFile.exists()) {
                Type mapType = new TypeToken<Map<String, Long>>(){}.getType(); // 修改为 Map<String, Long>
                Map<String, Long> addresses = gson.fromJson(new FileReader(addrFile), mapType);
                if (addresses != null && !addresses.isEmpty()) {
                    user2addr = (addresses);
                    System.out.println("Yes:" + addresses.size());
                }else {
                    System.out.println("Content:" + gson.toJson(user2addr));
                    System.out.println("NullError:" + addrFile.getName());
                }
            }

            if (playFile.exists()) {
                Type mapType = new TypeToken<Map<String, Integer>>(){}.getType();
                Map<String, Integer> plays = gson.fromJson(new FileReader(playFile), mapType);
                if (plays != null && !plays.isEmpty()) {
                    this.plays = (plays);
                    System.out.println("Yes:" + plays.size());
                }else {
                    //输出内容
                    System.out.println("Content:" + gson.toJson(plays));
                    System.out.println("NullError:" + playFile.getName());
                }
            }
            if (user2deathFile.exists()) {
                Type mapType = new TypeToken<Map<String, Integer>>(){}.getType();
                Map<String, Integer> deaths = gson.fromJson(new FileReader(user2deathFile), mapType);
                if (deaths != null && !deaths.isEmpty()) {
                    user2death = (deaths);
                    System.out.println("Yes:" + deaths.size());
                }else {
                    //输出内容
                    System.out.println("Content:" + gson.toJson(deaths));
                    System.out.println("NullError:" + user2deathFile.getName());
                }
            }
            if (userCoinsFile.exists()) {
                Type mapType = new TypeToken<Map<String, Long>>(){}.getType();
                Map<String, Long> coins = gson.fromJson(new FileReader(userCoinsFile), mapType);
                if (coins != null && !coins.isEmpty()) {
                    userCoins = (coins);
                    System.out.println("Yes:" + coins.size());
                }else {
                    //输出内容
                    System.out.println("Content:" + gson.toJson(coins));
                    System.out.println("NullError:" + userCoinsFile.getName());
                }
            }
        } catch (Exception e) {
            getLogger().severe("加载用户数据失败！");
            e.printStackTrace();
        }
    }

    // 保存数据
    public void saveData() {
        try {
            if (!dataFolder.exists()) dataFolder.mkdirs();
            try (FileWriter killWriter = new FileWriter(killFile)) {
                killWriter.write(gson.toJson(user2kill));
            }
            try (FileWriter addrWriter = new FileWriter(addrFile)) {
                addrWriter.write(gson.toJson(user2addr));
            }
            try (FileWriter playWriter = new FileWriter(playFile)) {
                playWriter.write(gson.toJson(plays));
            }
            try (FileWriter deathWriter = new FileWriter(user2deathFile)) {
                deathWriter.write(gson.toJson(user2death));
            }
            try (FileWriter coinWriter = new FileWriter(userCoinsFile)) {
                coinWriter.write(gson.toJson(userCoins));
            }
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
// 在 BedWarsPro.java 的 onEnable 方法中添加：
        GroupWorldManager groupWorldManager = new GroupWorldManager(this);
        InventoryStorage inventoryStorage = new InventoryStorage(this.getDataFolder());

        Bukkit.getPluginManager().registerEvents(new InventorySyncListener(groupWorldManager, inventoryStorage), this);

        white = new HashMap<>();
        loadWhiteList();
        white.put("Spasol",true);
        white.put("LovelyBoyBZ",true);


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
        Bukkit.getPluginManager().registerEvents(new TeleportConfirmListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerStatsGUIListener(), this);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            swordSaintDamageBuff.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }, 20L, 20L); // 每秒检查一次
        // 注册 /hub 指令
        // 加载击杀和地址数据
        loadData();
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
            }
        }
        return false;
    }
    @Override
    public void onDisable() {
        saveData(); // 关闭前保存一次
    }
    public FileConfiguration getGameConfig() {
        return getConfig();
    }
}