package com.armamc.plugincontrol;

import com.armamc.plugincontrol.commands.Command;
import com.armamc.plugincontrol.config.Config;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.MessageFormat;
import java.util.List;

public final class PluginControl extends JavaPlugin {
    private static PluginControl instance;
    private BukkitAudiences adventure;

    @Override
    public void onEnable() {
        instance = this;
        this.adventure = BukkitAudiences.create(this);
        registerCommands();
        saveDefaultConfig();
        Config.load();
        registerTask();
    }

    @Override
    public void onDisable() {
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    public static PluginControl getInstance() {
        return instance;
    }

    private void registerCommands() {
        Command command = new Command();
        PluginCommand pluginCommand = getCommand("plugincontrol");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }
    }

    private void registerTask() {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (Config.isEnabled()) {
                getLogger().info("Checking plugins...");
                checkPlugins();
            }
        }, 20L);
    }


    private void checkPlugins() {
        List<String> plugins = Config.getPluginList();
        boolean hasPlugins = false;
        for (String plugin : plugins) {
            if (getServer().getPluginManager().getPlugin(plugin) == null) {
                getLogger().warning(() -> MessageFormat.format("Plugin {0} não encontrado!", plugin));
                hasPlugins = true;
            }
        }
        if (hasPlugins) {
            getLogger().warning("One or more plugins required for the server to function properly were not found. Shutting down the server!");
            getServer().shutdown();
        } else {
            getLogger().info("Plugins successfully verified!");
        }
    }

    public @NonNull BukkitAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

}
