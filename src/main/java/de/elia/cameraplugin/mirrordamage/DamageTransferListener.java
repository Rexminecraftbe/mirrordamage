package de.elia.cameraplugin.mirrordamage;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

/**
 * Transfers damage from mirror armor stands to their players
 * and prevents players from hurting themselves.
 */
public class DamageTransferListener implements Listener {

    private final ArmorStandMirrorManager mirrorManager;

    public DamageTransferListener(ArmorStandMirrorManager mirrorManager) {
        this.mirrorManager = mirrorManager;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity target = event.getEntity();

        // Mirror armor stand hit? Transfer damage to owning player.
        if (target instanceof ArmorStand stand) {
            Player owner = mirrorManager.getPlayer(stand);
            if (owner != null) {
                event.setCancelled(true); // keep stand intact
                double damage = event.getFinalDamage();
                owner.damage(damage, event.getDamager());
                return;
            }
        }

        // Cancel self-inflicted damage
        if (target instanceof Player victim) {
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
}
