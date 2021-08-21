package net.nuggetmc.ai.command;

public abstract class CommandInstance {

    private String name;

    protected final CommandHandler commandHandler;

    public CommandInstance(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    protected void onLoad() { }
}
