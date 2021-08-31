package net.nuggetmc.tplus.bot.agent.legacyagent;

import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.PacketPlayOutBlockBreakAnimation;
import net.nuggetmc.tplus.bot.Bot;
import net.nuggetmc.tplus.bot.BotManager;
import net.nuggetmc.tplus.bot.agent.Agent;
import net.nuggetmc.tplus.bot.agent.legacyagent.ai.BotData;
import net.nuggetmc.tplus.bot.agent.legacyagent.ai.BotNode;
import net.nuggetmc.tplus.bot.agent.legacyagent.ai.NeuralNetwork;
import net.nuggetmc.tplus.bot.event.BotDamageByPlayerEvent;
import net.nuggetmc.tplus.bot.event.BotDeathEvent;
import net.nuggetmc.tplus.bot.event.BotFallDamageEvent;
import net.nuggetmc.tplus.utils.MathUtils;
import net.nuggetmc.tplus.utils.PlayerUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Boat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Yes, this code is very unoptimized, I know.
public class LegacyAgent extends Agent {

    private final LegacyBlockCheck blockCheck;

    private EnumTargetGoal goal;

    public boolean offsets = true;

    public LegacyAgent(BotManager manager) {
        super(manager);

        this.goal = EnumTargetGoal.NEAREST_VULNERABLE_PLAYER;
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
        manager.fetch().forEach(this::tickBot);
    }

    private void center(Bot bot) {
        if (bot == null || !bot.isAlive()) {
            return;
        }

        final Player botPlayer = bot.getBukkitEntity();

        Location prev = null;
        if (btList.containsKey(botPlayer)) {
            prev = btList.get(botPlayer);
        }

        Location loc = botPlayer.getLocation();

        if (prev != null) {
            if (loc.getBlockX() == prev.getBlockX() && loc.getBlockZ() == prev.getBlockZ()) {
                btCheck.put(botPlayer, true);
            } else {
                btCheck.put(botPlayer, false);
            }
        }

        btList.put(botPlayer, loc);
    }

    private void tickBot(Bot bot) {
        if (!bot.isAlive()) {
            return;
        }

        if (bot.tickDelay(20)) {
            center(bot);
        }

        Location loc = bot.getLocation();
        LivingEntity livingTarget = locateTarget(bot, loc);

        if (livingTarget == null) {
            stopMining(bot);
            return;
        }

        blockCheck.clutch(bot, livingTarget);

        fallDamageCheck(bot);
        miscellaneousChecks(bot, livingTarget);

        Player botPlayer = bot.getBukkitEntity();
        Location target = offsets ? livingTarget.getLocation().add(bot.getOffset()) : livingTarget.getLocation();

        boolean ai = bot.hasNeuralNetwork();

        NeuralNetwork network = ai ? bot.getNeuralNetwork() : null;

        if (ai) {
            network.feed(BotData.generate(bot, livingTarget));
        }

        if (bot.tickDelay(3) && !miningAnim.containsKey(botPlayer)) {
            Location botEyeLoc = botPlayer.getEyeLocation();
            Location playerEyeLoc = livingTarget.getEyeLocation();
            Location playerLoc = livingTarget.getLocation();

            if (ai) {
                if (network.check(BotNode.BLOCK) && loc.distance(livingTarget.getLocation()) < 6) {
                    bot.block(10, 10);
                }
            }

            if (LegacyUtils.checkFreeSpace(botEyeLoc, playerEyeLoc) || LegacyUtils.checkFreeSpace(botEyeLoc, playerLoc)) {
                attack(bot, livingTarget, loc);
            }
        }

        boolean waterGround = (LegacyMats.WATER.contains(loc.clone().add(0, -0.1, 0).getBlock().getType())
                && !LegacyMats.AIR.contains(loc.clone().add(0, -0.6, 0).getBlock().getType()));

        boolean withinTargetXZ = false, sameXZ = false;

        if (btCheck.containsKey(botPlayer)) sameXZ = btCheck.get(botPlayer);

        if (waterGround || bot.isOnGround() || onBoat(botPlayer)) {
            byte sideResult = 1;

            if (towerList.containsKey(botPlayer)) {
                if (loc.getBlockY() > livingTarget.getLocation().getBlockY()) {
                    towerList.remove(botPlayer);
                    resetHand(bot, livingTarget, botPlayer);
                }
            }

            Block block = loc.clone().add(0, 1, 0).getBlock();

            if (Math.abs(loc.getBlockX() - target.getBlockX()) <= 3 &&
                    Math.abs(loc.getBlockZ() - target.getBlockZ()) <= 3) {
                withinTargetXZ = true;
            }

            boolean bothXZ = withinTargetXZ || sameXZ;

            if (checkAt(bot, block, botPlayer)) return;

            if (checkFence(bot, loc.getBlock(), botPlayer)) return;

            if (checkDown(bot, botPlayer, livingTarget.getLocation(), bothXZ)) return;

            if ((withinTargetXZ || sameXZ) && checkUp(bot, livingTarget, botPlayer, target, withinTargetXZ)) return;

            if (bothXZ) sideResult = checkSide(bot, livingTarget, botPlayer);

            switch (sideResult) {
                case 1:
                    resetHand(bot, livingTarget, botPlayer);
                    if (!noJump.contains(botPlayer) && !waterGround) move(bot, livingTarget, loc, target, ai);
                    return;

                case 2:
                    if (!waterGround) move(bot, livingTarget, loc, target, ai);
            }
        }

        else if (LegacyMats.WATER.contains(loc.getBlock().getType())) {
            swim(bot, target, botPlayer, livingTarget, LegacyMats.WATER.contains(loc.clone().add(0, -1, 0).getBlock().getType()));
        }
    }

