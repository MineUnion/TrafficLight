package com.mineunion.trafficlight;

import com.mineunion.trafficlight.command.TrafficLightCommand;
import com.mineunion.trafficlight.config.ConfigManager;
import com.mineunion.trafficlight.listener.PlayerProximityListener;
import com.mineunion.trafficlight.listener.PluginReloadListener;
import com.mineunion.trafficlight.manager.GroupManager;
import com.mineunion.trafficlight.manager.PermissionManager;
import com.mineunion.trafficlight.manager.TrafficLightManager;
import com.mineunion.trafficlight.task.LightUpdateTask;
import com.mineunion.trafficlight.task.ProximityCheckTask;
import com.mineunion.trafficlight.util.MessageUtil;
import com.mineunion.trafficlight.util.SerializationUtil;
import org.bukkit.plugin.java.JavaPlugin;

public final class TrafficLight extends JavaPlugin {
    // 插件单例实例（全局可调用）
    private static TrafficLight instance;

    // 核心管理器实例
    private ConfigManager configManager;
    private TrafficLightManager trafficLightManager;
    private GroupManager groupManager;
    private PermissionManager permissionManager;

    /**
     * 插件启用时执行的核心方法
     * 初始化所有组件、注册指令/监听器、启动定时任务
     */
    @Override
    public void onEnable() {
        // 1. 初始化单例
        instance = this;
        getLogger().info("开始初始化TrafficLight插件...");

        // 2. 初始化配置管理器（加载config.yml）
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        getLogger().info("配置文件加载完成！");

        // 3. 初始化工具类（多语言、序列化）
        MessageUtil.init();
        SerializationUtil.registerSerializables();
        getLogger().info("工具类初始化完成！");

        // 4. 初始化核心业务管理器
        permissionManager = new PermissionManager(this); // 权限管理
        trafficLightManager = new TrafficLightManager(this); // 红绿灯管理
        groupManager = new GroupManager(this); // 分组管理
        getLogger().info("核心管理器初始化完成！");

        // 5. 注册事件监听器（玩家距离、插件重载）
        new PlayerProximityListener(this);
        new PluginReloadListener(this);
        getLogger().info("事件监听器注册完成！");

        // 6. 注册指令处理器（/tl 指令）
        new TrafficLightCommand(this);
        getLogger().info("指令注册完成！");

        // 7. 启动定时任务（异步+同步）
        // 距离检测任务（异步执行，避免阻塞主线程）
        int checkInterval = 20 * configManager.getProximityCheckInterval();
        new ProximityCheckTask(this).runTaskTimerAsynchronously(this, 0, checkInterval);
        // 红绿灯状态更新任务（同步执行，保证Bukkit API线程安全）
        new LightUpdateTask(this).runTaskTimer(this, 0, 20);
        getLogger().info("定时任务启动完成！");

        // 启动完成日志
        getLogger().info("========================================");
        getLogger().info("TrafficLight插件启用成功！");
        getLogger().info("版本：" + getDescription().getVersion());
        getLogger().info("作者：" + getDescription().getAuthors());
        getLogger().info("支持指令：/tl (aliases: trafficlight)");
        getLogger().info("========================================");
    }

    /**
     * 插件禁用时执行的方法
     * 保存数据、释放资源
     */
    @Override
    public void onDisable() {
        getLogger().info("开始禁用TrafficLight插件...");

        // 1. 保存所有红绿灯数据（文件/MySQL）
        if (trafficLightManager != null) {
            trafficLightManager.saveAllTrafficLights();
            getLogger().info("红绿灯数据已保存！");
        }

        // 2. 清空单例引用（避免内存泄漏）
        instance = null;

        // 禁用完成日志
        getLogger().info("TrafficLight插件已安全禁用！");
    }

    // ==================== 全局获取方法 ====================
    /**
     * 获取插件单例实例
     * @return TrafficLight实例
     */
    public static TrafficLight getInstance() {
        return instance;
    }

    /**
     * 获取配置管理器
     * @return ConfigManager实例
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * 获取红绿灯管理器
     * @return TrafficLightManager实例
     */
    public TrafficLightManager getTrafficLightManager() {
        return trafficLightManager;
    }

    /**
     * 获取分组管理器
     * @return GroupManager实例
     */
    public GroupManager getGroupManager() {
        return groupManager;
    }

    /**
     * 获取权限管理器
     * @return PermissionManager实例
     */
    public PermissionManager getPermissionManager() {
        return permissionManager;
    }
}