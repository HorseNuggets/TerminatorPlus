package net.nuggetmc.tplus.nms;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.net.SocketAddress;

@SuppressWarnings("JavaReflectionMemberAccess")
public class MockConnection extends Connection {
    private static final Field PACKET_LISTENER_FIELD;
    private static final Field DISCONNECT_LISTENER_FIELD;

    static {
        try {
            // https://mappings.cephx.dev/1.20.4/net/minecraft/network/Connection.html packetListener & disconnectListener
            PACKET_LISTENER_FIELD = Connection.class.getDeclaredField("q");
            DISCONNECT_LISTENER_FIELD = Connection.class.getDeclaredField("p");

            PACKET_LISTENER_FIELD.setAccessible(true);
            DISCONNECT_LISTENER_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public MockConnection() {
        super(PacketFlow.SERVERBOUND);
        this.channel = new MockChannel(null);
        this.address = new SocketAddress() {
        };
    }

    @Override
    public void flushChannel() {
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void send(@NotNull Packet<?> packet) {
    }

    @Override
    public void send(@NotNull Packet<?> packet, PacketSendListener sendListener) {
    }

    @Override
    public void send(@NotNull Packet<?> packet, PacketSendListener sendListener, boolean flag) {
    }

    @Override
    public void setListenerForServerboundHandshake(@NotNull PacketListener packetListener) {
        try {
            PACKET_LISTENER_FIELD.set(this, packetListener);
            DISCONNECT_LISTENER_FIELD.set(this, null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
