package org.ast.bedwarspro.been;

import org.bukkit.entity.EntityType;

public class EliteMob {
    private String name;
    private EntityType entityType;
    private double health;
    private double attackDamage;
    private double defense;
    private boolean customNameVisible;

    public EliteMob(String name, EntityType entityType, double health, double attackDamage, double defense) {
        this.name = name;
        this.entityType = entityType;
        this.health = health;
        this.attackDamage = attackDamage;
        this.defense = defense;
        this.customNameVisible = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public double getAttackDamage() {
        return attackDamage;
    }

    public void setAttackDamage(double attackDamage) {
        this.attackDamage = attackDamage;
    }

    public double getDefense() {
        return defense;
    }

    public void setDefense(double defense) {
        this.defense = defense;
    }

    public boolean isCustomNameVisible() {
        return customNameVisible;
    }

    public void setCustomNameVisible(boolean customNameVisible) {
        this.customNameVisible = customNameVisible;
    }
// Getters and Setters
}
