package org.ast.bedwarspro;

import org.bukkit.Bukkit;
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

public final class BedWars extends JavaPlugin {
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

    }
    public Map<String, String> getUserPro() {
        return userPro;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
                    sender.sendMessage("Only players can use this command.");
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