package com.armamc.plugincontrol.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProxyControlServiceTest {
    @TempDir
    Path directory;

    @Test
    void missingRequiredPluginBlocksLoginAndDisableClearsTheBlock() {
        YamlFiles.copyDefault(directory.resolve("config.yml"), "config.yml");
        final Map<String, Object> config = YamlFiles.load(directory.resolve("config.yml"));
        config.put("enabled", true);
        config.put("action", "disallow-player-login");
        YamlFiles.save(directory.resolve("config.yml"), config);
        new YamlRuleStorage(directory).save(new RuleSnapshot(Set.of("LuckPerms"),
                Map.of("economy", Set.of("Vault", "PlayerPoints"))));

        final StubPlatform platform = new StubPlatform();
        try (ProxyControlService control = new ProxyControlService(directory, platform)) {
            control.check();
            assertTrue(control.isLoginBlocked());
            assertTrue(platform.blocked);
            assertTrue(platform.messages.stream().anyMatch(message -> message.contains("LuckPerms")));
            control.execute(new StubActor(), "plugincontrol", new String[]{"disable"});
            assertFalse(control.isLoginBlocked());
            assertFalse(platform.blocked);
        }
    }

    @Test
    void commandsPersistRulesAcrossRestart() {
        final StubPlatform platform = new StubPlatform();
        try (ProxyControlService control = new ProxyControlService(directory, platform)) {
            final StubActor actor = new StubActor();
            control.execute(actor, "pc", new String[]{"add", "LuckPerms"});
            control.execute(actor, "pc", new String[]{"group", "create", "economy"});
            control.execute(actor, "pc", new String[]{"group", "add", "economy", "Vault"});
            assertFalse(actor.messages.isEmpty());
        }
        final RuleSnapshot snapshot = new YamlRuleStorage(directory).load();
        assertEquals(Set.of("LuckPerms"), snapshot.plugins());
        assertEquals(Set.of("Vault"), snapshot.groups().get("economy"));
    }

    private static final class StubActor implements ProxyControlService.Actor {
        private final List<String> messages = new ArrayList<>();
        @Override public boolean permitted() { return true; }
        @Override public void send(String message) { messages.add(message); }
    }

    private static final class StubPlatform implements ProxyControlService.Platform {
        private final List<String> messages = new ArrayList<>();
        private boolean blocked;
        @Override public List<ProxyControlService.PluginInfo> plugins() { return List.of(); }
        @Override public boolean isEnabled(String plugin) { return false; }
        @Override public void notifyStaff(String message) { messages.add(message); }
        @Override public void blockLogin(boolean blocked, String message) { this.blocked = blocked; }
        @Override public void shutdown() { fail("Unexpected proxy shutdown"); }
        @Override public void logError(String message, Throwable error) { fail(message, error); }
    }
}
