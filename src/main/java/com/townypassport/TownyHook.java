package com.townypassport;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public class TownyHook {

    private final TownyPassportPlugin plugin;

    public TownyHook(TownyPassportPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isTownyAvailable() {
        Plugin towny = Bukkit.getPluginManager().getPlugin("Towny");
        return towny != null && towny.isEnabled();
    }

    public boolean townExists(String townName) {
        return getTownObject(townName) != null;
    }

    public boolean nationExists(String nationName) {
        if (!isTownyAvailable()) {
            return false;
        }
        try {
            Class<?> townyApiClass = Class.forName("com.palmergames.bukkit.towny.TownyAPI");
            Method getInstance = townyApiClass.getMethod("getInstance");
            Object api = getInstance.invoke(null);
            Method getNation = townyApiClass.getMethod("getNation", String.class);
            return getNation.invoke(api, nationName) != null;
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().warning("Failed nation lookup: " + ex.getMessage());
            return false;
        }
    }

    public String getTownAt(Location location) {
        if (!isTownyAvailable()) {
            return null;
        }
        try {
            Class<?> townyApiClass = Class.forName("com.palmergames.bukkit.towny.TownyAPI");
            Method getInstance = townyApiClass.getMethod("getInstance");
            Object api = getInstance.invoke(null);
            Method getTownMethod = townyApiClass.getMethod("getTown", Location.class);
            Object town = getTownMethod.invoke(api, location);
            if (town == null) {
                return null;
            }
            Method getName = town.getClass().getMethod("getName");
            return (String) getName.invoke(town);
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().warning("Failed resolving town at location: " + ex.getMessage());
            return null;
        }
    }

    public String getNationForTown(String townName) {
        Object town = getTownObject(townName);
        if (town == null) {
            return null;
        }

        try {
            Method hasNation = town.getClass().getMethod("hasNation");
            boolean inNation = (boolean) hasNation.invoke(town);
            if (!inNation) {
                return null;
            }

            Object nation = null;
            try {
                nation = town.getClass().getMethod("getNation").invoke(town);
            } catch (NoSuchMethodException ignored) {
                nation = town.getClass().getMethod("getNationOrNull").invoke(town);
            }

            if (nation == null) {
                return null;
            }

            Method getName = nation.getClass().getMethod("getName");
            return (String) getName.invoke(nation);
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().warning("Failed fetching nation for town " + townName + ": " + ex.getMessage());
            return null;
        }
    }

    public boolean canIssueTownDocuments(Player player, String townName) {
        if (player.hasPermission("townypassport.issue.town.*") || player.hasPermission("townypassport.admin")) {
            return true;
        }
        return player.hasPermission("townypassport.issue.town." + townName.toLowerCase());
    }

    public boolean canIssueNationDocuments(Player player, String nationName) {
        if (player.hasPermission("townypassport.issue.nation.*") || player.hasPermission("townypassport.admin")) {
            return true;
        }
        return player.hasPermission("townypassport.issue.nation." + nationName.toLowerCase());
    }

    private Object getTownObject(String townName) {
        if (!isTownyAvailable()) {
            return null;
        }

        try {
            Class<?> townyApiClass = Class.forName("com.palmergames.bukkit.towny.TownyAPI");
            Method getInstance = townyApiClass.getMethod("getInstance");
            Object api = getInstance.invoke(null);
            Method getTownMethod = townyApiClass.getMethod("getTown", String.class);
            return getTownMethod.invoke(api, townName);
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().warning("Failed town lookup: " + ex.getMessage());
            return null;
        }
    }
}
