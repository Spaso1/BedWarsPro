package org.ast.bedwarspro.mannger;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class GroupWorldManager {
    private final Map<String, String> worldToGroupMap = new HashMap<>();

    public GroupWorldManager(JavaPlugin plugin) {
        loadGroups(plugin.getConfig());
    }

    private final List<WorldGroupPattern> worldGroupPatterns = new ArrayList<>();

    public static class WorldGroupPattern {
        private final String pattern;
        private final String group;

        public WorldGroupPattern(String pattern, String group) {
            this.pattern = pattern;
            this.group = group;
        }

        public boolean matches(String worldName) {
            if (pattern.endsWith("*")) {
                return worldName.startsWith(pattern.substring(0, pattern.length() - 1));
            }
            return pattern.equals(worldName);
        }
    }

    public void loadGroups(Configuration config) {
        ConfigurationSection groupsSection = config.getConfigurationSection("groups");
        if (groupsSection == null) return;

        for (String groupName : groupsSection.getKeys(false)) {
            List<String> worlds = config.getStringList("groups." + groupName + ".worlds");
            for (String pattern : worlds) {
                worldGroupPatterns.add(new WorldGroupPattern(pattern, groupName));
            }
        }
    }

    public String getGroupByWorld(String worldName) {
        return worldGroupPatterns.stream()
                .filter(p -> p.matches(worldName))
                .map(p -> p.group)
                .findFirst()
                .orElse(null);
    }


// 修改 GroupWorldManager.java
}
