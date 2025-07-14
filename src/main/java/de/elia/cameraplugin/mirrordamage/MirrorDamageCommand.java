package de.elia.cameraplugin.mirrordamage;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.elia.cameraplugin.mirrordamage.VillagerMirrorManager;

/**
 * Befehl /mirrordamage (Alias /md) – toggelt pro Spieler den Test‑Villager.
 */
public class MirrorDamageCommand implements CommandExecutor {

    private final VillagerMirrorManager manager;

    public MirrorDamageCommand(VillagerMirrorManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Nur Spieler können diesen Befehl nutzen.");
            return true;
        }

        if (manager.removeMirror(p)) {
            p.sendMessage("§aDein Test‑Villager wurde entfernt und dein Inventar zurückgegeben.");
        } else {
            manager.spawnMirror(p);
            p.sendMessage("§aTest‑Villager gespawnt – dein Inventar wurde gesichert.");
        }
        return true;
    }
}