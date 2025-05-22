package org.ast.bedwarspro.listener;

import org.ast.bedwarspro.BedWarsPro;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

import static org.ast.bedwarspro.BedWarsPro.white;

public class ReisaClickerListener implements Listener {
    private Map<Player, Location> lastLocation = new HashMap<>();
    private Map<Player, Long> lastMoveTime = new HashMap<>();
    private BedWarsPro plugin;
    private Map<Player, Long> speedViolationStartTime = new HashMap<>();
    private Map<Player, Integer> blockPlaceCount = new HashMap<>(); // 放置次数
    private Map<Player, Long> lastBlockPlaceTime = new HashMap<>(); // 最后一次放置时间
    private Map<Player, Integer> noBlockUnderFeetCount = new HashMap<>(); // 脚下无方块次数
    private Map<Player, Float> lastYawRecord = new HashMap<>(); // Yaw 视角记录
    private Map<Player, Long> inAirStartTime = new HashMap<>(); // 开始悬空时间
    private Map<Player, Location> airStartLocation = new HashMap<>(); // 悬空起点位置
    private Map<Player, Boolean> hasTouchedGround = new HashMap<>(); // 是否已落地
    private Map<Player, Boolean> shouldSlowDown = new HashMap<>(); // 标记玩家是否应该被减速
    private Map<Player, Boolean> usePoi = new HashMap<>(); // 标记玩家是否应该被减速

    private Map<Player, Long> flyingStartTime = new HashMap<>();
    private Map<Player, Location> flyingStartLocation = new HashMap<>();
    private static Map<Player,Boolean> isNeedExecute = new HashMap<>();

    public static Map<Player, Boolean> getIsNeedExecute() {
        return isNeedExecute;
    }

    public static void setIsNeedExecute(Map<Player, Boolean> isNeedExecute) {
        isNeedExecute = isNeedExecute;
    }

    public ReisaClickerListener(BedWarsPro plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location currentLocation = player.getLocation();
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }
        if (white.containsKey(player.getName())) {
            return;
        }
        if (isNeedExecute.containsKey(player)) {
            return;
        }

        if (lastLocation.containsKey(player)) {
            Location previousLocation = lastLocation.get(player);
            double deltaX = currentLocation.getX() - previousLocation.getX();
            double deltaZ = currentLocation.getZ() - previousLocation.getZ();
            double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
            // 只要坐标发生变化就认为在移动

            long timeElapsed = (System.currentTimeMillis() - lastMoveTime.get(player)) / 50;

            if (timeElapsed > 0) {
                double speed = distanceXZ / (timeElapsed * 0.05);
                //player.sendMessage("当前水平速度：" + String.format("%.2f m/s", speed));

                // 检查是否处于应减速状态
                boolean isSlowed = false;
                boolean hasPotionEffect = player.hasPotionEffect(PotionEffectType.SLOW); // 减速药水
                boolean isPotionActive = usePoi.getOrDefault(player, false);
                boolean inWeb = player.getLocation().getBlock().getType() == Material.WEB; // 蜘蛛网
                boolean underSoulSand = player.getLocation().clone().subtract(0, 1, 0)
                        .getBlock().getType() == Material.SOUL_SAND; // 脚下是灵魂沙

                if (hasPotionEffect || inWeb || underSoulSand || isPotionActive) {
                    isSlowed = true;
                    shouldSlowDown.put(player, true);
                } else {
                    shouldSlowDown.remove(player);
                }

                // 如果应减速，检查是否真的变慢了
                if (isSlowed && speed > 4.0) { // 正常应减速后速度不应超过 4m/s
                    player.kickPlayer("§c你处于减速状态但仍然高速移动，疑似使用加速外挂！");
                    plugin.getLogger().info(player.getName() + " 因无视减速效果被踢出");
                    return;
                }
                if (speed>4) {
                    isMoving.put(player, true);
                } else {
                    isMoving.put(player, false);
                }
                // 高速移动检测
                // === 高速移动检测开始 ===
                if (speed > 8.0) { // 更合理的阈值
                    if (!speedViolationStartTime.containsKey(player)) {
                        speedViolationStartTime.put(player, System.currentTimeMillis());
                        plugin.getLogger().info("[SpeedHack] " + player.getName() + " 开始高速移动");
                    }

                    long violationDuration = System.currentTimeMillis() - speedViolationStartTime.get(player);

                    //player.sendMessage("DEBUG：当前违例时间：" + violationDuration + " ms");
                    plugin.getLogger().info(player.getName() + " 违例时间：" + violationDuration);

                    if (violationDuration >= 300) {
                        player.kickPlayer("§c你因持续高速移动被踢出服务器（疑似使用加速外挂）");
                        plugin.getLogger().info(player.getName() + " 因高速移动被踢出");
                        speedViolationStartTime.remove(player);
                        return;
                    }
                } else {
                    if (speedViolationStartTime.containsKey(player)) {
                        plugin.getLogger().info("[SpeedHack] " + player.getName() + " 高速行为中断");
                    }
                    speedViolationStartTime.remove(player);
                }
// === 高速移动检测结束 ===

            }
        }

