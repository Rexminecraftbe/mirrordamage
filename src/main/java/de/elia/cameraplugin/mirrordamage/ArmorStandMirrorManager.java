package de.elia.cameraplugin.mirrordamage;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;

import java.util.*;

/**
 * Spawnt pro Spieler einen ArmorStand, kopiert seine Rüstung
 * und merkt sich <Stand‑UUID → Spieler‑UUID>.
 */
public class ArmorStandMirrorManager {

    private final Plugin plugin;
    private final Map<UUID, UUID> standToPlayer = new HashMap<>();

    public ArmorStandMirrorManager(Plugin plugin) {
        this.plugin = plugin;
    }

    /** Spawnt (oder ersetzt) den ArmorStand‑Doppelgänger des Spielers. */
    public void spawnMirror(Player player) {
        removeMirror(player);  // sorgt dafür, dass nur einer existiert

        ArmorStand stand = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
        // sichtbar und mit Hitbox spawnen, damit er nicht sofort entfernt wird
        stand.setInvisible(false);
        stand.setMarker(false);
        stand.setGravity(false);
        stand.setSilent(true);
        stand.setInvulnerable(true); // kann nicht zerstört werden
        // Rüstung kopieren
        stand.getEquipment().setArmorContents(player.getInventory().getArmorContents());

        standToPlayer.put(stand.getUniqueId(), player.getUniqueId());
    }

    /** Liefert den zugeordneten Spieler oder null. */
    public Player getPlayer(Entity armorStand) {
        UUID uuid = standToPlayer.get(armorStand.getUniqueId());
        return uuid == null ? null : Bukkit.getPlayer(uuid);
    }

    /** Prüft, ob bereits ein Mirror‑Stand existiert. */
    public boolean hasMirror(Player player) {
        return standToPlayer.containsValue(player.getUniqueId());
    }

    /** Entfernt Mirror‑Stand eines Spielers. @return true wenn einer entfernt wurde */
    public boolean removeMirror(Player player) {
        boolean[] removed = {false};
        standToPlayer.entrySet().removeIf(entry -> {
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
        standToPlayer.keySet().forEach(uuid -> {
            Entity e = Bukkit.getEntity(uuid);
            if (e != null) e.remove();
        });
        standToPlayer.clear();
    }
}