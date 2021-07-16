package net.nuggetmc.ai.bot.agent.legacyagent;

import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.PacketPlayOutBlockBreakAnimation;
import net.nuggetmc.ai.bot.Bot;
import net.nuggetmc.ai.bot.BotManager;
import net.nuggetmc.ai.bot.agent.Agent;
import net.nuggetmc.ai.utils.MathUtils;
import net.nuggetmc.ai.utils.PlayerUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

// Yes, this code is very unoptimized, I know.
public class LegacyAgent extends Agent {

    private final LegacyBlockCheck blockCheck;

    public LegacyAgent(BotManager manager) {
        super(manager);

        this.blockCheck = new LegacyBlockCheck(this);
    }

    private final Map<Player, BukkitRunnable> miningAnim = new HashMap<>();
    private final Set<Boat> boats = new HashSet<>();

    private final Map<Player, Location> btList = new HashMap<>();
    private final Map<Player, Boolean> btCheck = new HashMap<>();
    private final Map<Player, Location> towerList = new HashMap<>();

    private final Set<Bot> boatCooldown = new HashSet<>();
    private final Map<Block, Short> crackList = new HashMap<>();
    private final Map<BukkitRunnable, Byte> mining = new HashMap<>();

    private final Set<Bot> fallDamageCooldown = new HashSet<>();

    public final Set<Bot> noFace = new HashSet<>();
    public final Set<Player> noJump = new HashSet<>();

    public final Set<Bot> slow = new HashSet<>();

    @Override
    protected void tick() {
        try {
            manager.fetch().forEach(this::tickBot);
        } catch (ConcurrentModificationException e) {
            // Yes this is a really bad way to deal with this issue, but in the future I will have a thing
            // where when bots die they will be added to a cleanup cache that will be ticked after this (which will be refactored
            // to the BotManager) and will be removed separately from the set.
        }
    }

    private void center(Bot bot) {
        if (bot == null || !bot.isAlive()) {
            return;
        }

        final Player playerBot = bot.getBukkitEntity();

        Location prev = null;
        if (btList.containsKey(playerBot)) {
            prev = btList.get(playerBot);
        }

        Location loc = playerBot.getLocation();

        if (prev != null) {
            if (loc.getBlockX() == prev.getBlockX() && loc.getBlockZ() == prev.getBlockZ()) {
                btCheck.put(playerBot, true);
            } else {
                btCheck.put(playerBot, false);
            }
        }

        btList.put(playerBot, loc);
    }

    private void tickBot(Bot bot) {
        if (!bot.isAlive()) return;

        if (bot.tickDelay(20)) {
            center(bot);
        }

        Location loc = bot.getLocation();

        Player player = nearestPlayer(bot, loc);
        if (player == null) {
            // LESSLAG if (bot.tickDelay(20))
            stopMining(bot);
            return;
        }

        blockCheck.clutch(bot, player);

        Player playerBot = bot.getBukkitEntity();
        Location target = player.getLocation().add(bot.getOffset());

        if (bot.tickDelay(3) && !miningAnim.containsKey(playerBot)) {
            Location a = playerBot.getEyeLocation();
            Location b = player.getEyeLocation();
            Location c1 = player.getLocation();

            if (!LegacyUtils.checkIfBlocksOnVector(a, b) || !LegacyUtils.checkIfBlocksOnVector(a, c1)) {
                attack(bot, player, loc);
            }
        }

        boolean waterGround = (LegacyMats.WATER.contains(loc.clone().add(0, -0.1, 0).getBlock().getType())
                && !LegacyMats.AIR.contains(loc.clone().add(0, -0.6, 0).getBlock().getType()));

        boolean c = false, lc = false;

        if (btCheck.containsKey(playerBot)) lc = btCheck.get(playerBot);

        if (waterGround || bot.isOnGround() || onBoat(playerBot)) {
            byte j = 1;

            if (towerList.containsKey(playerBot)) {
                if (loc.getBlockY() > player.getLocation().getBlockY()) {
                    towerList.remove(playerBot);
                    resetHand(bot, player, playerBot);
                }
            }

            Block block = loc.clone().add(0, 1, 0).getBlock();

            if (Math.abs(loc.getBlockX() - target.getBlockX()) <= 3 &&
                    Math.abs(loc.getBlockZ() - target.getBlockZ()) <= 3) {
                c = true;
            }

            boolean bc = c || lc;

            // make this not destroy in scenarios where the bot can move out of the place
            if (checkAt(bot, block, playerBot)) {
                return;
            }

            else if (checkFence(bot, loc.getBlock(), playerBot)) {
                return;
            }

            else if (checkDown(bot, playerBot, player.getLocation(), bc)) {
                return;
            }

            else if ((c || lc) && checkUp(bot, player, playerBot, target, c)) {
                return;
            }

            else {
                if (bc) j = checkSide(bot, player, playerBot);

                switch (j) {
                    case 1:
                        resetHand(bot, player, playerBot);
                        if (!noJump.contains(playerBot)) {
                            if (!waterGround) move(bot, player, loc, target);
                        }
                        return;

                    case 2:
                        if (!waterGround) move(bot, player, loc, target);
                        return;
                }
            }
        } else if (LegacyMats.WATER.contains(loc.getBlock().getType())) {
            swim(bot, target, playerBot, player, LegacyMats.WATER.contains(loc.clone().add(0, -1, 0).getBlock().getType()));
        }
    }

