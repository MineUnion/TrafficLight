package com.mineunion.trafficlight.manager;

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

    // 基础配置
    private int proximityRadius = 10;
    private boolean autoSave = true;
    private long autoSaveInterval = 60 * 20L; // 60秒 = 1200刻

    // 性能优化配置（新增）
    private int proximityCheckInterval = 20;
    private boolean asyncProximityCheck = true;

    // 默认时长配置
    private int defaultRedDuration = 30;
    private int defaultGreenDuration = 30;
    private int defaultYellowDuration = 5;

    public ConfigManager(TrafficLight plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.getDataFolder().mkdirs();
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        // 加载所有配置项（带默认值）
        this.proximityRadius = config.getInt("proximity-radius", 10);
        this.autoSave = config.getBoolean("auto-save", true);
        this.autoSaveInterval = config.getInt("auto-save-interval", 60) * 20L;
        this.proximityCheckInterval = config.getInt("proximity-check-interval", 20);
        this.asyncProximityCheck = config.getBoolean("async-proximity-check", true);
        this.defaultRedDuration = config.getInt("default-duration.red", 30);
        this.defaultGreenDuration = config.getInt("default-duration.green", 30);
        this.defaultYellowDuration = config.getInt("default-duration.yellow", 5);
    }

    // 保存红绿灯数据
    public void saveTrafficLights(Map<String, TrafficLightEntity> lights) {
        if (!autoSave) {
            return;
        }

        config.set("traffic-lights", null); // 清空原有数据
        for (Map.Entry<String, TrafficLightEntity> entry : lights.entrySet()) {
            String path = "traffic-lights." + entry.getKey();
            TrafficLightEntity light = entry.getValue();
            config.set(path + ".name", light.getName());
            config.set(path + ".x", light.getLocation().getX());
            config.set(path + ".y", light.getLocation().getY());
            config.set(path + ".z", light.getLocation().getZ());
            config.set(path + ".world", light.getLocation().getWorld().getName());
            config.set(path + ".state", light.getState().name());
            config.set(path + ".activated", light.isActivated());
            config.set(path + ".duration.red", light.getDuration(TrafficLightEntity.LightState.RED));
            config.set(path + ".duration.green", light.getDuration(TrafficLightEntity.LightState.GREEN));
            config.set(path + ".duration.yellow", light.getDuration(TrafficLightEntity.LightState.YELLOW));
        }

        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("保存红绿灯配置失败：" + e.getMessage());
        }
    }

    // 加载红绿灯数据
    public Map<String, Object> loadTrafficLights() {
        Object data = config.get("traffic-lights");
        return data instanceof Map ? (Map<String, Object>) data : new HashMap<>();
    }

    // 所有 Getter 方法（完整）
    public int getProximityRadius() {
        return proximityRadius;
    }

    public boolean isAutoSave() {
        return autoSave;
    }

    public long getAutoSaveInterval() {
        return autoSaveInterval;
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
