package net.nuggetmc.tplus.bot.agent.legacyagent.ai;

import net.md_5.bungee.api.ChatColor;
import net.nuggetmc.tplus.utils.MathUtils;
import net.nuggetmc.tplus.utils.ChatUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public class NeuralNetwork {

    // thinking about making an enum called BotNode, and have a map here, .check(Node.L) or fetch
    // also randomize activation point between 0 and 0.5
    // randomize the blocking length and cooldown
    // also the XZ offset randomizers!! (or maybe just turn them off entirely, that works too)

    // b, j, l, r, ox, oz, av, bl, bc, dlr

    private final Map<BotNode, NodeConnections> nodes;

    private final boolean dynamicLR;

    public static final NeuralNetwork RANDOM = new NeuralNetwork(new HashMap<>());

    private NeuralNetwork(Map<BotNode, Map<BotDataType, Double>> profile) {
        this.nodes = new HashMap<>();
        this.dynamicLR = true;

        if (profile == null) {
            Arrays.stream(BotNode.values()).forEach(n -> this.nodes.put(n, new NodeConnections()));
        } else {
            profile.forEach((nodeType, map) -> nodes.put(nodeType, new NodeConnections(map)));
        }
    }

    public static NeuralNetwork createNetworkFromProfile(Map<BotNode, Map<BotDataType, Double>> profile) {
        return new NeuralNetwork(profile);
    }

    public static NeuralNetwork generateRandomNetwork() {
        return new NeuralNetwork(null);
    }

    public NodeConnections fetch(BotNode node) {
        return nodes.get(node);
    }

    public boolean check(BotNode node) {
        return nodes.get(node).check();
    }

    public double value(BotNode node) {
        return nodes.get(node).value();
    }

    public void feed(BotData data) {
        nodes.values().forEach(n -> n.test(data));
    }

    public Map<BotNode, NodeConnections> nodes() {
        return nodes;
    }

    public boolean dynamicLR() {
        return dynamicLR;
    }

    public Map<BotNode, Map<BotDataType, Double>> values() {
        Map<BotNode, Map<BotDataType, Double>> output = new HashMap<>();
        nodes.forEach((nodeType, node) -> output.put(nodeType, node.getValues()));
        return output;
    }

    public String output() {
        List<String> strings = new ArrayList<>();
        nodes.forEach((type, node) -> strings.add(type.name().toLowerCase() + "=" + (node.check() ? ChatUtils.ON + "1" : ChatUtils.OFF + "0") + ChatColor.RESET));
        Collections.sort(strings);
        return "[" + StringUtils.join(strings, ", ") + "]";
    }

    @Override
    public String toString() {
        List<String> strings = new ArrayList<>();

        nodes.forEach((nodeType, node) -> {
            List<String> values = new ArrayList<>();
            values.add("name=\"" + nodeType.name().toLowerCase() + "\"");
            node.getValues().forEach((dataType, value) -> values.add(dataType.getShorthand() + "=" + MathUtils.round2Dec(value)));
            strings.add("{" + StringUtils.join(values, ",") + "}");
        });

        return "NeuralNetwork{nodes:[" + StringUtils.join(strings, ",") + "]}";
    }
}
