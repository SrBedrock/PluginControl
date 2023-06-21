package com.armamc.plugincontrol.listeners;

import com.armamc.plugincontrol.PluginControl;
import com.armamc.plugincontrol.config.Config;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerLoginListener implements Listener {
    private final Config config;

    public PlayerLoginListener(PluginControl plugin) {
        this.config = plugin.getPluginConfig();
        if (config.isEnabled()) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    // TODO: Add support for mini-message
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onPlayerLogin(PlayerLoginEvent event) {
        String kickMessage = config.getKickMessage();
        if (kickMessage == null || kickMessage.isEmpty() || kickMessage.isBlank()) {
            kickMessage = "[PluginControl] You are not allowed to join the server!";
        }
        if (event.getPlayer().hasPermission("plugincontrol.bypass")) {
            return;
        }
        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, parseColor(kickMessage));
    }

    private String parseColor(String message) {
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (char c : ch) {
                builder.append("&").append(c);
            }

            message = message.replace(hexCode, builder.toString());
            matcher = pattern.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
