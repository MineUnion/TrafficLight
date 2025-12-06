package com.mineunion.trafficlight.manager;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.entity.TrafficLightEntity;
import com.mineunion.trafficlight.task.LightUpdateTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrafficLightManager {
    private final TrafficLight plugin;
    private final Map<String, TrafficLightEntity> lights = new HashMap<>(); // 泛型明确，消除unchecked警告

    public TrafficLightManager(TrafficLight plugin) {
        this.plugin = plugin;
        loadAllTrafficLights();
    }

    // 修复：补充 getAllLights() 方法（子命令 Tab 补全调用）
    public List<TrafficLightEntity> getAllLights() {
        return new ArrayList<>(lights.values());
    }

    // 修复：loadAllTrafficLights() 类型转换优化，消除 unchecked 警告
    public void loadAllTrafficLights() {
        Map<String, Object> configData = plugin.getConfigManager().loadTrafficLights();
        if (configData.isEmpty()) return;

        // 安全类型转换，添加抑制警告注解
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> lightDataMap = (Map<String, Map<String, Object>>) configData;

        for (Map.Entry<String, Map<String, Object>> entry : lightDataMap.entrySet()) {
            String lightId = entry.getKey();
            Map<String, Object> lightData = entry.getValue();
            String worldName = (String) lightData.get("world");
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                plugin.getLogger().warning("加载红绿灯失败：世界 " + worldName + " 不存在（ID：" + lightId + "）");
                continue;
            }

            // 解析位置和基础信息
            double x = (double) lightData.getOrDefault("x", 0.0);
            double y = (double) lightData.getOrDefault("y", 0.0);
            double z = (double) lightData.getOrDefault("z", 0.0);
            Location location = new Location(world, x, y, z);
            String name = (String) lightData.getOrDefault("name", "未命名红绿灯");

            // 解析状态（默认 RED）
            TrafficLightEntity.LightState state;
            try {
                state = TrafficLightEntity.LightState.valueOf(
                    ((String) lightData.getOrDefault("state", "RED")).toUpperCase()
                );
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("加载红绿灯失败：无效状态（ID：" + lightId + "），默认设为 RED");
                state = TrafficLightEntity.LightState.RED;
            }

            // 解析激活状态和持续时间
            boolean activated = (boolean) lightData.getOrDefault("activated", false);
            Map<String, Object> durationData = (Map<String, Object>) lightData.getOrDefault("duration", new HashMap<>());

            // 创建红绿灯实体
            TrafficLightEntity tle = new TrafficLightEntity(lightId, name, location);
            tle.setState(state);
            tle.setActivated(activated);
            tle.setDuration(TrafficLightEntity.LightState.RED, (int) durationData.getOrDefault("red", 30));
            tle.setDuration(TrafficLightEntity.LightState.GREEN, (int) durationData.getOrDefault("green", 30));
            tle.setDuration(TrafficLightEntity.LightState.YELLOW, (int) durationData.getOrDefault("yellow", 5));

            lights.put(lightId, tle);
        }

        plugin.getLogger().info("加载了 " + lights.size() + " 个红绿灯数据");
    }

    // 原有核心方法保留（完整实现）
    public boolean createTrafficLight(String id, String name, Location location) {
        if (lights.containsKey(id)) return false;
        TrafficLightEntity light = new TrafficLightEntity(id, name, location);
        lights.put(id, light);
        new LightUpdateTask(plugin, light).runTaskLater(plugin, light.getDuration(light.getState()) * 20L);
        return true;
    }

    public boolean deleteTrafficLight(String id) {
        return lights.remove(id) != null;
    }

    public boolean setLightDuration(String lightId, TrafficLightEntity.LightState state, int seconds) {
        TrafficLightEntity tle = getLight(lightId);
        if (tle == null) return false;
        tle.setDuration(state, seconds);
        return true;
    }

    public void saveAllTrafficLights() {
        plugin.getConfigManager().saveTrafficLights(lights);
    }

    public TrafficLightEntity getLight(String id) {
        return lights.get(id);
    }

    public List<String> getAllLightNames() {
        List<String> names = new ArrayList<>();
        for (TrafficLightEntity tle : lights.values()) {
            names.add(tle.getName());
        }
        return names;
    }

    public List<TrafficLightEntity> getTrafficLightsByWorld(String worldName) {
        List<TrafficLightEntity> result = new ArrayList<>();
        for (TrafficLightEntity tle : lights.values()) {
            if (tle.getLocation().getWorld().getName().equals(worldName)) {
                result.add(tle);
            }
        }
        return result;
    }

    public void updateLightActivation(TrafficLightEntity tle, boolean activated) {
        tle.setActivated(activated);
    }
}
