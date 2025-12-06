package com.mineunion.trafficlight.manager;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.config.ConfigManager;
import com.mineunion.trafficlight.entity.TrafficLightEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TrafficLightManager {
    private final TrafficLight plugin;
    private final ConfigManager configManager;
    // 存储所有红绿灯（线程安全）
    private final Map<String, TrafficLightEntity> trafficLightMap = new ConcurrentHashMap<>();
    // 数据存储文件
    private File dataFile;
    private YamlConfiguration dataConfig;

    public TrafficLightManager(TrafficLight plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        // 初始化数据文件
        initDataFile();
        // 加载红绿灯数据
        loadAllTrafficLights();
    }

    // 初始化数据文件
    private void initDataFile() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        dataFile = new File(plugin.getDataFolder(), "trafficlights.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("创建红绿灯数据文件失败：" + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    // 创建红绿灯
    public boolean createTrafficLight(String name, Location location) {
        if (trafficLightMap.containsKey(name)) {
            return false; // 名称已存在
        }
        TrafficLightEntity tle = new TrafficLightEntity(name, location);
        trafficLightMap.put(name, tle);
        saveTrafficLight(tle);
        return true;
    }

    // 删除红绿灯
    public boolean deleteTrafficLight(String name) {
        if (!trafficLightMap.containsKey(name)) {
            return false;
        }
        trafficLightMap.remove(name);
        // 从数据文件删除
        dataConfig.set(name, null);
        saveDataFile();
        return true;
    }

    // 修改红绿灯时长
    public boolean setLightDuration(String name, TrafficLightEntity.LightState state, int duration) {
        TrafficLightEntity tle = trafficLightMap.get(name);
        if (tle == null) {
            return false;
        }
        tle.setDuration(state, duration);
        saveTrafficLight(tle);
        return true;
    }

    // 获取红绿灯列表（按世界筛选）
    public List<TrafficLightEntity> getTrafficLightsByWorld(String worldName) {
        List<TrafficLightEntity> list = new ArrayList<>();
        for (TrafficLightEntity tle : trafficLightMap.values()) {
            if (tle.getLocation().getWorld().getName().equals(worldName)) {
                list.add(tle);
            }
        }
        return list;
    }

    // 获取所有红绿灯
    public Collection<TrafficLightEntity> getAllTrafficLights() {
        return trafficLightMap.values();
    }

    // 获取单个红绿灯
    public TrafficLightEntity getTrafficLight(String name) {
        return trafficLightMap.get(name);
    }

    // 激活/休眠红绿灯（距离激活）
    public void updateLightActivation(TrafficLightEntity tle, boolean activated) {
        tle.setActivated(activated);
    }

    // 加载所有红绿灯数据
    private void loadAllTrafficLights() {
        String storageType = configManager.getStorageType();
        if ("file".equals(storageType)) {
            // 文件加载
            loadFromFile();
        } else if ("mysql".equals(storageType)) {
            // MySQL加载（后续实现）
            loadFromMysql();
        }
    }

    // 保存所有红绿灯数据
    public void saveAllTrafficLights() {
        String storageType = configManager.getStorageType();
        if ("file".equals(storageType)) {
            saveToFile();
        } else if ("mysql".equals(storageType)) {
            saveToMysql();
        }
    }

    // 保存单个红绿灯
    private void saveTrafficLight(TrafficLightEntity tle) {
        String storageType = configManager.getStorageType();
        if ("file".equals(storageType)) {
            saveToFile(tle);
        } else if ("mysql".equals(storageType)) {
            saveToMysql(tle);
        }
    }

    // ---------------------- 文件存储实现 ----------------------
    private void loadFromFile() {
        Set<String> keys = dataConfig.getKeys(false);
        for (String key : keys) {
            Map<String, Object> data = dataConfig.getConfigurationSection(key).getValues(false);
            String worldName = (String) data.get("world");
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                plugin.getLogger().warning("加载红绿灯失败：世界" + worldName + "不存在！");
                continue;
            }
            TrafficLightEntity tle = TrafficLightEntity.deserialize(data, world);
            trafficLightMap.put(key, tle);
        }
        plugin.getLogger().info("从文件加载了" + trafficLightMap.size() + "个红绿灯数据");
    }

    private void saveToFile() {
        for (TrafficLightEntity tle : trafficLightMap.values()) {
            saveToFile(tle);
        }
        saveDataFile();
    }

    private void saveToFile(TrafficLightEntity tle) {
        Map<String, Object> data = tle.serialize();
        dataConfig.createSection(tle.getName(), data);
    }

    private void saveDataFile() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("保存红绿灯数据文件失败：" + e.getMessage());
        }
    }

    // ---------------------- MySQL存储实现（预留） ----------------------
    private void loadFromMysql() {
        // 后续补充MySQL加载逻辑
        plugin.getLogger().warning("MySQL存储方式暂未实现，自动切换为文件存储！");
        loadFromFile();
    }

    private void saveToMysql() {
        // 后续补充MySQL保存逻辑
        plugin.getLogger().warning("MySQL存储方式暂未实现，自动切换为文件存储！");
        saveToFile();
    }

    private void saveToMysql(TrafficLightEntity tle) {
        // 后续补充单条保存逻辑
        saveToFile(tle);
    }
}