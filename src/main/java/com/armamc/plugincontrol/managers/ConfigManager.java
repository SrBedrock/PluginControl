package com.armamc.plugincontrol.managers;

import com.armamc.plugincontrol.PluginControl;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.armamc.plugincontrol.Placeholders.ACTION;
import static com.armamc.plugincontrol.Placeholders.ENABLED;
import static com.armamc.plugincontrol.Placeholders.GROUPS;
import static com.armamc.plugincontrol.Placeholders.PLUGINS;

public class ConfigManager {
    private final PluginControl plugin;
    private final FileConfiguration config;
    private Set<String> pluginList;
    private Map<String, Set<String>> pluginGroups;

    public ConfigManager(@NotNull PluginControl plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        loadConfig();
    }

    private void loadConfig() {
        loadPlugins();
        loadGroups();
    }

    public void saveConfig() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, plugin::saveConfig);
    }

    public List<String> getServerPlugins() {
        return Arrays.stream(Bukkit.getPluginManager().getPlugins()).toList().stream().map(Plugin::getName).toList();
    }

    // update
    public boolean isUpdateNotifierEnabled() {
        return config.getBoolean("update-notifier", false);
    }

    // enabled
    public boolean isEnabled() {
        if (config.getBoolean(ENABLED)) {
            config.set(ENABLED, false);
            saveConfig();
        }
        return config.getBoolean(ENABLED);
    }

    public void setEnabled(boolean enabled) {
        config.set(ENABLED, enabled);
        saveConfig();
    }

    // actions
    public String getAction() {
        if (config.getString(ACTION) == null) {
            config.set(ACTION, ActionType.LOG_TO_CONSOLE.getAction());
            saveConfig();
        }
        return config.getString(ACTION);
    }

    public void setAction(@NotNull ConfigManager.ActionType action) {
        config.set(ACTION, action.getAction());
        saveConfig();
    }

    // plugins
    private void loadPlugins() {
        pluginList = new HashSet<>();
        if (config.contains(PLUGINS)) {
            var plugins = config.getStringList(PLUGINS);
            if (!plugins.isEmpty()) {
                pluginList.addAll(plugins);
            }
        }
    }

    public Set<String> getPluginList() {
        return this.pluginList.stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private void savePluginList() {
        config.set(PLUGINS, new ArrayList<>(pluginList));
        saveConfig();
    }

    public boolean addPlugin(String pluginName) {
        if (!pluginList.contains(pluginName)) {
            pluginList.add(pluginName);
            savePluginList();
            return true;
        } else {
            return false;
        }
    }

    public void addAllPlugins(List<String> pluginName) {
        pluginList.addAll(pluginName);
        savePluginList();
    }

    public boolean removePlugin(String pluginName) {
        if (pluginList.contains(pluginName)) {
            pluginList.remove(pluginName);
            savePluginList();
            return true;
        } else {
            return false;
        }
    }

    public void removeAllPlugins() {
        pluginList.clear();
        savePluginList();
    }

    // groups
    private void loadGroups() {
        pluginGroups = new HashMap<>();
        if (config.contains(GROUPS)) {
            var groupsSection = config.getConfigurationSection(GROUPS);
            if (groupsSection != null) {
                var groupNames = groupsSection.getKeys(false);
                for (var groupName : groupNames) {
                    var plugins = new HashSet<>(config.getStringList(GROUPS + "." + groupName));
                    pluginGroups.put(groupName, plugins);
                }
            }
        } else {
            config.createSection(GROUPS);
        }
        savePluginGroup();
    }

    private void savePluginGroup() {
        for (var entry : pluginGroups.entrySet()) {
            config.set(GROUPS + "." + entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        saveConfig();
    }

    public Map<String, Set<String>> getPluginGroups() {
        return pluginGroups;
    }

    public List<String> getPluginGroupList() {
        return this.pluginGroups.keySet().stream().toList();
    }

    public boolean addGroup(@NotNull String groupName) {
        if (pluginGroups.get(groupName) == null) {
            pluginGroups.put(groupName, new HashSet<>());
            savePluginGroup();
            return true;
        } else {
            return false;
        }
    }

    public boolean addPluginToGroup(String groupName, String plugin) {
        if (groupName == null || groupName.isEmpty() || plugin == null || plugin.isEmpty())
            return false;

        var existingPlugins = pluginGroups.get(groupName);
        if (existingPlugins == null)
            return false;

        existingPlugins.add(plugin);
        savePluginGroup();
        return true;
    }

    public boolean isGroupEmpty(String groupName) {
        return pluginGroups.get(groupName) != null && pluginGroups.get(groupName).isEmpty();
    }

    public boolean isGroupNonexistent(String groupName) {
        return pluginGroups.get(groupName) == null;
    }

    public boolean removePluginFromGroup(String groupName, String pluginName) {
        if (pluginGroups.containsKey(groupName)) {
            var pluginsInGroup = pluginGroups.get(groupName);
            var removed = pluginsInGroup.removeIf(p -> p.equalsIgnoreCase(pluginName));
            if (removed) {
                config.set(GROUPS + "." + groupName, new ArrayList<>(pluginsInGroup));
                saveConfig();
                return true;
            }
        }
        return false;
    }

    public boolean removeGroup(String groupName) {
        if (pluginGroups.containsKey(groupName)) {
            pluginGroups.remove(groupName);
            config.set(GROUPS + "." + groupName, null);
            savePluginGroup();
            return true;
        } else {
            return false;
        }
    }

    public Set<String> getPluginsOfGroup(String groupName) {
        return pluginGroups.get(groupName);
    }

    public enum ActionType {
        LOG_TO_CONSOLE("log-to-console"),
        DISALLOW_PLAYER_LOGIN("disallow-player-login"),
        SHUTDOWN_SERVER("shutdown-server");

        private final String action;
        private static final Map<String, ActionType> lookup = new HashMap<>();

        static {
            for (var actionType : ActionType.values()) {
                lookup.put(actionType.getAction(), actionType);
            }
        }

        @Contract(pure = true)
        ActionType(String action) {
            this.action = action;
        }

        public static @NotNull ActionType from(String action) {
            var result = lookup.get(action);
            if (result == null) {
                throw new IllegalArgumentException("Unexpected value: " + action);
            }

            return result;
        }

        @Contract(pure = true)
        public String getAction() {
            return action;
        }

    }

}
