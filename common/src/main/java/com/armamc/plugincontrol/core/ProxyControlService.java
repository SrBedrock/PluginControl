package com.armamc.plugincontrol.core;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Shared proxy application service. Platform-specific bootstraps only supply plugin state,
 * audiences, lifecycle and commands. No Bukkit, Velocity or Bungee APIs live here.
 */
public final class ProxyControlService implements AutoCloseable {
    public record PluginInfo(String name, Set<String> required, Set<String> optional) {
    }

    public interface Platform {
        List<PluginInfo> plugins();
        boolean isEnabled(String plugin);
        void notifyStaff(String miniMessage);
        void blockLogin(boolean blocked, String kickMiniMessage);
        void shutdown();
        void logError(String message, Throwable error);
    }

    public interface Actor {
        boolean permitted();
        void send(String miniMessage);
    }

    private final Path directory;
    private final Platform platform;
    private final ExecutorService writes = Executors.newSingleThreadExecutor(r -> {
        final Thread thread = new Thread(r, "PluginControl-proxy-storage");
        thread.setDaemon(true);
        return thread;
    });
    private Map<String, Object> config;
    private Map<String, Object> lang;
    private RuleStorage storage;
    private RuleSnapshot.Mutable rules;
    private volatile boolean blocked;
    private volatile String kickMessage;
    private boolean closed;

    public ProxyControlService(Path directory, Platform platform) {
        this.directory = directory;
        this.platform = platform;
        reloadFiles();
    }

    private void reloadFiles() {
        YamlFiles.copyDefault(directory.resolve("config.yml"), "config.yml");
        YamlFiles.copyDefault(directory.resolve("lang.yml"), "lang.yml");
        final Map<String, Object> nextConfig = YamlFiles.load(directory.resolve("config.yml"));
        final Map<String, Object> nextLang = YamlFiles.load(directory.resolve("lang.yml"));
        final Map<String, Object> storageSettings = YamlFiles.section(nextConfig, "storage");
        final String type = YamlFiles.text(storageSettings, "type", "yaml").toLowerCase(Locale.ROOT);
        final RuleStorage nextStorage = switch (type) {
            case "yaml" -> new YamlRuleStorage(directory);
            case "h2", "sqlite", "mysql" -> new JdbcRuleStorage(directory, type, storageSettings);
            default -> throw new IllegalArgumentException("Unsupported PluginControl storage: " + type);
        };
        try {
            RuleSnapshot nextRules = nextStorage.load();
            if (nextRules.plugins().isEmpty() && nextRules.groups().isEmpty() &&
                    (nextConfig.containsKey("plugins") || nextConfig.containsKey("groups"))) {
                final Set<String> plugins = names(nextConfig.get("plugins"));
                final Map<String, Set<String>> groups = new LinkedHashMap<>();
                YamlFiles.section(nextConfig, "groups").forEach((name, entries) -> groups.put(name, names(entries)));
                nextRules = new RuleSnapshot(plugins, groups);
                nextStorage.save(nextRules);
                nextConfig.remove("plugins");
                nextConfig.remove("groups");
                YamlFiles.save(directory.resolve("config.yml"), nextConfig);
            }
            if (storage != null) storage.close();
            storage = nextStorage;
            rules = nextRules.mutableCopy();
            config = nextConfig;
            lang = nextLang;
            kickMessage = raw("kick-message",
                    "<red>[PluginControl] You are not allowed to join the server!");
        } catch (RuntimeException exception) {
            nextStorage.close();
            throw exception;
        }
    }

    private static Set<String> names(Object value) {
        final Set<String> result = new LinkedHashSet<>();
        if (value instanceof Iterable<?> items) {
            for (Object item : items) if (item instanceof String s && !s.isBlank()) result.add(s);
        }
        return result;
    }

    private void persistRules() {
        final RuleSnapshot snapshot = rules.snapshot();
        writes.execute(() -> {
            try {
                storage.save(snapshot);
            } catch (RuntimeException exception) {
                platform.logError("Could not save PluginControl requirements", exception);
            }
        });
    }

    private void saveConfig() {
        YamlFiles.save(directory.resolve("config.yml"), config);
    }

    private void saveLang() {
        YamlFiles.save(directory.resolve("lang.yml"), lang);
    }

