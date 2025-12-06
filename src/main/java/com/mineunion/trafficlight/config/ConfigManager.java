package com.mineunion.trafficlight.config;

import com.mineunion.trafficlight.TrafficLight;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigManager {
    private final TrafficLight plugin;
    private FileConfiguration config;
    private File configFile;

    // 配置键常量
    public static final String DEFAULT_DURATION = "global.default_duration";
    public static final String PROXIMITY_ACTIVATION = "global.proximity_activation";
    public static final String ACTIVATION_RADIUS = "global.activation_radius";
    public static final String LANGUAGE = "global.language";
    public static final String STORAGE_TYPE = "global.storage_type";
    public static final String PROXIMITY_CHECK_INTERVAL = "global.proximity_check_interval";
    public static final String MYSQL_HOST = "mysql.host";
    public static final String MYSQL_PORT = "mysql.port";
    public static final String MYSQL_DATABASE = "mysql.database";
    public static final String MYSQL_USERNAME = "mysql.username";
    public static final String MYSQL_PASSWORD = "mysql.password";
    public static final String MYSQL_POOL_SIZE = "mysql.pool_size";

    public ConfigManager(TrafficLight plugin) {
        this.plugin = plugin;
        // 初始化配置文件
        saveDefaultConfig();
    }

    // 加载配置
    public void loadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        // 加载默认配置
        InputStream defaultStream = plugin.getResource("config.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8)
            );
            config.setDefaults(defaultConfig);
        }
    }

    // 重载配置
    public void reloadConfig() {
        loadConfig();
        plugin.getLogger().info("配置文件已重载！");
    }

    // 保存默认配置
    public void saveDefaultConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
    }

    // 保存配置
    public void saveConfig() {
        if (config == null || configFile == null) {
            return;
        }
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("保存配置文件失败：" + e.getMessage());
        }
    }

    // 获取配置对象
    public FileConfiguration getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }

    // 快捷获取配置值
    public int getDefaultDuration() {
        return getConfig().getInt(DEFAULT_DURATION, 10);
    }

    public boolean isProximityActivation() {
        return getConfig().getBoolean(PROXIMITY_ACTIVATION, true);
    }

    public int getActivationRadius() {
        return getConfig().getInt(ACTIVATION_RADIUS, 50);
    }

    public String getStorageType() {
        return getConfig().getString(STORAGE_TYPE, "file").toLowerCase();
    }

    public int getProximityCheckInterval() {
        return getConfig().getInt(PROXIMITY_CHECK_INTERVAL, 2);
    }

    // MySQL配置快捷获取
    public String getMysqlHost() {
        return getConfig().getString(MYSQL_HOST, "localhost");
    }

    public int getMysqlPort() {
        return getConfig().getInt(MYSQL_PORT, 3306);
    }

    public String getMysqlDatabase() {
        return getConfig().getString(MYSQL_DATABASE, "trafficlight");
    }

    public String getMysqlUsername() {
        return getConfig().getString(MYSQL_USERNAME, "root");
    }

    public String getMysqlPassword() {
        return getConfig().getString(MYSQL_PASSWORD, "123456");
    }

    public int getMysqlPoolSize() {
        return getConfig().getInt(MYSQL_POOL_SIZE, 5);
    }
}