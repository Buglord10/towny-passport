package com.townypassport;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Locale;

public class SpawnCommandRestrictionListener implements Listener {

    private final PassportService passportService;
    private final TownyHook townyHook;

    public SpawnCommandRestrictionListener(PassportService passportService, TownyHook townyHook) {
        this.passportService = passportService;
        this.townyHook = townyHook;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        if (message == null || message.isBlank()) {
            return;
        }

        String raw = message.startsWith("/") ? message.substring(1) : message;
        String[] args = raw.trim().split("\\s+");
        if (args.length < 2) {
            return;
        }

        String root = args[0].toLowerCase(Locale.ROOT);
        String sub = args[1].toLowerCase(Locale.ROOT);
        if (!sub.equals("spawn")) {
            return;
        }

        Player player = event.getPlayer();
        String targetTown = null;

        if (root.equals("t") || root.equals("town")) {
            String townName = args.length >= 3 ? args[2] : townyHook.getTownForPlayer(player);
            if (townName != null && !townName.isBlank()) {
                targetTown = townName;
            }
        }

        if (root.equals("n") || root.equals("nation")) {
            String nationName;
            if (args.length >= 3) {
                nationName = args[2];
            } else {
                String playerTown = townyHook.getTownForPlayer(player);
                nationName = playerTown == null ? null : townyHook.getNationForTown(playerTown);
            }
            if (nationName != null && !nationName.isBlank()) {
                targetTown = townyHook.getCapitalTownForNation(nationName);
            }
        }

        if (targetTown == null || targetTown.isBlank()) {
            return;
        }

        if (passportService.canAccessTown(player.getUniqueId(), targetTown)) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "Spawn denied: you need a valid passport or visa for " + targetTown + " (or its nation).");
    }
}
