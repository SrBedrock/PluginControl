package com.armamc.plugincontrol.core;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RuleEvaluatorTest {
    @Test
    void groupsAreAlternativesWhileStandalonePluginsAreRequired() {
        final var result = RuleEvaluator.evaluate(Set.of("LuckPerms", "EssentialsX"),
                Map.of("economy", Set.of("Vault", "PlayerPoints"),
                        "unused", Set.of()), name -> name.equals("LuckPerms") || name.equals("PlayerPoints"));
        assertEquals(Set.of("EssentialsX"), result.missingPlugins());
        assertTrue(result.missingGroups().isEmpty());
        assertTrue(result.hasMissingRequirements());
    }

    @Test
    void emptyGroupsAreIgnoredAndUnsatisfiedGroupsReported() {
        final var result = RuleEvaluator.evaluate(Set.of(),
                Map.of("economy", Set.of("Vault", "PlayerPoints"), "empty", Set.of()),
                name -> false);
        assertEquals(Set.of("economy"), result.missingGroups());
    }

    @Test
    void allRequirementsSatisfied() {
        assertFalse(RuleEvaluator.evaluate(Set.of("A"),
                Map.of("one-of", Set.of("B", "C")), Set.of("A", "C")::contains)
                .hasMissingRequirements());
    }
}
