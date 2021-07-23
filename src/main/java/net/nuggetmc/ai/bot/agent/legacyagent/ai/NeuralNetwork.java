package net.nuggetmc.ai.bot.agent.legacyagent.ai;

import net.md_5.bungee.api.ChatColor;
import net.nuggetmc.ai.utils.MathUtils;
import net.nuggetmc.ai.utils.StringUtilities;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public class NeuralNetwork {

    // thinking about making an enum called BotNode, and have a map here, .check(Node.L) or fetch
    // also randomize activation point between 0 and 0.5
    // randomize the blocking length and cooldown
    // also the XZ offset randomizers!! (or maybe just turn them off entirely, that works too)

    private final Map<BotNode, NodeConnections> nodes;

    private NeuralNetwork(BotNode... nodes) {
        this.nodes = new HashMap<>();

        Arrays.stream(nodes).forEach(n -> this.nodes.put(n, new NodeConnections()));
    }

    public static NeuralNetwork generateRandomNetwork() {
        return new NeuralNetwork(BotNode.values());
    }

    public NodeConnections fetch(BotNode node) {
        return nodes.get(node);
    }

    public boolean check(BotNode node) {
        return nodes.get(node).check();
    }

    public void feed(BotData data) {
        nodes.values().forEach(n -> n.test(data));
    }

    public String output() {
        return generateString(false);
    }

    @Override
    public String toString() {
        return generateString(true);
    }

    private String generateString(boolean values) {
        List<String> strings = new ArrayList<>();

        if (values) {
            nodes.forEach((type, node) -> {
                double value = node.value();
                strings.add(type.name().toLowerCase() + "=" + (value >= 0.5 ? StringUtilities.ON : StringUtilities.OFF) + MathUtils.round2Dec(value) + ChatColor.RESET);
            });
        } else {
            nodes.forEach((type, node) -> strings.add(type.name().toLowerCase() + "=" + (node.check() ? StringUtilities.ON + "1" : StringUtilities.OFF + "0") + ChatColor.RESET));
        }

        Collections.sort(strings);

        return "[" + StringUtils.join(strings, ", ") + "]";
    }
}