    private void move(Bot bot, LivingEntity livingTarget, Location loc, Location target, boolean ai) {
        Vector position = loc.toVector();
        Vector vel = target.toVector().subtract(position).normalize();

        if (bot.tickDelay(5)) bot.faceLocation(livingTarget.getLocation());
        if (!bot.isOnGround()) return; // calling this a second time later on

        bot.stand(); // eventually create a memory system so packets do not have to be sent every tick
        bot.setItem(null); // method to check item in main hand, bot.getItemInHand()

        try {
            vel.add(bot.getVelocity());
        } catch (IllegalArgumentException e) {
            if (MathUtils.isNotFinite(vel)) {
                MathUtils.clean(vel);
            }
        }

        if (vel.length() > 1) vel.normalize();

        double distance = loc.distance(target);

        if (distance <= 5) {
            vel.multiply(0.3);
        } else {
            vel.multiply(0.4);
        }

        if (slow.contains(bot)) {
            vel.setY(0).multiply(0.5);
        } else {
            vel.setY(0.4);
        }

        vel.setY(vel.getY() - Math.random() * 0.05);

        if (ai) {
            NeuralNetwork network = bot.getNeuralNetwork();

            if (network.dynamicLR()) {
                if (bot.isBlocking()) {
                    vel.multiply(0.6);
                }

                if (distance <= 6) {

                    // positive y rotation means left, negative means right
                    // if left > right, value will be positive

                    double value = network.value(BotNode.LEFT) - network.value(BotNode.RIGHT);

                    vel.rotateAroundY(value * Math.PI / 8);

                    if (network.check(BotNode.JUMP)) {
                        bot.jump(vel);
                    } else {
                        bot.walk(vel.clone().setY(0));
                        scheduler.runTaskLater(plugin, () -> bot.jump(vel), 10);
                    }

                    return;
                }
            }

            else {
                boolean left = network.check(BotNode.LEFT);
                boolean right = network.check(BotNode.RIGHT);

                if (bot.isBlocking()) {
                    vel.multiply(0.6);
                }

                if (left != right && distance <= 6) {

                    if (left) {
                        vel.rotateAroundY(Math.PI / 4);
                    }

                    if (right) {
                        vel.rotateAroundY(-Math.PI / 4);
                    }

                    if (network.check(BotNode.JUMP)) {
                        bot.jump(vel);
                    } else {
                        bot.walk(vel.clone().setY(0));
                        scheduler.runTaskLater(plugin, () -> bot.jump(vel), 10);
                    }

                    return;
                }
            }
        }

        bot.jump(vel);
    }

    private void fallDamageCheck(Bot bot) {
        if (bot.isFalling()) {
            bot.look(BlockFace.DOWN);

            Material itemType;

            if (bot.getBukkitEntity().getWorld().getEnvironment() == World.Environment.NETHER) {
                itemType = Material.TWISTING_VINES;
            } else {
                itemType = Material.WATER_BUCKET;
            }

            bot.setItem(new ItemStack(itemType));
        }
    }

    @Override
    public void onBotDeath(BotDeathEvent event) {
        if (!drops) {
            event.getDrops().clear();
        }
    }

