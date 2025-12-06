package com.mineunion.trafficlight.entity;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import java.util.HashMap;
import java.util.Map;

public class TrafficLightEntity implements ConfigurationSerializable {
    public enum LightState {
        RED, GREEN, YELLOW
    }

    private final String id;
    private LightState state;
    private double x, y, z;

    public TrafficLightEntity(String id, double x, double y, double z) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.state = LightState.RED; // 默认红灯
    }

    // Getter/Setter
    public String getId() { return id; }
    public LightState getState() { return state; }
    public void setState(LightState state) { this.state = state; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }

    // 序列化实现（适配ConfigurationSerializable）
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("state", state.name());
        data.put("x", x);
        data.put("y", y);
        data.put("z", z);
        return data;
    }

    // 反序列化实现
    public static TrafficLightEntity deserialize(Map<String, Object> data) {
        String id = (String) data.get("id");
        double x = (double) data.get("x");
        double y = (double) data.get("y");
        double z = (double) data.get("z");
        
        TrafficLightEntity entity = new TrafficLightEntity(id, x, y, z);
        entity.setState(LightState.valueOf((String) data.get("state")));
        return entity;
    }
}
