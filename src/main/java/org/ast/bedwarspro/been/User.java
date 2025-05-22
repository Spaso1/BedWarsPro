package org.ast.bedwarspro.been;

import java.util.*;

public class User {
    private String name;
    private int kills;
    private long addr;
    private int deaths;
    private int plays;
    private long coins;
    private String rank;
    private List<String> title;
    private String use_title;
    private Set<String> claimedRewards;
    private int healthInSurivialMax;
    public User(String name) {
        this.name = name;
        this.kills = 0;
        this.addr = 0L;
        this.deaths = 0;
        this.plays = 0;
        this.coins = 0L;
        this.rank = "Reiser";
        this.use_title = "Reiser";
        ArrayList<String> title = new ArrayList<>();
        title.add("Reiser");
        this.title = title;
        this.healthInSurivialMax = 20;
        this.claimedRewards = new HashSet<>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getUse_title() {
        return use_title;
    }

    public void setUse_title(String use_title) {
        this.use_title = use_title;
    }

    public int getHealthInSurivialMax() {
        return healthInSurivialMax;
    }

    public void setHealthInSurivialMax(int healthInSurivialMax) {
        this.healthInSurivialMax = healthInSurivialMax;
    }

    public List<String> getTitle() {
        return title;
    }

    public void setTitle(List<String> title) {
        this.title = title;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public long getAddr() {
        return addr;
    }

    public void setAddr(long addr) {
        this.addr = addr;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getPlays() {
        return plays;
    }

    public void setPlays(int plays) {
        this.plays = plays;
    }

    public long getCoins() {
        return coins;
    }

    public void setCoins(long coins) {
        this.coins = coins;
    }

    public Set<String> getClaimedRewards() {
        return claimedRewards;
    }

    public void addClaimedReward(String rewardKey) {
        claimedRewards.add(rewardKey);
    }
    public void removeClaimedReward(String rewardKey) {
        claimedRewards.remove(rewardKey);
    }
    public void setClaimedRewards(Set<String> rewards) {
        this.claimedRewards = rewards;
    }
}
