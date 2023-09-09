package com.armamc.plugincontrol.commands.subcommands;

import com.armamc.plugincontrol.PluginControl;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Contract;

import java.util.List;

public class HelpSubCommand implements SubCommand {
    @Contract(pure = true)
    public HelpSubCommand(PluginControl plugin) {

    }

    @Override
    public void execute(CommandSender sender, Command command, String label, String[] args) {

    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        return null;
    }

}
