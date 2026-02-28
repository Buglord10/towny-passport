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

        PassportCommand passportExecutor = new PassportCommand(passportService, townyHook, portraitIntegration);
        bindCommand("townypassport", passportExecutor, passportExecutor);
        bindCommand("passport", passportExecutor, passportExecutor);

        VisaCommand visaExecutor = new VisaCommand(passportService, townyHook);
        bindCommand("townyvisa", visaExecutor, visaExecutor);
        bindCommand("visa", visaExecutor, visaExecutor);

        Bukkit.getPluginManager().registerEvents(new BorderListener(passportService, townyHook), this);
        Bukkit.getPluginManager().registerEvents(new StarterPassportListener(passportService), this);
        Bukkit.getOnlinePlayers().forEach(passportService::ensureTownOwnerStarterPassport);
        getLogger().info("TownyPassport enabled.");
    }

    @Override
    public void onDisable() {
        if (passportStore != null) {
            passportStore.save();
        }
    }

    private void bindCommand(String name, org.bukkit.command.CommandExecutor executor, org.bukkit.command.TabCompleter completer) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            return;
        }
        command.setExecutor(executor);
        command.setTabCompleter(completer);
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
