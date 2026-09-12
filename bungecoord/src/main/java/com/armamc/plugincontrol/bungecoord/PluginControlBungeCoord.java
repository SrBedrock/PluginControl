package com.armamc.plugincontrol.bungecoord;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public final class PluginControlBungeCoord extends Plugin {
    @Override
    public void onEnable() {
        getLogger().info("PluginControl enabled on BungeeCord (" +
                ProxyServer.getInstance().getPluginManager().getPlugins().size() + " plugins detected).");
    }
}
