package net.nuggetmc.ai.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class MojangAPI {

    // Eventually create some sort of cache that stores the skin data so it doesn't have to keep pulling from the API
    // CATCHING NULLPOINTEREXCEPTION BAD!!!! eventually fix from the getAsJsonObject thingy
    public static String[] getSkin(String name) {
        try {
            String uuid = new JsonParser().parse(new InputStreamReader(new URL("https://api.mojang.com/users/profiles/minecraft/" + name)
                    .openStream())).getAsJsonObject().get("id").getAsString();
            JsonObject property = new JsonParser()
                    .parse(new InputStreamReader(new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false")
                            .openStream())).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
            return new String[] {property.get("value").getAsString(), property.get("signature").getAsString()};
        } catch (IOException | NullPointerException e) {
            return null;
        }
    }
}
