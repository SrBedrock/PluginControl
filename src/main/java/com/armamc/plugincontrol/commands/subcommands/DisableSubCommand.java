package com.armamc.plugincontrol.commands.subcommands;

import com.armamc.plugincontrol.PluginControl;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class DisableSubCommand implements SubCommand {
    private final PluginControl plugin;
    public DisableSubCommand(PluginControl plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, Command command, String label, String[] args) {

    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        return null;
    }

}