        lastLocation.put(player, currentLocation.clone());
        lastMoveTime.put(player, System.currentTimeMillis());

        // 原有逻辑：脚下是否有方块
        Location location = player.getLocation().clone().subtract(0, 1, 0);
        if (location.getBlock().getType() == Material.AIR) {
            //player.sendMessage("§c你脚下没有方块！");
        }

        // 原有视角变动检测
        float currentPitch = player.getLocation().getPitch();
        float currentYaw = player.getLocation().getYaw();

        if (lastPitch.containsKey(player) && lastYaw.containsKey(player)) {
            float pitchDiff = Math.abs(currentPitch - lastPitch.get(player));
            float yawDiff = Math.abs(currentYaw - lastYaw.get(player));

            if (pitchDiff + yawDiff > 30) {
                //player.sendMessage("§c检测到视角剧烈变化，请注意行为！");
            }
        }



        lastPitch.put(player, currentPitch);
        lastYaw.put(player, currentYaw);

        // === 新增：检测“虚空移动”作弊行为 ===
        Location feetLocation = currentLocation.clone().subtract(0, 1, 0);
        Material blockBelow = feetLocation.getBlock().getType();

        boolean isUnderFeetAir = blockBelow == Material.AIR;

        if (isUnderFeetAir) {
            if (!inAirStartTime.containsKey(player)) {
                inAirStartTime.put(player, System.currentTimeMillis());
                airStartLocation.put(player, currentLocation.clone());
            }

            long airDuration = System.currentTimeMillis() - inAirStartTime.getOrDefault(player, 0L);

            if (airDuration >= 1000 && !hasTouchedGround.getOrDefault(player, false)) {

                double deltaX = currentLocation.getX() - airStartLocation.get(player).getX();
                double deltaZ = currentLocation.getZ() - airStartLocation.get(player).getZ();
                double moveDistanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
                if (moveDistanceXZ > 10) {
                    player.kickPlayer("§c你在无支撑的情况下移动了超过 10 米，疑似使用外挂！");
                    plugin.getLogger().info(player.getName() + " 因虚空移动被踢出");

                    inAirStartTime.remove(player);
                    airStartLocation.remove(player);
                    hasTouchedGround.remove(player);
                    return;
                }
            }

        } else {
            inAirStartTime.remove(player);
            airStartLocation.remove(player);
            hasTouchedGround.put(player, true);
        }
        boolean isOnGround = blockBelow != Material.AIR;

