package de.elia.cameraplugin.mirrordamage;

import org.bukkit.plugin.java.JavaPlugin;

public class MirrorDamagePlugin extends JavaPlugin {

    private ArmorStandMirrorManager mirrorManager;

    @Override
    public void onEnable() {
        this.mirrorManager = new ArmorStandMirrorManager(this);

        getServer().getPluginManager().registerEvents(new DamageTransferListener(mirrorManager), this);

        // Alte und neue Befehle registrieren
        getCommand("damgesarmor").setExecutor(new DamgesArmorCommand(mirrorManager));
        getCommand("mirrordamage").setExecutor(new ToggleDamageCommand(mirrorManager)); // NEU

        getLogger().info("MirrorDamage aktiviert");

    }

    @Override
    public void onDisable() {
        // Aufräumen: übrig gebliebene ArmorStands entfernen
        if (mirrorManager != null) mirrorManager.cleanup();
    }
}