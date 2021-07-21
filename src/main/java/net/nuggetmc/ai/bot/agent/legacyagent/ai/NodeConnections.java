package net.nuggetmc.ai.bot.agent.legacyagent.ai;

public class NodeConnections {

    private final double connectionX; // horizontal distance
    private final double connectionY; // vertical distance
    private final double connectionB; // enemy blocking
    private final double connectionH; // health

    public NodeConnections() {
        this.connectionX = generate();
        this.connectionY = generate();
        this.connectionB = generate();
        this.connectionH = generate();
    }

    public NodeConnections(double y, double b, double t, double h) {
        this.connectionX = t;
        this.connectionY = y;
        this.connectionB = b;
        this.connectionH = h;
    }

    private double generate() {
        return Math.random() * 20 - 10;
    }

    public double getX() {
        return connectionX;
    }

    public double getY() {
        return connectionY;
    }

    public double getB() {
        return connectionB;
    }

    public double getH() {
        return connectionH;
    }

    public boolean test(double y, double b, double t, double h) {
        return Math.tanh(y * connectionX + b * connectionY + t * connectionB + h * connectionH) >= 0.5;
    }
}
