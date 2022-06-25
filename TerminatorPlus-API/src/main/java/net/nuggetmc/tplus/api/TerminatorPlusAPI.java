package net.nuggetmc.tplus.api;

public class TerminatorPlusAPI {
    private static InternalBridge internalBridge;
    private static BotManager botManager;

    public static InternalBridge getInternalBridge() {
        return internalBridge;
    }

    public static void setInternalBridge(InternalBridge internalBridge) {
        TerminatorPlusAPI.internalBridge = internalBridge;
    }

    public static BotManager getBotManager() {
        return botManager;
    }

    public static void setBotManager(BotManager botManager) {
        TerminatorPlusAPI.botManager = botManager;
    }
}
