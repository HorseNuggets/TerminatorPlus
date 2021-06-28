package net.nuggetmc.ai.utils;

import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

public class MathUtils {

    public static float[] fetchYawPitch(Vector dir) {
        double x = dir.getX();
        double z = dir.getZ();

        float[] out = new float[2];

        if (x == 0.0D && z == 0.0D) {
            out[1] = (float) (dir.getY() > 0.0D ? -90 : 90);
        }

        else {
            double theta = Math.atan2(-x, z);
            out[0] = (float) Math.toDegrees((theta + 6.283185307179586D) % 6.283185307179586D);

            double x2 = NumberConversions.square(x);
            double z2 = NumberConversions.square(z);
            double xz = Math.sqrt(x2 + z2);
            out[1] = (float) Math.toDegrees(Math.atan(-dir.getY() / xz));
        }

        return out;
    }
}
