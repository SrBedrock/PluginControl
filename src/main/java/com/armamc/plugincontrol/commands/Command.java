package com.armamc.plugincontrol.commands;

import com.armamc.plugincontrol.PluginControl;
import com.armamc.plugincontrol.config.Config;
import com.armamc.plugincontrol.config.Lang;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Command implements CommandExecutor, TabCompleter {
    private final PluginControl plugin;
    private final Config config;
    private final Lang lang;
    private static final String PLUGIN_TAG = "plugin";
    private static final String COMMAND_TAG = "command";

    public Command(PluginControl plugin) {
        this.plugin = plugin;
        this.config = plugin.getPluginConfig();
        this.lang = plugin.getPluginLang();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("plugincontrol.use")) {
            plugin.send(sender, lang.message("command.no-permission-error"), null);
            return true;
        }
        if (args.length >= 1) {
            switch (args[0]) {
                case "enable", "on" -> {
                    config.setEnabled(true);
                    plugin.send(sender, lang.message("command.plugin-enabled"), null);
                    return true;
                }
                case "disable", "off" -> {
                    config.setEnabled(false);
                    plugin.send(sender, lang.message("command.plugin-disabled"), null);
                    return true;
                }
                case "toggle" -> {
                    config.setEnabled(!config.isEnabled());
                    boolean enabled = config.isEnabled();
                    if (enabled) {
                        plugin.send(sender, lang.message("command.plugin-enabled"), null);
                    } else {
                        plugin.send(sender, lang.message("command.plugin-disabled"), null);
                    }
                    return true;
                }
                case "add" -> {
                    if (args.length < 2 || args[1].isBlank() || args.length >= 3) {
                        plugin.send(sender, lang.message("command.plugin-add-error"),
                                Placeholder.parsed(COMMAND_TAG, label));
                        return true;
                    }
                    if (config.addPlugin(args[1])) {
                        plugin.send(sender, lang.message("command.plugin-added"),
                                Placeholder.parsed(PLUGIN_TAG, args[1]));
                    } else {
                        plugin.send(sender, lang.message("command.plugin-already-added"),
                                Placeholder.parsed(PLUGIN_TAG, args[1]));
                    }
                    return true;
                }
                case "remove" -> {
                    if (config.getPluginList().isEmpty()) {
                        plugin.send(sender, lang.message("command.plugin-list-empty"), null);
                        return true;
                    }
                    if (args.length < 2 || args[1].isBlank() || args.length >= 3) {
                        plugin.send(sender, lang.message("command.plugin-remove-error"),
                                Placeholder.parsed(COMMAND_TAG, label));
                        return true;
                    }
                    if (config.removePlugin(args[1])) {
                        plugin.send(sender, lang.message("command.plugin-removed"),
                                Placeholder.parsed(PLUGIN_TAG, args[1]));
                    } else {
                        plugin.send(sender, lang.message("command.plugin-not-found"),
                                Placeholder.parsed(PLUGIN_TAG, args[1]));
                    }
                    return true;
                }
                case "list" -> {
                    if (config.getPluginList().isEmpty()) {
                        plugin.send(sender, lang.message("command.plugin-list-empty"), null);
                    } else {
                        plugin.send(sender, lang.message("command.plugin-list"),
                                Placeholder.parsed("plugins",
                                        String.join(", ", config.getPluginList())));
                    }
                    return true;
                }
                case "action" -> {
                    if (args.length < 2 || args[1].isBlank() || args.length >= 3) {
                        plugin.send(sender, lang.message("command.action-type"),
                                Placeholder.parsed("action", config.getAction().toLowerCase()));
                        return true;
                    }
                    List<String> actions = new ArrayList<>(List.of("log-to-console", "disallow-player-login", "shutdown-server"));
                    if (actions.contains(args[1])) {
                        config.setAction(args[1]);
                        plugin.send(sender, lang.message("command.action-set"),
                                Placeholder.parsed("action", config.getAction().toLowerCase()));
                    } else {
                        plugin.send(sender, lang.message("command.action-list"),
                                Placeholder.parsed("actions", String.join(", ", actions)));
                    }
                    return true;
                }
                case "kick-message" -> {
                    if (args.length < 2 || args[1].isBlank()) {
                        plugin.send(sender, lang.message("command.kick-message"),
                                Placeholder.parsed("kick-message", config.getKickMessage()));
                    } else {
                        config.setKickMessage(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                        plugin.send(sender, lang.message("command.kick-message-set"),
                                Placeholder.parsed("kick-message", config.getKickMessage()));
                    }
                    return true;
                }
                case "reload" -> {
                    reload();
                    plugin.send(sender, lang.message("command.plugin-reload"), null);
                    return true;
                }
                default -> {
                    plugin.send(sender, lang.message("command.command-not-found"),
                            Placeholder.parsed(COMMAND_TAG, label));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {

        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("enable", "disable", "toggle", "add", "remove", "list", "action", "kick-message", "reload");
            StringUtil.copyPartialMatches(args[0], subCommands, completions);
            return completions;
        }
        if (args.length == 2 && (args[0].equals("add"))) {
            List<String> add = Arrays.stream(Bukkit.getPluginManager().getPlugins())
                    .toList().stream().map(Plugin::getName).toList();
            StringUtil.copyPartialMatches(args[1], add, completions);
            return completions;
        }
        if (args.length == 2 && (args[0].equals("remove"))) {
            List<String> remove = new ArrayList<>(config.getPluginList());
            StringUtil.copyPartialMatches(args[1], remove, completions);
            return completions;
        }
        if (args.length == 2 && (args[0].equals("action"))) {
            List<String> actions = new ArrayList<>(List.of("log-to-console", "disallow-player-login", "shutdown-server"));
            StringUtil.copyPartialMatches(args[1], actions, completions);
            return completions;
        } else {
            return Collections.emptyList();
        }
    }

    private void reload() {
        config.saveConfig();
        plugin.unregisterListener();
        lang.reload();
        Bukkit.getScheduler().runTaskLater(plugin, plugin::checkPlugins, 20L);
    }

}
