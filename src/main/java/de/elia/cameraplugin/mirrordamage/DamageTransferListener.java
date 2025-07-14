package de.elia.cameraplugin.mirrordamage;

import org.bukkit.damage.DamageSource;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
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

    /**
     * Transfer any damage the mirror armor stand receives to its player.
     */
    @EventHandler
    public void onArmorStandDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof ArmorStand stand)) return;

        Player owner = mirrorManager.getPlayer(stand);
        if (owner == null) return;

        event.setCancelled(true);
        DamageSource source = event.getDamageSource();
        owner.damage(event.getFinalDamage(), source);
    }

    /**
     * Transfer potion effects from splash potions to the player.
     */
    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        for (LivingEntity entity : event.getAffectedEntities()) {
            if (!(entity instanceof ArmorStand stand)) continue;
            Player owner = mirrorManager.getPlayer(stand);
            if (owner == null) continue;
            double intensity = event.getIntensity(entity);
            for (PotionEffect effect : event.getPotion().getEffects()) {
                int duration = (int) Math.round(effect.getDuration() * intensity);
                PotionEffect applied = new PotionEffect(
                        effect.getType(), duration, effect.getAmplifier(),
                        effect.isAmbient(), effect.hasParticles(), effect.hasIcon());
                owner.addPotionEffect(applied, true);
            }
        }
    }

    /**
     * Transfer potion effects from tipped arrows to the player.
     */
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Entity hit = event.getHitEntity();
        if (!(hit instanceof ArmorStand stand)) return;

        Player owner = mirrorManager.getPlayer(stand);
        if (owner == null) return;

        Projectile projectile = event.getEntity();
        if (projectile instanceof Arrow arrow) {
            arrow.getBasePotionType().getPotionEffects()
                    .forEach(effect -> owner.addPotionEffect(effect, true));
            arrow.getCustomEffects()
                    .forEach(effect -> owner.addPotionEffect(effect, true));
        }
    }
}
