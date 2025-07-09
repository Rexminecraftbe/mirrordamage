package de.elia.cameraplugin.mirrordamage;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleDamageCommand implements CommandExecutor {

    private final ArmorStandMirrorManager manager;

    public ToggleDamageCommand(ArmorStandMirrorManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Nur Spieler können diesen Befehl nutzen.");
            return true;
        }

        if (!manager.hasMirror(p)) {
            p.sendMessage("§cDu musst zuerst mit /damgesarmor einen Test-ArmorStand erstellen!");
            return true;
        }

        boolean isNowActive = manager.toggleDamageTransfer(p);

        // --- DEBUG-NACHRICHT ---
        System.out.println("[MirrorDamage] ToggleDamageCommand: Status für " + p.getName() + " ist jetzt: " + isNowActive);

        if (isNowActive) {
            p.sendMessage("§aDer Schadenstransfer ist jetzt AKTIVIERT.");
        } else {
            p.sendMessage("§eDer Schadenstransfer ist jetzt DEAKTIVIERT.");
        }

        return true;
    }
}