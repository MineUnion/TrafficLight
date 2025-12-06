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

    // 配置项默认值
    private int proximityRadius = 10; //  proximity检测半径
    private boolean autoSave = true; // 自动保存开关

    public ConfigManager(TrafficLight plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    // 加载配置文件
    public void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.getDataFolder().mkdirs();
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        // 读取配置项
        this.proximityRadius = config.getInt("proximity-radius", 10);
        this.autoSave = config.getBoolean("auto-save", true);
    }

    // 保存红绿灯数据到配置
    public void saveTrafficLights(Map<String, TrafficLightEntity> lights) {
        if (!autoSave) return;

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
        }

        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("保存红绿灯配置失败：" + e.getMessage());
        }
    }

    // 加载红绿灯数据从配置
    public Map<String, Object> loadTrafficLights() {
        return (Map<String, Object>) config.get("traffic-lights", new HashMap<>());
    }

    // Getter/Setter
    public int getProximityRadius() { return proximityRadius; }
    public boolean isAutoSave() { return autoSave; }
    public FileConfiguration getConfig() { return config; }
}
