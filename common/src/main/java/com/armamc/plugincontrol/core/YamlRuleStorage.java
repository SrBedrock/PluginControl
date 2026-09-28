package com.armamc.plugincontrol.core;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class YamlRuleStorage implements RuleStorage {
    private final Path path;

    public YamlRuleStorage(Path directory) {
        path = directory.resolve("data.yml");
    }

    @Override
    public RuleSnapshot load() {
        final Map<String, Object> yaml = YamlFiles.load(path);
        final Set<String> plugins = strings(yaml.get("plugins"));
        final Map<String, Set<String>> groups = new LinkedHashMap<>();
        YamlFiles.section(yaml, "groups").forEach((name, members) -> groups.put(name, strings(members)));
        return new RuleSnapshot(plugins, groups);
    }

    @Override
    public void save(RuleSnapshot snapshot) {
        final Map<String, Object> yaml = new LinkedHashMap<>();
        yaml.put("plugins", snapshot.plugins().stream().sorted(String.CASE_INSENSITIVE_ORDER).toList());
        final Map<String, Object> groups = new LinkedHashMap<>();
        snapshot.groups().entrySet().stream().sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                .forEach(group -> groups.put(group.getKey(),
                        group.getValue().stream().sorted(String.CASE_INSENSITIVE_ORDER).toList()));
        yaml.put("groups", groups);
        YamlFiles.save(path, yaml);
    }

    private static Set<String> strings(Object value) {
        final Set<String> result = new LinkedHashSet<>();
        if (value instanceof Iterable<?> values) {
            for (Object element : values) {
                if (element instanceof String name && !name.isBlank()) result.add(name);
            }
        }
        return result;
    }
}
