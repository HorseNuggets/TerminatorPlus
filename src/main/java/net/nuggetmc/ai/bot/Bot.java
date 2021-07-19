package net.nuggetmc.ai.bot;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.v1_16_R3.*;
import net.nuggetmc.ai.PlayerAI;
import net.nuggetmc.ai.bot.event.BotFallDamageEvent;
import net.nuggetmc.ai.utils.BotUtils;
import net.nuggetmc.ai.utils.MathUtils;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

public class Bot extends EntityPlayer {

    public Vector velocity;
    private Vector oldVelocity;

    private boolean removeOnDeath;

    private byte aliveTicks;
    private byte fireTicks;
    private byte groundTicks;
    private byte jumpTicks;
    private byte kbTicks;
    private byte noFallTicks;

    private final Vector offset;

    private Bot(MinecraftServer minecraftServer, WorldServer worldServer, GameProfile profile, PlayerInteractManager manager) {
        super(minecraftServer, worldServer, profile, manager);

        this.velocity = new Vector(0, 0, 0);
        this.oldVelocity = velocity.clone();
        this.noFallTicks = 60;
        this.fireTicks = 0;
        this.offset = MathUtils.circleOffset(3);

        datawatcher.set(new DataWatcherObject<>(16, DataWatcherRegistry.a), (byte) 0xFF);
    }

    public static Bot createBot(Location loc, String name, String[] skin, boolean removeOnDeath) {
        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer nmsWorld = ((CraftWorld) Objects.requireNonNull(loc.getWorld())).getHandle();

        UUID uuid = BotUtils.randomSteveUUID();

        CustomGameProfile profile = new CustomGameProfile(uuid, name, skin);
        PlayerInteractManager interactManager = new PlayerInteractManager(nmsWorld);

        Bot bot = new Bot(nmsServer, nmsWorld, profile, interactManager);

        bot.playerConnection = new PlayerConnection(nmsServer, new NetworkManager(EnumProtocolDirection.CLIENTBOUND), bot);
        bot.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        bot.getBukkitEntity().setNoDamageTicks(0);
        bot.removeOnDeath = removeOnDeath;
        nmsWorld.addEntity(bot);

        bot.renderAll();

        PlayerAI.getInstance().getManager().add(bot);

        return bot;
    }

    private void renderAll() {
        Packet<?>[] packets = getRenderPackets();
        Bukkit.getOnlinePlayers().forEach(p -> render(((CraftPlayer) p).getHandle().playerConnection, packets, false));
    }

    private void render(PlayerConnection connection, Packet<?>[] packets, boolean login) {
        connection.sendPacket(packets[0]);
        connection.sendPacket(packets[1]);
        connection.sendPacket(packets[2]);

        if (login) {
            Bukkit.getScheduler().runTaskLater(PlayerAI.getInstance(), () -> connection.sendPacket(packets[3]), 10);
        } else {
            connection.sendPacket(packets[3]);
        }
    }

    public void render(PlayerConnection connection, boolean login) {
        render(connection, getRenderPackets(), login);
    }

    private Packet<?>[] getRenderPackets() {
        return new Packet[] {
            new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, this),
            new PacketPlayOutNamedEntitySpawn(this),
            new PacketPlayOutEntityMetadata(this.getId(), this.getDataWatcher(), true),
            new PacketPlayOutEntityHeadRotation(this, (byte) ((this.yaw * 256f) / 360f))
        };
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

    public boolean tickDelay(int i) {
        return aliveTicks % i == 0;
    }

