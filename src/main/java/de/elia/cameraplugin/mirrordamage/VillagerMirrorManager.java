package de.elia.cameraplugin.mirrordamage;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Spawns an invisible villager that mirrors the player's armour and
 * transfers any damage it receives to the player.
 */
public class VillagerMirrorManager {

    private final Plugin plugin;
    private final Map<UUID, UUID> villagerToPlayer = new HashMap<>();

    public VillagerMirrorManager(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Spawns (or replaces) an invisible, no-AI villager that mirrors the
     * player's armour.
     */
    public void spawnMirror(Player player) {
        removeMirror(player);  // ensure only one exists

        Location loc = player.getLocation();
        Villager villager = (Villager) player.getWorld().spawnEntity(loc, EntityType.VILLAGER);
        villager.setInvisible(true);
        villager.setAI(false);
        villager.setSilent(true);
        // The damage transfer listener cancels any incoming damage,
        // so the villager doesn't need to be invulnerable. Keeping it
        // vulnerable allows events like falling anvils to trigger and
        // be redirected to the player.
        villager.setInvulnerable(false);

        villager.getEquipment().setArmorContents(player.getInventory().getArmorContents());

        villagerToPlayer.put(villager.getUniqueId(), player.getUniqueId());
    }

    /** Returns the player associated with the given villager or {@code null}. */
    public Player getPlayer(Entity villager) {
        UUID uuid = villagerToPlayer.get(villager.getUniqueId());
        return uuid == null ? null : Bukkit.getPlayer(uuid);
    }

    /** Prüft, ob bereits ein Mirror‑Villager existiert. */
    public boolean hasMirror(Player player) {
        return villagerToPlayer.containsValue(player.getUniqueId());
    }

    /** Entfernt den Mirror‑Villager eines Spielers. @return true wenn einer entfernt wurde */
    public boolean removeMirror(Player player) {
        boolean[] removed = {false};
        villagerToPlayer.entrySet().removeIf(entry -> {
            if (entry.getValue().equals(player.getUniqueId())) {
                Entity e = Bukkit.getEntity(entry.getKey());
                if (e != null) e.remove();
                removed[0] = true;
                return true;
            }
            return false;
        });
        return removed[0];
    }

    /** Für onDisable */
    public void cleanup() {
        villagerToPlayer.keySet().forEach(uuid -> {
            Entity e = Bukkit.getEntity(uuid);
            if (e != null) e.remove();
        });
        villagerToPlayer.clear();
    }
}