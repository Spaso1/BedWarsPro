package org.ast.bedwarspro.command;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.ast.bedwarspro.gui.MarketGUI;
import org.ast.bedwarspro.gui.OfficialShopGUI;
import org.ast.bedwarspro.mannger.MarketManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BPSuivialCommand implements CommandExecutor {
    // 添加在类顶部
    private static final Map<String, String> pendingRequests = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以使用此命令");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("§c请提供子命令: menu, enchant, randomTp, tp, hub");
            return true;
        }
        if (args[0].equals("accepttp")) {
            Player target = (Player) sender;
            if (args.length < 2) {
                target.sendMessage("§c用法: /bps accepttp <玩家>");
                return true;
            }

            String requesterName = args[1];
            if (!pendingRequests.containsKey(target.getName()) || !pendingRequests.get(target.getName()).equals(requesterName)) {
                target.sendMessage("§c没有来自该玩家的传送请求");
                return true;
            }

            Player requester = Bukkit.getPlayer(requesterName);
            if (requester == null || !requester.isOnline()) {
                target.sendMessage("§c请求者已离线");
                pendingRequests.remove(target.getName());
                return true;
            }

            // 执行传送
            requester.teleport(target.getLocation());
            target.sendMessage("§a你已允许 §f" + requester.getName() + " §a传送到你身边");
            requester.sendMessage("§a你已成功传送到 §f" + target.getName() + " §a身边");

            // 清除请求
            pendingRequests.remove(target.getName());
            return true;
        }
        if (args.length >= 1 && args[0].equalsIgnoreCase("market")) {
            if (args.length == 1) {
                MarketGUI.openMarket(player,0);

            } else if (args.length == 3 && args[1].equalsIgnoreCase("add")) {
                try {
                    double price = Double.parseDouble(args[2]);
                    ItemStack item = player.getInventory().getItemInHand();
                    if (item == null || item.getType() == Material.AIR) {
                        player.sendMessage("§cYou must hold an item to list it!");
                        return true;
                    }
                    MarketManager.addItem(player, item, price);
                    player.getInventory().setItemInHand(null);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cInvalid price!");
                }
            }
            return true;
        } else if (args.length >= 1 && args[0].equalsIgnoreCase("offical")) {
            OfficialShopGUI.openShop(player);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "menu":
                openWorldMenu(player);
                break;
            case "enchant":
                openEnchantGUI(player);
                break;
            case "randomtp":
                randomTeleport(player);
                break;
            case "tp":
                if (args.length < 2) {
                    player.sendMessage("§c用法: /bps tp <玩家>");
                    return true;
                }
                teleportToPlayer(player, args[1]);
                break;
            case "hub":
                teleportToHub(player);
                break;
            default:
                player.sendMessage("§c未知的子命令: " + args[0]);
                break;
        }

        return true;
    }


    // 打开世界选择菜单
    private void openWorldMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 3 * 9, "生存世界菜单");

        addWorldItem(gui, 10, Material.GRASS, "主世界", "bps_w");
        addWorldItem(gui, 12, Material.NETHERRACK, "地狱", "bps_w_n");
        addWorldItem(gui, 14, Material.ENDER_STONE, "末地", "bps_w_theEnd");

        player.openInventory(gui);
    }

    private void addWorldItem(Inventory gui, int slot, Material material, String name, String worldName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§a" + name);
        ArrayList<String> lore = new ArrayList<>();
        lore.add("§7点击传送到该世界");
        meta.setLore(lore);
        item.setItemMeta(meta);

        gui.setItem(slot, item);
    }

    // 打开附魔面板
    private void openEnchantGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 5 * 9, "附魔商店");

        // 信息页
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§b附魔说明");
        ArrayList<String> infoLore = new ArrayList<>();
        infoLore.add("§7点击任意附魔书来购买");
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        gui.setItem(0, infoItem);

        // 锋利 X
        addEnchantBook(gui, 10, "锋利", "DAMAGE_ALL", 10, 10000);

        // 保护 X
        addEnchantBook(gui, 11, "保护", "PROTECTION_ENVIRONMENTAL", 10, 10000);

        // 力量 X
        addEnchantBook(gui, 12, "力量", "ARROW_DAMAGE", 10, 10000);

        // 火焰附加 X
        addEnchantBook(gui, 13, "火焰附加", "FIRE_ASPECT", 10, 10000);

        // 荆棘 X
        addEnchantBook(gui, 14, "荆棘", "THORNS", 10, 10000);

        // 耐久 X
        addEnchantBook(gui, 15, "耐久", "DURABILITY", 10, 10000);

        player.openInventory(gui);
    }
    private void addEnchantBook(Inventory gui, int slot, String name, String enchantKey, int level, long cost) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        meta.setDisplayName("§a" + name + " X"); // 统一显示为 X 级
        ArrayList<String> lore = new ArrayList<>();
        lore.add("§7花费: §e" + cost + " 金币");
        lore.add("§8enchant_key:" + enchantKey); // 隐藏字段供后续读取
        lore.add("§8level:10"); // 固定为10级
        lore.add("§8cost:" + cost);
        meta.setLore(lore);
        book.setItemMeta(meta);
        gui.setItem(slot, book);
    }

    // 随机传送
    private void randomTeleport(Player player) {
        World world = player.getWorld();
        String worldName = world.getName();

        if (!worldName.equals("bps_w") && !worldName.equals("bps_w_n") && !worldName.equals("bps_w_theEnd")) {
            player.sendMessage("§c你只能在指定的三个世界中随机传送");
            return;
        }

        int x = (int) (Math.random() * 20000 - 10000);
        int z = (int) (Math.random() * 20000 - 10000);
        int y = world.getHighestBlockYAt(x, z) + 1;

        player.teleport(world.getSpawnLocation().clone().add(x, y, z));
        player.sendMessage("§a你在当前世界随机传送了！");
    }

    // 传送到指定玩家
    private void teleportToPlayer(Player player, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage("§c玩家不存在或不在线");
            return;
        }

        if (player.getName().equals(targetName)) {
            player.sendMessage("§c你不能传送给自己");
            return;
        }
        if (!player.getWorld().getName().startsWith("bps")) {
            return;
        }

        //判断世界是不是bps开头
        if (!target.getWorld().getName().startsWith("bps")) {
            player.sendMessage("§c目标玩家不在指定的世界中");
            return;
        }

        // 记录请求
        pendingRequests.put(target.getName(), player.getName());

        // 发送确认信息给目标玩家
        target.sendMessage(" ");
        target.sendMessage("§7--------------------");

        // 创建可点击的文本
        TextComponent message = new TextComponent("§a[点击此处] 同意传送请求");
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bps accepttp " + player.getName()));
        target.spigot().sendMessage(message);

        target.sendMessage("§7--------------------");
        target.sendMessage(" ");

        player.sendMessage("§a已向 §f" + target.getName() + " §a发送传送请求");
    }
    // 返回大厅
    private void teleportToHub(Player player) {
        World hubWorld = Bukkit.getWorld("world"); // 替换为你的大厅世界名称
        if (hubWorld == null) {
            player.sendMessage("§c大厅世界未加载");
            return;
        }

        player.teleport(hubWorld.getSpawnLocation());
        player.sendMessage("§a你已传送到大厅");
    }
}
