package com.mineunion.trafficlight.command.subcommands;

import com.mineunion.trafficlight.TrafficLight;
import com.mineunion.trafficlight.command.TrafficLightCommand;
import com.mineunion.trafficlight.entity.TrafficLightEntity;
import com.mineunion.trafficlight.manager.TrafficLightManager;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
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
        // 参数校验
        if (args.length < 3) {
            sender.sendMessage("§c用法：/tl set <名称> <颜色> <时长>");
            sender.sendMessage("§7颜色支持：red/green/yellow（红/绿/黄）");
            return;
        }
        String name = args[0];
        String color = args[1].toLowerCase();
        int duration;

        // 解析时长
        try {
            duration = Integer.parseInt(args[2]);
            if (duration < 1) {
                sender.sendMessage("§c时长必须大于0！");
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§c时长必须是数字！");
            return;
        }

        // 解析颜色
        TrafficLightEntity.LightState state;
        switch (color) {
            case "red":
            case "红":
                state = TrafficLightEntity.LightState.RED;
                break;
            case "green":
            case "绿":
                state = TrafficLightEntity.LightState.GREEN;
                break;
            case "yellow":
            case "黄":
                state = TrafficLightEntity.LightState.YELLOW;
                break;
            default:
                sender.sendMessage("§c无效的颜色！支持：red/green/yellow（红/绿/黄）");
                return;
        }

        // 设置时长
        boolean success = lightManager.setLightDuration(name, state, duration);
        if (success) {
            sender.sendMessage("§a红绿灯" + name + "的" + color + "灯时长已设置为" + duration + "秒！");
        } else {
            sender.sendMessage("§c红绿灯" + name + "不存在！");
        }
    }

    // 指令补全（颜色）
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            List<String> colors = Arrays.asList("red", "green", "yellow", "红", "绿", "黄");
            return colors.stream().filter(c -> c.startsWith(args[1])).collect(Collectors.toList());
        }
        return super.tabComplete(sender, args);
    }
}