package com.armamc.plugincontrol.managers;

import com.armamc.plugincontrol.PluginControl;
import com.armamc.plugincontrol.core.ActionType;
import com.armamc.plugincontrol.core.RuleEvaluator;
import com.armamc.plugincontrol.listeners.PlayerListener;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static com.armamc.plugincontrol.Placeholders.GROUPS;
import static com.armamc.plugincontrol.Placeholders.PLUGINS;

public class PluginsManager {
    private final PluginControl plugin;
    private final ConfigManager config;
    private final MessageManager message;
    private PlayerListener playerListener;

    public PluginsManager(@NotNull PluginControl plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.message = plugin.getMessageManager();
    }

    public void checkPlugins() {
        if (!config.isEnabled()) {
            unregisterListener();
            message.send(message.getCheckingDisabled());
            return;
        }

        message.send(message.getCheckingMessage());

        final RuleEvaluator.Result result = RuleEvaluator.evaluate(
                config.getPluginList(), config.getPluginGroups(), plugin::isPluginEnabled);
        final Set<String> missingPlugins = result.missingPlugins();
        final Set<String> missingGroups = result.missingGroups();

        if (!missingPlugins.isEmpty() || !missingGroups.isEmpty()) {
            registerAction(missingPlugins, missingGroups);
        } else {
            unregisterListener();
            message.send(message.getCheckFinished());
        }
    }

    private void registerAction(@NotNull Set<String> missingPlugins, @NotNull Set<String> missingGroups) {
        TagResolver.Single pluginTag = null;
        if (!missingPlugins.isEmpty()) {
            pluginTag = Placeholder.component(PLUGINS, message.getPluginListComponent(missingPlugins));
        }

        TagResolver.Single groupTag = null;
        if (!missingGroups.isEmpty()) {
            groupTag = Placeholder.component(GROUPS, message.getGroupListComponent(missingGroups));
        }

        switch (ActionType.parse(config.getAction())) {
            case DISALLOW_PLAYER_LOGIN -> handleDisallowPlayerLogin(pluginTag, groupTag);
            case LOG_TO_CONSOLE -> logToConsole(pluginTag, groupTag);
            case SHUTDOWN_SERVER -> shutdownServer(pluginTag, groupTag);
            default -> throw new IllegalArgumentException("Unknown action: %s".formatted(config.getAction()));
        }
    }

    private void handleDisallowPlayerLogin(TagResolver.Single pluginTag, TagResolver.Single groupTag) {
        if (playerListener == null) {
            playerListener = new PlayerListener(plugin);
            playerListener.init();
        }

        logToConsole(pluginTag, groupTag);
    }

    private void shutdownServer(TagResolver.Single pluginTag, TagResolver.Single groupTag) {
        logToConsole(pluginTag, groupTag);
        message.send(message.getDisablingServer());
        plugin.getServer().shutdown();
    }

    private void logToConsole(TagResolver.Single pluginTag, TagResolver.Single groupTag) {
        if (pluginTag != null) {
            message.send(message.getLogToConsolePlugin(), pluginTag);
        }
        if (groupTag != null) {
            message.send(message.getLogToConsoleGroup(), groupTag);
        }

        message.send(message.getCheckFinished());
    }

    public void unregisterListener() {
        if (playerListener != null) {
            PlayerLoginEvent.getHandlerList().unregister(plugin);
            playerListener = null;
        }
    }
}
