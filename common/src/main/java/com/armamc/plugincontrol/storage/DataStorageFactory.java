package com.armamc.plugincontrol.storage;

import com.armamc.plugincontrol.PluginControl;
import org.jetbrains.annotations.NotNull;

public final class DataStorageFactory {
    private DataStorageFactory() {
    }

    public static DataStorage create(@NotNull PluginControl plugin) {
        var type = plugin.getConfig().getString("storage.type", "yaml").toLowerCase();
        return switch (type) {
            case "yaml" -> new YamlDataStorage(plugin);
            case "h2", "mysql", "sqlite" -> new JdbcDataStorage(plugin, type);
            default -> throw new IllegalArgumentException("Unsupported data storage: " + type);
        };
    }
}
