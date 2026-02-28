package com.townypassport;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class TownyPassportPlugin extends JavaPlugin {

    private static final int MAX_INIT_ATTEMPTS = 15;

    private Economy economy;
    private PassportStore passportStore;
    private boolean initialized;
    private int initAttempts;
    private BukkitTask initTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.passportStore = new PassportStore(this);
        this.passportStore.load();

        attemptInitialization();
        if (!initialized) {
            initTask = Bukkit.getScheduler().runTaskTimer(this, this::attemptInitialization, 40L, 40L);
        }
    }

    private void attemptInitialization() {
        if (initialized) {
            return;
        }

        initAttempts++;
        if (!setupVault()) {
            if (initAttempts >= MAX_INIT_ATTEMPTS) {
                getLogger().severe("Vault economy provider not found after " + initAttempts + " attempts. Disabling plugin.");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }

            getLogger().warning("Vault economy provider not ready yet (attempt " + initAttempts + "/" + MAX_INIT_ATTEMPTS + "). Retrying...");
            return;
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
        Bukkit.getOnlinePlayers().forEach(passportService::ensureTownOwnerStarterPassport);

        initialized = true;
        if (initTask != null) {
            initTask.cancel();
            initTask = null;
        }

        getLogger().info("TownyPassport enabled.");
    }

    @Override
    public void onDisable() {
        if (initTask != null) {
            initTask.cancel();
            initTask = null;
        }
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
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null || rsp.getProvider() == null) {
            return false;
        }

        economy = rsp.getProvider();
        return true;
    }
}
