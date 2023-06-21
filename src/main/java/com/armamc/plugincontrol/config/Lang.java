package com.armamc.plugincontrol.config;

import com.armamc.plugincontrol.PluginControl;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class Lang {
    private final PluginControl plugin;
    private static final String LANG_FILE_NAME = "lang.yml";
    private static FileConfiguration lang;
    private static File langFile;

    public Lang(PluginControl plugin) {
        this.plugin = plugin;
        loadLang();
        saveDefaultLang();
    }

    private void loadLang() {
        langFile = new File(plugin.getDataFolder(), LANG_FILE_NAME);
        lang = YamlConfiguration.loadConfiguration(langFile);
        if (!langFile.exists()) {
            plugin.getLogger().log(Level.INFO, "Creating the language file!");
            plugin.saveResource(LANG_FILE_NAME, false);
        }
    }

    private void saveDefaultLang() {
        if (!langFile.exists()) {
            plugin.saveResource(LANG_FILE_NAME, false);
        }
    }

    public void reload() {
        lang = YamlConfiguration.loadConfiguration(langFile);
        final InputStream defLangStream = plugin.getResource(LANG_FILE_NAME);
        if (defLangStream == null) {
            return;
        }
        lang.setDefaults(YamlConfiguration.loadConfiguration(
                new InputStreamReader(defLangStream, StandardCharsets.UTF_8)));
    }

    public String message(String path) {
        return lang.getString(path, path + " in lang.yaml not found! Update your lang.yaml file!");
    }

}
