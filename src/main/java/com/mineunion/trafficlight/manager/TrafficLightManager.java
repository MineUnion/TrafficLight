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
    private final Map<String, TrafficLightEntity> lights = new HashMap<>();

    public TrafficLightManager(TrafficLight plugin) {
        this.plugin = plugin;
        loadAllTrafficLights();
    }

    // 获取内部Map（供保存数据使用）
    public Map<String, TrafficLightEntity> getLightsMap() {
        return lights;
    }

    // 加载所有红绿灯数据（带刷新逻辑）
    public void loadAllTrafficLights() {
        lights.clear(); // 重载前清空旧数据
        Map<String, Object> configData = plugin.getConfigManager().loadTrafficLights();

        if (configData == null || configData.isEmpty()) {
            plugin.getLogger().info("未加载到红绿灯数据（数据文件为空）");
            return;
        }

        for (Map.Entry<String, Object> entry : configData.entrySet()) {
            String lightId = entry.getKey();
            Object lightObj = entry.getValue();

            if (!(lightObj instanceof Map<?, ?> lightDataRaw)) {
                plugin.getLogger().warning("加载红绿灯失败：配置格式错误（ID：" + lightId + "）");
                continue;
            }

            // 安全解析配置数据
            Map<String, Object> lightData = new HashMap<>();
            for (Map.Entry<?, ?> dataEntry : lightDataRaw.entrySet()) {
                if (dataEntry.getKey() instanceof String key) {
                    lightData.put(key, dataEntry.getValue());
                }
            }

            // 解析核心配置项
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

        // 加载后自动激活已启用的红绿灯
        for (TrafficLightEntity tle : lights.values()) {
            if (tle.isActivated()) {
                new LightUpdateTask(plugin, tle).runTaskLater(plugin, tle.getDuration(tle.getState()) * 20L);
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[Debug] 自动激活红绿灯：" + tle.getId());
                }
            }
        }
    }

    // 辅助方法：安全解析double
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

    // 辅助方法：安全解析int
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

    // 辅助方法：解析灯状态
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

    // 查找指定位置的红绿灯
    private String findLightIdByLocation(Location location) {
        for (Map.Entry<String, TrafficLightEntity> entry : lights.entrySet()) {
            TrafficLightEntity light = entry.getValue();
            Location lightLocation = light.getLocation();
            if (lightLocation.getWorld().getName().equals(location.getWorld().getName()) &&
                lightLocation.getBlockX() == location.getBlockX() &&
                lightLocation.getBlockY() == location.getBlockY() &&
                lightLocation.getBlockZ() == location.getBlockZ()) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    // 核心业务方法：创建红绿灯（支持强制替换）
    public boolean createTrafficLight(String id, String name, Location location) {
        // 检查是否已存在相同ID的红绿灯
        if (lights.containsKey(id)) {
            return false;
        }
        
        // 转换为方块坐标（去除小数点）
        Location blockLocation = location.toBlockLocation();
        
        // 强制替换：如果目标位置已有红绿灯，先删除
        String existingId = findLightIdByLocation(blockLocation);
        if (existingId != null) {
            deleteTrafficLight(existingId);
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[Debug] 替换位置上的原有红绿灯：ID=" + existingId + ", 新ID=" + id);
            }
        }
        
        TrafficLightEntity light = new TrafficLightEntity(id, name, blockLocation);
        
        // 应用当前世界的默认时长
        String worldName = blockLocation.getWorld().getName();
        ConfigManager configManager = plugin.getConfigManager();
        light.setDuration(TrafficLightEntity.LightState.RED, configManager.getDefaultDuration(worldName, TrafficLightEntity.LightState.RED));
        light.setDuration(TrafficLightEntity.LightState.GREEN, configManager.getDefaultDuration(worldName, TrafficLightEntity.LightState.GREEN));
        light.setDuration(TrafficLightEntity.LightState.YELLOW, configManager.getDefaultDuration(worldName, TrafficLightEntity.LightState.YELLOW));
        
        lights.put(id, light);
        new LightUpdateTask(plugin, light).runTaskLater(plugin, light.getDuration(light.getState()) * 20L);
        
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[Debug] 创建红绿灯：ID=" + id + ", 位置=" + blockLocation);
        }
        return true;
    }

    // 核心业务方法：删除红绿灯
    public boolean deleteTrafficLight(String id) {
        boolean removed = lights.remove(id) != null;
        if (removed && plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[Debug] 删除红绿灯：ID=" + id);
        }
        return removed;
    }

    // 核心业务方法：设置灯色时长
    public boolean setLightDuration(String lightId, TrafficLightEntity.LightState state, int seconds) {
        TrafficLightEntity tle = getLight(lightId);
        if (tle == null || seconds <= 0) {
            return false;
        }
        tle.setDuration(state, seconds);
        
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[Debug] 设置红绿灯时长：ID=" + lightId + ", 状态=" + state + ", 时长=" + seconds + "秒");
        }
        return true;
    }

    // 保存所有红绿灯数据
    public void saveAllTrafficLights() {
        plugin.getConfigManager().saveTrafficLights(lights);
    }

    // 获取单个红绿灯
    public TrafficLightEntity getLight(String id) {
        return lights.get(id);
    }

    // 获取所有红绿灯名称
    public List<String> getAllLightNames() {
        List<String> names = new ArrayList<>();
        for (TrafficLightEntity tle : lights.values()) {
            names.add(tle.getName());
        }
        return names;
    }

    // 获取指定世界的红绿灯
    public List<TrafficLightEntity> getTrafficLightsByWorld(String worldName) {
        List<TrafficLightEntity> result = new ArrayList<>();
        for (TrafficLightEntity tle : lights.values()) {
            if (tle.getLocation().getWorld().getName().equals(worldName)) {
                result.add(tle);
            }
        }
        return result;
    }

    // 更新红绿灯激活状态
    public void updateLightActivation(TrafficLightEntity tle, boolean activated) {
        if (tle != null) {
            tle.setActivated(activated);
            if (activated) {
                // 激活时启动状态更新任务
                new LightUpdateTask(plugin, tle).runTaskLater(plugin, tle.getDuration(tle.getState()) * 20L);
            }
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[Debug] 更新红绿灯激活状态：ID=" + tle.getId() + ", 激活=" + activated);
            }
        }
    }

    // 手动切换灯色
    public boolean switchLightState(String lightId, TrafficLightEntity.LightState state) {
        TrafficLightEntity tle = getLight(lightId);
        if (tle == null) {
            return false;
        }
        tle.setState(state);
        // 重启定时任务
        new LightUpdateTask(plugin, tle).runTaskLater(plugin, tle.getDuration(state) * 20L);
        
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[Debug] 手动切换红绿灯状态：ID=" + lightId + ", 新状态=" + state);
        }
        return true;
    }

    // 切换激活状态（toggle）
    public boolean toggleLightActivation(String lightId) {
        TrafficLightEntity tle = getLight(lightId);
        if (tle == null) {
            return false;
        }
        boolean newState = !tle.isActivated();
        tle.setActivated(newState);
        
        if (newState) {
            new LightUpdateTask(plugin, tle).runTaskLater(plugin, tle.getDuration(tle.getState()) * 20L);
        }
        
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[Debug] 切换红绿灯激活状态：ID=" + lightId + ", 新状态=" + newState);
        }
        return true;
    }

    // 获取单个红绿灯详情
    public String getLightInfo(String lightId) {
        TrafficLightEntity tle = getLight(lightId);
        if (tle == null) {
            return "红绿灯不存在（ID：" + lightId + "）";
        }
        Location loc = tle.getLocation();
        return String.format(
            "红绿灯详情：\nID：%s\n名称：%s\n位置：%s (%f, %f, %f)\n当前状态：%s\n激活状态：%s\n时长配置：红灯%d秒 / 绿灯%d秒 / 黄灯%d秒",
            tle.getId(),
            tle.getName(),
            loc.getWorld().getName(),
            loc.getX(),
            loc.getY(),
            loc.getZ(),
            tle.getState().name(),
            tle.isActivated() ? "已激活" : "未激活",
            tle.getDuration(TrafficLightEntity.LightState.RED),
            tle.getDuration(TrafficLightEntity.LightState.GREEN),
            tle.getDuration(TrafficLightEntity.LightState.YELLOW)
        );
    }

    // 获取所有红绿灯（供命令调用）
    public List<TrafficLightEntity> getAllTrafficLights() {
        return new ArrayList<>(lights.values());
    }

    // 兼容旧命令的别名方法
    public List<TrafficLightEntity> getAllLights() {
        return getAllTrafficLights();
    }
}
