package com.armamc.plugincontrol.listeners;

import com.armamc.plugincontrol.PluginControl;
import com.armamc.plugincontrol.managers.MessageManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

public class PlayerListener implements Listener {
    private final PluginControl plugin;
    private final MessageManager message;
    private final Permission bypass;

    public PlayerListener(@NotNull PluginControl plugin) {
        this.plugin = plugin;
        this.message = plugin.getMessageManager();
        this.bypass = new Permission("plugincontrol.bypass");
    }

    public void init() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        var onlinePlayers = plugin.getServer().getOnlinePlayers();
        for (var player : onlinePlayers) {
            if (player.hasPermission(bypass)) continue;
            player.kickPlayer(message.serialize(message.getKickMessage()));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onPlayerLogin(@NotNull PlayerLoginEvent event) {
        if (event.getPlayer().hasPermission(bypass)) return;
        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, message.serialize(message.getKickMessage()));
    }

}
