package com.armamc.plugincontrol.commands.subcommands;

import com.armamc.plugincontrol.PluginControl;
import com.armamc.plugincontrol.managers.ConfigManager;
import com.armamc.plugincontrol.managers.MessageManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ListSubCommand implements SubCommand {
    private final ConfigManager config;
    private final MessageManager message;

    @Contract(pure = true)
    public ListSubCommand(@NotNull PluginControl plugin) {
        this.config = plugin.getConfigManager();
        this.message = plugin.getMessageManager();
    }

    @Override
    public void execute(CommandSender sender, Command command, String label, String[] args) {
        if (config.getPluginList().isEmpty()) {
            message.send(sender, message.getPluginListEmpty());
        } else {
            message.send(sender, message.getPluginList(), Placeholder.component("plugins", message.getPluginListComponent(config.getPluginList())));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        return List.of();
    }

}
