package com.mineunion.trafficlight.command.subcommands;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.command.TrafficLightCommand;
import com.mineunion.trafficlight.config.ConfigManager;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements TrafficLightCommand.SubCommand {
    private final TrafficLight plugin;
    private final ConfigManager configManager;

    public ReloadCommand(TrafficLight plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("mu.trafficlight.reload");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // 重载配置
        configManager.reloadConfig();
        sender.sendMessage("§a配置文件已重载！");
        // 可选：重载红绿灯数据
        plugin.getTrafficLightManager().loadAllTrafficLights();
        sender.sendMessage("§a红绿灯数据已重新加载！");
    }
}