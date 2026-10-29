package com.albertsurvival.config;

import com.albertsurvival.AlbertSurvival;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private final AlbertSurvival plugin;
    private FileConfiguration cfg;

    // Cached values
    private int updateInterval;
    private boolean debug;
    private boolean thirstEnabled;
    private double thirstMax;
    private double thirstMin;
    private double thirstInitial;
    private double thirstBaseDecay;
    private double thirstSprint;
    private double thirstJump;
    private double waterBottleRestore;
    private boolean tempEnabled;
    private double tempMin;
    private double tempMax;
    private double tempInitial;
    private double comfortMin;
    private double comfortMax;
    private double changeRate;
    private int tempUpdateInterval;
    private boolean hudEnabled;
    private boolean useBossBar;
    private boolean useActionBar;
    private int saveIntervalSeconds;
    private boolean ignoreCreativeThirst;
    private boolean ignoreSpectatorThirst;
    private boolean ignoreCreativeTemp;
    private boolean ignoreSpectatorTemp;
    private boolean resetThirstOnDeath;
    private boolean resetTempOnDeath;
    private final Map<String, Double> biomeTemps = new HashMap<>();
    private final Map<String, Double> armorProtection = new HashMap<>();
    private final Map<String, Double> blockEffects = new HashMap<>();

    public ConfigManager(AlbertSurvival plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.reloadConfig();
        cfg = plugin.getConfig();

        updateInterval = Math.max(10, cfg.getInt("general.update-interval-ticks", 40));
        debug = cfg.getBoolean("general.debug", false);

        thirstEnabled = cfg.getBoolean("thirst.enabled", true);
        thirstMax = cfg.getDouble("thirst.max", 100.0);
        thirstMin = cfg.getDouble("thirst.minimum", 0.0);
        thirstInitial = cfg.getDouble("thirst.initial", 100.0);
        thirstBaseDecay = cfg.getDouble("thirst.decay.base", 0.08);
        thirstSprint = cfg.getDouble("thirst.decay.sprint", 0.12);
        thirstJump = cfg.getDouble("thirst.decay.jump", 0.04);
        waterBottleRestore = cfg.getDouble("thirst.items.water-bottle", 35.0);
        ignoreCreativeThirst = cfg.getBoolean("thirst.ignore-creative", true);
        ignoreSpectatorThirst = cfg.getBoolean("thirst.ignore-spectator", true);
        resetThirstOnDeath = cfg.getBoolean("thirst.reset-on-death", false);

        tempEnabled = cfg.getBoolean("temperature.enabled", true);
        tempMin = cfg.getDouble("temperature.min", -100.0);
        tempMax = cfg.getDouble("temperature.max", 100.0);
        tempInitial = cfg.getDouble("temperature.initial", 0.0);
        comfortMin = cfg.getDouble("temperature.comfort-min", -10.0);
        comfortMax = cfg.getDouble("temperature.comfort-max", 10.0);
        changeRate = cfg.getDouble("temperature.change-rate", 0.35);
        tempUpdateInterval = Math.max(10, cfg.getInt("temperature.update-interval-ticks", 40));
        ignoreCreativeTemp = cfg.getBoolean("temperature.ignore-creative", true);
        ignoreSpectatorTemp = cfg.getBoolean("temperature.ignore-spectator", true);
        resetTempOnDeath = cfg.getBoolean("temperature.reset-on-death", false);

        hudEnabled = cfg.getBoolean("hud.enabled", true);
        useBossBar = cfg.getBoolean("hud.use-bossbar", true);
        useActionBar = cfg.getBoolean("hud.use-actionbar", true);

        saveIntervalSeconds = Math.max(30, cfg.getInt("performance.save-interval-seconds", 60));

        // Biomes
        biomeTemps.clear();
        ConfigurationSection biomes = cfg.getConfigurationSection("temperature.biomes");
        if (biomes != null) {
            for (String key : biomes.getKeys(false)) {
                biomeTemps.put(key.toLowerCase(), biomes.getDouble(key));
            }
        }

        // Armor
        armorProtection.clear();
        ConfigurationSection armor = cfg.getConfigurationSection("temperature.armor");
        if (armor != null) {
            for (String key : armor.getKeys(false)) {
                if (!key.equals("enabled") && !key.equals("heat-multiplier")) {
                    armorProtection.put(key.toLowerCase(), armor.getDouble(key));
                }
            }
        }

        // Blocks
        blockEffects.clear();
        ConfigurationSection blocks = cfg.getConfigurationSection("temperature.blocks");
        if (blocks != null) {
            for (String key : blocks.getKeys(false)) {
                if (!key.equals("radius")) {
                    blockEffects.put(key.toLowerCase(), blocks.getDouble(key));
                }
            }
        }

        validate();
    }

    private void validate() {
        if (thirstMax <= thirstMin) {
            plugin.getLogger().warning("thirst.max <= thirst.min, varsayılanlara dönülüyor.");
            thirstMax = 100;
            thirstMin = 0;
        }
        if (tempMax <= tempMin) {
            plugin.getLogger().warning("temperature.max <= temperature.min, varsayılanlara dönülüyor.");
            tempMax = 100;
            tempMin = -100;
        }
    }

    public String getMessage(String path) {
        return cfg.getString("messages." + path, path);
    }

    public String getPrefix() {
        return cfg.getString("messages.prefix", "<gray>[AlbertSurvival]</gray> ");
    }

    // Getters
    public int getUpdateInterval() { return updateInterval; }
    public boolean isDebug() { return debug; }
    public boolean isThirstEnabled() { return thirstEnabled; }
    public double getThirstMax() { return thirstMax; }
    public double getThirstMin() { return thirstMin; }
    public double getThirstInitial() { return thirstInitial; }
    public double getThirstBaseDecay() { return thirstBaseDecay; }
    public double getThirstSprint() { return thirstSprint; }
    public double getThirstJump() { return thirstJump; }
    public double getWaterBottleRestore() { return waterBottleRestore; }
    public boolean isTempEnabled() { return tempEnabled; }
    public double getTempMin() { return tempMin; }
    public double getTempMax() { return tempMax; }
    public double getTempInitial() { return tempInitial; }
    public double getComfortMin() { return comfortMin; }
    public double getComfortMax() { return comfortMax; }
    public double getChangeRate() { return changeRate; }
    public boolean isHudEnabled() { return hudEnabled; }
    public boolean isUseBossBar() { return useBossBar; }
    public boolean isUseActionBar() { return useActionBar; }
    public int getSaveIntervalSeconds() { return saveIntervalSeconds; }
    public boolean isIgnoreCreativeThirst() { return ignoreCreativeThirst; }
    public boolean isIgnoreSpectatorThirst() { return ignoreSpectatorThirst; }
    public boolean isIgnoreCreativeTemp() { return ignoreCreativeTemp; }
    public boolean isIgnoreSpectatorTemp() { return ignoreSpectatorTemp; }
    public boolean isResetThirstOnDeath() { return resetThirstOnDeath; }
    public boolean isResetTempOnDeath() { return resetTempOnDeath; }
    public Map<String, Double> getBiomeTemps() { return biomeTemps; }
    public Map<String, Double> getArmorProtection() { return armorProtection; }
    public Map<String, Double> getBlockEffects() { return blockEffects; }

    public double getBiomeTemp(String biomeKey) {
        return biomeTemps.getOrDefault(biomeKey.toLowerCase(), biomeTemps.getOrDefault("default", 0.0));
    }

    public double getHotBiomeMultiplier() {
        return cfg.getDouble("thirst.decay.hot-biome-multiplier", 1.6);
    }

    public double getNetherMultiplier() {
        return cfg.getDouble("thirst.decay.nether-multiplier", 2.2);
    }

    public double getEndMultiplier() {
        return cfg.getDouble("thirst.decay.end-multiplier", 1.3);
    }

    public double getRainMultiplier() {
        return cfg.getDouble("thirst.decay.rain-multiplier", 0.7);
    }

    public double getSwimmingMultiplier() {
        return cfg.getDouble("thirst.decay.swimming-multiplier", 0.9);
    }

    public double getThirstCriticalDamage() {
        return cfg.getDouble("thirst.effects.critical-damage", 1.0);
    }

    public int getThirstDamageInterval() {
        return cfg.getInt("thirst.effects.damage-interval-ticks", 60);
    }

    public double getThirstCriticalLevel() {
        return cfg.getDouble("thirst.effects.critical-level", 10.0);
    }

    public double getThirstSevereLevel() {
        return cfg.getDouble("thirst.effects.severe-level", 25.0);
    }

    public int getBlockScanRadius() {
        return cfg.getInt("temperature.blocks.radius", 3);
    }

    public boolean isArmorEnabled() {
        return cfg.getBoolean("temperature.armor.enabled", true);
    }

    public FileConfiguration getRaw() {
        return cfg;
    }

    public double getColdThreshold(String stage) {
        return cfg.getDouble("temperature.cold_stages." + stage + ".threshold", -100);
    }

    public double getHeatThreshold(String stage) {
        return cfg.getDouble("temperature.heat_stages." + stage + ".threshold", 100);
    }

    public double getColdDamage(String stage) {
        return cfg.getDouble("temperature.cold_stages." + stage + ".damage", 0);
    }

    public int getColdDamageInterval(String stage) {
        return cfg.getInt("temperature.cold_stages." + stage + ".damage-interval-ticks", 100);
    }

    public int getColdSlowAmplifier(String stage) {
        return cfg.getInt("temperature.cold_stages." + stage + ".slow-amplifier", -1);
    }

    public double getHeatDamage(String stage) {
        return cfg.getDouble("temperature.heat_stages." + stage + ".damage", 0);
    }

    public int getHeatDamageInterval(String stage) {
        return cfg.getInt("temperature.heat_stages." + stage + ".damage-interval-ticks", 100);
    }

    public boolean isHeatSetFire(String stage) {
        return cfg.getBoolean("temperature.heat_stages." + stage + ".set-fire", false);
    }

    public double getNightOffset() {
        return cfg.getDouble("temperature.time.night-offset", -8.0);
    }

    public double getWeatherOffset(String weather) {
        return cfg.getDouble("temperature.weather." + weather, 0.0);
    }

    public double getHeightPer10Above() {
        return cfg.getDouble("temperature.height.per-10-blocks-above", -3.0);
    }

    public double getHeightPer10Below() {
        return cfg.getDouble("temperature.height.per-10-blocks-below", 2.0);
    }

    public int getSeaLevel() {
        return cfg.getInt("temperature.height.sea-level", 64);
    }

    public double getSprintHeat() {
        return cfg.getDouble("temperature.movement.sprint-heat", 2.0);
    }

    public double getSwimmingCool() {
        return cfg.getDouble("temperature.movement.swimming-cool", -8.0);
    }

    public double getInWaterCool() {
        return cfg.getDouble("temperature.movement.in-water-cool", -5.0);
    }

    public double getItemRestore(String key) {
        return cfg.getDouble("thirst.items." + key, 0.0);
    }
}
