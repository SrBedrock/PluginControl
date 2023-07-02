package com.armamc.plugincontrol.listeners;

import com.armamc.plugincontrol.PluginControl;
import com.armamc.plugincontrol.config.Config;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.permissions.Permission;

public class PlayerListener implements Listener {
    private final PluginControl plugin;
    private final Config config;
    private final Permission bypass;

    public PlayerListener(PluginControl plugin) {
        this.plugin = plugin;
        this.config = plugin.getPluginConfig();
        this.bypass = new Permission("plugincontrol.bypass");
    }

    public void init() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getOnlinePlayers().stream()
                .filter(player -> !player.hasPermission(bypass))
                .forEach(player -> player.kickPlayer(config.serialize(config.getKickMessage())));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getPlayer().hasPermission(bypass)) {
            return;
        }
        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, config.serialize(config.getKickMessage()));
    }

}
