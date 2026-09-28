package com.armamc.plugincontrol.storage;

import com.armamc.plugincontrol.PluginControl;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class YamlDataStorage implements DataStorage {
    private static final String FILE_NAME = "data.yml";
    private final PluginControl plugin;
    private final File file;

    public YamlDataStorage(@NotNull PluginControl plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), FILE_NAME);
    }

    @Override
    public DataSnapshot load() {
        var data = YamlConfiguration.loadConfiguration(file);
        var plugins = new HashSet<>(data.getStringList("plugins"));
        var groups = new HashMap<String, Set<String>>();
        var section = data.getConfigurationSection("groups");
        if (section != null) {
            for (var name : section.getKeys(false)) {
                groups.put(name, new HashSet<>(data.getStringList("groups." + name)));
            }
        }
        return new DataSnapshot(plugins, groups);
    }

    @Override
    public void save(Set<String> plugins, Map<String, Set<String>> groups) {
        var data = new YamlConfiguration();
        data.set("plugins", new ArrayList<>(plugins));
        for (var entry : groups.entrySet()) {
            data.set("groups." + entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        try {
            data.save(file);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save " + FILE_NAME, exception);
        }
    }
}
