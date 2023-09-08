package com.armamc.plugincontrol.managers;

import com.armamc.plugincontrol.PluginControl;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MessageManager {
    private final PluginControl plugin;
    private static final String LANG_FILE_NAME = "lang.yml";
    private static FileConfiguration lang;

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

    public String getPrefix() {
        return lang.getString("prefix");
    }

    public List<String> getHelpList() {
        return lang.getStringList("command.help");
    }

    public String getNoPermissionError() {
        return lang.getString("command.no-permission-error");
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

    public String getKickMessage() {
        return lang.getString("command.kick-message");
    }

    public String getKickMessageSet() {
        return lang.getString("command.kick-message-set");
    }

    public String getPluginReloaded() {
        return lang.getString("command.plugin-reload");
    }

    public String getCommandNotFound() {
        return lang.getString("command.command-not-found");
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

    public String getDisablingServer() {
        return lang.getString("console.disabling-server");
    }

    public String getPluginListSeparator() {
        return lang.getString("command.plugin-list-separator");
    }

    public String getPluginListSeparatorLast() {
        return lang.getString("console.plugin-list-separator-last");
    }

    public String getPluginClickAdd() {
        return lang.getString("command.plugin-click-add");
    }
}
