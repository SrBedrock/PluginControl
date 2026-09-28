package com.armamc.plugincontrol.storage;

import java.util.Map;
import java.util.Set;

public interface DataStorage extends AutoCloseable {
    DataSnapshot load();

    void save(Set<String> plugins, Map<String, Set<String>> groups);

    @Override
    default void close() {
    }

    record DataSnapshot(Set<String> plugins, Map<String, Set<String>> groups) {
    }
}
