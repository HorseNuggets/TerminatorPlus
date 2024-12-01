package net.nuggetmc.tplus.command.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.nuggetmc.tplus.TerminatorPlus;
import net.nuggetmc.tplus.api.utils.ChatUtils;
import net.nuggetmc.tplus.command.CommandHandler;
import net.nuggetmc.tplus.command.CommandInstance;
import net.nuggetmc.tplus.command.annotation.Command;
import net.nuggetmc.tplus.utils.MCLogs;
import org.bukkit.command.CommandSender;

import java.io.IOException;

public class MainCommand extends CommandInstance {

    private Component rootInfo;

    public MainCommand(CommandHandler handler, String name, String description, String... aliases) {
        super(handler, name, description, aliases);
    }

    @Command
    public void root(CommandSender sender) {
        if (rootInfo == null) {
            rootInfoSetup();
        }

        sender.sendMessage(rootInfo);
    }

    @Command(
            name = "debuginfo",
            desc = "Upload debug info to mclo.gs"
    )
    public void debugInfo(CommandSender sender) {
        sender.sendMessage(NamedTextColor.GREEN + "Uploading debug info to mclogs...");
        String url = null;
        try {
            url = MCLogs.postInfo();
        } catch (IOException e) {
            String error = e.getMessage();
            sender.sendMessage(NamedTextColor.RED + "Failed to upload debug info to mclogs: " + error);
            return;
        }
        sender.sendMessage(NamedTextColor.GREEN + "Debug info uploaded to " + url);
    }


    private void rootInfoSetup() {
        String pluginName = TerminatorPlus.getInstance().getName();

        rootInfo = Component.text()
                .append(Component.text(ChatUtils.LINE + "\n"))
                .append(Component.text(pluginName).color(NamedTextColor.GOLD))
                .append(Component.text(" [v" + TerminatorPlus.getVersion() + "]\n").color(NamedTextColor.GRAY))
                .append(Component.text("\nPlugin Information:\n"))
                .append(Component.text(ChatUtils.BULLET_FORMATTED + "Author" + ChatUtils.BULLET_FORMATTED))
                .append(Component.text("HorseNuggets\n").color(NamedTextColor.YELLOW))
                .append(Component.text(ChatUtils.BULLET_FORMATTED + "Links" + ChatUtils.BULLET_FORMATTED))
                .append(Component.text("YouTube")
                        .color(NamedTextColor.RED)
                        .clickEvent(ClickEvent.openUrl("https://youtube.com/horsenuggets"))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to visit HorseNuggets' YouTube!"))))
                .append(Component.text(", "))
                .append(Component.text("Discord")
                        .color(NamedTextColor.BLUE)
                        .clickEvent(ClickEvent.openUrl("https://discord.gg/vZVSf2D6mz"))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to visit HorseNuggets' Discord!"))))
                .append(Component.text("\n\nPlugin Commands:\n"))
                .build();

        commandHandler.getCommands().forEach((name, command) -> {
            if (!name.equalsIgnoreCase(pluginName)) {
                rootInfo = rootInfo.append(Component.text(ChatUtils.BULLET_FORMATTED))
                        .append(Component.text("/" + name).color(NamedTextColor.YELLOW))
                        .append(Component.text(ChatUtils.BULLET_FORMATTED + command.getDescription() + "\n"));
            }
        });

        rootInfo = rootInfo.append(Component.text(ChatUtils.LINE));
    }
}
