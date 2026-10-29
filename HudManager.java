package com.albertsurvival.hud;

import com.albertsurvival.AlbertSurvival;
import com.albertsurvival.config.ConfigManager;
import com.albertsurvival.data.PlayerData;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HUD Manager - Gerçek vanilla HUD üzerine ikon çizemez.
 * BossBar + ActionBar fallback kullanır.
 * Tam referans görünüm için client-side mod / resource pack + shader gerekir.
 */
public class HudManager {

    private final AlbertSurvival plugin;
    private final Map<UUID, BossBar> thirstBars = new ConcurrentHashMap<>();
    private final Map<UUID, BossBar> tempBars = new ConcurrentHashMap<>();
    private final MiniMessage mm = MiniMessage.miniMessage();

    public HudManager(AlbertSurvival plugin) {
        this.plugin = plugin;
    }

    public void update(Player player) {
        ConfigManager cfg = plugin.getConfigManager();
        if (!cfg.isHudEnabled()) return;

        PlayerData data = plugin.getDataManager().get(player);
        double thirst = data.getThirst();
        double temp = data.getTemperature();
        double thirstMax = cfg.getThirstMax();
        double tempMin = cfg.getTempMin();
        double tempMax = cfg.getTempMax();

        if (cfg.isUseBossBar()) {
            updateThirstBar(player, thirst, thirstMax);
            updateTempBar(player, temp, tempMin, tempMax);
        }

        if (cfg.isUseActionBar()) {
            String thirstColor = thirst <= 10 ? "<red>" : thirst <= 25 ? "<gold>" : "<aqua>";
            String tempColor = temp <= -50 ? "<dark_aqua>" : temp <= -25 ? "<blue>" : temp >= 50 ? "<red>" : temp >= 25 ? "<gold>" : "<green>";
            Component action = mm.deserialize(
                    thirstColor + "💧 " + String.format("%.0f", thirst) + "</" + thirstColor.substring(1) +
                    "  " + tempColor + "🌡 " + String.format("%.0f", temp) + "</" + tempColor.substring(1)
            );
            // Basit fallback
            player.sendActionBar(Component.text("💧 " + (int) thirst + "  |  🌡 " + (int) temp)
                    .color(NamedTextColor.AQUA));
        }
    }

    private void updateThirstBar(Player player, double thirst, double max) {
        float progress = (float) Math.max(0, Math.min(1, thirst / max));
        BossBar bar = thirstBars.computeIfAbsent(player.getUniqueId(), id -> {
            BossBar b = BossBar.bossBar(
                    Component.text("Susuzluk"),
                    progress,
                    BossBar.Color.BLUE,
                    BossBar.Overlay.NOTCHED_10
            );
            player.showBossBar(b);
            return b;
        });
        bar.progress(progress);
        bar.name(Component.text("💧 Susuzluk: " + (int) thirst + "/" + (int) max).color(NamedTextColor.AQUA));
        if (thirst <= 10) {
            bar.color(BossBar.Color.RED);
        } else if (thirst <= 25) {
            bar.color(BossBar.Color.YELLOW);
        } else {
            bar.color(BossBar.Color.BLUE);
        }
    }

    private void updateTempBar(Player player, double temp, double min, double max) {
        // Normalize -100..100 to 0..1
        float progress = (float) Math.max(0, Math.min(1, (temp - min) / (max - min)));
        BossBar bar = tempBars.computeIfAbsent(player.getUniqueId(), id -> {
            BossBar b = BossBar.bossBar(
                    Component.text("Sıcaklık"),
                    progress,
                    BossBar.Color.WHITE,
                    BossBar.Overlay.PROGRESS
            );
            player.showBossBar(b);
            return b;
        });
        bar.progress(progress);
        NamedTextColor color = NamedTextColor.GREEN;
        BossBar.Color barColor = BossBar.Color.GREEN;
        if (temp <= -50) {
            color = NamedTextColor.DARK_AQUA;
            barColor = BossBar.Color.BLUE;
        } else if (temp <= -25) {
            color = NamedTextColor.AQUA;
            barColor = BossBar.Color.BLUE;
        } else if (temp >= 50) {
            color = NamedTextColor.RED;
            barColor = BossBar.Color.RED;
        } else if (temp >= 25) {
            color = NamedTextColor.GOLD;
            barColor = BossBar.Color.YELLOW;
        }
        bar.color(barColor);
        bar.name(Component.text("🌡 Sıcaklık: " + (int) temp).color(color));
    }

    public void remove(Player player) {
        UUID id = player.getUniqueId();
        BossBar t = thirstBars.remove(id);
        if (t != null) player.hideBossBar(t);
        BossBar tmp = tempBars.remove(id);
        if (tmp != null) player.hideBossBar(tmp);
    }

    public void clearAll() {
        for (var entry : thirstBars.entrySet()) {
            Player p = plugin.getServer().getPlayer(entry.getKey());
            if (p != null) p.hideBossBar(entry.getValue());
        }
        for (var entry : tempBars.entrySet()) {
            Player p = plugin.getServer().getPlayer(entry.getKey());
            if (p != null) p.hideBossBar(entry.getValue());
        }
        thirstBars.clear();
        tempBars.clear();
    }
}
