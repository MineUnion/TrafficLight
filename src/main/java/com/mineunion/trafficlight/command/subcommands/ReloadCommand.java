package com.mineunion.trafficlight.command.subcommands;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.command.TrafficLightCommand;
import com.mineunion.trafficlight.manager.ConfigManager; // 正确导入
import com.mineunion.trafficlight.manager.LanguageManager;
import com.mineunion.trafficlight.manager.TrafficLightManager;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements TrafficLightCommand.SubCommand {
    // 类内容不变（之前已修复）
    private final TrafficLight plugin;
    private final LanguageManager languageManager;

    public ReloadCommand(TrafficLight plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // 权限校验
        if (!sender.hasPermission("mu.trafficlight.reload") && !sender.isOp()) {
            sender.sendMessage(languageManager.getMessage("no-permission"));
            return true;
        }

        try {
            // 获取核心管理器
            ConfigManager configManager = plugin.getConfigManager();
            TrafficLightManager lightManager = plugin.getTrafficLightManager();

            if (configManager == null || lightManager == null) {
                sender.sendMessage(languageManager.getMessage("reload-failed") + "：核心管理器未初始化");
                return true;
            }

            // 执行重载逻辑
            sender.sendMessage(languageManager.getMessage("reload-start"));

            // 1. 重载配置文件
            configManager.loadConfig();
            // 2. 重载红绿灯数据（清空旧数据+重新加载）
            lightManager.loadAllTrafficLights();
            // 3. 手动保存一次数据，确保同步
            configManager.manualSaveTrafficLights();

            // 重载成功反馈
            sender.sendMessage(languageManager.getMessage("reload-success"));
            plugin.getLogger().info("插件重载成功：配置文件+红绿灯数据已刷新");

            // Debug日志（带开关）
            if (configManager.isDebugMode()) {
                plugin.getLogger().info("[Debug] 插件重载详情：");
                plugin.getLogger().info("[Debug] - 配置文件：已重新加载");
                plugin.getLogger().info("[Debug] - 红绿灯数据：已重新加载 " + lightManager.getAllTrafficLights().size() + " 个红绿灯");
                plugin.getLogger().info("[Debug] - 自动保存：" + (configManager.isAutoSave() ? "已启用（" + configManager.getAutoSaveInterval() / 20 + "秒/次）" : "已禁用"));
                plugin.getLogger().info("[Debug] - Debug模式：" + (configManager.isDebugMode() ? "已启用" : "已禁用"));
            }

        } catch (Exception e) {
            // 重载失败处理
            String errorMsg = e.getMessage() != null ? e.getMessage() : "未知错误";
            sender.sendMessage(languageManager.getMessage("reload-failed") + "：" + errorMsg);
            plugin.getLogger().severe("插件重载失败：" + errorMsg);
            e.printStackTrace();
        }

        return true;
    }
}
