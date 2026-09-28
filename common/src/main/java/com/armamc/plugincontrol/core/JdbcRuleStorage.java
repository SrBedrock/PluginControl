package com.armamc.plugincontrol.core;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Shares the existing Bukkit table schema for H2, SQLite and MySQL proxy storage. */
public final class JdbcRuleStorage implements RuleStorage {
    private final HikariDataSource dataSource;

    public JdbcRuleStorage(Path directory, String type, Map<String, Object> settings) {
        final Map<String, Object> database = YamlFiles.section(settings, "database");
        final HikariConfig pool = new HikariConfig();
        pool.setPoolName("PluginControl-" + type);
        pool.setMaximumPoolSize(Math.max(1, YamlFiles.number(database, "pool-size", 5)));
        pool.setMinimumIdle(1);
        switch (type) {
            case "sqlite" -> {
                pool.setDriverClassName("org.sqlite.JDBC");
                pool.setJdbcUrl("jdbc:sqlite:" + directory.resolve(
                        YamlFiles.text(database, "file", "data.db")).toAbsolutePath());
                pool.setMaximumPoolSize(1);
            }
            case "h2" -> {
                pool.setDriverClassName("org.h2.Driver");
                pool.setJdbcUrl("jdbc:h2:file:" + directory.resolve(
                        YamlFiles.text(database, "file", "data")).toAbsolutePath() + ";AUTO_SERVER=TRUE");
            }
            case "mysql" -> {
                pool.setDriverClassName("com.mysql.cj.jdbc.Driver");
                final boolean ssl = YamlFiles.flag(database, "use-ssl", true);
                final boolean requireSsl = YamlFiles.flag(database, "require-ssl", ssl);
                final boolean verify = YamlFiles.flag(database, "verify-server-certificate", ssl);
                pool.setJdbcUrl("jdbc:mysql://%s:%d/%s?useSSL=%s&requireSSL=%s&verifyServerCertificate=%s&characterEncoding=utf8"
                        .formatted(YamlFiles.text(database, "host", "localhost"),
                                YamlFiles.number(database, "port", 3306),
                                YamlFiles.text(database, "name", "plugincontrol"), ssl, requireSsl, verify));
                pool.setUsername(YamlFiles.text(database, "username", "root"));
                pool.setPassword(YamlFiles.text(database, "password", ""));
            }
            default -> throw new IllegalArgumentException("Unsupported data storage: " + type);
        }
        dataSource = new HikariDataSource(pool);
        try (Connection connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS plugincontrol_plugins (name VARCHAR(255) PRIMARY KEY)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS plugincontrol_group_names (name VARCHAR(255) PRIMARY KEY)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS plugincontrol_groups (name VARCHAR(255) NOT NULL, plugin VARCHAR(255) NOT NULL, PRIMARY KEY (name, plugin))");
        } catch (SQLException exception) {
            dataSource.close();
            throw new IllegalStateException("Could not initialize PluginControl database", exception);
        }
    }

    @Override
    public RuleSnapshot load() {
        final Set<String> plugins = new LinkedHashSet<>();
        final Map<String, Set<String>> groups = new LinkedHashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            try (var stmt = connection.createStatement();
                 var result = stmt.executeQuery("SELECT name FROM plugincontrol_plugins")) {
                while (result.next()) plugins.add(result.getString(1));
            }
            try (var stmt = connection.createStatement();
                 var result = stmt.executeQuery("SELECT name FROM plugincontrol_group_names")) {
                while (result.next()) groups.put(result.getString(1), new LinkedHashSet<>());
            }
            try (var stmt = connection.createStatement();
                 var result = stmt.executeQuery("SELECT name, plugin FROM plugincontrol_groups")) {
                while (result.next()) groups.computeIfAbsent(result.getString(1),
                        key -> new LinkedHashSet<>()).add(result.getString(2));
            }
            return new RuleSnapshot(plugins, groups);
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not load PluginControl requirements", exception);
        }
    }

    @Override
    public void save(RuleSnapshot snapshot) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (var statement = connection.createStatement()) {
                    statement.executeUpdate("DELETE FROM plugincontrol_groups");
                    statement.executeUpdate("DELETE FROM plugincontrol_group_names");
                    statement.executeUpdate("DELETE FROM plugincontrol_plugins");
                }
                try (var plugins = connection.prepareStatement("INSERT INTO plugincontrol_plugins (name) VALUES (?)");
                     var names = connection.prepareStatement("INSERT INTO plugincontrol_group_names (name) VALUES (?)");
                     var groups = connection.prepareStatement("INSERT INTO plugincontrol_groups (name, plugin) VALUES (?, ?)")) {
                    for (String plugin : snapshot.plugins()) {
                        plugins.setString(1, plugin);
                        plugins.addBatch();
                    }
                    for (var group : snapshot.groups().entrySet()) {
                        names.setString(1, group.getKey());
                        names.addBatch();
                        for (String plugin : group.getValue()) {
                            groups.setString(1, group.getKey());
                            groups.setString(2, plugin);
                            groups.addBatch();
                        }
                    }
                    plugins.executeBatch();
                    names.executeBatch();
                    groups.executeBatch();
                }
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not persist PluginControl requirements", exception);
        }
    }

    @Override
    public void close() {
        dataSource.close();
    }
}
