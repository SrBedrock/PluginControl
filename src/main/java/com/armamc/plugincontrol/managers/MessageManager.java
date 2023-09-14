package com.armamc.plugincontrol.managers;

import com.armamc.plugincontrol.PluginControl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class MessageManager {
    private final PluginControl plugin;
    private static FileConfiguration lang;
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final String LANG_FILE_NAME = "lang.yml";

    public MessageManager(PluginControl plugin) {
        this.plugin = plugin;
        loadLang();
    }

    public void loadLang() {
        var langFile = new File(plugin.getDataFolder(), LANG_FILE_NAME);
        if (!langFile.exists()) {
            plugin.getLogger().info("Creating the lang.yml file!");
            plugin.saveResource(LANG_FILE_NAME, false);
        }

        lang = YamlConfiguration.loadConfiguration(langFile);
    }

    public void reloadLang() {
        var langFile = new File(plugin.getDataFolder(), LANG_FILE_NAME);
        lang = YamlConfiguration.loadConfiguration(langFile);

        final InputStream defConfigStream = plugin.getResource(LANG_FILE_NAME);
        if (defConfigStream == null) return;

        lang.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8)));
    }

    public void saveLang() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                lang.save(new File(plugin.getDataFolder(), LANG_FILE_NAME));
            } catch (Exception e) {
                plugin.getLogger().warning("Could not save lang.yml file!");
            }
        });
    }

    public void send(@NotNull CommandSender sender, @NotNull String message) {
        if (message.isEmpty() || message.isBlank()) return;
        plugin.adventure().sender(sender).sendMessage(MM.deserialize(message, getPrefix()));
    }

    public void send(@NotNull CommandSender sender, String message, @NotNull TagResolver tag) {
        if (message == null || message.isEmpty() || message.isBlank()) return;
        plugin.adventure().sender(sender).sendMessage(MM.deserialize(message, getPrefix(), tag));
    }

    public void send(@NotNull CommandSender sender, @NotNull String message, @NotNull TagResolver... tags) {
        if (message.isEmpty() || message.isBlank()) return;
        List<TagResolver> allTags = new ArrayList<>();
        allTags.add(getPrefix());
        allTags.addAll(List.of(tags));
        plugin.adventure().sender(sender).sendMessage(MM.deserialize(message, allTags.toArray(new TagResolver[0])));
    }

    public void send(@NotNull CommandSender sender, @NotNull List<String> message, @NotNull TagResolver tag) {
        if (message.isEmpty()) return;
        for (var line : message) {
            if (line.isEmpty()) continue;
            plugin.adventure().sender(sender).sendMessage(MM.deserialize(line, getPrefix(), tag));
        }
    }

    public @NotNull Component getPluginListComponent(@NotNull Set<String> pluginList) {
        var joinConfiguration = JoinConfiguration.separators(MM.deserialize(getPluginListSeparator()),
                MM.deserialize(getPluginListSeparatorLast()));

        var componentList = new ArrayList<Component>();
        if (!pluginList.isEmpty()) {
            var command = "/plugincontrol remove %s";
            for (var pluginName : pluginList) {
                var color = plugin.isPluginEnabled(pluginName) ? NamedTextColor.GREEN : NamedTextColor.RED;
                componentList.add(MM.deserialize(pluginName).color(color)
                        .hoverEvent(HoverEvent.showText(MM.deserialize(getPluginClickRemove())))
                        .clickEvent(ClickEvent.runCommand(command.formatted(pluginName))));
            }
        }

        return Component.join(joinConfiguration, componentList);
    }

    public @NotNull Component getGroupListComponent(@NotNull Map<String, Set<String>> pluginGroups) {
        if (pluginGroups.isEmpty()) return MM.deserialize(getGroupListEmpty());

        var componentList = new ArrayList<Component>();
        var groupCommand = "/plugincontrol group list %s";
        var sortedGroups = pluginGroups.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .toList();

        for (var groupEntry : sortedGroups) {
            var groupName = groupEntry.getKey();

            componentList.add(Component.newline().append(MM.deserialize(getGroupListName(), Placeholder.parsed("group", groupName))
                    .hoverEvent(HoverEvent.showText(MM.deserialize(getGroupClickInfo())))
                    .clickEvent(ClickEvent.runCommand(groupCommand.formatted(groupName)))));

            var plugins = new TreeSet<>(groupEntry.getValue());
            var pluginComponents = new ArrayList<Component>();
            if (!plugins.isEmpty()) {
                var joinConfiguration = JoinConfiguration.builder()
                        .separator(MM.deserialize(getPluginListSeparator()))
                        .lastSeparator(MM.deserialize(getPluginListSeparatorLast()))
                        .build();

                var pluginCommand = "/plugincontrol group remove %s %s";
                for (var pluginName : plugins) {
                    var color = plugin.isPluginEnabled(pluginName) ? NamedTextColor.GREEN : NamedTextColor.RED;
                    pluginComponents.add(Component.text()
                            .append(MM.deserialize(pluginName)).color(color)
                            .hoverEvent(HoverEvent.showText(MM.deserialize(getGroupClickRemovePlugin())))
                            .clickEvent(ClickEvent.runCommand(pluginCommand.formatted(groupName, pluginName)))
                            .asComponent());
                }

                componentList.add(Component.text(" [").color(NamedTextColor.GRAY)
                        .append(Component.join(joinConfiguration, pluginComponents))
                        .append(Component.text("]").color(NamedTextColor.GRAY)));
            } else {
                componentList.add(Component.text(" [ ]").color(NamedTextColor.GRAY));
            }
        }

        return Component.join(JoinConfiguration.separator(Component.empty()), componentList);
    }

    public Component deserialize(String string) {
        return MM.deserialize(string);
    }

    public String serialize(String string) {
        return LegacyComponentSerializer.builder().hexColors().build().serialize(MM.deserialize(string));
    }

    public TagResolver.Single getPrefix() {
        return Placeholder.parsed("prefix", lang.getString("prefix", "<dark_gray>[<red>PluginControl<dark_gray>]"));
    }

    public String getKickMessage() {
        var kick = "kick-message";
        if (lang.getString(kick) == null) {
            setKickMessage("<red>[PluginControl] You are not allowed to join the server!");
        }

        return lang.getString(kick);
    }

    public void setKickMessage(String kickMessage) {
        lang.set("kick-message", kickMessage);
        saveLang();
    }

    public List<String> getHelpList() {
        return lang.getStringList("command.help");
    }

    public String getPluginEnabled() {
        return lang.getString("command.plugin-enabled");
    }

    public String getPluginDisabled() {
        return lang.getString("command.plugin-disabled");
    }

    public String getPluginAdded() {
        return lang.getString("command.plugin-added");
    }

    public String getAllPluginsAdded() {
        return lang.getString("command.plugin-added-all");
    }

    public String getPluginAlreadyAdded() {
        return lang.getString("command.plugin-already-added");
    }

    public String getPluginAddError() {
        return lang.getString("command.plugin-add-error");
    }

    public String getPluginListEmpty() {
        return lang.getString("command.plugin-list-empty");
    }

    public String getPluginRemoveError() {
        return lang.getString("command.plugin-remove-error");
    }

    public String getPluginRemoved() {
        return lang.getString("command.plugin-removed");
    }

    public String getPluginNotFound() {
        return lang.getString("command.plugin-not-found");
    }

    public String getPluginList() {
        return lang.getString("command.plugin-list");
    }

    public String getActionType() {
        return lang.getString("command.action-type");
    }

    public String getActionSet() {
        return lang.getString("command.action-set");
    }

    public String getActionTypeList() {
        return lang.getString("command.action-list");
    }

    public String getKickMessageInfo() {
        return lang.getString("command.kick-message");
    }

    public String getKickMessageSet() {
        return lang.getString("command.kick-message-set");
    }

    public String getPluginReloaded() {
        return lang.getString("command.plugin-reload");
    }

    public String getCheckingMessage() {
        return lang.getString("console.checking-plugins");
    }

    public String getCheckFinished() {
        return lang.getString("console.finished-checking");
    }

    public String getLogToConsole() {
        return lang.getString("console.log-to-console");
    }

    public String getLogToConsoleGroup() {
        return lang.getString("console.log-to-console-group");
    }

    public String getDisablingServer() {
        return lang.getString("console.disabling-server");
    }

    public String getPluginListSeparator() {
        return lang.getString("command.plugin-list-separator");
    }

    public String getPluginListSeparatorLast() {
        return lang.getString("command.plugin-list-separator-last");
    }

    public String getPluginClickRemove() {
        return lang.getString("command.plugin-click-remove");
    }

    public List<String> getGroupHelp() {
        return lang.getStringList("command.group-help");
    }

    public String getGroupCreateError() {
        return lang.getString("command.group-create-error");
    }

    public String getGroupCreated() {
        return lang.getString("command.group-created");
    }

    public String getGroupAlreadyExist() {
        return lang.getString("command.group-already-exist");
    }

    public String getGroupRemoveError() {
        return lang.getString("command.group-remove-error");
    }

    public String getGroupRemoved() {
        return lang.getString("command.group-removed");
    }

    public String getGroupNotFound() {
        return lang.getString("command.group-not-found");
    }

    public String getGroupListEmpty() {
        return lang.getString("command.group-list-empty");
    }

    public String getGroupList() {
        return lang.getString("command.group-list");
    }

    public String getPluginAddedToGroup() {
        return lang.getString("command.plugin-added-to-group");
    }

    public String getPluginAddToGroupError() {
        return lang.getString("command.plugin-add-to-group-error");
    }

    public String getPluginRemoveFromGroupError() {
        return lang.getString("command.plugin-removed-from-group-error");
    }

    public String getPluginRemovedFromGroup() {
        return lang.getString("command.plugin-removed-from-group");
    }

    public String getPluginNotInGroupError() {
        return lang.getString("command.plugin-not-in-group");
    }

    public String getGroupHasNoPlugins() {
        return lang.getString("command.group-has-no-plugins");
    }

    public String getGroupPluginList() {
        return lang.getString("command.group-plugin-list");
    }

    public String getAllPluginsRemoved() {
        return lang.getString("command.plugin-removed-all");
    }

    public String getGroupListName() {
        return lang.getString("command.group-list-name");
    }

    public String getGroupClickRemovePlugin() {
        return lang.getString("command.group-click-remove-plugin");
    }

    public String getGroupClickInfo() {
        return lang.getString("command.group-click-info");
    }
}
