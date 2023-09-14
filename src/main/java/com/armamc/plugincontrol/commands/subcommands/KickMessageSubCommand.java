package com.armamc.plugincontrol.commands.subcommands;

import com.armamc.plugincontrol.PluginControl;
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
    private final MessageManager message;

    @Contract(pure = true)
    public KickMessageSubCommand(@NotNull PluginControl plugin) {
        this.message = plugin.getMessageManager();
    }

    @Override
    public void execute(CommandSender sender, Command command, String label, String @NotNull [] args) {
        var kick = "kick-message";
        if (args.length == 0 || args[0].isBlank()) {
            message.send(sender, message.getKickMessageInfo(), Placeholder.component(kick, message.deserialize(message.getKickMessage())));
        } else {
            message.setKickMessage(String.join(" ", Arrays.copyOfRange(args, 0, args.length)));
            message.send(sender, message.getKickMessageSet(), Placeholder.component(kick, message.deserialize(message.getKickMessage())));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Stream.of("<kick message>").filter(s -> s.startsWith(args[0])).toList();
    }

}
