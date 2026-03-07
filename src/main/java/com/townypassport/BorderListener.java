package com.townypassport;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BorderListener implements Listener {

    private final PassportService passportService;
    private final TownyHook townyHook;
    private final Map<UUID, Long> recentEjects = new HashMap<>();

    public BorderListener(PassportService passportService, TownyHook townyHook) {
        this.passportService = passportService;
        this.townyHook = townyHook;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null || event.getFrom().getBlock().equals(event.getTo().getBlock())) {
            return;
        }

        String toTown = townyHook.getTownAt(event.getTo());
        if (toTown == null || passportService.canAccessTown(event.getPlayer().getUniqueId(), toTown)) {
            return;
        }

        Location safeLocation = resolveSafeLocation(event.getPlayer(), event.getFrom());
        event.setCancelled(true);
        ejectPlayer(event.getPlayer(), toTown, safeLocation);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getTo() == null) {
            return;
        }

        String toTown = townyHook.getTownAt(event.getTo());
        if (toTown == null || passportService.canAccessTown(event.getPlayer().getUniqueId(), toTown)) {
            return;
        }

        Location safeLocation = resolveSafeLocation(event.getPlayer(), event.getFrom());
        event.setCancelled(true);
        ejectPlayer(event.getPlayer(), toTown, safeLocation);
    }

    private Location resolveSafeLocation(Player player, Location fallbackFrom) {
        String fromTown = fallbackFrom == null ? null : townyHook.getTownAt(fallbackFrom);
        if (fromTown == null || passportService.canAccessTown(player.getUniqueId(), fromTown)) {
            return fallbackFrom == null ? player.getWorld().getSpawnLocation() : fallbackFrom;
        }
        return player.getWorld().getSpawnLocation();
    }

    private void ejectPlayer(Player player, String deniedTown, Location safeLocation) {
        long now = System.currentTimeMillis();
        Long previous = recentEjects.get(player.getUniqueId());
        if (previous != null && now - previous < 1500L) {
            return;
        }
        recentEjects.put(player.getUniqueId(), now);

        player.teleport(safeLocation);
        player.sendMessage(ChatColor.RED + "You are not allowed in " + deniedTown + ". You were teleported to safety.");
    }
}
