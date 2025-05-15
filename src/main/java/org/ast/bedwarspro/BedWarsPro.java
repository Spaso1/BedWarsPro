package org.ast.bedwarspro;

import org.ast.bedwarspro.command.HubCommand;
import org.ast.bedwarspro.task.HubClearTask;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ast.bedwarspro.PlayerDamageListener.swordSaintDamageBuff;

public final class BedWarsPro extends JavaPlugin {
    private List<String> professionList = new ArrayList<>();
    private Map<String,String> userPro = new HashMap<>();
    @Override
    public void onEnable() {
        saveDefaultConfig();
        professionList = getConfig().getStringList("professions");
        getLogger().info("BedWars plugin has started!");
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ProfessionMenuListener(this), this);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            swordSaintDamageBuff.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }, 20L, 20L); // 每秒检查一次
        // 注册 /hub 指令

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
    }
    public FileConfiguration getGameConfig() {
        return getConfig();
    }
}