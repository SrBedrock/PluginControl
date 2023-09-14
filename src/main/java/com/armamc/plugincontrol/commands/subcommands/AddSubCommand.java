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

public class AddSubCommand implements SubCommand {
    private final ConfigManager config;
    private final MessageManager message;
    private static final String PLUGIN_TAG = "plugin";
    private static final String COMMAND_TAG = "command";

    @Contract(pure = true)
    public AddSubCommand(@NotNull PluginControl plugin) {
        this.config = plugin.getConfigManager();
        this.message = plugin.getMessageManager();
    }

    @Override
    public void execute(CommandSender sender, Command command, String label, String @NotNull [] args) {
        if (args.length == 0 || args[0].isBlank()) {
            message.send(sender, message.getPluginAddError(), Placeholder.parsed(COMMAND_TAG, label));
            return;
        }

        var target = args[0];
        if (target.equals("all")) {
            config.addAllPlugins(config.getServerPlugins());
            message.send(sender, message.getAllPluginsAdded(), Placeholder.component(PLUGIN_TAG,
                    message.getPluginListComponent(config.getPluginList())));
            return;
        }

        if (config.addPlugin(target)) {
            message.send(sender, message.getPluginAdded(), Placeholder.parsed(PLUGIN_TAG, target));
        } else {
            message.send(sender, message.getPluginAlreadyAdded(), Placeholder.parsed(PLUGIN_TAG, target));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String label, String @NotNull [] args) {
        if (args.length != 1) return List.of();
        return config.getServerPlugins().stream().filter(s -> s.startsWith(args[0])).toList();
    }


}
