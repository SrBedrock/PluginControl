package com.armamc.plugincontrol.commands.subcommands;

import com.armamc.plugincontrol.PluginControl;
import com.armamc.plugincontrol.managers.ConfigManager;
import com.armamc.plugincontrol.managers.MessageManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class GroupSubCommand implements SubCommand {
    private final PluginControl plugin;
    private final ConfigManager config;
    private final MessageManager message;
    private static final String GROUP_TAG = "group";
    private static final String PLUGIN_TAG = "plugin";
    private static final String COMMAND_TAG = "command";
    private final List<String> subcommands;

    @Contract(pure = true)
    public GroupSubCommand(@NotNull PluginControl plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.message = plugin.getMessageManager();
        this.subcommands = List.of("create", "delete", "list", "add", "remove", "help");
    }

    @Override
    public void execute(CommandSender sender, Command command, String label, String @NotNull [] args) {
        if (args.length == 0 || args[0].isBlank()) {
            message.send(sender, message.getGroupHelp(), Placeholder.parsed(COMMAND_TAG, label));
            return;
        }

        var target = args[0];
        if (target.equalsIgnoreCase("create")) {
            if (args.length == 1) {
                message.send(sender, message.getGroupCreateError(), Placeholder.parsed(COMMAND_TAG, label));
                return;
            }

            var targetGroup = args[1];
            if (config.addGroup(targetGroup)) {
                message.send(sender, message.getGroupCreated(), Placeholder.parsed(GROUP_TAG, targetGroup));
            } else {
                message.send(sender, message.getGroupAlreadyExist(), Placeholder.parsed(GROUP_TAG, targetGroup));
            }
            return;
        }

        if (target.equalsIgnoreCase("delete")) {
            if (args.length == 1) {
                message.send(sender, message.getGroupRemoveError(), Placeholder.parsed(COMMAND_TAG, label));
                return;
            }

            var targetGroup = args[1];
            if (config.removeGroup(targetGroup)) {
                message.send(sender, message.getGroupRemoved(), Placeholder.parsed(GROUP_TAG, targetGroup));
            } else {
                message.send(sender, message.getGroupNotFound(), Placeholder.parsed(GROUP_TAG, targetGroup));
            }
            return;
        }

        if (target.equalsIgnoreCase("list")) {
            if (args.length == 1) {
                if (config.getPluginGroups() == null || config.getPluginGroups().isEmpty()) {
                    message.send(sender, message.getGroupListEmpty());
                } else {
                    message.send(sender, message.getGroupList(), Placeholder.component("groups",
                            message.getGroupListComponent(config.getPluginGroups())));
                }
                return;
            }

            var targetGroup = args[1];
            if (isValidGroup(sender, targetGroup)) {
                var plugins = config.getPluginsOfGroup(targetGroup);
                message.send(sender, message.getGroupPluginList(), Placeholder.parsed(GROUP_TAG, targetGroup),
                        Placeholder.component("plugins", message.getPluginListComponent(plugins)));
            }
            return;
        }

        if (target.equalsIgnoreCase("add")) {
            if (args.length <= 2) {
                message.send(sender, message.getPluginAddToGroupError(), Placeholder.parsed(COMMAND_TAG, label));
                return;
            }

            var targetGroup = args[1];
            if (config.isGroupNonexistent(targetGroup)) {
                message.send(sender, message.getGroupNotFound(), Placeholder.parsed(GROUP_TAG, targetGroup));
                return;
            }

            var targetPlugin = args[2];
            if (config.addPluginToGroup(targetGroup, targetPlugin)) {
                message.send(sender, message.getPluginAddedToGroup(), Placeholder.parsed(GROUP_TAG, targetGroup),
                        Placeholder.parsed(PLUGIN_TAG, targetPlugin));
            } else {
                message.send(sender, message.getPluginAddToGroupError(), Placeholder.parsed(GROUP_TAG, targetGroup),
                        Placeholder.parsed(PLUGIN_TAG, targetPlugin));
            }
            return;
        }

        if (target.equalsIgnoreCase("remove")) {
            if (args.length == 1) {
                message.send(sender, message.getPluginRemoveFromGroupError(), Placeholder.parsed(COMMAND_TAG, label));
                return;
            }

            var targetGroup = args[1];
            if (isValidGroup(sender, targetGroup)) {
                var targetPlugin = args[2];
                if (config.removePluginFromGroup(targetGroup, targetPlugin)) {
                    message.send(sender, message.getPluginRemovedFromGroup(), Placeholder.parsed(GROUP_TAG, targetGroup),
                            Placeholder.parsed(PLUGIN_TAG, targetPlugin));
                } else {
                    message.send(sender, message.getPluginNotInGroupError(), Placeholder.parsed(GROUP_TAG, targetGroup),
                            Placeholder.parsed(PLUGIN_TAG, targetPlugin));
                }
            }
            return;
        }

        if (target.equalsIgnoreCase("help")) {
            message.send(sender, message.getGroupHelp(), Placeholder.parsed(COMMAND_TAG, label));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String label, String @NotNull [] args) {
        if (args.length == 1) {
            return subcommands.stream().filter(s -> s.startsWith(args[0])).toList();
        }

        var target = args[0];
        if (target.equalsIgnoreCase("create") && (args.length == 2)) {
            return Stream.of("<group-name>").filter(s -> s.startsWith(args[1])).toList();
        }

        if (target.equalsIgnoreCase("delete") && (args.length == 2)) {
            return config.getPluginGroupList().stream().filter(s -> s.startsWith(args[1])).toList();
        }

        if (target.equalsIgnoreCase("list")) {
            if (args.length == 2) {
                return config.getPluginGroupList().stream().filter(s -> s.startsWith(args[1])).toList();
            }
            if (args.length == 3 && !config.isGroupNonexistent(args[2])) {
                return config.getPluginsOfGroup(args[2]).stream().filter(s -> s.startsWith(args[2])).toList();
            }
        }

        if (target.equalsIgnoreCase("add") || target.equalsIgnoreCase("remove")) {
            if (args.length == 2) {
                return config.getPluginGroupList().stream().filter(s -> s.startsWith(args[1])).toList();
            }

            if (args.length == 3 && target.equalsIgnoreCase("add")) {
                return config.getServerPlugins().stream().filter(s -> s.startsWith(args[2])).toList();
            }

            if (args.length == 3 && target.equalsIgnoreCase("remove") && !config.isGroupNonexistent(args[1])) {
                return config.getPluginsOfGroup(args[1]).stream().filter(s -> s.startsWith(args[2])).toList();
            }
        }

        return List.of();
    }

    private boolean isValidGroup(CommandSender sender, String groupName) {
        if (config.isGroupNonexistent(groupName)) {
            message.send(sender, message.getGroupNotFound(), Placeholder.parsed(GROUP_TAG, groupName));
            return false;
        }

        if (config.isGroupEmpty(groupName)) {
            message.send(sender, message.getGroupHasNoPlugins(), Placeholder.parsed(GROUP_TAG, groupName));
            return false;
        }

        return true;
    }

}
