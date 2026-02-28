package com.townypassport;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;

public class EconomyProviderListener implements Listener {

    private final TownyPassportPlugin plugin;
    private final PassportService passportService;

    public EconomyProviderListener(TownyPassportPlugin plugin, PassportService passportService) {
        this.plugin = plugin;
        this.passportService = passportService;
    }

    @EventHandler
    public void onServiceRegister(ServiceRegisterEvent event) {
        if (!Economy.class.equals(event.getProvider().getService())) {
            return;
        }

        Economy economy = plugin.detectEconomyProvider();
        if (economy == null) {
            return;
        }

        passportService.updateEconomyProvider(economy);
        plugin.getLogger().info("Vault economy provider registered: " + economy.getName());
    }

    @EventHandler
    public void onServiceUnregister(ServiceUnregisterEvent event) {
        if (!Economy.class.equals(event.getProvider().getService())) {
            return;
        }

        Economy economy = plugin.detectEconomyProvider();
        passportService.updateEconomyProvider(economy);

        if (economy == null) {
            plugin.getLogger().warning("Vault economy provider unregistered. Fees are temporarily disabled.");
        } else {
            plugin.getLogger().info("Vault economy provider switched to: " + economy.getName());
        }
    }
}
