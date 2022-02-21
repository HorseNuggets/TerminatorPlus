package net.nuggetmc.tplus.bot;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.inventory.Slot;
import net.nuggetmc.tplus.TerminatorPlus;
import net.nuggetmc.tplus.bot.agent.Agent;
import net.nuggetmc.tplus.bot.agent.legacyagent.ai.NeuralNetwork;
import net.nuggetmc.tplus.bot.event.BotFallDamageEvent;
import net.nuggetmc.tplus.utils.BotUtils;
import net.nuggetmc.tplus.utils.ItemUtils;
import net.nuggetmc.tplus.utils.MathUtils;
import net.nuggetmc.tplus.utils.MojangAPI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

public class Bot extends ServerPlayer {

    private final TerminatorPlus plugin;
    private final BukkitScheduler scheduler;
    private final Agent agent;

    private NeuralNetwork network;

    public NeuralNetwork getNeuralNetwork() {
        return network;
    }

    public void setNeuralNetwork(NeuralNetwork network) {
        this.network = network;
    }

    public boolean hasNeuralNetwork() {
        return network != null;
    }

    public ItemStack defaultItem;

    private boolean shield;
    private boolean blocking;
    private boolean blockUse;

    private Vector velocity;
    private Vector oldVelocity;

    private boolean removeOnDeath;

    private int aliveTicks;
    private int kills;

    private byte fireTicks;
    private byte groundTicks;
    private byte jumpTicks;
    private byte noFallTicks;

    private final Vector offset;

    private Bot(MinecraftServer minecraftServer, ServerLevel level, GameProfile profile) {
        super(minecraftServer, level, profile);

        this.plugin = TerminatorPlus.getInstance();
        this.scheduler = Bukkit.getScheduler();
        this.agent = plugin.getManager().getAgent();
        this.defaultItem = new ItemStack(Material.AIR);
        this.velocity = new Vector(0, 0, 0);
        this.oldVelocity = velocity.clone();
        this.noFallTicks = 60;
        this.fireTicks = 0;
        this.removeOnDeath = true;
        this.offset = MathUtils.circleOffset(3);
    }

    public static Bot createBot(Location loc, String name) {
        return createBot(loc, name, MojangAPI.getSkin(name));
    }

    public static Bot createBot(Location loc, String name, String[] skin) {
        return null;
    }

    private void renderAll() {

    }

    public void render(ServerGamePacketListenerImpl connection) {
    }

    public void setDefaultItem(ItemStack item) {
        this.defaultItem = item;
    }

    public Vector getOffset() {
        return offset;
    }

    public Vector getVelocity() {
        return velocity.clone();
    }

    public void setVelocity(Vector vector) {
        this.velocity = vector;
    }

    public void addVelocity(Vector vector) { // This can cause lag? (maybe i fixed it with the new static method)
        if (MathUtils.isNotFinite(vector)) {
            velocity = vector;
            return;
        }

        velocity.add(vector);
    }

    public int getAliveTicks() {
        return aliveTicks;
    }

    public boolean tickDelay(int i) {
        return aliveTicks % i == 0;
    }

    private void sendPacket(Packet<?> packet) {
        Bukkit.getOnlinePlayers().forEach(p -> ((CraftPlayer) p).getHandle().connection.send(packet));
    }

    @Override
    public void tick() {
        loadChunks();

        super.tick();

        if (!isAlive()) return;

        aliveTicks++;

        if (fireTicks > 0) --fireTicks;
        // if (noDamageTicks > 0) --noDamageTicks;
        if (jumpTicks > 0) --jumpTicks;
        if (noFallTicks > 0) --noFallTicks;

        if (checkGround()) {
            if (groundTicks < 5) groundTicks++;
        } else {
            groundTicks = 0;
        }

        updateLocation();

        float health = getHealth();
        float maxHealth = getMaxHealth();
        float regenAmount = 0.025f;
        float amount;

        if (health < maxHealth - regenAmount) {
            amount = health + regenAmount;
        } else {
            amount = maxHealth;
        }

        setHealth(amount);

        fireDamageCheck();
        fallDamageCheck();
        
        /*if(locY() < -64) {
            an();
        }*/

        oldVelocity = velocity.clone();
    }

    private void loadChunks() {
    }

    private void fireDamageCheck() {
        if (!isAlive()) {
            return; // maybe also have packet reset thing
        }

        Material type = getLocation().getBlock().getType();

        if (type == Material.WATER) {
            setOnFirePackets(false); // maybe also play extinguish noise?
            fireTicks = 0;
            return;
        }

        boolean lava = type == org.bukkit.Material.LAVA;

        if (lava || type == org.bukkit.Material.FIRE || type == Material.SOUL_FIRE) {
            ignite();
        }

        /*if (noDamageTicks == 0) {
            if (lava) {
                damageEntity(DamageSource.LAVA, 4);
                noDamageTicks = 20;//this used to be 12 ticks but that would cause the bot to take damage too quickly
            } else if (fireTicks > 1) {
                damageEntity(DamageSource.FIRE, 1);
                noDamageTicks = 20;
            }
        }*/

        if (fireTicks == 1) {
            setOnFirePackets(false);
        }
    }

    public void ignite() {
        if (fireTicks <= 1) setOnFirePackets(true);
        fireTicks = 100;
    }

    public void setOnFirePackets(boolean onFire) {
    }

    public boolean isOnFire() {
        return fireTicks != 0;
    }

