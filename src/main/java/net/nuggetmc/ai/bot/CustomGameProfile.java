package net.nuggetmc.ai.bot;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.nuggetmc.ai.utils.MojangAPI;

import java.util.UUID;

public class CustomGameProfile extends GameProfile {

    public CustomGameProfile(UUID uuid, String name, String skin) {
        super(uuid, name);

        setSkin(skin);
    }

    public void setSkin(String skin) {
        String[] vals = MojangAPI.getSkin(skin);

        if (vals != null) {
            getProperties().put("textures", new Property("textures", vals[0], vals[1]));
        }
    }
}
