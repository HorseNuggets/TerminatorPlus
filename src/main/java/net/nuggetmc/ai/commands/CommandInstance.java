package net.nuggetmc.ai.commands;

public abstract class CommandInstance {

    private final CommandHandler commandHandler;

    public CommandInstance(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }
}
