package com.armamc.plugincontrol.commands.subcommands;

import com.armamc.plugincontrol.PluginControl;
import com.armamc.plugincontrol.managers.ConfigManager;
import com.armamc.plugincontrol.managers.MessageManager;
import com.armamc.plugincontrol.managers.PluginsManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.armamc.plugincontrol.Placeholders.ACTION;
import static com.armamc.plugincontrol.Placeholders.ACTIONS;

public class ActionSubCommand implements SubCommand {
    private final ConfigManager config;
    private final MessageManager message;
    private final PluginsManager manager;
    private final List<String> actions;

    @Contract(pure = true)
    public ActionSubCommand(@NotNull PluginControl plugin) {
        this.config = plugin.getConfigManager();
        this.message = plugin.getMessageManager();
        this.manager = plugin.getPluginsManager();
        this.actions = List.of("log-to-console", "disallow-player-login", "shutdown-server");
    }

    @Override
    public void execute(CommandSender sender, Command command, String label, String @NotNull [] args) {
        if (args.length == 0 || args[0].isBlank()) {
            message.send(sender, message.getActionType(), Placeholder.parsed(ACTION, config.getAction().toLowerCase()));
            return;
        }

        try {
            var actionType = ConfigManager.ActionType.from(args[0].toLowerCase());
            config.setAction(actionType);
            message.send(sender, message.getActionSet(), Placeholder.parsed(ACTION, actionType.getAction()));
            if (actionType != ConfigManager.ActionType.DISALLOW_PLAYER_LOGIN) {
                manager.unregisterListener();
            }
            manager.checkPlugins();
        } catch (IllegalArgumentException e) {
            message.send(sender, message.getActionTypeList(), Placeholder.parsed(ACTIONS, String.join(", ", actions)));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) return List.of();
        return actions.stream().filter(s -> s.startsWith(args[0])).toList();
    }

}
