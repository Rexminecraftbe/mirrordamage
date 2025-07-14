package de.elia.cameraplugin.mirrordamage;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
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
    private final Map<UUID, ItemStack[]> savedInventories = new HashMap<>();

    public enum ReturnMode {
        RESTORE,
        DROP,
        KEEP
    }

    public VillagerMirrorManager(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Spawns (or replaces) an invisible, no-AI villager that mirrors the
     * player's armour.
     */
    public void spawnMirror(Player player) {
        removeMirror(player, ReturnMode.RESTORE);

        Location loc = player.getLocation();
        Villager villager = (Villager) player.getWorld().spawnEntity(loc, EntityType.VILLAGER);
        villager.setInvisible(true);
        villager.setAI(false);
        villager.setSilent(true);
        villager.setInvulnerable(true); // keep the villager alive

        villager.getEquipment().setArmorContents(player.getInventory().getArmorContents());

        ItemStack[] contents = player.getInventory().getContents();
        savedInventories.put(player.getUniqueId(), contents);
        player.getInventory().clear();
        player.updateInventory();

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
    public boolean removeMirror(Player player, ReturnMode mode) {
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

        if (removed[0]) {
            if (mode == ReturnMode.RESTORE) {
                restoreInventory(player);
            } else if (mode == ReturnMode.DROP) {
                dropInventory(player);
            }
        }

        return removed[0];
    }

    public boolean removeMirror(Player player) {
        return removeMirror(player, ReturnMode.RESTORE);
    }

    public void restoreInventory(Player player) {
        ItemStack[] contents = savedInventories.remove(player.getUniqueId());
        if (contents != null) {
            player.getInventory().setContents(contents);
            player.updateInventory();
        }
    }

    private void dropInventory(Player player) {
        ItemStack[] contents = savedInventories.remove(player.getUniqueId());
        if (contents == null) return;
        Location loc = player.getLocation();
        for (ItemStack item : contents) {
            if (item != null && item.getType().isItem()) {
                player.getWorld().dropItemNaturally(loc, item);
            }
        }
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