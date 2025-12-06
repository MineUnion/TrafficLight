package com.mineunion.trafficlight.entity;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

// 注意：类名避免和主类重复，加Entity后缀
public class TrafficLightEntity {
    // 红绿灯状态枚举
    public enum LightState {
        RED, GREEN, YELLOW
    }

    private final String name; // 唯一名称
    private Location location; // 位置
    private final Map<LightState, Integer> durationMap; // 各状态时长
    private LightState currentState; // 当前状态
    private int currentDuration; // 当前状态已运行时长
    private boolean activated; // 是否激活（距离激活）
    private String groupName; // 所属分组

    public TrafficLightEntity(String name, Location location) {
        this.name = name;
        this.location = location;
        this.durationMap = new HashMap<>();
        // 默认时长
        this.durationMap.put(LightState.RED, 10);
        this.durationMap.put(LightState.GREEN, 10);
        this.durationMap.put(LightState.YELLOW, 3);
        this.currentState = LightState.RED;
        this.currentDuration = 0;
        this.activated = false;
        this.groupName = "default";
    }

    // 切换状态
    public void switchState() {
        switch (currentState) {
            case RED:
                currentState = LightState.GREEN;
                break;
            case GREEN:
                currentState = LightState.YELLOW;
                break;
            case YELLOW:
                currentState = LightState.RED;
                break;
        }
        currentDuration = 0; // 重置时长
    }

    // 更新时长（返回是否需要切换状态）
    public boolean updateDuration() {
        if (!activated) {
            return false;
        }
        currentDuration++;
        return currentDuration >= durationMap.get(currentState);
    }

    // Getter & Setter
    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Map<LightState, Integer> getDurationMap() {
        return durationMap;
    }

    public void setDuration(LightState state, int duration) {
        durationMap.put(state, duration);
    }

    public LightState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(LightState currentState) {
        this.currentState = currentState;
        this.currentDuration = 0;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    // 序列化（用于文件存储）
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("world", location.getWorld().getName());
        data.put("x", location.getX());
        data.put("y", location.getY());
        data.put("z", location.getZ());
        data.put("red_duration", durationMap.get(LightState.RED));
        data.put("green_duration", durationMap.get(LightState.GREEN));
        data.put("yellow_duration", durationMap.get(LightState.YELLOW));
        data.put("current_state", currentState.name());
        data.put("group", groupName);
        return data;
    }

    // 反序列化
    public static TrafficLightEntity deserialize(Map<String, Object> data, World world) {
        String name = (String) data.get("name");
        double x = (double) data.get("x");
        double y = (double) data.get("y");
        double z = (double) data.get("z");
        Location loc = new Location(world, x, y, z);

        TrafficLightEntity tle = new TrafficLightEntity(name, loc);
        tle.setDuration(LightState.RED, (int) data.get("red_duration"));
        tle.setDuration(LightState.GREEN, (int) data.get("green_duration"));
        tle.setDuration(LightState.YELLOW, (int) data.get("yellow_duration"));
        tle.setCurrentState(LightState.valueOf((String) data.get("current_state")));
        tle.setGroupName((String) data.get("group"));
        return tle;
    }
}