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
    private final ConfigManager configManager;
    private final MessageManager messageManager;
    private PlayerListener playerListener;
    private final ConsoleCommandSender console = Bukkit.getConsoleSender();

    public PluginsManager(@NotNull PluginControl plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.messageManager = plugin.getMessageManager();
    }

    public void checkPlugins() {
        if (!configManager.isEnabled()) return;

        messageManager.send(console, messageManager.getCheckingMessage());

        var missingPlugins = new HashSet<String>();
        for (var pluginName : configManager.getPluginList()) {
            if (!isPluginEnabled(pluginName)) {
                missingPlugins.add(pluginName);
            }
        }

        var pluginGroup = configManager.getPluginGroups();
        for (var plugins : pluginGroup.entrySet()) {
            boolean groupHasEnabledPlugin = false;
            for (var pl : plugins.getValue()) {
                if (isPluginEnabled(pl)) {
                    groupHasEnabledPlugin = true;
                    break;
                }
            }

            if (!groupHasEnabledPlugin) {
                // TODO: use lang.yml

                // [23:35:07 INFO]: [PluginControl] Checking plugins...
                // [23:35:07 INFO]: [PluginControl] Plugin No plugins from the group <Hologram> found and No plugins from the group <Economy> found not found or enabled successfully...
                missingPlugins.add("No plugins from the group <" + plugins.getKey() + "> found");
            }
        }

        if (!missingPlugins.isEmpty()) {
            registerAction(missingPlugins);
        } else {
            messageManager.send(console, messageManager.getCheckFinished());
        }
    }

    private boolean isPluginEnabled(String pluginName) {
        var pl = plugin.getServer().getPluginManager().getPlugin(pluginName);
        return pl != null && pl.isEnabled();
    }

    private void registerAction(Set<String> missingPlugins) {
        var tag = Placeholder.component("plugins", messageManager.getPluginListComponent(new HashSet<>(missingPlugins)));
        if (configManager.getAction().equalsIgnoreCase(ConfigManager.ActionType.DISALLOW_PLAYER_LOGIN.getAction())) {
            playerListener = new PlayerListener(plugin);
            playerListener.init();
            messageManager.send(console, messageManager.getLogToConsole(), tag);
            return;
        }
        if (configManager.getAction().equalsIgnoreCase(ConfigManager.ActionType.LOG_TO_CONSOLE.getAction())) {
            messageManager.send(console, messageManager.getLogToConsole(), tag);
            return;
        }
        if (configManager.getAction().equalsIgnoreCase(ConfigManager.ActionType.SHUTDOWN_SERVER.getAction())) {
            messageManager.send(console, messageManager.getDisablingServer(), tag);
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