        if (!isOnGround && !player.getGameMode().equals(GameMode.CREATIVE)) {
            if (!flyingStartTime.containsKey(player)) {
                flyingStartTime.put(player, System.currentTimeMillis());
                flyingStartLocation.put(player, currentLocation.clone());
            }

            long flyingDuration = System.currentTimeMillis() - flyingStartTime.get(player);

            // 如果飞行超过 5 秒，并且未明显下落，则判断为飞行作弊
            if (flyingDuration >= 5000) {
                double deltaY = currentLocation.getY() - flyingStartLocation.get(player).getY();

                // 如果 Y 值变化不大（上下浮动 < 0.5），说明是平飞
                if (Math.abs(deltaY) < 0.5) {
                    //player.kickPlayer("§c你因非法飞行被踢出服务器");
                    //plugin.getLogger().info(player.getName() + " 因飞行作弊被踢出");

                    flyingStartTime.remove(player);
                    flyingStartLocation.remove(player);
                    return;
                }
            }
        } else {
            // 玩家落地或进入创造模式，清除记录
            flyingStartTime.remove(player);
            flyingStartLocation.remove(player);
        }
// === 飞行检测结束 ===

    }
    @EventHandler
    public void onPlayerUsePotion(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInHand();

        // 判断是否是右键点击，并且手持的是药水
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (item.getType() == Material.POTION) {
                // 可以在这里记录时间、触发冷却等逻辑
                shouldSlowDown.put(player, true);
                usePoi.put(player, true);
            }
        }else{
            if (item.getType() == Material.POTION) {
                // 可以在这里记录时间、触发冷却等逻辑
                usePoi.put(player, false);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // 只处理右键放置方块的动作
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // 确保是玩家主动放置方块
        ItemStack itemInHand = player.getInventory().getItemInHand();
        Material material = itemInHand.getType();

        // 排除空气和非方块物品（如工具、武器）
        if (material == Material.AIR || !material.isBlock()) {
            return;
        }

        // 获取当前时间戳
        long currentTime = System.currentTimeMillis();

        // 初始化或更新计数器
        blockPlaceCount.putIfAbsent(player, 0);
        blockPlaceCount.put(player, blockPlaceCount.get(player) + 1);

        // 记录最后一次操作时间
        lastBlockPlaceTime.put(player, currentTime);

        // 记录视角
        lastYawRecord.putIfAbsent(player, player.getLocation().getYaw());

        float currentYaw = player.getLocation().getYaw();
        float yawDiff = Math.abs(currentYaw - lastYawRecord.get(player));
        boolean isViewStatic = yawDiff < 5.0f; // 判断视角是否基本不动

        if (isViewStatic) {
            lastYawRecord.put(player, currentYaw); // 更新视角
        }

        // 检查脚下是否有方块
        Location location = player.getLocation().clone().subtract(0, 1, 0);
        boolean isUnderFeetAir = location.getBlock().getType() == Material.AIR;

        if (isUnderFeetAir) {
            noBlockUnderFeetCount.putIfAbsent(player, 0);
            noBlockUnderFeetCount.put(player, noBlockUnderFeetCount.get(player) + 1);
        }

        // 检查是否满足踢人条件
        checkAndKickIfNecessary(player, currentTime);
    }
    private void checkAndKickIfNecessary(Player player, long currentTime) {

    }

    private boolean isAirLocation(Location location) {
        return location.clone().subtract(0, 1, 0).getBlock().getType() == Material.AIR;
    }


    private Map<Player, Boolean> isMoving = new HashMap<>(); // 记录玩家是否正在移动
    @EventHandler
    public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        // 只处理背包中的非快捷栏（即槽位 0~26）
        if (event.getSlot() >= 0 && event.getSlot() <= 26) {
            if (isMoving.getOrDefault(player, false)) {
                player.kickPlayer("§c你在移动时修改了背包内容，被判定为作弊！");
                plugin.getLogger().info(player.getName() + " 因移动时修改背包被踢出");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
            return;
        }

        Player attacker = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();

        boolean isCritical = attacker.isSprinting() && attacker.isOnGround();

        //判断是否在地上
        if (!isAirLocation(attacker.getLocation().subtract(0, 1, 0))) {
            //判断event是否暴击
            if (isCritical) {
                //event.setCancelled(true);
                //attacker.kickPlayer("§c你因暴击被踢出服务器");
                //plugin.getLogger().info(attacker.getName() + " 因暴击被踢出");
            }
        }

        // 排除弓箭等远程攻击方式
        if (event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
            return;
        }

        // 获取两者之间距离
        double distance = attacker.getLocation().distance(victim.getLocation());
        if (distance >= 4.5) {
            event.setCancelled(true);
            attacker.kickPlayer("§c你因远程攻击被踢出服务器");
            plugin.getLogger().info(attacker.getName() + " 因远程攻击 (" + String.format("%.2f", distance) + "m) 被踢出");
            return;
        }

        // 判断是否在视野范围内（设置最大视野角度为 90 度）
        boolean isInFOV = isTargetInFOV(attacker, victim, 90.0);

        if (!isInFOV) {
            event.setCancelled(true); // 取消伤害
            attacker.sendMessage("§c你不在视野内攻击了他人，疑似使用外挂！");
            plugin.getLogger().info(attacker.getName() + " 穿视口攻击");
            attacker.kickPlayer("§c你攻击了一个不在你视野中的玩家，已被系统检测到！");
        }
    }
    private boolean isTargetInFOV(Player attacker, Player victim, double maxFovAngle) {
        Vector playerDirection = attacker.getLocation().getDirection().normalize(); // 视线方向
        Vector targetVector = victim.getLocation().toVector().subtract(attacker.getLocation().toVector()).normalize(); // 目标方向

        double angle = playerDirection.clone().angle(targetVector); // 返回弧度
        return Math.toDegrees(angle) <= maxFovAngle / 2.0; // 半角即可命中
    }


    private Map<Player, Float> lastPitch = new HashMap<>();
    private Map<Player, Float> lastYaw = new HashMap<>();
}
