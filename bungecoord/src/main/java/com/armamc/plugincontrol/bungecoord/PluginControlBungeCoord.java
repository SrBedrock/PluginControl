package com.armamc.plugincontrol.bungecoord;

import com.armamc.plugincontrol.core.ProxyControlService;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.event.EventHandler;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class PluginControlBungeCoord extends Plugin implements Listener {
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacy = LegacyComponentSerializer.builder().hexColors().build();
    private volatile ProxyControlService control;

    @Override
    public void onEnable() {
        control = new ProxyControlService(getDataFolder().toPath(), new ProxyControlService.Platform() {
            @Override
            public List<ProxyControlService.PluginInfo> plugins() {
                return getProxy().getPluginManager().getPlugins().stream().map(plugin -> {
                    final var description = plugin.getDescription();
                    return new ProxyControlService.PluginInfo(description.getName(),
                            description.getDepends() == null ? Set.of() : Set.copyOf(description.getDepends()),
                            description.getSoftDepends() == null ? Set.of() : Set.copyOf(description.getSoftDepends()));
                }).toList();
            }

            @Override
            public boolean isEnabled(String name) {
                return getProxy().getPluginManager().getPlugins().stream()
                        .anyMatch(plugin -> plugin.getDescription().getName().equalsIgnoreCase(name));
            }

            @Override
            public void notifyStaff(String message) {
                final var parts = components(message);
                getProxy().getConsole().sendMessage(parts);
                getProxy().getPlayers().stream()
                        .filter(player -> player.hasPermission("plugincontrol.notify"))
                        .forEach(player -> player.sendMessage(parts));
            }

            @Override
            public void blockLogin(boolean blocked, String kickMessage) {
                if (!blocked) return;
                final var parts = components(kickMessage);
                getProxy().getPlayers().stream()
                        .filter(player -> !player.hasPermission("plugincontrol.bypass"))
                        .forEach(player -> player.disconnect(parts));
            }

            @Override
            public void shutdown() {
                getProxy().stop();
            }

            @Override
            public void logError(String message, Throwable error) {
                getLogger().log(java.util.logging.Level.SEVERE, message, error);
            }
        });
        getProxy().getPluginManager().registerCommand(this, new ControlCommand());
        getProxy().getPluginManager().registerListener(this, this);
        getProxy().getScheduler().schedule(this, () -> control.check(), 1, TimeUnit.SECONDS);
        getLogger().info("PluginControl initialized on BungeeCord.");
    }

    private net.md_5.bungee.api.chat.BaseComponent[] components(String message) {
        return TextComponent.fromLegacyText(legacy.serialize(miniMessage.deserialize(message)));
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        final ProxyControlService current = control;
        if (current != null && current.isLoginBlocked()
                && !event.getPlayer().hasPermission("plugincontrol.bypass")) {
            event.getPlayer().disconnect(components(current.kickMessage()));
        }
    }

    @Override
    public void onDisable() {
        if (control != null) control.close();
    }

    private final class ControlCommand extends Command implements TabExecutor {
        ControlCommand() {
            super("plugincontrol", "plugincontrol.use", "pc", "plcontrol");
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            control.execute(new ProxyControlService.Actor() {
                @Override
                public boolean permitted() {
                    return sender.hasPermission("plugincontrol.use");
                }

                @Override
                public void send(String message) {
                    sender.sendMessage(components(message));
                }
            }, "plugincontrol", args);
        }

        @Override
        public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
            return sender.hasPermission("plugincontrol.use")
                    ? control.suggestions(args) : List.of();
        }
    }
}
