package net.nuggetmc.tplus.bot.agent.legacyagent.ai;

import net.minecraft.server.v1_16_R3.EntityLiving;
import net.nuggetmc.tplus.TerminatorPlus;
import net.nuggetmc.tplus.bot.Bot;
import net.nuggetmc.tplus.bot.BotManager;
import net.nuggetmc.tplus.bot.agent.legacyagent.EnumTargetGoal;
import net.nuggetmc.tplus.bot.agent.legacyagent.LegacyAgent;
import net.nuggetmc.tplus.command.commands.AICommand;
import net.nuggetmc.tplus.utils.ChatUtils;
import net.nuggetmc.tplus.utils.MathUtils;
import net.nuggetmc.tplus.utils.MojangAPI;
import net.nuggetmc.tplus.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class IntelligenceAgent {

    /*
     * export all agent data to the plugin folder as separate folder things
     * commands /ai stop and /ai pause
     * if a session with name already exists keep adding underscores
     * /ai conclude or /ai finish
     * default anchor location, /ai relocateanchor
     */

    private final TerminatorPlus plugin;
    private final BotManager manager;
    private final AICommand aiManager;
    private final BukkitScheduler scheduler;

    private LegacyAgent agent;
    private Thread thread;
    private boolean active;

    private final String name;

    private final String botName;
    private final String botSkin;
    private final int cutoff;

    private final Map<String, Bot> bots;

    private int populationSize;
    private int generation;

    private Player primary;

    private final Set<CommandSender> users;
    private final Map<Integer, Set<Map<BotNode, Map<BotDataType, Double>>>> genProfiles;

    public IntelligenceAgent(AICommand aiManager, int populationSize, String name, String skin) {
        this.plugin = TerminatorPlus.getInstance();
        this.manager = plugin.getManager();
        this.aiManager = aiManager;
        this.scheduler = Bukkit.getScheduler();
        this.name = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(Calendar.getInstance().getTime());
        this.botName = name;
        this.botSkin = skin;
        this.bots = new HashMap<>();
        this.users = new HashSet<>(Collections.singletonList(Bukkit.getConsoleSender()));
        this.cutoff = 5;
        this.genProfiles = new HashMap<>();
        this.populationSize = populationSize;
        this.active = true;

        scheduler.runTaskAsynchronously(plugin, () -> {
            thread = Thread.currentThread();

            try {
                task();
            } catch (Exception e) {
                print(e);
                print("The thread has been interrupted.");
                print("The session will now close.");
                close();
            }
        });
    }

    private void task() throws InterruptedException {
        setup();
        sleep(1000);

        while (active) {
            runGeneration();
        }

        sleep(5000);
        close();
    }

    private void runGeneration() throws InterruptedException {
        generation++;

        print("Starting generation " + ChatColor.RED + generation + ChatColor.RESET + "...");

        sleep(2000);

        String skinName = botSkin == null ? this.botName : botSkin;

        print("Fetching skin data for " + ChatColor.GREEN + skinName + ChatColor.RESET + "...");

        String botName = this.botName.endsWith("%") ? this.botName : this.botName + "%";

        print("Creating " + (populationSize == 1 ? "new bot" : ChatColor.RED + NumberFormat.getInstance(Locale.US).format(populationSize) + ChatColor.RESET + " new bots")
                + " with name " + ChatColor.GREEN + botName.replace("%", ChatColor.LIGHT_PURPLE + "%" + ChatColor.RESET)
                + (botSkin == null ? "" : ChatColor.RESET + " and skin " + ChatColor.GREEN + botSkin)
                + ChatColor.RESET + "...");

        Set<Map<BotNode, Map<BotDataType, Double>>> loadedProfiles = genProfiles.get(generation);
        Location loc = PlayerUtils.findAbove(primary.getLocation(), 20);

        scheduler.runTask(plugin, () -> {
            Set<Bot> bots;

            if (loadedProfiles == null) {
                bots = manager.createBots(loc, botName, skinName, populationSize, NeuralNetwork.RANDOM);
            } else {
                List<NeuralNetwork> networks = new ArrayList<>();
                loadedProfiles.forEach(profile -> networks.add(NeuralNetwork.createNetworkFromProfile(profile)));

                if (populationSize != networks.size()) {
                    print("An exception has occured.");
                    print("The stored population size differs from the size of the stored networks.");
                    close();
                    return;
                }

                bots = manager.createBots(loc, botName, skinName, networks);
            }

            bots.forEach(bot -> {
                String name = bot.getName();

                while (this.bots.containsKey(name)) {
                    name += "_";
                }

                this.bots.put(name, bot);
            });
        });

        while (bots.size() != populationSize) {
            sleep(1000);
        }

        sleep(2000);
        print("The bots will now attack each other.");

        agent.setTargetType(EnumTargetGoal.NEAREST_BOT);

        while (aliveCount() > 1) {
            sleep(1000);
        }

        print("Generation " + ChatColor.RED + generation + ChatColor.RESET + " has ended.");

        HashMap<Bot, Integer> values = new HashMap<>();

        for (Bot bot : bots.values()) {
            values.put(bot, bot.getAliveTicks());
        }

        List<Map.Entry<Bot, Integer>> sorted = MathUtils.sortByValue(values);
        Set<Bot> winners = new HashSet<>();

        int i = 1;

        for (Map.Entry<Bot, Integer> entry : sorted) {
            Bot bot = entry.getKey();
            boolean check = i <= cutoff;
            if (check) {
                print(ChatColor.GRAY + "[" + ChatColor.YELLOW + "#" + i + ChatColor.GRAY + "] " + ChatColor.GREEN + bot.getName()
                        + ChatUtils.BULLET_FORMATTED + ChatColor.RED + bot.getKills() + " kills");
                winners.add(bot);
            }

            i++;
        }

        sleep(3000);

        Map<BotNode, Map<BotDataType, List<Double>>> lists = new HashMap<>();

        winners.forEach(bot -> {
            Map<BotNode, Map<BotDataType, Double>> data = bot.getNeuralNetwork().values();

            data.forEach((nodeType, node) -> {
                if (!lists.containsKey(nodeType)) {
                    lists.put(nodeType, new HashMap<>());
                }

                Map<BotDataType, List<Double>> nodeValues = lists.get(nodeType);

                node.forEach((dataType, value) -> {
                    if (!nodeValues.containsKey(dataType)) {
                        nodeValues.put(dataType, new ArrayList<>());
                    }

                    nodeValues.get(dataType).add(value);
                });
            });
        });

        Set<Map<BotNode, Map<BotDataType, Double>>> profiles = new HashSet<>();

        double mutationSize = Math.pow(Math.E, 2); //MathUtils.getMutationSize(generation);

        for (int j = 0; j < populationSize; j++) {
            Map<BotNode, Map<BotDataType, Double>> profile = new HashMap<>();

            lists.forEach((nodeType, map) -> {
                Map<BotDataType, Double> points = new HashMap<>();

                map.forEach((dataType, dataPoints) -> {
                    double value = ((int) (10 * MathUtils.generateConnectionValue(dataPoints, mutationSize))) / 10D;

                    points.put(dataType, value);
                });

                profile.put(nodeType, points);
            });

            profiles.add(profile);
        }

        genProfiles.put(generation + 1, profiles);

        sleep(2000);

        clearBots();

        agent.setTargetType(EnumTargetGoal.NONE);
    }

    private int aliveCount() {
        return (int) bots.values().stream().filter(EntityLiving::isAlive).count();
    }

    private void close() {
        aiManager.clearSession();
        stop(); // safety call
    }

    public void stop() {
        if (this.active) {
            this.active = false;
        }

        if (!thread.isInterrupted()) {
            this.thread.interrupt();
        }
    }

    private void sleep(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }

    public String getName() {
        return name;
    }

    public void addUser(CommandSender sender) {
        if (users.contains(sender)) return;

        users.add(sender);
        print(sender.getName() + " has been added to the userlist.");

        if (primary == null && sender instanceof Player) {
            setPrimary((Player) sender);
        }
    }

    public void setPrimary(Player player) {
        this.primary = player;
        print(player.getName() + " has been set as the primary user.");
    }

    private void print(Object... objects) {
        String message = ChatColor.DARK_GREEN + "[REINFORCEMENT] " + ChatColor.RESET + String.join(" ", Arrays.stream(objects).map(String::valueOf).toArray(String[]::new));
        users.forEach(u -> u.sendMessage(message));
        // log -> ChatColor.stripColor(message);
    }

    private void setup() {
        clearBots();

        if (populationSize < cutoff) {
            populationSize = cutoff;
            print("The input value for the population size is lower than the cutoff (" + ChatColor.RED + cutoff + ChatColor.RESET + ")!"
                    + " The new population size is " + ChatColor.RED + populationSize + ChatColor.RESET + ".");
        }

        if (!(manager.getAgent() instanceof LegacyAgent)) {
            print("The AI manager currently only supports " + ChatColor.AQUA + "LegacyAgent" + ChatColor.RESET + ".");
            close();
            return;
        }

        agent = (LegacyAgent) manager.getAgent();
        agent.setTargetType(EnumTargetGoal.NONE);

        print("The bot target goal has been set to " + ChatColor.YELLOW + EnumTargetGoal.NONE.name() + ChatColor.RESET + ".");
        print("Disabling target offsets...");

        agent.offsets = false;

        print("Disabling bot drops...");

        agent.setDrops(false);

        print(ChatColor.GREEN + "Setup is now complete.");
    }

    private void clearBots() {
        if (!bots.isEmpty()) {
            print("Removing all cached bots...");

            bots.values().forEach(Bot::removeVisually);
            bots.clear();
        }

        /*print("Removing all current bots...");

        int size = manager.fetch().size();
        manager.reset();

        String formatted = NumberFormat.getNumberInstance(Locale.US).format(size);
        print("Removed " + ChatColor.RED + formatted + ChatColor.RESET + " entit" + (size == 1 ? "y" : "ies") + ".");

        bots.clear();*/
    }
}
