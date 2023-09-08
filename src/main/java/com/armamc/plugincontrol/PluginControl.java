package com.armamc.plugincontrol;

import com.armamc.plugincontrol.commands.Command;
import com.armamc.plugincontrol.config.ConfigManager;
import com.armamc.plugincontrol.config.MessageManager;
import com.armamc.plugincontrol.listeners.PlayerListener;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PluginControl extends JavaPlugin {
    private final ConsoleCommandSender console = Bukkit.getConsoleSender();
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private BukkitAudiences adventure;
    private PlayerListener playerListener;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private static final String PREFIX = "prefix";

    @Override
    public void onEnable() {
        this.adventure = BukkitAudiences.create(this);
        registerConfig();
        registerCommands();
        registerTask();
    }

    @Override
    public void onDisable() {
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    @Contract(pure = true)
    public ConfigManager getConfigManager() {
        return configManager;
    }

    @Contract(pure = true)
    public MessageManager getMessageManager() {
        return messageManager;
    }

    public void unregisterListener() {
        if (playerListener != null) {
            PlayerLoginEvent.getHandlerList().unregister(this);
            playerListener = null;
        }
    }

    private void registerConfig() {
        if (!getDataFolder().exists() && getDataFolder().mkdir()) {
            getLogger().info("Creating the plugin folder!");
        }
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
    }

    private void registerCommands() {
        var command = new Command(this);
        var pluginCommand = getCommand("plugincontrol");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }
    }

    private void registerTask() {
        Bukkit.getScheduler().runTaskLater(this, this::checkPlugins, 20L);
    }

    public void checkPlugins() {
        // If the plugin is disabled, don't check for plugins
        if (!configManager.isEnabled()) return;

        // Send a checking message to console
        send(console, messageManager.getCheckingMessage());

        // Create a list of missing plugins
        var missingPlugins = new HashSet<String>();

        // Loop through the plugin list
        for (String plugin : configManager.getPluginList()) {
            // Check if the plugin is missing
            if (!isPluginEnabled(plugin)) {
                missingPlugins.add(plugin);
            }
        }

        // If there are missing plugins, register the action
        if (!missingPlugins.isEmpty()) {
            registerAction(missingPlugins);
        } else {
            send(console, messageManager.getCheckFinished());
        }
    }

    private boolean isPluginEnabled(String pluginName) {
        return getServer().getPluginManager().getPlugin(pluginName) != null;
    }

    private void registerAction(Set<String> missingPlugins) {
        var tag = Placeholder.component("plugins", getPluginListComponent(new ArrayList<>(missingPlugins)));
        if (configManager.getAction().equalsIgnoreCase(ConfigManager.ActionType.DISALLOW_PLAYER_LOGIN.toString())) {
            playerListener = new PlayerListener(this);
            playerListener.init();
            send(console, messageManager.getLogToConsole(), tag);
            return;
        }
        if (configManager.getAction().equalsIgnoreCase(ConfigManager.ActionType.LOG_TO_CONSOLE.toString())) {
            send(console, messageManager.getLogToConsole(), tag);
            return;
        }
        if (configManager.getAction().equalsIgnoreCase(ConfigManager.ActionType.SHUTDOWN_SERVER.toString())) {
            send(console, messageManager.getDisablingServer(), tag);
            getServer().shutdown();
        }
    }

    @Contract(pure = true)
    public @NonNull BukkitAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    public void send(@NotNull CommandSender sender, @NotNull String message) {
        if (message.isEmpty() || message.isBlank()) return;
        adventure().sender(sender).sendMessage(MM.deserialize(message, Placeholder.parsed(PREFIX, messageManager.getPrefix())));
    }

    public void send(@NotNull CommandSender sender, @NotNull String message, @NotNull TagResolver tag) {
        if (message.isEmpty() || message.isBlank()) return;
        adventure().sender(sender).sendMessage(MM.deserialize(message, Placeholder.parsed(PREFIX, messageManager.getPrefix()), tag));
    }

    public void send(@NotNull CommandSender sender, @NotNull List<String> message, @NotNull TagResolver tag) {
        if (message.isEmpty()) return;
        for (var line : message) {
            if (line.isEmpty()) continue;
            adventure().sender(sender).sendMessage(MM.deserialize(line, Placeholder.parsed(PREFIX, messageManager.getPrefix()), tag));
        }
    }

    public @NotNull Component getPluginListComponent(@NotNull List<String> pluginList) {
        var joinConfiguration = JoinConfiguration.separators(
                MM.deserialize(messageManager.getPluginListSeparator()),
                MM.deserialize(messageManager.getPluginListSeparatorLast()));

        var componentList = new ArrayList<Component>();
        var command = "/plugincontrol add %s";
        for (var pluginName : pluginList) {
            componentList.add(Component.text(pluginName)
                    .hoverEvent(HoverEvent.showText(MM.deserialize(messageManager.getPluginClickAdd())))
                    .clickEvent(ClickEvent.runCommand(command.formatted(pluginName))));
        }

        return Component.join(joinConfiguration, componentList);
    }

}
