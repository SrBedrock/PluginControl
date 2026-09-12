package com.armamc.plugincontrol.commands.subcommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public interface SubCommand {
    void execute(CommandSender sender, Command command, String label, String[] args);

    List<String> tabComplete(CommandSender sender, Command command, String label, String[] args);
}
