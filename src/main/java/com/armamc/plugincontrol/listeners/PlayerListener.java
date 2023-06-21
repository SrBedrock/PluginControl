package com.armamc.plugincontrol.listeners;

import com.armamc.plugincontrol.PluginControl;
import com.armamc.plugincontrol.config.Config;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.permissions.Permission;

public class PlayerListener implements Listener {
    private final PluginControl plugin;
    private final String kickMessage;
    private final Permission bypass;

    public PlayerListener(PluginControl plugin) {
        this.plugin = plugin;
        Config config = plugin.getPluginConfig();
        this.kickMessage = config.parseColor(config.getKickMessage());
        this.bypass = new Permission("plugincontrol.bypass");
    }

    public void init() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getOnlinePlayers().stream()
                .filter(player -> player.hasPermission(bypass))
                .forEach(player -> player.kickPlayer(kickMessage));
    }

    // TODO: Add support for mini-message
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getPlayer().hasPermission(bypass)) {
            return;
        }
        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, kickMessage);
    }
}
