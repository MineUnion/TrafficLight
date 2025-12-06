package com.mineunion.trafficlight.manager;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.entity.TrafficLightEntity;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrafficLightManager {
    private final TrafficLight plugin;
    private final Map<String, TrafficLightEntity> lights = new HashMap<>(); // id → 红绿灯实体

    public TrafficLightManager(TrafficLight plugin) {
        this.plugin = plugin;
    }

    // 修复1：补充getLight()方法（根据id获取红绿灯）
    public TrafficLightEntity getLight(String id) {
        return lights.get(id);
    }

    // 修复2：补充支持Location参数的addLight方法（适配调用时传入Location的场景）
    public void addLight(String id, String name, Location location) {
        TrafficLightEntity light = new TrafficLightEntity(id, name, location);
        lights.put(id, light);
        // 初始化时自动启动更新任务
        new LightUpdateTask(plugin, light).runTaskLater(plugin, light.getDuration(light.getState()) * 20L);
    }

    // 保留原addLight方法（兼容x/y/z参数）
    public void addLight(String id, String name, double x, double y, double z, World world) {
        addLight(id, name, new Location(world, x, y, z));
    }

    // 修复3：补充setDuration方法（设置红绿灯状态持续时间）
    public void setDuration(String lightId, TrafficLightEntity.LightState state, int seconds) {
        TrafficLightEntity tle = getLight(lightId);
        if (tle != null) {
            tle.setDuration(state, seconds);
        }
    }

    // 修复4：补充setActivated方法（设置红绿灯激活状态）
    public void setActivated(String lightId, boolean activated) {
        TrafficLightEntity tle = getLight(lightId);
        if (tle != null) {
            tle.setActivated(activated);
        }
    }

    // 修复5：补充getAllLightNames方法（获取所有红绿灯名称，适配ListCommand调用）
    public List<String> getAllLightNames() {
        List<String> names = new ArrayList<>();
        for (TrafficLightEntity tle : lights.values()) {
            names.add(tle.getName());
        }
        return names;
    }

    // 修复6：补充getAllLights方法（获取所有红绿灯，适配监听器/任务调用）
    public List<TrafficLightEntity> getAllLights() {
        return new ArrayList<>(lights.values());
    }

    // 补充：删除红绿灯
    public void removeLight(String id) {
        lights.remove(id);
    }

    // 补充：从配置反序列化加载红绿灯
    public void loadFromConfig(Map<String, Object> configData, World world) {
        for (Map.Entry<String, Object> entry : configData.entrySet()) {
            String lightId = entry.getKey();
            Map<String, Object> lightData = (Map<String, Object>) entry.getValue();
            // 调用带World参数的反序列化方法
            TrafficLightEntity tle = TrafficLightEntity.deserialize(lightData, world);
            lights.put(lightId, tle);
        }
    }
}
