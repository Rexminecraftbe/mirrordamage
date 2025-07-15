package de.elia.cameraplugin.mirrordamage;

import org.bukkit.plugin.java.JavaPlugin;

// Neue Listener und Command-Klasse
import de.elia.cameraplugin.mirrordamage.MirrorDamageCommand;
import de.elia.cameraplugin.mirrordamage.MirrorCleanupListener;
import de.elia.cameraplugin.mirrordamage.DamageTransferListener;
import de.elia.cameraplugin.mirrordamage.VillagerMirrorManager;
import de.elia.cameraplugin.mirrordamage.DamageMode;

public class MirrorDamagePlugin extends JavaPlugin {

    private VillagerMirrorManager mirrorManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        boolean damageArmor = getConfig().getBoolean("damage-armor", true);
        boolean villagerGravity = true; // villager-gravity is now always enabled
        boolean villagerWearsArmor = getConfig().getBoolean("villager-wears-armor", true);

        String modeRaw = getConfig().getString("damage-mode", "mirror");
        DamageMode damageMode;
        if ("custom".equalsIgnoreCase(modeRaw)) {
            damageMode = DamageMode.CUSTOM;
        } else if ("false".equalsIgnoreCase(modeRaw) || "off".equalsIgnoreCase(modeRaw)) {
            damageMode = DamageMode.OFF;
        } else {
            damageMode = DamageMode.MIRROR;
        }

        double customHearts = getConfig().getDouble("custom-damage-hearts", 0.5);
        int customDurability = getConfig().getInt("custom-armor-damage", 1);

        // Manager instanziieren & Listener + Command registrieren
        this.mirrorManager = new VillagerMirrorManager(this, villagerGravity, villagerWearsArmor);

        DamageTransferListener dmgListener = new DamageTransferListener(mirrorManager, damageArmor,
                damageMode, customHearts, customDurability);

        getServer().getPluginManager().registerEvents(dmgListener, this);
        getServer().getPluginManager().registerEvents(new MirrorCleanupListener(mirrorManager), this);
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