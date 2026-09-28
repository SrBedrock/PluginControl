package com.armamc.plugincontrol.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JdbcRuleStorageTest {
    @TempDir Path directory;

    @Test
    void h2PersistsStandaloneRulesAndEmptyGroups() {
        final RuleSnapshot snapshot = new RuleSnapshot(Set.of("LuckPerms"),
                Map.of("economy", Set.of("Vault", "PlayerPoints"), "empty", Set.of()));
        try (RuleStorage storage = new JdbcRuleStorage(directory, "h2",
                Map.of("database", Map.of("file", "requirements")))) {
            storage.save(snapshot);
            assertEquals(snapshot, storage.load());
        }
        try (RuleStorage reloaded = new JdbcRuleStorage(directory, "h2",
                Map.of("database", Map.of("file", "requirements")))) {
            assertEquals(snapshot, reloaded.load());
        }
    }
}
