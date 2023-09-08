package com.armamc.plugincontrol.listeners;

import com.armamc.plugincontrol.PluginControl;
import com.armamc.plugincontrol.managers.ConfigManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

public class PlayerListener implements Listener {
    private final PluginControl plugin;
    private final ConfigManager config;
    private final Permission bypass;

    public PlayerListener(@NotNull PluginControl plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.bypass = new Permission("plugincontrol.bypass");
    }

    public void init() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        var onlinePlayers = plugin.getServer().getOnlinePlayers();
        for (var player : onlinePlayers) {
            if (player.hasPermission(bypass)) continue;
            player.kickPlayer(config.serialize(config.getKickMessage()));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onPlayerLogin(@NotNull PlayerLoginEvent event) {
        if (event.getPlayer().hasPermission(bypass)) return;
        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, config.serialize(config.getKickMessage()));
    }

}
