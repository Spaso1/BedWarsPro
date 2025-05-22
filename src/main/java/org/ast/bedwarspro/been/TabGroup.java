package org.ast.bedwarspro.been;

public class TabGroup {
    private final String prefix;
    private final String worldPattern;

    public TabGroup(String prefix, String worldPattern) {
        this.prefix = prefix;
        this.worldPattern = worldPattern;
    }

    public boolean matches(String worldName) {
        if (worldPattern.equals("*")) return true;
        if (worldPattern.endsWith("*")) {
            return worldName.startsWith(worldPattern.substring(0, worldPattern.length() - 1));
        }
        return worldName.equals(worldPattern);
    }

    public String getPrefix() {
        return prefix;
    }
}
