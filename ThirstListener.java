package com.albertsurvival.listener;

import com.albertsurvival.AlbertSurvival;
import com.albertsurvival.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public class ThirstListener implements Listener {

    private final AlbertSurvival plugin;

    public ThirstListener(AlbertSurvival plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        if (!plugin.getConfigManager().isThirstEnabled()) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Material type = item.getType();
        ConfigManager cfg = plugin.getConfigManager();

        double restore = 0;

        if (type == Material.POTION) {
            // Su şişesi kontrolü
            if (item.hasItemMeta() && item.getItemMeta() instanceof PotionMeta meta) {
                PotionType potionType = meta.getBasePotionType();
                if (potionType == PotionType.WATER || potionType == null) {
                    restore = cfg.getWaterBottleRestore();
                } else {
                    restore = cfg.getItemRestore("potion");
                }
            } else {
                restore = cfg.getWaterBottleRestore();
            }
        } else if (type == Material.MILK_BUCKET) {
            restore = cfg.getItemRestore("milk-bucket");
        } else if (type == Material.HONEY_BOTTLE) {
            restore = cfg.getItemRestore("honey-bottle");
        }

        if (restore > 0) {
            plugin.getThirstManager().restore(player, restore);
            plugin.debugLog(player.getName() + " içti, +" + restore + " susuzluk");
        }
    }
}
