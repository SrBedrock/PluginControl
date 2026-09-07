package com.armamc.plugincontrol.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.armamc.plugincontrol.PluginControl;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class JdbcDataStorage implements DataStorage {
    private final PluginControl plugin;
    private final HikariDataSource dataSource;

    public JdbcDataStorage(@NotNull PluginControl plugin, @NotNull String type) {
        this.plugin = plugin;
        var hikariConfig = new HikariConfig();
        configure(hikariConfig, plugin, type);
        this.dataSource = new HikariDataSource(hikariConfig);
        try {
            createTables();
        } catch (SQLException exception) {
            dataSource.close();
            throw new IllegalStateException("Could not initialize " + type + " data storage", exception);
        }
    }

    private void configure(HikariConfig hikariConfig, PluginControl plugin, String type) {
        var config = plugin.getConfig();
        hikariConfig.setPoolName("PluginControl-" + type);
        hikariConfig.setMaximumPoolSize(Math.max(1, config.getInt("storage.database.pool-size", 5)));
        hikariConfig.setMinimumIdle(Math.min(1, hikariConfig.getMaximumPoolSize()));
        hikariConfig.setJdbcUrl(buildUrl(plugin, type));
        if (type.equals("mysql")) {
            hikariConfig.setUsername(config.getString("storage.database.username", "root"));
            hikariConfig.setPassword(config.getString("storage.database.password", ""));
        }
    }

    private String buildUrl(PluginControl plugin, String type) {
        var config = plugin.getConfig();
        return switch (type) {
            case "sqlite" -> "jdbc:sqlite:" + new File(plugin.getDataFolder(),
                    config.getString("storage.database.file", "data.db")).getPath();
            case "h2" -> "jdbc:h2:file:" + new File(plugin.getDataFolder(),
                    config.getString("storage.database.file", "data")).getPath() + ";AUTO_SERVER=TRUE";
            case "mysql" -> {
                var host = config.getString("storage.database.host", "localhost");
                var port = config.getInt("storage.database.port", 3306);
                var database = config.getString("storage.database.name", "plugincontrol");
                yield "jdbc:mysql://%s:%d/%s?useSSL=false&characterEncoding=utf8"
                        .formatted(host, port, database);
            }
            default -> throw new IllegalArgumentException("Unsupported data storage: " + type);
        };
    }

    private void createTables() throws SQLException {
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS plugincontrol_plugins (
                        name VARCHAR(255) PRIMARY KEY
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS plugincontrol_groups (
                        name VARCHAR(255) NOT NULL,
                        plugin VARCHAR(255) NOT NULL,
                        PRIMARY KEY (name, plugin)
                    )
                    """);
        }
    }

    @Override
    public DataSnapshot load() {
        var plugins = new HashSet<String>();
        var groups = new HashMap<String, Set<String>>();
        try (var connection = dataSource.getConnection();
             var pluginsStatement = connection.createStatement();
             var pluginsResult = pluginsStatement.executeQuery("SELECT name FROM plugincontrol_plugins");
             var groupsStatement = connection.createStatement();
             var groupsResult = groupsStatement.executeQuery("SELECT name, plugin FROM plugincontrol_groups")) {
            while (pluginsResult.next()) {
                plugins.add(pluginsResult.getString("name"));
            }
            while (groupsResult.next()) {
                groups.computeIfAbsent(groupsResult.getString("name"), ignored -> new HashSet<>())
                        .add(groupsResult.getString("plugin"));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not load plugin data", exception);
        }
        return new DataSnapshot(plugins, groups);
    }

    @Override
    public void save(Set<String> plugins, Map<String, Set<String>> groups) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (var clearPlugins = connection.createStatement();
                 var clearGroups = connection.createStatement()) {
                clearPlugins.executeUpdate("DELETE FROM plugincontrol_plugins");
                clearGroups.executeUpdate("DELETE FROM plugincontrol_groups");
            }
            try (var pluginStatement = connection.prepareStatement("INSERT INTO plugincontrol_plugins (name) VALUES (?)");
                 var groupStatement = connection.prepareStatement("INSERT INTO plugincontrol_groups (name, plugin) VALUES (?, ?)")) {
                for (var plugin : plugins) {
                    pluginStatement.setString(1, plugin);
                    pluginStatement.addBatch();
                }
                for (var group : groups.entrySet()) {
                    for (var plugin : group.getValue()) {
                        groupStatement.setString(1, group.getKey());
                        groupStatement.setString(2, plugin);
                        groupStatement.addBatch();
                    }
                }
                pluginStatement.executeBatch();
                groupStatement.executeBatch();
            }
            connection.commit();
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not save plugin data", exception);
        }
    }

    @Override
    public void close() {
        dataSource.close();
    }
}
