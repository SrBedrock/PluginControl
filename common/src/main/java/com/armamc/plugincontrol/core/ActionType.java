package com.armamc.plugincontrol.core;

import java.util.Arrays;
import java.util.Locale;

/** Platform-independent action identifiers shared by Bukkit and proxies. */
public enum ActionType {
    LOG_TO_CONSOLE("log-to-console"),
    DISALLOW_PLAYER_LOGIN("disallow-player-login"),
    SHUTDOWN_SERVER("shutdown-server");

    private final String key;

    ActionType(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    public static ActionType parse(String key) {
        if (key == null) throw new IllegalArgumentException("Missing PluginControl action");
        return Arrays.stream(values())
                .filter(action -> action.key.equals(key.toLowerCase(Locale.ROOT)))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown action: " + key));
    }
}
