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

    public static Vector circleOffset(double r) {
        double rad = 2 * Math.random() * Math.PI;

        double x = r * Math.random() * Math.cos(rad);
        double z = r * Math.random() * Math.sin(rad);

        return new Vector(x, 0, z);
    }

    public static boolean isFinite(Vector vector) {
        try {
            vector.checkFinite();
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static void clean(Vector vector) {
        if (!NumberConversions.isFinite(vector.getX())) vector.setX(0);
        if (!NumberConversions.isFinite(vector.getY())) vector.setY(0);
        if (!NumberConversions.isFinite(vector.getZ())) vector.setZ(0);
    }
}
