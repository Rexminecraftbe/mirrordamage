package de.elia.cameraplugin.mirrordamage;

import org.bukkit.plugin.java.JavaPlugin;

public class MirrorDamagePlugin extends JavaPlugin {

    private ArmorStandMirrorManager mirrorManager;

    @Override
    public void onEnable() {
        // Manager instanziieren & Listener + Command registrieren
        this.mirrorManager = new ArmorStandMirrorManager(this);

        getServer().getPluginManager().registerEvents(new DamageTransferListener(mirrorManager), this);
        // Command /damgesarmor
        getCommand("damgesarmor").setExecutor(new DamgesArmorCommand(mirrorManager));

        getLogger().info("MirrorDamage aktiviert");
    }

    @Override
    public void onDisable() {
        // Aufräumen: übrig gebliebene ArmorStands entfernen
        if (mirrorManager != null) mirrorManager.cleanup();
    }
}