package com.armamc.plugincontrol.config;

import com.armamc.plugincontrol.PluginControl;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class Config {
    private final PluginControl plugin;
    private static final String CONFIG_FILE_NAME = "config.yml";
    private static FileConfiguration config;
    private static File configFile;

    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("&#([0-9A-Fa-f]{6})");

    public Config(PluginControl plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        configFile = new File(plugin.getDataFolder(), CONFIG_FILE_NAME);
        if (!configFile.exists()) {
            plugin.getLogger().log(Level.INFO, "Creating the configuration file!");
            plugin.saveResource(CONFIG_FILE_NAME, false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void setEnabled(boolean enabled) {
        config.set("enabled", enabled);
        saveConfig();
    }

    private void setPluginList(List<String> pluginList) {
        config.set("plugins", pluginList);
        saveConfig();
    }

    public void saveConfig() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                config.save(configFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while saving the configuration file!", e);
            }
        });
    }

    public String getAction() {
        if (config.getString("action") == null) {
            config.set("action", "log-to-console");
            saveConfig();
        }
        return config.getString("action");
    }

    public void setAction(String action) {
        config.set("action", action);
        saveConfig();
    }

    public String getKickMessage() {
        if (config.getString("kick-message") == null) {
            config.set("kick-message", "&#FFFFFF[PluginControl] You are not allowed to join the server!");
            saveConfig();
        }
        return config.getString("kick-message");
    }

    public void setKickMessage(String kickMessage) {
        config.set("kick-message", kickMessage);
        saveConfig();
    }

    public boolean isEnabled() {
        if (config.getString("enabled") == null) {
            config.set("enabled", "false");
            saveConfig();
        }
        return config.getBoolean("enabled");
    }

    public List<String> getPluginList() {
        return config.getStringList("plugins");
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

    public String parseColor(String message) {
        var matcher = HEX_COLOR_PATTERN.matcher(message);
        var buffer = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, ChatColor.of("#" + matcher.group(1)).toString());
        }
        message = matcher.appendTail(buffer).toString();
        return ChatColor.translateAlternateColorCodes('&', message);
    }

}
