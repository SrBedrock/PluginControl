package com.armamc.plugincontrol.config;

import com.armamc.plugincontrol.PluginControl;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

public class Config {

    private static final PluginControl plugin = PluginControl.getInstance();
    private static final String CONFIG_FILE_NAME = "config.yml";
    private static FileConfiguration config;
    private static File configFile;

    public Config() {
        createDataFolder();
        loadConfig();
    }

    public static void load() {
        new Config();
    }

    /* Método que cria as pastas do plugin */
    private void createDataFolder() {
        if (!plugin.getDataFolder().exists() && (plugin.getDataFolder().mkdir())) {
                plugin.getLogger().log(Level.INFO, "Criando a pasta do plugin!");
        }
    }

    private void loadConfig() {
        configFile = new File(plugin.getDataFolder(), CONFIG_FILE_NAME);
        config = YamlConfiguration.loadConfiguration(configFile);
        if (!configFile.exists()) {
            plugin.getLogger().log(Level.INFO, "Criando o arquivo de configuração!");
            plugin.saveResource(CONFIG_FILE_NAME, false);
        }
    }

    public static void setEnabled(boolean enabled) {
        config.set("enabled", enabled);
        saveConfig();
    }

    public static void setPluginList(List<String> pluginList) {
        config.set("plugins", pluginList);
        saveConfig();
    }

    private static void saveConfig() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                config.save(configFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Erro ao salvar o arquivo de configuração!", e);
            }
        });
    }

    public static boolean isEnabled() {
        return config.getBoolean("enabled");
    }

    public static List<String> getPluginList() {
        return config.getStringList("plugins");
    }

    public static boolean addPlugin(String pluginName) {
        List<String> pluginList = getPluginList();
        if (!pluginList.contains(pluginName)) {
            pluginList.add(pluginName);
            setPluginList(pluginList);
            return true;
        } else {
            return false;
        }
    }

    public static boolean removePlugin(String pluginName) {
        List<String> pluginList = getPluginList();
        if (pluginList.contains(pluginName)) {
            pluginList.remove(pluginName);
            setPluginList(pluginList);
            return true;
        } else {
            return false;
        }
    }

}
