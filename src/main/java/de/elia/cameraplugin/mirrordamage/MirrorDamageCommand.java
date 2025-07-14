package de.elia.cameraplugin.mirrordamage;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Befehl /mirrordamage (Alias /md) – toggelt pro Spieler den Test‑ArmorStand.
 */
public class MirrorDamageCommand implements CommandExecutor {

    private final ArmorStandMirrorManager manager;

    public MirrorDamageCommand(ArmorStandMirrorManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Nur Spieler können diesen Befehl nutzen.");
            return true;
        }

        if (manager.removeMirror(p)) {
            p.sendMessage("§aDein Test‑ArmorStand wurde entfernt.");
        } else {
            manager.spawnMirror(p);
            p.sendMessage("§aTest‑ArmorStand gespawnt – greif ihn an, um den Schaden zu prüfen!");
        }
        return true;
    }
}