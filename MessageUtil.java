package com.albertsurvival.util;

import com.albertsurvival.AlbertSurvival;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MessageUtil {

    private static AlbertSurvival plugin;
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final Map<String, Long> cooldowns = new ConcurrentHashMap<>();

    private MessageUtil() {}

    public static void init(AlbertSurvival pl) {
        plugin = pl;
    }

    public static void send(Player player, String key) {
        if (plugin == null) return;
        String raw = plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage(key);
        player.sendMessage(MM.deserialize(raw));
    }

    public static void sendOnce(Player player, String key, int cooldownTicks) {
        String id = player.getUniqueId() + ":" + key;
        long now = System.currentTimeMillis();
        long last = cooldowns.getOrDefault(id, 0L);
        long cd = cooldownTicks * 50L;
        if (now - last < cd) return;
        cooldowns.put(id, now);
        send(player, key);
    }

    public static Component parse(String mini) {
        return MM.deserialize(mini);
    }

    public static void sendRaw(Player player, String miniMessage) {
        player.sendMessage(MM.deserialize(miniMessage));
    }
}
