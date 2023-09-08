package com.armamc.plugincontrol.config;

import com.armamc.plugincontrol.PluginControl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    private final PluginControl plugin;
    private final FileConfiguration config;
    private static final String PLUGINS = "plugins";
    private static final String ENABLED = "enabled";
    private static final String ACTION = "action";
    private static final String KICK_MESSAGE = "kick-message";

    public ConfigManager(@NotNull PluginControl plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        loadConfig();
    }

    private void loadConfig() {
        plugin.saveConfig();
        plugin.saveDefaultConfig();
    }

    public void setEnabled(boolean enabled) {
        config.set(ENABLED, enabled);
        saveConfig();
    }

    private void setPluginList(List<String> pluginList) {
        config.set(PLUGINS, pluginList);
        saveConfig();
    }

    public void saveConfig() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, plugin::saveConfig);
    }

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

    public boolean isEnabled() {
        if (config.getString(ENABLED) == null) {
            config.set(ENABLED, "false");
            saveConfig();
        }
        return config.getBoolean(ENABLED);
    }

    public List<String> getPluginList() {
        return config.getStringList(PLUGINS);
    }

    public boolean addPlugin(String pluginName) {
        var pluginList = getPluginList();
        if (!pluginList.contains(pluginName)) {
            pluginList.add(pluginName);
            setPluginList(pluginList);
            return true;
        } else {
            return false;
        }
    }

    public boolean removePlugin(String pluginName) {
        var pluginList = getPluginList();
        if (pluginList.contains(pluginName)) {
            pluginList.remove(pluginName);
            setPluginList(pluginList);
            return true;
        } else {
            return false;
        }
    }

    public Component deserialize(String string) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(string);
    }

    public String serialize(String string) {
        return LegacyComponentSerializer.legacyAmpersand().serialize(Component.text(string));
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
                throw new IllegalStateException("Unexpected value: " + action);
            }
            return result;
        }

        @Contract(pure = true)
        public String getAction() {
            return action;
        }

    }

}
