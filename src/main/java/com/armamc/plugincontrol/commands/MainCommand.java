package com.armamc.plugincontrol.commands;

import com.armamc.plugincontrol.PluginControl;
import com.armamc.plugincontrol.commands.subcommands.ActionSubCommand;
import com.armamc.plugincontrol.commands.subcommands.AddSubCommand;
import com.armamc.plugincontrol.commands.subcommands.CheckSubCommand;
import com.armamc.plugincontrol.commands.subcommands.DisableSubCommand;
import com.armamc.plugincontrol.commands.subcommands.EnableSubCommand;
import com.armamc.plugincontrol.commands.subcommands.GroupSubCommand;
import com.armamc.plugincontrol.commands.subcommands.HelpSubCommand;
import com.armamc.plugincontrol.commands.subcommands.KickMessageSubCommand;
import com.armamc.plugincontrol.commands.subcommands.ListSubCommand;
import com.armamc.plugincontrol.commands.subcommands.ReloadSubCommand;
import com.armamc.plugincontrol.commands.subcommands.RemoveSubCommand;
import com.armamc.plugincontrol.commands.subcommands.SubCommand;
import com.armamc.plugincontrol.commands.subcommands.ToggleSubCommand;
import com.armamc.plugincontrol.managers.MessageManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainCommand implements CommandExecutor, TabCompleter {
    private final MessageManager message;
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public MainCommand(@NotNull PluginControl plugin) {
        this.message = plugin.getMessageManager();
        subCommands.put("enable", new EnableSubCommand(plugin));
        subCommands.put("disable", new DisableSubCommand(plugin));
        subCommands.put("check", new CheckSubCommand(plugin));
        subCommands.put("toggle", new ToggleSubCommand(plugin));
        subCommands.put("add", new AddSubCommand(plugin));
        subCommands.put("remove", new RemoveSubCommand(plugin));
        subCommands.put("list", new ListSubCommand(plugin));
        subCommands.put("action", new ActionSubCommand(plugin));
        subCommands.put("kick-message", new KickMessageSubCommand(plugin));
        subCommands.put("help", new HelpSubCommand(plugin));
        subCommands.put("reload", new ReloadSubCommand(plugin));
        subCommands.put("group", new GroupSubCommand(plugin));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            message.send(sender, message.getHelpList(), Placeholder.parsed("command", label));
            return true;
        }

        var subCommand = subCommands.get(args[0].toLowerCase());
        if (subCommand != null) {
            subCommand.execute(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
        } else {
            message.send(sender, message.getHelpList(), Placeholder.parsed("command", label));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            var commands = subCommands.keySet();
            return commands.stream().filter(s -> s.startsWith(args[0])).toList();
        } else if (args.length >= 2) {
            var subCommand = subCommands.get(args[0].toLowerCase());
            if (subCommand != null) {
                return subCommand.tabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
            }
        }

        return List.of();
    }

}
