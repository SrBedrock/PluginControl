package com.armamc.plugincontrol.config;

import com.armamc.plugincontrol.PluginControl;
import com.google.common.base.Charsets;
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
            plugin.getLogger().warning("Creating the lang.yml file!");
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

    public String message(String path) {
        return lang.getString(path, path + " in lang.yaml not found! Update your lang.yaml file!");
    }

    public List<String> help() {
        List<?> list = lang.getList("command.help", List.of("command.help in lang.yaml not found! Update your lang.yaml file!"));
        return list.stream().map(Object::toString).toList();
    }

}
