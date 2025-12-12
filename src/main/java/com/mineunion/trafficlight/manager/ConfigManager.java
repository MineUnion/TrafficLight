package com.mineunion.trafficlight.manager; // 改为 manager 包

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.entity.TrafficLightEntity;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final TrafficLight plugin;
    private FileConfiguration config;
    private File configFile;
    private File trafficLightsFile;
    private FileConfiguration trafficLightsConfig;

    // 配置版本
    private static final String CURRENT_CONFIG_VERSION = "1.0.0";
    private String configVersion = CURRENT_CONFIG_VERSION;

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
        
        // 检查并更新配置文件版本
        checkConfigVersion();

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
    
    // 检查并更新配置文件版本
    private void checkConfigVersion() {
        // 读取配置文件版本
        String fileVersion = config.getString("config-version");
        
        // 如果配置文件没有版本号，或者版本号不匹配
        if (fileVersion == null || !fileVersion.equals(CURRENT_CONFIG_VERSION)) {
            plugin.getLogger().info("正在更新配置文件... 当前版本: " + (fileVersion == null ? "未知" : fileVersion) + ", 目标版本: " + CURRENT_CONFIG_VERSION);
            
            // 备份旧配置
            backupOldConfig();
            
            // 更新配置文件
            updateConfigFile();
            
            // 重新加载配置
            config = YamlConfiguration.loadConfiguration(configFile);
            
            plugin.getLogger().info("配置文件更新完成！");
        }
        
        // 更新当前配置版本
        this.configVersion = CURRENT_CONFIG_VERSION;
    }
    
    // 备份旧配置
    private void backupOldConfig() {
        try {
            File backupFile = new File(plugin.getDataFolder(), "config.yml.backup." + System.currentTimeMillis());
            Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            plugin.getLogger().info("旧配置文件已备份到: " + backupFile.getName());
        } catch (IOException e) {
            plugin.getLogger().severe("备份旧配置文件失败: " + e.getMessage());
        }
    }
    
    // 更新配置文件
    private void updateConfigFile() {
        try {
            // 加载默认配置
            InputStream defaultConfigStream = plugin.getResource("config.yml");
            if (defaultConfigStream == null) {
                plugin.getLogger().severe("无法加载默认配置文件！");
                return;
            }
            
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream));
            
            // 合并配置，保留原有配置值
            for (String key : defaultConfig.getKeys(true)) {
                if (!config.contains(key)) {
                    // 添加新配置项
                    config.set(key, defaultConfig.get(key));
                    if (isDebugMode()) {
                        plugin.getLogger().info("[Debug] 添加新配置项: " + key + " = " + defaultConfig.get(key));
                    }
                }
            }
            
            // 更新配置版本
            config.set("config-version", CURRENT_CONFIG_VERSION);
            
            // 保存更新后的配置
            config.save(configFile);
            
        } catch (Exception e) {
            plugin.getLogger().severe("更新配置文件失败: " + e.getMessage());
            e.printStackTrace();
        }
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

    // 保存红绿灯数据
    @SuppressWarnings("unchecked")
    public void saveTrafficLights(Map<String, TrafficLightEntity> lights) {
        if (lights == null || lights.isEmpty()) {
            if (isDebugMode()) {
                plugin.getLogger().info("[Debug] 没有红绿灯数据需要保存");
            }
            return;
        }

        try {
            // 清空现有数据
            trafficLightsConfig.set("traffic-lights", null);
            
            // 逐个保存红绿灯数据
            for (Map.Entry<String, TrafficLightEntity> entry : lights.entrySet()) {
                String path = "traffic-lights." + entry.getKey();
                TrafficLightEntity light = entry.getValue();
                
                // 确保位置和世界存在
                if (light.getLocation() == null || light.getLocation().getWorld() == null) {
                    plugin.getLogger().warning("跳过保存红绿灯 " + entry.getKey() + "：位置或世界无效");
                    continue;
                }
                
                trafficLightsConfig.set(path + ".name", light.getName());
                // 保存整数坐标，去除小数点
                trafficLightsConfig.set(path + ".x", light.getLocation().getBlockX());
                trafficLightsConfig.set(path + ".y", light.getLocation().getBlockY());
                trafficLightsConfig.set(path + ".z", light.getLocation().getBlockZ());
                trafficLightsConfig.set(path + ".world", light.getLocation().getWorld().getName());
                trafficLightsConfig.set(path + ".state", light.getState().name());
                trafficLightsConfig.set(path + ".activated", light.isActivated());
                trafficLightsConfig.set(path + ".duration.red", light.getDuration(TrafficLightEntity.LightState.RED));
                trafficLightsConfig.set(path + ".duration.green", light.getDuration(TrafficLightEntity.LightState.GREEN));
                trafficLightsConfig.set(path + ".duration.yellow", light.getDuration(TrafficLightEntity.LightState.YELLOW));
            }

            // 确保数据文件所在目录存在
            if (!trafficLightsFile.getParentFile().exists()) {
                trafficLightsFile.getParentFile().mkdirs();
            }
            
            // 保存文件
            trafficLightsConfig.save(trafficLightsFile);
            
            if (isDebugMode()) {
                plugin.getLogger().info("[Debug] 成功保存 " + lights.size() + " 个红绿灯数据");
            }
        } catch (IOException e) {
            plugin.getLogger().severe("保存红绿灯数据失败：" + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            plugin.getLogger().severe("保存红绿灯数据时发生未知异常：" + e.getMessage());
            e.printStackTrace();
        }
    }

    // 加载红绿灯数据
    @SuppressWarnings("unchecked")
    public Map<String, Object> loadTrafficLights() {
        try {
            Object data = trafficLightsConfig.get("traffic-lights");
            if (data instanceof Map<?, ?> mapData) {
                // 确保返回正确的Map<String, Object>结构
                Map<String, Object> result = new HashMap<>();
                for (Map.Entry<?, ?> entry : mapData.entrySet()) {
                    if (entry.getKey() instanceof String key) {
                        result.put(key, entry.getValue());
                    }
                }
                return result;
            }
        } catch (Exception e) {
            plugin.getLogger().severe("加载红绿灯数据时发生异常：" + e.getMessage());
        }
        return new HashMap<>();
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
    
    // 保存配置文件
    public void saveConfig() throws IOException {
        config.save(configFile);
    }
}
