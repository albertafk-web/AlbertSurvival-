package com.albertsurvival.temperature;

import com.albertsurvival.AlbertSurvival;
import com.albertsurvival.config.ConfigManager;
import com.albertsurvival.data.PlayerData;
import com.albertsurvival.util.MessageUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TemperatureManager {

    private final AlbertSurvival plugin;
    private final Map<UUID, String> lastStage = new ConcurrentHashMap<>();

    public TemperatureManager(AlbertSurvival plugin) {
        this.plugin = plugin;
    }

    public void tick(Player player) {
        ConfigManager cfg = plugin.getConfigManager();
        if (!cfg.isTempEnabled()) return;
        if (shouldIgnore(player)) return;
        if (player.hasPermission("albertsurvival.bypass.temperature")) return;

        PlayerData data = plugin.getDataManager().get(player);
        double current = data.getTemperature();
        double target = calculateTarget(player);

        double rate = cfg.getChangeRate();
        double next = current + (target - current) * rate;
        next = Math.max(cfg.getTempMin(), Math.min(cfg.getTempMax(), next));
        data.setTemperature(next);

        applyStageEffects(player, data, next);
    }

    private double calculateTarget(Player player) {
        ConfigManager cfg = plugin.getConfigManager();
        Location loc = player.getLocation();
        World world = loc.getWorld();
        if (world == null) return 0;

        double target = 0;

        // Biyom
        Biome biome = loc.getBlock().getBiome();
        String biomeKey = biome.getKey().getKey();
        target += cfg.getBiomeTemp(biomeKey);

        // Ortam
        if (world.getEnvironment() == World.Environment.NETHER) {
            target = Math.max(target, 60);
        } else if (world.getEnvironment() == World.Environment.THE_END) {
            target = Math.min(target, -15);
        }

        // Hava
        if (world.hasStorm()) {
            if (isColdBiome(biomeKey)) {
                target += cfg.getWeatherOffset("snow");
            } else {
                target += cfg.getWeatherOffset("rain");
            }
        }
        if (world.isThundering()) {
            target += cfg.getWeatherOffset("thunder");
        }

        // Zaman
        long time = world.getTime();
        boolean night = time > 13000 && time < 23000;
        if (night) {
            target += cfg.getNightOffset();
        }

        // Yükseklik
        int y = loc.getBlockY();
        int sea = cfg.getSeaLevel();
        if (y > sea) {
            target += ((y - sea) / 10.0) * cfg.getHeightPer10Above();
        } else if (y < sea) {
            target += ((sea - y) / 10.0) * cfg.getHeightPer10Below();
        }

        // Zırh
        if (cfg.isArmorEnabled()) {
            target += getArmorBonus(player, target);
        }

        // Bloklar
        target += scanNearbyBlocks(player);

        // Hareket
        if (player.isSprinting()) {
            target += cfg.getSprintHeat();
        }
        if (player.isSwimming()) {
            target += cfg.getSwimmingCool();
        } else if (player.isInWater()) {
            target += cfg.getInWaterCool();
        }

        return target;
    }

    private double getArmorBonus(Player player, double currentTarget) {
        ConfigManager cfg = plugin.getConfigManager();
        PlayerInventory inv = player.getInventory();
        double bonus = 0;
        ItemStack[] armor = inv.getArmorContents();
        for (ItemStack piece : armor) {
            if (piece == null || piece.getType() == Material.AIR) continue;
            String mat = piece.getType().name().toLowerCase();
            for (Map.Entry<String, Double> e : cfg.getArmorProtection().entrySet()) {
                if (mat.contains(e.getKey())) {
                    // Soğukta koruma, sıcakta azaltılmış
                    if (currentTarget < 0) {
                        bonus += e.getValue();
                    } else {
                        bonus += e.getValue() * 0.3; // sıcakta daha az koruma
                    }
                    break;
                }
            }
        }
        return bonus;
    }

    private double scanNearbyBlocks(Player player) {
        ConfigManager cfg = plugin.getConfigManager();
        int r = cfg.getBlockScanRadius();
        Location base = player.getLocation();
        World world = base.getWorld();
        if (world == null) return 0;

        double effect = 0;
        int count = 0;
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    if (count++ > 80) break; // performans limiti
                    Block b = world.getBlockAt(base.getBlockX() + x, base.getBlockY() + y, base.getBlockZ() + z);
                    Material type = b.getType();
                    String key = type.name().toLowerCase();
                    for (Map.Entry<String, Double> e : cfg.getBlockEffects().entrySet()) {
                        if (key.contains(e.getKey()) || type.name().equalsIgnoreCase(e.getKey())) {
                            effect += e.getValue() * (1.0 / (1 + Math.abs(x) + Math.abs(y) + Math.abs(z)));
                            break;
                        }
                    }
                }
            }
        }
        return Math.max(-50, Math.min(50, effect));
    }

    private void applyStageEffects(Player player, PlayerData data, double temp) {
        ConfigManager cfg = plugin.getConfigManager();
        String stage = "normal";

        if (temp <= cfg.getColdThreshold("critical")) {
            stage = "critical";
            applyColdDamage(player, data, "critical");
            applySlow(player, cfg.getColdSlowAmplifier("critical"));
            MessageUtil.sendOnce(player, "freeze-warning", 150);
        } else if (temp <= cfg.getColdThreshold("freeze")) {
            stage = "freeze";
            applyColdDamage(player, data, "freeze");
            applySlow(player, cfg.getColdSlowAmplifier("freeze"));
            MessageUtil.sendOnce(player, "freeze-warning", 200);
        } else if (temp <= cfg.getColdThreshold("severe")) {
            stage = "severe";
            applyColdDamage(player, data, "severe");
            applySlow(player, cfg.getColdSlowAmplifier("severe"));
            MessageUtil.sendOnce(player, "cold-severe", 300);
        } else if (temp <= cfg.getColdThreshold("chill")) {
            stage = "chill";
            applySlow(player, cfg.getColdSlowAmplifier("chill"));
            MessageUtil.sendOnce(player, "cold-warning", 400);
        } else if (temp <= cfg.getColdThreshold("cool")) {
            stage = "cool";
            MessageUtil.sendOnce(player, "cold-warning", 500);
        } else if (temp >= cfg.getHeatThreshold("critical")) {
            stage = "heat_critical";
            applyHeatDamage(player, data, "critical");
            MessageUtil.sendOnce(player, "burning-warning", 150);
        } else if (temp >= cfg.getHeatThreshold("burning")) {
            stage = "burning";
            applyHeatDamage(player, data, "burning");
            MessageUtil.sendOnce(player, "burning-warning", 200);
        } else if (temp >= cfg.getHeatThreshold("heatstroke")) {
            stage = "heatstroke";
            applyHeatDamage(player, data, "heatstroke");
            MessageUtil.sendOnce(player, "heat-severe", 300);
        } else if (temp >= cfg.getHeatThreshold("hot")) {
            stage = "hot";
            MessageUtil.sendOnce(player, "heat-warning", 400);
        } else if (temp >= cfg.getHeatThreshold("warm")) {
            stage = "warm";
            MessageUtil.sendOnce(player, "heat-warning", 500);
        }

        String prev = lastStage.put(player.getUniqueId(), stage);
        if (prev != null && !prev.equals(stage) && stage.equals("normal")) {
            clearSlow(player);
        }
    }

    private void applyColdDamage(Player player, PlayerData data, String stage) {
        if (player.hasPermission("albertsurvival.bypass.damage")) return;
        ConfigManager cfg = plugin.getConfigManager();
        long now = System.currentTimeMillis();
        long interval = cfg.getColdDamageInterval(stage) * 50L;
        if (now - data.getLastColdDamage() < interval) return;
        data.setLastColdDamage(now);
        double dmg = cfg.getColdDamage(stage);
        if (dmg > 0 && player.getHealth() > 0) {
            player.damage(dmg);
        }
    }

    private void applyHeatDamage(Player player, PlayerData data, String stage) {
        if (player.hasPermission("albertsurvival.bypass.damage")) return;
        ConfigManager cfg = plugin.getConfigManager();
        long now = System.currentTimeMillis();
        long interval = cfg.getHeatDamageInterval(stage) * 50L;
        if (now - data.getLastHeatDamage() < interval) return;
        data.setLastHeatDamage(now);
        double dmg = cfg.getHeatDamage(stage);
        if (dmg > 0 && player.getHealth() > 0) {
            player.damage(dmg);
        }
        if (cfg.isHeatSetFire(stage) && player.getFireTicks() < 40) {
            player.setFireTicks(60);
        }
    }

    private void applySlow(Player player, int amplifier) {
        if (amplifier < 0) return;
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, amplifier, true, false, false));
    }

    private void clearSlow(Player player) {
        // Sadece plugin kaynaklı yavaşlığı temizle - diğer eklentilere saygı
        // Basit yaklaşım: kısa süreli efektleri silme, sadece kendi eklediklerimizi takip etmiyoruz
        // Güvenli: hiçbir şey yapma veya sadece amplifier kontrolü
    }

    public void cleanupAllEffects() {
        lastStage.clear();
    }

    public void setTemperature(Player player, double value) {
        ConfigManager cfg = plugin.getConfigManager();
        PlayerData data = plugin.getDataManager().get(player);
        data.setTemperature(Math.max(cfg.getTempMin(), Math.min(cfg.getTempMax(), value)));
    }

    private boolean shouldIgnore(Player player) {
        ConfigManager cfg = plugin.getConfigManager();
        GameMode gm = player.getGameMode();
        if (cfg.isIgnoreCreativeTemp() && gm == GameMode.CREATIVE) return true;
        if (cfg.isIgnoreSpectatorTemp() && gm == GameMode.SPECTATOR) return true;
        return !player.isValid() || player.isDead();
    }

    private boolean isColdBiome(String key) {
        return key.contains("snow") || key.contains("ice") || key.contains("frozen") || key.contains("cold");
    }
}
