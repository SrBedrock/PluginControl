package com.armamc.plugincontrol;

import com.armamc.plugincontrol.commands.Command;
import com.armamc.plugincontrol.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.MessageFormat;
import java.util.List;

public final class PluginControl extends JavaPlugin {
    private static PluginControl instance;

    @Override
    public void onEnable() {
        instance = this;
        getCommand("plugincontrol").setExecutor(new Command());
        getCommand("plugincontrol").setTabCompleter(new Command());
        saveDefaultConfig();
        Config.load();
        registerTask();
    }

    public static PluginControl getInstance() {
        return instance;
    }

    private void registerTask() {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (Config.isEnabled()) {
                getLogger().info("Verificando plugins...");
                checkPlugins();
            }
        }, 20);
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
            getLogger().warning("Desativando o servidor!");
            getServer().shutdown();
        } else {
            getLogger().info("Plugins verificados com sucesso!");
        }
    }

}
