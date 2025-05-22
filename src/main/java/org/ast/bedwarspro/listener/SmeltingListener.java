package org.ast.bedwarspro.listener;

import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SmeltingListener implements Listener {
    private final Random random = new Random();
    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        ItemStack item = event.getItem();

        // Check if the item is a pickaxe,book
        if (item.getType().toString().endsWith("_PICKAXE") || item.getType().toString().endsWith("BOOK")) {
            // Random chance to apply the custom enchantment
            if (random.nextInt(100) < 40) { // 20% chance
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    List<String> lore = meta.getLore();
                    if (lore == null) {
                        lore = new ArrayList<>();
                    }
                    lore.add("§6熔炼 I"); // Add custom enchantment as lore
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                event.getEnchanter().sendMessage("§a获得了熔炼附魔!");
            }
        }
    }
    @EventHandler
    public void onAnvilClick(InventoryClickEvent event) {
        if (event.getInventory().getType() == InventoryType.ANVIL && event.getSlotType() == InventoryType.SlotType.RESULT) {
            AnvilInventory anvilInventory = (AnvilInventory) event.getInventory();
            ItemStack firstItem = anvilInventory.getItem(0); // First slot
            ItemStack secondItem = anvilInventory.getItem(1); // Second slot

            if (firstItem != null && secondItem != null && ((firstItem.getType().toString().endsWith("_PICKAXE") ||  firstItem.getType().toString().endsWith("BOOK")) || (secondItem.getType().toString().endsWith("_PICKAXE") || secondItem.getType().toString().endsWith("BOOK")))) {
                ItemStack result = event.getCurrentItem(); // Result item
                if (result != null) {
                    ItemMeta meta = result.getItemMeta();
                    if (meta != null) {
                        List<String> lore = meta.getLore();
                        if (lore == null) {
                            lore = new ArrayList<>();
                        }

                        // 合并第一个物品的 lore
                        if (firstItem.hasItemMeta() && firstItem.getItemMeta().getLore() != null) {
                            for (String line : firstItem.getItemMeta().getLore()) {
                                if (!lore.contains(line)) { // 避免重复
                                    lore.add(line);
                                }
                            }
                        }

                        // 合并第二个物品的 lore
                        if (secondItem.hasItemMeta() && secondItem.getItemMeta().getLore() != null) {
                            for (String line : secondItem.getItemMeta().getLore()) {
                                if (!lore.contains(line)) { // 避免重复
                                    lore.add(line);
                                }
                            }
                        }
                        // Apply color codes to the item's name
                        if (meta.hasDisplayName()) {
                            String displayName = meta.getDisplayName();
                            displayName = displayName.replace("&", "§"); // Replace & with § for color codes
                            meta.setDisplayName(displayName);
                        }
                        meta.setLore(lore);
                        result.setItemMeta(meta);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInHand();

        if (tool != null && tool.hasItemMeta() && tool.getItemMeta().getLore() != null) {
            List<String> lore = tool.getItemMeta().getLore();
            if (lore.contains("§6熔炼 I")) { // Check for custom enchantment in lore
                Material blockType = event.getBlock().getType();
                int fortuneLevel = tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
                int multiplier = fortuneLevel > 0 ? (1 + random.nextInt(fortuneLevel + 1)) : 1; // Fortune multiplier

                switch (blockType) {
                    case IRON_ORE:
                        event.setCancelled(true);
                        event.getBlock().setType(Material.AIR);
                        event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.IRON_INGOT, multiplier));
                        break;
                    case GOLD_ORE:
                        event.setCancelled(true);
                        event.getBlock().setType(Material.AIR);
                        event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.GOLD_INGOT, multiplier));
                        break;
                    default:
                        return;
                }

                // Play particle effects
                event.getBlock().getWorld().playEffect(event.getBlock().getLocation().add(0.5, 0.5, 0.5), org.bukkit.Effect.FLAME, 0);
                event.getBlock().getWorld().playEffect(event.getBlock().getLocation().add(0.5, 0.5, 0.5), org.bukkit.Effect.SMOKE, 0);
            }
        }
    }
}