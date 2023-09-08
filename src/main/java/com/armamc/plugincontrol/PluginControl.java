package com.armamc.plugincontrol;

import com.armamc.plugincontrol.commands.Command;
import com.armamc.plugincontrol.managers.ConfigManager;
import com.armamc.plugincontrol.managers.MessageManager;
import com.armamc.plugincontrol.managers.PluginsManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Contract;

public final class PluginControl extends JavaPlugin {
    private BukkitAudiences adventure;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private PluginsManager pluginsManager;

    @Override
    public void onEnable() {
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

    @Contract(pure = true)
    public ConfigManager getConfigManager() {
        return configManager;
    }

    @Contract(pure = true)
    public MessageManager getMessageManager() {
        return messageManager;
    }

    private void registerConfig() {
        if (!getDataFolder().exists() && getDataFolder().mkdir()) {
            getLogger().info("Creating the plugin folder!");
        }
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        adventure = BukkitAudiences.create(this);
        pluginsManager = new PluginsManager(this);
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
        Bukkit.getScheduler().runTaskLater(this, () -> pluginsManager.checkPlugins(), 20L);
    }

    @Contract(pure = true)
    public @NonNull BukkitAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

}