    private void swim(Bot bot, Location loc, Player playerNPC, Player ply, boolean anim) {
        playerNPC.setSneaking(false);

        Location at = bot.getLocation();

        Vector vector = loc.toVector().subtract(at.toVector());
        if (at.getBlockY() < ply.getLocation().getBlockY()) {
            vector.setY(0);
        }

        vector.normalize().multiply(0.05);
        vector.setY(vector.getY() * 1.2);

        if (miningAnim.containsKey(playerNPC)) {
            BukkitRunnable task = miningAnim.get(playerNPC);
            if (task != null) {
                task.cancel();
                miningAnim.remove(playerNPC);
            }
        }

        if (anim) {
            bot.swim();
        } else {
            vector.setY(0);
            vector.multiply(0.7);
        }

        bot.faceLocation(ply.getLocation());
        bot.addVelocity(vector);
    }

    private void stopMining(Bot bot) {
        Player playerNPC = bot.getBukkitEntity();
        if (miningAnim.containsKey(playerNPC)) {
            BukkitRunnable task = miningAnim.get(playerNPC);
            if (task != null) {
                task.cancel();
                miningAnim.remove(playerNPC);
            }
        }
    }

    /*private void moveSmall(Bot bot, Location loc, Location target) {
        Vector vel = target.toVector().subtract(loc.toVector()).setY(0).normalize();

        bot.stand(); // eventually create a memory system so packets do not have to be sent every tick

        try {
            Vector newVel = bot.velocity.clone().add(vel);
            if (newVel.length() > 1) newVel.normalize();
            bot.addVelocity(newVel.multiply(0.01));
        } catch (IllegalArgumentException ignored) { }
    }*/

    private void move(Bot bot, Player player, Location loc, Location target) {
        Vector vel = target.toVector().subtract(loc.toVector()).normalize();

        if (bot.tickDelay(5)) bot.faceLocation(player.getLocation());
        if (!bot.isOnGround()) return; // calling this a second time later on

        bot.stand(); // eventually create a memory system so packets do not have to be sent every tick
        bot.setItem(null); // method to check item in main hand, bot.getItemInHand()

        try {
            vel.add(bot.velocity);
        } catch (IllegalArgumentException e) {
            if (!MathUtils.isFinite(vel)) {
                MathUtils.clean(vel);
            }
        }

        if (vel.length() > 1) vel.normalize();

        if (loc.distance(target) <= 5) {
            vel.multiply(0.3);
        } else {
            vel.multiply(0.4);
        }

        if (slow.contains(bot)) {
            vel.setY(0).multiply(0.5);
        } else {
            vel.setY(0.4);
        }

        bot.jump(vel);
    }

    private byte checkSide(Bot npc, Player player, Player playerNPC) {  // make it so they don't jump when checking side
        Location a = playerNPC.getEyeLocation();
        Location b = player.getLocation().add(0, 1, 0);

        if (npc.getLocation().distance(player.getLocation()) < 2.9 && !LegacyUtils.checkIfBlocksOnVector(a, b)) {
            resetHand(npc, player, playerNPC);
            return 1;
        }

        LegacyLevel h = checkNearby(player, npc);

        if (h == null) {
            resetHand(npc, player, playerNPC);
            return 1;
        } else if (h.isSide()) {
            return 0;
        } else {
            return 2;
        }
    }

