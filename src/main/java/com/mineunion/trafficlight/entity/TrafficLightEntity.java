package com.mineunion.trafficlight.entity;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class TrafficLightEntity implements ConfigurationSerializable {
    public enum LightState {
        RED, GREEN, YELLOW
    }

    private final String id;
    private final String name; // 新增：名称字段
    private LightState state;
    private final Location location; // 新增：Location字段（替代单独的x/y/z）
    private boolean activated = false; // 新增：激活状态字段
    // 新增：每种状态的持续时间（默认30秒）
    private Map<LightState, Integer> durationMap = new HashMap<>() {{
        put(LightState.RED, 30);
        put(LightState.GREEN, 30);
        put(LightState.YELLOW, 5);
    }};

    // 修复1：新增支持Location参数的构造器（适配调用时传入Location的场景）
    public TrafficLightEntity(String id, String name, Location location) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.state = LightState.RED;
    }

    // 保留原x/y/z构造器（兼容旧代码）
    public TrafficLightEntity(String id, String name, double x, double y, double z, World world) {
        this.id = id;
        this.name = name;
        this.location = new Location(world, x, y, z);
        this.state = LightState.RED;
    }

    // 修复2：补充缺失的getter方法
    public String getId() { return id; }
    public String getName() { return name; } // 补充：getName()
    public LightState getState() { return state; }
    public Location getLocation() { return location; } // 补充：getLocation()
    public boolean isActivated() { return activated; } // 补充：isActivated()
    public int getDuration(LightState state) { return durationMap.getOrDefault(state, 30); } // 补充：获取持续时间

    // 修复3：补充缺失的setter方法
    public void setState(LightState state) { this.state = state; }
    public void setActivated(boolean activated) { this.activated = activated; } // 补充：setActivated()
    public void setDuration(LightState state, int seconds) { durationMap.put(state, seconds); } // 补充：setDuration()

    // 修复4：补充支持World参数的反序列化方法（适配调用时传入World的场景）
    public static TrafficLightEntity deserialize(Map<String, Object> data, World world) {
        String id = (String) data.get("id");
        String name = (String) data.get("name");
        double x = (double) data.get("x");
        double y = (double) data.get("y");
        double z = (double) data.get("z");
        LightState state = LightState.valueOf((String) data.get("state"));
        boolean activated = (boolean) data.getOrDefault("activated", false);

        TrafficLightEntity entity = new TrafficLightEntity(id, name, new Location(world, x, y, z));
        entity.setState(state);
        entity.setActivated(activated);
        return entity;
    }

    // 原反序列化方法（保留，兼容无World参数的场景）
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("name", name);
        data.put("x", location.getX());
        data.put("y", location.getY());
        data.put("z", location.getZ());
        data.put("world", location.getWorld().getName());
        data.put("state", state.name());
        data.put("activated", activated);
        return data;
    }

    // 原反序列化静态方法（保留）
    public static TrafficLightEntity deserialize(Map<String, Object> data) {
        String id = (String) data.get("id");
        String name = (String) data.get("name");
        double x = (double) data.get("x");
        double y = (double) data.get("y");
        double z = (double) data.get("z");
        World world = org.bukkit.Bukkit.getWorld((String) data.get("world"));
        LightState state = LightState.valueOf((String) data.get("state"));
        boolean activated = (boolean) data.getOrDefault("activated", false);

        TrafficLightEntity entity = new TrafficLightEntity(id, name, new Location(world, x, y, z));
        entity.setState(state);
        entity.setActivated(activated);
        return entity;
    }
}
