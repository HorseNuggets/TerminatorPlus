package net.nuggetmc.ai.utils;

import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.Set;

public class MathUtils {

    public static final Random RANDOM = new Random();
    public static final DecimalFormat FORMATTER_1 = new DecimalFormat("0.#");
    public static final DecimalFormat FORMATTER_2 = new DecimalFormat("0.##");

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

    public static float fetchPitch(Vector dir) {
        double x = dir.getX();
        double z = dir.getZ();

        float result;

        if (x == 0.0D && z == 0.0D) {
            result = (float) (dir.getY() > 0.0D ? -90 : 90);
        }

        else {
            double x2 = NumberConversions.square(x);
            double z2 = NumberConversions.square(z);
            double xz = Math.sqrt(x2 + z2);
            result = (float) Math.toDegrees(Math.atan(-dir.getY() / xz));
        }

        return result;
    }

    public static Vector circleOffset(double r) {
        double rad = 2 * Math.random() * Math.PI;

        double x = r * Math.random() * Math.cos(rad);
        double z = r * Math.random() * Math.sin(rad);

        return new Vector(x, 0, z);
    }

    public static boolean isNotFinite(Vector vector) {
        return !NumberConversions.isFinite(vector.getX()) || !NumberConversions.isFinite(vector.getY()) || !NumberConversions.isFinite(vector.getZ());
    }

    public static void clean(Vector vector) {
        if (!NumberConversions.isFinite(vector.getX())) vector.setX(0);
        if (!NumberConversions.isFinite(vector.getY())) vector.setY(0);
        if (!NumberConversions.isFinite(vector.getZ())) vector.setZ(0);
    }

    public static <E> E getRandomSetElement(Set<E> set) {
        return set.isEmpty() ? null : set.stream().skip(RANDOM.nextInt(set.size())).findFirst().orElse(null);
    }

    public static double square(double n) {
        return n * n;
    }

    public static String round1Dec(double n) {
        return FORMATTER_1.format(n);
    }

    public static String round2Dec(double n) {
        return FORMATTER_2.format(n);
    }
}
