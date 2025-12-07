package com.mineunion.trafficlight.config;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.entity.TrafficLightEntity;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final TrafficLight plugin;
    private FileConfiguration config;
    private File configFile;
    private File trafficLightsFile;
    private FileConfiguration trafficLightsConfig;

    // 基础配置
    private int proximityRadius = 10;
    private boolean autoSave = true;
    private long autoSaveInterval = 60 * 20L;
    private boolean proximityTrigger = true;
    private boolean debugMode = false;

    // 性能优化配置
    private int proximityCheckInterval = 20;
    private boolean asyncProximityCheck = true;

    // 默认时长配置
    private int defaultRedDuration = 30;
    private int defaultGreenDuration = 30;
    private int defaultYellowDuration = 5;

    public ConfigManager(TrafficLight plugin) {
        this.plugin = plugin;
        loadConfig();
        loadTrafficLightsData();
        startAutoSaveTask();
    }

    // 加载主配置
    public void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.getDataFolder().mkdirs();
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        this.proximityRadius = config.getInt("proximity-radius", 10);
        this.autoSave = config.getBoolean("auto-save", true);
        this.autoSaveInterval = config.getInt("auto-save-interval", 60) * 20L;
        this.proximityTrigger = config.getBoolean("proximity-trigger", true);
        this.debugMode = config.getBoolean("debug-mode", false);
        this.proximityCheckInterval = config.getInt("proximity-check-interval", 20);
        this.asyncProximityCheck = config.getBoolean("async-proximity-check", true);
        this.defaultRedDuration = config.getInt("default-duration.red", 30);
        this.defaultGreenDuration = config.getInt("default-duration.green", 30);
        this.defaultYellowDuration = config.getInt("default-duration.yellow", 5);
    }

    // 初始化红绿灯数据文件
    private void loadTrafficLightsData() {
        trafficLightsFile = new File(plugin.getDataFolder(), "traffic-lights.yml");
        if (!trafficLightsFile.exists()) {
            try {
                trafficLightsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("创建红绿灯数据文件失败：" + e.getMessage());
            }
        }
        trafficLightsConfig = YamlConfiguration.loadConfiguration(trafficLightsFile);
    }

    // 保存红绿灯数据（修复 unchecked 警告）
    @SuppressWarnings("unchecked")
    public void saveTrafficLights(Map<String, TrafficLightEntity> lights) {
        if (!autoSave) return;

        trafficLightsConfig.set("traffic-lights", null);
        for (Map.Entry<String, TrafficLightEntity> entry : lights.entrySet()) {
            String path = "traffic-lights." + entry.getKey();
            TrafficLightEntity light = entry.getValue();
            trafficLightsConfig.set(path + ".name", light.getName());
            trafficLightsConfig.set(path + ".x", light.getLocation().getX());
            trafficLightsConfig.set(path + ".y", light.getLocation().getY());
            trafficLightsConfig.set(path + ".z", light.getLocation().getZ());
            trafficLightsConfig.set(path + ".world", light.getLocation().getWorld().getName());
            trafficLightsConfig.set(path + ".state", light.getState().name());
            trafficLightsConfig.set(path + ".activated", light.isActivated());
            trafficLightsConfig.set(path + ".duration.red", light.getDuration(TrafficLightEntity.LightState.RED));
            trafficLightsConfig.set(path + ".duration.green", light.getDuration(TrafficLightEntity.LightState.GREEN));
            trafficLightsConfig.set(path + ".duration.yellow", light.getDuration(TrafficLightEntity.LightState.YELLOW));
        }

        try {
            trafficLightsConfig.save(trafficLightsFile);
            if (isDebugMode()) {
                plugin.getLogger().info("[Debug] 成功保存 " + lights.size() + " 个红绿灯数据");
            }
        } catch (IOException e) {
            plugin.getLogger().severe("保存红绿灯数据失败：" + e.getMessage());
        }
    }

    // 加载红绿灯数据（修复 unchecked 警告）
    @SuppressWarnings("unchecked")
    public Map<String, Object> loadTrafficLights() {
        Object data = trafficLightsConfig.get("traffic-lights");
        return data instanceof Map ? (Map<String, Object>) data : new HashMap<>();
    }

    // 自动保存任务
    private void startAutoSaveTask() {
        if (!autoSave) return;

        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            TrafficLightManager lightManager = plugin.getTrafficLightManager();
            if (lightManager != null) {
                saveTrafficLights(lightManager.getLightsMap());
                if (isDebugMode()) {
                    plugin.getLogger().info("[Debug] 自动保存红绿灯数据完成");
                }
            }
        }, autoSaveInterval, autoSaveInterval);
    }

    // 手动保存
    public void manualSaveTrafficLights() {
        TrafficLightManager lightManager = plugin.getTrafficLightManager();
        if (lightManager != null) {
            saveTrafficLights(lightManager.getLightsMap());
            plugin.getLogger().info("手动保存红绿灯数据完成");
        }
    }

    // 多世界配置支持
    public int getProximityRadius(String worldName) {
        if (config.contains("per-world-config." + worldName + ".proximity-radius")) {
            return config.getInt("per-world-config." + worldName + ".proximity-radius");
        }
        return this.proximityRadius;
    }

    public int getDefaultDuration(String worldName, TrafficLightEntity.LightState state) {
        String path = "per-world-config." + worldName + ".default-duration." + state.name().toLowerCase();
        if (config.contains(path)) {
            return config.getInt(path);
        }
        return switch (state) {
            case RED -> defaultRedDuration;
            case GREEN -> defaultGreenDuration;
            case YELLOW -> defaultYellowDuration;
        };
    }

    // 所有 Getter/Setter
    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        config.set("debug-mode", debugMode);
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("修改Debug模式失败：" + e.getMessage());
        }
    }

    public FileConfiguration getTrafficLightsConfig() {
        return trafficLightsConfig;
    }

    public int getProximityRadius() {
        return proximityRadius;
    }

    public boolean isAutoSave() {
        return autoSave;
    }

    public long getAutoSaveInterval() {
        return autoSaveInterval;
    }

    public boolean isProximityTrigger() {
        return proximityTrigger;
    }

    public int getProximityCheckInterval() {
        return proximityCheckInterval;
    }

    public boolean isAsyncProximityCheck() {
        return asyncProximityCheck;
    }

    public int getDefaultRedDuration() {
        return defaultRedDuration;
    }

    public int getDefaultGreenDuration() {
        return defaultGreenDuration;
    }

    public int getDefaultYellowDuration() {
        return defaultYellowDuration;
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
