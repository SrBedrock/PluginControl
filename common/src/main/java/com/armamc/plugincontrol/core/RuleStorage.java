package com.armamc.plugincontrol.core;

public interface RuleStorage extends AutoCloseable {
    RuleSnapshot load();
    void save(RuleSnapshot snapshot);

    @Override
    default void close() {
    }
}
