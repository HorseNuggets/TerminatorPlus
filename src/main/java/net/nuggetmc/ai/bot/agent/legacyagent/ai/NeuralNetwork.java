package net.nuggetmc.ai.bot.agent.legacyagent.ai;

public class NeuralNetwork {

    private final NodeConnections nodeL; // left strafe
    private final NodeConnections nodeR; // right strafe (if L and R are opposite, move forward)
    private final NodeConnections nodeB; // block
    private final NodeConnections nodeJ; // jump

    public NeuralNetwork() {
        this.nodeL = new NodeConnections();
        this.nodeR = new NodeConnections();
        this.nodeB = new NodeConnections();
        this.nodeJ = new NodeConnections();
    }

    public void feed(BotData data) {

    }
}
