package net.nuggetmc.ai.command.commands;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.OptArg;
import com.jonahseguin.drink.annotation.Sender;
import com.jonahseguin.drink.utils.ChatUtils;
import net.nuggetmc.ai.TerminatorPlus;
import net.nuggetmc.ai.bot.BotManager;
import net.nuggetmc.ai.command.CommandHandler;
import net.nuggetmc.ai.command.CommandInstance;
import org.bukkit.entity.Player;

public class AICommand extends CommandInstance {

    private final BotManager manager;

    public AICommand(CommandHandler commandHandler) {
        super(commandHandler);

        this.manager = TerminatorPlus.getInstance().getManager();
    }

    @Command(
        desc = "The root command for bot AI training."
    )
    public void root(@Sender Player sender) {
        sender.sendMessage(ChatUtils.LINE);
        commandHandler.getHelp(getClass()).forEach(sender::sendMessage);
        sender.sendMessage(ChatUtils.LINE);
    }

    @Command(
        name = "random",
        desc = "Create bots with random neural networks, collecting feed data.",
        usage = "<name> [skin]"
    )
    public void create(@Sender Player sender, String name, @OptArg String skin) {
        manager.createBots(sender, name, skin, 1);
    }
}
