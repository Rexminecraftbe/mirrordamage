package de.elia.cameraplugin.mirrordamage;

import org.bukkit.plugin.java.JavaPlugin;

// Neue Listener und Command-Klasse
import de.elia.cameraplugin.mirrordamage.MirrorDamageCommand;
import de.elia.cameraplugin.mirrordamage.MirrorCleanupListener;
import de.elia.cameraplugin.mirrordamage.DamageTransferListener;
import de.elia.cameraplugin.mirrordamage.VillagerMirrorManager;
import de.elia.cameraplugin.mirrordamage.VillagerJobBlockListener;

public class MirrorDamagePlugin extends JavaPlugin {

    private VillagerMirrorManager mirrorManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        boolean damageArmor = getConfig().getBoolean("damage-armor", true);
        boolean villagerGravity = getConfig().getBoolean("villager-gravity", true);
        // Manager instanziieren & Listener + Command registrieren
        this.mirrorManager = new VillagerMirrorManager(this, villagerGravity);

        getServer().getPluginManager().registerEvents(new DamageTransferListener(mirrorManager, damageArmor), this);
        getServer().getPluginManager().registerEvents(new MirrorCleanupListener(mirrorManager), this);
        getServer().getPluginManager().registerEvents(new VillagerJobBlockListener(mirrorManager), this);
        // Command /mirrordamage (alias /md)
        getCommand("mirrordamage").setExecutor(new MirrorDamageCommand(mirrorManager));

        getLogger().info("MirrorDamage aktiviert");
    }

    @Override
    public void onDisable() {
        // Aufräumen: übrig gebliebene Villager entfernen
        if (mirrorManager != null) mirrorManager.cleanup();
    }
}