    private void flushWrites() {
        try {
            final Future<?> future = writes.submit(() -> { });
            future.get();
        } catch (Exception exception) {
            throw new IllegalStateException("Could not finish saving PluginControl requirements", exception);
        }
    }

    private boolean enabled() {
        return YamlFiles.flag(config, "enabled", false);
    }

    private ActionType action() {
        return ActionType.parse(YamlFiles.text(config, "action", "log-to-console"));
    }

    public boolean isLoginBlocked() {
        return blocked;
    }

    public String kickMessage() {
        return kickMessage;
    }

    public void check() {
        if (!enabled()) {
            clearBlock();
            platform.notifyStaff(message("console.plugin-disabled"));
            return;
        }
        platform.notifyStaff(message("console.checking-plugins"));
        final RuleEvaluator.Result missing =
                RuleEvaluator.evaluate(rules.plugins(), rules.groups(), platform::isEnabled);
        if (!missing.hasMissingRequirements()) {
            clearBlock();
            platform.notifyStaff(message("console.finished-checking"));
            return;
        }
        if (!missing.missingPlugins().isEmpty()) {
            platform.notifyStaff(message("console.log-to-console-plugin",
                    Map.of("plugins", joined(missing.missingPlugins()))));
        }
        if (!missing.missingGroups().isEmpty()) {
            platform.notifyStaff(message("console.log-to-console-group",
                    Map.of("groups", joined(missing.missingGroups()))));
        }
        if (action() == ActionType.DISALLOW_PLAYER_LOGIN) {
            blocked = true;
            platform.blockLogin(true, kickMessage);
        } else {
            clearBlock();
            if (action() == ActionType.SHUTDOWN_SERVER) {
                platform.notifyStaff(message("console.disabling-server"));
                platform.shutdown();
            }
        }
        platform.notifyStaff(message("console.finished-checking"));
    }

    private void clearBlock() {
        blocked = false;
        platform.blockLogin(false, kickMessage);
    }

    private static String joined(Collection<String> names) {
        return names.stream().sorted(String.CASE_INSENSITIVE_ORDER)
                .map(ProxyControlService::literal).collect(Collectors.joining(", "));
    }

    /** Escape externally sourced names before inserting them into MiniMessage templates. */
    private static String literal(String input) {
        return input.replace("\\", "\\\\").replace("<", "\\<");
    }

    private String raw(String path, String fallback) {
        final String[] keys = path.split("\\.");
        Map<String, Object> current = lang;
        for (int index = 0; index < keys.length - 1; index++) current = YamlFiles.section(current, keys[index]);
        return YamlFiles.text(current, keys[keys.length - 1], fallback);
    }

    private List<String> lines(String path) {
        Map<String, Object> current = lang;
        final String[] keys = path.split("\\.");
        for (int i = 0; i < keys.length - 1; i++) current = YamlFiles.section(current, keys[i]);
        final Object value = current.get(keys[keys.length - 1]);
        if (!(value instanceof List<?> entries)) return List.of(message(path));
        return entries.stream().map(String::valueOf).toList();
    }

    private String message(String path) {
        return message(path, Map.of());
    }

    private String message(String path, Map<String, String> tags) {
        String value = raw(path, "<red>Missing PluginControl translation: " + path);
        value = value.replace("<prefix>", raw("prefix", "<dark_gray>[<red>PluginControl<dark_gray>]"));
        for (var entry : tags.entrySet()) value = value.replace("<" + entry.getKey() + ">", entry.getValue());
        return value;
    }

    private static String joinedArgs(String[] args, int start) {
        return String.join(" ", Arrays.copyOfRange(args, start, args.length));
    }

    private void send(Actor actor, String path, Map<String, String> tags) {
        actor.send(message(path, tags));
    }

    private void send(Actor actor, String path) {
        actor.send(message(path));
    }

    private void help(Actor actor, String label, boolean groups) {
        final String prefix = raw("prefix", "<dark_gray>[<red>PluginControl<dark_gray>]");
        for (String line : lines(groups ? "command.group-help" : "command.help")) {
            actor.send(line.replace("<prefix>", prefix).replace("<command>", literal(label)));
        }
    }

