package org.ast.bedwarspro;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.ast.bedwarspro.command.HubCommand;
import org.ast.bedwarspro.mannger.HubWorldManager;
import org.ast.bedwarspro.task.HubClearTask;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
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

    public void setUser2addr(Map<String, Long> user2addr) {
        this.user2addr = user2addr;
    }

    // 加载数据
    public void loadData() {
        try {
            if (!dataFolder.exists()) dataFolder.mkdirs();
            if (killFile.exists()) {
                Type mapType = new TypeToken<Map<String, Integer>>(){}.getType();
                Map<String, Integer> kills = gson.fromJson(new FileReader(killFile), mapType);
                if (kills != null) user2kill.putAll(kills);
            }

            if (addrFile.exists()) {
                Type mapType = new TypeToken<Map<String, String>>(){}.getType();
                Map<String, Long> addresses = gson.fromJson(new FileReader(addrFile), mapType);
                if (addresses != null) user2addr.putAll(addresses);
            }
            if (playFile.exists()) {
                Type mapType = new TypeToken<Map<String, Integer>>(){}.getType();
                Map<String, Integer> plays = gson.fromJson(new FileReader(playFile), mapType);
                if (plays != null) this.plays.putAll(plays);
            }
            if (user2deathFile.exists()) {
                Type mapType = new TypeToken<Map<String, Integer>>(){}.getType();
                Map<String, Integer> deaths = gson.fromJson(new FileReader(user2deathFile), mapType);
                if (deaths != null) user2death.putAll(deaths);
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
        } catch (Exception e) {
            getLogger().severe("保存用户数据失败！");
            e.printStackTrace();
        }
    }


    @Override
    public void onEnable() {
        saveDefaultConfig();
        professionList = getConfig().getStringList("professions");
        getLogger().info("BedWars plugin has started!");
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ProfessionMenuListener(this), this);
        getServer().getPluginManager().registerEvents(new HubWorldManager(this), this);

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