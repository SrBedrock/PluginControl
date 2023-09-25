package com.armamc.plugincontrol.managers;

import com.armamc.plugincontrol.PluginControl;
import com.armamc.plugincontrol.listeners.PlayerListener;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
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
            if (!plugin.isPluginEnabled(pluginName)) {
                missingPlugins.add(pluginName);
            }
        }

        var missingGroups = new HashSet<String>();
        var pluginGroup = config.getPluginGroups();
        for (var groups : pluginGroup.entrySet()) {
            boolean groupHasEnabledPlugin = false;
            if (groups.getValue().isEmpty()) continue;
            for (var pluginName : groups.getValue()) {
                if (plugin.isPluginEnabled(pluginName)) {
                    groupHasEnabledPlugin = true;
                    break;
                }
            }

            if (!groupHasEnabledPlugin) {
                missingGroups.add(groups.getKey());
            }
        }

        if (!missingPlugins.isEmpty() || !missingGroups.isEmpty()) {
            registerAction(missingPlugins, missingGroups);
        } else {
            message.send(console, message.getCheckFinished());
        }
    }

    private void registerAction(@NotNull Set<String> missingPlugins, @NotNull Set<String> missingGroups) {
        TagResolver.Single pluginTag = null;
        TagResolver.Single groupsTag = null;
        if (!missingPlugins.isEmpty()) {
            pluginTag = Placeholder.component("plugins", message.getPluginListComponent(new HashSet<>(missingPlugins)));
        }
        if (!missingGroups.isEmpty()) {
            groupsTag = Placeholder.component("groups", message.getGroupListComponent(new HashSet<>(missingGroups)));
        }

        if (config.getAction().equalsIgnoreCase(ConfigManager.ActionType.DISALLOW_PLAYER_LOGIN.getAction())) {
            if (playerListener == null) {
                playerListener = new PlayerListener(plugin);
                playerListener.init();
            }
            if (pluginTag != null) {
                message.send(console, message.getLogToConsole(), pluginTag);
            }
            if (groupsTag != null) {
                message.send(console, message.getLogToConsoleGroup(), groupsTag);
            }
            return;
        }

        if (config.getAction().equalsIgnoreCase(ConfigManager.ActionType.LOG_TO_CONSOLE.getAction())) {
            if (pluginTag != null) {
                message.send(console, message.getLogToConsole(), pluginTag);
            }
            if (groupsTag != null) {
                message.send(console, message.getLogToConsoleGroup(), groupsTag);
            }
            return;
        }
        if (config.getAction().equalsIgnoreCase(ConfigManager.ActionType.SHUTDOWN_SERVER.getAction())) {
            if (pluginTag != null) {
                message.send(console, message.getLogToConsole(), pluginTag);
            }
            if (groupsTag != null) {
                message.send(console, message.getLogToConsoleGroup(), groupsTag);
            }

            message.send(console, message.getDisablingServer());
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
