package com.mineunion.trafficlight.util;

import org.bukkit.Location;
import org.bukkit.World;

public class LocationUtil {
    // 计算两个位置的直线距离（忽略Y轴，仅平面距离）
    public static double getFlatDistance(Location loc1, Location loc2) {
        if (!loc1.getWorld().equals(loc2.getWorld())) {
            return Double.MAX_VALUE;
        }
        double dx = loc1.getX() - loc2.getX();
        double dz = loc1.getZ() - loc2.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    // 位置序列化（用于存储）
    public static String serializeLocation(Location loc) {
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
    }

    // 位置反序列化
    public static Location deserializeLocation(String str) {
        String[] parts = str.split(",");
        if (parts.length != 4) {
            return null;
        }
        World world = org.bukkit.Bukkit.getWorld(parts[0]);
        if (world == null) {
            return null;
        }
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        return new Location(world, x, y, z);
    }
}