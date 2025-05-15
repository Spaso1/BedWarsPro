package org.ast.bedwarspro;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

public class PlayerDamageListener implements Listener {
    private final BedWarsPro plugin;
    private final Map<UUID, Long> lastSneakPress = new HashMap<>();
    private final Map<UUID, Long> ninjaCooldown = new HashMap<>();
    public static final Map<UUID, DamageBuff> swordSaintDamageBuff = new HashMap<>();
    private static final Random random = new Random();

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

            if (attackerProfession.contains("战士")) {
                doZhanShi(attacker, victim,event);
            }else if (attackerProfession.contains("剑圣")) {
                doJianSheng(attacker,victim,event);
            }else  if (attackerProfession.contains("箭神") && event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
                doJianShen(attacker,  victim,event);
            }else if (attackerProfession.contains("刺客")){
                doCike(attacker,victim,event);
            }else if (attackerProfession.contains("神射手") && event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
                doShenJianShou(attacker, victim,event);
            }
            else {
                swordSaintDamageBuff.remove(attacker.getUniqueId());
            }
        }
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        World world = victim.getWorld();
        if (!world.getName().startsWith("bedworld")) {
            return;
        }

        // 清空玩家背包
        victim.getInventory().clear();

        if (victim.getKiller() instanceof Player) {
            Player killer = victim.getKiller();
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
    private void doShenJianShou(Player attacker, Player victim,EntityDamageByEntityEvent event) {
        attacker.sendMessage("你用【神射手】射中了" + victim.getName() + "，伤害x1.3!");
        event.setDamage(event.getDamage() * 1.3);
    }

    private void doJianSheng(Player attacker, Player victim,EntityDamageByEntityEvent event) {
        UUID attackerUuid = attacker.getUniqueId();
        if (swordSaintDamageBuff.containsKey(attackerUuid)) {
            DamageBuff buff = swordSaintDamageBuff.get(attackerUuid);
            if (!buff.isExpired()) {
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
        Vector direction = player.getLocation().getDirection().normalize(); // 获取视角方向单位向量
        player.setVelocity(direction.multiply(6)); // 初始速度设为 10 倍方向向量

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