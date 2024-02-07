package com.armamc.plugincontrol.commands.subcommands;

import com.armamc.plugincontrol.PluginControl;
import com.armamc.plugincontrol.managers.MessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class CheckDependSubCommand implements SubCommand {
    private final MessageManager message;

    @Contract(pure = true)
    public CheckDependSubCommand(@NotNull PluginControl plugin) {
        this.message = plugin.getMessageManager();
    }

    @Override
    public void execute(CommandSender sender, Command command, String label, String @NotNull [] args) {

        if (args.length < 1) {
            message.send(sender, message.getCheckDependError());
            return;
        }

        Map<String, List<String>> dependCheck = new HashMap<>();
        String targetPlugin = args[0];

        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {

            String pluginName = plugin.getName();
            if (pluginName.equalsIgnoreCase(targetPlugin)) {
                continue;
            }

            PluginDescriptionFile description = plugin.getDescription();
            if (description.getDepend().contains(targetPlugin)) {
                dependCheck.computeIfAbsent("depend", k -> new ArrayList<>()).add(pluginName);
            }

            if (description.getSoftDepend().contains(targetPlugin)) {
                dependCheck.computeIfAbsent("softdepend", k -> new ArrayList<>()).add(pluginName);
            }
        }

        TagResolver.Single pluginTag = Placeholder.parsed("plugin", targetPlugin);

        if (dependCheck.isEmpty()) {
            message.send(sender, message.getCheckDependNotFound(), pluginTag);
            return;
        }

        List<String> depend = dependCheck.get("depend");
        if (depend != null && !depend.isEmpty()) {
            Component dependMessage = message.getPluginListComponent(new HashSet<>(depend));
            message.send(sender, message.getCheckDependDepend(), pluginTag, Placeholder.component("plugins", dependMessage));
        }

        List<String> softdepend = dependCheck.get("softdepend");
        if (softdepend != null && !softdepend.isEmpty()) {
            Component softdependMessage = message.getPluginListComponent(new HashSet<>(softdepend));
            message.send(sender, message.getCheckDependSoftDepend(), pluginTag, Placeholder.component("plugins", softdependMessage));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String label, String @NotNull [] args) {
        if (args.length == 1) {
            return Arrays.stream(Bukkit.getPluginManager().getPlugins())
                    .map(Plugin::getName)
                    .filter(s -> s.startsWith(args[0]))
                    .toList();
        }
        return List.of();
    }

}
