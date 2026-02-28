package com.townypassport;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class PortraitIntegration {

    private final TownyPassportPlugin plugin;

    public PortraitIntegration(TownyPassportPlugin plugin) {
        this.plugin = plugin;
    }

    public String resolvePortraitLine(OfflinePlayer player) {
        if (!plugin.getConfig().getBoolean("book-portrait.enabled", false)) {
            return "";
        }

        String template = plugin.getConfig().getString("book-portrait.template", "");
        if (template == null || template.isBlank()) {
            return "";
        }

        String withName = template.replace("{player}", player.getName() == null ? "unknown" : player.getName());

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            try {
                Class<?> papiClass = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
                Object output = papiClass
                        .getMethod("setPlaceholders", org.bukkit.OfflinePlayer.class, String.class)
                        .invoke(null, player, withName);
                return output instanceof String ? (String) output : withName;
            } catch (Exception ignored) {
                return withName;
            }
        }

        return withName;
    }
}
