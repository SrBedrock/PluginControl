package com.armamc.plugincontrol.core;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public record RuleSnapshot(Set<String> plugins, Map<String, Set<String>> groups) {
    public RuleSnapshot {
        plugins = Set.copyOf(plugins);
        final Map<String, Set<String>> copy = new LinkedHashMap<>();
        groups.forEach((name, members) -> copy.put(name, Set.copyOf(members)));
        groups = Map.copyOf(copy);
    }

    public static RuleSnapshot empty() {
        return new RuleSnapshot(Set.of(), Map.of());
    }

    public Mutable mutableCopy() {
        final Map<String, Set<String>> copy = new LinkedHashMap<>();
        groups.forEach((name, members) -> copy.put(name, new LinkedHashSet<>(members)));
        return new Mutable(new LinkedHashSet<>(plugins), copy);
    }

    public record Mutable(Set<String> plugins, Map<String, Set<String>> groups) {
        public RuleSnapshot snapshot() {
            return new RuleSnapshot(plugins, groups);
        }
    }
}
