package com.townypassport;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class TownyPassportPlugin extends JavaPlugin {

    private Economy economy;
    private PassportStore passportStore;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (!setupVault()) {
            getLogger().severe("Vault economy not found. Disabling plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        TownyHook townyHook = new TownyHook(this);
        this.passportStore = new PassportStore(this);
        this.passportStore.load();
        PassportService passportService = new PassportService(this, economy, townyHook, passportStore);
        PortraitIntegration portraitIntegration = new PortraitIntegration(this);

        PluginCommand passportCommand = getCommand("passport");
        if (passportCommand != null) {
            PassportCommand executor = new PassportCommand(passportService, townyHook, portraitIntegration);
            passportCommand.setExecutor(executor);
            passportCommand.setTabCompleter(executor);
        }

        PluginCommand visaCommand = getCommand("visa");
        if (visaCommand != null) {
            VisaCommand executor = new VisaCommand(passportService, townyHook);
            visaCommand.setExecutor(executor);
            visaCommand.setTabCompleter(executor);
        }

        Bukkit.getPluginManager().registerEvents(new BorderListener(passportService, townyHook), this);
        getLogger().info("TownyPassport enabled.");
    }

    @Override
    public void onDisable() {
        if (passportStore != null) {
            passportStore.save();
        }
    }

    private boolean setupVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        economy = rsp.getProvider();
        return economy != null;
    }
}
