package net.nuggetmc.tplus;

import net.nuggetmc.tplus.api.TerminatorPlusAPI;
import net.nuggetmc.tplus.bot.BotManagerImpl;
import net.nuggetmc.tplus.bridge.InternalBridgeImpl;
import net.nuggetmc.tplus.command.CommandHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class TerminatorPlus extends JavaPlugin {

    public static final String REQUIRED_VERSION = "1.20.1";

    private static TerminatorPlus instance;
    private static String version;
    private static String mcVersion;

    private static boolean correctVersion;

    private BotManagerImpl manager;
    private CommandHandler handler;

    public static TerminatorPlus getInstance() {
        return instance;
    }

    public static String getVersion() {
        return version;
    }

    public BotManagerImpl getManager() {
        return manager;
    }

    public CommandHandler getHandler() {
        return handler;
    }

    @Override
    public void onEnable() {
        instance = this;
        version = getDescription().getVersion();

        String version = Bukkit.getVersion();
        correctVersion = version.contains(REQUIRED_VERSION);
        if (version.contains("MC:")) { // git-ABX-123 (MC: ABCD)
            version = version.substring(version.indexOf("MC:") + 3, version.indexOf(")")).trim();
        }
        mcVersion = version;
        getLogger().info("Running on version: " + version + ", required version: " + REQUIRED_VERSION + ", correct version: " + correctVersion);

        // Create Instances
        this.manager = new BotManagerImpl();
        this.handler = new CommandHandler(this);

        TerminatorPlusAPI.setBotManager(manager);
        TerminatorPlusAPI.setInternalBridge(new InternalBridgeImpl());

        // Register event listeners
        this.registerEvents(manager);

        if (!correctVersion) {
            for (int i = 0; i < 20; i++) { // Kids are stupid so we need to make sure they see this
                getLogger().severe("----------------------------------------");
                getLogger().severe("TerminatorPlus is not compatible with your server version!");
                getLogger().severe("You are running on version: " + version + ", required version: " + REQUIRED_VERSION);
                getLogger().severe("Either download the correct version of TerminatorPlus or update your server. (https://papermc.io/downloads)");
                getLogger().severe("----------------------------------------");
            }
        }
    }

    @Override
    public void onDisable() {
        manager.reset();
    }

    private void registerEvents(Listener... listeners) {
        Arrays.stream(listeners).forEach(li -> this.getServer().getPluginManager().registerEvents(li, this));
    }

    public static boolean isCorrectVersion() {
        return correctVersion;
    }

    public static String getMcVersion() {
        return mcVersion;
    }
}
