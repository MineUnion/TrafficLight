package com.mineunion.trafficlight.manager;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.entity.TrafficLightEntity;
import com.mineunion.trafficlight.task.LightUpdateTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrafficLightManager {
    private final TrafficLight plugin;
    private final Map<String, TrafficLightEntity> lights = new HashMap<>(); // 泛型明确，无警告

    public TrafficLightManager(TrafficLight plugin) {
        this.plugin = plugin;
        loadAllTrafficLights();
    }

    // 关键修复：明确 getAllTrafficLights() 方法（ProximityCheckTask 调用）
    public List<TrafficLightEntity> getAllTrafficLights() {
        return new ArrayList<>(lights.values());
    }

    // 关键修复：安全解析配置，避免类型转换错误
    public void loadAllTrafficLights() {
        Map<String, Object> configData = plugin.getConfigManager().loadTrafficLights();
        if (configData == null || configData.isEmpty()) {
            plugin.getLogger().info("未加载到红绿灯数据（配置文件为空）");
            return;
        }

        // 遍历配置，逐个解析（避免强制类型转换）
        for (Map.Entry<String, Object> entry : configData.entrySet()) {
            String lightId = entry.getKey();
            Object lightObj = entry.getValue();

            // 校验配置格式是否正确
            if (!(lightObj instanceof Map<?, ?> lightDataRaw)) {
                plugin.getLogger().warning("加载红绿灯失败：配置格式错误（ID：" + lightId + "）");
                continue;
            }

            // 安全转换为 String -> Object 映射（消除 unchecked 警告）
            Map<String, Object> lightData = new HashMap<>();
            for (Map.Entry<?, ?> dataEntry : lightDataRaw.entrySet()) {
                if (dataEntry.getKey() instanceof String key) {
                    lightData.put(key, dataEntry.getValue());
                }
            }

            // 解析核心配置项（带默认值，避免空指针）
            String worldName = (String) lightData.getOrDefault("world", "world");
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                plugin.getLogger().warning("加载红绿灯失败：世界 " + worldName + " 不存在（ID：" + lightId + "）");
                continue;
            }

            double x = getDoubleValue(lightData.get("x"), 0.0);
            double y = getDoubleValue(lightData.get("y"), 0.0);
            double z = getDoubleValue(lightData.get("z"), 0.0);
            Location location = new Location(world, x, y, z);

            String name = (String) lightData.getOrDefault("name", "未命名-" + lightId);
            TrafficLightEntity.LightState state = parseLightState((String) lightData.get("state"));
            boolean activated = (boolean) lightData.getOrDefault("activated", false);

            // 解析持续时间
            Map<String, Object> durationData = new HashMap<>();
            Object durationObj = lightData.get("duration");
            if (durationObj instanceof Map<?, ?> durationRaw) {
                for (Map.Entry<?, ?> durEntry : durationRaw.entrySet()) {
                    if (durEntry.getKey() instanceof String key) {
                        durationData.put(key, durEntry.getValue());
                    }
                }
            }
            int redDur = getIntValue(durationData.get("red"), 30);
            int greenDur = getIntValue(durationData.get("green"), 30);
            int yellowDur = getIntValue(durationData.get("yellow"), 5);

            // 创建实体并添加到集合
            TrafficLightEntity tle = new TrafficLightEntity(lightId, name, location);
            tle.setState(state);
            tle.setActivated(activated);
            tle.setDuration(TrafficLightEntity.LightState.RED, redDur);
            tle.setDuration(TrafficLightEntity.LightState.GREEN, greenDur);
            tle.setDuration(TrafficLightEntity.LightState.YELLOW, yellowDur);

            lights.put(lightId, tle);
        }

        plugin.getLogger().info("成功加载 " + lights.size() + " 个红绿灯数据");
    }

    // 辅助方法：安全解析 double 值（避免类型转换异常）
    private double getDoubleValue(Object value, double defaultValue) {
        if (value instanceof Number num) {
            return num.doubleValue();
        } else if (value instanceof String str) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    // 辅助方法：安全解析 int 值（避免类型转换异常）
    private int getIntValue(Object value, int defaultValue) {
        if (value instanceof Number num) {
            return num.intValue();
        } else if (value instanceof String str) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    // 辅助方法：解析灯状态（默认 RED，避免无效值）
    private TrafficLightEntity.LightState parseLightState(String stateStr) {
        if (stateStr == null || stateStr.isEmpty()) {
            return TrafficLightEntity.LightState.RED;
        }
        try {
            return TrafficLightEntity.LightState.valueOf(stateStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return TrafficLightEntity.LightState.RED;
        }
    }

    // 所有核心业务方法（完整实现，无缺失）
    public boolean createTrafficLight(String id, String name, Location location) {
        if (lights.containsKey(id)) {
            return false;
        }
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
        if (tle == null || seconds <= 0) {
            return false;
        }
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
        if (tle != null) {
            tle.setActivated(activated);
        }
    }

    // 子命令 Tab 补全调用（之前的 getAllLights() 别名，避免混淆）
    public List<TrafficLightEntity> getAllLights() {
        return getAllTrafficLights();
    }
}
