package org.ast.bedwarspro.been;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Plot implements Serializable {
    private String name;
    private String owner;
    private SerializableLocation loc1;
    private SerializableLocation loc2;
    private ArrayList<String> coOwners = new ArrayList<>();
    private Map<String, Boolean> permissions = new HashMap<>();

    public Plot(String name, String owner, SerializableLocation loc1, SerializableLocation loc2) {
        this.name = name;
        this.owner = owner;
        this.loc1 = loc1;
        this.loc2 = loc2;
        permissions.put("allowUse", false);
        permissions.put("allowBuild", false);
        permissions.put("allowBreak", false);
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public SerializableLocation getLoc1() {
        return loc1;
    }

    public SerializableLocation getLoc2() {
        return loc2;
    }

    public ArrayList<String> getCoOwners() {
        return coOwners;
    }

    public Map<String, Boolean> getPermissions() {
        return permissions;
    }

    public void setPermission(String permission, boolean value) {
        permissions.put(permission, value);
    }

    public boolean isInside(org.bukkit.Location loc) {
        org.bukkit.Location loc1 = this.loc1.toLocation();
        org.bukkit.Location loc2 = this.loc2.toLocation();

        int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        int minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
        int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
        try {
            return loc.getBlockY() >= minY && loc.getBlockY() <= maxY &&
                    loc.getBlockZ() >= minZ && loc.getBlockZ() <= maxZ &&
                    loc.getBlockX() >= minX && loc.getBlockX() <= maxX;
        }catch (Exception e) {
            return false;
        }

    }
}