    private void fallDamageCheck() { // TODO create a better bot event system in the future, also have bot.getAgent()
        if (groundTicks != 0 && noFallTicks == 0 && !(oldVelocity.getY() >= -0.8) && !BotUtils.NO_FALL.contains(getLocation().getBlock().getType())) {
            BotFallDamageEvent event = new BotFallDamageEvent(this);

            plugin.getManager().getAgent().onFallDamage(event);

            if (!event.isCancelled()) {
                // damageEntity(DamageSource.FALL, (float) Math.pow(3.6, -oldVelocity.getY()));
            }
        }
    }

    public boolean isFalling() {
        return velocity.getY() < -0.8;
    }

    public void block(int blockLength, int cooldown) {
        if (!shield || blockUse) return;
        startBlocking();
        scheduler.runTaskLater(plugin, () -> stopBlocking(cooldown), blockLength);
    }

    private void startBlocking() {
    }

    private void stopBlocking(int cooldown) {
    }

    public boolean isBlocking() {
        return blocking;
    }

    public void setShield(boolean enabled) {
        this.shield = enabled;

        setItemOffhand(new org.bukkit.inventory.ItemStack(enabled ? Material.SHIELD : Material.AIR));
    }

    private void updateLocation() {
        double y;

        MathUtils.clean(velocity); // TODO lag????

        if (isInWater()) {
            y = Math.min(velocity.getY() + 0.1, 0.1);
            addFriction(0.8);
            velocity.setY(y);
        }

        else {
            if (groundTicks != 0) {
                velocity.setY(0);
                addFriction(0.5);
                y = 0;
            } else {
                y = velocity.getY();
                velocity.setY(Math.max(y - 0.1, -3.5));
            }
        }

        // this.move(EnumMoveType.SELF, new Vec3D(velocity.getX(), y, velocity.getZ()));
    }

    @Override
    public boolean isInWater() {
        Location loc = getLocation();

        for (int i = 0; i <= 2; i++) {
            Material type = loc.getBlock().getType();

            if (type == Material.WATER || type == Material.LAVA) {
                return true;
            }

            loc.add(0, 0.9, 0);
        }

        return false;
    }

    public void jump(Vector vel) {
        if (jumpTicks == 0 && groundTicks > 1) {
            jumpTicks = 4;
            velocity = vel;
        }
    }

    public void jump() {
        jump(new Vector(0, 0.5, 0));
    }

    public void walk(Vector vel) {
        double max = 0.4;

        Vector sum = velocity.clone().add(vel);
        if (sum.length() > max) sum.normalize().multiply(max);

        velocity = sum;
    }

    public void attack(org.bukkit.entity.Entity entity) {
        faceLocation(entity.getLocation());
        punch();

        double damage = ItemUtils.getLegacyAttackDamage(defaultItem);

        if (entity instanceof Damageable) {
            ((Damageable) entity).damage(damage, getBukkitEntity());
        }
    }

    public void punch() {
    }

    public boolean checkGround() {
        return false;
    }

    @Override
    public boolean isOnGround() {
        return groundTicks != 0;
    }

    public void addFriction(double factor) {
        double frictionMin = 0.01;

        double x = velocity.getX();
        double z = velocity.getZ();

        velocity.setX(Math.abs(x) < frictionMin ? 0 : x * factor);
        velocity.setZ(Math.abs(z) < frictionMin ? 0 : z * factor);
    }

    public void removeVisually() {
        this.removeTab();
        this.setDead();
    }

    private void removeTab() {
    }

    public void setRemoveOnDeath(boolean enabled) {
        this.removeOnDeath = enabled;
    }

    private void setDead() {
    }

    private void dieCheck() {
        if (removeOnDeath) {

            // I replaced HashSet with ConcurrentHashMap.newKeySet which creates a "ConcurrentHashSet"
            // this should fix the concurrentmodificationexception mentioned above, I used the ConcurrentHashMap.newKeySet to make a "ConcurrentHashSet"
            plugin.getManager().remove(this);

            scheduler.runTaskLater(plugin, this::setDead, 30);

            this.removeTab();
        }
    }

    public void die() {
        this.dieCheck();
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        this.dieCheck();
    }

    private void kb(Location loc1, Location loc2) {
        Vector vel = loc1.toVector().subtract(loc2.toVector()).setY(0).normalize().multiply(0.3);

        if (isOnGround()) vel.multiply(0.8).setY(0.4);

        velocity = vel;
    }

    public int getKills() {
        return kills;
    }

    public void incrementKills() {
        kills++;
    }

    public Location getLocation() {
        return getBukkitEntity().getLocation();
    }

    public void faceLocation(Location loc) {
        look(loc.toVector().subtract(getLocation().toVector()), false);
    }

    public void look(BlockFace face) {
        look(face.getDirection(), face == BlockFace.DOWN || face == BlockFace.UP);
    }

    private void look(Vector dir, boolean keepYaw) {
    }

    public void attemptBlockPlace(Location loc, Material type, boolean down) {
        if (down) {
            look(BlockFace.DOWN);
        } else {
            faceLocation(loc);
        }

        setItem(new org.bukkit.inventory.ItemStack(Material.COBBLESTONE));
        punch();

        Block block = loc.getBlock();
        World world = loc.getWorld();

        if (!block.getType().isSolid()) {
            block.setType(type);
            if (world != null) world.playSound(loc, Sound.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1, 1);
        }
    }

    public void setItem(org.bukkit.inventory.ItemStack item) {
    }

    public void setItemOffhand(org.bukkit.inventory.ItemStack item) {
    }

    public void setItem(org.bukkit.inventory.ItemStack item, Slot slot) {
    }

    public void swim() {
    }

    public void sneak() {
    }

    public void stand() {
        Player player = getBukkitEntity();
        player.setSneaking(false);
        player.setSwimming(false);
    }

    private void registerPose(Pose pose) {
    }

    public void playerTick() {
    }
}
