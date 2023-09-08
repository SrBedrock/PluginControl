package com.armamc.plugincontrol.commands;

import com.armamc.plugincontrol.PluginControl;
import com.armamc.plugincontrol.managers.ConfigManager;
import com.armamc.plugincontrol.managers.MessageManager;
import com.armamc.plugincontrol.managers.PluginsManager;
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
    private final PluginsManager manager;
    private static final String PLUGIN_TAG = "plugin";
    private static final String COMMAND_TAG = "command";

    public Command(@NotNull PluginControl plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.message = plugin.getMessageManager();
        this.manager = plugin.getPluginsManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("plugincontrol.use")) {
            message.send(sender, message.getNoPermissionError());
            return true;
        }
        if (args.length >= 1) {
            switch (args[0]) {
                case "enable", "on" -> {
                    config.setEnabled(true);
                    message.send(sender, message.getPluginEnabled());
                    return true;
                }
                case "disable", "off" -> {
                    config.setEnabled(false);
                    message.send(sender, message.getPluginDisabled());
                    return true;
                }
                case "toggle" -> {
                    config.setEnabled(!config.isEnabled());
                    boolean enabled = config.isEnabled();
                    if (enabled) {
                        message.send(sender, message.getPluginEnabled());
                    } else {
                        message.send(sender, message.getPluginDisabled());
                    }
                    return true;
                }
                case "add" -> {
                    if (args.length < 2 || args[1].isBlank() || args.length >= 3) {
                        message.send(sender, message.getPluginAddError(), Placeholder.parsed(COMMAND_TAG, label));
                        return true;
                    }
                    if (config.addPlugin(args[1])) {
                        message.send(sender, message.getPluginAdded(), Placeholder.parsed(PLUGIN_TAG, args[1]));
                    } else {
                        message.send(sender, message.getPluginAlreadyAdded(), Placeholder.parsed(PLUGIN_TAG, args[1]));
                    }
                    return true;
                }
                case "remove" -> {
                    if (config.getPluginList().isEmpty()) {
                        message.send(sender, message.getPluginListEmpty());
                        return true;
                    }
                    if (args.length < 2 || args[1].isBlank() || args.length >= 3) {
                        message.send(sender, message.getPluginRemoveError(), Placeholder.parsed(COMMAND_TAG, label));
                        return true;
                    }
                    if (config.removePlugin(args[1])) {
                        message.send(sender, message.getPluginRemoved(), Placeholder.parsed(PLUGIN_TAG, args[1]));
                    } else {
                        message.send(sender, message.getPluginNotFound(), Placeholder.parsed(PLUGIN_TAG, args[1]));
                    }
                    return true;
                }
                case "list" -> {
                    if (config.getPluginList().isEmpty()) {
                        message.send(sender, message.getPluginListEmpty());
                    } else {
                        message.send(sender, message.getPluginList(), Placeholder.component("plugins", message.getPluginListComponent(config.getPluginList())));
                    }
                    return true;
                }
                case "action" -> {
                    if (args.length < 2 || args[1].isBlank() || args.length >= 3) {
                        message.send(sender, message.getActionType(), Placeholder.parsed("action", config.getAction().toLowerCase()));
                        return true;
                    }
                    try {
                        var actiontype = ConfigManager.ActionType.from(args[1].toLowerCase());
                        config.setAction(actiontype);
                        message.send(sender, message.getActionSet(), Placeholder.parsed("action", actiontype.getAction()));
                        manager.checkPlugins();
                    } catch (IllegalArgumentException e) {
                        var actions = List.of("log-to-console", "disallow-player-login", "shutdown-server");
                        message.send(sender, message.getActionTypeList(), Placeholder.parsed("actions", String.join(", ", actions)));
                    }
                    return true;
                }
                case "kick-message", "kickmessage" -> {
                    var kick = "kick-message";
                    if (args.length < 2 || args[1].isBlank()) {
                        message.send(sender, message.getKickMessage(), Placeholder.component(kick, config.deserialize(config.getKickMessage())));
                    } else {
                        config.setKickMessage(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                        message.send(sender, message.getKickMessageSet(), Placeholder.component(kick, config.deserialize(config.getKickMessage())));
                    }
                    return true;
                }
                case "help", "?" -> {
                    message.send(sender, message.getHelpList(), Placeholder.parsed(COMMAND_TAG, label));
                    return true;
                }
                case "reload" -> {
                    reload();
                    message.send(sender, message.getPluginReloaded());
                    return true;
                }
                default -> {
                    message.send(sender, message.getCommandNotFound(), Placeholder.parsed(COMMAND_TAG, label));
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
        manager.unregisterListener();
        plugin.reloadConfig();
        message.reloadLang();
        Bukkit.getScheduler().runTaskLater(plugin, manager::checkPlugins, 20L);
    }

}
