package net.nuggetmc.ai.utils;

import net.nuggetmc.ai.PlayerAI;
import net.nuggetmc.ai.bot.agent.BotAgent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.beans.Statement;

public class Debugger {

    private CommandSender sender;
    private String prefix;

    public Debugger(CommandSender sender) {
        this.sender = sender;
        this.prefix = ChatColor.YELLOW + "[DEBUG] " + ChatColor.RESET;
    }

    private void print(String msg) {
        sender.sendMessage(msg);
    }

    public void execute(String cmd) {
        try {
            Statement statement = new Statement(this, cmd.replace("()", ""), new Object[]{});
            print(prefix + "Running the expression \"" + ChatColor.AQUA + cmd + ChatColor.RESET + "\"...");
            statement.execute();
        }

        catch (Exception e) {
            print(prefix + "Error: the expression \"" + ChatColor.AQUA + cmd + ChatColor.RESET + "\" failed to execute.");
            print(prefix + e.toString());
        }
    }

    public void toggleAgent() {
        BotAgent agent = PlayerAI.getInstance().getManager().getAgent();

        boolean b = agent.isEnabled();
        agent.setEnabled(!b);

        print(prefix + "The Bot Agent is now "
                + (b ? ChatColor.RED + "DISABLED" : ChatColor.GREEN + "ENABLED")
                + ChatColor.RESET + ".");
    }
}