    @Override
    public void onPlayerDamage(BotDamageByPlayerEvent event) {
        Bot bot = event.getBot();
        Location loc = bot.getLocation();
        Player player = event.getPlayer();

        double dot = loc.toVector().subtract(player.getLocation().toVector()).normalize().dot(loc.getDirection());

        if (bot.isBlocking() && dot >= -0.1) {
            player.getWorld().playSound(bot.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1, 1);
            event.setCancelled(true);
        }
    }

    @Override
    public void onFallDamage(BotFallDamageEvent event) {
        Bot bot = event.getBot();
        World world = bot.getBukkitEntity().getWorld();

        bot.look(BlockFace.DOWN);

        Material itemType;
        Material placeType;
        Sound sound;

        if (bot.getBukkitEntity().getWorld().getEnvironment() == World.Environment.NETHER) {
            itemType = Material.TWISTING_VINES;
            sound = Sound.BLOCK_WEEPING_VINES_PLACE;
            placeType = itemType;
        } else {
            itemType = Material.WATER_BUCKET;
            sound = Sound.ITEM_BUCKET_EMPTY;
            placeType = Material.WATER;
        }

        Location loc = bot.getLocation();

        if (!loc.clone().add(0, -1, 0).getBlock().getType().isSolid()) return;

        event.setCancelled(true);

        if (loc.getBlock().getType() != placeType) {
            bot.punch();
            loc.getBlock().setType(placeType);
            world.playSound(loc, sound, 1, 1);

            if (itemType == Material.WATER_BUCKET) {
                bot.setItem(new ItemStack(Material.BUCKET));

                scheduler.runTaskLater(plugin, () -> {
                    Block block = loc.getBlock();

                    if (block.getType() == Material.WATER) {
                        bot.look(BlockFace.DOWN);
                        bot.setItem(new ItemStack(Material.WATER_BUCKET));
                        world.playSound(loc, Sound.ITEM_BUCKET_FILL, 1, 1);
                        block.setType(Material.AIR);
                    }
                }, 5);
            }
        }
    }

