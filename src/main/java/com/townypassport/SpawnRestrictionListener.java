package com.townypassport;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class SpawnRestrictionListener implements Listener {

    private final TownyPassportPlugin plugin;
    private final PassportService passportService;
    private final TownyHook townyHook;

    public SpawnRestrictionListener(TownyPassportPlugin plugin, PassportService passportService, TownyHook townyHook) {
        this.plugin = plugin;
        this.passportService = passportService;
        this.townyHook = townyHook;
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (!plugin.getConfig().getBoolean("spawn-restrictions.enabled", true)) {
            return;
        }

        Location respawnLocation = event.getRespawnLocation();
        String townName = townyHook.getTownAt(respawnLocation);
        if (townName == null) {
            return;
        }

        if (passportService.canEnterTown(event.getPlayer().getUniqueId(), townName)) {
            return;
        }

        if (plugin.getConfig().getBoolean("spawn-restrictions.fallback-to-world-spawn", true)) {
            World world = respawnLocation.getWorld();
            if (world != null) {
                event.setRespawnLocation(world.getSpawnLocation());
            }
        }

        event.getPlayer().sendMessage(ChatColor.RED + "Spawn denied in " + townName + ": you need a valid town or nation passport.");
    }
}
