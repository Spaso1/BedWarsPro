package org.ast.bedwarspro;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class PlayerDamageListener implements Listener {
    private final BedWars plugin;
    private final Map<UUID, Long> lastSneakPress = new HashMap<>();
    private final Map<UUID, Long> ninjaCooldown = new HashMap<>();
    public static final Map<UUID, DamageBuff> swordSaintDamageBuff = new HashMap<>();
    private static final Random random = new Random();

    public PlayerDamageListener(BedWars plugin) {
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
                plugin.getUserPro().remove(player.getName());
                player.sendMessage("你已离开 BedWorld 地图，职业已被清除！");
            }
        } else {
            if (!profession.equals("None")) {
                player.sendMessage("欢迎回来！你当前的职业为：" + profession);
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
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

            if (attackerProfession.equals("战士")) {
                doZhanShi(attacker, victim,event);
            }else if (attackerProfession.equals("剑圣")) {
                doJianSheng(attacker,victim,event);
            }else  if (attackerProfession.equals("箭神") && event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
                doJianShen(attacker,  victim,event);
            }else if (attackerProfession.equals("刺客")){
                doCike(attacker,victim,event);
            }else if (attackerProfession.equals("神射手") && event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
                doShenJianShou(attacker, victim,event);
            }
            else {
                swordSaintDamageBuff.remove(attacker.getUniqueId());
            }
            // Example: Send a message to both players
            attacker.sendMessage("You attacked " + victim.getName() + " who is a " + victimProfession + "!");
            victim.sendMessage("You were attacked by " + attacker.getName() + " who is a " + attackerProfession + "!");
        }
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        World world = victim.getWorld();
        if (!world.getName().startsWith("bedworld")) {
            return;
        }

        if (victim.getKiller() instanceof Player) {
            Player killer = victim.getKiller();
            String profession = plugin.getUserPro().getOrDefault(killer.getName(), "None");

            if (profession.equals("剑圣")) {
                // 设置初始 buff：1.1 倍，持续 10 秒
                swordSaintDamageBuff.put(killer.getUniqueId(), new DamageBuff(1.1, 10000));
                killer.sendMessage("§c你击杀了玩家，获得【剑圣】伤害加成！");

                // 显示带颜色的“斩杀!”标题
                killer.sendTitle("§c§l斩杀!","x" + swordSaintDamageBuff.get(killer.getUniqueId()).multiplier);
            }
        }
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
                    if (attackerProfession.equals("忍者")) {
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
    private void doShenJianShou(Player attacker, Player victim,EntityDamageByEntityEvent event) {
        attacker.sendMessage("你用【神射手】射中了" + victim.getName() + "，伤害x1.3!");
        event.setDamage(event.getDamage() * 1.3);
    }

    private void doJianSheng(Player attacker, Player victim,EntityDamageByEntityEvent event) {
        UUID attackerUuid = attacker.getUniqueId();
        if (swordSaintDamageBuff.containsKey(attackerUuid)) {
            DamageBuff buff = swordSaintDamageBuff.get(attackerUuid);
            if (!buff.isExpired()) {
                // 刷新 buff 时间
                buff.expireTime = System.currentTimeMillis() + 10000;

                // 叠加伤害倍率
                buff.multiplier *= 1.1;

                // 应用到本次伤害
                event.setDamage(event.getDamage() * buff.multiplier);
                attacker.sendMessage("剑圣连击强化！当前伤害倍率: x" + String.format("%.2f", buff.multiplier));
            }
        } else {
            // 初始化 buff
            swordSaintDamageBuff.put(attackerUuid, new DamageBuff(1.1, 10000));
            event.setDamage(event.getDamage() * 1.1);
            attacker.sendMessage("剑圣首次击杀，伤害提升至 x1.1");
        }
    }
    private void doCike(Player attacker, Player victim,EntityDamageByEntityEvent event) {
        //判断是否是背后攻击
        if (isBackAttack(attacker, victim)) {
            attacker.sendMessage("你背对攻击了，触发【刺客】技能！");
            event.setDamage(event.getDamage() * 1.5);
        }
    }
    private void doJianShen(Player attacker, Player victim,EntityDamageByEntityEvent event) {
        if (random.nextDouble() > 0.2) {
            return;
        }

        // 获取当前的力量等级
        int currentLevel = 1;
        boolean found = false;

        for (PotionEffect effect : attacker.getActivePotionEffects()) {
            if (effect.getType() == PotionEffectType.INCREASE_DAMAGE) {
                currentLevel = effect.getAmplifier() + 1; // Amplifier 是从 0 开始
                found = true;
                break;
            }
        }

        // 最大到 V 级
        if (currentLevel >= 5) {
            return;
        }

        PotionEffect powerEffect = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, currentLevel);
        attacker.addPotionEffect(powerEffect);
        attacker.sendMessage("你获得了力量 " + romanNumeral(currentLevel) + " 效果！");
    }
    private void doRenZe(Player player) {
        Vector direction = player.getLocation().getDirection().normalize(); // 获取视角方向单位向量
        player.setVelocity(direction.multiply(10)); // 初始速度设为 10 倍方向向量

        player.sendMessage("你使用忍者技能，技能效果已触发!");
    }
    private void doZhanShi(Player attacker, Player victim,EntityDamageByEntityEvent event) {
        double damage = event.getDamage(); // 获取当前伤害值
        event.setDamage(damage*1.15);
        attacker.sendMessage("你使用战士技能，伤害增加15%!伤害:" + damage*1.15);
    }

    // 辅助方法：罗马数字转换
    private String romanNumeral(int number) {
        switch (number) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
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


    private boolean hasCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        if (ninjaCooldown.containsKey(uuid)) {
            long remaining = ((ninjaCooldown.get(uuid) + 30000) - System.currentTimeMillis()) / 1000;
            if (remaining > 0) {
                player.sendMessage("忍者技能冷却中，剩余 " + remaining + " 秒");
                return true;
            }
        }
        ninjaCooldown.put(uuid, System.currentTimeMillis());
        return false;
    }

}