    private void swim(Bot bot, Location loc, Player playerNPC, LivingEntity target, boolean anim) {
        playerNPC.setSneaking(false);

        Location at = bot.getLocation();

        Vector vector = loc.toVector().subtract(at.toVector());
        if (at.getBlockY() < target.getLocation().getBlockY()) {
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

        bot.faceLocation(target.getLocation());
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

    private byte checkSide(Bot npc, LivingEntity target, Player playerNPC) {  // make it so they don't jump when checking side
        Location a = playerNPC.getEyeLocation();
        Location b = target.getLocation().add(0, 1, 0);

        if (npc.getLocation().distance(target.getLocation()) < 2.9 && LegacyUtils.checkFreeSpace(a, b)) {
            resetHand(npc, target, playerNPC);
            return 1;
        }

        LegacyLevel level = checkNearby(target, npc);

        if (level == null) {
            resetHand(npc, target, playerNPC);
            return 1;
        } else if (level.isSide()) {
            return 0;
        } else {
            return 2;
        }
    }

    private LegacyLevel checkNearby(LivingEntity target, Bot npc) {
        Player player = npc.getBukkitEntity();

        npc.faceLocation(target.getLocation());

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
        return !LegacyMats.BREAK.contains(type);// && !LegacyMats.LEAVES.contains(type);
    }

    private boolean checkUp(Bot npc, LivingEntity target, Player playerNPC, Location loc, boolean c) {
        Location a = playerNPC.getLocation();
        Location b = target.getLocation();

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

        if (playerNPC.getLocation().getBlockY() < target.getLocation().getBlockY() - 1) {
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
                scheduler.runTaskLater(plugin, () -> {
                    npc.sneak();
                    npc.setItem(new ItemStack(Material.COBBLESTONE));
                    npc.punch();
                    npc.look(BlockFace.DOWN);

                    scheduler.runTaskLater(plugin, () -> {
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
                    if (target.getLocation().distance(playerNPC.getLocation()) < 16) {
                        if (noJump.contains(playerNPC)) {

                            scheduler.runTaskLater(plugin, () -> {
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

        if (LegacyUtils.checkFreeSpace(npc.getLocation(), loc) || LegacyUtils.checkFreeSpace(player.getEyeLocation(), loc)) return false;

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

                scheduler.runTaskLater(plugin, () -> {
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

            scheduler.runTaskLater(plugin, () -> {
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

                            scheduler.runTaskLater(plugin, () -> {
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

    private void placeWaterDown(Bot bot, World world, Location loc) {
        if (loc.getBlock().getType() == Material.WATER) return;

        bot.look(BlockFace.DOWN);
        bot.punch();
        loc.getBlock().setType(Material.WATER);
        world.playSound(loc, Sound.ITEM_BUCKET_EMPTY, 1, 1);
        bot.setItem(new org.bukkit.inventory.ItemStack(Material.BUCKET));

        scheduler.runTaskLater(plugin, () -> {
            Block block = loc.getBlock();

            if (block.getType() == Material.WATER) {
                bot.look(BlockFace.DOWN);
                bot.setItem(new ItemStack(Material.WATER_BUCKET));
                world.playSound(loc, Sound.ITEM_BUCKET_FILL, 1, 1);
                block.setType(Material.AIR);
            }
        }, 5);
    }

    private void miscellaneousChecks(Bot bot, LivingEntity target) {
        Player botPlayer = bot.getBukkitEntity();
        World world = botPlayer.getWorld();
        String worldName = world.getName();
        Location loc = bot.getLocation();

        if (bot.isOnFire()) {
            if (bot.getBukkitEntity().getWorld().getEnvironment() != World.Environment.NETHER) {
                placeWaterDown(bot, world, loc);
            }
        }

        Material atType = loc.getBlock().getType();

        if (atType == Material.FIRE || atType == Material.SOUL_FIRE) {
            if (bot.getBukkitEntity().getWorld().getEnvironment() != World.Environment.NETHER) {
                placeWaterDown(bot, world, loc);
                world.playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1, 1);
            } else {
                bot.look(BlockFace.DOWN);
                bot.punch();
                world.playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1, 1);
                loc.getBlock().setType(Material.AIR);
            }
        }

        if (atType == Material.LAVA) {
            if (bot.getBukkitEntity().getWorld().getEnvironment() == World.Environment.NETHER) {
                bot.attemptBlockPlace(loc, Material.COBBLESTONE, false);
            } else {
                placeWaterDown(bot, world, loc);
            }
        }

        Location head = loc.clone().add(0, 1, 0);
        Material headType = head.getBlock().getType();

        if (headType == Material.LAVA) {
            if (bot.getBukkitEntity().getWorld().getEnvironment() == World.Environment.NETHER) {
                bot.attemptBlockPlace(head, Material.COBBLESTONE, false);
            } else {
                placeWaterDown(bot, world, head);
            }
        }

        if (headType == Material.FIRE || headType == Material.SOUL_FIRE) {
            if (bot.getBukkitEntity().getWorld().getEnvironment() == World.Environment.NETHER) {
                bot.look(BlockFace.DOWN);
                bot.punch();
                world.playSound(head, Sound.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1, 1);
                head.getBlock().setType(Material.AIR);
            } else {
                placeWaterDown(bot, world, head);
            }
        }

        Location under = loc.clone().add(0, -1, 0);
        Material underType = under.getBlock().getType();

        if (underType == Material.FIRE || underType == Material.SOUL_FIRE) {
            Block place = under.getBlock();
            bot.look(BlockFace.DOWN);
            bot.punch();
            world.playSound(under, Sound.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1, 1);
            place.setType(Material.AIR);
        }

        Location under2 = loc.clone().add(0, -2, 0);
        Material under2Type = under2.getBlock().getType();

        if (under2Type == Material.MAGMA_BLOCK) {
            if (LegacyMats.SPAWN.contains(under2Type)) {
                bot.attemptBlockPlace(under2, Material.COBBLESTONE, true);
            }
        }

        if (botPlayer.getLocation().getBlockY() <= target.getLocation().getBlockY() + 1) {
            if (!miningAnim.containsKey(botPlayer)) {
                Vector vel = botPlayer.getVelocity();
                double y = vel.getY();

                if (y >= -0.6) {
                    if (loc.clone().add(0, -0.6, 0).getBlock().getType() == Material.WATER
                            && !LegacyMats.NO_CRACK.contains(under2Type)
                            && botPlayer.getEyeLocation().getBlock().getType().isAir()) {

                        Block place = loc.clone().add(0, -1, 0).getBlock();
                        if (LegacyMats.WATER.contains(place.getType())) {
                            Location mlgLoc = place.getLocation();

                            bot.attemptBlockPlace(place.getLocation(), Material.COBBLESTONE, true);
                        }
                    }
                }
            }
        }

        underType = loc.clone().add(0, -0.6, 0).getBlock().getType();

        if (underType == Material.LAVA) {
            if (!boatCooldown.contains(bot)) {
                boatCooldown.add(bot);

                Location place = loc.clone().add(0, -0.1, 0);

                bot.setItem(new ItemStack(Material.OAK_BOAT));
                bot.look(BlockFace.DOWN);
                bot.punch();

                Boat boat = (Boat) world.spawnEntity(place, EntityType.BOAT);

                scheduler.runTaskLater(plugin, () -> {
                    if (!boat.isDead()) {
                        boats.remove(boat);
                        boat.remove();
                    }
                }, 20);

                scheduler.runTaskLater(plugin, () -> {
                    bot.look(BlockFace.DOWN);
                }, 1);

                boats.add(boat);

                Location targetLoc = target.getLocation();

                bot.stand();
                Vector vector = targetLoc.toVector().subtract(bot.getLocation().toVector()).normalize();
                vector.multiply(0.8);

                Vector move = bot.getVelocity().add(vector).setY(0);
                if (move.length() > 1) move = move.normalize();
                move.multiply(0.5);
                move.setY(0.42);
                bot.setVelocity(move);

                scheduler.runTaskLater(plugin, () -> {
                    boatCooldown.remove(bot);
                    if (bot.isAlive()) {
                        bot.faceLocation(target.getLocation());
                    }
                }, 5);
            }
        }
    }

    private void resetHand(Bot npc, LivingEntity target, Player playerNPC) {
        if (!noFace.contains(npc)) { // LESSLAG if there is no if statement here
            npc.faceLocation(target.getLocation());
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

    private void attack(Bot bot, LivingEntity target, Location loc) {
        if ((target instanceof Player && PlayerUtils.isInvincible(((Player) target).getGameMode())) || target.getNoDamageTicks() >= 5 || loc.distance(target.getLocation()) >= 4) return;

        bot.attack(target);
    }

    public void setTargetType(EnumTargetGoal goal) {
        this.goal = goal;
    }

    public LivingEntity locateTarget(Bot bot, Location loc) {
        LivingEntity result = null;

        switch (goal) {
            default:
                return null;

            case NEAREST_PLAYER: {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (validateCloserEntity(player, loc, result)) {
                        result = player;
                    }
                }

                break;
            }

            case NEAREST_VULNERABLE_PLAYER: {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!PlayerUtils.isInvincible(player.getGameMode()) && validateCloserEntity(player, loc, result)) {
                        result = player;
                    }
                }

                break;
            }
            
            case NEAREST_HOSTILE: {
                for (LivingEntity entity : bot.getBukkitEntity().getWorld().getLivingEntities()) {
                    if (entity instanceof Monster && validateCloserEntity(entity, loc, result)) {
                        result = entity;
                    }
                }

                break;
            }
            
            case NEAREST_MOB: {
                for (LivingEntity entity : bot.getBukkitEntity().getWorld().getLivingEntities()) {
                    if (entity instanceof Mob && validateCloserEntity(entity, loc, result)) {
                    	result = entity;
                    }
                }

                break;
            }

            case NEAREST_BOT: {
                for (Bot otherBot : manager.fetch()) {
                    if (bot != otherBot) {
                        Player player = otherBot.getBukkitEntity();

                        if (validateCloserEntity(player, loc, result)) {
                            result = player;
                        }
                    }
                }

                break;
            }

            case NEAREST_BOT_DIFFER: {
                String name = bot.getName();

                for (Bot otherBot : manager.fetch()) {
                    if (bot != otherBot) {
                        Player player = otherBot.getBukkitEntity();

                        if (!name.equals(otherBot.getName()) && validateCloserEntity(player, loc, result)) {
                            result = player;
                        }
                    }
                }

                break;
            }

            case NEAREST_BOT_DIFFER_ALPHA: {
                String name = bot.getName().replaceAll("[^A-Za-z]+", "");

                for (Bot otherBot : manager.fetch()) {
                    if (bot != otherBot) {
                        Player player = otherBot.getBukkitEntity();

                        if (!name.equals(otherBot.getName().replaceAll("[^A-Za-z]+", "")) && validateCloserEntity(player, loc, result)) {
                            result = player;
                        }
                    }
                }
            }
        }

        return result;
    }

    private boolean validateCloserEntity(LivingEntity entity, Location loc, LivingEntity result) {
        return loc.getWorld() == entity.getWorld() && !entity.isDead() && (result == null || loc.distance(entity.getLocation()) < loc.distance(result.getLocation()));
    }
}
