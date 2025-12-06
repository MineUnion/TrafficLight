package com.mineunion.trafficlight.command.subcommands;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.command.TrafficLightCommand;
import com.mineunion.trafficlight.entity.TrafficLightEntity;
import com.mineunion.trafficlight.manager.TrafficLightManager;
import com.mineunion.trafficlight.util.MessageUtil;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

public class SetCommand implements TrafficLightCommand.SubCommand {
    private final TrafficLight plugin;
    private final TrafficLightManager lightManager;

    public SetCommand(TrafficLight plugin) {
        this.plugin = plugin;
        this.lightManager = plugin.getTrafficLightManager();
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("mu.trafficlight.set");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // 权限校验
        if (!hasPermission(sender)) {
            MessageUtil.sendError(sender, "no-permission");
            return;
        }

        // 参数校验（必须传入 ID、状态、持续时间）
        if (args.length < 4) {
            MessageUtil.sendError(sender, "set-usage");
            MessageUtil.sendMessage(sender, "set-state-valid");
            return;
        }

        String id = args[1];
        String stateStr = args[2];
        int duration;

        // 校验持续时间（必须是正整数）
        try {
            duration = Integer.parseInt(args[3]);
            if (duration <= 0) throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) {
            MessageUtil.sendError(sender, "set-duration-valid");
            return;
        }

        // 校验状态（必须是 RED/GREEN/YELLOW）
        TrafficLightEntity.LightState state;
        try {
            state = TrafficLightEntity.LightState.valueOf(stateStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            MessageUtil.sendError(sender, "set-state-valid");
            return;
        }

        // 调用管理器设置持续时间
        boolean success = lightManager.setLightDuration(id, state, duration);

        if (success) {
            MessageUtil.sendMessage(sender, "set-success", 
                    "id", id, 
                    "state", state.name(), 
                    "duration", duration);
        } else {
            MessageUtil.sendError(sender, "set-fail-not-found", "id", id);
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        // 仅当有设置权限时显示补全
        if (!hasPermission(sender)) return List.of();

        // 补全逻辑：
        // args[1] → 红绿灯 ID
        if (args.length == 2) {
            return lightManager.getAllLights().stream()
                    .map(light -> light.getId())
                    .collect(Collectors.toList());
        }
        // args[2] → 状态（RED/GREEN/YELLOW）
        else if (args.length == 3) {
            return List.of("RED", "GREEN", "YELLOW");
        }
        // args[3] → 持续时间（无补全）
        else {
            return List.of();
        }
    }
}
