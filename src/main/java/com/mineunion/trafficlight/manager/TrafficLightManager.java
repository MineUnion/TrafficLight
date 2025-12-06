package com.mineunion.trafficlight.manager;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.entity.TrafficLightEntity;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// 修复：导入LightUpdateTask类
import com.mineunion.trafficlight.task.LightUpdateTask;

public class TrafficLightManager {
    private final TrafficLight plugin;
    private final Map<String, TrafficLightEntity> lights = new HashMap<>(); // id → 红绿灯实体

    public TrafficLightManager(TrafficLight plugin) {
        this.plugin = plugin;
        loadAllTrafficLights(); // 初始化时加载数据
    }

    // 修复1：补充createTrafficLight()方法（适配CreateCommand调用）
    public boolean createTrafficLight(String id, String name, Location location) {
        if (lights.containsKey(id)) {
            return false; // 已存在相同ID，创建失败
        }
        TrafficLightEntity light = new TrafficLightEntity(id, name, location);
        lights.put(id, light);
        // 启动更新任务
        new LightUpdateTask(plugin, light).runTaskLater(plugin, light.getDuration(light.getState()) * 20L);
        return true;
    }

    // 修复2：补充deleteTrafficLight()方法（适配DeleteCommand调用）
    public boolean deleteTrafficLight(String id) {
        return lights.remove(id) != null;
    }

    // 修复3：补充setLightDuration()方法（适配SetCommand调用）
    public boolean setLightDuration(String lightId, TrafficLightEntity.LightState state, int seconds) {
        TrafficLightEntity tle = getLight(lightId);
        if (tle == null) return false;
        tle.setDuration(state, seconds);
        return true;
    }

    // 修复4：补充loadAllTrafficLights()方法（适配ReloadCommand调用）
    public void loadAllTrafficLights() {
        Map<String, Object> configData = plugin.getConfigManager().loadTrafficLights();
        for (Map.Entry<String, Object> entry : configData.entrySet()) {
            String lightId = entry.getKey();
            Map<String, Object> lightData = (Map<String, Object>) entry.getValue();
            String worldName = (String) lightData.get("world");
            World world = org.bukkit.Bukkit.getWorld(worldName);
            if (world == null) continue;
            // 反序列化加载红绿灯
            TrafficLightEntity tle = TrafficLightEntity.deserialize(lightData, world);
            lights.put(lightId, tle);
        }
        plugin.getLogger().info("加载了 " + lights.size() + " 个红绿灯");
    }

    // 修复5：补充saveAllTrafficLights()方法（适配PluginReloadListener调用）
    public void saveAllTrafficLights() {
        plugin.getConfigManager().saveTrafficLights(lights);
        plugin.getLogger().info("保存了 " + lights.size() + " 个红绿灯");
    }

    // 修复6：补充getAllTrafficLights()方法（适配ProximityCheckTask调用）
    public List<TrafficLightEntity> getAllTrafficLights() {
        return new ArrayList<>(lights.values());
    }

    // 修复7：补充getTrafficLightsByWorld()方法（适配PlayerProximityListener调用）
    public List<TrafficLightEntity> getTrafficLightsByWorld(String worldName) {
        List<TrafficLightEntity> result = new ArrayList<>();
        for (TrafficLightEntity tle : lights.values()) {
            if (tle.getLocation().getWorld().getName().equals(worldName)) {
                result.add(tle);
            }
        }
        return result;
    }

    // 修复8：补充updateLightActivation()方法（适配ProximityCheckTask调用）
    public void updateLightActivation(TrafficLightEntity tle, boolean activated) {
        tle.setActivated(activated);
        // 可选：添加激活状态变更的日志
        plugin.getLogger().info("[DEBUG] 红绿灯" + tle.getName() + "激活状态：" + activated);
    }

    // 原有方法保留
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
}
