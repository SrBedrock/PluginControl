package com.armamc.plugincontrol.commands.subcommands;

import com.armamc.plugincontrol.PluginControl;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Contract;

import java.util.List;

public class ActionSubCommand implements SubCommand {
    @Contract(pure = true)
    public ActionSubCommand(PluginControl plugin) {

    }

    @Override
    public void execute(CommandSender sender, Command command, String label, String[] args) {

    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        var actions = List.of("log-to-console", "disallow-player-login", "shutdown-server");
        return actions.stream().filter(s -> s.startsWith(args[1])).toList();
    }

}
