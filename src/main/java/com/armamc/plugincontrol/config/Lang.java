package com.armamc.plugincontrol.config;

import com.armamc.plugincontrol.PluginControl;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

public class Lang {
    private final PluginControl plugin;
    private static final String LANG_FILE_NAME = "lang.yml";
    private static FileConfiguration lang;

    public Lang(PluginControl plugin) {
        this.plugin = plugin;
        loadLang();
    }

    public void loadLang() {
        var langFile = new File(plugin.getDataFolder(), LANG_FILE_NAME);
        if (!langFile.exists()) {
            plugin.getLogger().log(Level.INFO, "Creating the language file!");
            plugin.saveResource(LANG_FILE_NAME, false);
        }
        lang = YamlConfiguration.loadConfiguration(langFile);
    }

    public String message(String path) {
        return lang.getString(path, path + " in lang.yaml not found! Update your lang.yaml file!");
    }

    public List<String> help() {
        List<?> list = lang.getList("help", List.of("help in lang.yaml not found! Update your lang.yaml file!"));
        return list.stream().map(Object::toString).toList();
    }

}
