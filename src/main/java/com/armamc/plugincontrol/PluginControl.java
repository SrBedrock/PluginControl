package com.armamc.plugincontrol;

import com.armamc.plugincontrol.commands.MainCommand;
import com.armamc.plugincontrol.managers.ConfigManager;
import com.armamc.plugincontrol.managers.MessageManager;
import com.armamc.plugincontrol.managers.PluginsManager;
import com.technicjelle.UpdateChecker;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
        checkUpdate();
    }

    @Override
    public void onDisable() {
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    private void registerConfig() {
        if (!getDataFolder().exists() && getDataFolder().mkdir()) {
            getLogger().info("Creating the plugin folder!");
        }
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        this.adventure = BukkitAudiences.create(this);
        pluginsManager = new PluginsManager(this);
    }

    private void registerCommands() {
        final var command = new MainCommand(this);
        final var pluginCommand = getCommand("plugincontrol");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }
    }

    private void registerTask() {
        Bukkit.getScheduler().runTaskLater(this, pluginsManager::checkPlugins, 20L);
    }

    private void checkUpdate() {
        if (configManager.isUpdateNotifierEnabled()) {
            final UpdateChecker updateChecker = new UpdateChecker("SrBedrock", "PluginControl", "1.2.1");
            updateChecker.checkAsync();
            updateChecker.logUpdateMessage(getLogger());
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

    @Contract(pure = true)
    public PluginsManager getPluginsManager() {
        return pluginsManager;
    }

    @Contract(pure = true)
    public @NotNull BukkitAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    public boolean isPluginEnabled(final String pluginName) {
        final var plugin = getServer().getPluginManager().getPlugin(pluginName);
        return plugin != null && plugin.isEnabled();
    }

}
