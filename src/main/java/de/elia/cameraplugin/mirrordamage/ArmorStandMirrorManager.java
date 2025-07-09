package de.elia.cameraplugin.mirrordamage;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class ArmorStandMirrorManager {

    private final Plugin plugin;
    private final Map<UUID, UUID> standToPlayer = new HashMap<>();
    private final Set<UUID> damageActivePlayers = new HashSet<>();

    public ArmorStandMirrorManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean spawnMirror(Player player) {
        removeMirror(player); // Stellt sicher, dass nur einer existiert

        Location spawnLocation = player.getLocation().add(player.getLocation().getDirection().multiply(2));
        spawnLocation.setY(player.getLocation().getY());

        ArmorStand stand = (ArmorStand) player.getWorld().spawnEntity(spawnLocation, EntityType.ARMOR_STAND);

        // Setze es auf 'true' wenn du ihn nicht sehen willst
        stand.setInvisible(false);
        stand.setMarker(false);
        stand.setGravity(false);
        stand.setSilent(true);
        stand.setRemoveWhenFarAway(false);
        stand.setPersistent(true);
        stand.getEquipment().setArmorContents(player.getInventory().getArmorContents());

        standToPlayer.put(stand.getUniqueId(), player.getUniqueId());
        return false;
    }

    public boolean toggleDamageTransfer(Player player) {
        if (damageActivePlayers.contains(player.getUniqueId())) {
            damageActivePlayers.remove(player.getUniqueId());
            return false;
        } else {
            damageActivePlayers.add(player.getUniqueId());
            return true;
        }
    }

    public boolean isDamageActive(UUID playerUuid) {
        return damageActivePlayers.contains(playerUuid);
    }

    public Player getPlayer(Entity armorStand) {
        UUID uuid = standToPlayer.get(armorStand.getUniqueId());
        return uuid == null ? null : Bukkit.getPlayer(uuid);
    }

    public boolean hasMirror(Player player) {
        return standToPlayer.containsValue(player.getUniqueId());
    }

    // HIER WAR DER FEHLER: @Override entfernt.
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

        damageActivePlayers.remove(player.getUniqueId());

        return removed[0];
    }

    public void cleanup() {
        standToPlayer.keySet().forEach(uuid -> {
            Entity e = Bukkit.getEntity(uuid);
            if (e != null) e.remove();
        });
        standToPlayer.clear();
        damageActivePlayers.clear(); // Auch hier aufräumen
    }
}