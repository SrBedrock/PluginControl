package com.armamc.plugincontrol.config;

import com.armamc.plugincontrol.PluginControl;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.player.PlayerLoginEvent;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

public class Config {
    private final PluginControl plugin;
    private static final String CONFIG_FILE_NAME = "config.yml";
    private static FileConfiguration config;
    private static File configFile;

    public Config(PluginControl plugin) {
        this.plugin = plugin;
        createDataFolder();
        plugin.saveDefaultConfig();
        loadConfig();
    }

    /* Método que cria as pastas do plugin */
    private void createDataFolder() {
        if (!plugin.getDataFolder().exists() && (plugin.getDataFolder().mkdir())) {
            plugin.getLogger().log(Level.INFO, "Creating the plugin folder!");
        }
    }

    private void loadConfig() {
        configFile = new File(plugin.getDataFolder(), CONFIG_FILE_NAME);
        config = YamlConfiguration.loadConfiguration(configFile);
        if (!configFile.exists()) {
            plugin.getLogger().log(Level.INFO, "Creating the configuration file!");
            plugin.saveResource(CONFIG_FILE_NAME, false);
        }
    }

    public void setEnabled(boolean enabled) {
        config.set("enabled", enabled);
        saveConfig();
    }

    private void setPluginList(List<String> pluginList) {
        config.set("plugins", pluginList);
        saveConfig();
    }

    private void saveConfig() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                config.save(configFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while saving the configuration file!", e);
            }
        });
    }

    public void reload() {
        PlayerLoginEvent.getHandlerList().unregister(plugin);
    }

    public String getAction() {
        return config.getString("action");
    }

    public void setAction(String action) {
        config.set("action", action);
        saveConfig();
    }

    public String getKickMessage() {
        return config.getString("kick-message");
    }

    public void setKickMessage(String kickMessage) {
        config.set("kick-message", kickMessage);
        saveConfig();
    }

    public boolean isEnabled() {
        return config.getBoolean("enabled");
    }

    public List<String> getPluginList() {
        return config.getStringList("plugins");
    }

    public boolean addPlugin(String pluginName) {
        List<String> pluginList = getPluginList();
        if (!pluginList.contains(pluginName)) {
            pluginList.add(pluginName);
            setPluginList(pluginList);
            return true;
        } else {
            return false;
        }
    }

    public boolean removePlugin(String pluginName) {
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
