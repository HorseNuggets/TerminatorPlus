package net.nuggetmc.ai.command.commands;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.OptArg;
import com.jonahseguin.drink.annotation.Sender;
import net.nuggetmc.ai.TerminatorPlus;
import net.nuggetmc.ai.bot.BotManager;
import net.nuggetmc.ai.bot.agent.Agent;
import net.nuggetmc.ai.bot.agent.legacyagent.ai.NetworkType;
import net.nuggetmc.ai.command.CommandHandler;
import net.nuggetmc.ai.command.CommandInstance;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AICommand extends CommandInstance {

    private final BotManager manager;
    private final Agent agent;

    public AICommand(CommandHandler commandHandler) {
        super(commandHandler);

        this.manager = TerminatorPlus.getInstance().getManager();
        this.agent = manager.getAgent();
    }

    @Command(
        desc = "The root command for bot AI training."
    )
    public void root(@Sender CommandSender sender) {
        commandHandler.sendRootInfo(this, sender);
    }

    @Command(
        name = "random",
        desc = "Create bots with random neural networks, collecting feed data.",
        usage = "<amount> <name> [skin]"
    )
    public void random(@Sender Player sender, int n, String name, @OptArg String skin) {
        manager.createBots(sender, name, skin, n, NetworkType.RANDOM);
    }
}
