package de.elia.cameraplugin.mirrordamage;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageTransferListener implements Listener {

    private final ArmorStandMirrorManager mirrorManager;

    public DamageTransferListener(ArmorStandMirrorManager mirrorManager) {
        this.mirrorManager = mirrorManager;
    }

    @EventHandler
    public void onArmorStandDamage(EntityDamageByEntityEvent event) {
        // Prüfen, ob überhaupt ein Armor Stand getroffen wurde
        if (!(event.getEntity() instanceof ArmorStand stand)) {
            return;
        }

        // Prüfen, ob es unser Armor Stand ist
        Player owner = mirrorManager.getPlayer(stand);
        if (owner == null) {
            return;
        }

        // --- DEBUG-NACHRICHTEN ---
        System.out.println("[MirrorDamage] ArmorStand von " + owner.getName() + " wurde getroffen.");

        boolean isDamageActive = mirrorManager.isDamageActive(owner.getUniqueId());
        System.out.println("[MirrorDamage] Prüfe Schadens-Status für " + owner.getName() + ": " + isDamageActive);

        // Die eigentliche Prüfung
        if (!isDamageActive) {
            System.out.println("[MirrorDamage] Schaden ist NICHT aktiv. Breche ab.");
            return;
        }

        System.out.println("[MirrorDamage] Schaden ist AKTIV. Übertrage " + event.getDamage() + " Schaden.");

        // Schaden am ArmorStand verhindern
        event.setCancelled(true);

        // Schaden auf den Spieler übertragen
        if (owner.isOnline() && !owner.isDead()) {
            owner.damage(event.getDamage(), event.getDamager());
        }
    }
}