    public List<String> suggestions(String[] args) {
        if (args.length == 0) return List.of();
        final String first = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 1) return starts(List.of("add", "remove", "list", "group", "action", "kick-message",
                "enable", "disable", "toggle", "check", "check-depend", "reload", "help"), first);
        if (args.length == 2) {
            return switch (first) {
                case "action" -> starts(Arrays.stream(ActionType.values()).map(ActionType::key).toList(), args[1]);
                case "add", "check-depend" -> starts(platform.plugins().stream().map(PluginInfo::name).toList(), args[1]);
                case "remove" -> starts(rules.plugins(), args[1]);
                case "group" -> starts(List.of("create", "delete", "list", "add", "remove", "help"), args[1]);
                default -> List.of();
            };
        }
        if ("group".equals(first)) {
            if (args.length == 3) return starts(rules.groups().keySet(), args[2]);
            if (args.length == 4 && "add".equalsIgnoreCase(args[1]))
                return starts(platform.plugins().stream().map(PluginInfo::name).toList(), args[3]);
            if (args.length == 4 && "remove".equalsIgnoreCase(args[1]))
                return starts(rules.groups().getOrDefault(args[2], Set.of()), args[3]);
        }
        return List.of();
    }

    private static List<String> starts(Collection<String> values, String input) {
        return values.stream().filter(s -> s.regionMatches(true, 0, input, 0, input.length()))
                .sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }

    public void execute(Actor actor, String label, String[] args) {
        if (!actor.permitted()) {
            actor.send("<red>You do not have permission to use PluginControl.");
            return;
        }
        if (args.length == 0) {
            help(actor, label, false);
            return;
        }
        final String operation = args[0].toLowerCase(Locale.ROOT);
        final Map<String, String> usage = Map.of("command", literal(label));
        switch (operation) {
            case "help" -> help(actor, label, false);
            case "check" -> {
                check();
                send(actor, "command.checking-plugins");
            }
            case "enable", "disable", "toggle" -> {
                final boolean next = operation.equals("enable") || operation.equals("toggle") && !enabled();
                config.put("enabled", next);
                saveConfig();
                if (next) check();
                else clearBlock();
                send(actor, next ? "command.plugin-enabled" : "command.plugin-disabled");
            }
            case "list" -> {
                if (rules.plugins().isEmpty()) send(actor, "command.plugin-list-empty");
                else send(actor, "command.plugin-list", Map.of("plugins", joined(rules.plugins())));
            }
            case "add" -> {
                if (args.length < 2) { send(actor, "command.plugin-add-error", usage); break; }
                if (args[1].equalsIgnoreCase("all")) {
                    platform.plugins().forEach(plugin -> rules.plugins().add(plugin.name()));
                    persistRules();
                    send(actor, "command.plugin-added-all");
                } else if (rules.plugins().add(args[1])) {
                    persistRules();
                    send(actor, "command.plugin-added", Map.of("plugin", literal(args[1])));
                } else send(actor, "command.plugin-already-added");
            }
            case "remove" -> {
                if (args.length < 2) { send(actor, "command.plugin-remove-error", usage); break; }
                if (args[1].equalsIgnoreCase("all")) {
                    rules.plugins().clear();
                    persistRules();
                    send(actor, "command.plugin-removed-all");
                } else if (rules.plugins().removeIf(name -> name.equalsIgnoreCase(args[1]))) {
                    persistRules();
                    send(actor, "command.plugin-removed", Map.of("plugin", literal(args[1])));
                } else send(actor, "command.plugin-not-found", Map.of("plugin", literal(args[1])));
            }
            case "action" -> {
                if (args.length == 1) {
                    send(actor, "command.action-type", Map.of("action", action().key()));
                    break;
                }
                try {
                    final ActionType next = ActionType.parse(args[1]);
                    config.put("action", next.key());
                    saveConfig();
                    send(actor, "command.action-set", Map.of("action", next.key()));
                    check();
                } catch (IllegalArgumentException exception) {
                    send(actor, "command.action-list", Map.of("actions",
                            Arrays.stream(ActionType.values()).map(ActionType::key).collect(Collectors.joining(", "))));
                }
            }
            case "kick-message" -> {
                if (args.length == 1) send(actor, "command.kick-message", Map.of("kick-message", kickMessage));
                else {
                    kickMessage = joinedArgs(args, 1);
                    lang.put("kick-message", kickMessage);
                    saveLang();
                    send(actor, "command.kick-message-set", Map.of("kick-message", kickMessage));
                }
            }
            case "reload" -> {
                flushWrites();
                reloadFiles();
                check();
                send(actor, "command.plugin-reload");
            }
            case "check-depend" -> checkDepend(actor, label, args);
            case "group" -> group(actor, label, args);
            default -> help(actor, label, false);
        }
    }

    private void checkDepend(Actor actor, String label, String[] args) {
        if (args.length < 2) { send(actor, "command.check-depend-error", Map.of("command", literal(label))); return; }
        final String target = args[1];
        final Set<String> required = new LinkedHashSet<>();
        final Set<String> optional = new LinkedHashSet<>();
        for (PluginInfo plugin : platform.plugins()) {
            if (plugin.name().equalsIgnoreCase(target)) continue;
            if (plugin.required().stream().anyMatch(name -> name.equalsIgnoreCase(target))) required.add(plugin.name());
            if (plugin.optional().stream().anyMatch(name -> name.equalsIgnoreCase(target))) optional.add(plugin.name());
        }
        final String safe = literal(target);
        if (required.isEmpty() && optional.isEmpty()) {
            send(actor, "command.check-depend-not-found", Map.of("plugin", safe));
        } else {
            if (!required.isEmpty()) send(actor, "command.check-depend-depend",
                    Map.of("plugin", safe, "plugins", joined(required)));
            if (!optional.isEmpty()) send(actor, "command.check-depend-softdepend",
                    Map.of("plugin", safe, "plugins", joined(optional)));
        }
    }

    private void group(Actor actor, String label, String[] args) {
        if (args.length < 2 || args[1].equalsIgnoreCase("help")) { help(actor, label, true); return; }
        final String command = args[1].toLowerCase(Locale.ROOT);
        if (command.equals("list") && args.length == 2) {
            if (rules.groups().isEmpty()) send(actor, "command.group-list-empty");
            else {
                final String display = rules.groups().entrySet().stream()
                        .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                        .map(group -> literal(group.getKey()) + " [" + joined(group.getValue()) + "]")
                        .collect(Collectors.joining(", "));
                send(actor, "command.group-list", Map.of("groups", display));
            }
            return;
        }
        if (args.length < 3 || args[2].isBlank()) { help(actor, label, true); return; }
        final String name = args[2];
        final String safe = literal(name);
        final Set<String> members = rules.groups().get(name);
        switch (command) {
            case "create" -> {
                if (members != null) send(actor, "command.group-already-exist");
                else {
                    rules.groups().put(name, new LinkedHashSet<>());
                    persistRules();
                    send(actor, "command.group-created", Map.of("group", safe));
                }
            }
            case "delete" -> {
                if (rules.groups().remove(name) != null) {
                    persistRules();
                    send(actor, "command.group-removed", Map.of("group", safe));
                } else send(actor, "command.group-not-found", Map.of("group", safe));
            }
            case "list" -> {
                if (members == null) send(actor, "command.group-not-found", Map.of("group", safe));
                else if (members.isEmpty()) send(actor, "command.group-has-no-plugins", Map.of("group", safe));
                else send(actor, "command.group-plugin-list",
                        Map.of("group", safe, "plugins", joined(members)));
            }
            case "add", "remove" -> {
                if (args.length < 4) {
                    send(actor, command.equals("add") ? "command.plugin-add-to-group-error" :
                            "command.plugin-removed-from-group-error", Map.of("command", literal(label)));
                    return;
                }
                if (members == null) {
                    send(actor, "command.group-not-found", Map.of("group", safe));
                    return;
                }
                final String plugin = args[3];
                final String safePlugin = literal(plugin);
                final boolean changed = command.equals("add") ? members.add(plugin) :
                        members.removeIf(item -> item.equalsIgnoreCase(plugin));
                if (changed) persistRules();
                final String key = command.equals("add") ?
                        (changed ? "command.plugin-added-to-group" : "command.plugin-add-to-group-error") :
                        (changed ? "command.plugin-removed-from-group" : "command.plugin-not-in-group");
                send(actor, key, Map.of("group", safe, "plugin", safePlugin));
            }
            default -> help(actor, label, true);
        }
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        clearBlock();
        flushWrites();
        writes.shutdown();
        storage.close();
    }
}
