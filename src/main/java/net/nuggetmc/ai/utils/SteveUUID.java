package net.nuggetmc.ai.utils;

import java.util.UUID;

public class SteveUUID {

    public static UUID generate() {
        UUID uuid = UUID.randomUUID();

        if (uuid.hashCode() % 2 == 0) {
            return uuid;
        }

        return generate();
    }
}
