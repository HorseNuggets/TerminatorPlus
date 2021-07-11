package net.nuggetmc.ai.bot;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.v1_16_R3.*;
import net.nuggetmc.ai.PlayerAI;
import net.nuggetmc.ai.utils.MathUtils;
import net.nuggetmc.ai.utils.MojangAPI;
import net.nuggetmc.ai.utils.SteveUUID;
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
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Bot extends EntityPlayer {

    public Vector velocity;

    private byte aliveTicks;
    private byte kbTicks;
    private byte jumpTicks;
    private byte groundTicks;

    private final Vector offset;

    public Bot(MinecraftServer minecraftServer, WorldServer worldServer, GameProfile profile, PlayerInteractManager manager) {
        super(minecraftServer, worldServer, profile, manager);

        this.velocity = new Vector(0, 0, 0);
        this.offset = MathUtils.circleOffset(3);

        datawatcher.set(new DataWatcherObject<>(16, DataWatcherRegistry.a), (byte) 0xFF);
    }

    public static Bot createBot(Location loc, String name, String[] skin) {
        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer nmsWorld = ((CraftWorld) Objects.requireNonNull(loc.getWorld())).getHandle();

        UUID uuid = SteveUUID.generate();

        CustomGameProfile profile = new CustomGameProfile(uuid, name, skin);
        PlayerInteractManager interactManager = new PlayerInteractManager(nmsWorld);

        Bot bot = new Bot(nmsServer, nmsWorld, profile, interactManager);

        bot.playerConnection = new PlayerConnection(nmsServer, new NetworkManager(EnumProtocolDirection.CLIENTBOUND), bot);
        bot.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        bot.getBukkitEntity().setNoDamageTicks(0);
        nmsWorld.addEntity(bot);

        bot.renderAll();

        PlayerAI.getInstance().getManager().add(bot);

        return bot;
    }

    public Vector prevVel = new Vector(0, 0, 0);
    public int velCount;

    public static Bot createBot(Location loc, String name, String skinName) {
        return createBot(loc, name, MojangAPI.getSkin(skinName));
    }

    private void renderAll() {
        Packet<?>[] packets = getRenderPackets();
        Bukkit.getOnlinePlayers().forEach(p -> render(((CraftPlayer) p).getHandle().playerConnection, packets, false));
    }

    public void render(PlayerConnection connection, Packet<?>[] packets, boolean login) {
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

    public void addVelocity(Vector vector) {
        try {
            velocity.checkFinite();
        } catch (IllegalArgumentException e) {
            velocity = vector;
            return;
        }

        this.velocity.add(vector);
    }

    public boolean tickDelay(int i) {
        return aliveTicks % i == 0;
    }

    @Override
    public void tick() {
        super.tick();

        aliveTicks++;

        if (noDamageTicks > 0) --noDamageTicks;
        if (kbTicks > 0) --kbTicks;
        if (jumpTicks > 0) --jumpTicks;

        if (checkGround()) {
            if (groundTicks < 5) groundTicks++;
        } else {
            groundTicks = 0;
        }

        Player player = getBukkitEntity();
        if (player.isDead()) return;

        updateLocation();

        double health = player.getHealth();
        double maxHealth = player.getHealthScale();
        double regenAmount = 0.05;
        double amount;

        if (health < maxHealth - regenAmount) {
            amount = health + regenAmount;
        } else {
            amount = maxHealth;
        }

        player.setHealth(amount);
    }

    private void updateLocation() {
        // Eventually there will be a whole algorithm here to slow a player down to a certain velocity depending on the liquid a player is in

        double y;

        if (groundTicks != 0) {
            velocity.setY(0);
            addFriction();
            y = 0;
        } else {
            y = velocity.getY();
        }

        velocity.setY(Math.max(velocity.getY() - 0.1, -3.5));

        this.move(EnumMoveType.SELF, new Vec3D(velocity.getX(), y, velocity.getZ()));
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
        attack(((CraftEntity) entity).getHandle());
    }

    public void punch() {
        swingHand(EnumHand.MAIN_HAND);
    }

    public boolean checkGround() {
        double k = 0.01;
        double vy = velocity.getY();

        if (vy > 0) {
            return false;
        }

        World world = getBukkitEntity().getWorld();
        AxisAlignedBB box = getBoundingBox();

        double[] xVals = new double[] {
            box.minX + k,
            box.maxX - k
        };

        double[] zVals = new double[] {
            box.minZ + k,
            box.maxZ - k
        };

        for (double x : xVals) {
            for (double z : zVals) {
                if (world.getBlockAt(new Location(world, x, locY() - 0.01, z)).getType().isSolid()) {
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

    public void addFriction() {
        double frictionMin = 0.01;

        double x = velocity.getX();
        double z = velocity.getZ();

        velocity.setX(Math.abs(x) < frictionMin ? 0 : x * 0.5);
        velocity.setZ(Math.abs(z) < frictionMin ? 0 : z * 0.5);
    }

    public void despawn() {
        getBukkitEntity().remove();
    }

    public void remove() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
            connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, this));
        }

        getBukkitEntity().remove();
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
        }

        setYawPitch(yaw, pitch);

        PacketPlayOutEntityHeadRotation packet = new PacketPlayOutEntityHeadRotation(getBukkitEntity().getHandle(), (byte) (yaw * 256 / 360f));
        Bukkit.getOnlinePlayers().forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet));
    }

    public void attemptBlockPlace(Location loc, Material type) {
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

        CraftPlayer player = getBukkitEntity();
        player.getInventory().setItemInMainHand(item);

        List<Pair<EnumItemSlot, ItemStack>> equipment = new ArrayList<>();
        equipment.add(new Pair<>(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(item)));

        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(player.getEntityId(), equipment);
        Bukkit.getOnlinePlayers().forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet));
    }

    public void swim() {
        registerPose(EntityPose.SWIMMING);
    }

    public void sneak() {
        registerPose(EntityPose.CROUCHING);
    }

    public void stand() {
        registerPose(EntityPose.STANDING);
    }

    private void registerPose(EntityPose pose) {
        datawatcher.set(DataWatcherRegistry.s.a(6), pose);
        PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(getId(), datawatcher, false);
        Bukkit.getOnlinePlayers().forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet));
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
