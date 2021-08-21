package net.nuggetmc.ai.command.commands;

import com.jonahseguin.drink.annotation.Autofill;
import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.OptArg;
import com.jonahseguin.drink.annotation.Sender;
import com.jonahseguin.drink.utils.ChatUtils;
import net.nuggetmc.ai.TerminatorPlus;
import net.nuggetmc.ai.bot.Bot;
import net.nuggetmc.ai.bot.BotManager;
import net.nuggetmc.ai.bot.agent.legacyagent.ai.IntelligenceAgent;
import net.nuggetmc.ai.bot.agent.legacyagent.ai.NeuralNetwork;
import net.nuggetmc.ai.command.CommandHandler;
import net.nuggetmc.ai.command.CommandInstance;
import net.nuggetmc.ai.utils.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.List;

public class AICommand extends CommandInstance {

    /*
     * ideas
     * ability to export neural network data to a text file, and also load from them
     * maybe also have a custom extension like .tplus and encrypt it in base64
     */

    private final TerminatorPlus plugin;
    private final BotManager manager;
    private final BukkitScheduler scheduler;

    private IntelligenceAgent agent;

    public AICommand(CommandHandler commandHandler) {
        super(commandHandler);

        this.plugin = TerminatorPlus.getInstance();
        this.manager = plugin.getManager();
        this.scheduler = Bukkit.getScheduler();
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
        manager.createBots(sender, name, skin, n, NeuralNetwork.RANDOM);
    }

    @Command(
        name = "reinforcement",
        desc = "Begin an AI training session.",
        usage = "<population-size> <name> [skin]"
    )
    public void reinforcement(@Sender Player sender, int populationSize, String name, @OptArg String skin) {
        // automatically do the -% thing, store values in map
        // for now only 1 session at a time, have a set of commandsenders to see output, including console
        // automatically reset all existing bots at the start, set targets towards each other
        // also in the future make this a subcommand, with /ai reinforcement defaults, /ai reinforcement begin/start
        // or just make /ai defaults with reinforcement options

        if (agent != null) {
            sender.sendMessage("A session is already active.");
            return;
        }

        sender.sendMessage("Starting a new session...");

        agent = new IntelligenceAgent(this, populationSize, name, skin);
        agent.addUser(sender);
    }

    public IntelligenceAgent getSession() {
        return agent;
    }

    @Command(
        name = "stop",
        desc = "End a currently running AI training session."
    )
    public void stop(@Sender CommandSender sender) {
        if (agent == null) {
            sender.sendMessage("No session is currently active.");
            return;
        }

        sender.sendMessage("Stopping the current session...");
        String name = agent.getName();
        clearSession();

        scheduler.runTaskLater(plugin, () -> sender.sendMessage("The session " + ChatColor.YELLOW + name + ChatColor.RESET + " has been closed."), 10);
    }

    public void clearSession() {
        if (agent != null) {
            agent.stop();
            agent = null;
        }
    }

    public boolean hasActiveSession() {
        return agent != null;
    }

    @Command(
        name = "info",
        desc = "Display neural network information about a bot.",
        usage = "<name>",
        autofill = "infoAutofill"
    )
    public void info(@Sender CommandSender sender, String name) {
        sender.sendMessage("Processing request...");

        scheduler.runTaskAsynchronously(plugin, () -> {
            try {
                Bot bot = manager.getFirst(name);

                if (bot == null) {
                    sender.sendMessage("Could not find bot " + ChatColor.GREEN + name + ChatColor.RESET + "!");
                    return;
                }

                if (!bot.hasNeuralNetwork()) {
                    sender.sendMessage("The bot " + ChatColor.GREEN + name + ChatColor.RESET + " does not have a neural network!");
                    return;
                }

                NeuralNetwork network = bot.getNeuralNetwork();
                List<String> strings = new ArrayList<>();

                network.nodes().forEach((nodeType, node) -> {
                    strings.add("");
                    strings.add(ChatColor.YELLOW + "\"" + nodeType.name().toLowerCase() + "\"" + ChatColor.RESET + ":");
                    List<String> values = new ArrayList<>();
                    node.getValues().forEach((dataType, value) -> values.add(ChatUtils.BULLET_FORMATTED + "node"
                            + dataType.getShorthand().toUpperCase() + ": " + ChatColor.RED + MathUtils.round2Dec(value)));
                    strings.addAll(values);
                });

                sender.sendMessage(ChatUtils.LINE);
                sender.sendMessage(ChatColor.DARK_GREEN + "NeuralNetwork" + ChatUtils.BULLET_FORMATTED + ChatColor.GRAY + "[" + ChatColor.GREEN + name + ChatColor.GRAY + "]");
                strings.forEach(sender::sendMessage);
                sender.sendMessage(ChatUtils.LINE);
            }

            catch (Exception e) {
                sender.sendMessage(ChatUtils.EXCEPTION_MESSAGE);
            }
        });
    }

    @Autofill
    public List<String> infoAutofill(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return manager.fetchNames();
        } else {
            return null;
        }
    }
}
