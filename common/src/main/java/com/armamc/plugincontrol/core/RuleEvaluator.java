package com.armamc.plugincontrol.core;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/** Required plugins use AND; each non-empty alternative group uses OR. */
public final class RuleEvaluator {
    private RuleEvaluator() {
    }

    public record Result(Set<String> missingPlugins, Set<String> missingGroups) {
        public Result {
            missingPlugins = Set.copyOf(missingPlugins);
            missingGroups = Set.copyOf(missingGroups);
        }

        public boolean hasMissingRequirements() {
            return !missingPlugins.isEmpty() || !missingGroups.isEmpty();
        }
    }

    public static Result evaluate(Set<String> plugins, Map<String, Set<String>> groups,
                                  Predicate<String> isEnabled) {
        Objects.requireNonNull(isEnabled, "isEnabled");
        final Set<String> missingPlugins = new LinkedHashSet<>();
        for (String name : plugins) {
            if (!isEnabled.test(name)) missingPlugins.add(name);
        }
        final Set<String> missingGroups = new LinkedHashSet<>();
        for (var group : groups.entrySet()) {
            // An empty group is not a requirement, matching the existing Bukkit behavior.
            if (!group.getValue().isEmpty() && group.getValue().stream().noneMatch(isEnabled)) {
                missingGroups.add(group.getKey());
            }
        }
        return new Result(missingPlugins, missingGroups);
    }
}
