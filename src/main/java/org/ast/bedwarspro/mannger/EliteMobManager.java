package org.ast.bedwarspro.mannger;

import org.ast.bedwarspro.BedWarsPro;
import org.ast.bedwarspro.been.EliteMob;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class EliteMobManager {
    private final BedWarsPro plugin;
    private final Map<String, EliteMob> eliteMobs = new HashMap<>();

    public EliteMobManager(BedWarsPro plugin) {
        this.plugin = plugin;
        loadMobs();
    }

    public BedWarsPro getPlugin() {
        return plugin;
    }

    private void loadMobs() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("mobs");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            String name = section.getString(key + ".name");
            String typeName = section.getString(key + ".type");
            double health = section.getDouble(key + ".health");
            double attack = section.getDouble(key + ".attack_damage");
            double defense = section.getDouble(key + ".defense");

            try {
                EntityType type = EntityType.valueOf(typeName.toUpperCase());
                eliteMobs.put(key, new EliteMob(name, type, health, attack, defense));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("未知实体类型: " + typeName);
            }
        }
    }

    public Collection<EliteMob> getAllMobs() {
        return eliteMobs.values();
    }

    public EliteMob getMob(String key) {
        return eliteMobs.get(key);
    }
}
