package com.armamc.plugincontrol.commands.subcommands;

import com.armamc.plugincontrol.PluginControl;
import com.armamc.plugincontrol.managers.ConfigManager;
import com.armamc.plugincontrol.managers.MessageManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class KickMessageSubCommand implements SubCommand {
    private final ConfigManager config;
    private final MessageManager message;

    @Contract(pure = true)
    public KickMessageSubCommand(@NotNull PluginControl plugin) {
        this.config = plugin.getConfigManager();
        this.message = plugin.getMessageManager();
    }

    @Override
    public void execute(CommandSender sender, Command command, String label, String @NotNull [] args) {
        var kick = "kick-message";
        if (args.length == 0 || args[0].isBlank()) {
            message.send(sender, message.getKickMessage(), Placeholder.component(kick, message.deserialize(config.getKickMessage())));
        } else {
            config.setKickMessage(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
            message.send(sender, message.getKickMessageSet(), Placeholder.component(kick, message.deserialize(config.getKickMessage())));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Stream.of("<kick message>").filter(s -> s.startsWith(args[0])).toList();
    }

}
