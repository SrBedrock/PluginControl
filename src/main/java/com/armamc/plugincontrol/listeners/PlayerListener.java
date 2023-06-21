package com.armamc.plugincontrol.listeners;

import com.armamc.plugincontrol.PluginControl;
import com.armamc.plugincontrol.config.Config;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerListener implements Listener {
    private final Config config;

    public PlayerListener(PluginControl plugin) {
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
        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, config.parseColor(kickMessage));
    }
}
