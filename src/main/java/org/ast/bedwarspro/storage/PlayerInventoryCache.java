package org.ast.bedwarspro.storage;

import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerInventoryCache {
    private static final Map<UUID, Map<String, ItemStack[]>> cache = new HashMap<>();

    public static void save(UUID uuid, String group, ItemStack[] contents) {
        cache.computeIfAbsent(uuid, k -> new HashMap<>()).put(group, contents);
    }

    public static ItemStack[] get(UUID uuid, String group) {
        Map<String, ItemStack[]> groupInventories = cache.get(uuid);
        return groupInventories != null ? groupInventories.get(group) : null;
    }

    public static void clear(UUID uuid) {
        cache.remove(uuid);
    }
}
