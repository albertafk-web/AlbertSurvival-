package com.albertsurvival.command;

import com.albertsurvival.AlbertSurvival;
import com.albertsurvival.data.PlayerData;
import com.albertsurvival.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AlbertCommand implements CommandExecutor, TabCompleter {

    private final AlbertSurvival plugin;

    public AlbertCommand(AlbertSurvival plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("AlbertSurvival v" + plugin.getDescription().getVersion())
                    .color(NamedTextColor.AQUA));
            sender.sendMessage(Component.text("Komutlar: reload, status, setthirst, settemperature, reset, debug, hud")
                    .color(NamedTextColor.GRAY));
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "reload" -> {
                if (!sender.hasPermission("albertsurvival.reload")) {
                    MessageUtil.sendRaw(sender instanceof Player p ? p : null, plugin.getConfigManager().getMessage("no-permission"));
                    if (!(sender instanceof Player)) sender.sendMessage("Yetkin yok.");
                    return true;
                }
                plugin.reloadPlugin();
                sender.sendMessage(Component.text("Yapılandırma yeniden yüklendi.").color(NamedTextColor.GREEN));
            }
            case "status" -> {
                if (!sender.hasPermission("albertsurvival.status")) {
                    sender.sendMessage(Component.text("Yetkin yok.").color(NamedTextColor.RED));
                    return true;
                }
                Player target = resolvePlayer(sender, args, 1);
                if (target == null) return true;
                PlayerData data = plugin.getDataManager().get(target);
                sender.sendMessage(Component.text(target.getName() + " - Susuzluk: " + String.format("%.1f", data.getThirst())
                        + " | Sıcaklık: " + String.format("%.1f", data.getTemperature())).color(NamedTextColor.AQUA));
            }
            case "setthirst" -> {
                if (!sender.hasPermission("albertsurvival.setthirst")) {
                    sender.sendMessage(Component.text("Yetkin yok.").color(NamedTextColor.RED));
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Kullanım: /as setthirst <oyuncu> <0-100>").color(NamedTextColor.YELLOW));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(Component.text("Oyuncu bulunamadı.").color(NamedTextColor.RED));
                    return true;
                }
                try {
                    double val = Double.parseDouble(args[2]);
                    plugin.getThirstManager().setThirst(target, val);
                    sender.sendMessage(Component.text(target.getName() + " susuzluğu " + val + " olarak ayarlandı.")
                            .color(NamedTextColor.GREEN));
                } catch (NumberFormatException e) {
                    sender.sendMessage(Component.text("Geçersiz sayı.").color(NamedTextColor.RED));
                }
            }
            case "settemperature" -> {
                if (!sender.hasPermission("albertsurvival.settemperature")) {
                    sender.sendMessage(Component.text("Yetkin yok.").color(NamedTextColor.RED));
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Kullanım: /as settemperature <oyuncu> <-100..100>").color(NamedTextColor.YELLOW));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(Component.text("Oyuncu bulunamadı.").color(NamedTextColor.RED));
                    return true;
                }
                try {
                    double val = Double.parseDouble(args[2]);
                    plugin.getTemperatureManager().setTemperature(target, val);
                    sender.sendMessage(Component.text(target.getName() + " sıcaklığı " + val + " olarak ayarlandı.")
                            .color(NamedTextColor.GREEN));
                } catch (NumberFormatException e) {
                    sender.sendMessage(Component.text("Geçersiz sayı.").color(NamedTextColor.RED));
                }
            }
            case "reset" -> {
                if (!sender.hasPermission("albertsurvival.reset")) {
                    sender.sendMessage(Component.text("Yetkin yok.").color(NamedTextColor.RED));
                    return true;
                }
                Player target = resolvePlayer(sender, args, 1);
                if (target == null) return true;
                plugin.getDataManager().reset(target);
                sender.sendMessage(Component.text(target.getName() + " verileri sıfırlandı.").color(NamedTextColor.GREEN));
            }
            case "debug" -> {
                if (!sender.hasPermission("albertsurvival.debug")) {
                    sender.sendMessage(Component.text("Yetkin yok.").color(NamedTextColor.RED));
                    return true;
                }
                plugin.setDebug(!plugin.isDebug());
                sender.sendMessage(Component.text("Debug: " + (plugin.isDebug() ? "AÇIK" : "KAPALI"))
                        .color(NamedTextColor.YELLOW));
            }
            case "hud" -> {
                sender.sendMessage(Component.text("HUD: BossBar + ActionBar fallback aktif.")
                        .color(NamedTextColor.AQUA));
                sender.sendMessage(Component.text("Not: Vanilla HUD üzerine ikon çizimi sunucu eklentisi ile mümkün değildir.")
                        .color(NamedTextColor.GRAY));
            }
            default -> sender.sendMessage(Component.text("Bilinmeyen alt komut. /albertsurvival").color(NamedTextColor.RED));
        }
        return true;
    }

    private Player resolvePlayer(CommandSender sender, String[] args, int index) {
        if (args.length > index) {
            Player p = Bukkit.getPlayer(args[index]);
            if (p == null) {
                sender.sendMessage(Component.text("Oyuncu bulunamadı.").color(NamedTextColor.RED));
                return null;
            }
            return p;
        }
        if (sender instanceof Player p) return p;
        sender.sendMessage(Component.text("Konsoldan oyuncu adı belirtmelisin.").color(NamedTextColor.RED));
        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("reload", "status", "setthirst", "settemperature", "reset", "debug", "hud"), args[0]);
        }
        if (args.length == 2 && List.of("status", "setthirst", "settemperature", "reset").contains(args[0].toLowerCase())) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    private List<String> filter(List<String> options, String input) {
        return options.stream()
                .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }
}
