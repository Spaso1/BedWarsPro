package org.ast.bedwarspro.been;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.Serializable;

public class SerializableLocation implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String worldName;
    private final double x, y, z;
    private final float yaw, pitch;

    public SerializableLocation(Location location) {
        this.worldName = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    public String getWorldName() {
        return worldName;
    }
    public String getWorld() {
        return worldName;
    }
    public double getX() {
        return x;
    }
    public double getBlockX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public double getBlockY() {
        return y;
    }

    public double getZ() {
        return z;
    }
    public double getBlockZ() {
        return z;
    }
    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    @Override
    public String toString() {
        return "SerializableLocation{" +
                "worldName='" + worldName + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", yaw=" + yaw +
                ", pitch=" + pitch +
                '}';
    }

    public Location toLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            throw new IllegalStateException("World " + worldName + " is not loaded!");
        }
        return new Location(world, x, y, z, yaw, pitch);
    }
}