    private LegacyLevel checkNearby(Player ply, Bot npc) {
        Player player = npc.getBukkitEntity();

        npc.faceLocation(ply.getLocation());

        BlockFace dir = player.getFacing();
        LegacyLevel level = null;
        Block get = null;

        switch (dir) {
            case NORTH:
                get = player.getLocation().add(0, 1, -1).getBlock();
                if (checkSideBreak(get.getType())) {
                    level = LegacyLevel.NORTH;
                } else if (checkSideBreak(get.getLocation().add(0, -1, 0).getBlock().getType())) {
                    get = get.getLocation().add(0, -1, 0).getBlock();
                    level = LegacyLevel.NORTH_D;
                }
                break;
            case SOUTH:
                get = player.getLocation().add(0, 1, 1).getBlock();
                if (checkSideBreak(get.getType())) {
                    level = LegacyLevel.SOUTH;
                } else if (checkSideBreak(get.getLocation().add(0, -1, 0).getBlock().getType())) {
                    get = get.getLocation().add(0, -1, 0).getBlock();
                    level = LegacyLevel.SOUTH_D;
                }
                break;
            case EAST:
                get = player.getLocation().add(1, 1, 0).getBlock();
                if (checkSideBreak(get.getType())) {
                    level = LegacyLevel.EAST;
                } else if (checkSideBreak(get.getLocation().add(0, -1, 0).getBlock().getType())) {
                    get = get.getLocation().add(0, -1, 0).getBlock();
                    level = LegacyLevel.EAST_D;
                }
                break;
            case WEST:
                get = player.getLocation().add(-1, 1, 0).getBlock();
                if (checkSideBreak(get.getType())) {
                    level = LegacyLevel.WEST;
                } else if (checkSideBreak(get.getLocation().add(0, -1, 0).getBlock().getType())) {
                    get = get.getLocation().add(0, -1, 0).getBlock();
                    level = LegacyLevel.WEST_D;
                }
                break;
            default:
                break;
        }

        if (level == LegacyLevel.EAST_D || level == LegacyLevel.WEST_D || level == LegacyLevel.NORTH_D || level == LegacyLevel.SOUTH_D) {
            if (LegacyMats.AIR.contains(player.getLocation().add(0, 2, 0).getBlock().getType())
                    && LegacyMats.AIR.contains(get.getLocation().add(0, 2, 0).getBlock().getType())) {
                return null;
            }
        }

        if (level != null) {
            preBreak(npc, player, get, level);
        }

        return level;
    }

    private static boolean checkSideBreak(Material type) {
        return !LegacyMats.BREAK.contains(type) && !LegacyMats.LEAVES.contains(type);
    }

