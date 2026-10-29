package com.albertsurvival.data;

import com.albertsurvival.AlbertSurvival;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private final AlbertSurvival plugin;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();
    private final File dataFolder;

    public PlayerDataManager(AlbertSurvival plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public PlayerData get(Player player) {
        return get(player.getUniqueId());
    }

    public PlayerData get(UUID uuid) {
        return cache.computeIfAbsent(uuid, id -> loadFromDisk(id));
    }

    public void load(Player player) {
        PlayerData data = loadFromDisk(player.getUniqueId());
        cache.put(player.getUniqueId(), data);
    }

    private PlayerData loadFromDisk(UUID uuid) {
        File file = new File(dataFolder, uuid + ".yml");
        double thirst = plugin.getConfigManager().getThirstInitial();
        double temp = plugin.getConfigManager().getTempInitial();

        if (file.exists()) {
            try {
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                thirst = yaml.getDouble("thirst", thirst);
                temp = yaml.getDouble("temperature", temp);
            } catch (Exception e) {
                plugin.getLogger().warning("Oyuncu verisi yüklenemedi: " + uuid + " - " + e.getMessage());
            }
        }
        return new PlayerData(uuid, thirst, temp);
    }

    public void save(Player player) {
        PlayerData data = cache.get(player.getUniqueId());
        if (data != null) {
            saveToDisk(data);
        }
    }

    public void saveToDisk(PlayerData data) {
        File file = new File(dataFolder, data.getUuid() + ".yml");
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("uuid", data.getUuid().toString());
        yaml.set("thirst", data.getThirst());
        yaml.set("temperature", data.getTemperature());
        yaml.set("last-update", System.currentTimeMillis());
        try {
            yaml.save(file);
            data.markClean();
        } catch (IOException e) {
            plugin.getLogger().severe("Oyuncu verisi kaydedilemedi: " + data.getUuid() + " - " + e.getMessage());
        }
    }

    public void saveAllSync() {
        for (PlayerData data : cache.values()) {
            if (data.isDirty()) {
                saveToDisk(data);
            }
        }
    }

    public void saveAllAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveAllSync);
    }

    public void unload(Player player) {
        PlayerData data = cache.remove(player.getUniqueId());
        if (data != null) {
            saveToDisk(data);
        }
    }

    public void reset(Player player) {
        PlayerData data = get(player);
        data.setThirst(plugin.getConfigManager().getThirstInitial());
        data.setTemperature(plugin.getConfigManager().getTempInitial());
        saveToDisk(data);
    }
}
