package net.nuggetmc.tplus.api;

import net.nuggetmc.tplus.api.agent.Agent;
import net.nuggetmc.tplus.api.agent.legacyagent.ai.NeuralNetwork;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface BotManager {
    Set<Terminator> fetch();

    Agent getAgent();

    void add(Terminator bot);

    Terminator getFirst(String name, Location target);

    List<String> fetchNames();

    void createBots(Player sender, String name, String skinName, int n);

    void createBots(Player sender, String name, String skinName, int n, NeuralNetwork network);

    Set<Terminator> createBots(Location loc, String name, String[] skin, List<NeuralNetwork> networks);

    Set<Terminator> createBots(Location loc, String name, String[] skin, int n, NeuralNetwork network);

    void remove(Terminator bot);

    void reset();

    /**
     * Get a bot from a Player object
     *
     * @param player
     * @return
     * @deprecated Use {@link #getBot(UUID)} instead as this may no longer work
     */
    @Deprecated
    Terminator getBot(Player player);

    Terminator getBot(UUID uuid);

    Terminator getBot(int entityId);

    boolean isMobTarget();

    void setMobTarget(boolean mobTarget);

    boolean addToPlayerList();

    void setAddToPlayerList(boolean addPlayerList);
}
