package net.nuggetmc.ai.command.commands;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import com.jonahseguin.drink.command.DrinkCommandService;
import com.jonahseguin.drink.utils.ChatUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.nuggetmc.ai.TerminatorPlus;
import net.nuggetmc.ai.command.CommandHandler;
import net.nuggetmc.ai.command.CommandInstance;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class MainCommand extends CommandInstance {

    private final DrinkCommandService drink;

    private BaseComponent[] rootInfo;

    public MainCommand(CommandHandler commandHandler, DrinkCommandService drink) {
        super(commandHandler);

        this.drink = drink;

        Bukkit.getScheduler().runTask(TerminatorPlus.getInstance(), this::rootInfoSetup);
    }

    @Command(
        desc = "The TerminatorPlus main command."
    )
    public void root(@Sender CommandSender sender) {
        if (rootInfo == null) {
            rootInfoSetup();
        }

        sender.spigot().sendMessage(rootInfo);
    }

    private void rootInfoSetup() {
        ComponentBuilder message = new ComponentBuilder();
        String pluginName = TerminatorPlus.getInstance().getName();

        message.append(ChatUtils.LINE + "\n");
        message.append(ChatColor.GOLD + pluginName + ChatColor.GRAY + " [v" + TerminatorPlus.getVersion() + "]\n");
        message.append("\nPlugin Information:\n");
        message.append(ChatUtils.BULLET_FORMATTED + "Author" + ChatUtils.BULLET_FORMATTED + ChatColor.YELLOW + "HorseNuggets\n");
        message.append(ChatUtils.BULLET_FORMATTED + "Links" + ChatUtils.BULLET_FORMATTED);
        message.append(ChatColor.RED + "YouTube");
        message.event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.youtube.com/horsenuggets"));
        message.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to visit HorseNuggets' " + ChatColor.RED + "YouTube" + ChatColor.RESET + "!")));
        message.append(", ");
        message.event((ClickEvent) null);
        message.event((HoverEvent) null);
        message.append(ChatColor.BLUE + "Discord");
        message.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "lol okay this isn't actually ready yet"));
        message.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to visit HorseNuggets' " + ChatColor.BLUE + "Discord" + ChatColor.RESET + "!")));
        message.append("\n");
        message.event((ClickEvent) null);
        message.event((HoverEvent) null);
        message.append("\nPlugin Commands:\n");

        drink.getCommands().forEach((name, command) -> {
            if (!name.equalsIgnoreCase(pluginName)) {
                message.append(ChatUtils.BULLET_FORMATTED + ChatColor.YELLOW + "/" + name + ChatUtils.BULLET_FORMATTED + command.getDescription() + "\n");
            }
        });

        message.append(ChatUtils.LINE);

        this.rootInfo = message.create();
    }
}
