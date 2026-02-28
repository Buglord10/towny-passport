package com.townypassport;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class BorderListener implements Listener {

    private final PassportService passportService;
    private final TownyHook townyHook;

    public BorderListener(PassportService passportService, TownyHook townyHook) {
        this.passportService = passportService;
        this.townyHook = townyHook;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null || event.getFrom().getBlock().equals(event.getTo().getBlock())) {
            return;
        }

        String fromTown = townyHook.getTownAt(event.getFrom());
        String toTown = townyHook.getTownAt(event.getTo());

        if (toTown == null || toTown.equalsIgnoreCase(fromTown)) {
            return;
        }

        if (passportService.canEnterTown(event.getPlayer().getUniqueId(), toTown)) {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.RED + "Entry denied: you need a valid passport for town " + toTown
                + " or its nation.");
    }
}
