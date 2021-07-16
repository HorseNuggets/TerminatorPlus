package net.nuggetmc.ai.utils;

import net.minecraft.server.v1_16_R3.*;
import net.nuggetmc.ai.PlayerAI;
import net.nuggetmc.ai.bot.Bot;
import net.nuggetmc.ai.bot.agent.Agent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.permissions.ServerOperator;

import java.beans.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Debugger {

    private static final String PREFIX = ChatColor.YELLOW + "[DEBUG] " + ChatColor.RESET;

    private final CommandSender sender;

    public Debugger(CommandSender sender) {
        this.sender = sender;
    }

    public static void log(Object... objects) {
        String[] values = formStringArray(objects);
        Bukkit.getOnlinePlayers().stream().filter(ServerOperator::isOp).forEach(p -> p.sendMessage(PREFIX + String.join(" ", values)));
    }

    private static String[] formStringArray(Object[] objects) {
        return Arrays.stream(objects).map(String::valueOf).toArray(String[]::new);
    }

    private void print(Object... objects) {
        sender.sendMessage(PREFIX + String.join(" ", formStringArray(objects)));
    }

    public void execute(String cmd) {
        try {
            int[] pts = {cmd.indexOf('('), cmd.indexOf(')')};
            if (pts[0] == -1 || pts[1] == -1) throw new IllegalArgumentException();

            String name = cmd.substring(0, pts[0]);
            String content = cmd.substring(pts[0] + 1, pts[1]);

            Statement statement = new Statement(this, name, content.isEmpty() ? null : new Object[]{content});
            print("Running the expression \"" + ChatColor.AQUA + cmd + ChatColor.RESET + "\"...");
            statement.execute();
        }

        catch (Exception e) {
            print("Error: the expression \"" + ChatColor.AQUA + cmd + ChatColor.RESET + "\" failed to execute.");
            print(e.toString());
        }
    }

    public Object[] buildObjects(String content) {
        List<Object> list = new ArrayList<>();

        if (!content.isEmpty()) {
            String[] values = content.split(",");

            for (String str : values) {
                list.add(str.startsWith(" ") ? str.substring(1) : str);
            }
        }

        return list.toArray();
    }

    public void fire(String content) {
        Object[] obj = buildObjects(content);

        if (obj.length != 1) {
            print("Invalid arguments!");
            return;
        }

        boolean b = Boolean.parseBoolean((String) obj[0]);

        PlayerAI.getInstance().getManager().fetch().forEach(bot -> bot.setOnFirePackets(b));
    }

    public void sendPackets(String content) {
        Object[] obj = buildObjects(content);

        if (obj.length != 1) {
            print("Invalid arguments!");
            return;
        }

        byte b;

        try {
            b = Byte.parseByte((String) obj[0]);
        } catch (NumberFormatException e) {
            print("Invalid arguments!");
            return;
        }

        PlayerAI.getInstance().getManager().fetch().forEach(bot -> {
            /*DataWatcher datawatcher = bot.getDataWatcher();
            datawatcher.set(DataWatcherRegistry.s.a(6), EntityPose.DYING);
            PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(bot.getId(), datawatcher, false);
            Bukkit.getOnlinePlayers().forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(metadata));

            PacketPlayOutEntityStatus packet = new PacketPlayOutEntityStatus(bot, b);
            Bukkit.getOnlinePlayers().forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet));*/
            bot.setHealth(0);
        });

        Debugger.log("PACKETS_SENT");
    }

    public void trackYVel() {
        if (!(sender instanceof Player)) return;

        Player player = (Player) sender;

        Bukkit.getScheduler().scheduleSyncRepeatingTask(PlayerAI.getInstance(), () -> {
            print(player.getVelocity().getY());
        }, 0, 1);
    }

    public void t() {
        Bukkit.dispatchCommand(sender, "bot debug t(" + !PlayerUtils.getAllTargetable() + ")");
    }

    public void t(String content) {
        Object[] obj = buildObjects(content);

        if (obj.length != 1) {
            print("Invalid arguments!");
            return;
        }

        PlayerUtils.setAllTargetable(Boolean.parseBoolean((String) obj[0]));
        String var = "PlayerUtils.allTargetable";

        if (PlayerUtils.getAllTargetable()) {
            print(var + " is now " + ChatColor.GREEN + "TRUE" + ChatColor.RESET + ".");
        } else {
            print(var + " is now " + ChatColor.RED + "FALSE" + ChatColor.RESET + ".");
        }
    }

    public void hideNametags() { // this works for some reason
        Set<Bot> bots = PlayerAI.getInstance().getManager().fetch();

        for (Bot bot : bots) {
            Location loc = bot.getLocation();
            World world = loc.getWorld();

            if (world == null) continue;

            loc.setX(loc.getBlockX());
            loc.setY(loc.getBlockY());
            loc.setZ(loc.getBlockZ());

            loc.add(0.5, 0.5, 0.5);

            ArmorStand seat = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
            seat.setVisible(false);
            seat.setSmall(true);

            bot.getBukkitEntity().setPassenger(seat);
        }
    }

    public void sit() {
        Set<Bot> bots = PlayerAI.getInstance().getManager().fetch();

        for (Bot bot : bots) {
            Location loc = bot.getLocation();
            World world = loc.getWorld();

            if (world == null) continue;

            loc.setX(loc.getBlockX());
            loc.setY(loc.getBlockY());
            loc.setZ(loc.getBlockZ());

            loc.add(0.5, -1.5, 0.5);

            ArmorStand seat = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
            seat.setVisible(false);
            seat.setGravity(false);
            seat.setSmall(true);

            seat.addPassenger(bot.getBukkitEntity());
        }
    }

    public void look() {
        if (!(sender instanceof Player)) {
            print("Unspecified player.");
            return;
        }

        Player player = (Player) sender;

        for (Bot bot : PlayerAI.getInstance().getManager().fetch()) {
            bot.faceLocation(player.getEyeLocation());
        }
    }

    public void printObj(String content) {
        if (content.isEmpty()) {
            print("null");
            return;
        }

        Arrays.stream(buildObjects(content)).forEach(this::print);
    }

    public void toggleAgent() {
        Agent agent = PlayerAI.getInstance().getManager().getAgent();

        boolean b = agent.isEnabled();
        agent.setEnabled(!b);

        print("The Bot Agent is now "
                + (b ? ChatColor.RED + "DISABLED" : ChatColor.GREEN + "ENABLED")
                + ChatColor.RESET + ".");
    }
}
