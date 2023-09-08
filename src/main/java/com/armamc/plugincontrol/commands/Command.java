package com.armamc.plugincontrol.commands;

import com.armamc.plugincontrol.PluginControl;
import com.armamc.plugincontrol.config.ConfigManager;
import com.armamc.plugincontrol.config.MessageManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Command implements CommandExecutor, TabCompleter {
    private final PluginControl plugin;
    private final ConfigManager config;
    private final MessageManager message;
    private static final String PLUGIN_TAG = "plugin";
    private static final String COMMAND_TAG = "command";

    public Command(@NotNull PluginControl plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.message = plugin.getMessageManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("plugincontrol.use")) {
            plugin.send(sender, message.getNoPermissionError());
            return true;
        }
        if (args.length >= 1) {
            switch (args[0]) {
                case "enable", "on" -> {
                    config.setEnabled(true);
                    plugin.send(sender, message.getPluginEnabled());
                    return true;
                }
                case "disable", "off" -> {
                    config.setEnabled(false);
                    plugin.send(sender, message.getPluginDisabled());
                    return true;
                }
                case "toggle" -> {
                    config.setEnabled(!config.isEnabled());
                    boolean enabled = config.isEnabled();
                    if (enabled) {
                        plugin.send(sender, message.getPluginEnabled());
                    } else {
                        plugin.send(sender, message.getPluginDisabled());
                    }
                    return true;
                }
                case "add" -> {
                    if (args.length < 2 || args[1].isBlank() || args.length >= 3) {
                        plugin.send(sender, message.getPluginAddError(), Placeholder.parsed(COMMAND_TAG, label));
                        return true;
                    }
                    if (config.addPlugin(args[1])) {
                        plugin.send(sender, message.getPluginAdded(), Placeholder.parsed(PLUGIN_TAG, args[1]));
                    } else {
                        plugin.send(sender, message.getPluginAlreadyAdded(), Placeholder.parsed(PLUGIN_TAG, args[1]));
                    }
                    return true;
                }
                case "remove" -> {
                    if (config.getPluginList().isEmpty()) {
                        plugin.send(sender, message.getPluginListEmpty());
                        return true;
                    }
                    if (args.length < 2 || args[1].isBlank() || args.length >= 3) {
                        plugin.send(sender, message.getPluginRemoveError(), Placeholder.parsed(COMMAND_TAG, label));
                        return true;
                    }
                    if (config.removePlugin(args[1])) {
                        plugin.send(sender, message.getPluginRemoved(), Placeholder.parsed(PLUGIN_TAG, args[1]));
                    } else {
                        plugin.send(sender, message.getPluginNotFound(), Placeholder.parsed(PLUGIN_TAG, args[1]));
                    }
                    return true;
                }
                case "list" -> {
                    if (config.getPluginList().isEmpty()) {
                        plugin.send(sender, message.getPluginListEmpty());
                    } else {
                        plugin.send(sender, message.getPluginList(), Placeholder.component("plugins", plugin.getPluginListComponent(config.getPluginList())));
                    }
                    return true;
                }
                case "action" -> {
                    if (args.length < 2 || args[1].isBlank() || args.length >= 3) {
                        plugin.send(sender, message.getActionType(), Placeholder.parsed("action", config.getAction().toLowerCase()));
                        return true;
                    }
                    try {
                        var actiontype = ConfigManager.ActionType.valueOf(args[1].toUpperCase());
                        config.setAction(actiontype);
                        plugin.send(sender, message.getActionSet(), Placeholder.parsed("action", config.getAction().toLowerCase()));
                        plugin.checkPlugins();
                    } catch (IllegalArgumentException e) {
                        var actions = List.of("log-to-console", "disallow-player-login", "shutdown-server");
                        plugin.send(sender, message.getActionTypeList(), Placeholder.parsed("actions", String.join(", ", actions)));
                        return true;
                    }
                    return true;
                }
                case "kick-message", "kickmessage" -> {
                    var kick = "kick-message";
                    if (args.length < 2 || args[1].isBlank()) {
                        plugin.send(sender, message.getKickMessage(), Placeholder.component(kick, config.deserialize(config.getKickMessage())));
                    } else {
                        config.setKickMessage(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                        plugin.send(sender, message.getKickMessageSet(), Placeholder.component(kick, config.deserialize(config.getKickMessage())));
                    }
                    return true;
                }
                case "help", "?" -> {
                    plugin.send(sender, message.getHelpList(), Placeholder.parsed(COMMAND_TAG, label));
                    return true;
                }
                case "reload" -> {
                    reload();
                    plugin.send(sender, message.getPluginReloaded());
                    return true;
                }
                default -> {
                    plugin.send(sender, message.getCommandNotFound(), Placeholder.parsed(COMMAND_TAG, label));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            var subCommands = List.of("enable", "disable", "toggle", "add", "remove", "list", "action", "kick-message", "reload", "help");
            return subCommands.stream().filter(s -> s.startsWith(args[0])).toList();
        }
        if (args.length == 2 && (args[0].equals("add"))) {
            var add = Arrays.stream(Bukkit.getPluginManager().getPlugins()).toList().stream().map(Plugin::getName).toList();
            return add.stream().filter(s -> s.startsWith(args[1])).toList();
        }
        if (args.length == 2 && (args[0].equals("remove"))) {
            var remove = new ArrayList<>(config.getPluginList());
            return remove.stream().filter(s -> s.startsWith(args[1])).toList();
        }
        if (args.length == 2 && (args[0].equals("action"))) {
            var actions = List.of("log-to-console", "disallow-player-login", "shutdown-server");
            return actions.stream().filter(s -> s.startsWith(args[1])).toList();
        } else {
            return List.of();
        }
    }

    private void reload() {
        plugin.unregisterListener();
        plugin.reloadConfig();
        message.reloadLang();
        Bukkit.getScheduler().runTaskLater(plugin, plugin::checkPlugins, 20L);
    }

}
