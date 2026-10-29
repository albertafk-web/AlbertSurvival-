package com.albertsurvival;

import com.albertsurvival.command.AlbertCommand;
import com.albertsurvival.config.ConfigManager;
import com.albertsurvival.data.PlayerDataManager;
import com.albertsurvival.hud.HudManager;
import com.albertsurvival.listener.PlayerListener;
import com.albertsurvival.listener.ThirstListener;
import com.albertsurvival.manager.ThirstManager;
import com.albertsurvival.temperature.TemperatureManager;
import com.albertsurvival.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class AlbertSurvival extends JavaPlugin {

    private static AlbertSurvival instance;
    private ConfigManager configManager;
    private PlayerDataManager dataManager;
    private ThirstManager thirstManager;
    private TemperatureManager temperatureManager;
    private HudManager hudManager;
    private BukkitTask mainTask;
    private BukkitTask saveTask;
    private boolean debug;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.configManager.load();

        this.dataManager = new PlayerDataManager(this);
        this.thirstManager = new ThirstManager(this);
        this.temperatureManager = new TemperatureManager(this);
        this.hudManager = new HudManager(this);

        MessageUtil.init(this);

        // Komutlar
        AlbertCommand command = new AlbertCommand(this);
        var cmd = getCommand("albertsurvival");
        if (cmd != null) {
            cmd.setExecutor(command);
            cmd.setTabCompleter(command);
        }

        // Listener'lar
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ThirstListener(this), this);

        // Online oyuncuları yükle
        Bukkit.getOnlinePlayers().forEach(dataManager::load);

        startTasks();

        getLogger().info("AlbertSurvival v" + getDescription().getVersion() + " etkinleştirildi (Paper 26.3).");
        getLogger().info("HUD: BossBar + ActionBar fallback kullanılıyor. Tam vanilla HUD ikonu için client mod gerekir.");
    }

    @Override
    public void onDisable() {
        cancelTasks();
        if (dataManager != null) {
            dataManager.saveAllSync();
        }
        if (hudManager != null) {
            hudManager.clearAll();
        }
        if (temperatureManager != null) {
            temperatureManager.cleanupAllEffects();
        }
        getLogger().info("AlbertSurvival devre dışı bırakıldı. Veriler kaydedildi.");
    }

    public void reloadPlugin() {
        cancelTasks();
        reloadConfig();
        configManager.load();
        MessageUtil.init(this);
        startTasks();
        getLogger().info("AlbertSurvival yeniden yüklendi.");
    }

    private void startTasks() {
        int interval = configManager.getUpdateInterval();
        mainTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (var player : Bukkit.getOnlinePlayers()) {
                if (!player.isOnline()) continue;
                thirstManager.tick(player);
                temperatureManager.tick(player);
                hudManager.update(player);
            }
        }, interval, interval);

        int saveSec = configManager.getSaveIntervalSeconds();
        saveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            dataManager.saveAllAsync();
        }, saveSec * 20L, saveSec * 20L);
    }

    private void cancelTasks() {
        if (mainTask != null) {
            mainTask.cancel();
            mainTask = null;
        }
        if (saveTask != null) {
            saveTask.cancel();
            saveTask = null;
        }
    }

    public static AlbertSurvival getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public PlayerDataManager getDataManager() {
        return dataManager;
    }

    public ThirstManager getThirstManager() {
        return thirstManager;
    }

    public TemperatureManager getTemperatureManager() {
        return temperatureManager;
    }

    public HudManager getHudManager() {
        return hudManager;
    }

    public boolean isDebug() {
        return debug || configManager.isDebug();
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void debugLog(String msg) {
        if (isDebug()) {
            getLogger().info("[DEBUG] " + msg);
        }
    }
}
