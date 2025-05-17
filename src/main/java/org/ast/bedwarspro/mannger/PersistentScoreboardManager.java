package org.ast.bedwarspro.mannger;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PersistentScoreboardManager {
    private static final Map<UUID, Scoreboard> persistentBoards = new HashMap<>();

    public static void setPersistentBoard(Player player, Scoreboard scoreboard) {
        persistentBoards.put(player.getUniqueId(), scoreboard);
        player.setScoreboard(scoreboard);
    }

    public static boolean isPersistent(Player player) {
        return persistentBoards.containsKey(player.getUniqueId()) &&
                player.getScoreboard() == persistentBoards.get(player.getUniqueId());
    }

    public static void clear(UUID uuid) {
        persistentBoards.remove(uuid);
    }
}
