package net.nuggetmc.tplus.bot;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.nuggetmc.tplus.utils.MojangAPI;

import java.util.UUID;

public class CustomGameProfile extends GameProfile {

    public CustomGameProfile(UUID uuid, String name, String[] skin) {
        super(uuid, name);

        setSkin(skin);
    }

    public CustomGameProfile(UUID uuid, String name, String skinName) {
        super(uuid, name);

        setSkin(skinName);
    }

    public void setSkin(String skinName) {
        setSkin(MojangAPI.getSkin(skinName));
    }

    public void setSkin(String[] skin) {
        if (skin != null) {
            getProperties().put("textures", new Property("textures", skin[0], skin[1]));
        }
    }
}
