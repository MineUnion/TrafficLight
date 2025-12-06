package com.mineunion.trafficlight.manager;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.entity.TrafficLightEntity;

import java.util.HashMap;
import java.util.Map;

public class GroupManager {
    private final TrafficLight plugin;
    private final Map<String, String> lightGroups = new HashMap<>(); // 红绿灯ID → 分组名

    public GroupManager(TrafficLight plugin) {
        this.plugin = plugin;
    }

    public void addToGroup(String lightId, String groupName) {
        lightGroups.put(lightId, groupName);
    }

    public void removeFromGroup(String lightId) {
        lightGroups.remove(lightId);
    }

    public String getGroup(String lightId) {
        return lightGroups.getOrDefault(lightId, "default");
    }

    // 修复：调用TrafficLightManager的getLight()方法（之前已补充该方法）
    public void syncGroupState(String groupName, TrafficLightEntity.LightState state) {
        for (Map.Entry<String, String> entry : lightGroups.entrySet()) {
            if (entry.getValue().equals(groupName)) {
                String lightId = entry.getKey();
                // 现在TrafficLightManager有getLight()方法，不会报错
                TrafficLightEntity light = plugin.getTrafficLightManager().getLight(lightId);
                if (light != null) {
                    light.setState(state);
                    plugin.getLogger().info("[DEBUG] 同步分组" + groupName + "的红绿灯" + light.getName() + "为" + state + "状态");
                }
            }
        }
    }

    public Map<String, String> getAllGroups() {
        return new HashMap<>(lightGroups);
    }
}
