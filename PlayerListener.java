package com.albertsurvival.listener;

import com.albertsurvival.AlbertSurvival;
import com.albertsurvival.config.ConfigManager;
import com.albertsurvival.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerListener implements Listener {

    private final AlbertSurvival plugin;

    public PlayerListener(AlbertSurvival plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getDataManager().load(player);
        plugin.debugLog("Oyuncu yüklendi: " + player.getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getHudManager().remove(player);
        plugin.getDataManager().unload(player);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        ConfigManager cfg = plugin.getConfigManager();
        PlayerData data = plugin.getDataManager().get(player);
        if (cfg.isResetThirstOnDeath()) {
            data.setThirst(cfg.getThirstInitial());
        }
        if (cfg.isResetTempOnDeath()) {
            data.setTemperature(cfg.getTempInitial());
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        // Veri zaten cache'de, HUD yeniden gösterilecek
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getHudManager().update(event.getPlayer());
        }, 5L);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        plugin.getHudManager().update(event.getPlayer());
    }
}
