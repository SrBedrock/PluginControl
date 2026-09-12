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

import static com.armamc.plugincontrol.Placeholders.COMMAND;
import static com.armamc.plugincontrol.Placeholders.PLUGIN;

public class RemoveSubCommand implements SubCommand {
    private final ConfigManager config;
    private final MessageManager message;

    @Contract(pure = true)
    public RemoveSubCommand(@NotNull PluginControl plugin) {
        this.config = plugin.getConfigManager();
        this.message = plugin.getMessageManager();
    }

    @Override
    public void execute(CommandSender sender, Command command, String label, String[] args) {
        if (config.getPluginList().isEmpty()) {
            message.send(sender, message.getPluginListEmpty());
            return;
        }

        if (args.length == 0 || args[0].isBlank()) {
            message.send(sender, message.getPluginRemoveError(), Placeholder.parsed(COMMAND, label));
            return;
        }

        var target = args[0];
        if (target.equals("all")) {
            config.removeAllPlugins();
            message.send(sender, message.getAllPluginsRemoved());
            return;
        }

        if (config.removePlugin(target)) {
            message.send(sender, message.getPluginRemoved(), Placeholder.parsed(PLUGIN, target));
        } else {
            message.send(sender, message.getPluginNotFound(), Placeholder.parsed(PLUGIN, target));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        return config.getPluginList().stream().filter(s -> s.startsWith(args[0])).toList();
    }

}
