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
import java.util.Set;

public class GroupSubCommand implements SubCommand {
    private final ConfigManager config;
    private final MessageManager message;
    private static final String GROUP_TAG = "group";
    private static final String PLUGIN_TAG = "plugin";
    private static final String COMMAND_TAG = "command";

    @Contract(pure = true)
    public GroupSubCommand(@NotNull PluginControl plugin) {
        this.config = plugin.getConfigManager();
        this.message = plugin.getMessageManager();
    }

    @Override
    public void execute(CommandSender sender, Command command, String label, String @NotNull [] args) {
        if (args.length < 2 || args[0].isBlank()) {
            message.send(sender, message.getGroupHelp(), Placeholder.parsed(COMMAND_TAG, label));
        }

        var target = args[0];

        if (target.equalsIgnoreCase("create")) {
            if (args.length != 3) {
                message.send(sender, message.getGroupAddError(), Placeholder.parsed(COMMAND_TAG, label));
                return;
            }

            if (config.addOrUpdateGroup(args[2], null)) {
                message.send(sender, message.getGroupAdded(), Placeholder.parsed(GROUP_TAG, args[2]));
            } else {
                message.send(sender, message.getGroupAlreadyAdded(), Placeholder.parsed(GROUP_TAG, args[2]));
            }
        }

        if (target.equalsIgnoreCase("delete")) {
            if (args.length != 3) {
                message.send(sender, message.getGroupRemoveError(), Placeholder.parsed(COMMAND_TAG, label));
                return;
            }
            if (config.removeGroup(args[2])) {
                message.send(sender, message.getGroupRemoved(), Placeholder.parsed(GROUP_TAG, args[2]));
            } else {
                message.send(sender, message.getGroupNotFound(), Placeholder.parsed(GROUP_TAG, args[2]));
            }
        }

        if (target.equalsIgnoreCase("list")) {

            if (args.length == 2) {
                if (config.getPluginGroups() == null || config.getPluginGroups().isEmpty()) {
                    message.send(sender, message.getGroupListEmpty());
                } else {
                    message.send(sender, message.getGroupList(), Placeholder.component("groups", message.getGroupListComponent(config.getPluginGroups())));
                }
                return;
            }

            var plugins = config.getPluginsOfGroup(args[2]);
            if (plugins == null || plugins.isEmpty()) {
                message.send(sender, message.getGroupHasNoPlugins(), Placeholder.parsed(GROUP_TAG, args[2]));
            } else {
                message.send(sender, message.getGroupPluginList(), Placeholder.parsed(GROUP_TAG, args[2]), Placeholder.component("plugins", message.getPluginListComponent(plugins)));
            }
        }

        if (target.equalsIgnoreCase("add")) {

            if (args.length != 4) {
                message.send(sender, message.getPluginAddError(), Placeholder.parsed(COMMAND_TAG, label));
                return;
            }

            if (config.addPluginToGroup(args[2], args[3])) {
                message.send(sender, message.getPluginAddedToGroup(), Placeholder.parsed(GROUP_TAG, args[2]), Placeholder.parsed(PLUGIN_TAG, args[3]));
            } else {
                message.send(sender, message.getPluginAddToGroupError(), Placeholder.parsed(GROUP_TAG, args[2]), Placeholder.parsed(PLUGIN_TAG, args[3]));
            }
            return;
        }

        if (target.equalsIgnoreCase("remove")) {
            if (args.length != 4) {
                message.send(sender, message.getPluginRemoveFromGroupError(), Placeholder.parsed(COMMAND_TAG, label));
                return;
            }

            if (config.removePluginFromGroup(args[2], args[3])) {
                message.send(sender, message.getPluginRemovedFromGroup(), Placeholder.parsed(GROUP_TAG, args[2]), Placeholder.parsed(PLUGIN_TAG, args[3]));
            } else {
                message.send(sender, message.getPluginNotInGroupError(), Placeholder.parsed(GROUP_TAG, args[2]), Placeholder.parsed(PLUGIN_TAG, args[3]));
            }
        }

    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        return null;
    }

}
