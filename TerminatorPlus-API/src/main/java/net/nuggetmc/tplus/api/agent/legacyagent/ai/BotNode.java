package net.nuggetmc.tplus.api.agent.legacyagent.ai;

public enum BotNode {
    BLOCK, // block (can't attack while blocking)
    JUMP, // jump
    LEFT, // left strafe
    RIGHT // right strafe (if L and R are opposite, move forward)
}
