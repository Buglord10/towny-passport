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

        this.passportStore = new PassportStore(this);
        this.passportStore.load();

        this.economy = detectEconomyProvider();
        boolean economyRequired = getConfig().getBoolean("economy.required", false);
        if (economy == null && economyRequired) {
            getLogger().severe("No Vault economy provider found and economy.required=true. Disabling plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (economy == null) {
            getLogger().warning("No Vault economy provider found. Running in no-fee mode until one is registered.");
        } else {
            getLogger().info("Vault economy provider detected: " + economy.getName());
        }

        TownyHook townyHook = new TownyHook(this);
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
        Bukkit.getPluginManager().registerEvents(new EconomyProviderListener(this, passportService), this);
        Bukkit.getPluginManager().registerEvents(new SpawnRestrictionListener(this, passportService, townyHook), this);
        Bukkit.getPluginManager().registerEvents(new SpawnCommandRestrictionListener(passportService, townyHook), this);
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

    public Economy detectEconomyProvider() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null || rsp.getProvider() == null) {
            this.economy = null;
            return null;
        }

        this.economy = rsp.getProvider();
        return this.economy;
    }
}
