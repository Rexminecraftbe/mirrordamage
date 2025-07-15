package de.elia.cameraplugin.mirrordamage;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import de.elia.cameraplugin.mirrordamage.VillagerMirrorManager;

public class MirrorCleanupListener implements Listener {

    private final VillagerMirrorManager manager;

    public MirrorCleanupListener(VillagerMirrorManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        manager.removeMirror(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        manager.dropInventory(player);
        manager.removeMirror(player, false);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        manager.restoreInventory(event.getPlayer());
    }
}