    private void sendPacket(Packet<?> packet) {
        Bukkit.getOnlinePlayers().forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet));
    }

    @Override
    public void tick() {
        super.tick();

        if (!isAlive()) return;

        aliveTicks++;

        if (fireTicks > 0) --fireTicks;
        if (noDamageTicks > 0) --noDamageTicks;
        if (jumpTicks > 0) --jumpTicks;
        if (kbTicks > 0) --kbTicks;
        if (noFallTicks > 0) --noFallTicks;

        if (checkGround()) {
            if (groundTicks < 5) groundTicks++;
        } else {
            groundTicks = 0;
        }

        updateLocation();

        float health = getHealth();
        float maxHealth = getMaxHealth();
        float regenAmount = 0.05f;
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

        if (noDamageTicks == 0) {
            if (lava) {
                damageEntity(DamageSource.LAVA, 4);
                noDamageTicks = 12;
            } else if (fireTicks > 1) {
                damageEntity(DamageSource.FIRE, 1);
                noDamageTicks = 20;
            }
        }

        if (fireTicks == 1) {
            setOnFirePackets(false);
        }
    }

    public void ignite() {
        if (fireTicks <= 1) setOnFirePackets(true);
        fireTicks = 100;
    }

    public void setOnFirePackets(boolean onFire) {
        datawatcher.set(new DataWatcherObject<>(0, DataWatcherRegistry.a), onFire ? (byte) 1 : (byte) 0);
        sendPacket(new PacketPlayOutEntityMetadata(getId(), datawatcher, false));
    }

    public boolean isOnFire() {
        return fireTicks != 0;
    }

    private void fallDamageCheck() { // TODO create a better bot event system in the future, also have bot.getAgent()
        if (groundTicks != 0 && noFallTicks == 0 && !(oldVelocity.getY() >= -0.8) && !BotUtils.NO_FALL.contains(getLocation().getBlock().getType())) {
            BotFallDamageEvent event = new BotFallDamageEvent(this);

            PlayerAI.getInstance().getManager().getAgent().onFallDamage(event);

            if (!event.isCancelled()) {
                damageEntity(DamageSource.FALL, (float) Math.pow(3.6, -oldVelocity.getY()));
            }
        }
    }

    public boolean isFalling() {
        return velocity.getY() < -0.8;
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

        this.move(EnumMoveType.SELF, new Vec3D(velocity.getX(), y, velocity.getZ()));
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

    public void attack(org.bukkit.entity.Entity entity) {
        faceLocation(entity.getLocation());
        punch();

        if (entity instanceof Damageable) {
            ((Damageable) entity).damage(2, getBukkitEntity()); // fist damage is 0.25
        }
    }

    public void punch() {
        swingHand(EnumHand.MAIN_HAND);
    }

    public boolean checkGround() {
        double vy = velocity.getY();

        if (vy > 0) {
            return false;
        }

        World world = getBukkitEntity().getWorld();
        AxisAlignedBB box = getBoundingBox();

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
                Location loc = new Location(world, x, locY() - 0.01, z);
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

    public void despawn() {
        getBukkitEntity().remove();
    }

    public void removeVisually() {
        this.removeTab();
        this.setDead();
    }

    private void removeTab() {
        sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, this));
    }

    private void setDead() {
        this.dead = true;
        this.defaultContainer.b(this);
        if (this.activeContainer != null) {
            this.activeContainer.b(this);
        }
    }

    private void dieCheck() {
        if (removeOnDeath) {
            PlayerAI plugin = PlayerAI.getInstance();
            plugin.getManager().remove(this);
            this.removeTab();
            Bukkit.getScheduler().runTaskLater(plugin, this::setDead, 30);
        }
    }

    @Override
    public void die() {
        super.die();
        this.dieCheck();
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        this.dieCheck();
    }

    @Override
    public void collide(Entity entity) {
        if (!this.isSameVehicle(entity) && !entity.noclip && !this.noclip) {
            double d0 = entity.locX() - this.locX();
            double d1 = entity.locZ() - this.locZ();
            double d2 = MathHelper.a(d0, d1);
            if (d2 >= 0.009999999776482582D) {
                d2 = MathHelper.sqrt(d2);
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
                d0 *= 1.0F - this.I;
                d1 *= 1.0F - this.I;

                if (!this.isVehicle()) {
                    velocity.add(new Vector(-d0 * 3, 0.0D, -d1 * 3));
                }

                if (!entity.isVehicle()) {
                    entity.i(d0, 0.0D, d1);
                }
            }
        }
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        boolean damaged = super.damageEntity(damagesource, f);

        net.minecraft.server.v1_16_R3.Entity attacker = damagesource.getEntity();

        if (damaged && kbTicks == 0 && attacker != null) {
            Player player = getBukkitEntity();
            CraftEntity entity = attacker.getBukkitEntity();
            Location loc1 = player.getLocation();
            Location loc2 = entity.getLocation();

            kb(loc1, loc2);
        }

        return damaged;
    }

    private void kb(Location loc1, Location loc2) {
        double kbUp = 0.3;

        Vector vel = loc1.toVector().subtract(loc2.toVector()).setY(0).normalize().multiply(0.3);

        if (isOnGround()) vel.multiply(0.8).setY(0.4);
        else if (vel.getY() > kbUp) vel.setY(kbUp);

        velocity = vel;
        kbTicks = 10;
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
        float yaw, pitch;

        if (keepYaw) {
            yaw = this.yaw;
            pitch = MathUtils.fetchPitch(dir);
        } else {
            float[] vals = MathUtils.fetchYawPitch(dir);
            yaw = vals[0];
            pitch = vals[1];

            sendPacket(new PacketPlayOutEntityHeadRotation(getBukkitEntity().getHandle(), (byte) (yaw * 256 / 360f)));
        }

        setYawPitch(yaw, pitch);
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
        if (item == null) item = new org.bukkit.inventory.ItemStack(Material.AIR);

        getBukkitEntity().getInventory().setItemInMainHand(item);

        sendPacket(new PacketPlayOutEntityEquipment(getId(), new ArrayList<>(Collections.singletonList(
            new Pair<>(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(item))
        ))));
    }

    public void swim() {
        getBukkitEntity().setSwimming(true);
        registerPose(EntityPose.SWIMMING);
    }

    public void sneak() {
        getBukkitEntity().setSneaking(true);
        registerPose(EntityPose.CROUCHING);
    }

    public void stand() {
        Player player = getBukkitEntity();
        player.setSneaking(false);
        player.setSwimming(false);

        registerPose(EntityPose.STANDING);
    }

    private void registerPose(EntityPose pose) {
        datawatcher.set(DataWatcherRegistry.s.a(6), pose);
        sendPacket(new PacketPlayOutEntityMetadata(getId(), datawatcher, false));
    }

    @Override
    public void playerTick() {
        if (this.hurtTicks > 0) {
            this.hurtTicks -= 1;
        }

        entityBaseTick();
        tickPotionEffects();

        this.aU = (int) this.aT;
        this.aL = this.aK;
        this.lastYaw = this.yaw;
        this.lastPitch = this.pitch;
    }
}
