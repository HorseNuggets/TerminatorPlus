package net.nuggetmc.tplus.api.agent.legacyagent.ai;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.nuggetmc.tplus.api.AIManager;
import net.nuggetmc.tplus.api.BotManager;
import net.nuggetmc.tplus.api.Terminator;
import net.nuggetmc.tplus.api.agent.legacyagent.EnumTargetGoal;
import net.nuggetmc.tplus.api.agent.legacyagent.LegacyAgent;
import net.nuggetmc.tplus.api.utils.ChatUtils;
import net.nuggetmc.tplus.api.utils.MathUtils;
import net.nuggetmc.tplus.api.utils.MojangAPI;
import net.nuggetmc.tplus.api.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
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

    private final Plugin plugin;
    private final BotManager manager;
    private final AIManager aiManager;
    private final BukkitScheduler scheduler;

    private LegacyAgent agent;
    private Thread thread;
    private boolean active;

    private final String name;

    private final String botName;
    private final String botSkin;
    private final int cutoff;

    private final Map<String, Terminator> bots;

    private int populationSize;
    private int generation;

    private Player primary;

    private final Set<CommandSender> users;
    private final Map<Integer, Set<Map<BotNode, Map<BotDataType, Double>>>> genProfiles;

    public IntelligenceAgent(AIManager aiManager, int populationSize, String name, String skin, Plugin plugin, BotManager manager) {
        this.plugin = plugin;
        this.manager = manager;
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

        print(MiniMessage.miniMessage().deserialize("Starting generation <red>" + generation + "<white>..."));

        sleep(2000);

        String skinName = botSkin == null ? this.botName : botSkin;

        print(MiniMessage.miniMessage().deserialize("Fetching skin data for <green>" + skinName + "<white>..."));

        String[] skinData = MojangAPI.getSkin(skinName);

        String botName = this.botName.endsWith("%") ? this.botName : this.botName + "%";
        print(MiniMessage.miniMessage().deserialize("Creating " + (populationSize == 1 ? "new bot" : "<red>" + NumberFormat.getInstance(Locale.US).format(populationSize) + "<white> new bots")
                + " with name <green>" + botName.replace("%", "<light_purple>%<white>")
                + (botSkin == null ? "" : "<white> and skin <green>" + botSkin)
                + "<white>..."));

        Set<Map<BotNode, Map<BotDataType, Double>>> loadedProfiles = genProfiles.get(generation);
        Location loc = PlayerUtils.findAbove(primary.getLocation(), 20);

        scheduler.runTask(plugin, () -> {
            Set<Terminator> bots;

            if (loadedProfiles == null) {
                bots = manager.createBots(loc, botName, skinData, populationSize, NeuralNetwork.RANDOM);
            } else {
                List<NeuralNetwork> networks = new ArrayList<>();
                loadedProfiles.forEach(profile -> networks.add(NeuralNetwork.createNetworkFromProfile(profile)));

                if (populationSize != networks.size()) {
                    print("An exception has occured.");
                    print("The stored population size differs from the size of the stored networks.");
                    close();
                    return;
                }

                bots = manager.createBots(loc, botName, skinData, networks);
            }

            bots.forEach(bot -> {
                String name = bot.getBotName();

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

        print(MiniMessage.miniMessage().deserialize("Generation <red>" + generation + "<white> has ended."));

        HashMap<Terminator, Integer> values = new HashMap<>();

        for (Terminator bot : bots.values()) {
            values.put(bot, bot.getAliveTicks());
        }

        List<Map.Entry<Terminator, Integer>> sorted = MathUtils.sortByValue(values);
        Set<Terminator> winners = new HashSet<>();

        int i = 1;

        for (Map.Entry<Terminator, Integer> entry : sorted) {
            Terminator bot = entry.getKey();
            boolean check = i <= cutoff;
            if (check) {
                print(MiniMessage.miniMessage().deserialize("<gray>[<yellow>#" + i + "<gray>] <green>" + bot.getBotName()
                        + ChatUtils.BULLET_FORMATTED + "<red>" + bot.getKills() + " kills"));
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
        return (int) bots.values().stream().filter(Terminator::isBotAlive).count();
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
        Component message = MiniMessage.miniMessage().deserialize("<dark_green>[REINFORCEMENT] <white>" + String.join(" ", Arrays.stream(objects).map(String::valueOf).toArray(String[]::new)));

        users.forEach(u -> u.sendMessage(message));
        // log -> MiniMessage.miniMessage().stripTags(message.toString());
    }

    private void setup() {
        clearBots();

        if (populationSize < cutoff) {
            populationSize = cutoff;
            print(MiniMessage.miniMessage().deserialize("The input value for the population size is lower than the cutoff (<red>" + cutoff + "<white>)! "
                    + "The new population size is <red>" + populationSize + "<white>."));
        }

        if (!(manager.getAgent() instanceof LegacyAgent)) {
            print(MiniMessage.miniMessage().deserialize("The AI manager currently only supports <aqua>LegacyAgent<white>."));
            close();
            return;
        }

        agent = (LegacyAgent) manager.getAgent();
        agent.setTargetType(EnumTargetGoal.NONE);

        print(MiniMessage.miniMessage().deserialize("The bot target goal has been set to <yellow>" + EnumTargetGoal.NONE.name() + "<white>."));
        print("Disabling target offsets...");

        agent.offsets = false;

        print("Disabling bot drops...");

        agent.setDrops(false);

        print(MiniMessage.miniMessage().deserialize("<green>Setup is now complete."));
    }

    private void clearBots() {
        if (!bots.isEmpty()) {
            print("Removing all cached bots...");

            bots.values().forEach(Terminator::removeBot);
            bots.clear();
        }

        /*print("Removing all current bots...");

        int size = manager.fetch().size();
        manager.reset();

        String formatted = NumberFormat.getNumberInstance(Locale.US).format(size);
        print(MiniMessage.miniMessage().deserialize("Removed <red>" + formatted + "<white> entit" + (size == 1 ? "y" : "ies") + "."));

        bots.clear();*/
    }
}
