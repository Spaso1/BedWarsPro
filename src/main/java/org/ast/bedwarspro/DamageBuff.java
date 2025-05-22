package org.ast.bedwarspro;

public class DamageBuff {
    double multiplier = 1.0; // 基础无加成
    long expireTime; // 过期时间（ms）

    public DamageBuff(double multiplier, long durationMs) {
        this.multiplier = multiplier;
        this.expireTime = System.currentTimeMillis() + durationMs;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expireTime;
    }
}