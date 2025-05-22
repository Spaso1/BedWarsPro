package org.ast.bedwarspro.been;

public class PlayerStats {
    private int kills;
    private int wins;
    private int score;
    private int loses;
    private String name;
    private int destroyedBeds;
    private int deaths;

    // Getters and Setters
    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = kills; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getLoses() { return loses; }
    public void setLoses(int loses) { this.loses = loses; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getDestroyedBeds() { return destroyedBeds; }
    public void setDestroyedBeds(int destroyedBeds) { this.destroyedBeds = destroyedBeds; }

    public int getDeaths() { return deaths; }
    public void setDeaths(int deaths) { this.deaths = deaths; }
}
