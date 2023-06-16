package com.armamc.plugincontrol.commands;

import com.armamc.plugincontrol.PluginControl;
import com.armamc.plugincontrol.config.Config;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Command implements CommandExecutor, TabCompleter {

    private final BukkitAudiences adventure = PluginControl.getInstance().adventure();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length >= 1) {
            switch (args[0]) {
                case "enable", "on" -> {
                    Config.setEnabled(true);
                    adventure.sender(sender).sendMessage(Component.text("[PluginControl] Activating plugin features...")
                            .color(NamedTextColor.GREEN));
                    return true;
                }
                case "disable", "off" -> {
                    Config.setEnabled(false);
                    adventure.sender(sender).sendMessage(Component.text("[PluginControl] Disabling plugin features...")
                            .color(NamedTextColor.GREEN));
                    return true;
                }
                case "toggle" -> {
                    Config.setEnabled(!Config.isEnabled());
                    boolean enabled = Config.isEnabled();
                    if (enabled) {
                        adventure.sender(sender).sendMessage(Component.text("[PluginControl] Activating plugin features...")
                                .color(NamedTextColor.GREEN));
                    } else {
                        adventure.sender(sender).sendMessage(Component.text("[PluginControl] Disabling plugin features...")
                                .color(NamedTextColor.GREEN));
                    }
                    return true;
                }
                case "add" -> {
                    if (args.length < 2 || args[1].isBlank() || args.length >= 3) {
                        adventure.sender(sender).sendMessage(Component.text("[PluginControl] Use /plugincontrol add <plugin-name>")
                                .color(NamedTextColor.RED));
                        return true;
                    }
                    if (Config.addPlugin(args[1])) {
                        adventure.sender(sender).sendMessage(Component.text(MessageFormat.format("[PluginControl] Plugin {0} added!", args[1]))
                                .color(NamedTextColor.GREEN));
                    } else {
                        adventure.sender(sender).sendMessage(Component.text(MessageFormat.format("[PluginControl] Plugin {0} is already added!", args[1]))
                                .color(NamedTextColor.RED));
                    }
                    return true;
                }
                case "remove" -> {
                    if (Config.getPluginList().isEmpty()) {
                        adventure.sender(sender).sendMessage(Component.text("[PluginControl] No plugins added!").color(NamedTextColor.RED));
                        return true;
                    }
                    if (args.length < 2 || args[1].isBlank() || args.length >= 3) {
                        adventure.sender(sender).sendMessage(Component.text("[PluginControl] Use /plugincontrol remove <plugin-name>")
                                .color(NamedTextColor.RED));
                        return true;
                    }
                    if (Config.removePlugin(args[1])) {
                        adventure.sender(sender).sendMessage(Component.text(MessageFormat.format("[PluginControl] Plugin {0} removed!", args[1]))
                                .color(NamedTextColor.GREEN));
                    } else {
                        adventure.sender(sender).sendMessage(Component.text(MessageFormat.format("[PluginControl] Plugin {0} is not in the list!", args[1]))
                                .color(NamedTextColor.RED));
                    }
                    return true;
                }
                case "list" -> {
                    if (Config.getPluginList().isEmpty()) {
                        adventure.sender(sender).sendMessage(Component.text("[PluginControl] No plugins were added!")
                                .color(NamedTextColor.RED));
                    } else {
                        adventure.sender(sender).sendMessage(Component.text("[PluginControl] Plugins added:")
                                .color(NamedTextColor.YELLOW).appendNewline()
                                .append(Component.text(String.join(", ", Config.getPluginList()))
                                        .color(NamedTextColor.GREEN)));
                    }
                    return true;
                }
                default -> {
                    return false;
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
            List<String> remove = new ArrayList<>(Config.getPluginList());
            StringUtil.copyPartialMatches(args[1], remove, completions);
            return completions;
        } else {
            return Collections.emptyList();
        }

    }

}
