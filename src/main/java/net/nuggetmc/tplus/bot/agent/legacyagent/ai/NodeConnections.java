package net.nuggetmc.tplus.bot.agent.legacyagent.ai;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class NodeConnections {

    /*
     * more node ideas
     * how much the bot is to the left or right of target (not horizontal distance)
     * bot velocity?
     * if the player is temporarily invincible (has damage ticks > 0)
     */

    private final Map<BotDataType, Double> connections;

    private boolean active;

    private double value;

    public NodeConnections() {
        this.connections = new HashMap<>();

        Arrays.stream(BotDataType.values()).forEach(type -> connections.put(type, generateValue()));
    }

    public NodeConnections(Map<BotDataType, Double> values) {
        this.connections = values;
    }

    private double generateValue() {
        return ThreadLocalRandom.current().nextDouble(-10, 10);
    }

    public boolean check() {
        return active;
    }

    public double value() {
        return value;
    }

    public Map<BotDataType, Double> getValues() {
        return connections;
    }

    public double getValue(BotDataType dataType) {
        return connections.get(dataType);
    }

    /*
     * maybe a sinusoidal activation function?
     * maybe generate a random activation function?
     * definitely something less.. broad
     */
    public void test(BotData data) {
        this.activationFunction(data);
        this.active = this.value >= 0.5;
    }

    /*
     * try sin, sin x^2, cos, cos x^2
     */
    private void activationFunction(BotData data) {
        this.value = Math.tanh(data.getValues().entrySet().stream().mapToDouble(entry -> connections.get(entry.getKey()) * entry.getValue()).sum());
    }
}
