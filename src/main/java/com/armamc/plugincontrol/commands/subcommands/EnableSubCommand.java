package com.armamc.plugincontrol.commands.subcommands;

import com.armamc.plugincontrol.PluginControl;
import com.armamc.plugincontrol.managers.ConfigManager;
import com.armamc.plugincontrol.managers.MessageManager;
import com.armamc.plugincontrol.managers.PluginsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EnableSubCommand implements SubCommand {
    private final ConfigManager config;
    private final MessageManager message;
    private final PluginsManager manager;

    @Contract(pure = true)
    public EnableSubCommand(@NotNull PluginControl plugin) {
        this.config = plugin.getConfigManager();
        this.message = plugin.getMessageManager();
        this.manager = plugin.getPluginsManager();
    }

    @Override
    public void execute(CommandSender sender, Command command, String label, String[] args) {
        config.setEnabled(true);
        manager.checkPlugins();
        message.send(sender, message.getPluginEnabled());
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        return List.of();
    }

}
