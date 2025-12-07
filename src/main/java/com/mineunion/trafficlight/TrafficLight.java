package com.mineunion.trafficlight;

import com.mineunion.trafficlight.command.TrafficLightCommand;
import com.mineunion.trafficlight.manager.ConfigManager;
import com.mineunion.trafficlight.manager.LanguageManager;
import com.mineunion.trafficlight.manager.TrafficLightManager;
import org.bukkit.plugin.java.JavaPlugin;

public class TrafficLight extends JavaPlugin {
    // 核心管理器
    private ConfigManager configManager;
    private LanguageManager languageManager;
    private TrafficLightManager trafficLightManager;

    @Override
    public void onEnable() {
        // 初始化核心管理器
        this.configManager = new ConfigManager(this);
        this.languageManager = new LanguageManager(this);
        this.trafficLightManager = new TrafficLightManager(this);

        // 注册主命令（修复：直接注册 TrafficLightCommand 实例）
        this.getCommand("trafficlight").setExecutor(new TrafficLightCommand(this));
        this.getCommand("tl").setExecutor(new TrafficLightCommand(this));
        this.getCommand("traffic").setExecutor(new TrafficLightCommand(this));

        // 插件启用提示
        getLogger().info("TrafficLight 插件已成功启用！");
        getLogger().info("版本：" + getDescription().getVersion());
        getLogger().info("作者：" + getDescription().getAuthors());
    }

    @Override
    public void onDisable() {
        // 禁用时保存数据
        if (this.trafficLightManager != null) {
            this.trafficLightManager.saveAllTrafficLights();
        }
        getLogger().info("TrafficLight 插件已成功禁用！");
    }

    // Getter 方法
    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public TrafficLightManager getTrafficLightManager() {
        return trafficLightManager;
    }
}
