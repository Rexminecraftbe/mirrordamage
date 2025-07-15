package de.elia.cameraplugin.mirrordamage;

import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerCareerChangeEvent;

/**
 * Prevents mirror villagers from acquiring professions.
 */
public class VillagerJobBlockListener implements Listener {

    private final VillagerMirrorManager manager;

    public VillagerJobBlockListener(VillagerMirrorManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onCareerChange(VillagerCareerChangeEvent event) {
        Villager villager = event.getEntity();
        if (manager.getPlayer(villager) != null) {
            event.setCancelled(true);
        }
    }
}
