package de.elia.cameraplugin.mirrordamage;

import org.bukkit.damage.DamageSource;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.enchantments.Enchantment;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import java.util.EnumSet;

/**
 * Transfers damage from mirror villagers to their players
 * and prevents players from hurting themselves.
 */
public class DamageTransferListener implements Listener {

    private final VillagerMirrorManager mirrorManager;
    private final boolean damageArmor;
    private final EnumSet<EntityDamageEvent.DamageCause> durabilityCauses = EnumSet.of(
            EntityDamageEvent.DamageCause.ENTITY_ATTACK,
            EntityDamageEvent.DamageCause.PROJECTILE,
            EntityDamageEvent.DamageCause.ENTITY_EXPLOSION,
            EntityDamageEvent.DamageCause.BLOCK_EXPLOSION,
            EntityDamageEvent.DamageCause.FIRE,
            EntityDamageEvent.DamageCause.FIRE_TICK,
            EntityDamageEvent.DamageCause.HOT_FLOOR,
            EntityDamageEvent.DamageCause.FALL
    );

    public DamageTransferListener(VillagerMirrorManager mirrorManager, boolean damageArmor) {
        this.mirrorManager = mirrorManager;
        this.damageArmor = damageArmor;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity target = event.getEntity();

        // Mirror villager hit? Transfer damage to owning player.
        if (target instanceof Villager mirror) {
            Player owner = mirrorManager.getPlayer(mirror);
            if (owner != null) {
                event.setCancelled(true); // keep villager intact

                // Transfer potion effects from tipped arrows
                Entity damager = event.getDamager();
                if (damager instanceof Arrow arrow) {
                    var baseType = arrow.getBasePotionType();
                    if (baseType != null) {
                        baseType.getPotionEffects()
                                .forEach(effect -> owner.addPotionEffect(effect, true));
                    }
                    var customEffects = arrow.getCustomEffects();
                    if (customEffects != null) {
                        customEffects.forEach(effect -> owner.addPotionEffect(effect, true));
                    }
                }

                double damage = event.getFinalDamage();
                owner.damage(damage, damager);
                damagePlayerArmor(owner, event.getCause());
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
     * Transfer any damage the mirror villager receives to its player.
     */
    @EventHandler(ignoreCancelled = true)
    public void onMirrorDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Villager mirror)) return;

        // Entity-caused damage is handled in onEntityDamageByEntity to avoid
        // double armour durability loss.
        if (event instanceof EntityDamageByEntityEvent) return;

        // prevent drowning when wearing a turtle helmet
        if (event.getCause() == EntityDamageEvent.DamageCause.DROWNING) {
            ItemStack helmet = mirror.getEquipment().getHelmet();
            if (helmet != null && helmet.getType() == Material.TURTLE_HELMET) {
                event.setCancelled(true);
                return;
            }
        }



        Player owner = mirrorManager.getPlayer(mirror);
        if (owner == null) return;


        // Entity-caused damage is handled in onEntityDamageByEntity to avoid
        // double armour durability loss.
        if (event instanceof EntityDamageByEntityEvent) return;

        event.setCancelled(true);
        DamageSource source = event.getDamageSource();
        owner.damage(event.getFinalDamage(), source);
        damagePlayerArmor(owner, event.getCause());
    }

    /**
     * Transfer potion effects from splash potions to the player.
     */
    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        for (LivingEntity entity : event.getAffectedEntities()) {
            if (!(entity instanceof Villager mirror)) continue;
            Player owner = mirrorManager.getPlayer(mirror);
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
        if (!(hit instanceof Villager mirror)) return;

        Player owner = mirrorManager.getPlayer(mirror);
        if (owner == null) return;

        Projectile projectile = event.getEntity();
        if (projectile instanceof Arrow arrow) {
            var baseType = arrow.getBasePotionType();
            if (baseType != null) {
                baseType.getPotionEffects()
                        .forEach(effect -> owner.addPotionEffect(effect, true));
            }
            var customEffects = arrow.getCustomEffects();
            if (customEffects != null) {
                customEffects.forEach(effect -> owner.addPotionEffect(effect, true));
            }

            // Also mirror arrow damage to the player and damage armour
            double damage = arrow.getDamage();
            ProjectileSource shooter = arrow.getShooter();
            Entity damager = shooter instanceof Entity ent ? ent : arrow;
            owner.damage(damage, damager);
            damagePlayerArmor(owner, EntityDamageEvent.DamageCause.PROJECTILE);
        }
    }

    private void damagePlayerArmor(Player player, EntityDamageEvent.DamageCause cause) {
        if (!damageArmor || !durabilityCauses.contains(cause)) return;
        // damage currently equipped armour (when players regain it later)
        ItemStack[] playerArmor = player.getInventory().getArmorContents();
        if (damageItems(playerArmor)) {
            player.getInventory().setArmorContents(playerArmor);
        }

        // also damage armour stored on a mirror villager
        Villager mirror = mirrorManager.getMirror(player);
        if (mirror != null) {
            ItemStack[] mirrorArmor = mirror.getEquipment().getArmorContents();
            if (damageItems(mirrorArmor)) {
                mirror.getEquipment().setArmorContents(mirrorArmor);
            }

            // keep stored inventory in sync if it was cloned
            ItemStack[] stored = mirrorManager.getStoredInventory(player);
            if (stored != null && stored.length >= 40) {
                for (int i = 0; i < mirrorArmor.length && (36 + i) < stored.length; i++) {
                    stored[36 + i] = mirrorArmor[i];
                }
            }
        }
    }

    /**
     * Damages the provided armour contents by one durability point each.
     *
     * @return {@code true} if any item was modified
     */
    private boolean damageItems(ItemStack[] armor) {
        boolean changed = false;
        for (int i = 0; i < armor.length; i++) {
            ItemStack item = armor[i];
            if (item == null) continue;
            if (item.getType() == Material.ELYTRA) continue; // Elytra worn by the villager should not lose durability
            var meta = item.getItemMeta();
            if (meta instanceof Damageable dmg) {
                boolean applyDamage = true;
                int unbreaking = item.getEnchantmentLevel(Enchantment.UNBREAKING);
                if (unbreaking > 0) {
                    double chance = (60.0 + 40.0 / (unbreaking + 1)) / 100.0; // probability to take durability damage
                    applyDamage = ThreadLocalRandom.current().nextDouble() < chance;
                }
                if (applyDamage) {
                    dmg.setDamage(dmg.getDamage() + 1);
                    item.setItemMeta(meta);
                    armor[i] = item;
                    changed = true;
                }
            }
        }
        return changed;
    }
}