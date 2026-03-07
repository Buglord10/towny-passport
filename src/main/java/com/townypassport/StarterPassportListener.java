package com.townypassport;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class StarterPassportListener implements Listener {

    private final PassportService passportService;

    public StarterPassportListener(PassportService passportService) {
        this.passportService = passportService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        passportService.ensureTownOwnerStarterPassport(event.getPlayer());
    }
}
