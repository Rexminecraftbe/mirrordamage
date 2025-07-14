package de.elia.cameraplugin.mirrordamage;

import org.bukkit.plugin.java.JavaPlugin;

// Neue Listener und Command-Klasse
import de.elia.cameraplugin.mirrordamage.MirrorDamageCommand;
import de.elia.cameraplugin.mirrordamage.MirrorCleanupListener;
import de.elia.cameraplugin.mirrordamage.DamageTransferListener;
import de.elia.cameraplugin.mirrordamage.ArmorStandMirrorManager;

public class MirrorDamagePlugin extends JavaPlugin {

    private ArmorStandMirrorManager mirrorManager;

    @Override
    public void onEnable() {
        // Manager instanziieren & Listener + Command registrieren
        this.mirrorManager = new ArmorStandMirrorManager(this);

        getServer().getPluginManager().registerEvents(new DamageTransferListener(mirrorManager), this);
        getServer().getPluginManager().registerEvents(new MirrorCleanupListener(mirrorManager), this);
        // Command /mirrordamage (alias /md)
        getCommand("mirrordamage").setExecutor(new MirrorDamageCommand(mirrorManager));

        getLogger().info("MirrorDamage aktiviert");
    }

    @Override
    public void onDisable() {
        // Aufräumen: übrig gebliebene ArmorStands entfernen
        if (mirrorManager != null) mirrorManager.cleanup();
    }
}