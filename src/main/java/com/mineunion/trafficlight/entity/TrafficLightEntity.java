package com.mineunion.trafficlight.entity;

import org.bukkit.Location;

public class TrafficLightEntity {
    public enum LightState {
        RED, GREEN, YELLOW
    }

    private final String id;
    private final String name;
    private final Location location;
    private LightState state;
    private boolean activated;
    private int redDuration;
    private int greenDuration;
    private int yellowDuration;

    public TrafficLightEntity(String id, String name, Location location) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.state = LightState.RED; // 默认红灯
        this.activated = true; // 默认激活状态
        this.redDuration = 30;
        this.greenDuration = 30;
        this.yellowDuration = 5;
    }

    // 补充 setState 方法
    public void setState(LightState state) {
        this.state = state;
    }

    // 原有 Getter/Setter 方法保留
    public String getId() { return id; }
    public String getName() { return name; }
    public Location getLocation() { return location; }
    public LightState getState() { return state; }
    public boolean isActivated() { return activated; }
    public void setActivated(boolean activated) { this.activated = activated; }
    public int getDuration(LightState state) {
        return switch (state) {
            case RED -> redDuration;
            case GREEN -> greenDuration;
            case YELLOW -> yellowDuration;
        };
    }
    public void setDuration(LightState state, int seconds) {
        switch (state) {
            case RED -> this.redDuration = seconds;
            case GREEN -> this.greenDuration = seconds;
            case YELLOW -> this.yellowDuration = seconds;
        }
    }
}
