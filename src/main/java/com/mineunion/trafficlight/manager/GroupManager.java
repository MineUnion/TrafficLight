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

    // 添加红绿灯到分组
    public void addToGroup(String lightId, String groupName) {
        lightGroups.put(lightId, groupName);
    }

    // 从分组移除红绿灯
    public void removeFromGroup(String lightId) {
        lightGroups.remove(lightId);
    }

    // 获取红绿灯所属分组
    public String getGroup(String lightId) {
        return lightGroups.getOrDefault(lightId, "default");
    }

    // 同步分组内所有红绿灯状态
    public void syncGroupState(String groupName, TrafficLightEntity.LightState state) {
        for (Map.Entry<String, String> entry : lightGroups.entrySet()) {
            if (entry.getValue().equals(groupName)) {
                String lightId = entry.getKey();
                TrafficLightEntity light = plugin.getTrafficLightManager().getLight(lightId);
                if (light != null) {
                    light.setState(state);
                    // 修复：替换debug()为info()，添加[DEBUG]标识
                    plugin.getLogger().info("[DEBUG] 同步分组" + groupName + "的红绿灯" + lightId + "为" + state + "状态");
                }
            }
        }
    }

    // 获取所有分组
    public Map<String, String> getAllGroups() {
        return new HashMap<>(lightGroups);
    }
}