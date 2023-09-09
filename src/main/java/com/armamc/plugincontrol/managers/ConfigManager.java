package com.armamc.plugincontrol.managers;

import com.armamc.plugincontrol.PluginControl;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ConfigManager {
    private final PluginControl plugin;
    private final FileConfiguration config;
    private static final String PLUGINS = "plugins";
    private static final String ENABLED = "enabled";
    private static final String ACTION = "action";
    private static final String KICK_MESSAGE = "kick-message";

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

    // enabled
    public boolean isEnabled() {
        if (config.getString(ENABLED) == null) {
            config.set(ENABLED, "false");
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

    // kick-message
    public String getKickMessage() {
        if (config.getString(KICK_MESSAGE) == null) {
            config.set(KICK_MESSAGE, "&#FFF000[PluginControl] You are not allowed to join the server!");
            saveConfig();
        }

        return config.getString(KICK_MESSAGE);
    }

    public void setKickMessage(String kickMessage) {
        config.set(KICK_MESSAGE, kickMessage);
        saveConfig();
    }

    // plugins
    private void loadPlugins() {
        pluginList = new HashSet<>();

        if (config.contains("plugins")) {

            var plugins = config.getStringList("plugins");
            if (!plugins.isEmpty()) {
                pluginList.addAll(plugins);
            }
        }
    }

    public Set<String> getPluginList() {
        return this.pluginList;
    }

    private void savePluginList() {
        config.set(PLUGINS, pluginList);
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
        if (config.contains("groups")) {
            ConfigurationSection groupsSection = config.getConfigurationSection("groups");
            if (groupsSection != null) {
                Set<String> groupNames = groupsSection.getKeys(false);
                for (String groupName : groupNames) {
                    Set<String> plugins = new HashSet<>(config.getStringList("groups." + groupName));
                    pluginGroups.put(groupName, plugins);
                }
            }
        } else {
            config.createSection("groups");
        }

        savePluginGroup();
    }

    private void savePluginGroup() {
        for (Map.Entry<String, Set<String>> entry : pluginGroups.entrySet()) {
            config.set("groups." + entry.getKey(), entry.getValue());
        }

        saveConfig();
    }

    public Map<String, Set<String>> getPluginGroups() {
        return pluginGroups;
    }

    public boolean addOrUpdateGroup(String groupName, Set<String> plugins) {
        if (groupName == null || groupName.isEmpty()) {
            return false;
        }

        var existingPlugins = pluginGroups.get(groupName);
        if (existingPlugins == null) {
            pluginGroups.put(groupName, plugins == null ? new HashSet<>() : new HashSet<>(plugins));
            savePluginGroup();
            return true;
        }

        if (plugins != null) {
            existingPlugins.addAll(plugins);
        }

        savePluginGroup();
        return true;
    }

    public boolean addPluginToGroup(String groupName, String plugin) {
        if (groupName == null || groupName.isEmpty() || plugin == null || plugin.isEmpty()) {
            return false; // Entradas inválidas.
        }

        var existingPlugins = pluginGroups.get(groupName);
        if (existingPlugins == null) {
            return false; // Grupo não existe.
        }

        existingPlugins.add(plugin);
        savePluginGroup();
        return true;
    }

    public boolean isGroupEmptyOrNonexistent(String groupName) {
        return pluginGroups.get(groupName) == null || pluginGroups.get(groupName).isEmpty();
    }

    public boolean removePluginFromGroup(String groupName, String pluginName) {
        if (pluginGroups.containsKey(groupName)) {
            var pluginsInGroup = pluginGroups.get(groupName);
            var removed = pluginsInGroup.removeIf(p -> p.equalsIgnoreCase(pluginName));

            if (removed) {
                savePluginGroup();
                return true;
            }
        }

        return false;
    }

    public boolean removeGroup(String arg) {
        if (pluginGroups.containsKey(arg)) {
            pluginGroups.remove(arg);
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
            for (ActionType actionType : ActionType.values()) {
                lookup.put(actionType.getAction(), actionType);
            }
        }

        @Contract(pure = true)
        ActionType(String action) {
            this.action = action;
        }

        public static @NotNull ActionType from(String action) {
            ActionType result = lookup.get(action);
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
