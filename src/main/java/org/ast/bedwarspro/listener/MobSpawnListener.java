package org.ast.bedwarspro.listener;

import com.comphenix.protocol.wrappers.EnumWrappers;
import org.ast.bedwarspro.been.EliteMob;
import org.ast.bedwarspro.mannger.EliteMobManager;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MobSpawnListener implements Listener {

    private final EliteMobManager eliteMobManager;
    private final Random random = new Random();

    public MobSpawnListener(EliteMobManager eliteMobManager) {
        this.eliteMobManager = eliteMobManager;
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        World world = event.getLocation().getWorld();
        if (world == null || !world.getName().startsWith("bps")) return;

        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) return;

        if (random.nextDouble() < 0.1) { // 10% 概率替换为精英怪
            List<EliteMob> mobs = new ArrayList<>(eliteMobManager.getAllMobs());

            if (mobs.isEmpty()) return;

            EliteMob eliteMob = mobs.get(random.nextInt(mobs.size()));
            Location loc = event.getLocation();

            LivingEntity entity = (LivingEntity) world.spawnEntity(loc, eliteMob.getEntityType());
            entity.setMaxHealth(eliteMob.getHealth());
            entity.setHealth(eliteMob.getHealth());
            entity.setCustomName(org.bukkit.ChatColor.translateAlternateColorCodes('&', eliteMob.getName()));
            entity.setCustomNameVisible(true);

            // 设置装备
            if (entity instanceof Zombie) {
                ((Zombie) entity).getEquipment().setItemInHand( new ItemStack(Material.DIAMOND_SWORD));
            }

            // 添加抗性效果
            entity.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 2));

            event.setCancelled(true); // 取消原生怪物生成
        }
    }
    @EventHandler
    public void onEliteMobDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        // 检查是否是精英怪（通过名称匹配）
        boolean isEliteMob = eliteMobManager.getAllMobs().stream()
                .anyMatch(mob -> ChatColor.stripColor(entity.getCustomName()).equalsIgnoreCase(ChatColor.stripColor(mob.getName())));

        if (!isEliteMob) return;



        Player killer = entity.getKiller();
        if (killer == null) return;  // 无玩家击杀不给予奖励
        if (killer != null) {
            if (killer.getLocation().getWorld().getEnvironment() == World.Environment.NORMAL) {
                for (int i = 0; i < 10; i++) {
                    killer.getWorld().playEffect( killer.getLocation(), Effect.EXPLOSION_LARGE, 1);
                }
            }
        }

        // 随机生成经验值
        int minExp = 10;
        int maxExp = 50;
        int expReward = random.nextInt(maxExp - minExp + 1) + minExp;

        // 给予经验
        killer.giveExp(expReward);
        killer.sendMessage(ChatColor.GREEN + "你击败了精英怪！获得了 §6" + expReward + "§a 点经验值！");
        minExp = 30;
        maxExp = 100;
        expReward = random.nextInt(maxExp - minExp + 1) + minExp;
        killer.sendMessage(ChatColor.GREEN + "你击败了精英怪！获得了 §6" + expReward + "§a 点金币！");
        eliteMobManager.getPlugin().addCoins(killer,  expReward);
    }
}
