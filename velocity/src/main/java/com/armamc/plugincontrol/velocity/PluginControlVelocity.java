package com.armamc.plugincontrol.velocity;

import com.armamc.plugincontrol.core.ProxyControlService;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;

import com.google.inject.Inject;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Plugin(id = "plugincontrol", name = "PluginControl", version = "1.3.0", authors = {"ThiagoROX"})
public final class PluginControlVelocity {
    private final ProxyServer server;
    private final Logger logger;
    private final Path directory;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private volatile ProxyControlService control;

    @Inject
    public PluginControlVelocity(ProxyServer server, Logger logger, @DataDirectory Path directory) {
        this.server = server;
        this.logger = logger;
        this.directory = directory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        control = new ProxyControlService(directory, new ProxyControlService.Platform() {
            @Override
            public List<ProxyControlService.PluginInfo> plugins() {
                return server.getPluginManager().getPlugins().stream().map(plugin -> {
                    final var description = plugin.getDescription();
                    final Set<String> required = description.getDependencies().stream()
                            .filter(dependency -> !dependency.isOptional())
                            .map(dependency -> dependency.getId()).collect(Collectors.toSet());
                    final Set<String> optional = description.getDependencies().stream()
                            .filter(dependency -> dependency.isOptional())
                            .map(dependency -> dependency.getId()).collect(Collectors.toSet());
                    return new ProxyControlService.PluginInfo(description.getId(), required, optional);
                }).toList();
            }

            @Override
            public boolean isEnabled(String plugin) {
                return server.getPluginManager().getPlugin(plugin.toLowerCase(Locale.ROOT))
                        .map(container -> container.getInstance().isPresent()).orElse(false);
            }

            @Override
            public void notifyStaff(String message) {
                final var component = miniMessage.deserialize(message);
                server.getConsoleCommandSource().sendMessage(component);
                server.getAllPlayers().stream().filter(player -> player.hasPermission("plugincontrol.notify"))
                        .forEach(player -> player.sendMessage(component));
            }

            @Override
            public void blockLogin(boolean blocked, String kickMessage) {
                if (!blocked) return;
                final var component = miniMessage.deserialize(kickMessage);
                server.getAllPlayers().stream()
                        .filter(player -> !player.hasPermission("plugincontrol.bypass"))
                        .forEach(player -> player.disconnect(component));
            }

            @Override
            public void shutdown() {
                server.shutdown();
            }

            @Override
            public void logError(String message, Throwable error) {
                logger.error(message, error);
            }
        });

        server.getCommandManager().register(
                server.getCommandManager().metaBuilder("plugincontrol")
                        .aliases("pc", "plcontrol").plugin(this).build(),
                new SimpleCommand() {
                    @Override
                    public void execute(Invocation invocation) {
                        control.execute(new ProxyControlService.Actor() {
                            @Override
                            public boolean permitted() {
                                return invocation.source().hasPermission("plugincontrol.use");
                            }

                            @Override
                            public void send(String message) {
                                invocation.source().sendMessage(miniMessage.deserialize(message));
                            }
                        }, invocation.alias(), invocation.arguments());
                    }

                    @Override
                    public List<String> suggest(Invocation invocation) {
                        return invocation.source().hasPermission("plugincontrol.use")
                                ? control.suggestions(invocation.arguments()) : List.of();
                    }

                    @Override
                    public boolean hasPermission(Invocation invocation) {
                        return invocation.source().hasPermission("plugincontrol.use");
                    }
                });
        // Wait until all other proxy plugins have completed their initialization.
        server.getScheduler().buildTask(this, () -> control.check())
                .delay(1, TimeUnit.SECONDS).schedule();
        logger.info("PluginControl initialized on Velocity.");
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        final ProxyControlService current = control;
        if (current != null && current.isLoginBlocked() && event.getResult().isAllowed()
                && !event.getPlayer().hasPermission("plugincontrol.bypass")) {
            event.setResult(ResultedEvent.ComponentResult.denied(
                    miniMessage.deserialize(current.kickMessage())));
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (control != null) control.close();
    }
}
