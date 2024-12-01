package net.nuggetmc.tplus.api.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MojangAPI {

    private static final boolean CACHE_ENABLED = false;

    private static final Map<String, String[]> CACHE = new HashMap<>();

    public static String[] getSkin(String name) {
        if (CACHE_ENABLED && CACHE.containsKey(name)) {
            return CACHE.get(name);
        }

        String[] values = pullFromAPI(name);
        CACHE.put(name, values);
        return values;
    }

    // CATCHING NULL ILLEGALSTATEEXCEPTION BAD!!!! eventually fix from the getAsJsonObject thingy
    public static String[] pullFromAPI(String name) {
        try {
            // Get UUID from Mojang API
            URL uuidURL = URI.create("https://api.mojang.com/users/profiles/minecraft/" + name).toURL();
            String uuid = JsonParser.parseString(String.valueOf(new InputStreamReader(uuidURL.openStream())))
                    .getAsJsonObject()
                    .get("id")
                    .getAsString();

            // Get skin data from session server
            URL sessionURL = URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false").toURL();
            JsonObject property = JsonParser.parseString(String.valueOf(new InputStreamReader(sessionURL.openStream())))
                    .getAsJsonObject()
                    .get("properties")
                    .getAsJsonArray()
                    .get(0)
                    .getAsJsonObject();

            return new String[] {
                    property.get("value").getAsString(),
                    property.get("signature").getAsString()
            };
        } catch (IOException | IllegalStateException e) {
            return null;
        }
    }
}
