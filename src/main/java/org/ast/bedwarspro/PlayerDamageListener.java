package org.ast.bedwarspro;

import org.ast.bedwarspro.been.User;
import org.ast.bedwarspro.gui.ProfessionGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

import static org.ast.bedwarspro.listener.ReisaClickerListener.getIsNeedExecute;

public class PlayerDamageListener implements Listener {
    private final BedWarsPro plugin;
    private final Map<UUID, Long> lastSneakPress = new HashMap<>();
    private final Map<UUID, Long> ninjaCooldown = new HashMap<>();
    public static final Map<UUID, DamageBuff> swordSaintDamageBuff = new HashMap<>();
    private static final Random random = new Random();
    // 记录玩家是否处于战斗状态及最后活动时间
    private final Map<UUID, Long> combatCooldown = new HashMap<>();
    private Map<String,List<String>> playering = new HashMap<>();
    private List<String> hasChange = new ArrayList<>();
    private Map<String,Integer> userNeed = new HashMap<>();
    private final Map<UUID, Long> justiceCooldown = new HashMap<>();
    private final Map<UUID, ItemStack> justiceOriginalWeapons = new HashMap<>();
    private final Map<String, Integer> healthTaskIds = new HashMap<>(); // 添加到类成员变量

    public PlayerDamageListener(BedWarsPro plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        String fromWorld = event.getFrom().getWorld().getName();
        String toWorld = event.getTo().getWorld().getName();

        if (!fromWorld.equals(toWorld)) {
            handleWorldChange(player);
        }

        World world = player.getWorld();
        if (!world.getName().startsWith("bedworld")) {
            return;
        }

        // 如果进入 bedworld 开头的地图，设置最大血量为 40
        if (world.getName().startsWith("bedworld")) {
            if (player.getMaxHealth() != 40) {
                player.setMaxHealth(40);
                player.setHealth(40);
                if (playering.containsKey(world.getName())) {
                    ArrayList<String> playering = new ArrayList<>();
                    playering.add(player.getName());
                }else {
                    ArrayList<String> playering2 = new ArrayList<>();
                    playering2.add(player.getName());
                    playering.put(world.getName(), playering2);
                }
                player.sendMessage("§a你进入了 BedWorld，最大生命值已设为 20❤");
            }
        } else {
            player.resetMaxHealth();
        }
        // === 职业选择钻石逻辑开始 ===
        boolean hasSelectorDiamond = false;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.DIAMOND && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();

                if (meta != null && meta.hasDisplayName() && "§6职业选择".equals(meta.getDisplayName())) {
                    hasSelectorDiamond = true;
                    break;
                }
            }
        }

        if (!hasSelectorDiamond) {
            ItemStack professionDiamond = new ItemStack(Material.DIAMOND);
            ItemMeta meta = professionDiamond.getItemMeta();

            if (meta != null) {
                meta.setDisplayName("§6职业选择");
                List<String> lore = new ArrayList<>();
                lore.add("§e右键点击选择职业");
                meta.setLore(lore);
                professionDiamond.setItemMeta(meta);
            }

            player.getInventory().addItem(professionDiamond);
            player.sendMessage("§e你获得了一个 §6职业选择 §e钻石，右键点击它来选择职业！");
        }
        // === 职业选择钻石逻辑结束 ===
    }

    @EventHandler
    public void onPlayerPickupItem(org.bukkit.event.player.PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem().getItemStack();

        World world = player.getWorld();
        if (!world.getName().startsWith("bedworld")) {
            return;
        }

        if (item.getType() == Material.CLAY_BRICK) {  // 修改这里
            if (!hasChange.contains(player.getName())) {
                //获取同一个场次的所有人Rating
                List<String> playersInSameMatch = playering.get(world.getName());
                double maxRa = 0;
                for (String name : playersInSameMatch) {
                    double rating = getRating(name);
                    if (rating > maxRa) {
                        maxRa = rating;
                    }
                }
                int rat = (int) (maxRa / getRating(player.getName()));
                hasChange.add(player.getName());
                //每10分钟增加玩家20血量

                // 存储定时器任务 ID
                int taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    if (player.isOnline()) {
                        player.setMaxHealth(player.getMaxHealth() + 20);
                        player.sendMessage("§a你获得了20点最大生命值！");
                    }
                }, 10 * 60 * 20L, 10 * 60 * 20L).getTaskId(); // 每10分钟执行一次

                healthTaskIds.put(player.getName(), taskId);
            }
        }
    }
    @EventHandler
    public void onItemDespawn(org.bukkit.event.entity.ItemDespawnEvent event) {
        // 获取掉落物所在的 World 对象
        World world = event.getEntity().getWorld();

        // 检查是否是 bedworld 开头的地图
        if (!world.getName().startsWith("bedworld")) {
            return;
        }

        // 获取掉落的物品
        ItemStack itemStack = event.getEntity().getItemStack();

        // 判断是否是空玻璃瓶（Material.AIR 在旧版本可能为 Material.GLASS_BOTTLE）
        if (itemStack.getType() == Material.GLASS_BOTTLE && itemStack.getAmount() == 1) {
            // 取消该物品的自然消失
            event.setCancelled(true);

            // 删除该掉落物实体
            event.getEntity().remove();
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        if (!world.getName().startsWith("bedworld")) {
            return;
        }

        ItemStack item = player.getInventory().getItemInHand(); // 适用于 1.12 及以下

        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
                && item != null && item.getType() == Material.DIAMOND && item.hasItemMeta()) {

            ItemMeta meta = item.getItemMeta();
            if (meta != null && "§6职业选择".equals(meta.getDisplayName())) {
                ProfessionGUI.openProfessionGUI(plugin, player);
                event.setCancelled(true); // 防止误操作方块或实体
            }
        }

        // 判官技能触发
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
                && item != null && isWeapon(item.getType())) {

            String profession = plugin.getUserPro().getOrDefault(player.getName(), "None");
            if (profession.contains("判官")) {
                if (!isJusticeOnCooldown(player)) {
                    int level = 10;
                    org.bukkit.enchantments.Enchantment enchantment = org.bukkit.enchantments.Enchantment.getByName("DAMAGE_ALL");
                    if (!(item.getType().name().contains("SWORD"))) {
                        player.sendMessage("§c请手持一个剑");
                        return;
                    }
                    // 添加附魔（强制覆盖已有同名附魔）
                    if (item.containsEnchantment(enchantment)) {
                        item.removeEnchantment(enchantment);
                    }
                    item.addUnsafeEnchantment(enchantment, level);
                    player.updateInventory();
                    player.sendMessage("§a已成功为你的物品添加 §b锋利X");
// 获取物品的唯一标识
                    ItemMeta meta = item.getItemMeta();
                    List<String> lore = new ArrayList<>();
                    lore.add("§c§l判官专属武器");
                    lore.add("§7禁止丢弃或移动");
                    meta.setLore(lore);
                    meta.addEnchant(Enchantment.DAMAGE_ALL, 10, true);
                    item.setItemMeta(meta);

                    justiceOriginalWeapons.put(player.getUniqueId(), item);
                    justiceCooldown.put(player.getUniqueId(), System.currentTimeMillis());

                    player.sendMessage("§a判官技能激活！5秒内获得锋利X效果");

                    // 5秒后武器消失
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (player.isOnline()) {
                            for (ItemStack item2 : player.getInventory().getContents()) {
                                if (item2 != null && item2.hasItemMeta() && item2.getItemMeta().hasLore()) {
                                    for (String loreLine : item2.getItemMeta().getLore()) {
                                        if (loreLine.contains("判官专属武器")) {
                                            player.getInventory().remove(item2);
                                        }
                                    }
                                }
                            }
                            player.sendMessage("§c判官技能：武器已消失！");
                        }
                    }, 5 * 20L);

                    event.setCancelled(true);
                }
            }
        }
    }
    // 检查物品是否是判官武器
    private boolean isJusticeWeapon(ItemStack item) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            return item.getItemMeta().getLore().contains("§c§l判官专属武器");
        }
        return false;
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemDrop(PlayerDropItemEvent event) {
        Item droppedItem = event.getItemDrop();
        if (isJusticeWeapon(droppedItem.getItemStack())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§c判官武器无法丢弃！");
        }
    }

    // 阻止判官武器移动到其他槽位
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        ItemStack clickedItem = event.getCurrentItem();
        if (isJusticeWeapon(clickedItem)) {
            event.setCancelled(true);
            ((Player) event.getWhoClicked()).sendMessage("§c判官武器无法移动！");
        }
    }
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        handleWorldChange(player);
    }

    private void handleWorldChange(Player player) {
        String currentWorld = player.getWorld().getName();
        String profession = plugin.getUserPro().getOrDefault(player.getName(), "None");

        if (!currentWorld.startsWith("bedworld")) {
            if (!profession.equals("None")) {
                // 取消定时增加血量的任务
                Integer taskId = healthTaskIds.remove(player.getName());
                if (taskId != null) {
                    Bukkit.getScheduler().cancelTask(taskId);
                }

                plugin.getUserPro().remove(player.getName());
                User user = plugin.getUser(player.getName());
                if (user.getPlays()!=0) {
                    user.setPlays(user.getPlays()+1);
                }else {
                    user.setPlays(1);
                }
                sendMatchSummary(player);
                playering.get(currentWorld).remove(player.getName());
                hasChange.remove(player.getName());
                // 移除力量效果
                player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
                userNeed.remove(player.getName());
            }
        } else {
            if (!profession.equals("None")) {
                player.sendMessage("§d欢迎回来！你当前的职业为：" + profession);
            }
        }
    }
    private void sendMatchSummary(Player player) {
        String name = player.getName();

        User user = plugin.getUser(name);
        int kills = user.getKills();
        int deaths = user.getDeaths();
        long totalDamage = user.getAddr();

        player.sendMessage("--------------------------------");
        player.sendMessage("§a【战绩总结】");
        player.sendMessage("§f赛季击杀数: §c" + kills);
        player.sendMessage("§f赛季死亡数: §c" + deaths);
        player.sendMessage("§f总造成伤害: §c" + totalDamage);
        player.sendMessage("§fBW Rating: §c" + getRating(name));
        player.sendMessage("-------------------------------");
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
            Entity damager = event.getDamager();
            if (damager instanceof Arrow) {
                Player attacker = (Player) ((Arrow) damager).getShooter();
                String attackerProfession = plugin.getUserPro().getOrDefault(attacker.getName(), "None");
                if (attackerProfession.contains("箭神") && event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
                    doJianShen(attacker,event);
                }else if (attackerProfession.contains("神射手") && event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
                    doShenJianShou(attacker, event);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        // 首先检查事件是否被取消
        if (event.isCancelled()) {
            return;
        }
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Player attacker = (Player) event.getDamager();


            World world = victim.getWorld();
            if (!world.getName().startsWith("bedworld")) {
                return;
            }
            // Retrieve professions
            String victimProfession = plugin.getUserPro().getOrDefault(victim.getName(), "None");
            String attackerProfession = plugin.getUserPro().getOrDefault(attacker.getName(), "None");
            // 更新战斗状态
            updateCombatStatus(attacker);
            updateCombatStatus(victim);
            if (attackerProfession.contains("战士")) {
                doZhanShi(attacker, victim,event);
            }else if (attackerProfession.contains("剑圣")) {
                doJianSheng(attacker,victim,event);
            }else  if (attackerProfession.contains("刺客")){
                doCike(attacker,victim,event);
            }
            else {
                swordSaintDamageBuff.remove(attacker.getUniqueId());
            }
            int damage = (int)event.getDamage();
            User user = plugin.getUser(attacker.getName());
            user.setAddr(damage+user.getAddr());

            // Cancel knockback direction for fall damage
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL && event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                player.setVelocity(new Vector(0, 0, 0)); // Reset velocity to cancel knockback
            }
        }
    }

    private void doPanGu(Player attacker, Player victim, EntityDamageByEntityEvent event) {
        // 检查冷却
        if (isJusticeOnCooldown(attacker)) {
            return;
        }

        // 获取主手物品
        ItemStack weapon = attacker.getInventory().getItemInHand();

        // 检查是否是有效的武器
        if (weapon == null || !isWeapon(weapon.getType())) {
            attacker.sendMessage("§c判官技能需要手持武器！");
            return;
        }

        // 保存原始武器
        justiceOriginalWeapons.put(attacker.getUniqueId(), weapon.clone());

        // 添加锋利X效果
        ItemMeta meta = weapon.getItemMeta();
        meta.addEnchant(Enchantment.DAMAGE_ALL, 10, true);
        weapon.setItemMeta(meta);

        // 设置5秒后武器消失
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (attacker.isOnline()) {
                ItemStack currentWeapon = attacker.getInventory().getItemInHand();
                // 检查是否还是同一把武器
                if (currentWeapon != null && currentWeapon.equals(weapon)) {
                    attacker.getInventory().setItemInHand(new ItemStack(Material.AIR));
                    attacker.sendMessage("§c判官技能：武器已消失！");
                }
                // 恢复冷却
                justiceCooldown.put(attacker.getUniqueId(), System.currentTimeMillis());
            }
        }, 5 * 20L); // 5秒

        attacker.sendMessage("§a判官技能激活！5秒内获得锋利X效果");
        event.setDamage(event.getDamage() * 1.5); // 增加50%伤害
    }

    private boolean isWeapon(Material material) {
        return material.name().endsWith("_SWORD") ||
                material.name().endsWith("_AXE") ||
                material == Material.STICK; // 可以根据需要扩展
    }

    private boolean isJusticeOnCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        if (justiceCooldown.containsKey(uuid)) {
            long remaining = ((justiceCooldown.get(uuid) + 60000) - System.currentTimeMillis()) / 1000;
            if (remaining > 0) {
                player.sendMessage("§c判官技能冷却中，剩余 " + remaining + " 秒");
                return true;
            }
        }
        return false;
    }

    private void updateCombatStatus(Player player) {
        combatCooldown.put(player.getUniqueId(), System.currentTimeMillis());
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        World world = victim.getWorld();
        if (!world.getName().startsWith("bedworld")) {
            return;
        }
        User user = plugin.getUser(victim.getName());
        user.setDeaths(user.getDeaths() + 1);
        // 清空玩家背包
        victim.getInventory().clear();

        if (victim.getKiller() instanceof Player) {
            Player killer = victim.getKiller();
            User killer2 = plugin.getUser(killer.getName());
            killer2.setKills(user.getKills() + 1);
            String profession = plugin.getUserPro().getOrDefault(killer.getName(), "None");

            if (profession.contains("剑圣")) {
                // 设置初始 buff：1.1 倍，持续 10 秒
                swordSaintDamageBuff.put(killer.getUniqueId(), new DamageBuff(1.1, 10000));
                killer.sendMessage("§c你击杀了玩家，获得【剑圣】伤害加成！");

                // 显示带颜色的“斩杀!”标题
                killer.sendTitle("§c§l斩杀!","x" + swordSaintDamageBuff.get(killer.getUniqueId()).multiplier);
            }
        }
    }
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player victim = event.getPlayer();
        if (!victim.getWorld().getName().startsWith("bedworld")) {
            return;
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // 你的原有代码
            if (userNeed.containsKey(victim.getName())) {
                PotionEffect strength = new PotionEffect(
                        PotionEffectType.INCREASE_DAMAGE,
                        Integer.MAX_VALUE,
                        userNeed.get(victim.getName()),
                        true,
                        true
                );
                PotionEffect haste = new PotionEffect(
                        PotionEffectType.FAST_DIGGING, // 急迫
                        60, // 3秒
                        50, // 255级
                        true,
                        true
                );

                victim.addPotionEffect(strength);
                victim.addPotionEffect(haste);
                victim.sendMessage("§a你复活后恢复了永久力量增益！");
            }else{
                // 给予 3 秒的 255 级力量和急迫
                PotionEffect strength = new PotionEffect(
                        PotionEffectType.INCREASE_DAMAGE, // 力量
                        40, // 2秒 = 40 ticks (20 ticks = 1秒)
                        50, // 255级 = 254（等级从0开始计算）
                        true, // 显示粒子效果
                        true // 覆盖已有效果
                );

                PotionEffect haste = new PotionEffect(
                        PotionEffectType.FAST_DIGGING, // 急迫
                        60, // 3秒
                        50, // 255级
                        true,
                        true
                );

                victim.addPotionEffect(strength);
                victim.addPotionEffect(haste);

                victim.sendMessage("§c你复活后获得了 2 秒的 255 级力量和急迫！");
            }
        }, 1L); // 延迟2tick

    }

    @EventHandler
    public void onPlayerToggleSneak(org.bukkit.event.player.PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        World world = player.getWorld();
        if (!world.getName().startsWith("bedworld")) {
            return;
        }
        if (event.isSneaking()) { // 按下 Shift
            long currentTime = System.currentTimeMillis();
            if (lastSneakPress.containsKey(uuid)) {
                long timeDiff = currentTime - lastSneakPress.get(uuid);
                if (timeDiff < 300) { // 双击间隔设为 300ms
                    // 触发双击 Shift 事件
                    String attackerProfession = plugin.getUserPro().getOrDefault(player.getName(), "None");
                    if (attackerProfession.contains("忍者")) {
                        if (!hasCooldown(player)) {
                            doRenZe(player);
                        }
                    }
                    lastSneakPress.remove(uuid); // 避免多次触发
                    return;
                }
            }
            lastSneakPress.put(uuid, currentTime);
        }
    }
    private void doShenJianShou(Player attacker, EntityDamageByEntityEvent event) {
        attacker.sendMessage("§d你用【神射手】射中了敌人,伤害x1.3!");
        event.setDamage(event.getDamage() * 1.3);
    }

    private void doJianSheng(Player attacker, Player victim,EntityDamageByEntityEvent event) {
        UUID attackerUuid = attacker.getUniqueId();
        if (swordSaintDamageBuff.containsKey(attackerUuid)) {
            DamageBuff buff = swordSaintDamageBuff.get(attackerUuid);
            if (!buff.isExpired()) {
                event.setDamage(event.getDamage() * buff.multiplier);
                attacker.sendMessage("§c剑圣连击强化！当前伤害倍率: x" + String.format("%.2f", buff.multiplier));
            }
        } else {
            // 初始化 buff
            swordSaintDamageBuff.put(attackerUuid, new DamageBuff(1.1, 10000));
            event.setDamage(event.getDamage() * 1.1);
            attacker.sendMessage("§c剑圣首次击杀，伤害提升至 x1.1");
        }
    }
    private void doCike(Player attacker, Player victim,EntityDamageByEntityEvent event) {
        //判断是否是背后攻击
        if (isBackAttack(attacker, victim)) {
            attacker.sendMessage("§c你背对攻击了，触发【刺客】技能！");
            event.setDamage(event.getDamage() * 1.5);
        }
    }

    private PotionEffect getPotionEffect(Player player, PotionEffectType type) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.getType().equals(type)) {
                return effect;
            }
        }
        return null;
    }

    private void doJianShen(Player attacker,EntityDamageByEntityEvent event) {
        double a =random.nextDouble();
        if (a > 0.4) {
            return;
        }
        // 获取当前的力量等级
        int currentLevel = 0;

        PotionEffect powerEffect = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, 0);
        attacker.addPotionEffect(powerEffect);
        attacker.sendMessage("§d你获得了力量 " + romanNumeral(currentLevel) + " 效果！");
    }


    @EventHandler
    public void onFallDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        // 只处理摔落伤害
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;

        String profession = plugin.getUserPro().getOrDefault(player.getName(), "None");

        // 检查是否是忍者职业
        if (profession.contains("忍者")) {
            event.setCancelled(true); // 取消摔落伤害
            player.sendMessage("§a你触发了忍者技能：摔落无伤害！");
        }
    }
    private void doRenZe(Player player) {
        getIsNeedExecute().put(player,true);
        Vector direction = player.getLocation().getDirection().normalize(); // 获取视角方向单位向量
        Vector upward = new Vector(0, 1, 0); // 向上方向向量（Y轴正方向）
        Vector totalVelocity = direction.multiply(3).add(upward.multiply(1)); // 添加 Y 轴方向力度 4
        player.setVelocity(totalVelocity); // 设置合成速度

        player.sendMessage("§d你使用忍者技能，技能效果已触发!");

        //5s后移除
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (getIsNeedExecute().get(player)) {
                player.setVelocity(new Vector(0, 0, 0));
                player.sendMessage("§d你使用忍者技能，技能效果已移除!");
                getIsNeedExecute().put(player,false);
            }
        }, 5 * 20L);
        //30s后显示冷却结束
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!getIsNeedExecute().get(player)) {
                player.sendMessage("§d忍者技能，技能冷却结束!");
            }
        }, 30 * 20L);
    }
    @EventHandler
    public void onPlayerDropItem(org.bukkit.event.player.PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        if (!world.getName().startsWith("bedworld")) {
            return;
        }

        if (isInCombat(player)) {
            event.setCancelled(true); // 取消丢物品操作
            player.sendMessage("§c你正处于战斗状态，无法丢弃物品！");
        }
    }

    private void doZhanShi(Player attacker, Player victim,EntityDamageByEntityEvent event) {
        double damage = event.getDamage(); // 获取当前伤害值
        event.setDamage(damage*1.15);
        attacker.sendMessage("§d你使用战士技能，伤害增加15%!伤害:" + damage*1.15);
    }

    // 辅助方法：罗马数字转换
    private String romanNumeral(int number) {
        switch (number) {
            case 0: return "I";
            case 1: return "II";
            case 2: return "III";
            case 3: return "IV";
            case 4: return "V";
            default: return "I";
        }
    }
    /**
     * 判断是否是从背后攻击（135° ~ 225°）
     */
    private boolean isBackAttack(Player attacker, Player victim) {
        // 获取受害者朝向
        Vector victimDirection = victim.getLocation().getDirection().normalize();

        // 获取攻击者到受害者的向量
        Vector attackDirection = attacker.getLocation().toVector()
                .subtract(victim.getLocation().toVector())
                .normalize();

        // 计算两向量的点积
        double dot = victimDirection.getX() * attackDirection.getX() +
                victimDirection.getY() * attackDirection.getY() +
                victimDirection.getZ() * attackDirection.getZ();

        // 计算夹角弧度值（使用 acos）
        double angleRad = Math.acos(dot);

        // 转换为角度（0~180°）
        double angleDeg = Math.toDegrees(angleRad);

        // 判断是否在 135° ~ 225° 范围内
        return angleDeg >= 135 && angleDeg <= 225;
    }
    @EventHandler
    public void onItemSpawn(org.bukkit.event.entity.ItemSpawnEvent event) {
        World world = event.getEntity().getWorld();

        // 只在 bedworld 开头的世界生效
        if (!world.getName().startsWith("bedworld")) {
            return;
        }

        ItemStack itemStack = event.getEntity().getItemStack();

        // 遍历附近实体，寻找相同类型的掉落物进行合并
        for (org.bukkit.entity.Item nearbyItem : world.getEntitiesByClass(org.bukkit.entity.Item.class)) {
            if (nearbyItem == event.getEntity()) continue;

            ItemStack nearbyStack = nearbyItem.getItemStack();

            // 判断是否可以合并（物品类型和NBT相同）
            if (itemStack.isSimilar(nearbyStack) && nearbyItem.getPickupDelay() <= 0) {
                if (event.getEntity().getLocation().distance(nearbyItem.getLocation()) <= 2.0) {
                    int combinedAmount = itemStack.getAmount() + nearbyStack.getAmount();

                    if (combinedAmount <= itemStack.getMaxStackSize()) {
                        // 合并成功，删除当前掉落物，更新附近的掉落物数量
                        nearbyStack.setAmount(combinedAmount);
                        nearbyItem.setItemStack(nearbyStack);
                        event.getEntity().remove(); // 删除新生成的掉落物
                        break;
                    }
                }
            }
        }
    }

    private boolean isInCombat(Player player) {
        UUID uuid = player.getUniqueId();
        if (!combatCooldown.containsKey(uuid)) {
            return false;
        }
        long lastCombatTime = combatCooldown.get(uuid);
        long currentTime = System.currentTimeMillis();
        // 如果最后一次战斗在 3 秒内，则视为处于战斗状态
        return (currentTime - lastCombatTime) < 3000;
    }

    private boolean hasCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        if (ninjaCooldown.containsKey(uuid)) {
            long remaining = ((ninjaCooldown.get(uuid) + 30000) - System.currentTimeMillis()) / 1000;
            if (remaining > 0) {
                player.sendMessage("§c忍者技能冷却中，剩余 " + remaining + " 秒");
                return true;
            }
        }
        ninjaCooldown.put(uuid, System.currentTimeMillis());
        return false;
    }
    private double getRating(String name) {
        try {
            User user = plugin.getUser(name);
            long addr  = user.getAddr();
            double addr2k = addr / 40.0;
            int kill = user.getKills() + 1;
            int death = user.getDeaths() + 1;
            int k_d = kill - death;
            double kd = (double) kill / death;
            return (kd + addr2k) / death * (k_d+1) / (k_d +2);
        }catch (Exception e) {
            // plugin.re();
            e.printStackTrace();
            return 1.0;
        }

    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        justiceCooldown.remove(player.getUniqueId());
        justiceOriginalWeapons.remove(player.getUniqueId());
        ninjaCooldown.remove(player.getUniqueId());
    }
}