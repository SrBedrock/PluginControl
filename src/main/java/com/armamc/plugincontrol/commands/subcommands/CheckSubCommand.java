package com.armamc.plugincontrol.commands.subcommands;

import com.armamc.plugincontrol.PluginControl;
import com.armamc.plugincontrol.managers.PluginsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CheckSubCommand implements SubCommand {
    private final PluginsManager manager;

    @Contract(pure = true)
    public CheckSubCommand(@NotNull PluginControl plugin) {
        this.manager = plugin.getPluginsManager();
    }

    @Override
    public void execute(CommandSender sender, Command command, String label, String @NotNull [] args) {
        manager.checkPlugins();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        return List.of();
    }

}
