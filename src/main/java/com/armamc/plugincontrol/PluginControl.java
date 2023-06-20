package com.armamc.plugincontrol;

import com.armamc.plugincontrol.commands.Command;
import com.armamc.plugincontrol.config.Config;
import com.armamc.plugincontrol.config.Lang;
import com.armamc.plugincontrol.listeners.PlayerLoginListener;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class PluginControl extends JavaPlugin {
    private BukkitAudiences adventure;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private Config config;
    private Lang lang;

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

    private void registerConfig() {
        config = new Config(this);
        lang = new Lang(this);
    }

    private void registerCommands() {
        Command command = new Command(this);
        PluginCommand pluginCommand = getCommand("plugincontrol");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }
    }

    private void registerTask() {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (config.isEnabled()) {
                sendToConsole(lang.message("checking-plugins"));
                checkPlugins();
            }
        }, 20L);
    }


    public void checkPlugins() {
        List<String> plugins = config.getPluginList();
        List<String> missingPlugins = new ArrayList<>();
        boolean hasPlugins = false;
        for (String plugin : plugins) {
            if (getServer().getPluginManager().getPlugin(plugin) == null) {
                missingPlugins.add(plugin);
                hasPlugins = true;
            }
        }
        if (hasPlugins) {
            TagResolver.Single tag = Placeholder.parsed("plugins",
                    String.join(", ", missingPlugins));

            if (config.getAction().equals("disallow-player-login")) {
                new PlayerLoginListener(this);
                sendToConsole(lang.message("log-to-console"), tag);
                return;
            }
            if (config.getAction().equals("log-to-console")) {
                sendToConsole(lang.message("log-to-console"), tag);
                return;
            }
            if (config.getAction().equals("shutdown-server")) {
                sendToConsole(lang.message("disabling-server"), tag);
                getServer().shutdown();
            }
        } else {
            sendToConsole(lang.message("finished-checking"));
        }
    }

    public @NonNull BukkitAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    public void sendToConsole(String message) {
        TagResolver.Single prefix = Placeholder.parsed("prefix", lang.message("prefix"));
        adventure().console().sendMessage(miniMessage.deserialize(message, prefix));
    }

    public void sendToConsole(String message, TagResolver tags) {
        TagResolver.Single prefix = Placeholder.parsed("prefix", lang.message("prefix"));
        adventure().console().sendMessage(miniMessage.deserialize(message, prefix, tags));
    }

    public void sendToPlayer(CommandSender sender, String message) {
        TagResolver.Single prefix = Placeholder.parsed("prefix", lang.message("prefix"));
        adventure().sender(sender).sendMessage(miniMessage.deserialize(message, prefix));
    }

    public void sendToPlayer(CommandSender sender, String message, TagResolver tags) {
        TagResolver.Single prefix = Placeholder.parsed("prefix", lang.message("prefix"));
        adventure().sender(sender).sendMessage(miniMessage.deserialize(message, prefix, tags));
    }

}
