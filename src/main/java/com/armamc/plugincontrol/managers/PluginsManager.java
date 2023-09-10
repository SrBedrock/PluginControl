package com.armamc.plugincontrol.managers;

import com.armamc.plugincontrol.PluginControl;
import com.armamc.plugincontrol.listeners.PlayerListener;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class PluginsManager {
    private final PluginControl plugin;
    private final ConfigManager config;
    private final MessageManager message;
    private PlayerListener playerListener;
    private final ConsoleCommandSender console = Bukkit.getConsoleSender();

    public PluginsManager(@NotNull PluginControl plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.message = plugin.getMessageManager();
    }

    public void checkPlugins() {
        if (!config.isEnabled()) return;

        message.send(console, message.getCheckingMessage());

        var missingPlugins = new HashSet<String>();
        for (var pluginName : config.getPluginList()) {
            if (!isPluginEnabled(pluginName)) {
                missingPlugins.add(pluginName);
            }
        }

        var pluginGroup = config.getPluginGroups();
        for (var groups : pluginGroup.entrySet()) {
            boolean groupHasEnabledPlugin = false;
            for (var pluginName : groups.getValue()) {
                if (isPluginEnabled(pluginName)) {
                    groupHasEnabledPlugin = true;
                    break;
                }
            }

            if (!groupHasEnabledPlugin) {
                missingPlugins.add(message.getLogToConsoleGroup().replace("<group>", groups.getKey()));
            }
        }

        if (!missingPlugins.isEmpty()) {
            registerAction(missingPlugins);
        } else {
            message.send(console, message.getCheckFinished());
        }
    }

    private boolean isPluginEnabled(String pluginName) {
        var pl = plugin.getServer().getPluginManager().getPlugin(pluginName);
        return pl != null && pl.isEnabled();
    }

    private void registerAction(Set<String> missingPlugins) {
        var tag = Placeholder.component("plugins", message.getPluginListComponent(new HashSet<>(missingPlugins)));
        if (config.getAction().equalsIgnoreCase(ConfigManager.ActionType.DISALLOW_PLAYER_LOGIN.getAction())) {
            playerListener = new PlayerListener(plugin);
            playerListener.init();
            message.send(console, message.getLogToConsole(), tag);
            return;
        }
        if (config.getAction().equalsIgnoreCase(ConfigManager.ActionType.LOG_TO_CONSOLE.getAction())) {
            message.send(console, message.getLogToConsole(), tag);
            return;
        }
        if (config.getAction().equalsIgnoreCase(ConfigManager.ActionType.SHUTDOWN_SERVER.getAction())) {
            message.send(console, message.getDisablingServer(), tag);
            plugin.getServer().shutdown();
        }
    }

    public void unregisterListener() {
        if (playerListener != null) {
            PlayerLoginEvent.getHandlerList().unregister(plugin);
            playerListener = null;
        }
    }
}
