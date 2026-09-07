package com.armamc.plugincontrol.managers;

import com.armamc.plugincontrol.PluginControl;
import com.armamc.plugincontrol.storage.DataStorage;
import com.armamc.plugincontrol.storage.DataStorageFactory;
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
    private final DataStorage dataStorage;
    private Set<String> pluginList;
    private Map<String, Set<String>> pluginGroups;

    public ConfigManager(@NotNull PluginControl plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.dataStorage = DataStorageFactory.create(plugin);
        loadData();
    }

    private void loadData() {
        var data = dataStorage.load();
        if (data.plugins().isEmpty() && data.groups().isEmpty() && hasLegacyData()) {
            data = migrateLegacyData();
        }
        pluginList = new HashSet<>(data.plugins());
        pluginGroups = new HashMap<>();
        data.groups().forEach((name, plugins) -> pluginGroups.put(name, new HashSet<>(plugins)));
        dataStorage.save(pluginList, pluginGroups);
    }

    private boolean hasLegacyData() {
        return config.contains(PLUGINS) || config.contains(GROUPS);
    }

    private DataStorage.DataSnapshot migrateLegacyData() {
        var plugins = new HashSet<>(config.getStringList(PLUGINS));
        var groups = new HashMap<String, Set<String>>();
        var section = config.getConfigurationSection(GROUPS);
        if (section != null) {
            for (var name : section.getKeys(false)) {
                groups.put(name, new HashSet<>(config.getStringList(GROUPS + "." + name)));
            }
        }
        config.set(PLUGINS, null);
        config.set(GROUPS, null);
        plugin.saveConfig();
        plugin.getLogger().info("Migrated plugin data from config.yml to the configured data storage.");
        return new DataStorage.DataSnapshot(plugins, groups);
    }

    private void saveData() {
        dataStorage.save(pluginList, pluginGroups);
    }

    public void saveConfig() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, plugin::saveConfig);
    }

    public List<String> getServerPlugins() {
        return Arrays.stream(Bukkit.getPluginManager().getPlugins()).map(Plugin::getName).toList();
    }

    public boolean isUpdateNotifierEnabled() {
        return config.getBoolean("update-notifier", false);
    }

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

    public String getAction() {
        if (config.getString(ACTION) == null) {
            config.set(ACTION, ActionType.LOG_TO_CONSOLE.getAction());
            saveConfig();
        }
        return config.getString(ACTION);
    }

    public void setAction(@NotNull ActionType action) {
        config.set(ACTION, action.getAction());
        saveConfig();
    }

    public Set<String> getPluginList() {
        return pluginList.stream().sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public boolean addPlugin(String pluginName) {
        if (!pluginList.add(pluginName)) return false;
        saveData();
        return true;
    }

    public void addAllPlugins(List<String> pluginName) {
        pluginList.addAll(pluginName);
        saveData();
    }

    public boolean removePlugin(String pluginName) {
        if (!pluginList.remove(pluginName)) return false;
        saveData();
        return true;
    }

    public void removeAllPlugins() {
        pluginList.clear();
        saveData();
    }

    public Map<String, Set<String>> getPluginGroups() {
        return pluginGroups;
    }

    public List<String> getPluginGroupList() {
        return pluginGroups.keySet().stream().toList();
    }

    public boolean addGroup(@NotNull String groupName) {
        if (pluginGroups.containsKey(groupName)) return false;
        pluginGroups.put(groupName, new HashSet<>());
        saveData();
        return true;
    }

    public boolean addPluginToGroup(String groupName, String plugin) {
        if (groupName == null || groupName.isEmpty() || plugin == null || plugin.isEmpty()) return false;
        var existingPlugins = pluginGroups.get(groupName);
        if (existingPlugins == null) return false;
        existingPlugins.add(plugin);
        saveData();
        return true;
    }

    public boolean isGroupEmpty(String groupName) {
        return pluginGroups.get(groupName) != null && pluginGroups.get(groupName).isEmpty();
    }

    public boolean isGroupNonexistent(String groupName) {
        return pluginGroups.get(groupName) == null;
    }

    public boolean removePluginFromGroup(String groupName, String pluginName) {
        var pluginsInGroup = pluginGroups.get(groupName);
        if (pluginsInGroup == null || !pluginsInGroup.removeIf(p -> p.equalsIgnoreCase(pluginName))) return false;
        saveData();
        return true;
    }

    public boolean removeGroup(String groupName) {
        if (pluginGroups.remove(groupName) == null) return false;
        saveData();
        return true;
    }

    public Set<String> getPluginsOfGroup(String groupName) {
        return pluginGroups.get(groupName);
    }

    public void close() {
        dataStorage.close();
    }

    public enum ActionType {
        LOG_TO_CONSOLE("log-to-console"),
        DISALLOW_PLAYER_LOGIN("disallow-player-login"),
        SHUTDOWN_SERVER("shutdown-server");

        private final String action;
        private static final Map<String, ActionType> lookup = new HashMap<>();

        static {
            for (var actionType : values()) lookup.put(actionType.getAction(), actionType);
        }

        @Contract(pure = true)
        ActionType(String action) {
            this.action = action;
        }

        public static @NotNull ActionType from(String action) {
            var result = lookup.get(action);
            if (result == null) throw new IllegalArgumentException("Unexpected value: " + action);
            return result;
        }

        @Contract(pure = true)
        public String getAction() {
            return action;
        }
    }
}
