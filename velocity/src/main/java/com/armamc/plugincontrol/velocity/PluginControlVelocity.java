package com.armamc.plugincontrol.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import javax.inject.Inject;

@Plugin(
        id = "plugincontrol",
        name = "PluginControl",
        version = "1.3.0",
        authors = {"ThiagoROX"}
)
public final class PluginControlVelocity {
    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public PluginControlVelocity(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("PluginControl enabled on Velocity ({} plugins detected).",
                server.getPluginManager().getPlugins().size());
    }
}