    private boolean checkUp(Bot npc, Player player, Player playerNPC, Location loc, boolean c) {
        Location a = playerNPC.getLocation();
        Location b = player.getLocation();

        a.setY(0);
        b.setY(0);

        boolean above = LegacyWorldManager.aboveGround(playerNPC.getLocation());

        BlockFace dir = playerNPC.getFacing();
        Block get;

        switch (dir) {
            case NORTH:
                get = playerNPC.getLocation().add(0, 1, -1).getBlock();
                break;
            case SOUTH:
                get = playerNPC.getLocation().add(0, 1, 1).getBlock();
                break;
            case EAST:
                get = playerNPC.getLocation().add(1, 1, 0).getBlock();
                break;
            case WEST:
                get = playerNPC.getLocation().add(-1, 1, 0).getBlock();
                break;
            default:
                get = null;
        }

        if (get == null || LegacyMats.BREAK.contains(get.getType())) {
            if (a.distance(b) >= 16 && above) return false;
        }

        if (playerNPC.getLocation().getBlockY() < player.getLocation().getBlockY() - 1) {
            Material m0 = playerNPC.getLocation().getBlock().getType();
            Material m1 = playerNPC.getLocation().add(0, 1, 0).getBlock().getType();
            Material m2 = playerNPC.getLocation().add(0, 2, 0).getBlock().getType();

            if (LegacyMats.BREAK.contains(m0) && LegacyMats.BREAK.contains(m1) && LegacyMats.BREAK.contains(m2)) {

                npc.setItem(new ItemStack(Material.COBBLESTONE));

                Block place = playerNPC.getLocation().getBlock();

                if (miningAnim.containsKey(playerNPC)) {
                    BukkitRunnable task = miningAnim.get(playerNPC);
                    if (task != null) {
                        task.cancel();
                        miningAnim.remove(playerNPC);
                    }
                }

                npc.look(BlockFace.DOWN);

                // maybe put this in lower if statement onGround()
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    npc.sneak();
                    npc.setItem(new ItemStack(Material.COBBLESTONE));
                    npc.punch();
                    npc.look(BlockFace.DOWN);

                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        npc.look(BlockFace.DOWN);
                    }, 1);

                    blockCheck.placeBlock(npc, playerNPC, place);

                    if (!towerList.containsKey(playerNPC)) {
                        if (c) {
                            towerList.put(playerNPC, playerNPC.getLocation());
                        }
                    }
                }, 5);

                if (npc.isOnGround()) {
                    if (player.getLocation().distance(playerNPC.getLocation()) < 16) {
                        if (noJump.contains(playerNPC)) {

                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                npc.setVelocity(new Vector(0, 0.5, 0));
                            }, 1);

                        } else {
                            Vector vector = loc.toVector().subtract(playerNPC.getLocation().toVector()).normalize();
                            npc.stand();

                            Vector move = npc.getVelocity().add(vector);
                            if (move.length() > 1) move = move.normalize();
                            move.multiply(0.1);
                            move.setY(0.5);

                            npc.setVelocity(move);
                            return true;
                        }
                    } else {
                        if (npc.isOnGround()) {
                            Location locBlock = playerNPC.getLocation();
                            locBlock.setX(locBlock.getBlockX() + 0.5);
                            locBlock.setZ(locBlock.getBlockZ() + 0.5);

                            Vector vector = locBlock.toVector().subtract(playerNPC.getLocation().toVector());
                            if (vector.length() > 1) vector = vector.normalize();
                            vector.multiply(0.1);
                            vector.setY(0.5);

                            npc.addVelocity(vector);
                            return true;
                        }
                    }
                }

                return false;

            } else if (LegacyMats.BREAK.contains(m0) && LegacyMats.BREAK.contains(m1) && !LegacyMats.BREAK.contains(m2)) {
                Block block = npc.getLocation().add(0, 2, 0).getBlock();
                npc.look(BlockFace.UP);
                preBreak(npc, playerNPC, block, LegacyLevel.ABOVE);

                if (npc.isOnGround()) {
                    Location locBlock = playerNPC.getLocation();
                    locBlock.setX(locBlock.getBlockX() + 0.5);
                    locBlock.setZ(locBlock.getBlockZ() + 0.5);

                    Vector vector = locBlock.toVector().subtract(playerNPC.getLocation().toVector());
                    if (vector.length() > 1) vector = vector.normalize();
                    vector.multiply(0.1);
                    vector.setY(0);

                    npc.addVelocity(vector);
                }

                return true;
            }
        }

        return false;
    }

    private boolean checkDown(Bot npc, Player player, Location loc, boolean c) { // possibly a looser check for c

        if (!LegacyUtils.checkIfBlocksOnVector(npc.getLocation(), loc) || !LegacyUtils.checkIfBlocksOnVector(player.getEyeLocation(), loc)) return false;

        if (c && npc.getLocation().getBlockY() > loc.getBlockY() + 1) {
            Block block = npc.getLocation().add(0, -1, 0).getBlock();
            npc.look(BlockFace.DOWN);

            downMine(npc, player, block);
            preBreak(npc, player, block, LegacyLevel.BELOW);
            return true;
        }

        else {
            Location a = loc.clone();
            Location b = player.getLocation();

            a.setY(0);
            b.setY(0);

            if (npc.getLocation().getBlockY() > loc.getBlockY() + 10 && a.distance(b) < 10) {
                Block block = npc.getLocation().add(0, -1, 0).getBlock();
                npc.look(BlockFace.DOWN);

                downMine(npc, player, block);
                preBreak(npc, player, block, LegacyLevel.BELOW);
                return true;

            } else {
                return false;
            }
        }
    }

    private void downMine(Bot npc, Player player, Block block) {
        if (!LegacyMats.NO_CRACK.contains(block.getType())) {
            Location locBlock = player.getLocation();
            locBlock.setX(locBlock.getBlockX() + 0.5);
            locBlock.setZ(locBlock.getBlockZ() + 0.5);

            Vector vector = locBlock.toVector().subtract(player.getLocation().toVector());
            if (vector.length() > 1) vector = vector.normalize();
            vector.setY(0);
            vector.multiply(0.1);
            npc.setVelocity(vector);
        }

        if (npc.isInWater()) {
            Location locBlock = player.getLocation();
            locBlock.setX(locBlock.getBlockX() + 0.5);
            locBlock.setZ(locBlock.getBlockZ() + 0.5);

            Vector vector = locBlock.toVector().subtract(player.getLocation().toVector());
            if (vector.length() > 1) vector = vector.normalize();
            vector.multiply(0.3);
            vector.setY(-1);

            if (!fallDamageCooldown.contains(npc)) {
                fallDamageCooldown.add(npc);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    fallDamageCooldown.remove(npc);
                }, 10);
            }

            npc.setVelocity(vector);
        }
    }

    private boolean checkFence(Bot bot, Block block, Player player) {
        if (LegacyMats.FENCE.contains(block.getType())) {
            preBreak(bot, player, block, LegacyLevel.AT_D);
            return true;
        }

        return false;
    }

    private boolean checkAt(Bot bot, Block block, Player player) {
        if (LegacyMats.BREAK.contains(block.getType())) {
            return false;
        } else {
            preBreak(bot, player, block, LegacyLevel.AT);
            return true;
        }
    }

    private void preBreak(Bot bot, Player player, Block block, LegacyLevel level) {
        Material item;
        Material type = block.getType();

        if (LegacyMats.SHOVEL.contains(type)) {
            item = LegacyItems.SHOVEL;
        } else if (LegacyMats.AXE.contains(type)) {
            item = LegacyItems.AXE;
        } else {
            item = LegacyItems.PICKAXE;
        }

        bot.setItem(new ItemStack(item));

        if (level == LegacyLevel.EAST_D || level == LegacyLevel.NORTH_D || level == LegacyLevel.SOUTH_D || level == LegacyLevel.WEST_D) {
            bot.pitch = 69;

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                btCheck.put(player, true);
            }, 5);
        } else if (level == LegacyLevel.AT_D || level == LegacyLevel.AT) {
            Location blockLoc = block.getLocation().add(0.5, -1, 0.5);
            bot.faceLocation(blockLoc);
        }

        if (!miningAnim.containsKey(player)) {

            BukkitRunnable task = new BukkitRunnable() {

                @Override
                public void run() {
                   bot.punch();
                }
            };

            task.runTaskTimer(plugin, 0, 4);
            taskList.add(task);
            miningAnim.put(player, task);
        }

        blockBreakEffect(player, block, level);
    }

    private void blockBreakEffect(Player player, Block block, LegacyLevel level) {

        if (LegacyMats.NO_CRACK.contains(block.getType())) return;

        if (!crackList.containsKey(block)) {
            BukkitRunnable task = new BukkitRunnable() {

                @Override
                public void run() {
                    byte i = mining.get(this);

                    Block cur;
                    switch (level) {
                        case ABOVE:
                            cur = player.getLocation().add(0, 2, 0).getBlock();
                            break;
                        case BELOW:
                            cur = player.getLocation().add(0, -1, 0).getBlock();
                            break;
                        case NORTH:
                            cur = player.getLocation().add(0, 1, -1).getBlock();
                            break;
                        case SOUTH:
                            cur = player.getLocation().add(0, 1, 1).getBlock();
                            break;
                        case EAST:
                            cur = player.getLocation().add(1, 1, 0).getBlock();
                            break;
                        case WEST:
                            cur = player.getLocation().add(-1, 1, 0).getBlock();
                            break;
                        case NORTH_D:
                            cur = player.getLocation().add(0, 0, -1).getBlock();
                            break;
                        case SOUTH_D:
                            cur = player.getLocation().add(0, 0, 1).getBlock();
                            break;
                        case EAST_D:
                            cur = player.getLocation().add(1, 0, 0).getBlock();
                            break;
                        case WEST_D:
                            cur = player.getLocation().add(-1, 0, 0).getBlock();
                            break;
                        case AT_D:
                            cur = player.getLocation().getBlock();
                            break;
                        default:
                            cur = player.getLocation().add(0, 1, 0).getBlock();
                    }

                    // wow this repeated code is so bad lmao

                    if (player.isDead()) {
                        this.cancel();

                        PacketPlayOutBlockBreakAnimation crack = new PacketPlayOutBlockBreakAnimation(crackList.get(block), new BlockPosition(block.getX(), block.getY(), block.getZ()), -1);
                        for (Player all : Bukkit.getOnlinePlayers()) {
                            ((CraftPlayer) all).getHandle().playerConnection.sendPacket(crack);
                        }

                        crackList.remove(block);
                        mining.remove(this);
                        return;
                    }

                    if (!block.equals(cur) || block.getType() != cur.getType()) {
                        this.cancel();

                        PacketPlayOutBlockBreakAnimation crack = new PacketPlayOutBlockBreakAnimation(crackList.get(block), new BlockPosition(block.getX(), block.getY(), block.getZ()), -1);
                        for (Player all : Bukkit.getOnlinePlayers()) {
                            ((CraftPlayer) all).getHandle().playerConnection.sendPacket(crack);
                        }

                        crackList.remove(block);
                        mining.remove(this);
                        return;
                    }

                    Sound sound = LegacyUtils.breakBlockSound(block);

                    if (i == 9) {
                        this.cancel();

                        PacketPlayOutBlockBreakAnimation crack = new PacketPlayOutBlockBreakAnimation(crackList.get(block), new BlockPosition(block.getX(), block.getY(), block.getZ()), -1);
                        for (Player all : Bukkit.getOnlinePlayers()) {
                            ((CraftPlayer) all).getHandle().playerConnection.sendPacket(crack);
                        }

                        if (sound != null) {
                            for (Player all : Bukkit.getOnlinePlayers()) all.playSound(block.getLocation(), sound, SoundCategory.BLOCKS, 1, 1);
                        }

                        block.breakNaturally();

                        if (level == LegacyLevel.ABOVE) {
                            noJump.add(player);

                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                noJump.remove(player);
                            }, 15);
                        }

                        crackList.remove(block);
                        mining.remove(this);
                        return;
                    }

                    if (sound != null) {
                        for (Player all : Bukkit.getOnlinePlayers()) all.playSound(block.getLocation(), sound, SoundCategory.BLOCKS, (float) 0.3, 1);
                    }

                    if (block.getType() == Material.BARRIER || block.getType() == Material.BEDROCK || block.getType() == Material.END_PORTAL_FRAME) return;

                    PacketPlayOutBlockBreakAnimation crack = new PacketPlayOutBlockBreakAnimation(crackList.get(block), new BlockPosition(block.getX(), block.getY(), block.getZ()), i);
                    for (Player all : Bukkit.getOnlinePlayers()) {
                        ((CraftPlayer) all).getHandle().playerConnection.sendPacket(crack);
                    }

                    mining.put(this, (byte) (i + 1));
                }
            };

            taskList.add(task);
            mining.put(task, (byte) 0);
            crackList.put(block, (short) random.nextInt(2000));
            task.runTaskTimer(plugin, 0, 2);
        }
    }

    private void resetHand(Bot npc, Player player, Player playerNPC) {
        if (!noFace.contains(npc)) { // LESSLAG if there is no if statement here
            npc.faceLocation(player.getLocation());
        }

        if (miningAnim.containsKey(playerNPC)) {
            BukkitRunnable task = miningAnim.get(playerNPC);
            if (task != null) {
                task.cancel();
                miningAnim.remove(playerNPC);
            }
        }

        if (boatCooldown.contains(npc)) return;

        npc.setItem(null);
    }

    private boolean onBoat(Player player) {
        Set<Boat> cache = new HashSet<>();

        boolean check = false;

        for (Boat boat : boats) {
            if (player.getWorld() != boat.getWorld()) continue;

            if (boat.isDead()) {
                cache.add(boat);
                continue;
            }

            if (player.getLocation().distance(boat.getLocation()) < 1) {
                check = true;
                break;
            }
        }

        boats.removeAll(cache);

        return check;
    }

    private void attack(Bot bot, Player player, Location loc) {
        if (!PlayerUtils.isVulnerableGameMode(player.getGameMode()) || player.getNoDamageTicks() >= 5 || loc.distance(player.getLocation()) >= 4) return;

        bot.attack(player);
    }

    private Player nearestPlayer(Bot bot, Location loc) {
        return nearestBot(bot, loc);
    }

    private Player nearestRealPlayer(Location loc) {
        Player result = null;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!PlayerUtils.isTargetable(player.getGameMode()) || loc.getWorld() != player.getWorld()) continue;

            if (result == null || loc.distance(player.getLocation()) < loc.distance(result.getLocation())) {
                result = player;
            }
        }

        return result;
    }

    private Player nearestBot(Bot bot, Location loc) {
        Player result = null;

        for (Bot otherBot : manager.fetch()) {
            if (bot == otherBot) continue;

            Player player = otherBot.getBukkitEntity();

            if (!bot.getName().equals(otherBot.getName())) {
                if (loc.getWorld() != player.getWorld()) continue;

                if (result == null || loc.distance(player.getLocation()) < loc.distance(result.getLocation())) {
                    result = player;
                }
            }
        }

        return result;
    }
}
