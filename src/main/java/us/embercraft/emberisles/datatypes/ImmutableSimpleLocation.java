package us.embercraft.emberisles.datatypes;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import org.bukkit.Location;
import org.bukkit.World;

@Immutable
@ThreadSafe
public class ImmutableSimpleLocation implements Serializable {
    public ImmutableSimpleLocation(final double x, final double y, final double z, final float yaw, final float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public ImmutableSimpleLocation(final double x, final double y, final double z) {
        this(x, y, z, 0, 0);
    }

    public ImmutableSimpleLocation(final Location loc) {
        this(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public int getBlockX() {
        return (int) x;
    }

    public int getBlockY() {
        return (int) y;
    }

    public int getBlockZ() {
        return (int) z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public Location toLocation(World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ImmutableSimpleLocation))
            return false;
        final ImmutableSimpleLocation other = (ImmutableSimpleLocation) o;
        if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x) ||
                Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y) ||
                Double.doubleToLongBits(this.z) != Double.doubleToLongBits(other.z) ||
                Float.floatToIntBits(this.pitch) != Float.floatToIntBits(other.pitch) ||
                Float.floatToIntBits(this.yaw) != Float.floatToIntBits(other.yaw)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = 19 * 3 + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
            hashCode = 19 * hashCode + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
            hashCode = 19 * hashCode + (int) (Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
            hashCode = 19 * hashCode + Float.floatToIntBits(this.pitch);
            hashCode = 19 * hashCode + Float.floatToIntBits(this.yaw);
        }
        return hashCode;
    }

    @Override
    public String toString() {
        return String.format("[x: %.2f, y: %.2f, z: %.2f, yaw: %.2f, pitch: %.2f]", x, y, z, yaw, pitch);
    }

    transient private int hashCode = 0;

    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    private static final long serialVersionUID = 1L;
}
