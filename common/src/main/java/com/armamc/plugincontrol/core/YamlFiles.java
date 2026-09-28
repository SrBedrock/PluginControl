package com.armamc.plugincontrol.core;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

/** Safe YAML parsing and atomic writes shared by proxy configuration and rule storage. */
public final class YamlFiles {
    private YamlFiles() {
    }

    public static void copyDefault(Path file, String resource) {
        try {
            Files.createDirectories(file.getParent());
            if (Files.exists(file)) return;
            try (InputStream input = YamlFiles.class.getResourceAsStream("/" + resource)) {
                if (input == null) throw new IllegalStateException("Missing resource " + resource);
                Files.copy(input, file);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create " + file, exception);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> load(Path path) {
        if (!Files.exists(path)) return new LinkedHashMap<>();
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            final Object value = new Yaml(new SafeConstructor(new LoaderOptions())).load(reader);
            if (value == null) return new LinkedHashMap<>();
            if (!(value instanceof Map<?, ?> map)) {
                throw new IllegalArgumentException("Expected a YAML mapping in " + path);
            }
            final Map<String, Object> copy = new LinkedHashMap<>();
            map.forEach((key, entry) -> copy.put(String.valueOf(key), entry));
            return copy;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read " + path, exception);
        }
    }

    public static void save(Path path, Map<String, ?> data) {
        final DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        final Path temp = path.resolveSibling(path.getFileName() + ".tmp");
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(temp, new Yaml(options).dump(data), StandardCharsets.UTF_8);
            try {
                Files.move(temp, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save " + path, exception);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> section(Map<String, Object> map, String key) {
        return map.get(key) instanceof Map<?, ?> value ? (Map<String, Object>) value : new LinkedHashMap<>();
    }

    public static String text(Map<String, Object> map, String key, String fallback) {
        final Object value = map.get(key);
        return value == null ? fallback : String.valueOf(value);
    }

    public static boolean flag(Map<String, Object> map, String key, boolean fallback) {
        final Object value = map.get(key);
        return value == null ? fallback : Boolean.parseBoolean(String.valueOf(value));
    }

    public static int number(Map<String, Object> map, String key, int fallback) {
        final Object value = map.get(key);
        if (value == null) return fallback;
        return value instanceof Number number ? number.intValue() : Integer.parseInt(String.valueOf(value));
    }
}
