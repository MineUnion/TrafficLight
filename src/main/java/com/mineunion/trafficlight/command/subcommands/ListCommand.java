package com.mineunion.trafficlight.command.subcommands;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.command.TrafficLightCommand;
import com.mineunion.trafficlight.manager.TrafficLightManager;
import com.mineunion.trafficlight.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class ListCommand implements TrafficLightCommand.SubCommand {
    private final TrafficLight plugin;
    private final TrafficLightManager lightManager;

    public ListCommand(TrafficLight plugin) {
        this.plugin = plugin;
        this.lightManager = plugin.getTrafficLightManager();
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("mu.trafficlight.list");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // 权限校验
        if (!hasPermission(sender)) {
            MessageUtil.sendError(sender, "no-permission");
            return;
        }

        // 仅玩家可执行（可选，根据需求调整）
        if (!(sender instanceof Player)) {
            MessageUtil.sendError(sender, "only-player");
            return;
        }

        // 获取所有红绿灯名称
        List<String> lightNames = lightManager.getAllLightNames();
        
        if (lightNames.isEmpty()) {
            MessageUtil.sendMessage(sender, "list-empty");
            return;
        }

        // 发送列表（拼接名称，每行显示5个）
        MessageUtil.sendMessage(sender, "list-title");
        String lightList = lightNames.stream()
                .collect(Collectors.joining(" §8| §7", "§7- ", ""));
        sender.sendMessage(lightList);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
