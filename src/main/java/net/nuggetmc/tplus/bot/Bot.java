package net.nuggetmc.tplus.bot;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.nuggetmc.tplus.TerminatorPlus;
import net.nuggetmc.tplus.bot.agent.Agent;
import net.nuggetmc.tplus.bot.agent.legacyagent.ai.NeuralNetwork;
import net.nuggetmc.tplus.bot.event.BotDamageByPlayerEvent;
import net.nuggetmc.tplus.bot.event.BotFallDamageEvent;
import net.nuggetmc.tplus.bot.event.BotKilledByPlayerEvent;
import net.nuggetmc.tplus.utils.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

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

    private Bot(MinecraftServer minecraftserver, ServerLevel world, GameProfile profile) {
        super(minecraftserver, world, profile);

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

        entityData.set(new EntityDataAccessor<>(16, EntityDataSerializers.INT), 0xFF);
    }

    public static Bot createBot(Location loc, String name) {
        return createBot(loc, name, MojangAPI.getSkin(name));
    }

    public static Bot createBot(Location loc, String name, String[] skin) {
        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        ServerLevel nmsWorld = ((CraftWorld) Objects.requireNonNull(loc.getWorld())).getHandle();

        UUID uuid = BotUtils.randomSteveUUID();

        CustomGameProfile profile = new CustomGameProfile(uuid, ChatUtils.trim16(name), skin);

        Bot bot = new Bot(nmsServer, nmsWorld, profile);

        bot.connection = new ServerGamePacketListenerImpl(nmsServer, new Connection(PacketFlow.CLIENTBOUND) {

            @Override
            public void send(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> genericfuturelistener) { }

        }, bot);

        bot.teleportTo(loc.getX(), loc.getY(), loc.getZ());
        bot.setRot(loc.getYaw(), loc.getPitch());
        bot.getBukkitPlayer().setNoDamageTicks(0);
        nmsWorld.addEntity(bot, CreatureSpawnEvent.SpawnReason.COMMAND);

        bot.renderAll();

        TerminatorPlus.getInstance().getManager().add(bot);

        return bot;
    }

    private void renderAll() {
        Packet<?>[] packets = getRenderPackets();
        Bukkit.getOnlinePlayers().forEach(p -> render(((CraftPlayer) p).getHandle().connection, packets, false));
    }

    private void render(ServerPlayerConnection connection, Packet<?>[] packets, boolean login) {
        connection.send(packets[0]);
        connection.send(packets[1]);
        connection.send(packets[2]);

        if (login) {
            scheduler.runTaskLater(plugin, () -> connection.send(packets[3]), 10);
        } else {
            connection.send(packets[3]);
        }
    }

    public void render(ServerPlayerConnection connection, boolean login) {
        render(connection, getRenderPackets(), login);
    }

    private Packet<?>[] getRenderPackets() {
        return new Packet[] {
                new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, this),
                new ClientboundAddPlayerPacket(this),
                new ClientboundSetEntityDataPacket(this.getId(), this.getEntityData(), true),
                new ClientboundRotateHeadPacket(this, (byte) ((getYRot() * 256f) / 360f))
        };
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
        //if (noDamageTicks > 0) --noDamageTicks; // TODO: Reimplement
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

        oldVelocity = velocity.clone();
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }

    private void loadChunks() {
        Level world = level;

        for (int i = chunkPosition().x - 1; i <= chunkPosition().x + 1; i++) {
            for (int j = chunkPosition().z - 1; j <= chunkPosition().z + 1; j++) {
                LevelChunk chunk = world.getChunkAt(i, j);

                if (!chunk.loaded) {
                    chunk.loaded = true;
                }
            }
        }
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

        /*if (noDamageTicks == 0) { TODO: Reimplement
            if (lava) {
                damageEntity(DamageSource.LAVA, 4);
                noDamageTicks = 12;
            } else if (fireTicks > 1) {
                damageEntity(DamageSource.IN_FIRE, 1);
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
        entityData.set(new EntityDataAccessor<>(0, EntityDataSerializers.BYTE), onFire ? (byte) 1 : (byte) 0);
        sendPacket(new ClientboundSetEntityDataPacket(getId(), getEntityData(), false));
    }

    public boolean isOnFire() {
        return fireTicks != 0;
    }

    private void fallDamageCheck() { // TODO create a better bot event system in the future, also have bot.getAgent()
        if (groundTicks != 0 && noFallTicks == 0 && !(oldVelocity.getY() >= -0.8) && !BotUtils.NO_FALL.contains(getLocation().getBlock().getType())) {
            BotFallDamageEvent event = new BotFallDamageEvent(this);

            plugin.getManager().getAgent().onFallDamage(event);

            if (!event.isCancelled()) {
                damageEntity0(DamageSource.FALL, (float) Math.pow(3.6, -oldVelocity.getY()));
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
        this.blocking = true;
        this.blockUse = true;
        startUsingItem(InteractionHand.OFF_HAND);
        sendPacket(new ClientboundSetEntityDataPacket(getId(), getEntityData(), false));
    }

    private void stopBlocking(int cooldown) {
        this.blocking = false;
        stopUsingItem();
        scheduler.runTaskLater(plugin, () -> this.blockUse = false, cooldown);
        sendPacket(new ClientboundSetEntityDataPacket(getId(), getEntityData(), false));
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

        this.move(MoverType.SELF, new Vec3(velocity.getX(), y, velocity.getZ()));
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
            ((Damageable) entity).damage(damage, getBukkitPlayer());
        }
    }

    public void punch() {
        swing(InteractionHand.MAIN_HAND);
    }

    public boolean checkGround() {
        double vy = velocity.getY();

        if (vy > 0) {
            return false;
        }

        World world = getBukkitPlayer().getWorld();
        AABB box = getBoundingBox();

        double[] xVals = new double[] {
            box.minX,
            box.maxX
        };

        double[] zVals = new double[] {
            box.minZ,
            box.maxZ
        };

        for (double x : xVals) {
            for (double z : zVals) {
                Location loc = new Location(world, x, getY() - 0.01, z);
                Block block = world.getBlockAt(loc);

                if (block.getType().isSolid() && BotUtils.solidAt(loc)) {
                    return true;
                }
            }
        }

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
        sendPacket(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, this));
    }

    public void setRemoveOnDeath(boolean enabled) {
        this.removeOnDeath = enabled;
    }

    private void setDead() {
        sendPacket(new ClientboundRemoveEntitiesPacket(getId()));

        this.dead = true;
        this.inventoryMenu.removed(this);
        if (this.containerMenu != null) {
            this.containerMenu.removed(this);
        }
    }

    private void dieCheck() {
        if (removeOnDeath) {
            remove(RemovalReason.KILLED);
            scheduler.runTask(plugin, () -> plugin.getManager().remove(this)); // maybe making this later will fix the concurrentmodificationexception?
            scheduler.runTaskLater(plugin, this::setDead, 30);

            this.removeTab();
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        this.dieCheck();
    }

    @Override
    public void push(Entity entity) {
        if (!this.isPassengerOfSameVehicle(entity) && !entity.noPhysics && !this.noPhysics) {
            double d0 = entity.getX() - this.getX();
            double d1 = entity.getZ() - this.getZ();
            double d2 = Mth.absMax(d0, d1);
            if (d2 >= 0.009999999776482582D) {
                d2 = Math.sqrt(d2);
                d0 /= d2;
                d1 /= d2;
                double d3 = 1.0D / d2;
                if (d3 > 1.0D) {
                    d3 = 1.0D;
                }

                d0 *= d3;
                d1 *= d3;
                d0 *= 0.05000000074505806D;
                d1 *= 0.05000000074505806D;
                if (!this.isVehicle()) {
                    velocity.add(new Vector(-d0 * 3, 0.0D, -d1 * 3));
                }

                if (!entity.isVehicle()) {
                    entity.push(d0, 0.0D, d1);
                }
            }
        }
    }

    @Override
    public boolean damageEntity0(DamageSource damagesource, float f) {
        Entity attacker = damagesource.getEntity();

        float damage;

        boolean playerInstance = attacker instanceof net.minecraft.world.entity.player.Player;

        Player killer;

        if (playerInstance) {
            killer = (Player) ((net.minecraft.world.entity.player.Player) attacker).getBukkitEntity();

            BotDamageByPlayerEvent event = new BotDamageByPlayerEvent(this, killer, f);

            agent.onPlayerDamage(event);

            if (event.isCancelled()) {
                return false;
            }

            damage = event.getDamage();
        } else {
            killer = null;
            damage = f;
        }

        boolean damaged = super.damageEntity0(damagesource, damage);

        if (!damaged && blocking) {
            getBukkitPlayer().getWorld().playSound(getLocation(), Sound.ITEM_SHIELD_BLOCK, 1, 1);
        }

        if (damaged && attacker != null) {
            if (playerInstance && !isAlive()) {
                agent.onBotKilledByPlayer(new BotKilledByPlayerEvent(this, killer));
            }

            kb(getLocation(), attacker.getBukkitEntity().getLocation());
        }

        return damaged;
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
        return getBukkitPlayer().getLocation();
    }

    public void faceLocation(Location loc) {
        look(loc.toVector().subtract(getLocation().toVector()), false);
    }

    public void look(BlockFace face) {
        look(face.getDirection(), face == BlockFace.DOWN || face == BlockFace.UP);
    }

    private void look(Vector dir, boolean keepYaw) {
        float yaw, pitch;

        if (keepYaw) {
            yaw = this.getYHeadRot();
            pitch = MathUtils.fetchPitch(dir);
        } else {
            float[] vals = MathUtils.fetchYawPitch(dir);
            yaw = vals[0];
            pitch = vals[1];

            sendPacket(new ClientboundRotateHeadPacket(this, (byte) (yaw * 256 / 360f)));
        }
        
        setRot(yaw, pitch);
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
        setItem(item, EquipmentSlot.MAINHAND);
    }

    public void setItemOffhand(org.bukkit.inventory.ItemStack item) {
        setItem(item, EquipmentSlot.OFFHAND);
    }

    private void setItem(org.bukkit.inventory.ItemStack item, EquipmentSlot slot) {
        if (item == null) item = defaultItem;

        if (slot == EquipmentSlot.MAINHAND) {
            getBukkitPlayer().getInventory().setItemInMainHand(item);
        } else if (slot == EquipmentSlot.OFFHAND) {
            getBukkitPlayer().getInventory().setItemInOffHand(item);
        }

        sendPacket(new ClientboundSetEquipmentPacket(getId(), new ArrayList<>(Collections.singletonList(
                new Pair<>(slot, CraftItemStack.asNMSCopy(item))
        ))));
    }

    public void swim() {
        getBukkitPlayer().setSwimming(true);
        registerPose(Pose.SWIMMING);
    }

    public void sneak() {
        getBukkitPlayer().setSneaking(true);
        registerPose(Pose.CROUCHING);
    }

    public void stand() {
        Player player = (Player) getBukkitPlayer();
        player.setSneaking(false);
        player.setSwimming(false);

        registerPose(Pose.STANDING);
    }

    private void registerPose(Pose pose) {
        entityData.set(EntityDataSerializers.POSE.createAccessor(6), pose);
        sendPacket(new ClientboundSetEntityDataPacket(getId(), getEntityData(), false));
    }

    @Override
    public void doTick() {
        if (this.hurtTime > 0) {
            this.hurtTime -= 1;
        }

        baseTick();
        tickEffects();

        this.lerpSteps = (int) this.zza;
        this.animStep = this.run;
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    public Player getBukkitPlayer() {
        return getBukkitEntity();
    }

}
