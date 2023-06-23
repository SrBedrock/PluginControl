package com.armamc.plugincontrol;

import com.armamc.plugincontrol.commands.Command;
import com.armamc.plugincontrol.config.Config;
import com.armamc.plugincontrol.config.Lang;
import com.armamc.plugincontrol.listeners.PlayerListener;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public final class PluginControl extends JavaPlugin {
    private final ConsoleCommandSender sender = Bukkit.getConsoleSender();
    private BukkitAudiences adventure;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private PlayerListener playerListener;
    private Config config;
    private Lang lang;
    private static final String PREFIX = "prefix";

    @Override
    public void onEnable() {
        this.adventure = BukkitAudiences.create(this);
        registerConfig();
        registerCommands();
        registerTask();
    }

    @Override
    public void onDisable() {
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    public Config getPluginConfig() {
        return config;
    }

    public Lang getPluginLang() {
        return lang;
    }

    public void unregisterListener() {
        if (playerListener != null) {
            PlayerLoginEvent.getHandlerList().unregister(this);
            playerListener = null;
        }
    }

    private void registerConfig() {
        if (!getDataFolder().exists() && getDataFolder().mkdir()) {
            getLogger().info("Creating the plugin folder!");
        }
        config = new Config(this);
        lang = new Lang(this);
    }

    private void registerCommands() {
        var command = new Command(this);
        var pluginCommand = getCommand("plugincontrol");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }
    }

    private void registerTask() {
        Bukkit.getScheduler().runTaskLater(this, this::checkPlugins, 20L);
    }

    public void checkPlugins() {
        if (!config.isEnabled()) return;

        send(sender, lang.message("console.checking-plugins"), null);
        var plugins = config.getPluginList();
        var missingPlugins = new ArrayList<String>();
        boolean hasPlugins = false;
        for (String plugin : plugins) {
            if (getServer().getPluginManager().getPlugin(plugin) == null) {
                missingPlugins.add(plugin);
                hasPlugins = true;
            }
        }
        if (hasPlugins) {
            var tag = Placeholder.parsed("plugins", String.join(", ", missingPlugins));
            if (config.getAction().equals("disallow-player-login")) {
                playerListener = new PlayerListener(this);
                playerListener.init();
                send(sender, lang.message("console.log-to-console"), tag);
                return;
            }
            if (config.getAction().equals("log-to-console")) {
                send(sender, lang.message("console.log-to-console"), tag);
                return;
            }
            if (config.getAction().equals("shutdown-server")) {
                send(sender, lang.message("console.disabling-server"), tag);
                getServer().shutdown();
            }
        } else {
            send(sender, lang.message("console.finished-checking"), null);
        }
    }

    public @NonNull BukkitAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    public void send(@NotNull CommandSender sender, @NotNull String message, @Nullable TagResolver tag) {
        if (message.isEmpty() || message.isBlank()) {
            return;
        }
        var prefix = Placeholder.parsed(PREFIX, lang.message(PREFIX));
        if (tag == null) {
            adventure().sender(sender).sendMessage(miniMessage.deserialize(message, prefix));
        } else {
            adventure().sender(sender).sendMessage(miniMessage.deserialize(message, prefix, tag));
        }
    }

}
