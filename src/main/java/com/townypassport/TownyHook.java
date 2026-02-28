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
            Object api = townyApiClass.getMethod("getInstance").invoke(null);
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
            Object api = townyApiClass.getMethod("getInstance").invoke(null);
            Object town = townyApiClass.getMethod("getTown", Location.class).invoke(api, location);
            if (town == null) {
                return null;
            }
            return (String) town.getClass().getMethod("getName").invoke(town);
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
            boolean inNation = (boolean) town.getClass().getMethod("hasNation").invoke(town);
            if (!inNation) {
                return null;
            }

            Object nation;
            try {
                nation = town.getClass().getMethod("getNation").invoke(town);
            } catch (NoSuchMethodException ignored) {
                nation = town.getClass().getMethod("getNationOrNull").invoke(town);
            }
            if (nation == null) {
                return null;
            }
            return (String) nation.getClass().getMethod("getName").invoke(nation);
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().warning("Failed fetching nation for town " + townName + ": " + ex.getMessage());
            return null;
        }
    }

    public String getTownForPlayer(Player player) {
        if (!isTownyAvailable()) {
            return null;
        }
        try {
            Class<?> townyApiClass = Class.forName("com.palmergames.bukkit.towny.TownyAPI");
            Object api = townyApiClass.getMethod("getInstance").invoke(null);
            Object resident;
            try {
                resident = townyApiClass.getMethod("getResident", org.bukkit.OfflinePlayer.class).invoke(api, player);
            } catch (NoSuchMethodException ignored) {
                resident = townyApiClass.getMethod("getResident", org.bukkit.entity.Player.class).invoke(api, player);
            }
            if (resident == null) {
                return null;
            }
            Object town;
            try {
                town = resident.getClass().getMethod("getTownOrNull").invoke(resident);
            } catch (NoSuchMethodException ignored) {
                town = resident.getClass().getMethod("getTown").invoke(resident);
            }
            if (town == null) {
                return null;
            }
            return (String) town.getClass().getMethod("getName").invoke(town);
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().warning("Failed resolving player's town: " + ex.getMessage());
            return null;
        }
    }

    public boolean isTownOwner(Player player, String townName) {
        Object town = getTownObject(townName);
        if (town == null) {
            return false;
        }
        try {
            Object mayor = town.getClass().getMethod("getMayor").invoke(town);
            if (mayor == null) {
                return false;
            }
            try {
                Object mayorUuid = mayor.getClass().getMethod("getUUID").invoke(mayor);
                return mayorUuid instanceof java.util.UUID && ((java.util.UUID) mayorUuid).equals(player.getUniqueId());
            } catch (NoSuchMethodException ignored) {
                Object mayorPlayer = mayor.getClass().getMethod("getPlayer").invoke(mayor);
                return mayorPlayer instanceof Player && ((Player) mayorPlayer).getUniqueId().equals(player.getUniqueId());
            }
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().warning("Failed town owner check: " + ex.getMessage());
            return false;
        }
    }

    public boolean canIssueTownDocuments(Player player, String townName) {
        if (player.hasPermission("townypassport.issue.town.*") || player.hasPermission("townypassport.admin")) {
            return true;
        }
        return player.hasPermission("townypassport.issue.town." + townName.toLowerCase())
                || isTownOwner(player, townName);
    }

    public boolean canIssueNationDocuments(Player player, String nationName) {
        if (player.hasPermission("townypassport.issue.nation.*") || player.hasPermission("townypassport.admin")) {
            return true;
        }
        return player.hasPermission("townypassport.issue.nation." + nationName.toLowerCase());
    }



    public java.util.List<String> getTownNames() {
        if (!isTownyAvailable()) {
            return java.util.List.of();
        }
        try {
            Class<?> townyApiClass = Class.forName("com.palmergames.bukkit.towny.TownyAPI");
            Object api = townyApiClass.getMethod("getInstance").invoke(null);
            Object towns = townyApiClass.getMethod("getTowns").invoke(api);
            java.util.List<String> names = new java.util.ArrayList<>();
            if (towns instanceof java.lang.Iterable<?> iterable) {
                for (Object town : iterable) {
                    Object name = town.getClass().getMethod("getName").invoke(town);
                    if (name instanceof String) {
                        names.add((String) name);
                    }
                }
            }
            return names;
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().warning("Failed reading town list: " + ex.getMessage());
            return java.util.List.of();
        }
    }

    public java.util.List<String> getNationNames() {
        if (!isTownyAvailable()) {
            return java.util.List.of();
        }
        try {
            Class<?> townyApiClass = Class.forName("com.palmergames.bukkit.towny.TownyAPI");
            Object api = townyApiClass.getMethod("getInstance").invoke(null);
            Object nations = townyApiClass.getMethod("getNations").invoke(api);
            java.util.List<String> names = new java.util.ArrayList<>();
            if (nations instanceof java.lang.Iterable<?> iterable) {
                for (Object nation : iterable) {
                    Object name = nation.getClass().getMethod("getName").invoke(nation);
                    if (name instanceof String) {
                        names.add((String) name);
                    }
                }
            }
            return names;
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().warning("Failed reading nation list: " + ex.getMessage());
            return java.util.List.of();
        }
    }

    private Object getTownObject(String townName) {
        if (!isTownyAvailable()) {
            return null;
        }

        try {
            Class<?> townyApiClass = Class.forName("com.palmergames.bukkit.towny.TownyAPI");
            Object api = townyApiClass.getMethod("getInstance").invoke(null);
            return townyApiClass.getMethod("getTown", String.class).invoke(api, townName);
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().warning("Failed town lookup: " + ex.getMessage());
            return null;
        }
    }
}
