package com.mineunion.trafficlight.command.subcommands;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.command.TrafficLightCommand;
import com.mineunion.trafficlight.manager.TrafficLightManager;
import com.mineunion.trafficlight.util.MessageUtil;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

public class DeleteCommand implements TrafficLightCommand.SubCommand {
    private final TrafficLight plugin;
    private final TrafficLightManager lightManager;

    public DeleteCommand(TrafficLight plugin) {
        this.plugin = plugin;
        this.lightManager = plugin.getTrafficLightManager();
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("mu.trafficlight.delete");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // 权限校验
        if (!hasPermission(sender)) {
            MessageUtil.sendError(sender, "no-permission");
            return;
        }

        // 参数校验（必须传入 ID）
        if (args.length < 2) {
            MessageUtil.sendError(sender, "delete-usage");
            return;
        }

        String id = args[1];

        // 调用管理器删除红绿灯
        boolean success = lightManager.deleteTrafficLight(id);

        if (success) {
            MessageUtil.sendMessage(sender, "delete-success", "id", id);
        } else {
            MessageUtil.sendError(sender, "delete-fail-not-found", "id", id);
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        // 仅当有删除权限且输入到 args[1] 时，补全已存在的红绿灯 ID
        if (!hasPermission(sender) || args.length != 2) return List.of();
        
        return lightManager.getAllLights().stream()
                .map(light -> light.getId())
                .collect(Collectors.toList());
    }
}
