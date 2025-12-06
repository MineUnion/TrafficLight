package com.mineunion.trafficlight.command.subcommands;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.command.TrafficLightCommand;
import com.mineunion.trafficlight.manager.ConfigManager;
import com.mineunion.trafficlight.manager.LanguageManager;
import com.mineunion.trafficlight.manager.TrafficLightManager;
import com.mineunion.trafficlight.util.MessageUtil;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ReloadCommand implements TrafficLightCommand.SubCommand {
    private final TrafficLight plugin;
    private final ConfigManager configManager;
    private final LanguageManager languageManager;
    private final TrafficLightManager lightManager;

    public ReloadCommand(TrafficLight plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.languageManager = plugin.getLanguageManager();
        this.lightManager = plugin.getTrafficLightManager();
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        // 重载权限要求更高（通常是管理员权限）
        return sender.hasPermission("mu.trafficlight.reload") || sender.isOp();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // 权限校验
        if (!hasPermission(sender)) {
            MessageUtil.sendError(sender, "no-permission");
            return;
        }

        try {
            // 重载配置、语言文件、红绿灯数据
            configManager.loadConfig();
            languageManager.loadLanguageFile();
            lightManager.loadAllTrafficLights();

            MessageUtil.sendMessage(sender, "reload-success");
        } catch (Exception e) {
            MessageUtil.sendError(sender, "reload-fail");
            plugin.getLogger().severe("重载失败：" + e.getMessage());
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        // 仅当有重载权限时显示补全（无额外参数，返回空列表）
        return hasPermission(sender) ? List.of() : List.of();
    }
}
