package com.albertsurvival.manager;

import com.albertsurvival.AlbertSurvival;
import com.albertsurvival.config.ConfigManager;
import com.albertsurvival.data.PlayerData;
import com.albertsurvival.util.MessageUtil;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ThirstManager {

    private final AlbertSurvival plugin;

    public ThirstManager(AlbertSurvival plugin) {
        this.plugin = plugin;
    }

    public void tick(Player player) {
        ConfigManager cfg = plugin.getConfigManager();
        if (!cfg.isThirstEnabled()) return;
        if (shouldIgnore(player)) return;
        if (player.hasPermission("albertsurvival.bypass.thirst")) return;

        PlayerData data = plugin.getDataManager().get(player);
        double current = data.getThirst();
        if (current <= cfg.getThirstMin()) {
            applyCriticalEffects(player, data);
            return;
        }

        double decay = cfg.getThirstBaseDecay();

        if (player.isSprinting()) {
            decay += cfg.getThirstSprint();
        }

        // Jump tracking basit: y velocity
        if (player.getVelocity().getY() > 0.3) {
            decay += cfg.getThirstJump();
        }

        World world = player.getWorld();
        String env = world.getEnvironment().name();
        if (env.equals("NETHER")) {
            decay *= cfg.getNetherMultiplier();
        } else if (env.equals("THE_END")) {
            decay *= cfg.getEndMultiplier();
        } else {
            // Sıcak biyom kontrolü basit
            String biome = player.getLocation().getBlock().getBiome().getKey().getKey();
            double biomeTemp = cfg.getBiomeTemp(biome);
            if (biomeTemp > 25) {
                decay *= cfg.getHotBiomeMultiplier();
            }
        }

        if (world.hasStorm() || world.isThundering()) {
            decay *= cfg.getRainMultiplier();
        }

        if (player.isSwimming() || player.isInWater()) {
            decay *= cfg.getSwimmingMultiplier();
        }

        double newThirst = Math.max(cfg.getThirstMin(), current - decay);
        data.setThirst(newThirst);

        applyEffects(player, data, newThirst);
    }

    private void applyEffects(Player player, PlayerData data, double thirst) {
        ConfigManager cfg = plugin.getConfigManager();
        if (thirst <= cfg.getThirstCriticalLevel()) {
            applyCriticalEffects(player, data);
            if (thirst <= cfg.getThirstSevereLevel() + 5) {
                MessageUtil.sendOnce(player, "thirst-critical", 200);
            }
        } else if (thirst <= cfg.getThirstSevereLevel()) {
            MessageUtil.sendOnce(player, "thirst-severe", 300);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0, true, false, false));
        } else if (thirst <= 50) {
            MessageUtil.sendOnce(player, "thirst-warning", 400);
        }
    }

    private void applyCriticalEffects(Player player, PlayerData data) {
        if (player.hasPermission("albertsurvival.bypass.damage")) return;
        ConfigManager cfg = plugin.getConfigManager();
        long now = System.currentTimeMillis();
        long interval = cfg.getThirstDamageInterval() * 50L; // tick to ms approx
        if (now - data.getLastThirstDamage() < interval) return;

        data.setLastThirstDamage(now);
        double dmg = cfg.getThirstCriticalDamage();
        if (dmg > 0 && player.getHealth() > 0) {
            // Paper 1.20.4+ DamageSource, fallback to classic
            try {
                player.damage(dmg);
            } catch (Exception e) {
                player.setHealth(Math.max(0, player.getHealth() - dmg));
            }
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1, true, false, false));
    }

    public void restore(Player player, double amount) {
        ConfigManager cfg = plugin.getConfigManager();
        PlayerData data = plugin.getDataManager().get(player);
        double newVal = Math.min(cfg.getThirstMax(), data.getThirst() + amount);
        data.setThirst(newVal);
    }

    public void setThirst(Player player, double value) {
        ConfigManager cfg = plugin.getConfigManager();
        PlayerData data = plugin.getDataManager().get(player);
        data.setThirst(Math.max(cfg.getThirstMin(), Math.min(cfg.getThirstMax(), value)));
    }

    private boolean shouldIgnore(Player player) {
        ConfigManager cfg = plugin.getConfigManager();
        GameMode gm = player.getGameMode();
        if (cfg.isIgnoreCreativeThirst() && gm == GameMode.CREATIVE) return true;
        if (cfg.isIgnoreSpectatorThirst() && gm == GameMode.SPECTATOR) return true;
        return !player.isValid() || player.isDead();
    }
}
