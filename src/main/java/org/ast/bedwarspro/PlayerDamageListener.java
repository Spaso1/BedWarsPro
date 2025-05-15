package org.ast.bedwarspro;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;

public class PlayerDamageListener implements Listener {
    private final BedWars plugin;

    public PlayerDamageListener(BedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Player attacker = (Player) event.getDamager();

            // Retrieve professions
            String victimProfession = plugin.getUserPro().getOrDefault(victim.getName(), "None");
            String attackerProfession = plugin.getUserPro().getOrDefault(attacker.getName(), "None");

            // Example: Send a message to both players
            attacker.sendMessage("You attacked " + victim.getName() + " who is a " + victimProfession + "!");
            victim.sendMessage("You were attacked by " + attacker.getName() + " who is a " + attackerProfession + "!");
        }
    }
}