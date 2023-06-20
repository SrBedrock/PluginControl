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
            plugin.sendToPlayer(sender, lang.message("no-permission-error"));
            return true;
        }
        if (args.length >= 1) {
            switch (args[0]) {
                case "enable", "on" -> {
                    config.setEnabled(true);
                    plugin.sendToPlayer(sender, lang.message("plugin-enabled"));
                    return true;
                }
                case "disable", "off" -> {
                    config.setEnabled(false);
                    plugin.sendToPlayer(sender, lang.message("plugin-disabled"));
                    return true;
                }
                case "toggle" -> {
                    config.setEnabled(!config.isEnabled());
                    boolean enabled = config.isEnabled();
                    if (enabled) {
                        plugin.sendToPlayer(sender, lang.message("plugin-enabled"));
                    } else {
                        plugin.sendToPlayer(sender, lang.message("plugin-disabled"));
                    }
                    return true;
                }
                case "add" -> {
                    if (args.length < 2 || args[1].isBlank() || args.length >= 3) {
                        plugin.sendToPlayer(sender, lang.message("plugin-add-error"),
                                Placeholder.parsed(COMMAND_TAG, label));
                        return true;
                    }
                    if (config.addPlugin(args[1])) {
                        plugin.sendToPlayer(sender, lang.message("plugin-added"),
                                Placeholder.parsed(PLUGIN_TAG, args[1]));
                    } else {
                        plugin.sendToPlayer(sender, lang.message("plugin-already-added"),
                                Placeholder.parsed(PLUGIN_TAG, args[1]));
                    }
                    return true;
                }
                case "remove" -> {
                    if (config.getPluginList().isEmpty()) {
                        plugin.sendToPlayer(sender, lang.message("plugin-list-empty"));
                        return true;
                    }
                    if (args.length < 2 || args[1].isBlank() || args.length >= 3) {
                        plugin.sendToPlayer(sender, lang.message("plugin-remove-error"),
                                Placeholder.parsed(COMMAND_TAG, label));
                        return true;
                    }
                    if (config.removePlugin(args[1])) {
                        plugin.sendToPlayer(sender, lang.message("plugin-removed"),
                                Placeholder.parsed(PLUGIN_TAG, args[1]));
                    } else {
                        plugin.sendToPlayer(sender, lang.message("plugin-not-found"),
                                Placeholder.parsed(PLUGIN_TAG, args[1]));
                    }
                    return true;
                }
                case "list" -> {
                    if (config.getPluginList().isEmpty()) {
                        plugin.sendToPlayer(sender, lang.message("plugin-list-empty"));
                    } else {
                        plugin.sendToPlayer(sender, lang.message("plugin-list"),
                                Placeholder.parsed("plugins",
                                        String.join(", ", config.getPluginList())));
                    }
                    return true;
                }
                case "reload" -> {
                    plugin.reloadConfig();
                    lang.reloadLang();
                    plugin.checkPlugins();
                    plugin.sendToPlayer(sender, lang.message("plugin-reload"));
                    return true;
                }
                default -> {
                    plugin.sendToPlayer(sender, lang.message("command-not-found"));
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
            List<String> subCommands = Arrays.asList("enable", "disable", "toggle", "add", "remove", "list");
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
        } else {
            return Collections.emptyList();
        }

    }

}
