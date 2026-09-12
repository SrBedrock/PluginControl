package com.armamc.plugincontrol.commands.subcommands;

import com.armamc.plugincontrol.PluginControl;
import com.armamc.plugincontrol.managers.MessageManager;
import com.armamc.plugincontrol.managers.PluginsManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReloadSubCommand implements SubCommand {
    private final PluginControl plugin;
    private final MessageManager message;
    private final PluginsManager manager;

    @Contract(pure = true)
    public ReloadSubCommand(@NotNull PluginControl plugin) {
        this.plugin = plugin;
        this.message = plugin.getMessageManager();
        this.manager = plugin.getPluginsManager();
    }

    @Override
    public void execute(CommandSender sender, Command command, String label, String[] args) {
        manager.unregisterListener();
        plugin.reloadConfig();
        message.reloadLang();
        Bukkit.getScheduler().runTaskLater(plugin, manager::checkPlugins, 20L);
        message.send(sender, message.getPluginReloaded());
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        return List.of();
    }

}
