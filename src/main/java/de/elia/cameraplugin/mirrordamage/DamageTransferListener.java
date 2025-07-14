package de.elia.cameraplugin.mirrordamage;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

/**
 * Listener that prevents players from damaging themselves.
 */
public class DamageTransferListener implements Listener {

    public DamageTransferListener(ArmorStandMirrorManager mirrorManager) {
        // Currently unused but kept for future functionality
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity target = event.getEntity();
        if (!(target instanceof Player victim)) {
            return;
        }

        Player attacker = null;
        Entity damager = event.getDamager();
        if (damager instanceof Player p) {
            attacker = p;
        } else if (damager instanceof Projectile projectile) {
            ProjectileSource source = projectile.getShooter();
            if (source instanceof Player p) {
                attacker = p;
            }
        }

        if (attacker != null && attacker.getUniqueId().equals(victim.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
