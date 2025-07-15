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
    private final Map<UUID, ItemStack[]> storedInventories = new HashMap<>();

    public VillagerMirrorManager(Plugin plugin, boolean villagerGravity) {
        this.plugin = plugin;
    }

    /**
     * Spawns (or replaces) an invisible, no-AI villager that mirrors the
     * player's armour.
     */
    public void spawnMirror(Player player) {
        removeMirror(player);  // ensure only one exists

        // store inventory (including armour) and clear it
        ItemStack[] contents = player.getInventory().getContents();
        storedInventories.put(player.getUniqueId(), contents);
        ItemStack[] armor = player.getInventory().getArmorContents();
        player.getInventory().clear();


        Location loc = player.getLocation();
        Villager villager = player.getWorld().spawn(loc, Villager.class, v -> {
            v.setInvisible(false);
            // Enable AI so gravity and water flow apply, but freeze the villager
            // by setting its movement speed to zero.
            v.setAI(true);
            // Use GENERIC_MOVEMENT_SPEED which controls how fast a mob can move.
            var speedAttr = v.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED);
            if (speedAttr != null) speedAttr.setBaseValue(0.0);
            // Give the mirror villager a profession but block all interaction
            // so players cannot trade with it.
            v.setProfession(Villager.Profession.FISHERMAN);
            v.setGravity(true); // allow falling and water movement
            v.setSilent(true);
            // The damage transfer listener cancels any incoming damage,
            // so the villager doesn't need to be invulnerable. Keeping it
            // vulnerable allows events like falling anvils to trigger and
            // be redirected to the player.
            v.setInvulnerable(false);

            // give the villager the player's armour so durability changes can be
            // reflected back later
            v.getEquipment().setArmorContents(armor);
        });

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

    /** Returns the mirror villager for the given player or {@code null}. */
    public Villager getMirror(Player player) {
        for (var entry : villagerToPlayer.entrySet()) {
            if (entry.getValue().equals(player.getUniqueId())) {
                Entity e = Bukkit.getEntity(entry.getKey());
                if (e instanceof Villager v) return v;
            }
        }
        return null;
    }

    /** Returns the stored inventory for the player if one exists. */
    public ItemStack[] getStoredInventory(Player player) {
        return storedInventories.get(player.getUniqueId());
    }

    /** Entfernt den Mirror‑Villager eines Spielers. @return true wenn einer entfernt wurde */
    public boolean removeMirror(Player player) {
        return removeMirror(player, true);
    }

    public boolean removeMirror(Player player, boolean restoreInventory) {
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
        if (removed[0] && restoreInventory) {
            ItemStack[] items = storedInventories.remove(player.getUniqueId());
            if (items != null && player.isOnline()) {
                player.getInventory().setContents(items);
            } else if (items != null) {
                storedInventories.put(player.getUniqueId(), items);
            }
        }
        return removed[0];
    }

    /**
     * Drops stored items at the given player's location and removes them from storage.
     */
    public void dropInventory(Player player) {
        ItemStack[] items = storedInventories.remove(player.getUniqueId());
        if (items == null) return;
        Location loc = player.getLocation();
        for (ItemStack item : items) {
            if (item != null && item.getType().isItem()) {
                loc.getWorld().dropItemNaturally(loc, item);
            }
        }
    }

    /**
     * Restores stored inventory when the player rejoins.
     */
    public void restoreInventory(Player player) {
        ItemStack[] items = storedInventories.remove(player.getUniqueId());
        if (items != null) {
            player.getInventory().setContents(items);
        }
    }

    /** Für onDisable */
    public void cleanup() {
        villagerToPlayer.keySet().forEach(uuid -> {
            Entity e = Bukkit.getEntity(uuid);
            if (e != null) e.remove();
        });
        villagerToPlayer.clear();

        // give back inventories to online players
        storedInventories.forEach((uuid, items) -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.getInventory().setContents(items);
            }
        });
        storedInventories.clear();
    }
}